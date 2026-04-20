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
                String userId = (String) exchange.getAttribute("userId");
                
                if (userId == null) {
                    userId = exchange.getRequest().getRemoteAddress() != null ? 
                            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
                }

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
                    
                    log.debug("Rate limit check passed for userId: {}, remaining tokens: {}", userId, remainingTokens);
                    return chain.filter(exchange);
                } else {
                    return handleRateLimitExceeded(exchange);
                }

            } catch (Exception e) {
                log.error("Error during rate limit check: {}", e.getMessage());
                return chain.filter(exchange);
            }
        };
    }

    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1");
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
