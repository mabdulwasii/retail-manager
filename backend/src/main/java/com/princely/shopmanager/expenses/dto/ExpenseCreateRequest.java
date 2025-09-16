package com.princely.shopmanager.expenses.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Request DTO for creating expenses
 */
@Builder
public record ExpenseCreateRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Amount must have at most 8 integer digits and 2 decimal places")
    BigDecimal amount,

    @NotNull(message = "Expense date is required")
    @PastOrPresent(message = "Expense date cannot be in the future")
    LocalDate expenseDate,

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    String paymentMethod,

    @Size(max = 255, message = "Vendor name must not exceed 255 characters")
    String vendorName,

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    String referenceNumber,

    Set<@Size(max = 50, message = "Tag must not exceed 50 characters") String> tags,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes,

    Boolean submitForApproval
) {}