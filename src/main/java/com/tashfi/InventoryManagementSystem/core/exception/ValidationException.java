package com.tashfi.InventoryManagementSystem.core.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}