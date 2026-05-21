package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.dto.request.CreateWorkOrderRequest
import com.vitfelix.mech_shop.application.dto.request.UpdateWorkOrderItemsRequest
import com.vitfelix.mech_shop.application.dto.response.WorkOrderResponse
import com.vitfelix.mech_shop.application.service.WorkOrderService
import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import com.vitfelix.mech_shop.domain.model.WorkOrderEntity
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/work-orders")
class AdminWorkOrderController(
    private val workOrderService: WorkOrderService
) {
    @PostMapping
    fun createWorkOrder(@Valid @RequestBody dto: CreateWorkOrderRequest): ResponseEntity<WorkOrderEntity> {
        return ResponseEntity.status(HttpStatus.CREATED).body(workOrderService.createWorkOrder(dto))
    }

    // Mecânico adiciona peças/serviços -> vira WAITING_APPROVAL
    @PutMapping("/{id}/items")
    fun updateItems(
        @PathVariable id: Long,
        @Valid @RequestBody dto: UpdateWorkOrderItemsRequest
    ): ResponseEntity<WorkOrderEntity> {
        return ResponseEntity.ok(workOrderService.updateItemsAndGenerateBudget(id, dto))
    }

    @GetMapping
    fun listAllWorkOrders(): ResponseEntity<List<WorkOrderResponse>> {
        return ResponseEntity.ok(workOrderService.getAllWorkOrders())
    }

    // Forçar mudança de status manual (ex: ir para FINISHED)
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestParam status: WorkOrderStatusEnum
    ): ResponseEntity<WorkOrderEntity> {
        return ResponseEntity.ok(workOrderService.updateStatus(id, status))
    }
}
