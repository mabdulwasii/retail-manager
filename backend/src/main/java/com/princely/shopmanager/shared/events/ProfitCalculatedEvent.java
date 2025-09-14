package com.princely.shopmanager.shared.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProfitCalculatedEvent(
    String distributionId,
    BigDecimal amount,
    String investmentId,
    LocalDateTime timestamp
) {
    public ProfitCalculatedEvent(String distributionId, BigDecimal amount, String investmentId) {
        this(distributionId, amount, investmentId, LocalDateTime.now());
    }
}