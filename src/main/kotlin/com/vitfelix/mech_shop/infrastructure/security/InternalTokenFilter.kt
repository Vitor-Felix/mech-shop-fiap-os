package com.vitfelix.mech_shop.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class InternalTokenFilter(
    private val expectedToken: String
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.method != "PUT" ||
            !request.requestURI.removePrefix(request.contextPath).startsWith("/internal/vehicles/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val providedToken = request.getHeader("Authorization")
            ?.takeIf { it.startsWith("Bearer ") }
            ?.removePrefix("Bearer ")

        val valid = expectedToken.isNotBlank() &&
            providedToken != null &&
            MessageDigest.isEqual(
                expectedToken.toByteArray(StandardCharsets.UTF_8),
                providedToken.toByteArray(StandardCharsets.UTF_8)
            )

        if (!valid) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write("""{"error":"Unauthorized"}""")
            return
        }

        filterChain.doFilter(request, response)
    }
}
