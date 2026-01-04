package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for tenant-level analytics aggregated across all shops.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAnalyticsDto {

    private String tenantId;
    private String tenantName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    // Shop metrics
    private Integer totalShops;
    private Integer activeShops;
    private Integer suspendedShops;

    // Sales metrics (aggregated across all shops)
    private BigDecimal totalRevenue;
    private BigDecimal totalProfit;
    private Long totalTransactions;
    private BigDecimal averageTransactionValue;

    // Inventory metrics
    private Long totalProducts;
    private Long totalInventoryBatches;
    private BigDecimal totalInventoryValue;

    // Investment metrics
    private BigDecimal totalInvestments;
    private BigDecimal totalInvestorShares;
    private BigDecimal totalDistributions;

    // User metrics
    private Integer totalUsers;
    private Integer activeUsers;

    // API usage
    private Long apiRequestsThisPeriod;
    private Long apiRequestsRemaining;

    // Storage usage
    private BigDecimal storageUsedGb;
    private BigDecimal storageRemainingGb;

    // Top performing shops
    private List<ShopPerformanceDto> topShopsByRevenue;

    // Growth metrics
    private BigDecimal revenueGrowthPercentage;
    private BigDecimal profitGrowthPercentage;
}
