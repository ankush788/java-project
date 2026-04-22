package com.usermanagement.caching;

import com.usermanagement.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    private static final Logger log = LoggerFactory.getLogger(UserCacheManager.class);

    private final RedisTemplate<String, UserResponse> redisTemplate;
    private static final String USER_CACHE_KEY_PREFIX = "user:id:";
    private static final String EMAIL_CACHE_KEY_PREFIX = "user:email:";

    public UserCacheManager(RedisTemplate<String, UserResponse> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheUser(String correlationId, UserResponse userResponse) {
        redisTemplate.opsForValue().set(buildIdCacheKey(userResponse.id()), userResponse, 10, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(buildEmailCacheKey(userResponse.email().toLowerCase()), userResponse, 10, TimeUnit.MINUTES);
        log.info("correlationId: {} - Cached user id: {}", correlationId, userResponse.id());
    }

    public UserResponse getCachedUserById(String correlationId, Long id) {
        UserResponse userResponse = redisTemplate.opsForValue().get(buildIdCacheKey(id));
        log.info("correlationId: {} - Cache {} for user id: {}", correlationId, userResponse != null ? "HIT" : "MISS", id);
        return userResponse;
    }

    public UserResponse getCachedUserByEmail(String correlationId, String email) {
        UserResponse userResponse = redisTemplate.opsForValue().get(buildEmailCacheKey(email.toLowerCase()));
        log.info("correlationId: {} - Cache {} for user email: {}", correlationId, userResponse != null ? "HIT" : "MISS", email);
        return userResponse;
    }

    public void evictCache(String correlationId, String email, Long id) {
        redisTemplate.delete(Arrays.asList(buildIdCacheKey(id), buildEmailCacheKey(email.toLowerCase())));
        log.info("correlationId: {} - Cache invalidated for user id: {}", correlationId, id);
    }

    private String buildIdCacheKey(Long id) {
        return USER_CACHE_KEY_PREFIX + id;
    }

    private String buildEmailCacheKey(String email) {
        return EMAIL_CACHE_KEY_PREFIX + email;
    }
}
