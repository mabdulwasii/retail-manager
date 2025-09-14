package com.princely.shopmanager.investment.dto;

import java.math.BigDecimal;

public record ProfitCalculationResult(
    BigDecimal totalRevenue,
    BigDecimal grossProfit,
    BigDecimal netProfit,
    BigDecimal operationalCosts
) {
    public ProfitCalculationResult(BigDecimal totalRevenue, BigDecimal totalProfit) {
        this(totalRevenue, totalProfit, totalProfit, BigDecimal.ZERO);
    }
}