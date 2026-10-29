package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.application.service.VehicleSyncService
import com.vitfelix.mech_shop.domain.model.VehicleProjectionEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@SpringBootTest(properties = ["MECH_SHOP_INTERNAL_TOKEN=local-internal-token"])
class InternalVehicleSyncControllerTest {
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @MockitoBean
    private lateinit var vehicleSyncService: VehicleSyncService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `rejects synchronization when internal token is missing or invalid`() {
        mockMvc.perform(
            put("/internal/vehicles/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest)
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            put("/internal/vehicles/42")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `accepts a valid synchronization request with the configured bearer token`() {
        val syncedRequest = VehicleSyncRequest("Toyota", "Corolla", 2024, "Prata", BigDecimal("125000.00"), 1)
        `when`(vehicleSyncService.synchronize(42L, syncedRequest)).thenReturn(
            VehicleProjectionEntity(
                vehicleId = 42L,
                brand = "Toyota",
                model = "Corolla",
                year = 2024,
                color = "Prata",
                price = BigDecimal("125000.00"),
                version = 1
            )
        )

        mockMvc.perform(
            put("/internal/vehicles/42")
                .header("Authorization", "Bearer local-internal-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.vehicleId").value(42))
            .andExpect(jsonPath("$.availability").value("AVAILABLE"))
    }

    @Test
    fun `rejects invalid synchronization payload`() {
        mockMvc.perform(
            put("/internal/vehicles/42")
                .header("Authorization", "Bearer local-internal-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"brand":"","model":"Corolla","year":2024,"color":"Prata","price":125000.00,"version":1}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `rejects a price with more than ten integer digits or two decimal places`() {
        val tooManyIntegerDigits =
            """{"brand":"Toyota","model":"Corolla","year":2024,"color":"Prata","price":10000000000.00,"version":1}"""
        val tooManyFractionDigits =
            """{"brand":"Toyota","model":"Corolla","year":2024,"color":"Prata","price":125000.001,"version":1}"""

        listOf(tooManyIntegerDigits, tooManyFractionDigits).forEach { payload ->
            mockMvc.perform(
                put("/internal/vehicles/42")
                    .header("Authorization", "Bearer local-internal-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payload)
            ).andExpect(status().isBadRequest)
        }
    }

    @Test
    fun `rejects a non-positive vehicle id`() {
        mockMvc.perform(
            put("/internal/vehicles/0")
                .header("Authorization", "Bearer local-internal-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest)
        ).andExpect(status().isBadRequest)
    }

    private val validRequest =
        """{"brand":"Toyota","model":"Corolla","year":2024,"color":"Prata","price":125000.00,"version":1}"""
}
