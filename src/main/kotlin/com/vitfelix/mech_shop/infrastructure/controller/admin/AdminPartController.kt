package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.PartService
import com.vitfelix.mech_shop.domain.model.PartEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/parts")
class AdminPartController(private val adminService: PartService) {
    @GetMapping
    fun getAll() = ResponseEntity.ok(adminService.getAllParts())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = ResponseEntity.ok(adminService.getPartById(id))

    @PostMapping
    fun create(@Valid @RequestBody part: PartEntity) = ResponseEntity.status(HttpStatus.CREATED).body(adminService.createPart(part))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody part: PartEntity) = ResponseEntity.ok(adminService.updatePart(id, part))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminService.deletePart(id)
        return ResponseEntity.noContent().build()
    }
}
