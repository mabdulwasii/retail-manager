package com.princely.shopmanager.shared.exception;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String productId, int available, int requested) {
        super("INSUFFICIENT_STOCK",
            String.format("Insufficient stock for product %s. Available: %d, Requested: %d",
                productId, available, requested));
    }
}