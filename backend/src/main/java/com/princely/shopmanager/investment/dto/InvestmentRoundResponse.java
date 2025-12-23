package com.princely.shopmanager.investment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.domain.InvestmentRound;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for investment round details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Investment round details with summary information")
public class InvestmentRoundResponse {

    @Schema(description = "Investment round ID")
    private String id;

    @Schema(description = "Round number", example = "ROUND-2025-Q4-001")
    private String roundNumber;

    @Schema(description = "Shop ID")
    private String shopId;

    @Schema(description = "Shop name")
    private String shopName;

    @Schema(description = "Investment type", example = "SHOP_WIDE")
    private Investment.InvestmentType investmentType;

    @Schema(description = "Investment type display name", example = "Shop Wide")
    private String investmentTypeDisplay;

    @Schema(description = "Profit sharing model", example = "PROPORTIONAL_BY_AMOUNT")
    private Investment.ProfitSharingModel profitSharingModel;

    @Schema(description = "Profit sharing model display name", example = "Proportional by Amount")
    private String profitSharingModelDisplay;

    @Schema(description = "Maturity date")
    private LocalDateTime maturityDate;

    @Schema(description = "Round status", example = "OPEN")
    private InvestmentRound.RoundStatus status;

    @Schema(description = "Round status display name", example = "Open")
    private String statusDisplay;

    @Schema(description = "Total investment amount in this round")
    private BigDecimal totalAmount;

    @Schema(description = "Number of investors in this round")
    private Integer totalInvestors;

    @Schema(description = "Tier configuration (only for TIERED model)")
    private InvestmentRoundCreateRequest.TierConfigurationDTO tierConfiguration;

    @Schema(description = "Time weighting rules (only for TIME_WEIGHTED model)")
    private InvestmentRoundCreateRequest.TimeWeightingRulesDTO timeWeightingRules;

    @Schema(description = "Notes about the round")
    private String notes;

    @Schema(description = "List of investments in this round")
    private List<InvestmentSummary> investments;

    @Schema(description = "Round creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Round last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Summary information about individual investments in the round.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual investment summary within the round")
    public static class InvestmentSummary {

        @Schema(description = "Investment ID")
        private String id;

        @Schema(description = "Investment number")
        private String investmentNumber;

        @Schema(description = "Investor ID")
        private String investorId;

        @Schema(description = "Investor name")
        private String investorName;

        @Schema(description = "Investment amount")
        private BigDecimal amount;

        @Schema(description = "Fixed shares (for FIXED_SHARES model)")
        private Integer fixedShares;

        @Schema(description = "Investment date")
        private LocalDateTime investmentDate;

        @Schema(description = "Investment status")
        private Investment.InvestmentStatus status;

        @Schema(description = "Investment status display name")
        private String statusDisplay;

        @Schema(description = "Total profit earned")
        private BigDecimal totalProfitEarned;

        @Schema(description = "Total withdrawn")
        private BigDecimal totalWithdrawn;

        @Schema(description = "Available balance")
        private BigDecimal availableBalance;

        @Schema(description = "Investor-specific notes")
        private String notes;
    }
}
