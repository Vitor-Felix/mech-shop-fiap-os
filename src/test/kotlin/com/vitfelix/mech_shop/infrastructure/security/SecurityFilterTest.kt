package com.vitfelix.mech_shop.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.core.context.SecurityContextHolder

class SecurityFilterTest {

    private val tokenService = mock(TokenService::class.java)
    private val securityFilter = SecurityFilter(tokenService)

    private val request = mock(HttpServletRequest::class.java)
    private val response = mock(HttpServletResponse::class.java)
    private val filterChain = mock(FilterChain::class.java)

    @BeforeEach
    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should proceed without authentication when header is missing`() {
        `when`(request.getHeader("Authorization")).thenReturn(null)

        securityFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should proceed without authentication when header does not start with Bearer`() {
        `when`(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz")

        securityFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should authenticate when token is valid`() {
        val token = "valid-token"
        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(tokenService.validateToken(token)).thenReturn("admin")

        securityFilter.doFilter(request, response, filterChain)

        val auth = SecurityContextHolder.getContext().authentication
        assertEquals("admin", auth?.principal)
        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should not authenticate when token validation fails`() {
        val token = "expired-token"
        `when`(request.getHeader("Authorization")).thenReturn("Bearer $token")
        `when`(tokenService.validateToken(token)).thenThrow(RuntimeException("Token expired"))

        securityFilter.doFilter(request, response, filterChain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(filterChain).doFilter(request, response)
    }
}
