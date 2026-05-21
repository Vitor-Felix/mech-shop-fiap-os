package com.vitfelix.mech_shop.domain.model

import jakarta.persistence.*

@Entity
data class VehicleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    val licensePlate: String,
    val brand: String,
    val model: String,
    @Column(name = "vehicle_year")
    val year: Int,
    @ManyToOne val customer: CustomerEntity
)
