package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for individual shop performance metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopPerformanceDto {

    private String shopId;
    private String shopName;
    private String status;

    private BigDecimal revenue;
    private BigDecimal profit;
    private Long transactions;
    private BigDecimal averageTransactionValue;

    private Long totalProducts;
    private BigDecimal inventoryValue;

    private Integer userCount;
}
