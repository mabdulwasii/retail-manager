package com.princely.shopmanager.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RevenueAnalyticsDto(
    String shopId,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    BigDecimal currentRevenue,
    BigDecimal previousRevenue,
    BigDecimal growthRate,
    long currentTransactions,
    long previousTransactions,
    LocalDateTime calculatedAt
) {}