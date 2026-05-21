package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.service.WorkOrderService
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.CustomerEntity
import com.vitfelix.mech_shop.domain.model.VehicleEntity
import com.vitfelix.mech_shop.domain.model.WorkOrderEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class WorkOrderControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    // Nova anotação padrão do Spring Boot 4.0+ para substituir o @MockBean
    @MockitoBean
    private lateinit var workOrderService: WorkOrderService

    @BeforeEach
    fun setup() {
        // Agora podemos encadear tudo de novo, passando o tipo correto para o Kotlin não se perder
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val sampleCustomer = CustomerEntity(id = 1L, name = "João", document = "12345678901")
    private val sampleVehicle = VehicleEntity(
        id = 1L,
        licensePlate = "ABC1D23",
        brand = "Ford",
        model = "Fiesta",
        year = 2020,
        customer = sampleCustomer
    )

    @Test
    @WithMockUser
    fun `should return work order when found by id`() {
        val mockOrder = WorkOrderEntity(
            id = 1L,
            customer = sampleCustomer,
            vehicle = sampleVehicle,
            observation = "Troca de óleo",
            status = WorkOrderStatusEnum.RECEIVED
        )

        `when`(workOrderService.getWorkOrderById(1L)).thenReturn(mockOrder)

        mockMvc.perform(get("/api/work-orders/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.observation").value("Troca de óleo"))
            .andExpect(jsonPath("$.status").value("RECEIVED"))
    }

    @Test
    @WithMockUser
    fun `should approve work order successfully`() {
        val approvedOrder = WorkOrderEntity(
            id = 1L,
            customer = sampleCustomer,
            vehicle = sampleVehicle,
            observation = "Troca de óleo",
            status = WorkOrderStatusEnum.IN_EXECUTION
        )

        `when`(workOrderService.approveWorkOrder(1L)).thenReturn(approvedOrder)

        mockMvc.perform(post("/api/work-orders/1/approve")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("IN_EXECUTION"))
    }
}
