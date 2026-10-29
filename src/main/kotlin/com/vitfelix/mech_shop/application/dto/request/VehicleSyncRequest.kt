package com.vitfelix.mech_shop.application.dto.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class VehicleSyncRequest(
    @field:NotBlank
    val brand: String,

    @field:NotBlank
    val model: String,

    @field:Min(1886)
    @field:Max(2100)
    val year: Int,

    @field:NotBlank
    val color: String,

    @field:DecimalMin("0.01")
    @field:Digits(integer = 10, fraction = 2)
    val price: BigDecimal,

    @field:Positive
    val version: Long
)
