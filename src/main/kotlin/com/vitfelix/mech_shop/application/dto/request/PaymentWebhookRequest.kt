package com.vitfelix.mech_shop.application.dto.request

import com.vitfelix.mech_shop.domain.enums.PaymentEventStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PaymentWebhookRequest(
    @field:NotBlank
    @field:Size(max = 128)
    val eventId: String,

    @field:NotBlank
    @field:Size(max = 64)
    val paymentCode: String,

    val status: PaymentEventStatus
)
