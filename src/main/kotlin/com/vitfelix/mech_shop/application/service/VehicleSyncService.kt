package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.application.dto.request.VehicleSyncRequest
import com.vitfelix.mech_shop.domain.enums.VehicleAvailability
import com.vitfelix.mech_shop.domain.model.VehicleProjectionEntity
import com.vitfelix.mech_shop.domain.repository.VehicleProjectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VehicleSyncService(
    private val vehicleProjectionRepository: VehicleProjectionRepository
) {
    @Transactional
    fun synchronize(vehicleId: Long, request: VehicleSyncRequest): VehicleProjectionEntity {
        require(vehicleId > 0) { "vehicleId must be positive" }

        val existing = vehicleProjectionRepository.findByVehicleIdForUpdate(vehicleId)
        if (existing != null && request.version <= existing.version) {
            return existing
        }

        val projection = VehicleProjectionEntity(
            vehicleId = vehicleId,
            brand = request.brand,
            model = request.model,
            year = request.year,
            color = request.color,
            price = request.price,
            version = request.version,
            availability = existing?.availability ?: VehicleAvailability.AVAILABLE
        )
        return vehicleProjectionRepository.save(projection)
    }
}
