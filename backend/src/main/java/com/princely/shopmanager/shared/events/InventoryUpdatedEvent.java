package com.princely.shopmanager.shared.events;

import java.time.LocalDateTime;

public record InventoryUpdatedEvent(
    String inventoryId,
    String productId,
    String shopId,
    int previousStock,
    int newStock,
    String updateType,
    LocalDateTime timestamp
) {
    public InventoryUpdatedEvent(String inventoryId, String productId, String shopId, int previousStock, int newStock, String updateType) {
        this(inventoryId, productId, shopId, previousStock, newStock, updateType, LocalDateTime.now());
    }
}