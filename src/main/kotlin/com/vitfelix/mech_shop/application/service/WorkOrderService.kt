package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.CreateWorkOrderRequest
import com.vitfelix.mech_shop.application.dto.request.UpdateWorkOrderItemsRequest
import com.vitfelix.mech_shop.application.dto.response.WorkOrderResponse
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.WorkOrderEntity
import com.vitfelix.mech_shop.domain.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class WorkOrderService(
    private val vehicleRepository: VehicleRepository,
    private val workRepository: WorkRepository,
    private val partRepository: PartRepository,
    private val workOrderRepository: WorkOrderRepository
) {

    @Transactional
    fun createWorkOrder(dto: CreateWorkOrderRequest): WorkOrderEntity {
        // Agora recebe apenas o ID do veículo, pressupondo que já foi cadastrado
        val vehicle = vehicleRepository.findById(dto.vehicleId)
            .orElseThrow { IllegalArgumentException("Vehicle not found") }

        val workOrder = WorkOrderEntity(
            customer = vehicle.customer, // Cliente vem atrelado ao veículo
            vehicle = vehicle,
            observation = dto.observation,
            status = WorkOrderStatusEnum.RECEIVED // Nasce apenas Recebida
        )

        return workOrderRepository.save(workOrder)
    }

    fun getWorkOrderById(id: Long): WorkOrderEntity {
        return workOrderRepository.findById(id)
            .orElseThrow { RuntimeException("Work Order not found with id: $id") }
    }

    @Transactional
    fun updateStatus(id: Long, newStatus: WorkOrderStatusEnum): WorkOrderEntity {
        val workOrder = getWorkOrderById(id)

        // Alteração automática dos timestamps baseada no status
        if (newStatus == WorkOrderStatusEnum.IN_EXECUTION) {
            workOrder.startedAt = LocalDateTime.now()
        } else if (newStatus == WorkOrderStatusEnum.FINISHED) {
            workOrder.finishedAt = LocalDateTime.now()

        }

        workOrder.status = newStatus
        return workOrderRepository.save(workOrder)
    }

    // Monitoramento do tempo médio de execução (em minutos)
    fun getAverageExecutionTimeInMinutes(): Double {
        val completedOrders = workOrderRepository.findAll().filter {
            it.startedAt != null && it.finishedAt != null
        }
        if (completedOrders.isEmpty()) return 0.0

        val totalMinutes = completedOrders.sumOf {
            Duration.between(it.startedAt, it.finishedAt).toSeconds()
        }
        return totalMinutes.toDouble() / completedOrders.size
    }

    fun getAllWorkOrders(): List<WorkOrderResponse> {
        return workOrderRepository.findAll().map {
            WorkOrderResponse(
                it.id,
                it.customer.name,
                it.vehicle.licensePlate,
                it.status,
                it.totalAmount,
                it.startedAt,
                it.finishedAt
            )
        }
    }

    @Transactional
    fun updateItemsAndGenerateBudget(id: Long, dto: UpdateWorkOrderItemsRequest): WorkOrderEntity {
        val workOrder = getWorkOrderById(id)

        // Limpa e adiciona os novos itens (substituição total, facilita o front)
        workOrder.works.clear()
        workOrder.works.addAll(workRepository.findAllById(dto.serviceIds))

        workOrder.parts.clear()
        workOrder.parts.addAll(partRepository.findAllById(dto.partIds))

        // Regra: Orçamento gerado automaticamente
        workOrder.calculateBudget()

        // Avança o status para aguardar o cliente
        workOrder.status = WorkOrderStatusEnum.WAITING_APPROVAL

        return workOrderRepository.save(workOrder)
    }

    @Transactional
    fun approveWorkOrder(id: Long): WorkOrderEntity {
        val workOrder = getWorkOrderById(id)

        // Regra de negócio: Só pode aprovar se estiver aguardando aprovação
        if (workOrder.status != WorkOrderStatusEnum.WAITING_APPROVAL) {
            throw IllegalStateException("Work Order cannot be approved in its current status: ${workOrder.status}")
        }

        // Muda o status para Em Execução e inicia o relógio
        workOrder.status = WorkOrderStatusEnum.IN_EXECUTION
        workOrder.startedAt = LocalDateTime.now()

        return workOrderRepository.save(workOrder)
    }
}
