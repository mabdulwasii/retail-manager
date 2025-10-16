package com.princely.shopmanager.expenses.dto;

import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for expense data
 */
@Builder
public record ExpenseResponse(
    UUID id,
    String shopId,
    String title,
    String description,
    ExpenseCategoryResponse category,
    BigDecimal amount,
    LocalDate expenseDate,
    String paymentMethod,
    String vendorName,
    String referenceNumber,
    String receiptUrl,
    ExpenseStatus status,
    Set<String> tags,
    String notes,
    UUID createdBy,
    String createdByName,
    UUID approvedBy,
    String approvedByName,
    LocalDate approvalDate,
    String approvalNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long version
) {}

