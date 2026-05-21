package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.PartService
import com.vitfelix.mech_shop.domain.model.PartEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
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
import java.math.BigDecimal

@SpringBootTest
class AdminPartControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var partService: PartService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val samplePart = PartEntity(
        id = 1L,
        name = "Pastilha de Freio",
        unitPrice = BigDecimal("150.00"),
        stockQuantity = 10
    )

    @Test
    @WithMockUser
    fun `should return all parts`() {
        `when`(partService.getAllParts()).thenReturn(listOf(samplePart))

        mockMvc.perform(get("/api/admin/parts")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Pastilha de Freio"))
            .andExpect(jsonPath("$[0].stockQuantity").value(10))
    }

    @Test
    @WithMockUser
    fun `should return part by id`() {
        `when`(partService.getPartById(1L)).thenReturn(samplePart)

        mockMvc.perform(get("/api/admin/parts/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Pastilha de Freio"))
    }

    @Test
    @WithMockUser
    fun `should create new part`() {
        // Trocado any() por samplePart diretamente
        `when`(partService.createPart(samplePart)).thenReturn(samplePart)

        mockMvc.perform(post("/api/admin/parts")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(samplePart)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Pastilha de Freio"))
    }

    @Test
    @WithMockUser
    fun `should update existing part`() {
        // Trocado eq(1L) e any() por valores brutos (regradov do Mockito: ou tudo é matcher, ou nada é)
        `when`(partService.updatePart(1L, samplePart)).thenReturn(samplePart)

        mockMvc.perform(put("/api/admin/parts/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(samplePart)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @WithMockUser
    fun `should delete part by id`() {
        mockMvc.perform(delete("/api/admin/parts/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(partService).deletePart(1L)
    }
}
