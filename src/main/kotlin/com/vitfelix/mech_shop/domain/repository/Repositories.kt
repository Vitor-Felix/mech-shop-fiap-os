package com.vitfelix.mech_shop.domain.repository

import com.vitfelix.mech_shop.domain.model.*
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
}

interface WorkRepository : JpaRepository<WorkEntity, Long>
interface PartRepository : JpaRepository<PartEntity, Long>
interface WorkOrderRepository : JpaRepository<WorkOrderEntity, Long>
