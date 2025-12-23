package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.repository.FraudAlertRepository;
import com.princely.shopmanager.fraud.repository.FraudRuleRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudDetectionService Tests")
class FraudDetectionServiceTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Shop testShop;
    private User testCashier;
    private SalesTransaction testTransaction;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenant = Tenant.builder()
            .id("tenant-1")
            .name("Test Tenant")
            .build();

        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .tenant(testTenant)
            .build();

        testCashier = User.builder()
            .id("cashier-1")
            .username("cashier@test.com")
            .email("cashier@test.com")
            .firstName("John")
            .lastName("Doe")
            .tenant(testTenant)
            .build();

        testTransaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .shop(testShop)
            .cashier(testCashier)
            .totalAmount(BigDecimal.valueOf(10000))
            .transactionDate(LocalDateTime.now())
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();
    }

    @Test
    @DisplayName("Should return null when no fraud rules are applicable")
    void shouldReturnNullWhenNoRules() {
        // Given
        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of());
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result).isNull();
        verify(riskAssessmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should assess LOW risk for transaction below thresholds")
    void shouldAssessLowRisk() {
        // Given: Rule threshold is 50000, transaction is 10000
        FraudRule rule = createHighAmountRule(BigDecimal.valueOf(50000), BigDecimal.valueOf(1.0), false);
        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.LOW);
        assertThat(result.getRiskScore()).isLessThan(BigDecimal.valueOf(20));
        assertThat(result.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.APPROVED);
        verify(fraudAlertRepository, never()).save(any()); // No alert for LOW risk
    }

    @Test
    @DisplayName("Should assess HIGH risk for high amount transaction")
    void shouldAssessHighRiskForHighAmount() {
        // Given: High amount rule with threshold 5000, transaction is 10000
        FraudRule rule = createHighAmountRule(BigDecimal.valueOf(5000), BigDecimal.valueOf(5.0), false);
        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        RiskAssessment savedAssessment = RiskAssessment.builder()
            .id("assessment-1")
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .riskScore(BigDecimal.valueOf(50))
            .build();

        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(savedAssessment);
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(result.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.PENDING);

        // Verify fraud alert created
        ArgumentCaptor<FraudAlert> alertCaptor = ArgumentCaptor.forClass(FraudAlert.class);
        verify(fraudAlertRepository).save(alertCaptor.capture());

        FraudAlert createdAlert = alertCaptor.getValue();
        assertThat(createdAlert.getAlertType()).isEqualTo(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION);
        assertThat(createdAlert.getSeverity()).isEqualTo(FraudAlert.AlertSeverity.HIGH);
        assertThat(createdAlert.getShop()).isEqualTo(testShop);
    }

    @Test
    @DisplayName("Should assess CRITICAL risk and auto-block transaction")
    void shouldAutoBlockCriticalRisk() {
        // Given: Rule with high weight that will trigger auto-block
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Critical Amount Rule")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(5000))
            .riskScoreWeight(BigDecimal.valueOf(8.5)) // Score will be 85 (CRITICAL)
            .autoBlock(true)
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        RiskAssessment savedAssessment = RiskAssessment.builder()
            .id("assessment-1")
            .riskLevel(RiskAssessment.RiskLevel.CRITICAL)
            .riskScore(BigDecimal.valueOf(85))
            .build();

        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(savedAssessment);
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.CRITICAL);

        // Verify transaction was blocked
        ArgumentCaptor<SalesTransaction> txnCaptor = ArgumentCaptor.forClass(SalesTransaction.class);
        verify(salesTransactionRepository, atLeastOnce()).save(txnCaptor.capture());

        SalesTransaction blockedTxn = txnCaptor.getAllValues().stream()
            .filter(t -> t.getStatus() == SalesTransaction.TransactionStatus.CANCELLED)
            .findFirst()
            .orElse(null);

        assertThat(blockedTxn).isNotNull();
        assertThat(blockedTxn.getVoidReason()).contains("fraud risk");
        assertThat(blockedTxn.getVoidedBy()).isEqualTo("FRAUD_DETECTION_SYSTEM");

        // Verify critical alert created
        ArgumentCaptor<FraudAlert> alertCaptor = ArgumentCaptor.forClass(FraudAlert.class);
        verify(fraudAlertRepository).save(alertCaptor.capture());
        assertThat(alertCaptor.getValue().getSeverity()).isEqualTo(FraudAlert.AlertSeverity.CRITICAL);
    }

    @Test
    @DisplayName("Should evaluate high frequency rule correctly")
    void shouldEvaluateHighFrequencyRule() {
        // Given: High frequency rule - more than 10 transactions in 60 minutes
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("High Frequency Rule")
            .ruleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS)
            .enabled(true)
            .thresholdCount(10)
            .timeWindowMinutes(60)
            .riskScoreWeight(BigDecimal.valueOf(5.0))
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        // Mock repository to return count > threshold
        LocalDateTime windowStart = testTransaction.getTransactionDate().minusMinutes(60);
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(
            eq("shop-1"),
            eq(windowStart),
            eq(testTransaction.getTransactionDate())
        )).thenReturn(15L); // More than threshold

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(result.getFlags()).contains("HIGH_FREQUENCY_TRANSACTIONS_VIOLATION");
    }

    @Test
    @DisplayName("Should evaluate unusual time rule correctly")
    void shouldEvaluateUnusualTimeRule() {
        // Given: Transaction at 3 AM (unusual time)
        SalesTransaction earlyMorningTxn = SalesTransaction.builder()
            .id("txn-2")
            .transactionNumber("TXN-002")
            .shop(testShop)
            .cashier(testCashier)
            .totalAmount(BigDecimal.valueOf(5000))
            .transactionDate(LocalDateTime.now().with(LocalTime.of(3, 0)))
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();

        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Unusual Time Rule")
            .ruleType(FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION)
            .enabled(true)
            .riskScoreWeight(BigDecimal.valueOf(3.0))
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(earlyMorningTxn);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.MEDIUM);
        assertThat(result.getFlags()).contains("UNUSUAL_TIME_TRANSACTION_VIOLATION");
    }

    @Test
    @DisplayName("Should evaluate rapid successive transactions rule")
    void shouldEvaluateRapidSuccessiveRule() {
        // Given: Rapid successive rule
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Rapid Successive Rule")
            .ruleType(FraudRule.FraudRuleType.RAPID_SUCCESSIVE_TRANSACTIONS)
            .enabled(true)
            .thresholdCount(5)
            .timeWindowMinutes(10)
            .riskScoreWeight(BigDecimal.valueOf(4.0))
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        // Mock recent transactions from same cashier
        List<SalesTransaction> recentTransactions = List.of(
            createTransaction("txn-1", testCashier),
            createTransaction("txn-2", testCashier),
            createTransaction("txn-3", testCashier),
            createTransaction("txn-4", testCashier),
            createTransaction("txn-5", testCashier),
            createTransaction("txn-6", testCashier)
        );

        when(salesTransactionRepository.findByShopAndDateRange(
            anyString(),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(recentTransactions);

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.MEDIUM);
        assertThat(result.getFlags()).contains("RAPID_SUCCESSIVE_TRANSACTIONS_VIOLATION");
    }

    @Test
    @DisplayName("Should evaluate velocity check rule")
    void shouldEvaluateVelocityRule() {
        // Given: Velocity rule - total amount exceeds 5x threshold
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Velocity Check")
            .ruleType(FraudRule.FraudRuleType.VELOCITY_CHECK)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(10000))
            .timeWindowMinutes(60)
            .riskScoreWeight(BigDecimal.valueOf(6.0))
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        // Mock large transactions that exceed 5x threshold
        List<SalesTransaction> largeTransactions = List.of(
            createTransactionWithAmount("txn-1", BigDecimal.valueOf(20000)),
            createTransactionWithAmount("txn-2", BigDecimal.valueOf(25000))
        );

        when(salesTransactionRepository.findLargeTransactionsSince(
            anyString(),
            eq(BigDecimal.valueOf(10000)),
            any(LocalDateTime.class)
        )).thenReturn(largeTransactions);

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(result.getFlags()).contains("VELOCITY_CHECK_VIOLATION");
    }

    @Test
    @DisplayName("Should combine multiple triggered rules")
    void shouldCombineMultipleRules() {
        // Given: Multiple rules that will all trigger
        FraudRule highAmountRule = createHighAmountRule(BigDecimal.valueOf(5000), BigDecimal.valueOf(3.0), false);
        FraudRule unusualTimeRule = FraudRule.builder()
            .id("rule-2")
            .ruleName("Unusual Time")
            .ruleType(FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION)
            .enabled(true)
            .riskScoreWeight(BigDecimal.valueOf(3.0))
            .build();

        // Transaction at 2 AM with high amount
        SalesTransaction highRiskTxn = SalesTransaction.builder()
            .id("txn-3")
            .transactionNumber("TXN-003")
            .shop(testShop)
            .cashier(testCashier)
            .totalAmount(BigDecimal.valueOf(15000))
            .transactionDate(LocalDateTime.now().with(LocalTime.of(2, 0)))
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(highAmountRule, unusualTimeRule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(highRiskTxn);

        // Then: Risk score should be sum of both rules (30 + 30 = 60)
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(result.getRiskScore()).isEqualByComparingTo(BigDecimal.valueOf(60.00));
        assertThat(result.getFlags()).hasSize(2);
        assertThat(result.getFlags()).contains("HIGH_AMOUNT_TRANSACTION_VIOLATION");
        assertThat(result.getFlags()).contains("UNUSUAL_TIME_TRANSACTION_VIOLATION");
    }

    @Test
    @DisplayName("Should cap risk score at 100")
    void shouldCapRiskScoreAt100() {
        // Given: Rules with very high weights
        FraudRule rule1 = createHighAmountRule(BigDecimal.valueOf(1000), BigDecimal.valueOf(8.0), false);
        FraudRule rule2 = FraudRule.builder()
            .id("rule-2")
            .ruleName("High Frequency")
            .ruleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS)
            .enabled(true)
            .thresholdCount(5)
            .timeWindowMinutes(60)
            .riskScoreWeight(BigDecimal.valueOf(9.0))
            .build();

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(rule1, rule2));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(
            anyString(),
            any(LocalDateTime.class),
            any(LocalDateTime.class)
        )).thenReturn(10L);

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then: Score should be capped at 100
        assertThat(result.getRiskScore()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.CRITICAL);
    }

    @Test
    @DisplayName("Should approve risk assessment successfully")
    void shouldApproveRiskAssessment() {
        // Given
        RiskAssessment assessment = RiskAssessment.builder()
            .id("assessment-1")
            .riskLevel(RiskAssessment.RiskLevel.MEDIUM)
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        when(riskAssessmentRepository.findById("assessment-1")).thenReturn(Optional.of(assessment));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.approveRiskAssessment("assessment-1", "admin", "Looks legitimate");

        // Then
        verify(riskAssessmentRepository).save(assessment);
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.APPROVED);
        assertThat(assessment.getReviewedBy()).isEqualTo("admin");
        assertThat(assessment.getReviewNotes()).isEqualTo("Looks legitimate");
    }

    @Test
    @DisplayName("Should reject risk assessment successfully")
    void shouldRejectRiskAssessment() {
        // Given
        RiskAssessment assessment = RiskAssessment.builder()
            .id("assessment-1")
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        when(riskAssessmentRepository.findById("assessment-1")).thenReturn(Optional.of(assessment));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.rejectRiskAssessment(
            "assessment-1",
            "admin",
            "Confirmed fraud",
            RiskAssessment.ResolutionAction.BLOCK_TRANSACTION
        );

        // Then
        verify(riskAssessmentRepository).save(assessment);
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.REJECTED);
        assertThat(assessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.BLOCK_TRANSACTION);
    }

    @Test
    @DisplayName("Should throw exception when risk assessment not found for approval")
    void shouldThrowExceptionWhenAssessmentNotFoundForApproval() {
        // Given
        when(riskAssessmentRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
            fraudDetectionService.approveRiskAssessment("nonexistent", "admin", "notes")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Risk assessment not found");
    }

    @Test
    @DisplayName("Should acknowledge fraud alert successfully")
    void shouldAcknowledgeFraudAlert() {
        // Given
        FraudAlert alert = FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALERT-001")
            .status(FraudAlert.AlertStatus.ACTIVE)
            .build();

        when(fraudAlertRepository.findById("alert-1")).thenReturn(Optional.of(alert));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.acknowledgeFraudAlert("alert-1", "admin");

        // Then
        verify(fraudAlertRepository).save(alert);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should resolve fraud alert successfully")
    void shouldResolveFraudAlert() {
        // Given
        FraudAlert alert = FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALERT-001")
            .status(FraudAlert.AlertStatus.ACKNOWLEDGED)
            .build();

        when(fraudAlertRepository.findById("alert-1")).thenReturn(Optional.of(alert));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.resolveFraudAlert("alert-1", "admin", "False positive");

        // Then
        verify(fraudAlertRepository).save(alert);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.RESOLVED);
        assertThat(alert.getResolvedBy()).isEqualTo("admin");
        assertThat(alert.getResolutionNotes()).isEqualTo("False positive");
    }

    @Test
    @DisplayName("Should throw exception when fraud alert not found")
    void shouldThrowExceptionWhenAlertNotFound() {
        // Given
        when(fraudAlertRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
            fraudDetectionService.acknowledgeFraudAlert("nonexistent", "admin")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fraud alert not found");
    }

    // Helper methods
    private FraudRule createHighAmountRule(BigDecimal threshold, BigDecimal weight, boolean autoBlock) {
        return FraudRule.builder()
            .id("rule-high-amount")
            .ruleName("High Amount Transaction")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(threshold)
            .riskScoreWeight(weight)
            .autoBlock(autoBlock)
            .build();
    }

    private SalesTransaction createTransaction(String id, User cashier) {
        return SalesTransaction.builder()
            .id(id)
            .transactionNumber("TXN-" + id)
            .shop(testShop)
            .cashier(cashier)
            .totalAmount(BigDecimal.valueOf(1000))
            .transactionDate(LocalDateTime.now())
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();
    }

    private SalesTransaction createTransactionWithAmount(String id, BigDecimal amount) {
        return SalesTransaction.builder()
            .id(id)
            .transactionNumber("TXN-" + id)
            .shop(testShop)
            .cashier(testCashier)
            .totalAmount(amount)
            .transactionDate(LocalDateTime.now())
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();
    }
}
