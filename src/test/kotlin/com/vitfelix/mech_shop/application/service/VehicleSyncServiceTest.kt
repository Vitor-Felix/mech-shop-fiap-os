package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import com.vitfelix.mech_shop.domain.model.VehicleProjectionEntity
import com.vitfelix.mech_shop.domain.repository.VehicleProjectionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.math.BigDecimal

class VehicleSyncServiceTest {
    private val repository = mock(VehicleProjectionRepository::class.java)
    private val service = VehicleSyncService(repository)
    private val request = VehicleSyncRequest(
        brand = "Toyota",
        model = "Corolla",
        year = 2024,
        color = "Prata",
        price = BigDecimal("125000.00"),
        version = 2
    )

    @Test
    fun `creates a projection as available when vehicle is first synchronized`() {
        `when`(repository.findByVehicleIdForUpdate(42L)).thenReturn(null)
        `when`(repository.save(any(VehicleProjectionEntity::class.java))).thenAnswer { it.getArgument(0) }

        val result = service.synchronize(42L, request)

        assertEquals(42L, result.vehicleId)
        assertEquals(VehicleAvailability.AVAILABLE, result.availability)
        assertEquals(request.price, result.price)
        assertEquals(2L, result.version)
    }

    @Test
    fun `ignores an older version`() {
        val existing = projection(version = 3)
        `when`(repository.findByVehicleIdForUpdate(42L)).thenReturn(existing)

        val result = service.synchronize(42L, request)

        assertEquals(existing, result)
        verify(repository, never()).save(any(VehicleProjectionEntity::class.java))
    }

    @Test
    fun `ignores a repeated version`() {
        val existing = projection(version = 2)
        `when`(repository.findByVehicleIdForUpdate(42L)).thenReturn(existing)

        val result = service.synchronize(42L, request.copy(price = BigDecimal("99000.00")))

        assertEquals(existing, result)
        verify(repository, never()).save(any(VehicleProjectionEntity::class.java))
    }

    @Test
    fun `applies a newer version without reopening a reserved or sold vehicle`() {
        val existing = projection(version = 1, availability = VehicleAvailability.SOLD)
        `when`(repository.findByVehicleIdForUpdate(42L)).thenReturn(existing)
        `when`(repository.save(any(VehicleProjectionEntity::class.java))).thenAnswer { it.getArgument(0) }

        val result = service.synchronize(42L, request)

        assertEquals("Toyota", result.brand)
        assertEquals(request.price, result.price)
        assertEquals(2L, result.version)
        assertEquals(VehicleAvailability.SOLD, result.availability)
    }

    private fun projection(
        version: Long,
        availability: VehicleAvailability = VehicleAvailability.AVAILABLE
    ) = VehicleProjectionEntity(
        vehicleId = 42L,
        brand = "Ford",
        model = "Focus",
        year = 2020,
        color = "Azul",
        price = BigDecimal("80000.00"),
        version = version,
        availability = availability
    )
}
