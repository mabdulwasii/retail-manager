package com.princely.shopmanager.analytics.controller;

import com.princely.shopmanager.analytics.dto.SalesSummaryDto;
import com.princely.shopmanager.analytics.dto.InvestmentRoiDto;
import com.princely.shopmanager.analytics.dto.FraudStatisticsDto;
import com.princely.shopmanager.analytics.dto.RevenueAnalyticsDto;
import com.princely.shopmanager.analytics.service.AnalyticsService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST Controller for analytics operations.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.features.analytics.enabled", havingValue = "true")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sales-summary")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ANALYTICS_SALES_VIEW)")
    public ResponseEntity<SalesSummaryDto> getSalesSummary(
            @RequestParam String shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        SalesSummaryDto summary = analyticsService.getSalesSummary(shopId, startDate, endDate, principal);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/investment-roi")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ANALYTICS_INVESTMENT_VIEW)")
    public ResponseEntity<InvestmentRoiDto> getInvestmentROI(
            @RequestParam String shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InvestmentRoiDto roi = analyticsService.getInvestmentROI(shopId, startDate, endDate, principal);
        return ResponseEntity.ok(roi);
    }

    @GetMapping("/fraud-statistics")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).FRAUD_VIEW)")
    public ResponseEntity<FraudStatisticsDto> getFraudStatistics(
            @RequestParam String shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        FraudStatisticsDto stats = analyticsService.getFraudStatistics(shopId, startDate, endDate, principal);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue-analytics")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ANALYTICS_SALES_VIEW)")
    public ResponseEntity<RevenueAnalyticsDto> getRevenueAnalytics(
            @RequestParam String shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        RevenueAnalyticsDto analytics = analyticsService.getRevenueAnalytics(shopId, startDate, endDate, principal);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/clear-cache/{shopId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ANALYTICS_MANAGE)")
    public ResponseEntity<Void> clearCacheForShop(
            @PathVariable String shopId,
            @AuthenticationPrincipal JwtPrincipal principal) {
        analyticsService.clearCacheForShop(shopId, principal);
        return ResponseEntity.ok().build();
    }
}