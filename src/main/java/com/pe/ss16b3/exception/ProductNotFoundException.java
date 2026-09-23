package com.pe.ss16b3.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("Product not found with ID: " + productId);
    }
}
