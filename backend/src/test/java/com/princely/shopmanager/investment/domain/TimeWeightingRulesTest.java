package com.princely.shopmanager.investment.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TimeWeightingRules Domain Tests")
class TimeWeightingRulesTest {

    private TimeWeightingRules rules;

    @BeforeEach
    void setUp() {
        rules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();
    }

    // Default values tests
    @Test
    @DisplayName("Should have correct default baseYears")
    void shouldHaveCorrectDefaultBaseYears() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getBaseYears()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("Should have correct default baseMultiplier")
    void shouldHaveCorrectDefaultBaseMultiplier() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getBaseMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("Should have correct default year2Threshold")
    void shouldHaveCorrectDefaultYear2Threshold() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getYear2Threshold()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test
    @DisplayName("Should have correct default year2Multiplier")
    void shouldHaveCorrectDefaultYear2Multiplier() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getYear2Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("Should have correct default year3Threshold")
    void shouldHaveCorrectDefaultYear3Threshold() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getYear3Threshold()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
    }

    @Test
    @DisplayName("Should have correct default year3Multiplier")
    void shouldHaveCorrectDefaultYear3Multiplier() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getYear3Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("Should have correct default maxMultiplier")
    void shouldHaveCorrectDefaultMaxMultiplier() {
        // Given
        TimeWeightingRules defaultRules = TimeWeightingRules.builder().build();

        // Then
        assertThat(defaultRules.getMaxMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    // getMultiplierForYears tests - Below baseYears
    @Test
    @DisplayName("getMultiplierForYears - Should return baseMultiplier for zero years")
    void getMultiplierForYears_shouldReturnBaseMultiplierForZeroYears() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.ZERO);

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return baseMultiplier for 0.5 years")
    void getMultiplierForYears_shouldReturnBaseMultiplierForHalfYear() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(0.5));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return baseMultiplier for exactly baseYears")
    void getMultiplierForYears_shouldReturnBaseMultiplierForExactlyBaseYears() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(1.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    // getMultiplierForYears tests - Between baseYears and year2Threshold (Linear Interpolation)
    @Test
    @DisplayName("getMultiplierForYears - Should interpolate for 1.5 years")
    void getMultiplierForYears_shouldInterpolateFor1Point5Years() {
        // Given: 1.5 years is midway between 1.0 and 2.0
        // Expected: Midway between 1.0 and 1.2 = 1.1

        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(1.5));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.1));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should interpolate for 1.25 years")
    void getMultiplierForYears_shouldInterpolateFor1Point25Years() {
        // Given: 1.25 years is 25% of the way from 1.0 to 2.0
        // Progress: 0.25 / 1.0 = 0.25
        // Bonus: (1.2 - 1.0) * 0.25 = 0.05
        // Expected: 1.0 + 0.05 = 1.05

        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(1.25));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.05));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should interpolate for 1.75 years")
    void getMultiplierForYears_shouldInterpolateFor1Point75Years() {
        // Given: 1.75 years is 75% of the way from 1.0 to 2.0
        // Progress: 0.75 / 1.0 = 0.75
        // Bonus: (1.2 - 1.0) * 0.75 = 0.15
        // Expected: 1.0 + 0.15 = 1.15

        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(1.75));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.15));
    }

    // getMultiplierForYears tests - Exactly at thresholds
    @Test
    @DisplayName("getMultiplierForYears - Should return year2Multiplier for exactly 2.0 years")
    void getMultiplierForYears_shouldReturnYear2MultiplierForExactly2Years() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(2.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return year2Multiplier for 2.5 years")
    void getMultiplierForYears_shouldReturnYear2MultiplierFor2Point5Years() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(2.5));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return year3Multiplier for exactly 3.0 years")
    void getMultiplierForYears_shouldReturnYear3MultiplierForExactly3Years() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(3.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return year3Multiplier for 4.0 years")
    void getMultiplierForYears_shouldReturnYear3MultiplierFor4Years() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(4.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should return year3Multiplier for 10.0 years")
    void getMultiplierForYears_shouldReturnYear3MultiplierFor10Years() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(10.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    // getMultiplierForYears tests - Max cap enforcement
    @Test
    @DisplayName("getMultiplierForYears - Should cap at maxMultiplier when year3Multiplier exceeds cap")
    void getMultiplierForYears_shouldCapAtMaxMultiplierWhenExceeded() {
        // Given: year3Multiplier (2.5) > maxMultiplier (2.0)
        TimeWeightingRules highBonusRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.5))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(2.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        BigDecimal multiplier = highBonusRules.getMultiplierForYears(BigDecimal.valueOf(3.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(2.0));
    }

    @Test
    @DisplayName("getMultiplierForYears - Should not cap when multiplier below max")
    void getMultiplierForYears_shouldNotCapWhenBelowMax() {
        // Given: year3Multiplier (1.5) < maxMultiplier (2.0)
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(3.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    // isValid tests - Valid configurations
    @Test
    @DisplayName("isValid - Should return true for valid configuration")
    void isValid_shouldReturnTrueForValidConfiguration() {
        // When
        boolean isValid = rules.isValid();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("isValid - Should return true when multipliers are equal")
    void isValid_shouldReturnTrueWhenMultipliersAreEqual() {
        // Given: Equal multipliers are allowed (<=)
        TimeWeightingRules equalMultiplierRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.0))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.0))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = equalMultiplierRules.isValid();

        // Then
        assertThat(isValid).isTrue();
    }

    // isValid tests - Invalid thresholds
    @Test
    @DisplayName("isValid - Should return false when baseYears >= year2Threshold")
    void isValid_shouldReturnFalseWhenBaseYearsGreaterThanOrEqualToYear2() {
        // Given
        TimeWeightingRules invalidRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(2.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = invalidRules.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isValid - Should return false when year2Threshold >= year3Threshold")
    void isValid_shouldReturnFalseWhenYear2GreaterThanOrEqualToYear3() {
        // Given
        TimeWeightingRules invalidRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(3.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = invalidRules.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isValid - Should return false when thresholds are inverted")
    void isValid_shouldReturnFalseWhenThresholdsAreInverted() {
        // Given
        TimeWeightingRules invalidRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(3.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(1.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = invalidRules.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    // isValid tests - Invalid multipliers
    @Test
    @DisplayName("isValid - Should return false when baseMultiplier > year2Multiplier")
    void isValid_shouldReturnFalseWhenBaseMultiplierGreaterThanYear2() {
        // Given
        TimeWeightingRules invalidRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.5))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.2))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.8))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = invalidRules.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("isValid - Should return false when year2Multiplier > year3Multiplier")
    void isValid_shouldReturnFalseWhenYear2MultiplierGreaterThanYear3() {
        // Given
        TimeWeightingRules invalidRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.0))
            .year2Multiplier(BigDecimal.valueOf(1.8))
            .year3Threshold(BigDecimal.valueOf(3.0))
            .year3Multiplier(BigDecimal.valueOf(1.5))
            .maxMultiplier(BigDecimal.valueOf(2.0))
            .build();

        // When
        boolean isValid = invalidRules.isValid();

        // Then
        assertThat(isValid).isFalse();
    }

    // Edge cases
    @Test
    @DisplayName("Should handle very small year increments")
    void shouldHandleVerySmallYearIncrements() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(1.01));

        // Then
        assertThat(multiplier).isGreaterThan(BigDecimal.valueOf(1.0));
        assertThat(multiplier).isLessThan(BigDecimal.valueOf(1.2));
    }

    @Test
    @DisplayName("Should handle very large year values")
    void shouldHandleVeryLargeYearValues() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(100.0));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.5));
    }

    @Test
    @DisplayName("Should handle decimal year values")
    void shouldHandleDecimalYearValues() {
        // When
        BigDecimal multiplier = rules.getMultiplierForYears(BigDecimal.valueOf(2.75));

        // Then
        assertThat(multiplier).isEqualByComparingTo(BigDecimal.valueOf(1.2));
    }

    // Constructor and builder tests
    @Test
    @DisplayName("Should create with no-args constructor")
    void shouldCreateWithNoArgsConstructor() {
        // When
        TimeWeightingRules newRules = new TimeWeightingRules();

        // Then
        assertThat(newRules).isNotNull();
    }

    @Test
    @DisplayName("Should create with all-args constructor")
    void shouldCreateWithAllArgsConstructor() {
        // When
        TimeWeightingRules newRules = new TimeWeightingRules(
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(1.0),
            BigDecimal.valueOf(2.0),
            BigDecimal.valueOf(1.2),
            BigDecimal.valueOf(3.0),
            BigDecimal.valueOf(1.5),
            BigDecimal.valueOf(2.0)
        );

        // Then
        assertThat(newRules.getBaseYears()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        assertThat(newRules.getYear2Threshold()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        assertThat(newRules.getYear3Threshold()).isEqualByComparingTo(BigDecimal.valueOf(3.0));
    }

    @Test
    @DisplayName("Should create with builder")
    void shouldCreateWithBuilder() {
        // When
        TimeWeightingRules newRules = TimeWeightingRules.builder()
            .baseYears(BigDecimal.valueOf(1.0))
            .baseMultiplier(BigDecimal.valueOf(1.0))
            .year2Threshold(BigDecimal.valueOf(2.5))
            .year2Multiplier(BigDecimal.valueOf(1.3))
            .year3Threshold(BigDecimal.valueOf(4.0))
            .year3Multiplier(BigDecimal.valueOf(1.6))
            .maxMultiplier(BigDecimal.valueOf(2.5))
            .build();

        // Then
        assertThat(newRules.getYear2Threshold()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
        assertThat(newRules.getYear2Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.3));
        assertThat(newRules.getYear3Threshold()).isEqualByComparingTo(BigDecimal.valueOf(4.0));
        assertThat(newRules.getYear3Multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.6));
        assertThat(newRules.getMaxMultiplier()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }
}
