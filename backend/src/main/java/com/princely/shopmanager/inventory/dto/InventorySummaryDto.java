package com.princely.shopmanager.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Summary statistics for inventory dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryDto {

    private Integer totalItems;
    private BigDecimal totalValue;
    private Integer lowStockItems;
    private Integer expiredItems;
    private Integer expiringSoonItems;
    private List<CategoryBreakdown> categoryBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Integer itemCount;
        private BigDecimal totalValue;
        private Integer lowStockCount;
    }
}