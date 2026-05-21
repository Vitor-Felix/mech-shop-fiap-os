package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.CreateWorkOrderRequest
import com.vitfelix.mech_shop.application.dto.request.UpdateWorkOrderItemsRequest
import com.vitfelix.mech_shop.application.dto.response.WorkOrderResponse
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal

@SpringBootTest
class AdminWorkOrderControllerTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var workOrderService: WorkOrderService

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    private val customer = CustomerEntity(id = 1L, name = "João Silva", document = "11144477735")

    // Corrigido: adicionados os parâmetros 'brand' e 'year'
    private val vehicle = VehicleEntity(
        id = 1L,
        licensePlate = "ABC1234",
        brand = "Volkswagen",
        model = "Gol",
        year = 2022,
        customer = customer
    )

    private val sampleOrder = WorkOrderEntity(
        id = 1L,
        customer = customer,
        vehicle = vehicle,
        observation = "Barulho na suspensão",
        status = WorkOrderStatusEnum.RECEIVED,
        totalAmount = BigDecimal.ZERO
    )

    @Test
    @WithMockUser
    fun `should create work order`() {
        val request = CreateWorkOrderRequest(vehicleId = 1L, observation = "Barulho na suspensão")
        `when`(workOrderService.createWorkOrder(request)).thenReturn(sampleOrder)

        mockMvc.perform(post("/api/admin/work-orders")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("RECEIVED"))
    }

    @Test
    @WithMockUser
    fun `should update items and generate budget`() {
        val request = UpdateWorkOrderItemsRequest(serviceIds = listOf(1L), partIds = listOf(2L))
        val budgetedOrder = sampleOrder.copy(
            status = WorkOrderStatusEnum.WAITING_APPROVAL,
            totalAmount = BigDecimal("250.00")
        )
        `when`(workOrderService.updateItemsAndGenerateBudget(1L, request)).thenReturn(budgetedOrder)

        mockMvc.perform(put("/api/admin/work-orders/1/items")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("WAITING_APPROVAL"))
            .andExpect(jsonPath("$.totalAmount").value(250.00))
    }

    @Test
    @WithMockUser
    fun `should list all work orders`() {
        // Corrigido: alterado de 'vehicleLicensePlate' para 'vehiclePlate'
        val responseDto = WorkOrderResponse(
            id = 1L,
            customerName = "João Silva",
            vehiclePlate = "ABC1234",
            status = WorkOrderStatusEnum.RECEIVED,
            totalAmount = BigDecimal.ZERO,
            startedAt = null,
            finishedAt = null
        )
        `when`(workOrderService.getAllWorkOrders()).thenReturn(listOf(responseDto))

        mockMvc.perform(get("/api/admin/work-orders")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].customerName").value("João Silva"))
    }

    @Test
    @WithMockUser
    fun `should update status via patch mapping`() {
        val executedOrder = sampleOrder.copy(status = WorkOrderStatusEnum.FINISHED)
        `when`(workOrderService.updateStatus(1L, WorkOrderStatusEnum.FINISHED)).thenReturn(executedOrder)

        mockMvc.perform(patch("/api/admin/work-orders/1/status")
            .with(csrf())
            .param("status", "FINISHED")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("FINISHED"))
    }
}
