package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.SaleService
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/admin/vehicles")
class AdminVehicleSalesController(
    private val saleService: SaleService
) {
    @GetMapping("/{vehicleId}/sale-status")
    fun getSaleStatus(@PathVariable @Positive vehicleId: Long) =
        saleService.getVehicleSaleStatus(vehicleId)
}
