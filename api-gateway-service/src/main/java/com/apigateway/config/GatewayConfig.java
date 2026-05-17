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

    //RouteLocatorBuilder is a helper class in Spring Cloud Gateway used to create API Gateway routes.
    //RouteLocator → final collection of all routes
    //builder → used to define routes
    // no need to add "apigateway controller" here because it is local for api gateway
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        log.info("Initializing API Gateway routes");

        return builder.routes()

                // Route for authentication service
                .route("auth-service", r -> r
                        // Matches auth API paths
                        .path("/api/auth/**")
                        .filters(f -> f
                                // Applies rate limiter filter
                                .filter(rateLimiterFilter.apply(rateLimiterConfig())))
                        // Forwards request to auth service using load balancer
                        .uri("lb://AUTH-SERVICE")
                )

                .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                        .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                        .filter(rateLimiterFilter.apply(rateLimiterConfig()))
                )
                .uri("lb://USER-MANAGEMENT-SERVICE")
                )

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