package com.vitfelix.mech_shop.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class TokenService {
    private val secret = "minha-chave-secreta-muito-segura-mvp"

    fun generateToken(username: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withIssuer("mech-shop-api")
            .withSubject(username) // Aqui vai o 'admin'
            .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS)) // Token dura 2 horas
            .sign(algorithm)
    }

    fun validateToken(token: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.require(algorithm)
            .withIssuer("mech-shop-api")
            .build()
            .verify(token)
            .subject // Retorna o 'admin' se o token for válido, ou lança exceção se for inválido/expirado
    }
}
