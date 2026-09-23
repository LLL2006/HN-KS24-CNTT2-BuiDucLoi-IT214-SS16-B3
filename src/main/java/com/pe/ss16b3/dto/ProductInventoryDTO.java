package com.pe.ss16b3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productName;
    private Integer quantity;
    private LocalDateTime lastUpdated;
}
