package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.VehicleRequest
import com.vitfelix.mech_shop.application.service.VehicleService
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper

@SpringBootTest
class AdminVehicleControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var vehicleService: VehicleService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val sampleCustomer = CustomerEntity(id = 1L, name = "João Silva", document = "12345678901")

    // Placa "ABC1D23" passa perfeitamente no Regex de validação do DTO
    private val sampleRequest = VehicleRequest(
        licensePlate = "ABC1D23",
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        customerId = 1L
    )

    private val sampleEntity = VehicleEntity(
        id = 1L,
        licensePlate = "ABC1D23",
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        customer = sampleCustomer
    )

    @Test
    @WithMockUser
    fun `should return all vehicles`() {
        `when`(vehicleService.getAllVehicles()).thenReturn(listOf(sampleEntity))

        mockMvc.perform(get("/api/admin/vehicles")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].licensePlate").value("ABC1D23"))
    }

    @Test
    @WithMockUser
    fun `should return vehicle by id`() {
        `when`(vehicleService.getVehicleById(1L)).thenReturn(sampleEntity)

        mockMvc.perform(get("/api/admin/vehicles/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.model").value("Fiesta"))
    }

    @Test
    @WithMockUser
    fun `should create new vehicle`() {
        `when`(vehicleService.createVehicle(sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(post("/api/admin/vehicles")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
    }

    @Test
    @WithMockUser
    fun `should update existing vehicle`() {
        `when`(vehicleService.updateVehicle(1L, sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(put("/api/admin/vehicles/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @WithMockUser
    fun `should delete vehicle by id`() {
        mockMvc.perform(delete("/api/admin/vehicles/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(vehicleService).deleteVehicle(1L)
    }
}
