package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.dto.request.PaymentWebhookRequest
import com.vitfelix.mech_shop.application.service.SaleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks/payments")
class PaymentWebhookController(
    private val saleService: SaleService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun process(@Valid @RequestBody request: PaymentWebhookRequest) {
        saleService.processPaymentEvent(request)
    }
}
