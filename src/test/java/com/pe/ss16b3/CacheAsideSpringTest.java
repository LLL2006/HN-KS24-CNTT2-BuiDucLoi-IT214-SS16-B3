package com.pe.ss16b3;

import com.pe.ss16b3.dto.ProductInventoryDTO;
import com.pe.ss16b3.model.ProductInventory;
import com.pe.ss16b3.repository.InventoryRepository;
import com.pe.ss16b3.service.InventoryService;
import com.pe.ss16b3.service.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        CacheAsideSpringTest.TestCacheConfig.class,
        InventoryServiceImpl.class
})
class CacheAsideSpringTest {

    @Configuration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(InventoryServiceImpl.CACHE_NAME);
        }
    }

    @MockitoBean
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CacheManager cacheManager;

    private ProductInventory product;

    @BeforeEach
    void setUp() {
        // Clear caches before each test
        cacheManager.getCache(InventoryServiceImpl.CACHE_NAME).clear();

        product = ProductInventory.builder()
                .productId("iphone-15")
                .productName("iPhone 15")
                .quantity(100)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Cache-Aside Flow: Read (Miss -> Hit) -> Write (Update DB & Evict) -> Read (Miss & Load new data)")
    void testCacheAsideFullLifecycle() {
        when(inventoryRepository.findById("iphone-15")).thenReturn(Optional.of(product));
        when(inventoryRepository.save(any(ProductInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. First Read: Cache MISS -> Repository findById MUST be called 1 time
        ProductInventoryDTO firstRead = inventoryService.getInventory("iphone-15");
        assertNotNull(firstRead);
        assertEquals(100, firstRead.getQuantity());
        verify(inventoryRepository, times(1)).findById("iphone-15");

        // 2. Second Read: Cache HIT -> Repository findById should NOT be called again (still 1 time)
        ProductInventoryDTO secondRead = inventoryService.getInventory("iphone-15");
        assertNotNull(secondRead);
        assertEquals(100, secondRead.getQuantity());
        verify(inventoryRepository, times(1)).findById("iphone-15");

        // 3. Write Operation: Update stock to 95 -> DB updated (findById called to fetch entity + save called) and Cache evicted
        ProductInventoryDTO updateResult = inventoryService.updateInventory("iphone-15", 95);
        assertNotNull(updateResult);
        assertEquals(95, updateResult.getQuantity());
        verify(inventoryRepository, times(2)).findById("iphone-15");
        verify(inventoryRepository, times(1)).save(any(ProductInventory.class));

        // 4. Third Read: Cache MISS (due to eviction) -> Repository findById called again (total 3 times)
        ProductInventoryDTO thirdRead = inventoryService.getInventory("iphone-15");
        assertNotNull(thirdRead);
        assertEquals(95, thirdRead.getQuantity());
        verify(inventoryRepository, times(3)).findById("iphone-15");
    }
}
