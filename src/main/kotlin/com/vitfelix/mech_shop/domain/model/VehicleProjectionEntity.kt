package com.vitfelix.mech_shop.domain.model

import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "vehicle_projection")
data class VehicleProjectionEntity(
    @Id
    val vehicleId: Long,

    val brand: String,
    val model: String,

    @Column(name = "vehicle_year")
    val year: Int,

    val color: String,

    @Column(precision = 12, scale = 2)
    val price: BigDecimal,

    @Column(name = "source_version")
    val version: Long,

    @Enumerated(EnumType.STRING)
    val availability: VehicleAvailability = VehicleAvailability.AVAILABLE
)
