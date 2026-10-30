package com.vitfelix.mech_shop.domain.repository

import com.vitfelix.mech_shop.domain.model.*
import com.vitfelix.mech_shop.domain.enums.SaleStatus
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CustomerRepository : JpaRepository<CustomerEntity, Long> {
    fun findByDocument(document: String): CustomerEntity?
}

interface VehicleRepository : JpaRepository<VehicleEntity, Long>
interface VehicleProjectionRepository : JpaRepository<VehicleProjectionEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select vehicle from VehicleProjectionEntity vehicle where vehicle.vehicleId = :vehicleId")
    fun findByVehicleIdForUpdate(@Param("vehicleId") vehicleId: Long): VehicleProjectionEntity?

    fun findAllByAvailabilityOrderByPriceAscVehicleIdAsc(
        availability: VehicleAvailability
    ): List<VehicleProjectionEntity>
}

interface SaleRepository : JpaRepository<SaleEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sale from SaleEntity sale where sale.paymentCode = :paymentCode")
    fun findByPaymentCodeForUpdate(@Param("paymentCode") paymentCode: String): SaleEntity?

    fun findFirstByVehicleIdOrderByCreatedAtDescIdDesc(vehicleId: Long): SaleEntity?

    fun findAllByVehicleIdInAndStatus(vehicleIds: Collection<Long>, status: SaleStatus): List<SaleEntity>
}

interface ProcessedPaymentEventRepository : JpaRepository<ProcessedPaymentEventEntity, String>

interface WorkRepository : JpaRepository<WorkEntity, Long>
interface PartRepository : JpaRepository<PartEntity, Long>
interface WorkOrderRepository : JpaRepository<WorkOrderEntity, Long>
