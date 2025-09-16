package com.princely.shopmanager.expenses.dto;

import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Filter criteria for searching and filtering expenses
 */
@Data
@Builder
public class ExpenseFilterCriteria {
    private LocalDate startDate;
    private LocalDate endDate;
    private ExpenseStatus status;
    private UUID categoryId;
    private UUID createdBy;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String searchQuery;
    private String paymentMethod;
    private String vendorName;
}