package com.princely.shopmanager.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvestmentRoiDto(
    String shopId,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    BigDecimal totalInvestmentAmount,
    BigDecimal totalDistributions,
    BigDecimal roiPercentage,
    LocalDateTime calculatedAt
) {}