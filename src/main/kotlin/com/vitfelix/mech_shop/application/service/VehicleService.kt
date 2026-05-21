package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import com.vitfelix.mech_shop.domain.repository.VehicleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class VehicleService(
    private val vehicleRepository: VehicleRepository,
    private val customerRepository: CustomerRepository
) {
    fun getAllVehicles() = vehicleRepository.findAll()

    fun getVehicleById(id: Long) = vehicleRepository.findByIdOrNull(id)
        ?: throw RuntimeException("Vehicle not found")

    fun createVehicle(dto: VehicleRequest): VehicleEntity {
        val customer = customerRepository.findByIdOrNull(dto.customerId)
            ?: throw RuntimeException("Customer not found with id: ${dto.customerId}")

        val vehicle = VehicleEntity(
            licensePlate = dto.licensePlate,
            brand = dto.brand,
            model = dto.model,
            year = dto.year,
            customer = customer
        )
        return vehicleRepository.save(vehicle)
    }

    fun updateVehicle(id: Long, dto: VehicleRequest): VehicleEntity {
        val existing = getVehicleById(id)
        val customer = customerRepository.findByIdOrNull(dto.customerId)
            ?: throw RuntimeException("Customer not found with id: ${dto.customerId}")

        return vehicleRepository.save(
            existing.copy(
                licensePlate = dto.licensePlate,
                brand = dto.brand,
                model = dto.model,
                year = dto.year,
                customer = customer
            )
        )
    }

    fun deleteVehicle(id: Long) = vehicleRepository.deleteById(id)
}
