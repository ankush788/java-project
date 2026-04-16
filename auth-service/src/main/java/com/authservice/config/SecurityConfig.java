package com.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
 * SecurityConfig:
 * Central configuration for Spring Security in auth-service.
 * Defines:
 *  - Which endpoints are public vs secured
 *  - Session behavior (stateless)
 *  - Disabled default security mechanisms (CSRF, HTTP Basic)
 *  - Password encoding strategy
 */
@Configuration
public class SecurityConfig {

    /* securityFilterChain
     * Defines the security rules for all incoming HTTP requests.
     * Spring uses this filter chain to decide:
     *  - Who can access which endpoint
     *  - Whether authentication is required
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            /*
             * Disable CSRF protection.
             * Reason:
             * - CSRF is needed for session-based (cookie) auth
             * - This service is stateless (JWT-based), so it's unnecessary
             */
            .csrf(csrf -> csrf.disable())

            /*
             * Configure authorization rules:
             * - /auth/** → public endpoints (login, register)
             * - any other request → must be authenticated
             */
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            /*
             * Configure session management:
             * - STATELESS means:
             *   - No session stored on server
             *   - Each request must carry authentication (e.g., JWT)
             */
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            /*
             * Disable HTTP Basic authentication:
             * - Prevents browser login popup
             * - Not needed because we use token-based auth (JWT)
             */
            .httpBasic(httpBasic -> httpBasic.disable());

        /*
         * Build and return the configured SecurityFilterChain.
         * This is what Spring Security uses internally to secure APIs.
         */
        return http.build();
    }

    /*
     * PasswordEncoder Bean:
     * Provides password hashing using BCrypt algorithm.
     *
     * Used for:
     * - Encoding password before saving to DB
     * - Matching raw password with hashed password during login
     *
     * BCrypt is recommended because:
     * - It automatically handles salting
     * - Resistant to brute-force attacks
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}