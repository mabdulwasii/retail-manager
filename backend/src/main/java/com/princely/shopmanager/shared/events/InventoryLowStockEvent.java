package com.princely.shopmanager.shared.events;

import java.time.LocalDateTime;

public record InventoryLowStockEvent(
    String inventoryId,
    String productName,
    String shopId,
    int currentStock,
    int minimumStock,
    LocalDateTime timestamp
) {
    public InventoryLowStockEvent(String inventoryId, String productName, String shopId, int currentStock, int minimumStock) {
        this(inventoryId, productName, shopId, currentStock, minimumStock, LocalDateTime.now());
    }
}