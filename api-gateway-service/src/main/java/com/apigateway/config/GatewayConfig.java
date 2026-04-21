package com.apigateway.config;

import com.apigateway.filter.jwt.JwtValidationFilter;
import com.apigateway.filter.ratelimiter.RateLimiterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayConfig {

    private final JwtValidationFilter jwtValidationFilter;
    private final RateLimiterFilter rateLimiterFilter;

    public GatewayConfig(JwtValidationFilter jwtValidationFilter,
                         RateLimiterFilter rateLimiterFilter) {
        this.jwtValidationFilter = jwtValidationFilter;
        this.rateLimiterFilter = rateLimiterFilter;
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // =========================
                // PUBLIC ROUTES (NO JWT)
                // =========================
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .uri("lb://AUTH-SERVICE")
                )

                // =========================
                // USER SERVICE (PROTECTED)
                // =========================
                .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                        .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                        .filter(rateLimiterFilter.apply(rateLimiterConfig()))
                )
                .uri("lb://USER-MANAGEMENT-SERVICE")
                )
                // =========================
                // BUG SERVICE (PROTECTED)
                // =========================
                .route("bug-service", r -> r
                        .path("/api/bugs/**")
                        .filters(f -> f
                                .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig()))
                        )
                        .uri("lb://BUG-TRIAGE-SERVICE")
                )

                .build();
    }

    private RateLimiterFilter.Config rateLimiterConfig() {
        RateLimiterFilter.Config config = new RateLimiterFilter.Config();
        config.setCapacity(100);
        config.setRefillRate(10);
        config.setWindowSizeSeconds(60);
        return config;
    }
}