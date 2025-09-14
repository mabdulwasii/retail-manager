package com.princely.shopmanager.inventory.events;

import com.princely.shopmanager.shared.events.InventoryLowStockEvent;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "features.inventory.event-handling.enabled", havingValue = "true", matchIfMissing = true)
public class InventoryEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventHandler.class);

    @Async
    @ApplicationModuleListener
    public void handleLowStock(InventoryLowStockEvent event) {
        logger.warn("Low stock alert for product {} in shop {}. Current: {}, Minimum: {}",
            event.productName(), event.shopId(), event.currentStock(), event.minimumStock());

        // Future: Send notifications, trigger reorder alerts
        // notificationService.sendLowStockAlert(event);
    }

    @Async
    @ApplicationModuleListener
    public void handleInventoryUpdate(InventoryUpdatedEvent event) {
        logger.info("Inventory updated for product {} in shop {}. Previous: {}, New: {}, Type: {}",
            event.productId(), event.shopId(), event.previousStock(), event.newStock(), event.updateType());

        // Future: Update analytics cache, trigger business rules
        // analyticsService.refreshInventoryMetrics(event.getShopId());
    }
}