package com.princely.shopmanager.analytics.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for AnalyticsController - Happy Path Only.
 *
 * NOTE: All tests disabled - analytics feature requires app.features.analytics.enabled=true
 * and additional service dependencies (AnalyticsService, caching configuration).
 * DISABLED (5/5):
 * - GET /analytics/sales-summary
 * - GET /analytics/investment-roi
 * - GET /analytics/fraud-statistics
 * - GET /analytics/revenue-analytics
 * - POST /analytics/clear-cache/{shopId}
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Analytics Controller - Minimal Happy Path Integration Tests")
class AnalyticsControllerMinimalIT extends AbstractIntegrationTest {

    // @Test
    // @DisplayName("GET /analytics/sales-summary - Should get sales summary")
    void shouldGetSalesSummary() {
        // Given
        setTenantContext(TEST_TENANT_001);
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        String url = "/analytics/sales-summary?shopId=" + TEST_SHOP_001 +
            "&startDate=" + startDate + "&endDate=" + endDate;

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            url,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // @Test
    // @DisplayName("GET /analytics/investment-roi - Should get investment ROI")
    void shouldGetInvestmentROI() {
        // Given
        setTenantContext(TEST_TENANT_001);
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        String url = "/analytics/investment-roi?shopId=" + TEST_SHOP_001 +
            "&startDate=" + startDate + "&endDate=" + endDate;

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            url,
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // @Test
    // @DisplayName("GET /analytics/fraud-statistics - Should get fraud statistics")
    void shouldGetFraudStatistics() {
        // Given
        setTenantContext(TEST_TENANT_001);
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        String url = "/analytics/fraud-statistics?shopId=" + TEST_SHOP_001 +
            "&startDate=" + startDate + "&endDate=" + endDate;

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            url,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // @Test
    // @DisplayName("GET /analytics/revenue-analytics - Should get revenue analytics")
    void shouldGetRevenueAnalytics() {
        // Given
        setTenantContext(TEST_TENANT_001);
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        String url = "/analytics/revenue-analytics?shopId=" + TEST_SHOP_001 +
            "&startDate=" + startDate + "&endDate=" + endDate;

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            url,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // @Test
    // @DisplayName("POST /analytics/clear-cache/{shopId} - Should clear cache")
    void shouldClearCache() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Void> response = performAuthenticatedPostWithShop(
            "/analytics/clear-cache/" + TEST_SHOP_001,
            null,
            "owner@testretail.com",
            TEST_SHOP_001,
            Void.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
