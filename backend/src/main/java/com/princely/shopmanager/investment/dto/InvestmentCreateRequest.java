package com.princely.shopmanager.investment.dto;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.validator.ValidInvestmentCreateRequest;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidInvestmentCreateRequest
public class InvestmentCreateRequest {

    @NotBlank(message = "Investor ID is required")
    private String investorId;

    @NotBlank(message = "Shop ID is required")
    private String shopId;

    @NotNull(message = "Investment type is required")
    private Investment.InvestmentType investmentType;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(value = "100.00", message = "Investment amount must be at least ₦100")
    private BigDecimal amount;

    @NotNull(message = "Profit sharing model is required")
    private Investment.ProfitSharingModel profitSharingModel;

    @DecimalMin(value = "0.00", message = "Profit percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Profit percentage cannot exceed 100%")
    private BigDecimal profitPercentage;

    @Min(value = 1, message = "Fixed shares must be at least 1")
    private Integer fixedShares;

    private LocalDateTime maturityDate;

    private Set<String> productIds;

    private String categoryFilter;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}