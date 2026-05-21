package com.vitfelix.mech_shop.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SecurityFilter(
    private val tokenService: TokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = recoverToken(request)

        if (token != null) {
            try {
                // Se o token for válido, pega o subject (admin)
                val subject = tokenService.validateToken(token)

                // Avisa o Spring Security que o usuário está autenticado
                val authentication = UsernamePasswordAuthenticationToken(subject, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                // Token inválido ou expirado, não faz nada (vai dar 403 Forbidden nas rotas protegidas)
            }
        }

        // Passa a requisição pra frente
        filterChain.doFilter(request, response)
    }

    private fun recoverToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null
        return authHeader.replace("Bearer ", "")
    }
}
