package com.vitfelix.mech_shop.infrastructure.security

import org.springframework.beans.factory.annotation.Value
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
    private val securityFilter: SecurityFilter,
    @Value("\${MECH_SHOP_INTERNAL_TOKEN:}") internalToken: String,
    @Value("\${PAYMENT_WEBHOOK_SECRET:}") webhookSecret: String
) {
    private val internalTokenFilter = InternalTokenFilter(internalToken)
    private val paymentWebhookSecretFilter = PaymentWebhookSecretFilter(webhookSecret)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { req ->
                // 1. Rotas Públicas do Sistema
                req.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                req.requestMatchers("/api/work-orders/**").permitAll()
                req.requestMatchers(HttpMethod.GET, "/api/vehicles/available", "/api/vehicles/sold").permitAll()
                req.requestMatchers(HttpMethod.POST, "/api/vehicles/*/purchases").permitAll()
                req.requestMatchers(HttpMethod.POST, "/api/webhooks/payments").permitAll()

                // 2. Rotas Públicas do Swagger / OpenAPI
                req.requestMatchers("/swagger-ui/**").permitAll()
                req.requestMatchers("/v3/api-docs/**").permitAll()
                req.requestMatchers("/swagger-ui.html").permitAll()

                // 3. Libera a rota de erro (Evita que o Spring Security transforme um 404 ou 500 em 403)
                req.requestMatchers("/error").permitAll()
                req.requestMatchers(HttpMethod.PUT, "/internal/vehicles/*").permitAll()

                // 4. Rotas Privadas (Tudo que for admin precisa de token)
                req.requestMatchers("/api/admin/**").authenticated()

                // Qualquer outra rota que sobrar, bloqueia
                req.anyRequest().authenticated()
            }
            .addFilterBefore(paymentWebhookSecretFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(internalTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
