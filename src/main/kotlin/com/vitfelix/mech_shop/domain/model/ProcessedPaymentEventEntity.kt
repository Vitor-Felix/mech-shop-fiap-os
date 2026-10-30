package com.vitfelix.mech_shop.domain.model

import com.vitfelix.mech_shop.domain.enums.PaymentEventStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "processed_payment_event")
data class ProcessedPaymentEventEntity(
    @Id
    @Column(name = "event_id", length = 128)
    val eventId: String,

    @Column(name = "payment_code", nullable = false, length = 64)
    val paymentCode: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 16)
    val status: PaymentEventStatus,

    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant
)
