package com.vitfelix.mech_shop.application.service

import com.vitfelix.mech_shop.domain.model.PartEntity
import com.vitfelix.mech_shop.domain.repository.PartRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.math.BigDecimal
import java.util.*

class PartServiceTest {

    private val partRepository: PartRepository = mock(PartRepository::class.java)
    private val partService = PartService(partRepository)

    @Test
    fun `should return all parts`() {
        val part = PartEntity(1L, "Filtro de Óleo", BigDecimal("45.00"), 10)
        `when`(partRepository.findAll()).thenReturn(listOf(part))

        val result = partService.getAllParts()

        assertEquals(1, result.size)
        assertEquals("Filtro de Óleo", result[0].name)
    }

    @Test
    fun `should return part by id when exists`() {
        val part = PartEntity(1L, "Filtro de Óleo", BigDecimal("45.00"), 10)
        `when`(partRepository.findById(1L)).thenReturn(Optional.of(part))

        val result = partService.getPartById(1L)

        assertNotNull(result)
        assertEquals(1L, result.id)
    }

    @Test
    fun `should throw exception when part id does not exist`() {
        `when`(partRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            partService.getPartById(1L)
        }
        assertEquals("Part not found", exception.message)
    }

    @Test
    fun `should create part successfully`() {
        val part = PartEntity(null, "Filtro de Óleo", BigDecimal("45.00"), 10)
        val savedPart = part.copy(id = 1L)
        `when`(partRepository.save(part)).thenReturn(savedPart)

        val result = partService.createPart(part)

        assertNotNull(result.id)
        assertEquals("Filtro de Óleo", result.name)
    }

    @Test
    fun `should update part successfully`() {
        val existingPart = PartEntity(1L, "Filtro Antigo", BigDecimal("40.00"), 5)
        val updateInfo = PartEntity(null, "Filtro Novo", BigDecimal("50.00"), 15)
        val updatedPart = existingPart.copy(name = "Filtro Novo", unitPrice = BigDecimal("50.00"), stockQuantity = 15)

        `when`(partRepository.findById(1L)).thenReturn(Optional.of(existingPart))
        `when`(partRepository.save(any(PartEntity::class.java))).thenReturn(updatedPart)

        val result = partService.updatePart(1L, updateInfo)

        assertEquals("Filtro Novo", result.name)
        assertEquals(BigDecimal("50.00"), result.unitPrice)
        assertEquals(15, result.stockQuantity)
    }

    @Test
    fun `should delete part successfully`() {
        doNothing().`when`(partRepository).deleteById(1L)

        assertDoesNotThrow { partService.deletePart(1L) }
        verify(partRepository, times(1)).deleteById(1L)
    }
}
