package com.pe.ss16b3.repository;

import com.pe.ss16b3.model.ProductInventory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {

    private static final Logger log = LoggerFactory.getLogger(InMemoryInventoryRepository.class);
    private final Map<String, ProductInventory> database = new ConcurrentHashMap<>();

    @PostConstruct
    public void initData() {
        log.info("[DATABASE] Initializing sample inventory data in Database...");
        database.put("iphone-15", ProductInventory.builder()
                .productId("iphone-15")
                .productName("iPhone 15")
                .quantity(100)
                .lastUpdated(LocalDateTime.now())
                .build());

        database.put("macbook-pro-m3", ProductInventory.builder()
                .productId("macbook-pro-m3")
                .productName("MacBook Pro M3")
                .quantity(50)
                .lastUpdated(LocalDateTime.now())
                .build());

        database.put("samsung-s24", ProductInventory.builder()
                .productId("samsung-s24")
                .productName("Samsung Galaxy S24 Ultra")
                .quantity(80)
                .lastUpdated(LocalDateTime.now())
                .build());
    }

    @Override
    public Optional<ProductInventory> findById(String productId) {
        log.info(">>> [DATABASE QUERY] Fetching inventory from Database for productId: {}", productId);
        ProductInventory item = database.get(productId);
        return Optional.ofNullable(item);
    }

    @Override
    public ProductInventory save(ProductInventory inventory) {
        log.info(">>> [DATABASE UPDATE] Saving inventory to Database: productId={}, quantity={}", 
                inventory.getProductId(), inventory.getQuantity());
        inventory.setLastUpdated(LocalDateTime.now());
        database.put(inventory.getProductId(), inventory);
        return inventory;
    }

    @Override
    public boolean existsById(String productId) {
        return database.containsKey(productId);
    }
}
