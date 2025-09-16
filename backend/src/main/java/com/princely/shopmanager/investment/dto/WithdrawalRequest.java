package com.princely.shopmanager.investment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "10.00", message = "Withdrawal amount must be at least ₦10")
    private BigDecimal amount;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    private String paymentMethod;

    private String bankAccount;

    private String notes;
}