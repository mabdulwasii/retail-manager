package com.princely.shopmanager.expenses.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nested category response for expense
 */
@Builder
public record ExpenseCategoryResponse(
    UUID id,
    String name,
    String description,
    Boolean requiresApproval,
    BigDecimal approvalLimit,
    String defaultPaymentMethod,
    Boolean taxDeductible
) {}