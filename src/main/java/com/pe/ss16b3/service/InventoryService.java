package com.pe.ss16b3.service;

import com.pe.ss16b3.dto.ProductInventoryDTO;

public interface InventoryService {
    /**
     * Read inventory details by productId.
     * Follows Cache-Aside: Checks Redis cache first, falls back to DB on cache miss.
     *
     * @param productId Product identifier (not null or empty)
     * @return ProductInventoryDTO containing inventory information
     */
    ProductInventoryDTO getInventory(String productId);

    /**
     * Update inventory quantity for a product.
     * Follows Cache-Aside: Writes to DB first, then evicts cache.
     *
     * @param productId Product identifier (not null or empty)
     * @param newQuantity New stock quantity (must be >= 0)
     * @return ProductInventoryDTO containing updated inventory information
     */
    ProductInventoryDTO updateInventory(String productId, Integer newQuantity);
}
