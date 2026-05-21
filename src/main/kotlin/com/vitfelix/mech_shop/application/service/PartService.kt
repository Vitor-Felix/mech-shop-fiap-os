package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.PartEntity
import com.vitfelix.mech_shop.domain.repository.PartRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PartService(private val partRepository: PartRepository) {
    fun getAllParts() = partRepository.findAll()
    fun getPartById(id: Long) = partRepository.findByIdOrNull(id) ?: throw RuntimeException("Part not found")
    fun createPart(part: PartEntity) = partRepository.save(part)
    fun updatePart(id: Long, part: PartEntity): PartEntity {
        val existing = getPartById(id)
        return partRepository.save(existing.copy(name = part.name, unitPrice = part.unitPrice, stockQuantity = part.stockQuantity))
    }
    fun deletePart(id: Long) = partRepository.deleteById(id)
}
