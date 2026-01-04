package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.dto.SyncStatusDto;
import com.princely.shopmanager.aggregator.dto.TenantAnalyticsDto;
import com.princely.shopmanager.aggregator.service.CloudAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Analytics Controller.
 * Provides aggregated analytics and reporting for cloud tenants.
 */
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Aggregated analytics and reporting")
public class AnalyticsController {

    private final CloudAnalyticsService analyticsService;

    /**
     * Get tenant-level analytics aggregated across all shops.
     *
     * GET /api/cloud/tenants/{tenantId}/analytics
     *
     * @param tenantId Tenant ID
     * @param periodStart Period start date (optional, defaults to 30 days ago)
     * @param periodEnd Period end date (optional, defaults to now)
     * @return Aggregated analytics
     */
    @GetMapping
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

        // Default to last 30 days if not specified
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
     *
     * GET /api/cloud/tenants/{tenantId}/analytics/sync-status
     *
     * @param tenantId Tenant ID
     * @return List of shop sync statuses
     */
    @GetMapping("/sync-status")
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
     * Get platform-wide overview (admin endpoint).
     *
     * GET /api/cloud/analytics/platform
     *
     * @return Platform overview
     */
    @GetMapping("/platform")
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
}
