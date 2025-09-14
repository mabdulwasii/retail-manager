package com.princely.shopmanager.shared.events;

import java.time.LocalDateTime;

public record ShopCreatedEvent(
    String shopId,
    String tenantId,
    String shopName,
    LocalDateTime timestamp
) {
    public ShopCreatedEvent(String shopId, String tenantId, String shopName) {
        this(shopId, tenantId, shopName, LocalDateTime.now());
    }
}