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
}
