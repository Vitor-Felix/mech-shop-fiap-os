package com.vitfelix.mech_shop.infrastructure.validation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CpfCnpjValidatorTest {

    private val validator = CpfCnpjValidator()

    @Test
    fun `should return false when value is null or blank`() {
        assertFalse(validator.isValid(null, null))
        assertFalse(validator.isValid("", null))
        assertFalse(validator.isValid("   ", null))
    }

    @Test
    fun `should return false when length is invalid`() {
        assertFalse(validator.isValid("123", null))
        assertFalse(validator.isValid("123456789012", null)) // 12 dígitos
    }

    @Test
    fun `should validate CPF successfully`() {
        assertTrue(validator.isValid("11144477735", null))
        assertTrue(validator.isValid("111.444.777-35", null)) // Com máscara
    }

    @Test
    fun `should return false for invalid CPF`() {
        assertFalse(validator.isValid("11144477736", null)) // Último dígito incorreto
        assertFalse(validator.isValid("11111111111", null)) // Todos os números iguais
    }

    @Test
    fun `should validate CNPJ successfully`() {
        assertTrue(validator.isValid("00000000000191", null)) // CNPJ válido real
        assertTrue(validator.isValid("00.000.000/0001-91", null)) // Com máscara
    }

    @Test
    fun `should return false for invalid CNPJ`() {
        assertFalse(validator.isValid("00000000000192", null)) // Último dígito incorreto
        assertFalse(validator.isValid("00000000000000", null)) // Todos os números iguais
    }
}
