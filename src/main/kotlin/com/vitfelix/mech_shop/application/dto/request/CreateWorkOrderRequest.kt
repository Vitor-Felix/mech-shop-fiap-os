package com.vitfelix.mech_shop.application.dto.request

import jakarta.validation.constraints.NotNull

data class CreateWorkOrderRequest(
    @field:NotNull(message = "O ID do veículo é obrigatório")
    val vehicleId: Long,

    val observation: String?
)
