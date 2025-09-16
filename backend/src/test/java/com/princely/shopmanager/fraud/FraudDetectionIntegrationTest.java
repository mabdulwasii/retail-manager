package com.princely.shopmanager.fraud;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.repository.FraudAlertRepository;
import com.princely.shopmanager.fraud.repository.FraudRuleRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.fraud.service.FraudDetectionService;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for fraud detection and risk management system.
 * Tests the complete workflow from rule configuration to alert generation.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.features.fraud.enabled=true"
})
@Transactional
@DisplayName("Fraud Detection System Integration Tests")
class FraudDetectionIntegrationTest {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Test
    @DisplayName("Should detect high-amount transaction fraud and create alert")
    void shouldDetectHighAmountTransactionFraud() {
        // Given: Create a high-amount fraud rule
        FraudRule rule = FraudRule.builder()
            .ruleName("High Amount Alert")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(100000)) // ₦100,000 threshold
            .riskScoreWeight(BigDecimal.valueOf(3.0))
            .severity(RiskAssessment.RiskLevel.HIGH)
            .autoBlock(false)
            .requiresManualReview(true)
            .build();

        fraudRuleRepository.save(rule);

        // Given: Create a high-value transaction
        SalesTransaction transaction = createMockTransaction(BigDecimal.valueOf(150000)); // ₦150,000

        // When: Assess transaction risk
        RiskAssessment assessment = fraudDetectionService.assessTransactionRisk(transaction);

        // Then: Risk assessment should be created
        assertThat(assessment).isNotNull();
        assertThat(assessment.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(assessment.getRiskScore()).isGreaterThan(BigDecimal.valueOf(20));
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.PENDING);

        // Then: Fraud alert should be generated
        List<FraudAlert> alerts = fraudAlertRepository.findAll();
        assertThat(alerts).isNotEmpty();

        FraudAlert alert = alerts.get(0);
        assertThat(alert.getSeverity()).isEqualTo(FraudAlert.AlertSeverity.HIGH);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACTIVE);
        assertThat(alert.getAlertType()).isEqualTo(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION);
    }

    @Test
    @DisplayName("Should not create alert for low-risk transactions")
    void shouldNotCreateAlertForLowRiskTransactions() {
        // Given: Create a high-amount fraud rule
        FraudRule rule = FraudRule.builder()
            .ruleName("High Amount Alert")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(100000)) // ₦100,000 threshold
            .riskScoreWeight(BigDecimal.valueOf(1.0))
            .severity(RiskAssessment.RiskLevel.LOW)
            .autoBlock(false)
            .requiresManualReview(false)
            .build();

        fraudRuleRepository.save(rule);

        // Given: Create a low-value transaction
        SalesTransaction transaction = createMockTransaction(BigDecimal.valueOf(50000)); // ₦50,000

        // When: Assess transaction risk
        RiskAssessment assessment = fraudDetectionService.assessTransactionRisk(transaction);

        // Then: No assessment should be created (no rules triggered)
        assertThat(assessment).isNull();

        // Then: No fraud alerts should be generated
        List<FraudAlert> alerts = fraudAlertRepository.findAll();
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple rules and calculate cumulative risk score")
    void shouldHandleMultipleRulesAndCalculateCumulativeRiskScore() {
        // Given: Create multiple fraud rules
        FraudRule highAmountRule = FraudRule.builder()
            .ruleName("High Amount Alert")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .thresholdAmount(BigDecimal.valueOf(100000))
            .riskScoreWeight(BigDecimal.valueOf(2.0))
            .severity(RiskAssessment.RiskLevel.MEDIUM)
            .autoBlock(false)
            .requiresManualReview(true)
            .build();

        FraudRule unusualTimeRule = FraudRule.builder()
            .ruleName("Unusual Time Alert")
            .ruleType(FraudRule.FraudRuleType.UNUSUAL_TIME_TRANSACTION)
            .enabled(true)
            .riskScoreWeight(BigDecimal.valueOf(1.5))
            .severity(RiskAssessment.RiskLevel.MEDIUM)
            .autoBlock(false)
            .requiresManualReview(true)
            .build();

        fraudRuleRepository.saveAll(List.of(highAmountRule, unusualTimeRule));

        // Given: Create transaction that triggers both rules (high amount + unusual time)
        SalesTransaction transaction = createMockTransaction(BigDecimal.valueOf(150000));
        transaction.setTransactionDate(LocalDateTime.now().withHour(2)); // 2 AM - unusual time

        // When: Assess transaction risk
        RiskAssessment assessment = fraudDetectionService.assessTransactionRisk(transaction);

        // Then: Risk assessment should reflect multiple rule triggers
        assertThat(assessment).isNotNull();
        assertThat(assessment.getRiskLevel()).isIn(
            RiskAssessment.RiskLevel.MEDIUM,
            RiskAssessment.RiskLevel.HIGH
        );
        assertThat(assessment.getRiskScore()).isGreaterThan(BigDecimal.valueOf(30));
        assertThat(assessment.getFlags()).hasSize(2);

        // Then: Fraud alert should be generated
        List<FraudAlert> alerts = fraudAlertRepository.findAll();
        assertThat(alerts).isNotEmpty();
    }

    @Test
    @DisplayName("Should acknowledge and resolve fraud alerts")
    void shouldAcknowledgeAndResolveFraudAlerts() {
        // Given: Create a fraud alert
        FraudAlert alert = FraudAlert.builder()
            .alertNumber("TEST-ALERT-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.HIGH)
            .title("Test Alert")
            .description("Test fraud alert")
            .riskScore(BigDecimal.valueOf(75))
            .confidenceLevel(BigDecimal.valueOf(90))
            .build();

        alert = fraudAlertRepository.save(alert);

        // When: Acknowledge the alert
        fraudDetectionService.acknowledgeFraudAlert(alert.getId(), "test-user");

        // Then: Alert should be acknowledged
        FraudAlert acknowledgedAlert = fraudAlertRepository.findById(alert.getId()).orElseThrow();
        assertThat(acknowledgedAlert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACKNOWLEDGED);
        assertThat(acknowledgedAlert.getAcknowledgedBy()).isEqualTo("test-user");
        assertThat(acknowledgedAlert.getAcknowledgedAt()).isNotNull();

        // When: Resolve the alert
        fraudDetectionService.resolveFraudAlert(alert.getId(), "test-user", "Investigated and resolved");

        // Then: Alert should be resolved
        FraudAlert resolvedAlert = fraudAlertRepository.findById(alert.getId()).orElseThrow();
        assertThat(resolvedAlert.getStatus()).isEqualTo(FraudAlert.AlertStatus.RESOLVED);
        assertThat(resolvedAlert.getResolvedBy()).isEqualTo("test-user");
        assertThat(resolvedAlert.getResolvedAt()).isNotNull();
        assertThat(resolvedAlert.getResolutionNotes()).isEqualTo("Investigated and resolved");
    }

    @Test
    @DisplayName("Should approve and reject risk assessments")
    void shouldApproveAndRejectRiskAssessments() {
        // Given: Create a risk assessment
        RiskAssessment assessment = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .riskScore(BigDecimal.valueOf(65))
            .assessmentDate(LocalDateTime.now())
            .details("High-risk transaction requiring review")
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        assessment = riskAssessmentRepository.save(assessment);

        // When: Approve the assessment
        fraudDetectionService.approveRiskAssessment(
            assessment.getId(),
            "test-reviewer",
            "Reviewed and approved"
        );

        // Then: Assessment should be approved
        RiskAssessment approvedAssessment = riskAssessmentRepository.findById(assessment.getId()).orElseThrow();
        assertThat(approvedAssessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.APPROVED);
        assertThat(approvedAssessment.getReviewedBy()).isEqualTo("test-reviewer");
        assertThat(approvedAssessment.getReviewedAt()).isNotNull();
        assertThat(approvedAssessment.getReviewNotes()).isEqualTo("Reviewed and approved");

        // Given: Create another assessment for rejection
        RiskAssessment assessment2 = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.CRITICAL)
            .riskScore(BigDecimal.valueOf(85))
            .assessmentDate(LocalDateTime.now())
            .details("Critical risk transaction")
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();

        assessment2 = riskAssessmentRepository.save(assessment2);

        // When: Reject the assessment
        fraudDetectionService.rejectRiskAssessment(
            assessment2.getId(),
            "test-reviewer",
            "Rejected due to high risk",
            RiskAssessment.ResolutionAction.BLOCK_TRANSACTION
        );

        // Then: Assessment should be rejected
        RiskAssessment rejectedAssessment = riskAssessmentRepository.findById(assessment2.getId()).orElseThrow();
        assertThat(rejectedAssessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.REJECTED);
        assertThat(rejectedAssessment.getReviewedBy()).isEqualTo("test-reviewer");
        assertThat(rejectedAssessment.getReviewNotes()).isEqualTo("Rejected due to high risk");
        assertThat(rejectedAssessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.BLOCK_TRANSACTION);
    }

    private SalesTransaction createMockTransaction(BigDecimal amount) {
        // Create mock shop
        Shop shop = new Shop();
        shop.setId("test-shop-id");
        shop.setName("Test Shop");

        // Create mock cashier
        User cashier = new User();
        cashier.setId("test-cashier-id");
        cashier.setFirstName("Test");
        cashier.setLastName("Cashier");

        // Create transaction
        SalesTransaction transaction = new SalesTransaction();
        transaction.setId("test-transaction-id");
        transaction.setTransactionNumber("TXN-" + System.currentTimeMillis());
        transaction.setShop(shop);
        transaction.setCashier(cashier);
        transaction.setTotalAmount(amount);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(SalesTransaction.TransactionStatus.COMPLETED);

        return transaction;
    }
}