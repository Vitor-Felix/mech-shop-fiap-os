package com.vitfelix.mech_shop.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class VehicleRequest(
    @field:NotBlank(message = "A placa é obrigatória")
    @field:Pattern(
        regexp = "^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$",
        message = "Formato de placa inválido. Padrões aceitos: Mercosul ou Nacional"
    )
    val licensePlate: String,

    @field:NotBlank(message = "A marca é obrigatória")
    val brand: String,

    @field:NotBlank(message = "O modelo é obrigatório")
    val model: String,

    @field:NotNull(message = "O ano é obrigatório")
    val year: Int,

    @field:NotNull(message = "O ID do cliente é obrigatório")
    val customerId: Long
)
