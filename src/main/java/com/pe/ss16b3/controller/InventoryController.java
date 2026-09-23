package com.pe.ss16b3.controller;

import com.pe.ss16b3.dto.ApiResponse;
import com.pe.ss16b3.dto.ProductInventoryDTO;
import com.pe.ss16b3.dto.UpdateInventoryRequest;
import com.pe.ss16b3.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    /**
     * Read inventory details by productId.
     * GET /api/v1/inventory/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> getInventory(@PathVariable String productId) {
        log.info("[REST API] GET /api/v1/inventory/{}", productId);
        ProductInventoryDTO dto = inventoryService.getInventory(productId);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Inventory retrieved successfully"));
    }

    /**
     * Update inventory via PUT with request body.
     * PUT /api/v1/inventory/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> updateInventory(
            @PathVariable String productId,
            @RequestParam(required = false) Integer newQuantity,
            @Valid @RequestBody(required = false) UpdateInventoryRequest body) {
        
        Integer quantityToUpdate = (body != null && body.getNewQuantity() != null) 
                ? body.getNewQuantity() 
                : newQuantity;

        if (quantityToUpdate == null) {
            throw new IllegalArgumentException("Parameter 'newQuantity' or request body with 'newQuantity' is required.");
        }

        log.info("[REST API] PUT /api/v1/inventory/{} -> newQuantity: {}", productId, quantityToUpdate);
        ProductInventoryDTO dto = inventoryService.updateInventory(productId, quantityToUpdate);
        return ResponseEntity.ok(ApiResponse.ok(dto, "Inventory updated successfully"));
    }

    /**
     * Update inventory via POST with JSON body.
     * POST /api/v1/inventory/update
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<ProductInventoryDTO>> updateInventoryPost(
            @Valid @RequestBody UpdateInventoryRequest request) {
        log.info("[REST API] POST /api/v1/inventory/update -> productId: {}, newQuantity: {}", 
                request.getProductId(), request.getNewQuantity());
        ProductInventoryDTO dto = inventoryService.updateInventory(request.getProductId(), request.getNewQuantity());
        return ResponseEntity.ok(ApiResponse.ok(dto, "Inventory updated successfully"));
    }
}
