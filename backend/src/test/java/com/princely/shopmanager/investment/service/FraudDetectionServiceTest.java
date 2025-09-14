package com.princely.shopmanager.investment.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.investment.domain.FraudRule;
import com.princely.shopmanager.investment.domain.RiskAssessment;
import com.princely.shopmanager.investment.repository.FraudRuleRepository;
import com.princely.shopmanager.investment.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private FraudDetectionService fraudDetectionService;

    private Shop testShop;
    private User testCashier;
    private SalesTransaction testTransaction;
    private FraudRule highAmountRule;
    private FraudRule unusualTimeRule;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        testCashier = new User();
        testCashier.setId("cashier-1");
        testCashier.setUsername("test-cashier");

        testTransaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .shop(testShop)
            .cashier(testCashier)
            .totalAmount(BigDecimal.valueOf(15000))
            .transactionDate(LocalDateTime.now())
            .status(SalesTransaction.TransactionStatus.COMPLETED)
            .build();

        highAmountRule = FraudRule.builder()
            .id("rule-1")
            .ruleName("High Amount Transaction")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(10000))
            .riskScoreWeight(BigDecimal.valueOf(5.0))
            .severity(RiskAssessment.RiskLevel.HIGH)
            .autoBlock(false)
            .requiresManualReview(true)
            .build();

        unusualTimeRule = FraudRule.builder()
            .id("rule-2")
            .ruleName("Unusual Time Transaction")
            .ruleType(FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION)
            .enabled(true)
            .riskScoreWeight(BigDecimal.valueOf(3.0))
            .severity(RiskAssessment.RiskLevel.MEDIUM)
            .autoBlock(false)
            .requiresManualReview(false)
            .build();
    }

    @Test
    void testAssessTransactionRisk_HighAmountViolation() {
        // Given
        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(highAmountRule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(testShop, result.getShop());
        assertEquals(testTransaction, result.getTransaction());
        assertEquals(RiskAssessment.AssessmentType.TRANSACTION_FRAUD, result.getAssessmentType());
        assertEquals(RiskAssessment.RiskLevel.HIGH, result.getRiskLevel());
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(result.getRiskScore()));
        assertTrue(result.getFlags().contains("HIGH_AMOUNT_TRANSACTION_VIOLATION"));
        assertEquals(RiskAssessment.AssessmentStatus.PENDING, result.getStatus());

        // Verify transaction is updated
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(testTransaction.getFraudScore()));
        assertEquals("HIGH", testTransaction.getRiskLevel());
        assertTrue(testTransaction.isRequiresReview());

        verify(riskAssessmentRepository).save(any(RiskAssessment.class));
        verify(salesTransactionRepository).save(testTransaction);
        verify(auditService).logCustomEvent(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testAssessTransactionRisk_UnusualTimeViolation() {
        // Given - transaction at 2 AM
        testTransaction.setTransactionDate(LocalDateTime.now().with(LocalTime.of(2, 0)));

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(unusualTimeRule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(RiskAssessment.RiskLevel.MEDIUM, result.getRiskLevel());
        assertEquals(0, BigDecimal.valueOf(30.0).compareTo(result.getRiskScore()));
        assertTrue(result.getFlags().contains("UNUSUAL_TIME_TRANSACTION_VIOLATION"));
        assertEquals(RiskAssessment.AssessmentStatus.APPROVED, result.getStatus()); // Not high risk, auto-approved
    }

    @Test
    void testAssessTransactionRisk_MultipleViolations() {
        // Given - transaction at 2 AM with high amount
        testTransaction.setTransactionDate(LocalDateTime.now().with(LocalTime.of(2, 0)));

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(highAmountRule, unusualTimeRule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(RiskAssessment.RiskLevel.CRITICAL, result.getRiskLevel());
        assertEquals(0, BigDecimal.valueOf(80.0).compareTo(result.getRiskScore()));
        assertEquals(2, result.getFlags().size());
        assertTrue(result.getFlags().contains("HIGH_AMOUNT_TRANSACTION_VIOLATION"));
        assertTrue(result.getFlags().contains("UNUSUAL_TIME_TRANSACTION_VIOLATION"));
        assertEquals(RiskAssessment.AssessmentStatus.PENDING, result.getStatus());
    }

    @Test
    void testAssessTransactionRisk_NoRulesApplicable() {
        // Given
        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of());
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertNull(result);
        verify(riskAssessmentRepository, never()).save(any());
        verify(salesTransactionRepository, never()).save(any());
    }

    @Test
    void testAssessTransactionRisk_AutoBlockRule() {
        // Given
        highAmountRule.setAutoBlock(true);

        when(fraudRuleRepository.findGlobalEnabledRules()).thenReturn(List.of(highAmountRule));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true)).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(salesTransactionRepository.save(any(SalesTransaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        RiskAssessment result = fraudDetectionService.assessTransactionRisk(testTransaction);

        // Then
        assertNotNull(result);
        assertEquals(RiskAssessment.ResolutionAction.BLOCK_TRANSACTION, result.getResolutionAction());

        // Verify transaction is blocked
        assertEquals(SalesTransaction.TransactionStatus.CANCELLED, testTransaction.getStatus());
        assertEquals("Automatically blocked due to fraud risk assessment", testTransaction.getVoidReason());
        assertEquals("FRAUD_DETECTION_SYSTEM", testTransaction.getVoidedBy());
        assertNotNull(testTransaction.getVoidedAt());

        verify(auditService).logSecurityEvent(any(), any(), any(), any(), any(), any(), eq(true));
    }

    @Test
    void testApproveRiskAssessment() {
        // Given
        RiskAssessment assessment = RiskAssessment.builder()
            .id("assessment-1")
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        when(riskAssessmentRepository.findById("assessment-1")).thenReturn(java.util.Optional.of(assessment));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.approveRiskAssessment("assessment-1", "reviewer", "Reviewed and approved");

        // Then
        assertEquals(RiskAssessment.AssessmentStatus.APPROVED, assessment.getStatus());
        assertEquals("reviewer", assessment.getReviewedBy());
        assertEquals("Reviewed and approved", assessment.getReviewNotes());
        assertEquals(RiskAssessment.ResolutionAction.NO_ACTION, assessment.getResolutionAction());
        assertNotNull(assessment.getReviewedAt());

        verify(riskAssessmentRepository).save(assessment);
    }

    @Test
    void testRejectRiskAssessment() {
        // Given
        RiskAssessment assessment = RiskAssessment.builder()
            .id("assessment-1")
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        when(riskAssessmentRepository.findById("assessment-1")).thenReturn(java.util.Optional.of(assessment));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        fraudDetectionService.rejectRiskAssessment("assessment-1", "reviewer",
            "Suspicious activity confirmed", RiskAssessment.ResolutionAction.SUSPEND_ACCOUNT);

        // Then
        assertEquals(RiskAssessment.AssessmentStatus.REJECTED, assessment.getStatus());
        assertEquals("reviewer", assessment.getReviewedBy());
        assertEquals("Suspicious activity confirmed", assessment.getReviewNotes());
        assertEquals(RiskAssessment.ResolutionAction.SUSPEND_ACCOUNT, assessment.getResolutionAction());
        assertNotNull(assessment.getReviewedAt());

        verify(riskAssessmentRepository).save(assessment);
    }

    @Test
    void testGetHighRiskAssessments() {
        // Given
        List<RiskAssessment> expectedAssessments = List.of(
            RiskAssessment.builder().id("assessment-1").build(),
            RiskAssessment.builder().id("assessment-2").build()
        );

        when(riskAssessmentRepository.findByRiskLevelAndStatus(
            RiskAssessment.RiskLevel.HIGH,
            RiskAssessment.AssessmentStatus.PENDING))
            .thenReturn(expectedAssessments);

        // When
        List<RiskAssessment> result = fraudDetectionService.getHighRiskAssessments();

        // Then
        assertEquals(expectedAssessments, result);
        verify(riskAssessmentRepository).findByRiskLevelAndStatus(
            RiskAssessment.RiskLevel.HIGH,
            RiskAssessment.AssessmentStatus.PENDING);
    }
}