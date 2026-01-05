package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for shop performance comparison analytics.
 * Matches frontend ShopPerformanceAnalytics interface in cloudAnalyticsService.ts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPerformanceAnalyticsDto {

    /**
     * Performance data for all shops
     */
    private List<ShopPerformance> shops;

    /**
     * Total number of shops
     */
    private Integer totalShops;

    /**
     * Best performing shop (highest revenue)
     */
    private ShopPerformance bestPerformingShop;

    /**
     * Worst performing shop (lowest revenue)
     */
    private ShopPerformance worstPerformingShop;

    /**
     * Individual shop performance data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopPerformance {
        /**
         * Shop ID
         */
        private String shopId;

        /**
         * Shop name
         */
        private String shopName;

        /**
         * Total revenue for this shop
         */
        private BigDecimal revenue;

        /**
         * Number of transactions
         */
        private Long transactionCount;

        /**
         * Average order value
         */
        private BigDecimal averageOrderValue;

        /**
         * Name of top selling product
         */
        private String topProduct;

        /**
         * Revenue growth percentage compared to previous period
         */
        private BigDecimal growthPercentage;
    }
}
