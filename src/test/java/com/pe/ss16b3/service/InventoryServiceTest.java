package com.pe.ss16b3.service;

import com.pe.ss16b3.dto.ProductInventoryDTO;
import com.pe.ss16b3.exception.InvalidInventoryQuantityException;
import com.pe.ss16b3.exception.ProductNotFoundException;
import com.pe.ss16b3.model.ProductInventory;
import com.pe.ss16b3.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private ProductInventory sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = ProductInventory.builder()
                .productId("iphone-15")
                .productName("iPhone 15")
                .quantity(100)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Read Inventory - Success (Return valid DTO)")
    void testGetInventory_Success() {
        when(inventoryRepository.findById("iphone-15")).thenReturn(Optional.of(sampleProduct));

        ProductInventoryDTO result = inventoryService.getInventory("iphone-15");

        assertNotNull(result);
        assertEquals("iphone-15", result.getProductId());
        assertEquals("iPhone 15", result.getProductName());
        assertEquals(100, result.getQuantity());
        verify(inventoryRepository, times(1)).findById("iphone-15");
    }

    @Test
    @DisplayName("Read Inventory - Throws ProductNotFoundException when product does not exist")
    void testGetInventory_NotFound() {
        when(inventoryRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> {
            inventoryService.getInventory("non-existent");
        });
        verify(inventoryRepository, times(1)).findById("non-existent");
    }

    @Test
    @DisplayName("Read Inventory - Throws IllegalArgumentException on blank productId")
    void testGetInventory_BlankProductId() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.getInventory("  ");
        });
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Update Inventory - Success (Updates DB with new positive quantity)")
    void testUpdateInventory_Success() {
        when(inventoryRepository.findById("iphone-15")).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.save(any(ProductInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductInventoryDTO result = inventoryService.updateInventory("iphone-15", 95);

        assertNotNull(result);
        assertEquals(95, result.getQuantity());
        verify(inventoryRepository, times(1)).findById("iphone-15");
        verify(inventoryRepository, times(1)).save(any(ProductInventory.class));
    }

    @Test
    @DisplayName("Update Inventory - Trapping negative quantity (Throws InvalidInventoryQuantityException)")
    void testUpdateInventory_NegativeQuantity() {
        // Tình huống 1: newQuantity bị nhập số âm (-10)
        assertThrows(InvalidInventoryQuantityException.class, () -> {
            inventoryService.updateInventory("iphone-15", -10);
        });

        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Update Inventory - Trapping null quantity (Throws IllegalArgumentException)")
    void testUpdateInventory_NullQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.updateInventory("iphone-15", null);
        });

        verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("Update Inventory - Zero quantity is allowed (Stock = 0)")
    void testUpdateInventory_ZeroQuantity_Allowed() {
        when(inventoryRepository.findById("iphone-15")).thenReturn(Optional.of(sampleProduct));
        when(inventoryRepository.save(any(ProductInventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductInventoryDTO result = inventoryService.updateInventory("iphone-15", 0);

        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        verify(inventoryRepository, times(1)).save(any(ProductInventory.class));
    }
}
