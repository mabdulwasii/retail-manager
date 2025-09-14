package com.princely.shopmanager.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvestmentRoiDto {
    private String shopId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalInvestmentAmount;
    private BigDecimal totalDistributions;
    private BigDecimal roiPercentage;
    private LocalDateTime calculatedAt;
}