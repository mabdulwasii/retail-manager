package com.princely.shopmanager.expenses.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for expense summary information
 */
@Data
@Builder
public class ExpenseSummaryDto {
    private Long totalExpenses;
    private Long pendingApproval;
    private Long approvedExpenses;
    private BigDecimal totalAmount;
    private BigDecimal monthlyTotal;
    private List<CategoryBreakdown> categoryBreakdown;

    @Data
    @Builder
    public static class CategoryBreakdown {
        private UUID categoryId;
        private String categoryName;
        private BigDecimal amount;
        private Long count;
    }
}