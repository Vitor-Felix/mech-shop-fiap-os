package com.vitfelix.mech_shop.infrastructure.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        // Mapeia os erros dizendo qual campo falhou e o porquê
        val errors = ex.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Valor inválido")
        }

        val response = mapOf(
            "error" to "Falha de validação",
            "details" to errors
        )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }
}
