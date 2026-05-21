package com.vitfelix.mech_shop.application.dto.response

import com.vitfelix.mech_shop.domain.enums.WorkOrderStatusEnum
import java.math.BigDecimal
import java.time.LocalDateTime

data class WorkOrderResponse(
    val id: Long?,
    val customerName: String,
    val vehiclePlate: String,
    val status: WorkOrderStatusEnum,
    val totalAmount: BigDecimal,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?
)