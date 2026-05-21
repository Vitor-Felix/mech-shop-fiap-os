package com.vitfelix.mech_shop.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val securityFilter: SecurityFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { req ->
                // 1. Rotas Públicas do Sistema
                req.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                req.requestMatchers("/api/work-orders/**").permitAll()

                // 2. Rotas Públicas do Swagger / OpenAPI
                req.requestMatchers("/swagger-ui/**").permitAll()
                req.requestMatchers("/v3/api-docs/**").permitAll()
                req.requestMatchers("/swagger-ui.html").permitAll()

                // 3. Libera a rota de erro (Evita que o Spring Security transforme um 404 ou 500 em 403)
                req.requestMatchers("/error").permitAll()

                // 4. Rotas Privadas (Tudo que for admin precisa de token)
                req.requestMatchers("/api/admin/**").authenticated()

                // Qualquer outra rota que sobrar, bloqueia
                req.anyRequest().authenticated()
            }
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
