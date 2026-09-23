package com.pe.ss16b3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.ss16b3.dto.ProductInventoryDTO;
import com.pe.ss16b3.dto.UpdateInventoryRequest;
import com.pe.ss16b3.exception.GlobalExceptionHandler;
import com.pe.ss16b3.exception.ProductNotFoundException;
import com.pe.ss16b3.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{productId} - 200 OK")
    void testGetInventory_Success() throws Exception {
        ProductInventoryDTO dto = ProductInventoryDTO.builder()
                .productId("iphone-15")
                .productName("iPhone 15")
                .quantity(100)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryService.getInventory("iphone-15")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/inventory/iphone-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value("iphone-15"))
                .andExpect(jsonPath("$.data.quantity").value(100));
    }

    @Test
    @DisplayName("GET /api/v1/inventory/{productId} - 404 Not Found")
    void testGetInventory_NotFound() throws Exception {
        when(inventoryService.getInventory("invalid-id")).thenThrow(new ProductNotFoundException("invalid-id"));

        mockMvc.perform(get("/api/v1/inventory/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found with ID: invalid-id"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/update - Trapping negative quantity (400 Bad Request)")
    void testUpdateInventory_NegativeQuantity_BadRequest() throws Exception {
        UpdateInventoryRequest invalidRequest = UpdateInventoryRequest.builder()
                .productId("iphone-15")
                .newQuantity(-10) // Invalid negative quantity
                .build();

        mockMvc.perform(post("/api/v1/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.newQuantity").value("Quantity must be greater than or equal to 0"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/update - Blank productId (400 Bad Request)")
    void testUpdateInventory_BlankProductId_BadRequest() throws Exception {
        UpdateInventoryRequest invalidRequest = UpdateInventoryRequest.builder()
                .productId("") // Blank productId
                .newQuantity(50)
                .build();

        mockMvc.perform(post("/api/v1/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.productId").value("Product ID cannot be blank"));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/update - Valid Request - 200 OK")
    void testUpdateInventory_Success() throws Exception {
        UpdateInventoryRequest validRequest = UpdateInventoryRequest.builder()
                .productId("iphone-15")
                .newQuantity(95)
                .build();

        ProductInventoryDTO updatedDto = ProductInventoryDTO.builder()
                .productId("iphone-15")
                .productName("iPhone 15")
                .quantity(95)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(inventoryService.updateInventory(eq("iphone-15"), eq(95))).thenReturn(updatedDto);

        mockMvc.perform(post("/api/v1/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(95));
    }
}
