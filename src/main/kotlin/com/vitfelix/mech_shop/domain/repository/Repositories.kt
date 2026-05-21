package com.vitfelix.mech_shop.domain.repository

import com.vitfelix.mech_shop.domain.model.*
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<CustomerEntity, Long> {
    fun findByDocument(document: String): CustomerEntity?
}

interface VehicleRepository : JpaRepository<VehicleEntity, Long>
interface WorkRepository : JpaRepository<WorkEntity, Long>
interface PartRepository : JpaRepository<PartEntity, Long>
interface WorkOrderRepository : JpaRepository<WorkOrderEntity, Long>
