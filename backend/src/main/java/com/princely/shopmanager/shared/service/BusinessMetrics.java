package com.princely.shopmanager.shared.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "features.metrics.business.enabled", havingValue = "true", matchIfMissing = true)
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter shopCreationCounter;
    private final Timer profitCalculationTimer;
    private final Counter inventoryUpdatesCounter;
    private final Counter lowStockAlertsCounter;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.shopCreationCounter = Counter.builder("shops.created")
            .description("Number of shops created")
            .register(meterRegistry);
        this.profitCalculationTimer = Timer.builder("profit.calculation.duration")
            .description("Time taken to calculate profit distributions")
            .register(meterRegistry);
        this.inventoryUpdatesCounter = Counter.builder("inventory.updates")
            .description("Number of inventory updates")
            .register(meterRegistry);
        this.lowStockAlertsCounter = Counter.builder("inventory.low_stock_alerts")
            .description("Number of low stock alerts generated")
            .register(meterRegistry);
    }

    public void recordShopCreation(String tenantId) {
        Counter.builder("shops.created")
            .tags("tenant", tenantId)
            .register(meterRegistry)
            .increment();
    }

    public void recordProfitCalculation(Duration duration) {
        profitCalculationTimer.record(duration);
    }

    public void recordInventoryUpdate(String shopId, String updateType) {
        Counter.builder("inventory.updates")
            .tags("shop", shopId, "type", updateType)
            .register(meterRegistry)
            .increment();
    }

    public void recordLowStockAlert(String shopId, String productId) {
        Counter.builder("inventory.low_stock_alerts")
            .tags("shop", shopId, "product", productId)
            .register(meterRegistry)
            .increment();
    }

    public void recordBusinessOperation(String operation, String result) {
        Counter.builder("business.operations")
            .description("Business operations performed")
            .tags("operation", operation, "result", result)
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startTimer(String timerName) {
        Timer timer = Timer.builder(timerName)
            .register(meterRegistry);
        return Timer.start(meterRegistry);
    }
}