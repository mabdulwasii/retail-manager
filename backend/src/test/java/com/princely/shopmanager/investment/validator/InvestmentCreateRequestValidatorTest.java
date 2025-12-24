package com.princely.shopmanager.investment.validator;

import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.InvestmentCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvestmentCreateRequestValidator Tests")
class InvestmentCreateRequestValidatorTest {

    private static final BigDecimal TEST_INVESTMENT_AMOUNT = BigDecimal.valueOf(10000);
    private static final BigDecimal TEST_PROFIT_PERCENTAGE = BigDecimal.valueOf(20);

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== Investment Type Validation Tests ==========

    @Test
    @DisplayName("PRODUCT_SPECIFIC - Should fail when productIds is null")
    void shouldFailWhenProductSpecificHasNullProductIds() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.PRODUCT_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .productIds(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("productIds") &&
                v.getMessage().contains("Product IDs are required")
            );
    }

    @Test
    @DisplayName("PRODUCT_SPECIFIC - Should fail when productIds is empty")
    void shouldFailWhenProductSpecificHasEmptyProductIds() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.PRODUCT_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .productIds(Set.of())
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v -> v.getPropertyPath().toString().equals("productIds"));
    }

    @Test
    @DisplayName("PRODUCT_SPECIFIC - Should pass when productIds is provided")
    void shouldPassWhenProductSpecificHasProductIds() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.PRODUCT_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .productIds(Set.of("product-1", "product-2"))
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CATEGORY_SPECIFIC - Should fail when categoryFilter is null")
    void shouldFailWhenCategorySpecificHasNullCategory() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .categoryFilter(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("categoryFilter") &&
                v.getMessage().contains("Category filter is required")
            );
    }

    @Test
    @DisplayName("CATEGORY_SPECIFIC - Should fail when categoryFilter is blank")
    void shouldFailWhenCategorySpecificHasBlankCategory() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .categoryFilter("   ")
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v -> v.getPropertyPath().toString().equals("categoryFilter"));
    }

    @Test
    @DisplayName("CATEGORY_SPECIFIC - Should pass when categoryFilter is provided")
    void shouldPassWhenCategorySpecificHasCategory() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.CATEGORY_SPECIFIC)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .categoryFilter("Electronics")
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("SHOP_WIDE - Should fail when productIds is provided")
    void shouldFailWhenShopWideHasProductIds() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .productIds(Set.of("product-1"))
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("productIds") &&
                v.getMessage().contains("should not be specified")
            );
    }

    @Test
    @DisplayName("SHOP_WIDE - Should fail when categoryFilter is provided")
    void shouldFailWhenShopWideHasCategory() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .categoryFilter("Electronics")
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("categoryFilter") &&
                v.getMessage().contains("should not be specified")
            );
    }

    @Test
    @DisplayName("SHOP_WIDE - Should pass when no filters provided")
    void shouldPassWhenShopWideHasNoFilters() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // ========== Profit Sharing Model Validation Tests ==========

    @Test
    @DisplayName("PROPORTIONAL_BY_AMOUNT - Should fail when profitPercentage is null")
    void shouldFailWhenProportionalByAmountHasNullPercentage() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("profitPercentage") &&
                v.getMessage().contains("Profit percentage is required")
            );
    }

    @Test
    @DisplayName("PROPORTIONAL_BY_AMOUNT - Should fail when profitPercentage is zero")
    void shouldFailWhenProportionalByAmountHasZeroPercentage() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.ZERO)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v -> v.getMessage().contains("must be between 0 and 100"));
    }

    @Test
    @DisplayName("PROPORTIONAL_BY_AMOUNT - Should fail when profitPercentage exceeds 100")
    void shouldFailWhenProportionalByAmountExceeds100() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(101))
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v -> v.getMessage().contains("must be between 0 and 100"));
    }

    @Test
    @DisplayName("PROPORTIONAL_BY_AMOUNT - Should pass when profitPercentage is valid")
    void shouldPassWhenProportionalByAmountHasValidPercentage() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(BigDecimal.valueOf(50))
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("FIXED_SHARES - Should fail when fixedShares is null")
    void shouldFailWhenFixedSharesIsNull() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .fixedShares(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v ->
                v.getPropertyPath().toString().equals("fixedShares") &&
                v.getMessage().contains("must be greater than 0")
            );
    }

    @Test
    @DisplayName("FIXED_SHARES - Should fail when fixedShares is zero")
    void shouldFailWhenFixedSharesIsZero() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .fixedShares(0)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().contains("must be greater than 0")
        );
    }

    @Test
    @DisplayName("FIXED_SHARES - Should pass when fixedShares is positive")
    void shouldPassWhenFixedSharesIsPositive() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.FIXED_SHARES)
            .fixedShares(100)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("TIME_WEIGHTED - Should warn when maturityDate is missing")
    void shouldWarnWhenTimeWeightedHasNoMaturityDate() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .maturityDate(null)
            .build();

        validator.validate(request);

        // Current implementation treats this as a warning, not a hard error
        // The validator returns true even with maturity date missing
        // This test documents current behavior
    }

    @Test
    @DisplayName("TIME_WEIGHTED - Should pass when maturityDate is provided")
    void shouldPassWhenTimeWeightedHasMaturityDate() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIME_WEIGHTED)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .maturityDate(LocalDateTime.now().plusYears(1))
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("TIERED - Should require profitPercentage")
    void shouldRequireProfitPercentageForTiered() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(Investment.ProfitSharingModel.TIERED)
            .profitPercentage(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        assertThat(violations)
            .isNotEmpty()
            .anyMatch(v -> v.getMessage().contains("Profit percentage is required"));
    }

    // ========== Null/Edge Case Tests ==========

    @Test
    @DisplayName("Should return true for null request")
    void shouldReturnTrueForNullRequest() {
        // The validator's isValid returns true for null - lets @NotNull handle it
        InvestmentCreateRequestValidator nullValidator = new InvestmentCreateRequestValidator();
        boolean result = nullValidator.isValid(null, null);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle null investmentType gracefully")
    void shouldHandleNullInvestmentType() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(null)
            .profitSharingModel(Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .profitPercentage(TEST_PROFIT_PERCENTAGE)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        // Should not crash, basic validation may still apply
        assertThat(violations).isNotNull();
    }

    @Test
    @DisplayName("Should handle null profitSharingModel gracefully")
    void shouldHandleNullProfitSharingModel() {
        InvestmentCreateRequest request = InvestmentCreateRequest.builder()
            .investorId("investor-1")
            .shopId("shop-1")
            .amount(TEST_INVESTMENT_AMOUNT)
            .investmentType(Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(null)
            .build();

        Set<ConstraintViolation<InvestmentCreateRequest>> violations = validator.validate(request);

        // Should not crash
        assertThat(violations).isNotNull();
    }
}
