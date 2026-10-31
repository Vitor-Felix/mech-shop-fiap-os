package com.vitfelix.mech_shop.application.dto.response

import com.vitfelix.mech_shop.domain.enums.SaleStatus
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import java.time.Instant

data class VehicleSaleStatusResponse(
    val vehicleId: Long,
    val availability: VehicleAvailability,
    val saleStatus: SaleStatus?,
    val paymentCode: String?,
    val createdAt: Instant?,
    val soldAt: Instant?
)
