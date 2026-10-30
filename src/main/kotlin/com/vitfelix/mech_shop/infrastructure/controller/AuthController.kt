package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.infrastructure.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class LoginRequest(val login: String, val pass: String)
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenService: TokenService,
    @Value("\${LOGIN_USERNAME:}") private val configuredUsername: String,
    @Value("\${LOGIN_PASSWORD:}") private val configuredPassword: String
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        if (matches(configuredUsername, req.login) && matches(configuredPassword, req.pass)) {
            val token = tokenService.generateToken(req.login)
            return ResponseEntity.ok(LoginResponse(token))
        }
        return ResponseEntity.status(401).build()
    }

    private fun matches(expected: String, provided: String): Boolean =
        expected.isNotBlank() &&
            MessageDigest.isEqual(
                expected.toByteArray(StandardCharsets.UTF_8),
                provided.toByteArray(StandardCharsets.UTF_8)
            )
}
