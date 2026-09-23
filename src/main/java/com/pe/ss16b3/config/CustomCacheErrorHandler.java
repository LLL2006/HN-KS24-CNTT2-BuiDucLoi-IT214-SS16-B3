package com.pe.ss16b3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

/**
 * Custom CacheErrorHandler to ensure system resiliency and graceful degradation
 * when Redis encounters connectivity issues or downtime.
 */
@Component
public class CustomCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomCacheErrorHandler.class);

    /**
     * When Redis GET fails:
     * We suppress the exception and log a warning.
     * Spring Cache will proceed as a cache MISS, seamlessly falling back to the Database.
     */
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn(">>> [REDIS GET ERROR] Failed to fetch from cache '{}' for key '{}'. " +
                 "Gracefully falling back to Database. Error: {}", cache.getName(), key, exception.getMessage());
    }

    /**
     * When Redis PUT fails:
     * We suppress the exception so the caller still gets their response without an internal server error.
     */
    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn(">>> [REDIS PUT ERROR] Failed to put value into cache '{}' for key '{}'. Error: {}", 
                cache.getName(), key, exception.getMessage());
    }

    /**
     * When Redis EVICT fails:
     * We suppress the exception so the DB write transaction is not rolled back.
     * Stale cache risk is mitigated by a short TTL and/or async reconciliation.
     */
    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn(">>> [REDIS EVICT ERROR] Failed to evict key '{}' from cache '{}'. " +
                 "Warning: Cache entry might be stale until TTL expires. Error: {}", 
                key, cache.getName(), exception.getMessage());
    }

    /**
     * When Redis CLEAR fails:
     */
    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn(">>> [REDIS CLEAR ERROR] Failed to clear cache '{}'. Error: {}", 
                cache.getName(), exception.getMessage());
    }
}
