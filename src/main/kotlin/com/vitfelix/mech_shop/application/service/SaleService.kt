package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.PaymentWebhookRequest
import com.vitfelix.mech_shop.application.dto.request.PurchaseRequest
import com.vitfelix.mech_shop.application.dto.response.AvailableVehicleResponse
import com.vitfelix.mech_shop.application.dto.response.PurchaseResponse
import com.vitfelix.mech_shop.application.dto.response.SoldVehicleResponse
import com.vitfelix.mech_shop.application.dto.response.VehicleSaleStatusResponse
import com.vitfelix.mech_shop.domain.enums.PaymentEventStatus
import com.vitfelix.mech_shop.domain.enums.SaleStatus
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import com.vitfelix.mech_shop.domain.model.ProcessedPaymentEventEntity
import com.vitfelix.mech_shop.domain.model.SaleEntity
import com.vitfelix.mech_shop.domain.repository.ProcessedPaymentEventRepository
import com.vitfelix.mech_shop.domain.repository.SaleRepository
import com.vitfelix.mech_shop.domain.repository.VehicleProjectionRepository
import com.vitfelix.mech_shop.infrastructure.validation.CpfCnpjValidator
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

class SaleNotFoundException(message: String) : RuntimeException(message)
class SaleConflictException(message: String) : RuntimeException(message)

@Service
class SaleService(
    private val vehicleRepository: VehicleProjectionRepository,
    private val saleRepository: SaleRepository,
    private val paymentEventRepository: ProcessedPaymentEventRepository
) {
    fun getVehicleSaleStatus(vehicleId: Long): VehicleSaleStatusResponse {
        val vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow { SaleNotFoundException("Vehicle not found") }
        val latestSale = saleRepository.findFirstByVehicleIdOrderByCreatedAtDescIdDesc(vehicleId)

        return VehicleSaleStatusResponse(
            vehicleId = vehicle.vehicleId,
            availability = vehicle.availability,
            saleStatus = latestSale?.status,
            paymentCode = latestSale?.paymentCode,
            createdAt = latestSale?.createdAt,
            soldAt = latestSale?.soldAt
        )
    }

    fun getAvailableVehicles() =
        vehicleRepository.findAllByAvailabilityOrderByPriceAscVehicleIdAsc(VehicleAvailability.AVAILABLE)
            .map {
                AvailableVehicleResponse(
                    vehicleId = it.vehicleId,
                    brand = it.brand,
                    model = it.model,
                    year = it.year,
                    color = it.color,
                    price = it.price
                )
            }

    fun getSoldVehicles(): List<SoldVehicleResponse> {
        val vehicles = vehicleRepository.findAllByAvailabilityOrderByPriceAscVehicleIdAsc(VehicleAvailability.SOLD)
        if (vehicles.isEmpty()) return emptyList()

        val soldAtByVehicleId = saleRepository
            .findAllByVehicleIdInAndStatus(vehicles.map { it.vehicleId }, SaleStatus.PAID)
            .associate { sale ->
                sale.vehicleId to requireNotNull(sale.soldAt) { "Paid sale must have soldAt" }
            }

        return vehicles.map { vehicle ->
            SoldVehicleResponse(
                vehicleId = vehicle.vehicleId,
                brand = vehicle.brand,
                model = vehicle.model,
                year = vehicle.year,
                color = vehicle.color,
                price = vehicle.price,
                soldAt = requireNotNull(soldAtByVehicleId[vehicle.vehicleId]) {
                    "Sold vehicle must have an approved sale"
                }
            )
        }
    }

    @Transactional
    fun purchase(vehicleId: Long, request: PurchaseRequest): PurchaseResponse {
        val normalizedCpf = request.buyerCpf.filter { it in '0'..'9' }
        if (normalizedCpf.length != 11 || !CpfCnpjValidator().isValid(normalizedCpf, null)) {
            throw IllegalArgumentException("buyerCpf must be a valid CPF")
        }

        val vehicle = vehicleRepository.findByVehicleIdForUpdate(vehicleId)
            ?: throw SaleNotFoundException("Vehicle not found")
        if (vehicle.availability != VehicleAvailability.AVAILABLE) {
            throw SaleConflictException("Vehicle is not available")
        }

        val paymentCode = generatePaymentCode()
        val sale = saleRepository.save(
            SaleEntity(
                vehicleId = vehicle.vehicleId,
                buyerCpf = normalizedCpf,
                paymentCode = paymentCode,
                priceSnapshot = vehicle.price,
                createdAt = Instant.now()
            )
        )
        vehicleRepository.save(vehicle.copy(availability = VehicleAvailability.RESERVED))

        return PurchaseResponse(
            saleId = requireNotNull(sale.id),
            vehicleId = sale.vehicleId,
            paymentCode = sale.paymentCode,
            status = sale.status,
            soldAt = sale.soldAt
        )
    }

    @Transactional
    fun processPaymentEvent(request: PaymentWebhookRequest) {
        val priorEvent = paymentEventRepository.findByIdOrNull(request.eventId)
        if (priorEvent != null) {
            ensureDuplicateMatches(priorEvent, request)
            return
        }

        val sale = saleRepository.findByPaymentCodeForUpdate(request.paymentCode)
            ?: throw SaleNotFoundException("Payment code not found")

        val concurrentDuplicate = paymentEventRepository.findByIdOrNull(request.eventId)
        if (concurrentDuplicate != null) {
            ensureDuplicateMatches(concurrentDuplicate, request)
            return
        }

        if (sale.status != SaleStatus.PENDING_PAYMENT) {
            throw SaleConflictException("Payment event conflicts with the sale state")
        }

        val vehicle = vehicleRepository.findByVehicleIdForUpdate(sale.vehicleId)
            ?: throw SaleNotFoundException("Vehicle not found")
        if (vehicle.availability != VehicleAvailability.RESERVED) {
            throw SaleConflictException("Vehicle is not reserved by this sale")
        }

        val processedAt = Instant.now()
        when (request.status) {
            PaymentEventStatus.APPROVED -> {
                saleRepository.save(sale.copy(status = SaleStatus.PAID, soldAt = processedAt))
                vehicleRepository.save(vehicle.copy(availability = VehicleAvailability.SOLD))
            }
            PaymentEventStatus.CANCELED -> {
                saleRepository.save(sale.copy(status = SaleStatus.CANCELED))
                vehicleRepository.save(vehicle.copy(availability = VehicleAvailability.AVAILABLE))
            }
        }
        paymentEventRepository.save(
            ProcessedPaymentEventEntity(
                eventId = request.eventId,
                paymentCode = request.paymentCode,
                status = request.status,
                processedAt = processedAt
            )
        )
    }

    private fun ensureDuplicateMatches(
        priorEvent: ProcessedPaymentEventEntity,
        request: PaymentWebhookRequest
    ) {
        if (priorEvent.paymentCode != request.paymentCode || priorEvent.status != request.status) {
            throw SaleConflictException("Event ID was already used for a different payment event")
        }
    }

    private fun generatePaymentCode(): String {
        val bytes = ByteArray(PAYMENT_CODE_BYTES)
        secureRandom.nextBytes(bytes)
        return "pay-${Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)}"
    }

    companion object {
        private const val PAYMENT_CODE_BYTES = 32
        private val secureRandom = SecureRandom()
    }
}
