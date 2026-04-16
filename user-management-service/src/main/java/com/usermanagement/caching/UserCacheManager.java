package com.usermanagement.caching;

import com.usermanagement.dto.UserResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class UserCacheManager {

    private final RedisTemplate<String, UserResponse> redisTemplate;
    private static final String USER_CACHE_KEY_PREFIX = "user:id:";
    private static final String EMAIL_CACHE_KEY_PREFIX = "user:email:";

    public UserCacheManager(RedisTemplate<String, UserResponse> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheUser(UserResponse userResponse) {
        redisTemplate.opsForValue().set(buildIdCacheKey(userResponse.id()), userResponse, 10, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(buildEmailCacheKey(userResponse.email().toLowerCase()), userResponse, 10, TimeUnit.MINUTES);
    }

    public UserResponse getCachedUserById(Long id) {
        return redisTemplate.opsForValue().get(buildIdCacheKey(id));
    }

    public UserResponse getCachedUserByEmail(String email) {
        return redisTemplate.opsForValue().get(buildEmailCacheKey(email.toLowerCase()));
    }

    public void evictCache(String email, Long id) {
        redisTemplate.delete(Arrays.asList(buildIdCacheKey(id), buildEmailCacheKey(email.toLowerCase())));
    }

    private String buildIdCacheKey(Long id) {
        return USER_CACHE_KEY_PREFIX + id;
    }

    private String buildEmailCacheKey(String email) {
        return EMAIL_CACHE_KEY_PREFIX + email;
    }
}
