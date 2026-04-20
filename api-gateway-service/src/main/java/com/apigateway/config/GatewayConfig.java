package com.apigateway.config;

import com.apigateway.filter.jwt.JwtValidationFilter;
import com.apigateway.filter.ratelimiter.RateLimiterFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GatewayConfig {

    private final JwtValidationFilter jwtValidationFilter;
    private final RateLimiterFilter rateLimiterFilter;
    private final String authServiceUrl;
    private final String userManagementServiceUrl;
    private final String bugTriageServiceUrl;

    public GatewayConfig(
            JwtValidationFilter jwtValidationFilter,
            RateLimiterFilter rateLimiterFilter,
            @Value("${services.auth.url}") String authServiceUrl,
            @Value("${services.user-management.url}") String userManagementServiceUrl,
            @Value("${services.bug-triage.url}") String bugTriageServiceUrl) {
        this.jwtValidationFilter = jwtValidationFilter;
        this.rateLimiterFilter = rateLimiterFilter;
        this.authServiceUrl = authServiceUrl;
        this.userManagementServiceUrl = userManagementServiceUrl;
        this.bugTriageServiceUrl = bugTriageServiceUrl;
    }

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth Service Routes - No JWT required
                .route("auth-login", r -> r
                        .path("/auth/login")
                        .and().method("POST")
                        .uri(authServiceUrl))
                
                .route("auth-register", r -> r
                        .path("/auth/register")
                        .and().method("POST")
                        .uri(authServiceUrl))

                // Protected User Management Routes
                .route("user-all", r -> r
                        .path("/user/**")
                        .filters(f -> f
                                .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig()))
                        )
                        .uri(userManagementServiceUrl))

                // Protected Bug Triage Routes
                .route("bug-all", r -> r
                        .path("/bug/**")
                        .filters(f -> f
                                .filter(jwtValidationFilter.apply(new JwtValidationFilter.Config()))
                                .filter(rateLimiterFilter.apply(rateLimiterConfig()))
                        )
                        .uri(bugTriageServiceUrl))

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
