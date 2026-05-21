package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.CustomerRequest
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    private fun sanitizeDocument(document: String): String {
        return document.replace(Regex("[^0-9]"), "")
    }

    fun getAllCustomers() = customerRepository.findAll()

    fun getCustomerById(id: Long) = customerRepository.findByIdOrNull(id)
        ?: throw RuntimeException("Customer not found")

    fun findByDocument(document: String): CustomerEntity? {
        val cleanDocument = sanitizeDocument(document)
        return customerRepository.findByDocument(cleanDocument)
    }

    fun createCustomer(dto: CustomerRequest): CustomerEntity {
        val customerEntity = CustomerEntity(
            name = dto.name,
            document = sanitizeDocument(dto.document)
        )
        return customerRepository.save(customerEntity)
    }

    fun updateCustomer(id: Long, dto: CustomerRequest): CustomerEntity {
        val existing = getCustomerById(id)
        val cleanDocument = sanitizeDocument(dto.document)
        return customerRepository.save(existing.copy(name = dto.name, document = cleanDocument))
    }

    fun deleteCustomer(id: Long) = customerRepository.deleteById(id)
}
