package com.apigateway.filter.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimiterFilter(TokenBucketRateLimiter rateLimiter) {
        super(Config.class);
        this.rateLimiter = rateLimiter;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
                String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
                String path = exchange.getRequest().getPath().value();
                String userId = (String) exchange.getAttribute("userId");
                
                if (userId == null) {
                    userId = exchange.getRequest().getRemoteAddress() != null ? 
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
                }

                log.info("correlationId: {} - [RATE LIMIT CHECK] {} {} - userId: {}, capacity: {}", 
                        correlationId, method, path, userId, config.getCapacity());

                boolean allowed = rateLimiter.allowRequest(
                        userId,
                        config.getCapacity(),
                        config.getRefillRate(),
                        config.getWindowSizeSeconds()
                );

                if (allowed) {
                    long remainingTokens = rateLimiter.getRemainingTokens(userId);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Capacity", String.valueOf(config.getCapacity()));
                    
                    log.info("correlationId: {} - [RATE LIMIT PASSED] {} {} - userId: {}, remaining tokens: {}", 
                            correlationId, method, path, userId, remainingTokens);
                    return chain.filter(exchange);
                } else {
                    log.warn("correlationId: {} - [RATE LIMIT EXCEEDED] {} {} - userId: {}", 
                            correlationId, method, path, userId);
                    return handleRateLimitExceeded(exchange, correlationId);
                }

            } catch (Exception e) {
                String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
                log.error("correlationId: {} - [RATE LIMIT ERROR] Error during rate limit check: {}", correlationId, e.getMessage(), e);
                return chain.filter(exchange);
            }
        };
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String correlationId) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1");
        log.warn("correlationId: {} - [RATE LIMIT REJECTION] Returning 429 Too Many Requests", correlationId);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(
                        ("{ \"error\": \"Rate limit exceeded. Please try again later.\" }").getBytes()
                ))
        );
    }

    public static class Config {
        private long capacity = 100;
        private long refillRate = 10;
        private long windowSizeSeconds = 60;

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public long getRefillRate() {
            return refillRate;
        }

        public void setRefillRate(long refillRate) {
            this.refillRate = refillRate;
        }

        public long getWindowSizeSeconds() {
            return windowSizeSeconds;
        }

        public void setWindowSizeSeconds(long windowSizeSeconds) {
            this.windowSizeSeconds = windowSizeSeconds;
        }
    }
}
