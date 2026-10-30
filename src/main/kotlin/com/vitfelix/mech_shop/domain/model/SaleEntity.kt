package com.vitfelix.mech_shop.domain.model

import com.vitfelix.mech_shop.domain.enums.SaleStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "vehicle_sale",
    indexes = [Index(name = "idx_vehicle_sale_vehicle_id", columnList = "vehicle_id")]
)
data class SaleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "vehicle_id", nullable = false)
    val vehicleId: Long,

    @Column(name = "buyer_cpf", nullable = false, length = 11)
    val buyerCpf: String,

    @Column(name = "payment_code", nullable = false, unique = true, length = 64)
    val paymentCode: String,

    @Column(name = "price_snapshot", nullable = false, precision = 12, scale = 2)
    val priceSnapshot: BigDecimal,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false, length = 24)
    val status: SaleStatus = SaleStatus.PENDING_PAYMENT,

    @Column(name = "sold_at")
    val soldAt: Instant? = null
)
