package com.vitfelix.mech_shop.application.dto.response

import com.vitfelix.mech_shop.domain.enums.SaleStatus
import java.time.Instant

data class PurchaseResponse(
    val saleId: Long,
    val vehicleId: Long,
    val paymentCode: String,
    val status: SaleStatus,
    val soldAt: Instant?
)
