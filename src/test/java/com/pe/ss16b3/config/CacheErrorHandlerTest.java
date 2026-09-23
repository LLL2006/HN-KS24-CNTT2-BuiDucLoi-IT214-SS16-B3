package com.pe.ss16b3.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheErrorHandlerTest {

    private CustomCacheErrorHandler errorHandler;

    @Mock
    private Cache cache;

    @BeforeEach
    void setUp() {
        errorHandler = new CustomCacheErrorHandler();
    }

    @Test
    @DisplayName("Redis GET Error - Suppresses exception and allows graceful DB fallback")
    void testHandleCacheGetError() {
        when(cache.getName()).thenReturn("product_inventory");
        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis connection timed out");

        // Should not throw any exception to allow Spring Cache fallback
        assertDoesNotThrow(() -> errorHandler.handleCacheGetError(redisException, cache, "iphone-15"));
    }

    @Test
    @DisplayName("Redis PUT Error - Suppresses exception to avoid failing user request")
    void testHandleCachePutError() {
        when(cache.getName()).thenReturn("product_inventory");
        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis connection refused");

        assertDoesNotThrow(() -> errorHandler.handleCachePutError(redisException, cache, "iphone-15", "some-value"));
    }

    @Test
    @DisplayName("Redis EVICT Error - Suppresses exception and logs warning for TTL reconciliation")
    void testHandleCacheEvictError() {
        when(cache.getName()).thenReturn("product_inventory");
        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis cluster unavailable");

        assertDoesNotThrow(() -> errorHandler.handleCacheEvictError(redisException, cache, "iphone-15"));
    }

    @Test
    @DisplayName("Redis CLEAR Error - Suppresses exception")
    void testHandleCacheClearError() {
        when(cache.getName()).thenReturn("product_inventory");
        RedisConnectionFailureException redisException = new RedisConnectionFailureException("Redis connection error");

        assertDoesNotThrow(() -> errorHandler.handleCacheClearError(redisException, cache));
    }
}
