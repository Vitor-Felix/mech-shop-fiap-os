package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.WorkEntity
import com.vitfelix.mech_shop.domain.repository.WorkRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*

class WorkServiceTest {

    private val workRepository: WorkRepository = mock(WorkRepository::class.java)
    private val workService = WorkService(workRepository)

    @Test
    fun `should return all works`() {
        val work = WorkEntity(1L, "Alinhamento", BigDecimal("100.00"))
        `when`(workRepository.findAll()).thenReturn(listOf(work))

        val result = workService.getAllWorks()

        assertEquals(1, result.size)
        assertEquals("Alinhamento", result[0].description)
    }

    @Test
    fun `should return work by id when exists`() {
        val work = WorkEntity(1L, "Alinhamento", BigDecimal("100.00"))
        `when`(workRepository.findById(1L)).thenReturn(Optional.of(work))

        val result = workService.getWorkById(1L)

        assertNotNull(result)
        assertEquals(1L, result.id)
    }

    @Test
    fun `should throw exception when work id does not exist`() {
        `when`(workRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            workService.getWorkById(1L)
        }
        assertEquals("Work not found", exception.message)
    }

    @Test
    fun `should create work successfully`() {
        val work = WorkEntity(null, "Alinhamento", BigDecimal("100.00"))
        val savedWork = work.copy(id = 1L)
        `when`(workRepository.save(work)).thenReturn(savedWork)

        val result = workService.createWork(work)

        assertNotNull(result.id)
        assertEquals("Alinhamento", result.description)
    }

    @Test
    fun `should update work successfully`() {
        val existingWork = WorkEntity(1L, "Troca de Óleo", BigDecimal("50.00"))
        val updateInfo = WorkEntity(null, "Troca de Óleo Premium", BigDecimal("70.00"))
        val updatedWork = existingWork.copy(description = "Troca de Óleo Premium", amount = BigDecimal("70.00"))

        `when`(workRepository.findById(1L)).thenReturn(Optional.of(existingWork))
        `when`(workRepository.save(any(WorkEntity::class.java))).thenReturn(updatedWork)

        val result = workService.updateWork(1L, updateInfo)

        assertEquals("Troca de Óleo Premium", result.description)
        assertEquals(BigDecimal("70.00"), result.amount)
    }

    @Test
    fun `should delete work successfully`() {
        doNothing().`when`(workRepository).deleteById(1L)

        assertDoesNotThrow { workService.deleteWork(1L) }
        verify(workRepository, times(1)).deleteById(1L)
    }
}
