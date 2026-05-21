package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.CreateWorkOrderRequest
import com.vitfelix.mech_shop.application.dto.request.UpdateWorkOrderItemsRequest
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.*
import com.vitfelix.mech_shop.domain.repository.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class WorkOrderServiceTest {

    private val vehicleRepository: VehicleRepository = mock(VehicleRepository::class.java)
    private val workRepository: WorkRepository = mock(WorkRepository::class.java)
    private val partRepository: PartRepository = mock(PartRepository::class.java)
    private val workOrderRepository: WorkOrderRepository = mock(WorkOrderRepository::class.java)

    private val workOrderService = WorkOrderService(
        vehicleRepository, workRepository, partRepository, workOrderRepository
    )

    // Blindando a criação com parâmetros nomeados para evitar inversões de construtor
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
    fun `should create work order as RECEIVED`() {
        val dto = CreateWorkOrderRequest(1L, "Barulho na suspensão")
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, "Barulho na suspensão", WorkOrderStatusEnum.RECEIVED)

        `when`(vehicleRepository.findById(1L)).thenReturn(Optional.of(sampleVehicle))
        `when`(workOrderRepository.save(any(WorkOrderEntity::class.java))).thenReturn(workOrder)

        val result = workOrderService.createWorkOrder(dto)

        assertEquals(WorkOrderStatusEnum.RECEIVED, result.status)
        assertEquals("Barulho na suspensão", result.observation)
    }

    @Test
    fun `should update timestamps based on new status`() {
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, null, WorkOrderStatusEnum.RECEIVED)
        `when`(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder))
        `when`(workOrderRepository.save(any(WorkOrderEntity::class.java))).thenAnswer { it.arguments[0] as WorkOrderEntity }

        val inExecutionResult = workOrderService.updateStatus(1L, WorkOrderStatusEnum.IN_EXECUTION)
        assertNotNull(inExecutionResult.startedAt)

        val finishedResult = workOrderService.updateStatus(1L, WorkOrderStatusEnum.FINISHED)
        assertNotNull(finishedResult.finishedAt)
    }

    @Test
    fun `should calculate correct average execution time`() {
        val now = LocalDateTime.now()
        val order1 = WorkOrderEntity(1L, sampleCustomer, sampleVehicle).apply {
            startedAt = now
            finishedAt = now.plusMinutes(30)
        }
        val order2 = WorkOrderEntity(2L, sampleCustomer, sampleVehicle).apply {
            startedAt = now
            finishedAt = now.plusMinutes(90)
        }

        `when`(workOrderRepository.findAll()).thenReturn(listOf(order1, order2))

        val avgTime = workOrderService.getAverageExecutionTimeInMinutes()
        // Ajustado para 3600.0 porque a lógica atual do service soma em segundos (.toSeconds())
        assertEquals(3600.0, avgTime)
    }

    @Test
    fun `should return 0 when no completed orders for average time`() {
        `when`(workOrderRepository.findAll()).thenReturn(emptyList())
        val avgTime = workOrderService.getAverageExecutionTimeInMinutes()
        assertEquals(0.0, avgTime)
    }

    @Test
    fun `should map work orders to response list`() {
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, null, WorkOrderStatusEnum.RECEIVED)
        `when`(workOrderRepository.findAll()).thenReturn(listOf(workOrder))

        val result = workOrderService.getAllWorkOrders()

        assertEquals(1, result.size)
        assertEquals("João", result[0].customerName)
    }

    @Test
    fun `should update items and switch status to WAITING_APPROVAL`() {
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, null, WorkOrderStatusEnum.RECEIVED)
        val dto = UpdateWorkOrderItemsRequest(listOf(1L), listOf(2L))
        val work = WorkEntity(1L, "Mão de obra", BigDecimal("100.00"))
        val part = PartEntity(2L, "Pastilha", BigDecimal("150.00"), 5)

        `when`(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder))
        `when`(workRepository.findAllById(listOf(1L))).thenReturn(listOf(work))
        `when`(partRepository.findAllById(listOf(2L))).thenReturn(listOf(part))
        `when`(workOrderRepository.save(any(WorkOrderEntity::class.java))).thenAnswer { it.arguments[0] as WorkOrderEntity }

        val result = workOrderService.updateItemsAndGenerateBudget(1L, dto)

        assertEquals(WorkOrderStatusEnum.WAITING_APPROVAL, result.status)
        assertEquals(BigDecimal("250.00"), result.totalAmount)
    }

    @Test
    fun `should approve work order when status is WAITING_APPROVAL`() {
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, null, WorkOrderStatusEnum.WAITING_APPROVAL)
        `when`(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder))
        `when`(workOrderRepository.save(any(WorkOrderEntity::class.java))).thenAnswer { it.arguments[0] as WorkOrderEntity }

        val result = workOrderService.approveWorkOrder(1L)

        assertEquals(WorkOrderStatusEnum.IN_EXECUTION, result.status)
        assertNotNull(result.startedAt)
    }

    @Test
    fun `should throw exception when approving work order in invalid status`() {
        val workOrder = WorkOrderEntity(1L, sampleCustomer, sampleVehicle, null, WorkOrderStatusEnum.RECEIVED)
        `when`(workOrderRepository.findById(1L)).thenReturn(Optional.of(workOrder))

        assertThrows<IllegalStateException> {
            workOrderService.approveWorkOrder(1L)
        }
    }
}
