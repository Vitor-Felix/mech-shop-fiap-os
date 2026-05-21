package com.vitfelix.mech_shop.infrastructure.controller.admin

import com.vitfelix.mech_shop.application.service.WorkOrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/metrics")
class AdminMetricsController(private val workOrderService: WorkOrderService) {
    @GetMapping("/average-time")
    fun getAverageExecutionTime(): ResponseEntity<Map<String, Any>> {
        val avgTime = workOrderService.getAverageExecutionTimeInMinutes()
        return ResponseEntity.ok(mapOf("averageExecutionTimeInMinutes" to avgTime))
    }
}
