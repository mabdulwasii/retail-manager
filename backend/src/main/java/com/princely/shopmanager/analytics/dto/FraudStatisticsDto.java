package com.princely.shopmanager.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FraudStatisticsDto {
    private String shopId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private long totalAssessments;
    private long highRiskCount;
    private long criticalRiskCount;
    private BigDecimal riskRate;
    private LocalDateTime calculatedAt;
}