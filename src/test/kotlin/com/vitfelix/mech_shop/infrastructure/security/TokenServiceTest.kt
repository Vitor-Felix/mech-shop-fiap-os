package com.vitfelix.mech_shop.infrastructure.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TokenServiceTest {

    private val tokenService = TokenService("test-secret-that-is-at-least-32-bytes-long")

    @Test
    fun `should generate and validate token successfully`() {
        val username = "admin"

        val token = tokenService.generateToken(username)
        assertNotNull(token)

        val subject = tokenService.validateToken(token)
        assertEquals(username, subject)
    }

    @Test
    fun `should throw exception when token is invalid`() {
        assertThrows<Exception> {
            tokenService.validateToken("token-completamente-invalido")
        }
    }

    @Test
    fun `should reject a missing or weak signing secret`() {
        assertThrows<IllegalArgumentException> {
            TokenService("").generateToken("admin")
        }
    }
}
