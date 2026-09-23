package com.pe.ss16b3.exception;

public class InvalidInventoryQuantityException extends RuntimeException {
    public InvalidInventoryQuantityException(String message) {
        super(message);
    }

    public InvalidInventoryQuantityException(Integer quantity) {
        super("Invalid inventory quantity: " + quantity + ". Quantity must be greater than or equal to 0.");
    }
}
