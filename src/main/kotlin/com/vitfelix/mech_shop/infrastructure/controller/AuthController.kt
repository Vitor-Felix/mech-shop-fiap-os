package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.infrastructure.security.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

// DTOs simples para o login (podem ficar aqui no mesmo arquivo para agilizar)
data class LoginRequest(val login: String, val pass: String)
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenService: TokenService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        // Validação simples direto no controller para o MVP
        if (req.login == "admin" && req.pass == "admin123") {
            val token = tokenService.generateToken(req.login)
            return ResponseEntity.ok(LoginResponse(token))
        }
        return ResponseEntity.status(401).build() // Unauthorized
    }
}
