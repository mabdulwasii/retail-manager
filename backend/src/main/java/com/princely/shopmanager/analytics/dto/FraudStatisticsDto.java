package com.princely.shopmanager.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FraudStatisticsDto(
    String shopId,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    long totalAssessments,
    long highRiskCount,
    long criticalRiskCount,
    BigDecimal riskRate,
    LocalDateTime calculatedAt
) {}