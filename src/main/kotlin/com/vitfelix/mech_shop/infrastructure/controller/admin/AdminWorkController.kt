package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.WorkService
import com.vitfelix.mech_shop.domain.model.WorkEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/works")
class AdminWorkController(private val adminService: WorkService) {
    @GetMapping
    fun getAll() = ResponseEntity.ok(adminService.getAllWorks())

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = ResponseEntity.ok(adminService.getWorkById(id))

    @PostMapping
    fun create(@Valid @RequestBody work: WorkEntity) = ResponseEntity.status(HttpStatus.CREATED).body(adminService.createWork(work))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody work: WorkEntity) = ResponseEntity.ok(adminService.updateWork(id, work))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        adminService.deleteWork(id)
        return ResponseEntity.noContent().build()
    }
}
