package com.vitfelix.mech_shop.infrastructure.exception

import com.vitfelix.mech_shop.application.service.SaleConflictException
import com.vitfelix.mech_shop.application.service.SaleNotFoundException
import jakarta.validation.ConstraintViolationException
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

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(ex: ConstraintViolationException): ResponseEntity<Map<String, Any>> {
        val errors = ex.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "Falha de validação",
                "details" to errors
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleInvalidRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Invalid request")))

    @ExceptionHandler(SaleNotFoundException::class)
    fun handleSaleNotFound(ex: SaleNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (ex.message ?: "Not found")))

    @ExceptionHandler(SaleConflictException::class)
    fun handleSaleConflict(ex: SaleConflictException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to (ex.message ?: "Conflict")))
}
