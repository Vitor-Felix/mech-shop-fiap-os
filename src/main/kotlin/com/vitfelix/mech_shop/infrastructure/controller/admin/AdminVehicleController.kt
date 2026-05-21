package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.application.service.VehicleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/vehicles")
class AdminVehicleController(private val service: VehicleService) {
    @GetMapping
    fun getAll() = service.getAllVehicles()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = service.getVehicleById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: VehicleRequest) = service.createVehicle(dto)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody dto: VehicleRequest) = service.updateVehicle(id, dto)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.deleteVehicle(id)
}
