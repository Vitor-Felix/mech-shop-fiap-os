package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.dto.request.PurchaseRequest
import com.vitfelix.mech_shop.application.service.SaleService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/vehicles")
class VehicleListingController(
    private val saleService: SaleService
) {
    @GetMapping("/available")
    fun available() = saleService.getAvailableVehicles()

    @GetMapping("/sold")
    fun sold() = saleService.getSoldVehicles()

    @PostMapping("/{vehicleId}/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    fun purchase(
        @PathVariable @Positive vehicleId: Long,
        @Valid @RequestBody request: PurchaseRequest
    ) = saleService.purchase(vehicleId, request)
}
