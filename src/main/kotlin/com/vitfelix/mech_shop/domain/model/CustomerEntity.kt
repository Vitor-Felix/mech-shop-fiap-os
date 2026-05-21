package com.vitfelix.mech_shop.domain.model

import jakarta.persistence.*

@Entity
data class CustomerEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val document: String, // CPF ou CNPJ
    val name: String
)
