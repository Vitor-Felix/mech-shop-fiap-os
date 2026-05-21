package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.WorkEntity
import com.vitfelix.mech_shop.domain.repository.WorkRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class WorkService(private val workRepository: WorkRepository) {
    fun getAllWorks() = workRepository.findAll()
    fun getWorkById(id: Long) = workRepository.findByIdOrNull(id) ?: throw RuntimeException("Work not found")
    fun createWork(work: WorkEntity) = workRepository.save(work)
    fun updateWork(id: Long, work: WorkEntity): WorkEntity {
        val existing = getWorkById(id)
        return workRepository.save(existing.copy(description = work.description, amount = work.amount))
    }
    fun deleteWork(id: Long) = workRepository.deleteById(id)
}
