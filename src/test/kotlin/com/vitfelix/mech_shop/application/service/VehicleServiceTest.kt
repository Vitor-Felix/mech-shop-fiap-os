package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.repository.CustomerRepository
import com.vitfelix.mech_shop.domain.repository.VehicleRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.*

class VehicleServiceTest {

    private val vehicleRepository: VehicleRepository = mock(VehicleRepository::class.java)
    private val customerRepository: CustomerRepository = mock(CustomerRepository::class.java)
    private val vehicleService = VehicleService(vehicleRepository, customerRepository)

    private val sampleCustomer = CustomerEntity(1L, "João Silva", "12345678901")

    @Test
    fun `should return all vehicles`() {
        val vehicle = VehicleEntity(1L, "ABC1D23", "Ford", "Fiesta", 2020, sampleCustomer)
        `when`(vehicleRepository.findAll()).thenReturn(listOf(vehicle))

        val result = vehicleService.getAllVehicles()

        assertEquals(1, result.size)
        assertEquals("ABC1D23", result[0].licensePlate)
    }

    @Test
    fun `should return vehicle by id when exists`() {
        val vehicle = VehicleEntity(1L, "ABC1D23", "Ford", "Fiesta", 2020, sampleCustomer)
        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle))

        val result = vehicleService.getVehicleById(1L)

        assertNotNull(result)
        assertEquals(1L, result.id)
    }

    @Test
    fun `should throw exception when vehicle id does not exist`() {
        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            vehicleService.getVehicleById(1L)
        }
        assertEquals("Vehicle not found", exception.message)
    }

    @Test
    fun `should create vehicle successfully`() {
        val dto = VehicleRequest("ABC1D23", "Ford", "Fiesta", 2020, 1L)
        val vehicle = VehicleEntity(1L, "ABC1D23", "Ford", "Fiesta", 2020, sampleCustomer)

        `when`(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer))
        `when`(vehicleRepository.save(any(VehicleEntity::class.java))).thenReturn(vehicle)

        val result = vehicleService.createVehicle(dto)

        assertEquals("ABC1D23", result.licensePlate)
        assertEquals(1L, result.customer.id)
    }

    @Test
    fun `should throw exception when creating vehicle with non-existent customer`() {
        val dto = VehicleRequest("ABC1D23", "Ford", "Fiesta", 2020, 99L)
        `when`(customerRepository.findById(99L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            vehicleService.createVehicle(dto)
        }
        assertEquals("Customer not found with id: 99", exception.message)
    }

    @Test
    fun `should update vehicle successfully`() {
        val existingVehicle = VehicleEntity(1L, "OLD1234", "Ford", "Fiesta", 2018, sampleCustomer)
        val dto = VehicleRequest("NEW1234", "Chevrolet", "Onix", 2022, 1L)
        val updatedVehicle = VehicleEntity(1L, "NEW1234", "Chevrolet", "Onix", 2022, sampleCustomer)

        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle))
        `when`(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer))
        `when`(vehicleRepository.save(any(VehicleEntity::class.java))).thenReturn(updatedVehicle)

        val result = vehicleService.updateVehicle(1L, dto)

        assertEquals("NEW1234", result.licensePlate)
        assertEquals("Chevrolet", result.brand)
        assertEquals("Onix", result.model)
    }

    @Test
    fun `should delete vehicle successfully`() {
        doNothing().`when`(vehicleRepository).deleteById(1L)

        assertDoesNotThrow { vehicleService.deleteVehicle(1L) }
        verify(vehicleRepository, times(1)).deleteById(1L)
    }
}
