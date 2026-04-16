package com.bugtriage.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Component
public class CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    private final RedisTemplate<String, Object> redisTemplate;

    // Cache key prefix for bugs
    private static final String BUG_CACHE_KEY_PREFIX = "bug:";
    
    // Default cache TTL: 1 hour (3600 seconds)
    private static final long DEFAULT_CACHE_TTL = 3600;

    public CacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get cached bug by ID
     * 
     * @param bugId the bug ID
     * @param valueType the class type to deserialize to
     * @param <T> generic type
     * @return cached object or null if not found
     */
    public <T> T getCachedBug(Long bugId, Class<T> valueType) {
        String cacheKey = generateCacheKey(bugId);
        try {
            Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                log.debug("Cache HIT for bug ID: {}", bugId);
                return valueType.cast(cachedValue);
            }
            log.debug("Cache MISS for bug ID: {}", bugId);
        } catch (Exception e) {
            log.warn("Error retrieving cache for bug ID: {}", bugId, e);
        }
        return null;
    }

    /**
     * Cache a bug object
     * 
     * @param bugId the bug ID
     * @param value the bug object to cache
     */
    public void cacheBug(Long bugId, Object value) {
        String cacheKey = generateCacheKey(bugId);
        try {
            redisTemplate.opsForValue().set(cacheKey, value, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("Cached bug ID: {} with TTL: {} seconds", bugId, DEFAULT_CACHE_TTL);
        } catch (Exception e) {
            log.warn("Error caching bug ID: {}", bugId, e);
        }
    }

    /**
     * Invalidate cache for a specific bug
     * Used when bug is updated or deleted
     * 
     * @param bugId the bug ID
     */
    public void invalidateBugCache(Long bugId) {
        String cacheKey = generateCacheKey(bugId);
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            log.debug("Cache invalidated for bug ID: {} - deleted: {}", bugId, deleted);
        } catch (Exception e) {
            log.warn("Error invalidating cache for bug ID: {}", bugId, e);
        }
    }

    /**
     * Generate cache key for a bug ID
     * Cache key format: bug:{bugId}
     * 
     * @param bugId the bug ID
     * @return the cache key
     */
    private String generateCacheKey(Long bugId) {
        return BUG_CACHE_KEY_PREFIX + bugId;
    }

}
