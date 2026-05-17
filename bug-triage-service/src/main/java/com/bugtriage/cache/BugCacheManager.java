package com.bugtriage.cache;

import com.bugtriage.dto.BugResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * Cache utility component implementing the cache-aside pattern
 * Handles cache operations for bug entities
 * 
 * Cache-aside pattern flow:
 * 1. Check cache first
 * 2. If hit, return cached data
 * 3. If miss, fetch from database
 * 4. Store in cache
 * 5. Return data
 */
@Slf4j
@Component
public class BugCacheManager {

    private final RedisTemplate<String, BugResponse> redisTemplate;

    // Cache key prefix for bugs
    private static final String BUG_CACHE_KEY_PREFIX = "bug:";
    
    // Default cache TTL: 1 hour (3600 seconds)
    private static final long DEFAULT_CACHE_TTL = 3600;

    public BugCacheManager(RedisTemplate<String, BugResponse> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public BugResponse getCachedBug(String correlationId, Long bugId) {
        String cacheKey = generateCacheKey(bugId);
        try {
            BugResponse cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                log.debug("correlationId: {} - Cache HIT for bug ID: {}", correlationId, bugId);
                return cachedValue;
            }
            log.debug("correlationId: {} - Cache MISS for bug ID: {}", correlationId, bugId);
        } catch (Exception e) {
            log.warn("correlationId: {} - Error retrieving cache for bug ID: {}", correlationId, bugId, e);
        }
        return null;
    }

  
    public void cacheBug(String correlationId, Long bugId, BugResponse value) {
        String cacheKey = generateCacheKey(bugId);
        try {
            redisTemplate.opsForValue().set(cacheKey, value, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("correlationId: {} - Cached bug ID: {} with TTL: {} seconds", correlationId, bugId, DEFAULT_CACHE_TTL);
        } catch (Exception e) {
            log.warn("correlationId: {} - Error caching bug ID: {}", correlationId, bugId, e);
        }
    }

  
    public void invalidateBugCache(String correlationId, Long bugId) {
        String cacheKey = generateCacheKey(bugId);
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.debug("correlationId: {} - Cache invalidated for bug ID: {} - deleted: {}", correlationId, bugId, deleted);
        } catch (Exception e) {
            log.warn("correlationId: {} - Error invalidating cache for bug ID: {}", correlationId, bugId, e);
        }
    }

    private String generateCacheKey(Long bugId) {
        return BUG_CACHE_KEY_PREFIX + bugId;
    }

}
