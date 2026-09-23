package com.pe.ss16b3.repository;

import com.pe.ss16b3.model.ProductInventory;

import java.util.Optional;

public interface InventoryRepository {
    Optional<ProductInventory> findById(String productId);
    ProductInventory save(ProductInventory inventory);
    boolean existsById(String productId);
}
