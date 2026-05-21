package com.vitfelix.mech_shop.application.dto.request

import com.vitfelix.mech_shop.infrastructure.validation.CpfOrCnpj
import jakarta.validation.constraints.NotBlank

data class CustomerRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    val name: String,

    @field:NotBlank(message = "O documento é obrigatório")
    @field:CpfOrCnpj
    val document: String
)
