package com.princely.shopmanager.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SalesSummaryDto {
    private String shopId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalRevenue;
    private long totalTransactions;
    private BigDecimal averageTransactionValue;
    private LocalDateTime calculatedAt;
}