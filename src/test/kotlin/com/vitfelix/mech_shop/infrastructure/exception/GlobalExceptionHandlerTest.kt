package com.vitfelix.mech_shop.infrastructure.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `should handle validation exception with custom field messages`() {
        // Arrange
        val exception = mock(MethodArgumentNotValidException::class.java)
        val bindingResult = mock(BindingResult::class.java)
        val fieldError = FieldError("customer", "cpf", "CPF inválido")

        `when`(exception.bindingResult).thenReturn(bindingResult)
        `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError))

        // Act
        val response = handler.handleValidationExceptions(exception)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val body = response.body
        assertNotNull(body)
        assertEquals("Falha de validação", body?.get("error"))

        @Suppress("UNCHECKED_CAST")
        val details = body?.get("details") as Map<String, String>
        assertEquals("CPF inválido", details["cpf"])
    }

    @Test
    fun `should fallback to default message when field error message is null`() {
        // Arrange
        val exception = mock(MethodArgumentNotValidException::class.java)
        val bindingResult = mock(BindingResult::class.java)

        // Mocamos o FieldError para driblar a restrição do construtor Java no Kotlin
        val fieldError = mock(FieldError::class.java)
        `when`(fieldError.field).thenReturn("email")
        `when`(fieldError.defaultMessage).thenReturn(null)

        `when`(exception.bindingResult).thenReturn(bindingResult)
        `when`(bindingResult.fieldErrors).thenReturn(listOf(fieldError))

        // Act
        val response = handler.handleValidationExceptions(exception)

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        @Suppress("UNCHECKED_CAST")
        val details = response.body?.get("details") as Map<String, String>
        assertEquals("Valor inválido", details["email"])
    }
}
