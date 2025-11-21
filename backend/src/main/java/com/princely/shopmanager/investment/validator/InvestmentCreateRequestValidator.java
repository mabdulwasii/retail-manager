package com.princely.shopmanager.investment.validator;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.InvestmentCreateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator for InvestmentCreateRequest that enforces business rules based on
 * investment type and profit sharing model combinations.
 */
public class InvestmentCreateRequestValidator implements ConstraintValidator<ValidInvestmentCreateRequest, InvestmentCreateRequest> {

    @Override
    public boolean isValid(InvestmentCreateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null validation
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // Validate based on Investment Type
        if (request.getInvestmentType() != null) {
            isValid &= validateInvestmentType(request, context);
        }

        // Validate based on Profit Sharing Model
        if (request.getProfitSharingModel() != null) {
            isValid &= validateProfitSharingModel(request, context);
        }

        return isValid;
    }

    private boolean validateInvestmentType(InvestmentCreateRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;

        switch (request.getInvestmentType()) {
            case PRODUCT_SPECIFIC:
                if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
                    context.buildConstraintViolationWithTemplate(
                            "Product IDs are required for " + request.getInvestmentType().getDisplayName() + " investment type"
                    ).addPropertyNode("productIds").addConstraintViolation();
                    isValid = false;
                }
                break;

            case CATEGORY_SPECIFIC:
                if (request.getCategoryFilter() == null || request.getCategoryFilter().isBlank()) {
                    context.buildConstraintViolationWithTemplate(
                            "Category filter is required for " + request.getInvestmentType().getDisplayName() + " investment type"
                    ).addPropertyNode("categoryFilter").addConstraintViolation();
                    isValid = false;
                }
                break;

            case SHOP_WIDE:
                if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
                    context.buildConstraintViolationWithTemplate(
                            "Product IDs should not be specified for " + request.getInvestmentType().getDisplayName() + " investment type"
                    ).addPropertyNode("productIds").addConstraintViolation();
                    isValid = false;
                }
                if (request.getCategoryFilter() != null && !request.getCategoryFilter().isBlank()) {
                    context.buildConstraintViolationWithTemplate(
                            "Category filter should not be specified for " + request.getInvestmentType().getDisplayName() + " investment type"
                    ).addPropertyNode("categoryFilter").addConstraintViolation();
                    isValid = false;
                }
                break;
        }

        return isValid;
    }

    private boolean validateProfitSharingModel(InvestmentCreateRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;

        switch (request.getProfitSharingModel()) {
            case PROPORTIONAL_BY_AMOUNT:
            case TIME_WEIGHTED:
            case TIERED:
                if (request.getProfitPercentage() == null) {
                    context.buildConstraintViolationWithTemplate(
                            String.format("Profit percentage is required for %s profit sharing model",
                                    request.getProfitSharingModel().getDisplayName())
                    ).addPropertyNode("profitPercentage").addConstraintViolation();
                    isValid = false;
                } else if (request.getProfitPercentage().compareTo(BigDecimal.ZERO) <= 0 ||
                        request.getProfitPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
                    context.buildConstraintViolationWithTemplate(
                            "Profit percentage must be between 0 and 100"
                    ).addPropertyNode("profitPercentage").addConstraintViolation();
                    isValid = false;
                }
                break;

            case FIXED_SHARES:
                if (request.getFixedShares() == null || request.getFixedShares() <= 0) {
                    context.buildConstraintViolationWithTemplate(
                            "Fixed shares must be greater than 0 for " + request.getProfitSharingModel().getDisplayName() + " profit sharing model"
                    ).addPropertyNode("fixedShares").addConstraintViolation();
                    isValid = false;
                }
                break;
        }

        // TIME_WEIGHTED should ideally have a maturity date
        if (request.getProfitSharingModel() == Investment.ProfitSharingModel.TIME_WEIGHTED) {
            if (request.getMaturityDate() == null) {
                context.buildConstraintViolationWithTemplate(
                        "Maturity date is recommended for " + request.getProfitSharingModel().getDisplayName() + " profit sharing model"
                ).addPropertyNode("maturityDate").addConstraintViolation();
                // This is a warning, not a hard error - you can decide to make it isValid = false
            }
        }

        return isValid;
    }
}
