package com.vitfelix.mech_shop.application.dto.response

import java.math.BigDecimal
import java.time.Instant

data class AvailableVehicleResponse(
    val vehicleId: Long,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: BigDecimal
)

data class SoldVehicleResponse(
    val vehicleId: Long,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
    val price: BigDecimal,
    val soldAt: Instant
)
