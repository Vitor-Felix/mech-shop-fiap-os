package com.vitfelix.mech_shop.domain.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
data class WorkEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val description: String,
    val amount: BigDecimal
)
