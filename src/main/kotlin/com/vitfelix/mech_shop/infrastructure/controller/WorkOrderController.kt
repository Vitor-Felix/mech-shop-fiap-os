package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.application.service.WorkOrderService
import com.vitfelix.mech_shop.domain.model.WorkOrderEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/work-orders")
class WorkOrderController(
    private val workOrderService: WorkOrderService
) {
    // Consulta pública para o cliente acompanhar
    @GetMapping("/{id}")
    fun getWorkOrder(@PathVariable id: Long): ResponseEntity<WorkOrderEntity> {
        return ResponseEntity.ok(workOrderService.getWorkOrderById(id))
    }

    // Cliente aprova o orçamento
    @PostMapping("/{id}/approve")
    fun approveWorkOrder(@PathVariable id: Long): ResponseEntity<WorkOrderEntity> {
        return ResponseEntity.ok(workOrderService.approveWorkOrder(id))
    }
}
