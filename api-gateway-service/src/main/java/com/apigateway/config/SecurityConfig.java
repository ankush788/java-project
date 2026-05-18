package com.apigateway.config; // Package for security configuration

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean; // Used to create Spring beans
import org.springframework.context.annotation.Configuration; // Marks class as configuration class
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity; // Enables WebFlux security
import org.springframework.security.config.web.server.ServerHttpSecurity; // Used to configure security settings
import org.springframework.security.web.server.SecurityWebFilterChain; // Represents security filter chain

@Slf4j
@Configuration
@EnableWebFluxSecurity // Enables Spring Security for WebFlux/Gateway
public class SecurityConfig {

    @Bean // Registers this method return object as Spring bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        log.info("Initializing Spring Security filter chain for API Gateway");

        return http

                // Disables CSRF protection for REST APIs
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // Disables default basic authentication popup
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // Disables Spring default login form
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Disables default logout handling
                .logout(ServerHttpSecurity.LogoutSpec::disable)

                // Starts route authorization configuration
                .authorizeExchange(exchange -> exchange

                        // Allows these routes without authentication
                        .pathMatchers(
                                "/api/auth/**",
                                "/api/users/**",
                                "/api/bugs/**",
                                "/actuator/health",
                                "/actuator/metrics",
                                "/gateway/validate-token"
                        ).permitAll()
                        // Block everything else (request which path is not known)
                        .anyExchange().denyAll()
                )

                // Builds and returns security configuration
                .build();
    }
}

