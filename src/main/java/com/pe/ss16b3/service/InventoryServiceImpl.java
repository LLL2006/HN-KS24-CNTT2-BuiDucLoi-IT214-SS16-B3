package com.pe.ss16b3.service;

import com.pe.ss16b3.dto.ProductInventoryDTO;
import com.pe.ss16b3.exception.InvalidInventoryQuantityException;
import com.pe.ss16b3.exception.ProductNotFoundException;
import com.pe.ss16b3.model.ProductInventory;
import com.pe.ss16b3.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private final InventoryRepository inventoryRepository;

    public static final String CACHE_NAME = "product_inventory";

    /**
     * Cache-Aside Read:
     * 1. Spring Cache checks Redis for key `productId`.
     * 2. If HIT, returns cached value directly (this method is NOT executed).
     * 3. If MISS, this method executes -> loads from DB -> Spring Cache puts result into Redis.
     */
    @Override
    @Cacheable(value = CACHE_NAME, key = "#productId")
    public ProductInventoryDTO getInventory(String productId) {
        validateProductId(productId);

        log.info("[CACHE MISS / SERVICE EXECUTION] Querying DB for inventory of product: {}", productId);
        ProductInventory item = inventoryRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return toDTO(item);
    }

    /**
     * Cache-Aside Write:
     * 1. Validates inputs (chặn số lượng âm, kiểm tra productId).
     * 2. Writes updated data to Database first.
     * 3. Spring Cache executes @CacheEvict to remove stale key from Redis after DB update.
     */
    @Override
    @CacheEvict(value = CACHE_NAME, key = "#productId")
    public ProductInventoryDTO updateInventory(String productId, Integer newQuantity) {
        validateProductId(productId);
        validateQuantity(newQuantity);

        log.info("[SERVICE WRITE] Updating stock in DB for product: {} with new quantity: {}", productId, newQuantity);

        ProductInventory item = inventoryRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        item.setQuantity(newQuantity);
        item.setLastUpdated(LocalDateTime.now());
        ProductInventory saved = inventoryRepository.save(item);

        log.info("[SERVICE WRITE SUCCESS] Successfully updated DB for productId: {}. Evicting cache entry...", productId);
        return toDTO(saved);
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null.");
        }
        if (quantity < 0) {
            log.error("[VALIDATION ERROR] Attempted to update negative quantity: {}", quantity);
            throw new InvalidInventoryQuantityException(quantity);
        }
    }

    private ProductInventoryDTO toDTO(ProductInventory inventory) {
        return ProductInventoryDTO.builder()
                .productId(inventory.getProductId())
                .productName(inventory.getProductName())
                .quantity(inventory.getQuantity())
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }
}
