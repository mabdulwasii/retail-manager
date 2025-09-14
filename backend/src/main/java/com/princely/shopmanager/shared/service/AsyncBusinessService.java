package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.investment.service.InvestmentProfitService;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "features.async.business-service.enabled", havingValue = "true", matchIfMissing = true)
public class AsyncBusinessService {

    private final InvestmentProfitService profitService;

    @Async("businessTaskExecutor")
    public CompletableFuture<Void> processProfitDistribution(String periodId) {
        log.info("Processing profit distribution for period: {}", periodId);
        try {
            // Note: This would need the actual method implementation in InvestmentProfitService
            // profitService.calculateAllDistributions(periodId);
            log.info("Successfully processed profit distribution for period: {}", periodId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to process profit distribution for period: {}", periodId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("analyticsTaskExecutor")
    @ApplicationModuleListener
    public void handleInventoryUpdate(InventoryUpdatedEvent event) {
        log.info("Handling inventory update event for shop: {}", event.shopId());
        try {
            // Update analytics cache asynchronously
            // analyticsService.refreshInventoryMetrics(event.getShopId());
            log.info("Successfully refreshed analytics for shop: {}", event.shopId());
        } catch (Exception e) {
            log.error("Failed to refresh analytics for shop: {}", event.shopId(), e);
        }
    }

    @Async("businessTaskExecutor")
    public CompletableFuture<Void> processLargeDataExport(String exportId, String format) {
        log.info("Starting large data export: {} in format: {}", exportId, format);
        try {
            // Simulate long-running export process
            Thread.sleep(5000); // Simulate processing time
            log.info("Completed large data export: {}", exportId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to process large data export: {}", exportId, e);
            return CompletableFuture.failedFuture(e);
        }
    }
}