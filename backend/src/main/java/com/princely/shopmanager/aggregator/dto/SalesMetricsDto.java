package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for sales metrics aggregated across shops.
 * Matches frontend SalesMetrics interface in cloudAnalyticsService.ts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesMetricsDto {

    /**
     * Total number of sales transactions
     */
    private Long totalSales;

    /**
     * Total revenue from all sales
     */
    private BigDecimal totalRevenue;

    /**
     * Average order value
     */
    private BigDecimal averageOrderValue;

    /**
     * Date with highest sales volume
     */
    private LocalDate topSellingDay;

    /**
     * Hour of day (0-23) with peak sales activity
     */
    private Integer peakHour;

    /**
     * Total sales in previous period (for comparison)
     */
    private Long previousPeriodSales;

    /**
     * Sales growth percentage compared to previous period
     */
    private BigDecimal salesGrowth;
}
