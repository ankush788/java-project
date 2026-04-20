package com.apigateway.filter.ratelimiter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TokenBucketRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public TokenBucketRateLimiter(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean allowRequest(String key, long capacity, long refillRate, long windowSizeSeconds) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            String bucketKey = "rate_limit:" + key;
            String bucket = redisTemplate.opsForValue().get(bucketKey);

            TokenBucket tokenBucket;
            if (bucket == null) {
                tokenBucket = new TokenBucket(capacity, capacity, currentTime);
            } else {
                tokenBucket = objectMapper.readValue(bucket, TokenBucket.class);
                long timePassed = currentTime - tokenBucket.getLastRefillTime();
                long tokensToAdd = timePassed * refillRate;
                tokenBucket.setTokens(Math.min(capacity, tokenBucket.getTokens() + tokensToAdd));
                tokenBucket.setLastRefillTime(currentTime);
            }

            if (tokenBucket.getTokens() >= 1) {
                tokenBucket.setTokens(tokenBucket.getTokens() - 1);
                redisTemplate.opsForValue().set(bucketKey, objectMapper.writeValueAsString(tokenBucket), 
                        windowSizeSeconds, TimeUnit.SECONDS);
                log.debug("Request allowed for key: {}, remaining tokens: {}", key, tokenBucket.getTokens());
                return true;
            } else {
                log.warn("Rate limit exceeded for key: {}", key);
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing token bucket from Redis", e);
            return false;
        }
    }

    public void resetLimit(String key) {
        redisTemplate.delete("rate_limit:" + key);
        log.debug("Rate limit reset for key: {}", key);
    }

    public long getRemainingTokens(String key) {
        try {
            String bucket = redisTemplate.opsForValue().get("rate_limit:" + key);
            if (bucket == null) {
                return 0;
            }
            TokenBucket tokenBucket = objectMapper.readValue(bucket, TokenBucket.class);
            return Math.round(tokenBucket.getTokens());
        } catch (JsonProcessingException e) {
            log.error("Error reading token bucket from Redis", e);
            return 0;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenBucket {
        private double tokens;
        private long capacity;
        private long lastRefillTime;
    }
}
