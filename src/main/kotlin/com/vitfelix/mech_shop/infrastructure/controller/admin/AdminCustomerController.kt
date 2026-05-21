package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.CustomerRequest
import com.vitfelix.mech_shop.application.service.CustomerService
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/customers")
class AdminCustomerController(private val customerService: CustomerService) {
    @GetMapping
    fun getAll() = customerService.getAllCustomers()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = customerService.getCustomerById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody dto: CustomerRequest) = customerService.createCustomer(dto)

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody dto: CustomerRequest
    ) = customerService.updateCustomer(id, dto)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = customerService.deleteCustomer(id)

    @GetMapping("/document/{document}")
    fun getByDocument(@PathVariable document: String): ResponseEntity<CustomerEntity> {
        val customer = customerService.findByDocument(document)
        return if (customer != null) ResponseEntity.ok(customer) else ResponseEntity.notFound().build()
    }
}
