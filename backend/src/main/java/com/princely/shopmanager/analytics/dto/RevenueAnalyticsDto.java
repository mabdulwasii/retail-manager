package com.princely.shopmanager.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueAnalyticsDto {
    private String shopId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal currentRevenue;
    private BigDecimal previousRevenue;
    private BigDecimal growthRate;
    private long currentTransactions;
    private long previousTransactions;
    private LocalDateTime calculatedAt;
}