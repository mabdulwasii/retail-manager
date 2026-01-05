package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for revenue analytics aggregated across shops.
 * Matches frontend RevenueAnalytics interface in cloudAnalyticsService.ts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueAnalyticsDto {

    /**
     * Daily revenue data points
     */
    private List<RevenueDataPoint> dataPoints;

    /**
     * Total revenue for the period
     */
    private BigDecimal totalRevenue;

    /**
     * Total number of transactions
     */
    private Long totalTransactions;

    /**
     * Average order value (totalRevenue / totalTransactions)
     */
    private BigDecimal averageOrderValue;

    /**
     * Revenue from previous period (for comparison)
     */
    private BigDecimal previousPeriodRevenue;

    /**
     * Growth percentage compared to previous period
     */
    private BigDecimal growthPercentage;

    /**
     * Individual data point for a specific date
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        /**
         * Date for this data point
         */
        private LocalDate date;

        /**
         * Total revenue for this date
         */
        private BigDecimal revenue;

        /**
         * Number of transactions for this date
         */
        private Long transactionCount;
    }
}
