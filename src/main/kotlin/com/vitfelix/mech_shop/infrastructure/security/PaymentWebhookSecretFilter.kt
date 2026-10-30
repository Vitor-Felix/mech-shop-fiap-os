package com.vitfelix.mech_shop.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class PaymentWebhookSecretFilter(
    private val expectedSecret: String
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.method != "POST" ||
            request.requestURI.removePrefix(request.contextPath) != "/api/webhooks/payments"

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val providedSecret = request.getHeader("X-Webhook-Secret")
        val valid = expectedSecret.isNotBlank() &&
            providedSecret != null &&
            MessageDigest.isEqual(
                expectedSecret.toByteArray(StandardCharsets.UTF_8),
                providedSecret.toByteArray(StandardCharsets.UTF_8)
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
