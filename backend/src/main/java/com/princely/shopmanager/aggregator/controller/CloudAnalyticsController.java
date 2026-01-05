package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.dto.*;
import com.princely.shopmanager.aggregator.service.CloudAnalyticsService;
import com.princely.shopmanager.aggregator.service.CrossShopAnalyticsService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analytics Controller.
 * Provides aggregated analytics and reporting for cloud tenants.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cloud Analytics", description = "Aggregated analytics and reporting")
public class CloudAnalyticsController {

    private final CloudAnalyticsService analyticsService;
    private final CrossShopAnalyticsService crossShopAnalyticsService;

    // ==================== Tenant-specific analytics endpoints ====================

    /**
     * Get tenant-level analytics aggregated across all shops.
     * GET /api/cloud/tenants/{tenantId}/analytics
     */
    @GetMapping("/api/cloud/tenants/{tenantId}/analytics")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_REVENUE_VIEW)")
    @Operation(summary = "Get tenant analytics",
            description = "Get aggregated analytics across all shops for a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved analytics"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<TenantAnalyticsDto> getTenantAnalytics(
            @PathVariable String tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime periodEnd) {

        log.info("Getting analytics for tenant: {}", tenantId);

        if (periodStart == null) {
            periodStart = LocalDateTime.now().minusDays(30);
        }
        if (periodEnd == null) {
            periodEnd = LocalDateTime.now();
        }

        TenantAnalyticsDto analytics = analyticsService.getTenantAnalytics(tenantId, periodStart, periodEnd);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get sync status for all shops under a tenant.
     * GET /api/cloud/tenants/{tenantId}/analytics/sync-status
     */
    @GetMapping("/api/cloud/tenants/{tenantId}/analytics/sync-status")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_REVENUE_VIEW)")
    @Operation(summary = "Get shop sync status",
            description = "Get sync status for all shops under a tenant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sync status"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<List<SyncStatusDto>> getShopSyncStatus(@PathVariable String tenantId) {
        log.info("Getting sync status for tenant: {}", tenantId);
        List<SyncStatusDto> syncStatus = analyticsService.getShopSyncStatus(tenantId);
        return ResponseEntity.ok(syncStatus);
    }

    /**
     * Get a platform-wide overview (admin endpoint).
     * GET /api/cloud/analytics/platform
     */
    @GetMapping("/api/cloud/analytics/platform")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_PLATFORM_VIEW)")
    @Operation(summary = "Get platform overview",
            description = "Get platform-wide statistics (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved platform overview")
    })
    public ResponseEntity<CloudAnalyticsService.PlatformOverviewDto> getPlatformOverview() {
        log.info("Getting platform overview");
        CloudAnalyticsService.PlatformOverviewDto overview = analyticsService.getPlatformOverview();
        return ResponseEntity.ok(overview);
    }

    // ==================== Cross-shop analytics endpoints (for frontend) ====================

    /**
     * Get revenue analytics for a tenant across selected shops.
     * GET /api/cloud/analytics/revenue
     */
    @GetMapping("/api/cloud/analytics/revenue")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_REVENUE_VIEW)")
    @Operation(summary = "Get revenue analytics",
            description = "Get revenue analytics aggregated across selected shops")
    public ResponseEntity<RevenueAnalyticsDto> getRevenueAnalytics(
            @RequestParam String tenantId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String shopIds) {

        log.info("Getting revenue analytics for tenant: {} (period: {}, shops: {})", tenantId, period, shopIds);

        List<String> shopIdList = shopIds != null
                ? Arrays.stream(shopIds.split(",")).collect(Collectors.toList())
                : null;

        RevenueAnalyticsDto analytics = crossShopAnalyticsService.getRevenueAnalytics(
                tenantId, startDate, endDate, shopIdList);

        return ResponseEntity.ok(analytics);
    }

    /**
     * Get sales metrics for a tenant across selected shops.
     * GET /api/cloud/analytics/sales
     */
    @GetMapping("/api/cloud/analytics/sales")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_SALES_VIEW)")
    @Operation(summary = "Get sales metrics",
            description = "Get sales metrics aggregated across selected shops")
    public ResponseEntity<SalesMetricsDto> getSalesMetrics(
            @RequestParam String tenantId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String shopIds) {

        log.info("Getting sales metrics for tenant: {} (period: {}, shops: {})", tenantId, period, shopIds);

        List<String> shopIdList = shopIds != null
                ? Arrays.stream(shopIds.split(",")).collect(Collectors.toList())
                : null;

        SalesMetricsDto metrics = crossShopAnalyticsService.getSalesMetrics(
                tenantId, startDate, endDate, shopIdList);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Get top selling products for a tenant across selected shops.
     * GET /api/cloud/analytics/top-products
     */
    @GetMapping("/api/cloud/analytics/top-products")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_PRODUCTS_VIEW)")
    @Operation(summary = "Get top products",
            description = "Get top selling products aggregated across selected shops")
    public ResponseEntity<TopProductsAnalyticsDto> getTopProducts(
            @RequestParam String tenantId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String shopIds,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        log.info("Getting top {} products for tenant: {} (period: {}, shops: {})", limit, tenantId, period, shopIds);

        List<String> shopIdList = shopIds != null
                ? Arrays.stream(shopIds.split(",")).collect(Collectors.toList())
                : null;

        TopProductsAnalyticsDto analytics = crossShopAnalyticsService.getTopProducts(
                tenantId, startDate, endDate, shopIdList, limit);

        return ResponseEntity.ok(analytics);
    }

    /**
     * Get shop performance comparison for a tenant.
     * GET /api/cloud/analytics/shop-performance
     */
    @GetMapping("/api/cloud/analytics/shop-performance")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_PERFORMANCE_VIEW)")
    @Operation(summary = "Get shop performance",
            description = "Get performance comparison across all shops in a tenant")
    public ResponseEntity<ShopPerformanceAnalyticsDto> getShopPerformanceComparison(
            @RequestParam String tenantId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting shop performance for tenant: {} (period: {})", tenantId, period);

        ShopPerformanceAnalyticsDto analytics = crossShopAnalyticsService.getShopPerformance(
                tenantId, startDate, endDate);

        return ResponseEntity.ok(analytics);
    }

    /**
     * Export analytics data to CSV.
     * GET /api/cloud/analytics/export/csv
     */
    @GetMapping("/api/cloud/analytics/export/csv")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CLOUD_ANALYTICS_EXPORT)")
    @Operation(summary = "Export analytics to CSV",
            description = "Export analytics data to CSV format")
    public ResponseEntity<String> exportAnalyticsToCSV(
            @RequestParam String tenantId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String shopIds) {

        log.info("Exporting analytics to CSV for tenant: {} (period: {}, shops: {})", tenantId, period, shopIds);

        List<String> shopIdList = shopIds != null
                ? Arrays.stream(shopIds.split(",")).collect(Collectors.toList())
                : null;

        String csv = crossShopAnalyticsService.exportAnalyticsToCSV(
                tenantId, startDate, endDate, shopIdList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "analytics-export.csv");

        return ResponseEntity.ok()
            .headers(headers)
            .body(csv);
    }
}
