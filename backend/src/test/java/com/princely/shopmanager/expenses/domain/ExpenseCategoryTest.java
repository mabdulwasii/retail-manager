package com.princely.shopmanager.expenses.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExpenseCategory Domain Tests")
class ExpenseCategoryTest {

    private ExpenseCategory category;

    @BeforeEach
    void setUp() {
        category = ExpenseCategory.builder()
            .shopId("shop-1")
            .name("Office Supplies")
            .description("Supplies for office use")
            .build();
    }

    @Test
    @DisplayName("Should have default isActive as true")
    void shouldHaveDefaultIsActiveAsTrue() {
        // Given
        ExpenseCategory newCategory = ExpenseCategory.builder()
            .shopId("shop-1")
            .name("Test Category")
            .build();

        // Then
        assertThat(newCategory.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should have default requiresApproval as true")
    void shouldHaveDefaultRequiresApprovalAsTrue() {
        // Given
        ExpenseCategory newCategory = ExpenseCategory.builder()
            .shopId("shop-1")
            .name("Test Category")
            .build();

        // Then
        assertThat(newCategory.getRequiresApproval()).isTrue();
    }

    @Test
    @DisplayName("Should have default taxDeductible as true")
    void shouldHaveDefaultTaxDeductibleAsTrue() {
        // Given
        ExpenseCategory newCategory = ExpenseCategory.builder()
            .shopId("shop-1")
            .name("Test Category")
            .build();

        // Then
        assertThat(newCategory.getTaxDeductible()).isTrue();
    }

    @Test
    @DisplayName("Should have default autoApprovalEnabled as false")
    void shouldHaveDefaultAutoApprovalEnabledAsFalse() {
        // Given
        ExpenseCategory newCategory = ExpenseCategory.builder()
            .shopId("shop-1")
            .name("Test Category")
            .build();

        // Then
        assertThat(newCategory.getAutoApprovalEnabled()).isFalse();
    }

    // requiresApprovalForAmount tests
    @Test
    @DisplayName("requiresApprovalForAmount - Should return false when requiresApproval is false")
    void requiresApprovalForAmount_shouldReturnFalseWhenRequiresApprovalIsFalse() {
        // Given
        category.setRequiresApproval(false);
        BigDecimal amount = BigDecimal.valueOf(1000);

        // When
        boolean result = category.requiresApprovalForAmount(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should return true when approvalLimit is null")
    void requiresApprovalForAmount_shouldReturnTrueWhenApprovalLimitIsNull() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(null);
        BigDecimal amount = BigDecimal.valueOf(100);

        // When
        boolean result = category.requiresApprovalForAmount(amount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should return true when amount exceeds approval limit")
    void requiresApprovalForAmount_shouldReturnTrueWhenAmountExceedsLimit() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        BigDecimal amount = BigDecimal.valueOf(1000);

        // When
        boolean result = category.requiresApprovalForAmount(amount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should return false when amount equals approval limit")
    void requiresApprovalForAmount_shouldReturnFalseWhenAmountEqualsLimit() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        BigDecimal amount = BigDecimal.valueOf(500);

        // When
        boolean result = category.requiresApprovalForAmount(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should return false when amount is below approval limit")
    void requiresApprovalForAmount_shouldReturnFalseWhenAmountBelowLimit() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        BigDecimal amount = BigDecimal.valueOf(250);

        // When
        boolean result = category.requiresApprovalForAmount(amount);

        // Then
        assertThat(result).isFalse();
    }

    // canAutoApprove tests
    @Test
    @DisplayName("canAutoApprove - Should return true when all conditions met")
    void canAutoApprove_shouldReturnTrueWhenAllConditionsMet() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        category.setIsActive(true);
        BigDecimal amount = BigDecimal.valueOf(250); // Below limit

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canAutoApprove - Should return false when autoApprovalEnabled is false")
    void canAutoApprove_shouldReturnFalseWhenAutoApprovalDisabled() {
        // Given
        category.setAutoApprovalEnabled(false);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        category.setIsActive(true);
        BigDecimal amount = BigDecimal.valueOf(250);

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAutoApprove - Should return false when amount requires approval")
    void canAutoApprove_shouldReturnFalseWhenAmountRequiresApproval() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        category.setIsActive(true);
        BigDecimal amount = BigDecimal.valueOf(1000); // Above limit

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAutoApprove - Should return false when category is not active")
    void canAutoApprove_shouldReturnFalseWhenNotActive() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        category.setIsActive(false);
        BigDecimal amount = BigDecimal.valueOf(250);

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAutoApprove - Should return true when requiresApproval is false")
    void canAutoApprove_shouldReturnTrueWhenRequiresApprovalIsFalse() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(false);
        category.setIsActive(true);
        BigDecimal amount = BigDecimal.valueOf(1000);

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isTrue();
    }

    // activate tests
    @Test
    @DisplayName("activate - Should set isActive to true")
    void activate_shouldSetIsActiveToTrue() {
        // Given
        category.setIsActive(false);

        // When
        category.activate();

        // Then
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("activate - Should keep isActive as true when already active")
    void activate_shouldKeepIsActiveTrueWhenAlreadyActive() {
        // Given
        category.setIsActive(true);

        // When
        category.activate();

        // Then
        assertThat(category.getIsActive()).isTrue();
    }

    // setApprovalLimit tests
    @Test
    @DisplayName("setApprovalLimit - Should set positive approval limit")
    void setApprovalLimit_shouldSetPositiveApprovalLimit() {
        // Given
        BigDecimal limit = BigDecimal.valueOf(1000);

        // When
        category.setApprovalLimit(limit);

        // Then
        assertThat(category.getApprovalLimit()).isEqualByComparingTo(limit);
    }

    @Test
    @DisplayName("setApprovalLimit - Should set zero approval limit")
    void setApprovalLimit_shouldSetZeroApprovalLimit() {
        // Given
        BigDecimal limit = BigDecimal.ZERO;

        // When
        category.setApprovalLimit(limit);

        // Then
        assertThat(category.getApprovalLimit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("setApprovalLimit - Should throw exception for negative approval limit")
    void setApprovalLimit_shouldThrowExceptionForNegativeLimit() {
        // Given
        BigDecimal negativeLimit = BigDecimal.valueOf(-100);

        // When / Then
        assertThatThrownBy(() -> category.setApprovalLimit(negativeLimit))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Approval limit cannot be negative");
    }

    @Test
    @DisplayName("setApprovalLimit - Should allow null approval limit")
    void setApprovalLimit_shouldAllowNullApprovalLimit() {
        // When
        category.setApprovalLimit(null);

        // Then
        assertThat(category.getApprovalLimit()).isNull();
    }

    // Edge cases and combinations
    @Test
    @DisplayName("Should handle category with no approval limit and requiresApproval true")
    void shouldHandleCategoryWithNoApprovalLimitAndRequiresApprovalTrue() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(null);

        // When
        boolean requiresApproval = category.requiresApprovalForAmount(BigDecimal.valueOf(1));
        boolean canAutoApprove = category.canAutoApprove(BigDecimal.valueOf(1));

        // Then
        assertThat(requiresApproval).isTrue();
        assertThat(canAutoApprove).isFalse();
    }

    @Test
    @DisplayName("Should handle category with approval limit at boundary")
    void shouldHandleCategoryWithApprovalLimitAtBoundary() {
        // Given
        BigDecimal limit = BigDecimal.valueOf(500);
        category.setRequiresApproval(true);
        category.setApprovalLimit(limit);

        // When
        boolean requiresApprovalForExact = category.requiresApprovalForAmount(limit);
        boolean requiresApprovalForAbove = category.requiresApprovalForAmount(limit.add(BigDecimal.ONE));
        boolean requiresApprovalForBelow = category.requiresApprovalForAmount(limit.subtract(BigDecimal.ONE));

        // Then
        assertThat(requiresApprovalForExact).isFalse();
        assertThat(requiresApprovalForAbove).isTrue();
        assertThat(requiresApprovalForBelow).isFalse();
    }

    @Test
    @DisplayName("Should handle inactive category with auto-approval enabled")
    void shouldHandleInactiveCategoryWithAutoApprovalEnabled() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(false);
        category.setIsActive(false);

        // When
        boolean canAutoApprove = category.canAutoApprove(BigDecimal.valueOf(100));

        // Then
        assertThat(canAutoApprove).isFalse();
    }

    @Test
    @DisplayName("Should handle large approval amounts")
    void shouldHandleLargeApprovalAmounts() {
        // Given
        BigDecimal largeLimit = new BigDecimal("999999999.99");
        category.setRequiresApproval(true);
        category.setApprovalLimit(largeLimit);

        // When
        boolean requiresApprovalBelow = category.requiresApprovalForAmount(largeLimit.subtract(BigDecimal.ONE));
        boolean requiresApprovalAbove = category.requiresApprovalForAmount(largeLimit.add(BigDecimal.ONE));

        // Then
        assertThat(requiresApprovalBelow).isFalse();
        assertThat(requiresApprovalAbove).isTrue();
    }

    @Test
    @DisplayName("Should handle decimal approval amounts")
    void shouldHandleDecimalApprovalAmounts() {
        // Given
        BigDecimal limit = new BigDecimal("500.50");
        category.setRequiresApproval(true);
        category.setApprovalLimit(limit);

        // When
        boolean requiresApprovalForExact = category.requiresApprovalForAmount(new BigDecimal("500.50"));
        boolean requiresApprovalForSlightlyAbove = category.requiresApprovalForAmount(new BigDecimal("500.51"));

        // Then
        assertThat(requiresApprovalForExact).isFalse();
        assertThat(requiresApprovalForSlightlyAbove).isTrue();
    }

    // ==================== Additional Builder Tests ====================

    @Test
    @DisplayName("Builder - Should create category with all fields")
    void builder_shouldCreateCategoryWithAllFields() {
        // Given
        String shopId = "shop-test";
        String name = "Test Category";
        String description = "Test Description";
        Boolean isActive = false;
        Boolean requiresApproval = false;
        BigDecimal approvalLimit = BigDecimal.valueOf(1000);
        String defaultPaymentMethod = "CREDIT_CARD";
        String glAccountCode = "GL-1001";
        Boolean taxDeductible = false;
        Boolean autoApprovalEnabled = true;

        // When
        ExpenseCategory newCategory = ExpenseCategory.builder()
            .shopId(shopId)
            .name(name)
            .description(description)
            .isActive(isActive)
            .requiresApproval(requiresApproval)
            .approvalLimit(approvalLimit)
            .defaultPaymentMethod(defaultPaymentMethod)
            .glAccountCode(glAccountCode)
            .taxDeductible(taxDeductible)
            .autoApprovalEnabled(autoApprovalEnabled)
            .build();

        // Then
        assertThat(newCategory.getShopId()).isEqualTo(shopId);
        assertThat(newCategory.getName()).isEqualTo(name);
        assertThat(newCategory.getDescription()).isEqualTo(description);
        assertThat(newCategory.getIsActive()).isEqualTo(isActive);
        assertThat(newCategory.getRequiresApproval()).isEqualTo(requiresApproval);
        assertThat(newCategory.getApprovalLimit()).isEqualByComparingTo(approvalLimit);
        assertThat(newCategory.getDefaultPaymentMethod()).isEqualTo(defaultPaymentMethod);
        assertThat(newCategory.getGlAccountCode()).isEqualTo(glAccountCode);
        assertThat(newCategory.getTaxDeductible()).isEqualTo(taxDeductible);
        assertThat(newCategory.getAutoApprovalEnabled()).isEqualTo(autoApprovalEnabled);
    }

    // ==================== Additional Edge Cases ====================

    @Test
    @DisplayName("canAutoApprove - Should return false when all conditions fail")
    void canAutoApprove_shouldReturnFalseWhenAllConditionsFail() {
        // Given
        category.setAutoApprovalEnabled(false);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(100));
        category.setIsActive(false);
        BigDecimal amount = BigDecimal.valueOf(500);

        // When
        boolean result = category.canAutoApprove(amount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should handle zero amount")
    void requiresApprovalForAmount_shouldHandleZeroAmount() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.ZERO);

        // When
        boolean requiresApprovalForZero = category.requiresApprovalForAmount(BigDecimal.ZERO);
        boolean requiresApprovalForPositive = category.requiresApprovalForAmount(BigDecimal.ONE);

        // Then
        assertThat(requiresApprovalForZero).isFalse(); // Zero equals limit
        assertThat(requiresApprovalForPositive).isTrue(); // Positive exceeds zero limit
    }

    @Test
    @DisplayName("canAutoApprove - Should handle category with zero approval limit")
    void canAutoApprove_shouldHandleCategoryWithZeroApprovalLimit() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.ZERO);
        category.setIsActive(true);

        // When
        boolean canAutoApproveZero = category.canAutoApprove(BigDecimal.ZERO);
        boolean canAutoApprovePositive = category.canAutoApprove(BigDecimal.ONE);

        // Then
        assertThat(canAutoApproveZero).isTrue(); // Zero equals limit
        assertThat(canAutoApprovePositive).isFalse(); // Positive exceeds limit
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should handle very large amounts")
    void requiresApprovalForAmount_shouldHandleVeryLargeAmounts() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(new BigDecimal("1000000"));
        BigDecimal veryLargeAmount = new BigDecimal("999999999.99");

        // When
        boolean result = category.requiresApprovalForAmount(veryLargeAmount);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("requiresApprovalForAmount - Should handle small decimal amounts")
    void requiresApprovalForAmount_shouldHandleSmallDecimalAmounts() {
        // Given
        category.setRequiresApproval(true);
        category.setApprovalLimit(new BigDecimal("10.00"));
        BigDecimal smallAmount = new BigDecimal("0.01");

        // When
        boolean result = category.requiresApprovalForAmount(smallAmount);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("activate - Should activate inactive category")
    void activate_shouldActivateInactiveCategory() {
        // Given
        category.setIsActive(false);

        // When
        category.activate();

        // Then
        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("canAutoApprove - Should require category to be active")
    void canAutoApprove_shouldRequireCategoryToBeActive() {
        // Given
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(false);
        category.setIsActive(false);

        // When
        boolean result = category.canAutoApprove(BigDecimal.valueOf(100));

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canAutoApprove - Should work when approval limit equals amount")
    void canAutoApprove_shouldWorkWhenApprovalLimitEqualsAmount() {
        // Given
        BigDecimal limit = BigDecimal.valueOf(500);
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(limit);
        category.setIsActive(true);

        // When
        boolean result = category.canAutoApprove(limit);

        // Then
        assertThat(result).isTrue(); // Amount equals limit, so doesn't require approval
    }

    @Test
    @DisplayName("setApprovalLimit - Should allow setting limit to null")
    void setApprovalLimit_shouldAllowSettingLimitToNull() {
        // Given
        category.setApprovalLimit(BigDecimal.valueOf(1000));

        // When
        category.setApprovalLimit(null);

        // Then
        assertThat(category.getApprovalLimit()).isNull();
    }

    @Test
    @DisplayName("setApprovalLimit - Should replace existing limit")
    void setApprovalLimit_shouldReplaceExistingLimit() {
        // Given
        category.setApprovalLimit(BigDecimal.valueOf(500));

        // When
        category.setApprovalLimit(BigDecimal.valueOf(1000));

        // Then
        assertThat(category.getApprovalLimit()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Complete workflow - Auto-approval scenario")
    void completeWorkflow_autoApprovalScenario() {
        // Given: Category with auto-approval enabled and $500 limit
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(true);
        category.setApprovalLimit(BigDecimal.valueOf(500));
        category.setIsActive(true);

        // When & Then: Amount below limit can be auto-approved
        assertThat(category.canAutoApprove(BigDecimal.valueOf(250))).isTrue();
        assertThat(category.requiresApprovalForAmount(BigDecimal.valueOf(250))).isFalse();

        // When & Then: Amount above limit requires manual approval
        assertThat(category.canAutoApprove(BigDecimal.valueOf(750))).isFalse();
        assertThat(category.requiresApprovalForAmount(BigDecimal.valueOf(750))).isTrue();
    }

    @Test
    @DisplayName("Complete workflow - No approval required scenario")
    void completeWorkflow_noApprovalRequiredScenario() {
        // Given: Category with no approval required
        category.setRequiresApproval(false);
        category.setAutoApprovalEnabled(true);
        category.setIsActive(true);

        // When & Then: Any amount can be auto-approved
        assertThat(category.canAutoApprove(BigDecimal.valueOf(1))).isTrue();
        assertThat(category.canAutoApprove(BigDecimal.valueOf(1000000))).isTrue();
        assertThat(category.requiresApprovalForAmount(BigDecimal.valueOf(1000000))).isFalse();
    }

    @Test
    @DisplayName("Complete workflow - Always requires approval scenario")
    void completeWorkflow_alwaysRequiresApprovalScenario() {
        // Given: Category with no approval limit (always requires approval)
        category.setRequiresApproval(true);
        category.setApprovalLimit(null);
        category.setAutoApprovalEnabled(false);
        category.setIsActive(true);

        // When & Then: All amounts require approval
        assertThat(category.requiresApprovalForAmount(BigDecimal.valueOf(0.01))).isTrue();
        assertThat(category.requiresApprovalForAmount(BigDecimal.valueOf(1000000))).isTrue();
        assertThat(category.canAutoApprove(BigDecimal.valueOf(1))).isFalse();
    }

    @Test
    @DisplayName("Complete workflow - Inactive category scenario")
    void completeWorkflow_inactiveCategoryScenario() {
        // Given: Inactive category
        category.setIsActive(false);
        category.setAutoApprovalEnabled(true);
        category.setRequiresApproval(false);

        // When & Then: Inactive category cannot auto-approve
        assertThat(category.canAutoApprove(BigDecimal.valueOf(100))).isFalse();

        // When: Activate the category
        category.activate();

        // Then: Now can auto-approve
        assertThat(category.canAutoApprove(BigDecimal.valueOf(100))).isTrue();
    }
}
