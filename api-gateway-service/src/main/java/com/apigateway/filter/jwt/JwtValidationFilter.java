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
                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || authHeader.isEmpty()) {
                    log.warn("Missing authorization header");
                    return handleUnauthorized(exchange, "Missing authorization header");
                }

                String token = jwtTokenUtil.extractToken(authHeader);
                Claims claims = jwtTokenUtil.validateToken(token);

                String userId = claims.getSubject();
                
                exchange.getAttributes().put("userId", userId);
                exchange.getAttributes().put("claims", claims);

                log.debug("JWT token validated successfully for userId: {}", userId);

                return chain.filter(exchange);

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return handleUnauthorized(exchange, e.getMessage());
            }
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        ("{ \"error\": \"" + message + "\" }").getBytes()
                ))
        );
    }

    public static class Config {
    }
}
