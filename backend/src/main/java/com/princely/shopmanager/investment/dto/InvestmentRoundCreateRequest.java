package com.princely.shopmanager.investment.dto;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.TierConfiguration;
import com.princely.shopmanager.investment.domain.TimeWeightingRules;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating an investment round with multiple investors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create an investment round with investors")
public class InvestmentRoundCreateRequest {

    @Schema(description = "Shop ID where the investment round is created")
    @NotBlank(message = "Shop ID is required")
    private String shopId;

    @Schema(description = "Investment type for this round", example = "SHOP_WIDE")
    @NotNull(message = "Investment type is required")
    private Investment.InvestmentType investmentType;

    @Schema(description = "Profit sharing model for this round", example = "PROPORTIONAL_BY_AMOUNT")
    @NotNull(message = "Profit sharing model is required")
    private Investment.ProfitSharingModel profitSharingModel;

    @Schema(description = "Maturity date for investments in this round", example = "2026-12-31")
    private LocalDate maturityDate;

    @Schema(description = "Tier configuration for TIERED profit sharing model")
    @Valid
    private TierConfigurationDTO tierConfiguration;

    @Schema(description = "Time weighting rules for TIME_WEIGHTED profit sharing model")
    @Valid
    private TimeWeightingRulesDTO timeWeightingRules;

    @Schema(description = "Notes about this investment round")
    private String notes;

    @Schema(description = "List of investors participating in this round")
    @NotEmpty(message = "At least one investor is required")
    @Valid
    private List<InvestorInput> investors;

    /**
     * Individual investor input for the round.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual investor participation details")
    public static class InvestorInput {

        @Schema(description = "Investor user ID")
        @NotBlank(message = "Investor ID is required")
        private String investorId;

        @Schema(description = "Investment amount", example = "100000.00")
        @NotNull(message = "Investment amount is required")
        private java.math.BigDecimal amount;

        @Schema(description = "Fixed shares for FIXED_SHARES model", example = "100")
        private Integer fixedShares;

        @Schema(description = "Notes specific to this investor")
        private String notes;
    }

    /**
     * DTO for tier configuration.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Tier configuration for tiered profit sharing")
    public static class TierConfigurationDTO {

        @Schema(description = "Tier 1 threshold amount", example = "0")
        @NotNull(message = "Tier 1 threshold is required")
        private java.math.BigDecimal tier1Threshold;

        @Schema(description = "Tier 1 multiplier", example = "1.0")
        @NotNull(message = "Tier 1 multiplier is required")
        private java.math.BigDecimal tier1Multiplier;

        @Schema(description = "Tier 2 threshold amount", example = "50000")
        @NotNull(message = "Tier 2 threshold is required")
        private java.math.BigDecimal tier2Threshold;

        @Schema(description = "Tier 2 multiplier", example = "1.1")
        @NotNull(message = "Tier 2 multiplier is required")
        private java.math.BigDecimal tier2Multiplier;

        @Schema(description = "Tier 3 threshold amount", example = "100000")
        @NotNull(message = "Tier 3 threshold is required")
        private java.math.BigDecimal tier3Threshold;

        @Schema(description = "Tier 3 multiplier", example = "1.2")
        @NotNull(message = "Tier 3 multiplier is required")
        private java.math.BigDecimal tier3Multiplier;

        public TierConfiguration toEntity() {
            return TierConfiguration.builder()
                .tier1Threshold(tier1Threshold)
                .tier1Multiplier(tier1Multiplier)
                .tier2Threshold(tier2Threshold)
                .tier2Multiplier(tier2Multiplier)
                .tier3Threshold(tier3Threshold)
                .tier3Multiplier(tier3Multiplier)
                .build();
        }
    }

    /**
     * DTO for time weighting rules.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Time weighting rules for time-weighted profit sharing")
    public static class TimeWeightingRulesDTO {

        @Schema(description = "Base years for 1.0x multiplier", example = "1.0")
        @NotNull(message = "Base years is required")
        private java.math.BigDecimal baseYears;

        @Schema(description = "Base multiplier", example = "1.0")
        @NotNull(message = "Base multiplier is required")
        private java.math.BigDecimal baseMultiplier;

        @Schema(description = "Year 2 threshold", example = "2.0")
        @NotNull(message = "Year 2 threshold is required")
        private java.math.BigDecimal year2Threshold;

        @Schema(description = "Year 2 multiplier", example = "1.2")
        @NotNull(message = "Year 2 multiplier is required")
        private java.math.BigDecimal year2Multiplier;

        @Schema(description = "Year 3 threshold", example = "3.0")
        @NotNull(message = "Year 3 threshold is required")
        private java.math.BigDecimal year3Threshold;

        @Schema(description = "Year 3 multiplier", example = "1.5")
        @NotNull(message = "Year 3 multiplier is required")
        private java.math.BigDecimal year3Multiplier;

        @Schema(description = "Maximum multiplier cap", example = "2.0")
        @NotNull(message = "Maximum multiplier is required")
        private java.math.BigDecimal maxMultiplier;

        public TimeWeightingRules toEntity() {
            return TimeWeightingRules.builder()
                .baseYears(baseYears)
                .baseMultiplier(baseMultiplier)
                .year2Threshold(year2Threshold)
                .year2Multiplier(year2Multiplier)
                .year3Threshold(year3Threshold)
                .year3Multiplier(year3Multiplier)
                .maxMultiplier(maxMultiplier)
                .build();
        }
    }
}
