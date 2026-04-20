package com.apigateway.filter.ratelimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimiterManager {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final TokenBucketRateLimiter rateLimiter;

    @Value("${rate-limit.default-capacity:100}")
    private long defaultCapacity;

    @Value("${rate-limit.default-refill-rate:10}")
    private long defaultRefillRate;

    @Value("${rate-limit.default-window-seconds:60}")
    private long defaultWindowSeconds;

    public RateLimiterManager(RedisTemplate<String, String> redisTemplate, 
                            ObjectMapper objectMapper,
                            TokenBucketRateLimiter rateLimiter) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiter;
    }

    public boolean isRequestAllowed(String userId) {
        return rateLimiter.allowRequest(userId, defaultCapacity, defaultRefillRate, defaultWindowSeconds);
    }

    public boolean isRequestAllowed(String userId, long capacity, long refillRate) {
        return rateLimiter.allowRequest(userId, capacity, refillRate, defaultWindowSeconds);
    }

    public boolean isRequestAllowed(String userId, long capacity, long refillRate, long windowSeconds) {
        return rateLimiter.allowRequest(userId, capacity, refillRate, windowSeconds);
    }

    public long getRemainingRequests(String userId) {
        return rateLimiter.getRemainingTokens(userId);
    }

    public void resetRateLimit(String userId) {
        rateLimiter.resetLimit(userId);
        log.info("Rate limit reset for user: {}", userId);
    }

    public void setUserRateLimit(String userId, long capacity, long refillRate, long windowSeconds) {
        String key = "rate_limit_config:" + userId;
        try {
            RateLimitConfig config = new RateLimitConfig(capacity, refillRate, windowSeconds);
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(config), windowSeconds, TimeUnit.SECONDS);
            log.info("Rate limit config set for user: {} - capacity: {}, refillRate: {}", userId, capacity, refillRate);
        } catch (Exception e) {
            log.error("Error setting rate limit config for user: {}", userId, e);
        }
    }

    public RateLimitConfig getUserRateLimit(String userId) {
        String key = "rate_limit_config:" + userId;
        try {
            String config = redisTemplate.opsForValue().get(key);
            if (config != null) {
                return objectMapper.readValue(config, RateLimitConfig.class);
            }
        } catch (Exception e) {
            log.error("Error retrieving rate limit config for user: {}", userId, e);
        }
        return new RateLimitConfig(defaultCapacity, defaultRefillRate, defaultWindowSeconds);
    }

    public static class RateLimitConfig {
        public long capacity;
        public long refillRate;
        public long windowSeconds;

        public RateLimitConfig() {}

        public RateLimitConfig(long capacity, long refillRate, long windowSeconds) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.windowSeconds = windowSeconds;
        }

        public long getCapacity() {
            return capacity;
        }

        public long getRefillRate() {
            return refillRate;
        }

        public long getWindowSeconds() {
            return windowSeconds;
        }
    }
}
