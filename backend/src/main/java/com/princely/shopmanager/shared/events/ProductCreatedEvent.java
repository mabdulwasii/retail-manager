package com.princely.shopmanager.shared.events;

import java.time.LocalDateTime;

/**
 * Event published when a new product is created in the catalog.
 */
public record ProductCreatedEvent(
    String productId,
    String shopId,
    String productName,
    String sku,
    LocalDateTime timestamp
) {
    public ProductCreatedEvent(String productId, String shopId, String productName, String sku) {
        this(productId, shopId, productName, sku, LocalDateTime.now());
    }
}
