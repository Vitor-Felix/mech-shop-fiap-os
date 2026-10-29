package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import com.vitfelix.mech_shop.domain.repository.VehicleProjectionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@Transactional
class VehicleSyncPersistenceTest {
    @Autowired
    private lateinit var service: VehicleSyncService

    @Autowired
    private lateinit var repository: VehicleProjectionRepository

    @Test
    fun `persists newer source versions without resetting availability`() {
        val vehicleId = 987654321L
        val initial = service.synchronize(vehicleId, request(version = 1))
        repository.save(initial.copy(availability = VehicleAvailability.RESERVED))

        val updated = service.synchronize(vehicleId, request(version = 2, price = "130000.00"))
        val stale = service.synchronize(vehicleId, request(version = 1, price = "90000.00"))

        assertEquals(2L, updated.version)
        assertEquals(BigDecimal("130000.00"), updated.price)
        assertEquals(VehicleAvailability.RESERVED, updated.availability)
        assertEquals(updated, stale)
        assertEquals(updated, repository.findById(vehicleId).orElseThrow())
    }

    private fun request(version: Long, price: String = "125000.00") = VehicleSyncRequest(
        brand = "Toyota",
        model = "Corolla",
        year = 2024,
        color = "Prata",
        price = BigDecimal(price),
        version = version
    )
}
