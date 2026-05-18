package com.apigateway.filter.jwt;

import com.apigateway.utility.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component

//AbstractGatewayFilterFactory : it used to create custom route-level gateway filters
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtValidationFilter(JwtTokenUtil jwtTokenUtil) {
        super(Config.class);
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
                String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
                String path = exchange.getRequest().getPath().value();
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                log.info("correlationId: {} - [JWT VALIDATION] {} {} - Processing JWT token", correlationId, method, path);

                if (authHeader == null || authHeader.isEmpty()) {
                    log.warn("correlationId: {} - [JWT VALIDATION] {} {} - Missing authorization header", correlationId, method, path);
                    return handleUnauthorized(exchange, "Missing authorization header", correlationId);
                }

                String token = jwtTokenUtil.extractToken(authHeader);
                Claims claims = jwtTokenUtil.validateToken(token);

                String userId = claims.getSubject();
                
                exchange.getAttributes().put("userId", userId);
                exchange.getAttributes().put("claims", claims);

                log.info("correlationId: {} - [JWT VALIDATION] {} {} - Token validated successfully for userId: {}", correlationId, method, path, userId);

                return chain.filter(exchange);

            } catch (Exception e) {
                String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
                String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
                String path = exchange.getRequest().getPath().value();
                log.error("correlationId: {} - [JWT VALIDATION] {} {} - JWT validation failed: {}", correlationId, method, path, e.getMessage());
                return handleUnauthorized(exchange, e.getMessage(), correlationId);
            }
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message, String correlationId) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        log.warn("correlationId: {} - [JWT REJECTION] Returning 401 Unauthorized - {}", correlationId, message);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        ("{ \"error\": \"" + message + "\" }").getBytes()
                ))
        );
    }

    public static class Config {
    }
}
