package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.WorkService
import com.vitfelix.mech_shop.domain.model.WorkEntity
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
import java.math.BigDecimal

@SpringBootTest
class AdminWorkControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var workService: WorkService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val sampleWork = WorkEntity(
        id = 1L,
        description = "Alinhamento e Balanceamento",
        amount = BigDecimal("120.00")
    )

    @Test
    @WithMockUser
    fun `should return all works`() {
        `when`(workService.getAllWorks()).thenReturn(listOf(sampleWork))

        mockMvc.perform(get("/api/admin/works")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].description").value("Alinhamento e Balanceamento"))
            .andExpect(jsonPath("$[0].amount").value(120.00))
    }

    @Test
    @WithMockUser
    fun `should return work by id`() {
        `when`(workService.getWorkById(1L)).thenReturn(sampleWork)

        mockMvc.perform(get("/api/admin/works/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Alinhamento e Balanceamento"))
    }

    @Test
    @WithMockUser
    fun `should create new work`() {
        // Blindado: enviando o objeto real sem os matchers do Mockito
        `when`(workService.createWork(sampleWork)).thenReturn(sampleWork)

        mockMvc.perform(post("/api/admin/works")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleWork)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Alinhamento e Balanceamento"))
    }

    @Test
    @WithMockUser
    fun `should update existing work`() {
        // Blindado: ID e Objeto concretos para evitar NullPointerException do Kotlin
        `when`(workService.updateWork(1L, sampleWork)).thenReturn(sampleWork)

        mockMvc.perform(put("/api/admin/works/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleWork)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Alinhamento e Balanceamento"))
    }

    @Test
    @WithMockUser
    fun `should delete work by id`() {
        mockMvc.perform(delete("/api/admin/works/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(workService).deleteWork(1L)
    }
}
