package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.application.service.VehicleSyncService
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/internal/vehicles")
class InternalVehicleSyncController(
    private val vehicleSyncService: VehicleSyncService
) {
    @PutMapping("/{vehicleId}")
    fun synchronize(
        @PathVariable @Positive vehicleId: Long,
        @Valid @RequestBody request: VehicleSyncRequest
    ) = vehicleSyncService.synchronize(vehicleId, request)
}
