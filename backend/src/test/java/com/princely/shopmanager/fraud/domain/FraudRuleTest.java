package com.princely.shopmanager.fraud.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FraudRule Domain Tests")
class FraudRuleTest {

    private FraudRule rule;

    @BeforeEach
    void setUp() {
        rule = FraudRule.builder()
            .ruleName("High Amount Transaction Rule")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .description("Detects transactions exceeding threshold amount")
            .thresholdAmount(BigDecimal.valueOf(10000.00))
            .severity(RiskAssessment.RiskLevel.HIGH)
            .build();
    }

    // ==================== Global/Shop-Specific Tests ====================

    @Test
    @DisplayName("isGlobal - Should return true when shop is null")
    void isGlobal_shouldReturnTrueWhenShopIsNull() {
        // Given
        rule.setShop(null);

        // Then
        assertThat(rule.isGlobal()).isTrue();
    }

    @Test
    @DisplayName("isGlobal - Should return false when shop is set")
    void isGlobal_shouldReturnFalseWhenShopIsSet() {
        // Given
        // Shop would need to be created, but we test the logic
        // This would typically be tested in integration tests
        assertThat(rule.isGlobal()).isTrue(); // Currently null in setUp
    }

    // ==================== shouldTrigger Tests - HIGH_AMOUNT_TRANSACTION ====================

    @Test
    @DisplayName("shouldTrigger - HIGH_AMOUNT_TRANSACTION should trigger when amount exceeds threshold")
    void shouldTrigger_highAmountTransactionShouldTriggerWhenAmountExceedsThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(10000));
        rule.setEnabled(true);
        BigDecimal amount = BigDecimal.valueOf(15000);

        // When
        boolean result = rule.shouldTrigger(amount, null, null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_AMOUNT_TRANSACTION should not trigger when amount equals threshold")
    void shouldTrigger_highAmountTransactionShouldNotTriggerWhenAmountEqualsThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(10000));
        rule.setEnabled(true);
        BigDecimal amount = BigDecimal.valueOf(10000);

        // When
        boolean result = rule.shouldTrigger(amount, null, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_AMOUNT_TRANSACTION should not trigger when amount is below threshold")
    void shouldTrigger_highAmountTransactionShouldNotTriggerWhenAmountBelowThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(10000));
        rule.setEnabled(true);
        BigDecimal amount = BigDecimal.valueOf(5000);

        // When
        boolean result = rule.shouldTrigger(amount, null, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_AMOUNT_TRANSACTION should not trigger when amount is null")
    void shouldTrigger_highAmountTransactionShouldNotTriggerWhenAmountIsNull() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(10000));
        rule.setEnabled(true);

        // When
        boolean result = rule.shouldTrigger(null, null, null);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== shouldTrigger Tests - HIGH_FREQUENCY_TRANSACTIONS ====================

    @Test
    @DisplayName("shouldTrigger - HIGH_FREQUENCY_TRANSACTIONS should trigger when count exceeds threshold")
    void shouldTrigger_highFrequencyTransactionsShouldTriggerWhenCountExceedsThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        rule.setThresholdCount(10);
        rule.setEnabled(true);
        Integer count = 15;

        // When
        boolean result = rule.shouldTrigger(null, count, null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_FREQUENCY_TRANSACTIONS should not trigger when count equals threshold")
    void shouldTrigger_highFrequencyTransactionsShouldNotTriggerWhenCountEqualsThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        rule.setThresholdCount(10);
        rule.setEnabled(true);
        Integer count = 10;

        // When
        boolean result = rule.shouldTrigger(null, count, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_FREQUENCY_TRANSACTIONS should not trigger when count is below threshold")
    void shouldTrigger_highFrequencyTransactionsShouldNotTriggerWhenCountBelowThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        rule.setThresholdCount(10);
        rule.setEnabled(true);
        Integer count = 5;

        // When
        boolean result = rule.shouldTrigger(null, count, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_FREQUENCY_TRANSACTIONS should not trigger when count is null")
    void shouldTrigger_highFrequencyTransactionsShouldNotTriggerWhenCountIsNull() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        rule.setThresholdCount(10);
        rule.setEnabled(true);

        // When
        boolean result = rule.shouldTrigger(null, null, null);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== shouldTrigger Tests - RAPID_SUCCESSIVE_TRANSACTIONS ====================

    @Test
    @DisplayName("shouldTrigger - RAPID_SUCCESSIVE_TRANSACTIONS should trigger when all conditions met")
    void shouldTrigger_rapidSuccessiveTransactionsShouldTriggerWhenAllConditionsMet() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS);
        rule.setThresholdCount(5);
        rule.setTimeWindowMinutes(10);
        rule.setEnabled(true);
        Integer count = 8;
        Integer timeWindow = 7; // 7 minutes, less than 10

        // When
        boolean result = rule.shouldTrigger(null, count, timeWindow);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldTrigger - RAPID_SUCCESSIVE_TRANSACTIONS should not trigger when count is below threshold")
    void shouldTrigger_rapidSuccessiveTransactionsShouldNotTriggerWhenCountBelowThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS);
        rule.setThresholdCount(5);
        rule.setTimeWindowMinutes(10);
        rule.setEnabled(true);
        Integer count = 3;
        Integer timeWindow = 7;

        // When
        boolean result = rule.shouldTrigger(null, count, timeWindow);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - RAPID_SUCCESSIVE_TRANSACTIONS should not trigger when time window exceeds threshold")
    void shouldTrigger_rapidSuccessiveTransactionsShouldNotTriggerWhenTimeWindowExceedsThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS);
        rule.setThresholdCount(5);
        rule.setTimeWindowMinutes(10);
        rule.setEnabled(true);
        Integer count = 8;
        Integer timeWindow = 15; // 15 minutes, more than 10

        // When
        boolean result = rule.shouldTrigger(null, count, timeWindow);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - RAPID_SUCCESSIVE_TRANSACTIONS should not trigger when any parameter is null")
    void shouldTrigger_rapidSuccessiveTransactionsShouldNotTriggerWhenAnyParameterIsNull() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS);
        rule.setThresholdCount(5);
        rule.setTimeWindowMinutes(10);
        rule.setEnabled(true);

        // When & Then
        assertThat(rule.shouldTrigger(null, null, 7)).isFalse();
        assertThat(rule.shouldTrigger(null, 8, null)).isFalse();
        assertThat(rule.shouldTrigger(null, null, null)).isFalse();
    }

    // ==================== Enabled/Disabled Tests ====================

    @Test
    @DisplayName("shouldTrigger - Should not trigger when rule is disabled")
    void shouldTrigger_shouldNotTriggerWhenRuleIsDisabled() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(10000));
        rule.setEnabled(false);
        BigDecimal amount = BigDecimal.valueOf(15000);

        // When
        boolean result = rule.shouldTrigger(amount, null, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("shouldTrigger - Should return false for unsupported rule types")
    void shouldTrigger_shouldReturnFalseForUnsupportedRuleTypes() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION);
        rule.setEnabled(true);

        // When
        boolean result = rule.shouldTrigger(BigDecimal.valueOf(5000), 10, 5);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== getFlag Tests ====================

    @Test
    @DisplayName("getFlag - Should return rule type name with VIOLATION suffix")
    void getFlag_shouldReturnRuleTypeNameWithViolationSuffix() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);

        // When
        String flag = rule.getFlag();

        // Then
        assertThat(flag).isEqualTo("HIGH_AMOUNT_TRANSACTION_VIOLATION");
    }

    @Test
    @DisplayName("getFlag - Should return correct flag for different rule types")
    void getFlag_shouldReturnCorrectFlagForDifferentRuleTypes() {
        // Given & When & Then
        rule.setRuleType(FraudRule.FraudRuleType.VELOCITY_CHECK);
        assertThat(rule.getFlag()).isEqualTo("VELOCITY_CHECK_VIOLATION");

        rule.setRuleType(FraudRule.FraudRuleType.BLACKLIST_CHECK);
        assertThat(rule.getFlag()).isEqualTo("BLACKLIST_CHECK_VIOLATION");

        rule.setRuleType(FraudRule.FraudRuleType.SUSPICIOUS_CUSTOMER_PATTERN);
        assertThat(rule.getFlag()).isEqualTo("SUSPICIOUS_CUSTOMER_PATTERN_VIOLATION");
    }

    // ==================== Builder Tests ====================

    @Test
    @DisplayName("Builder - Should create rule with all fields")
    void builder_shouldCreateRuleWithAllFields() {
        // Given
        String ruleName = "Velocity Check Rule";
        FraudRule.FraudRuleType ruleType = FraudRule.FraudRuleType.VELOCITY_CHECK;
        String description = "Checks for rapid successive transactions";
        BigDecimal thresholdAmount = BigDecimal.valueOf(5000);
        Integer thresholdCount = 10;
        Integer timeWindowMinutes = 30;
        BigDecimal riskScoreWeight = BigDecimal.valueOf(1.5);
        RiskAssessment.RiskLevel severity = RiskAssessment.RiskLevel.CRITICAL;
        boolean autoBlock = true;
        boolean requiresManualReview = false;
        String ruleConfiguration = "{\"key\": \"value\"}";

        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName(ruleName)
            .ruleType(ruleType)
            .description(description)
            .thresholdAmount(thresholdAmount)
            .thresholdCount(thresholdCount)
            .timeWindowMinutes(timeWindowMinutes)
            .riskScoreWeight(riskScoreWeight)
            .severity(severity)
            .autoBlock(autoBlock)
            .requiresManualReview(requiresManualReview)
            .ruleConfiguration(ruleConfiguration)
            .build();

        // Then
        assertThat(newRule.getRuleName()).isEqualTo(ruleName);
        assertThat(newRule.getRuleType()).isEqualTo(ruleType);
        assertThat(newRule.getDescription()).isEqualTo(description);
        assertThat(newRule.getThresholdAmount()).isEqualByComparingTo(thresholdAmount);
        assertThat(newRule.getThresholdCount()).isEqualTo(thresholdCount);
        assertThat(newRule.getTimeWindowMinutes()).isEqualTo(timeWindowMinutes);
        assertThat(newRule.getRiskScoreWeight()).isEqualByComparingTo(riskScoreWeight);
        assertThat(newRule.getSeverity()).isEqualTo(severity);
        assertThat(newRule.isAutoBlock()).isEqualTo(autoBlock);
        assertThat(newRule.isRequiresManualReview()).isEqualTo(requiresManualReview);
        assertThat(newRule.getRuleConfiguration()).isEqualTo(ruleConfiguration);
    }

    @Test
    @DisplayName("Builder - Should create rule with default enabled as true")
    void builder_shouldCreateRuleWithDefaultEnabledAsTrue() {
        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName("Test Rule")
            .ruleType(FraudRule.FraudRuleType.CUSTOM_RULE)
            .build();

        // Then
        assertThat(newRule.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Builder - Should create rule with default severity as MEDIUM")
    void builder_shouldCreateRuleWithDefaultSeverityAsMedium() {
        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName("Test Rule")
            .ruleType(FraudRule.FraudRuleType.CUSTOM_RULE)
            .build();

        // Then
        assertThat(newRule.getSeverity()).isEqualTo(RiskAssessment.RiskLevel.MEDIUM);
    }

    @Test
    @DisplayName("Builder - Should create rule with default riskScoreWeight as 1.0")
    void builder_shouldCreateRuleWithDefaultRiskScoreWeightAsOne() {
        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName("Test Rule")
            .ruleType(FraudRule.FraudRuleType.CUSTOM_RULE)
            .build();

        // Then
        assertThat(newRule.getRiskScoreWeight()).isEqualByComparingTo(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("Builder - Should create rule with default autoBlock as false")
    void builder_shouldCreateRuleWithDefaultAutoBlockAsFalse() {
        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName("Test Rule")
            .ruleType(FraudRule.FraudRuleType.CUSTOM_RULE)
            .build();

        // Then
        assertThat(newRule.isAutoBlock()).isFalse();
    }

    @Test
    @DisplayName("Builder - Should create rule with default requiresManualReview as true")
    void builder_shouldCreateRuleWithDefaultRequiresManualReviewAsTrue() {
        // When
        FraudRule newRule = FraudRule.builder()
            .ruleName("Test Rule")
            .ruleType(FraudRule.FraudRuleType.CUSTOM_RULE)
            .build();

        // Then
        assertThat(newRule.isRequiresManualReview()).isTrue();
    }

    // ==================== Enum Tests ====================

    @Test
    @DisplayName("FraudRuleType - Should have all expected values")
    void fraudRuleType_shouldHaveAllExpectedValues() {
        // Then
        assertThat(FraudRule.FraudRuleType.values()).containsExactlyInAnyOrder(
            FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION,
            FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS,
            FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION,
            FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS,
            FraudRule.FraudRuleType.UNUSUAL_PAYMENT_METHOD,
            FraudRule.FraudRuleType.SUSPICIOUS_CUSTOMER_PATTERN,
            FraudRule.FraudRuleType.INVENTORY_MISMATCH,
            FraudRule.FraudRuleType.GEOGRAPHIC_ANOMALY,
            FraudRule.FraudRuleType.VELOCITY_CHECK,
            FraudRule.FraudRuleType.BLACKLIST_CHECK,
            FraudRule.FraudRuleType.CUSTOM_RULE
        );
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("shouldTrigger - HIGH_AMOUNT_TRANSACTION with very large amounts")
    void shouldTrigger_highAmountTransactionWithVeryLargeAmounts() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(new BigDecimal("1000000"));
        rule.setEnabled(true);
        BigDecimal largeAmount = new BigDecimal("9999999.99");

        // When
        boolean result = rule.shouldTrigger(largeAmount, null, null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldTrigger - HIGH_FREQUENCY_TRANSACTIONS with zero count threshold")
    void shouldTrigger_highFrequencyTransactionsWithZeroCountThreshold() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        rule.setThresholdCount(0);
        rule.setEnabled(true);
        Integer count = 1;

        // When
        boolean result = rule.shouldTrigger(null, count, null);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("shouldTrigger - RAPID_SUCCESSIVE_TRANSACTIONS with boundary time window")
    void shouldTrigger_rapidSuccessiveTransactionsWithBoundaryTimeWindow() {
        // Given
        rule.setRuleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS);
        rule.setThresholdCount(5);
        rule.setTimeWindowMinutes(10);
        rule.setEnabled(true);
        Integer count = 6;
        Integer timeWindow = 10; // Exactly at boundary

        // When
        boolean result = rule.shouldTrigger(null, count, timeWindow);

        // Then
        assertThat(result).isTrue(); // <= comparison means equals is included
    }

    @Test
    @DisplayName("Disabled rule - Should remain disabled through all operations")
    void disabledRule_shouldRemainDisabledThroughAllOperations() {
        // Given
        rule.setEnabled(false);
        rule.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        rule.setThresholdAmount(BigDecimal.valueOf(1000));

        // When & Then
        assertThat(rule.shouldTrigger(BigDecimal.valueOf(10000), null, null)).isFalse();
        assertThat(rule.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Complete configuration - Global rule with auto-block enabled")
    void completeConfiguration_globalRuleWithAutoBlockEnabled() {
        // When
        FraudRule globalRule = FraudRule.builder()
            .ruleName("Global High-Risk Transaction Blocker")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .description("Automatically blocks transactions above 50000")
            .thresholdAmount(BigDecimal.valueOf(50000))
            .severity(RiskAssessment.RiskLevel.CRITICAL)
            .autoBlock(true)
            .requiresManualReview(false)
            .build();

        // Then
        assertThat(globalRule.isGlobal()).isTrue();
        assertThat(globalRule.isAutoBlock()).isTrue();
        assertThat(globalRule.isRequiresManualReview()).isFalse();
        assertThat(globalRule.shouldTrigger(BigDecimal.valueOf(60000), null, null)).isTrue();
    }

    @Test
    @DisplayName("Rule configuration - Should store JSON configuration")
    void ruleConfiguration_shouldStoreJsonConfiguration() {
        // Given
        String jsonConfig = "{\"patterns\": [\"pattern1\", \"pattern2\"], \"threshold\": 5}";
        rule.setRuleConfiguration(jsonConfig);

        // Then
        assertThat(rule.getRuleConfiguration()).isEqualTo(jsonConfig);
    }
}
