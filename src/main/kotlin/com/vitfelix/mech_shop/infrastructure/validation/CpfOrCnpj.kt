package com.vitfelix.mech_shop.infrastructure.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CpfCnpjValidator::class])
annotation class CpfOrCnpj(
    val message: String = "Documento inválido. Deve ser um CPF ou CNPJ válido.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class CpfCnpjValidator : ConstraintValidator<CpfOrCnpj, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return false
        val cleanValue = value.replace(Regex("[^0-9]"), "")

        return when (cleanValue.length) {
            11 -> isValidCPF(cleanValue)
            14 -> isValidCNPJ(cleanValue)
            else -> false
        }
    }

    private fun isValidCPF(cpf: String): Boolean {
        if (cpf.all { it == cpf[0] }) return false
        val numbers = cpf.map { it.toString().toInt() }
        val digit1 = calculateDigit(numbers.take(9), 10)
        val digit2 = calculateDigit(numbers.take(10), 11)
        return numbers[9] == digit1 && numbers[10] == digit2
    }

    private fun isValidCNPJ(cnpj: String): Boolean {
        if (cnpj.all { it == cnpj[0] }) return false
        val numbers = cnpj.map { it.toString().toInt() }
        val weight1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val weight2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

        val sum1 = numbers.take(12).mapIndexed { i, num -> num * weight1[i] }.sum()
        val digit1 = if (sum1 % 11 < 2) 0 else 11 - (sum1 % 11)

        val sum2 = numbers.take(13).mapIndexed { i, num -> num * weight2[i] }.sum()
        val digit2 = if (sum2 % 11 < 2) 0 else 11 - (sum2 % 11)

        return numbers[12] == digit1 && numbers[13] == digit2
    }

    private fun calculateDigit(numbers: List<Int>, weight: Int): Int {
        var w = weight
        val sum = numbers.sumOf { it * w-- }
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }
}
