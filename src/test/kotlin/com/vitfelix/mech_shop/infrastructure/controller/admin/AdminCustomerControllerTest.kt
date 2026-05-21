package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.CustomerRequest
import com.vitfelix.mech_shop.application.service.CustomerService
import com.vitfelix.mech_shop.domain.model.CustomerEntity
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
class AdminCustomerControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var customerService: CustomerService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    // Ajustado para um CPF matematicamente válido para passar no @CpfOrCnpj
    private val validCpf = "11144477735"
    private val sampleRequest = CustomerRequest(name = "João Silva", document = validCpf)
    private val sampleEntity = CustomerEntity(id = 1L, name = "João Silva", document = validCpf)

    @Test
    @WithMockUser
    fun `should return all customers`() {
        `when`(customerService.getAllCustomers()).thenReturn(listOf(sampleEntity))

        mockMvc.perform(get("/api/admin/customers")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("João Silva"))
    }

    @Test
    @WithMockUser
    fun `should return customer by id`() {
        `when`(customerService.getCustomerById(1L)).thenReturn(sampleEntity)

        mockMvc.perform(get("/api/admin/customers/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("João Silva"))
    }

    @Test
    @WithMockUser
    fun `should create new customer`() {
        `when`(customerService.createCustomer(sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(post("/api/admin/customers")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("João Silva"))
    }

    @Test
    @WithMockUser
    fun `should update existing customer`() {
        `when`(customerService.updateCustomer(1L, sampleRequest)).thenReturn(sampleEntity)

        mockMvc.perform(put("/api/admin/customers/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleRequest)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @WithMockUser
    fun `should delete customer by id`() {
        mockMvc.perform(delete("/api/admin/customers/1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)

        verify(customerService).deleteCustomer(1L)
    }

    @Test
    @WithMockUser
    fun `should return customer by document when found`() {
        `when`(customerService.findByDocument(validCpf)).thenReturn(sampleEntity)

        mockMvc.perform(get("/api/admin/customers/document/$validCpf")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.document").value(validCpf))
    }

    @Test
    @WithMockUser
    fun `should return 404 when customer by document not found`() {
        `when`(customerService.findByDocument("99999999999")).thenReturn(null)

        mockMvc.perform(get("/api/admin/customers/document/99999999999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }
}
