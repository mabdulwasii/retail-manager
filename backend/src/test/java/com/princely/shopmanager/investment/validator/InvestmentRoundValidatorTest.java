package com.princely.shopmanager.investment.validator;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestmentRoundValidator Tests")
class InvestmentRoundValidatorTest {

    private static final BigDecimal TEST_AMOUNT_10K = BigDecimal.valueOf(10000);
    private static final BigDecimal TEST_AMOUNT_5K = BigDecimal.valueOf(5000);
    private static final BigDecimal TEST_AMOUNT_50K = BigDecimal.valueOf(50000);
    private static final BigDecimal TEST_AMOUNT_100K = BigDecimal.valueOf(100000);

    private InvestmentRoundValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InvestmentRoundValidator();
    }

    // ========== Profit Sharing Model Validation Tests ==========

    @Test
    @DisplayName("FIXED_SHARES - Should fail when investors missing fixedShares")
    void shouldFailWhenFixedSharesMissingFromInvestors() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .fixedShares(null) // Missing
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-2")
                    .amount(TEST_AMOUNT_5K)
                    .fixedShares(50)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("All investors must provide fixed shares"));
    }

    @Test
    @DisplayName("FIXED_SHARES - Should pass when all investors have fixedShares")
    void shouldPassWhenAllInvestorsHaveFixedShares() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .fixedShares(100)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-2")
                    .amount(TEST_AMOUNT_5K)
                    .fixedShares(50)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("PROPORTIONAL_BY_AMOUNT - Should pass without additional validation")
    void shouldPassForProportionalByAmount() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("TIME_WEIGHTED - Should fail when timeWeightingRules is missing")
    void shouldFailWhenTimeWeightedMissingRules() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(null)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Time weighting rules are required"));
    }

    @Test
    @DisplayName("TIERED - Should fail when tierConfiguration is missing")
    void shouldFailWhenTieredMissingConfiguration() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .tierConfiguration(null)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Tier configuration is required"));
    }

    // ========== Tier Configuration Validation Tests ==========

    @Test
    @DisplayName("Tier Config - Should fail when tier1 threshold >= tier2 threshold")
    void shouldFailWhenTier1GreaterOrEqualTier2() {
        InvestmentRoundCreateRequest.TierConfigurationDTO tierConfig =
            InvestmentRoundCreateRequest.TierConfigurationDTO.builder()
                .tier1Threshold(TEST_AMOUNT_50K)
                .tier2Threshold(BigDecimal.valueOf(40000)) // Less than tier1
                .tier3Threshold(TEST_AMOUNT_100K)
                .tier1Multiplier(BigDecimal.valueOf(1.0))
                .tier2Multiplier(BigDecimal.valueOf(1.5))
                .tier3Multiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .tierConfiguration(tierConfig)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Tier 1 threshold must be less than Tier 2"));
    }

    @Test
    @DisplayName("Tier Config - Should fail when tier2 threshold >= tier3 threshold")
    void shouldFailWhenTier2GreaterOrEqualTier3() {
        InvestmentRoundCreateRequest.TierConfigurationDTO tierConfig =
            InvestmentRoundCreateRequest.TierConfigurationDTO.builder()
                .tier1Threshold(TEST_AMOUNT_10K)
                .tier2Threshold(TEST_AMOUNT_100K)
                .tier3Threshold(TEST_AMOUNT_50K) // Less than tier2
                .tier1Multiplier(BigDecimal.valueOf(1.0))
                .tier2Multiplier(BigDecimal.valueOf(1.5))
                .tier3Multiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .tierConfiguration(tierConfig)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Tier 2 threshold must be less than Tier 3"));
    }

    @Test
    @DisplayName("Tier Config - Should fail when multipliers are zero or negative")
    void shouldFailWhenMultipliersAreInvalid() {
        InvestmentRoundCreateRequest.TierConfigurationDTO tierConfig =
            InvestmentRoundCreateRequest.TierConfigurationDTO.builder()
                .tier1Threshold(TEST_AMOUNT_10K)
                .tier2Threshold(TEST_AMOUNT_50K)
                .tier3Threshold(TEST_AMOUNT_100K)
                .tier1Multiplier(BigDecimal.ZERO) // Invalid
                .tier2Multiplier(BigDecimal.valueOf(-1)) // Invalid
                .tier3Multiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .tierConfiguration(tierConfig)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Tier 1 multiplier must be greater than 0"));
        assertThat(errors).anyMatch(e -> e.contains("Tier 2 multiplier must be greater than 0"));
    }

    @Test
    @DisplayName("Tier Config - Should pass with valid configuration")
    void shouldPassWithValidTierConfiguration() {
        InvestmentRoundCreateRequest.TierConfigurationDTO tierConfig =
            InvestmentRoundCreateRequest.TierConfigurationDTO.builder()
                .tier1Threshold(TEST_AMOUNT_10K)
                .tier2Threshold(TEST_AMOUNT_50K)
                .tier3Threshold(TEST_AMOUNT_100K)
                .tier1Multiplier(BigDecimal.valueOf(1.0))
                .tier2Multiplier(BigDecimal.valueOf(1.5))
                .tier3Multiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .tierConfiguration(tierConfig)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }

    // ========== Time Weighting Rules Validation Tests ==========

    @Test
    @DisplayName("Time Weighting - Should fail when baseYears >= year2Threshold")
    void shouldFailWhenBaseYearsGreaterOrEqualYear2() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(2))
                .year2Threshold(BigDecimal.valueOf(1)) // Less than baseYears
                .year3Threshold(BigDecimal.valueOf(5))
                .baseMultiplier(BigDecimal.valueOf(1.0))
                .year2Multiplier(BigDecimal.valueOf(1.2))
                .year3Multiplier(BigDecimal.valueOf(1.5))
                .maxMultiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Base years must be less than Year 2 threshold"));
    }

    @Test
    @DisplayName("Time Weighting - Should fail when year2Threshold >= year3Threshold")
    void shouldFailWhenYear2GreaterOrEqualYear3() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(0))
                .year2Threshold(BigDecimal.valueOf(5))
                .year3Threshold(BigDecimal.valueOf(3)) // Less than year2
                .baseMultiplier(BigDecimal.valueOf(1.0))
                .year2Multiplier(BigDecimal.valueOf(1.2))
                .year3Multiplier(BigDecimal.valueOf(1.5))
                .maxMultiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Year 2 threshold must be less than Year 3 threshold"));
    }

    @Test
    @DisplayName("Time Weighting - Should fail when multipliers are invalid")
    void shouldFailWhenTimeWeightingMultipliersInvalid() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(0))
                .year2Threshold(BigDecimal.valueOf(2))
                .year3Threshold(BigDecimal.valueOf(5))
                .baseMultiplier(BigDecimal.ZERO) // Invalid
                .year2Multiplier(BigDecimal.valueOf(-0.5)) // Invalid
                .year3Multiplier(BigDecimal.valueOf(1.5))
                .maxMultiplier(BigDecimal.ZERO) // Invalid
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Base multiplier must be greater than 0"));
        assertThat(errors).anyMatch(e -> e.contains("Year 2 multiplier must be greater than 0"));
        assertThat(errors).anyMatch(e -> e.contains("Maximum multiplier must be greater than 0"));
    }

    @Test
    @DisplayName("Time Weighting - Should fail when multipliers not in ascending order")
    void shouldFailWhenMultipliersNotAscending() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(0))
                .year2Threshold(BigDecimal.valueOf(2))
                .year3Threshold(BigDecimal.valueOf(5))
                .baseMultiplier(BigDecimal.valueOf(1.5)) // Higher than year2
                .year2Multiplier(BigDecimal.valueOf(1.0))
                .year3Multiplier(BigDecimal.valueOf(1.5))
                .maxMultiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Base multiplier should not be greater than Year 2 multiplier"));
    }

    @Test
    @DisplayName("Time Weighting - Should fail when year3 exceeds maxMultiplier")
    void shouldFailWhenYear3ExceedsMax() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(0))
                .year2Threshold(BigDecimal.valueOf(2))
                .year3Threshold(BigDecimal.valueOf(5))
                .baseMultiplier(BigDecimal.valueOf(1.0))
                .year2Multiplier(BigDecimal.valueOf(1.2))
                .year3Multiplier(BigDecimal.valueOf(2.5)) // Exceeds max
                .maxMultiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Year 3 multiplier cannot exceed maximum multiplier"));
    }

    @Test
    @DisplayName("Time Weighting - Should pass with valid rules")
    void shouldPassWithValidTimeWeightingRules() {
        InvestmentRoundCreateRequest.TimeWeightingRulesDTO rules =
            InvestmentRoundCreateRequest.TimeWeightingRulesDTO.builder()
                .baseYears(BigDecimal.valueOf(0))
                .year2Threshold(BigDecimal.valueOf(2))
                .year3Threshold(BigDecimal.valueOf(5))
                .baseMultiplier(BigDecimal.valueOf(1.0))
                .year2Multiplier(BigDecimal.valueOf(1.2))
                .year3Multiplier(BigDecimal.valueOf(1.5))
                .maxMultiplier(BigDecimal.valueOf(2.0))
                .build();

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .timeWeightingRules(rules)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }

    // ========== Investor Validation Tests ==========

    @Test
    @DisplayName("Should fail when investors list is null")
    void shouldFailWhenInvestorsNull() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(null)
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("At least one investor is required"));
    }

    @Test
    @DisplayName("Should fail when investors list is empty")
    void shouldFailWhenInvestorsEmpty() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of())
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("At least one investor is required"));
    }

    @Test
    @DisplayName("Should fail when investor has null amount")
    void shouldFailWhenInvestorHasNullAmount() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(null)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Investment amount must be greater than 0"));
    }

    @Test
    @DisplayName("Should fail when investor has zero or negative amount")
    void shouldFailWhenInvestorHasInvalidAmount() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(BigDecimal.ZERO)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-2")
                    .amount(BigDecimal.valueOf(-100))
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Investor 1") && e.contains("amount must be greater than 0"));
        assertThat(errors).anyMatch(e -> e.contains("Investor 2") && e.contains("amount must be greater than 0"));
    }

    @Test
    @DisplayName("Should fail when investor has null or blank investorId")
    void shouldFailWhenInvestorHasInvalidId() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId(null)
                    .amount(TEST_AMOUNT_10K)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("   ")
                    .amount(TEST_AMOUNT_5K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Investor ID is required"));
    }

    @Test
    @DisplayName("Should fail when duplicate investor IDs exist")
    void shouldFailWhenDuplicateInvestorIds() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1") // Duplicate
                    .amount(TEST_AMOUNT_5K)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-2")
                    .amount(BigDecimal.valueOf(3000))
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isNotEmpty();
        assertThat(errors).anyMatch(e -> e.contains("Duplicate investor IDs are not allowed"));
    }

    @Test
    @DisplayName("Should pass with valid investors")
    void shouldPassWithValidInvestors() {
        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId("shop-1")
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-1")
                    .amount(TEST_AMOUNT_10K)
                    .build(),
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId("investor-2")
                    .amount(TEST_AMOUNT_5K)
                    .build()
            ))
            .build();

        List<String> errors = validator.validate(request);

        assertThat(errors).isEmpty();
    }
}
