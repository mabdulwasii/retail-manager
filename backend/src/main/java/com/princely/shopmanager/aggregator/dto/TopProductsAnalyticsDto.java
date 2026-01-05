package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for top products analytics aggregated across shops.
 * Matches frontend TopProductsAnalytics interface in cloudAnalyticsService.ts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsAnalyticsDto {

    /**
     * List of top selling products
     */
    private List<TopProduct> products;

    /**
     * Total number of unique products sold
     */
    private Integer totalProducts;

    /**
     * Individual product performance data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        /**
         * Product ID
         */
        private String productId;

        /**
         * Product name
         */
        private String productName;

        /**
         * Product category
         */
        private String category;

        /**
         * Total quantity sold
         */
        private Long quantitySold;

        /**
         * Total revenue from this product
         */
        private BigDecimal revenue;

        /**
         * Average selling price
         */
        private BigDecimal averagePrice;
    }
}
