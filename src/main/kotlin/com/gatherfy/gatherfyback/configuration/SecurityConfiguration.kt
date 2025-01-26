package com.gatherfy.gatherfyback.configuration

import com.gatherfy.gatherfyback.Exception.AuthenticationEntryPoint
import com.gatherfy.gatherfyback.Exception.CustomAccessDeniedHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        authenticationEntryPoint: AuthenticationEntryPoint,
        customAccessDeniedHandler: CustomAccessDeniedHandler
    ): DefaultSecurityFilterChain{
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.GET,
                        "/api/v1/events/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/v1/tags",
                        "/api/v1/views",
                        "/api/v1/questions/event/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST,
                        "/api/v1/login",
                        "/api/v1/signup")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET,
                        "/api/v1/tickets")
                    .hasRole("Attendee")
                    .requestMatchers(HttpMethod.GET,
                        "/api/v1/registrations",
                        "/api/v2/registrations",
                        "/api/v1/registrations/**",
                        "/api/v2/backoffice/**",
                        "/api/v1/backoffice/**",
                        "/api/v1/feedbacks/**",
                        "/api/v2/feedbacks/**",
                        "/api/v1/answers/**")
                    .hasRole("Organization")
                    .requestMatchers(HttpMethod.POST,
                        "/api/v2/registrations",
                        "/api/v1/check-in/**")
                    .hasRole("Attendee")
                    .requestMatchers(HttpMethod.POST,
                        "/api/v1/questions",
                        "/api/v1/backoffice/**",
                        "/api/v2/backoffice/**",
                        "/api/v1/files/**")
                    .hasRole("Organization")
                    .requestMatchers(HttpMethod.PUT,
                        "/api/v1/questions/**",
                        "/api/v1/backoffice/**",
                        "/api/v1/check-in",
                        "/api/v2/check-in")
                    .hasRole("Organization")
                    .requestMatchers(HttpMethod.DELETE,
                        "/api/v1/questions/**",
                        "/api/v1/backoffice/**",
                        "/api/v1/files/**",
                        "/api/v1/feedbacks/**",)
                    .hasRole("Organization")
                    .anyRequest()
                    .authenticated()
            }
            .exceptionHandling {
                it.accessDeniedHandler(customAccessDeniedHandler)
                it.authenticationEntryPoint(authenticationEntryPoint)
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter,UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}