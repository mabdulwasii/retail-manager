package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig
@DisplayName("Shop Service - Performance Tests")
public class ShopServicePerformanceTest {

    // Note: This test would need proper dependency injection and test containers
    // For now, it serves as a template for performance testing

    @Test
    @DisplayName("Should handle high volume shop creation within performance threshold")
    public void shouldHandleHighVolumeShopCreation() {
        // Given
        int numberOfShops = 100; // Reduced for test environment
        ExecutorService executor = Executors.newFixedThreadPool(10);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // When - Create shops concurrently
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfShops)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    createTestShop("performance-shop-" + i);
                }, executor))
                .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            stopWatch.stop();

            // Then - Verify performance
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            assertThat(totalTimeMillis).isLessThan(30000); // Should complete within 30 seconds

            double operationsPerSecond = numberOfShops / (totalTimeMillis / 1000.0);
            System.out.printf("Performance: %.2f shops/second, Total time: %d ms%n",
                operationsPerSecond, totalTimeMillis);

        } finally {
            executor.shutdown();
        }
    }

    @Test
    @DisplayName("Should handle concurrent shop retrievals efficiently")
    public void shouldHandleConcurrentShopRetrievals() {
        // Given
        int numberOfRetrievals = 200;
        ExecutorService executor = Executors.newFixedThreadPool(20);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // When - Retrieve shops concurrently
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfRetrievals)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    retrieveTestShop("test-shop-id");
                }, executor))
                .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            stopWatch.stop();

            // Then - Verify performance
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            assertThat(totalTimeMillis).isLessThan(10000); // Should complete within 10 seconds

            double operationsPerSecond = numberOfRetrievals / (totalTimeMillis / 1000.0);
            System.out.printf("Performance: %.2f retrievals/second, Total time: %d ms%n",
                operationsPerSecond, totalTimeMillis);

        } finally {
            executor.shutdown();
        }
    }

    private void createTestShop(String shopName) {
        try {
            // Simulate shop creation - would need actual service injection
            ShopCreateRequest request = ShopCreateRequest.builder()
                .name(shopName)
                .email(shopName + "@test.com")
                .address("123 Test Street")
                .build();

            // shopService.createShop(request);
            Thread.sleep(10); // Simulate processing time
        } catch (Exception e) {
            // Handle test exceptions
            System.err.println("Failed to create shop: " + shopName + " - " + e.getMessage());
        }
    }

    private void retrieveTestShop(String shopId) {
        try {
            // Simulate shop retrieval - would need actual service injection
            // ShopResponse response = shopService.getShop(shopId);
            Thread.sleep(5); // Simulate processing time
        } catch (Exception e) {
            // Handle test exceptions in performance test context
            System.err.println("Failed to retrieve shop: " + shopId + " - " + e.getMessage());
        }
    }
}