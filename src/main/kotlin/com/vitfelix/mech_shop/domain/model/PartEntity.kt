package com.vitfelix.mech_shop.domain.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class PartEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val name: String,
    val unitPrice: BigDecimal,
    var stockQuantity: Int
)
