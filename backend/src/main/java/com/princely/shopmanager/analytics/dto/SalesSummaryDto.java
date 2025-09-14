package com.princely.shopmanager.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SalesSummaryDto(
    String shopId,
    LocalDateTime periodStart,
    LocalDateTime periodEnd,
    BigDecimal totalRevenue,
    long totalTransactions,
    BigDecimal averageTransactionValue,
    LocalDateTime calculatedAt
) {}