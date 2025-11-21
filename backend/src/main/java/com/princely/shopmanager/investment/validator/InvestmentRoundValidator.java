package com.princely.shopmanager.investment.validator;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for investment round creation and updates.
 * Ensures business rules are enforced for different profit sharing models.
 */
@Component
@Slf4j
public class InvestmentRoundValidator {

    /**
     * Validates an investment round creation request.
     *
     * @param request The request to validate
     * @return List of validation errors (empty if valid)
     */
    public List<String> validate(InvestmentRoundCreateRequest request) {
        List<String> errors = new ArrayList<>();

        // Validate profit sharing model specific requirements
        validateProfitSharingModel(request, errors);

        // Validate tier configuration if TIERED model
        if (request.getProfitSharingModel() == Investment.ProfitSharingModel.TIERED) {
            validateTierConfiguration(request, errors);
        }

        // Validate time weighting rules if TIME_WEIGHTED model
        if (request.getProfitSharingModel() == Investment.ProfitSharingModel.TIME_WEIGHTED) {
            validateTimeWeightingRules(request, errors);
        }

        // Validate investor inputs
        validateInvestors(request, errors);

        return errors;
    }

    private void validateProfitSharingModel(InvestmentRoundCreateRequest request, List<String> errors) {
        Investment.ProfitSharingModel model = request.getProfitSharingModel();

        switch (model) {
            case FIXED_SHARES:
                // All investors must provide fixed shares
                boolean allHaveShares = request.getInvestors().stream()
                    .allMatch(investor -> investor.getFixedShares() != null && investor.getFixedShares() > 0);
                if (!allHaveShares) {
                    errors.add("All investors must provide fixed shares for FIXED_SHARES profit sharing model");
                }
                break;

            case PROPORTIONAL_BY_AMOUNT:
                // No additional validation needed - calculated from amounts
                break;

            case TIME_WEIGHTED:
                // Time weighting rules must be provided
                if (request.getTimeWeightingRules() == null) {
                    errors.add("Time weighting rules are required for TIME_WEIGHTED profit sharing model");
                }
                break;

            case TIERED:
                // Tier configuration must be provided
                if (request.getTierConfiguration() == null) {
                    errors.add("Tier configuration is required for TIERED profit sharing model");
                }
                break;
        }
    }

    private void validateTierConfiguration(InvestmentRoundCreateRequest request, List<String> errors) {
        var config = request.getTierConfiguration();
        if (config == null) {
            return; // Already validated in previous step
        }

        // Validate thresholds are in ascending order
        if (config.getTier1Threshold().compareTo(config.getTier2Threshold()) >= 0) {
            errors.add("Tier 1 threshold must be less than Tier 2 threshold");
        }

        if (config.getTier2Threshold().compareTo(config.getTier3Threshold()) >= 0) {
            errors.add("Tier 2 threshold must be less than Tier 3 threshold");
        }

        // Validate multipliers are valid
        if (config.getTier1Multiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Tier 1 multiplier must be greater than 0");
        }

        if (config.getTier2Multiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Tier 2 multiplier must be greater than 0");
        }

        if (config.getTier3Multiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Tier 3 multiplier must be greater than 0");
        }

        // Validate multipliers are in logical order (increasing bonuses)
        if (config.getTier1Multiplier().compareTo(config.getTier2Multiplier()) > 0) {
            log.warn("Tier 1 multiplier is greater than Tier 2 - unusual but allowed");
        }

        if (config.getTier2Multiplier().compareTo(config.getTier3Multiplier()) > 0) {
            log.warn("Tier 2 multiplier is greater than Tier 3 - unusual but allowed");
        }
    }

    private void validateTimeWeightingRules(InvestmentRoundCreateRequest request, List<String> errors) {
        var rules = request.getTimeWeightingRules();
        if (rules == null) {
            return; // Already validated in previous step
        }

        // Validate year thresholds are in ascending order
        if (rules.getBaseYears().compareTo(rules.getYear2Threshold()) >= 0) {
            errors.add("Base years must be less than Year 2 threshold");
        }

        if (rules.getYear2Threshold().compareTo(rules.getYear3Threshold()) >= 0) {
            errors.add("Year 2 threshold must be less than Year 3 threshold");
        }

        // Validate multipliers are valid
        if (rules.getBaseMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Base multiplier must be greater than 0");
        }

        if (rules.getYear2Multiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Year 2 multiplier must be greater than 0");
        }

        if (rules.getYear3Multiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Year 3 multiplier must be greater than 0");
        }

        if (rules.getMaxMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Maximum multiplier must be greater than 0");
        }

        // Validate multipliers are in ascending order (increasing rewards over time)
        if (rules.getBaseMultiplier().compareTo(rules.getYear2Multiplier()) > 0) {
            errors.add("Base multiplier should not be greater than Year 2 multiplier");
        }

        if (rules.getYear2Multiplier().compareTo(rules.getYear3Multiplier()) > 0) {
            errors.add("Year 2 multiplier should not be greater than Year 3 multiplier");
        }

        // Validate max multiplier is reasonable
        if (rules.getYear3Multiplier().compareTo(rules.getMaxMultiplier()) > 0) {
            errors.add("Year 3 multiplier cannot exceed maximum multiplier");
        }
    }

    private void validateInvestors(InvestmentRoundCreateRequest request, List<String> errors) {
        if (request.getInvestors() == null || request.getInvestors().isEmpty()) {
            errors.add("At least one investor is required");
            return;
        }

        // Validate each investor
        for (int i = 0; i < request.getInvestors().size(); i++) {
            var investor = request.getInvestors().get(i);
            String prefix = "Investor " + (i + 1) + ": ";

            if (investor.getAmount() == null || investor.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(prefix + "Investment amount must be greater than 0");
            }

            if (investor.getInvestorId() == null || investor.getInvestorId().isBlank()) {
                errors.add(prefix + "Investor ID is required");
            }
        }

        // Check for duplicate investor IDs
        long uniqueInvestors = request.getInvestors().stream()
            .map(InvestmentRoundCreateRequest.InvestorInput::getInvestorId)
            .distinct()
            .count();

        if (uniqueInvestors < request.getInvestors().size()) {
            errors.add("Duplicate investor IDs are not allowed in the same round");
        }
    }
}
