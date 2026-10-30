package com.vitfelix.mech_shop.application.dto.request

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

data class PurchaseRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^(?:[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2})$")
    @field:ValidCpf
    val buyerCpf: String
)

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidCpfValidator::class])
annotation class ValidCpf(
    val message: String = "CPF inválido",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ValidCpfValidator : ConstraintValidator<ValidCpf, String> {
    private val validator = com.vitfelix.mech_shop.infrastructure.validation.CpfCnpjValidator()

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return false
        val digits = value.filter { it in '0'..'9' }
        return digits.length == 11 && validator.isValid(digits, context)
    }
}
