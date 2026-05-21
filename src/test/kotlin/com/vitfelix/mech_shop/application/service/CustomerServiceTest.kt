package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.CustomerRequest
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class CustomerServiceTest {

    private val customerRepository: CustomerRepository = mock(CustomerRepository::class.java)
    private val customerService = CustomerService(customerRepository)

    @Test
    fun `should return all customers`() {
        val customer = CustomerEntity(id = 1L, name = "João Silva", document = "12345678901")
        `when`(customerRepository.findAll()).thenReturn(listOf(customer))

        val result = customerService.getAllCustomers()

        assertEquals(1, result.size)
        assertEquals("João Silva", result[0].name)
    }

    @Test
    fun `should return customer by id when exists`() {
        val customer = CustomerEntity(id = 1L, name = "João Silva", document = "12345678901")
        `when`(customerRepository.findById(1L)).thenReturn(Optional.of(customer))

        val result = customerService.getCustomerById(1L)

        assertNotNull(result)
        assertEquals(1L, result.id)
    }

    @Test
    fun `should throw exception when customer id does not exist`() {
        `when`(customerRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            customerService.getCustomerById(1L)
        }
        assertEquals("Customer not found", exception.message)
    }

    @Test
    fun `should find customer by document after sanitizing it`() {
        val customer = CustomerEntity(id = 1L, name = "João Silva", document = "12345678901")
        `when`(customerRepository.findByDocument("12345678901")).thenReturn(customer)

        val result = customerService.findByDocument("123.456.789-01")

        assertNotNull(result)
        assertEquals("João Silva", result?.name)
    }

    @Test
    fun `should create customer sanitizing document`() {
        val dto = CustomerRequest("João Silva", "123.456.789-01")
        val savedCustomer = CustomerEntity(id = 1L, name = "João Silva", document = "12345678901")
        `when`(customerRepository.save(any(CustomerEntity::class.java))).thenReturn(savedCustomer)

        val result = customerService.createCustomer(dto)

        assertEquals("12345678901", result.document)
        assertEquals("João Silva", result.name)
    }

    @Test
    fun `should update customer sanitizing document`() {
        val existingCustomer = CustomerEntity(id = 1L, name = "João Antigo", document = "11111111111")
        val dto = CustomerRequest("João Novo", "222.222.222-22")
        val updatedCustomer = CustomerEntity(id = 1L, name = "João Novo", document = "22222222222")

        `when`(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer))
        `when`(customerRepository.save(any(CustomerEntity::class.java))).thenReturn(updatedCustomer)

        val result = customerService.updateCustomer(1L, dto)

        assertEquals("João Novo", result.name)
        assertEquals("22222222222", result.document)
    }

    @Test
    fun `should delete customer successfully`() {
        doNothing().`when`(customerRepository).deleteById(1L)

        assertDoesNotThrow { customerService.deleteCustomer(1L) }
        verify(customerRepository, times(1)).deleteById(1L)
    }
}
