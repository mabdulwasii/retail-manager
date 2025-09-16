package com.princely.shopmanager.expenses.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Request DTO for approving or rejecting expenses
 */
@Builder
public record ExpenseApprovalRequest(
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes
) {}