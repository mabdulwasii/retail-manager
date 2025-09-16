package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.repository.FraudAlertRepository;
import com.princely.shopmanager.fraud.repository.FraudRuleRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.fraud.event.FraudAlertCreatedEvent;
import com.princely.shopmanager.fraud.event.RiskAssessmentCreatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.features.fraud.enabled", havingValue = "true")
public class FraudDetectionService {

    private final FraudRuleRepository fraudRuleRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RiskAssessment assessTransactionRisk(SalesTransaction transaction) {
        log.debug("Assessing fraud risk for transaction {}", transaction.getTransactionNumber());

        // Get applicable fraud rules
        List<FraudRule> rules = getApplicableFraudRules(transaction);

        if (rules.isEmpty()) {
            log.debug("No fraud rules applicable for transaction {}", transaction.getTransactionNumber());
            return null;
        }

        // Calculate risk score
        FraudAssessmentResult result = calculateRiskScore(transaction, rules);

        // Create risk assessment
        RiskAssessment assessment = RiskAssessment.builder()
            .shop(transaction.getShop())
            .transaction(transaction)
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(result.riskLevel())
            .riskScore(result.riskScore())
            .assessmentDate(LocalDateTime.now())
            .flags(result.flags())
            .details(result.details())
            .status(result.riskLevel().ordinal() >= RiskAssessment.RiskLevel.HIGH.ordinal() ?
                RiskAssessment.AssessmentStatus.PENDING : RiskAssessment.AssessmentStatus.APPROVED)
            .build();

        assessment = riskAssessmentRepository.save(assessment);

        // Publish risk assessment created event
        eventPublisher.publishEvent(new RiskAssessmentCreatedEvent(
            this, assessment, getCurrentTenantId(), transaction.getShop().getId()));

        // Create fraud alerts if high risk
        if (result.riskLevel() == RiskAssessment.RiskLevel.HIGH ||
            result.riskLevel() == RiskAssessment.RiskLevel.CRITICAL) {
            createFraudAlert(transaction, assessment, result);
        }

        // Update transaction with fraud information
        transaction.setFraudScore(result.riskScore());
        transaction.setRiskLevel(result.riskLevel().name());
        transaction.setRequiresReview(assessment.requiresReview());
        transaction.setFraudFlags(String.join(",", result.flags()));
        salesTransactionRepository.save(transaction);

        // Auto-block if required
        if (result.shouldAutoBlock()) {
            blockTransaction(transaction, assessment);
        }

        log.info("Fraud assessment completed for transaction {} - Risk Level: {}, Score: {}",
            transaction.getTransactionNumber(), result.riskLevel(), result.riskScore());

        auditService.logCustomEvent(
            transaction.getShop(),
            "SYSTEM",
            "fraud-detection",
            AuditLog.AuditCategory.SECURITY_EVENT,
            AuditLog.ActionType.CREATE,
            "RISK_ASSESSMENT",
            assessment.getId(),
            String.format("Fraud risk assessment - Level: %s, Score: %s",
                result.riskLevel(), result.riskScore()),
            Map.of(
                "transaction_id", transaction.getId(),
                "risk_score", result.riskScore().toString(),
                "flags", String.join(",", result.flags())
            ),
            result.riskLevel() == RiskAssessment.RiskLevel.CRITICAL ?
                AuditLog.Severity.CRITICAL : AuditLog.Severity.WARNING
        );

        return assessment;
    }

    private void createFraudAlert(SalesTransaction transaction, RiskAssessment assessment, FraudAssessmentResult result) {
        FraudAlert alert = FraudAlert.builder()
            .alertNumber(generateAlertNumber())
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(mapRiskLevelToAlertSeverity(result.riskLevel()))
            .title(String.format("High Risk Transaction: %s", transaction.getTransactionNumber()))
            .description(String.format(
                "Transaction %s flagged for fraud with risk score %.2f. Flags: %s",
                transaction.getTransactionNumber(),
                result.riskScore(),
                String.join(", ", result.flags())
            ))
            .shop(transaction.getShop())
            .transactionId(transaction.getId())
            .riskScore(result.riskScore())
            .confidenceLevel(BigDecimal.valueOf(85.0)) // Default confidence
            .evidence(createEvidenceMap(transaction, result))
            .detectionRule(String.join(",", result.triggeredRules()))
            .build();

        alert = fraudAlertRepository.save(alert);

        // Publish fraud alert created event
        eventPublisher.publishEvent(new FraudAlertCreatedEvent(
            this, alert, getCurrentTenantId(), transaction.getShop().getId()));

        log.warn("Fraud alert {} created for transaction {}",
            alert.getAlertNumber(), transaction.getTransactionNumber());
    }

    private Map<String, String> createEvidenceMap(SalesTransaction transaction, FraudAssessmentResult result) {
        Map<String, String> evidence = new HashMap<>();
        evidence.put("transaction_amount", transaction.getTotalAmount().toString());
        evidence.put("transaction_time", transaction.getTransactionDate().toString());
        evidence.put("cashier_id", transaction.getCashier().getId());
        evidence.put("flags", String.join(",", result.flags()));
        evidence.put("risk_score", result.riskScore().toString());
        return evidence;
    }

    private FraudAlert.AlertSeverity mapRiskLevelToAlertSeverity(RiskAssessment.RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> FraudAlert.AlertSeverity.LOW;
            case MEDIUM -> FraudAlert.AlertSeverity.MEDIUM;
            case HIGH -> FraudAlert.AlertSeverity.HIGH;
            case CRITICAL -> FraudAlert.AlertSeverity.CRITICAL;
        };
    }

    private String generateAlertNumber() {
        return "ALERT-" + System.currentTimeMillis() + "-" +
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<FraudRule> getApplicableFraudRules(SalesTransaction transaction) {
        // Get both global and shop-specific rules
        List<FraudRule> globalRules = fraudRuleRepository.findGlobalEnabledRules();
        List<FraudRule> shopRules = fraudRuleRepository.findByShopAndEnabled(transaction.getShop(), true);

        List<FraudRule> allRules = new ArrayList<>(globalRules);
        allRules.addAll(shopRules);

        return allRules;
    }

    private FraudAssessmentResult calculateRiskScore(SalesTransaction transaction, List<FraudRule> rules) {
        BigDecimal totalRiskScore = BigDecimal.ZERO;
        List<String> flags = new ArrayList<>();
        List<String> triggeredRules = new ArrayList<>();
        StringBuilder details = new StringBuilder();
        boolean shouldAutoBlock = false;

        for (FraudRule rule : rules) {
            boolean ruleTriggered = evaluateRule(rule, transaction);

            if (ruleTriggered) {
                BigDecimal ruleScore = rule.getRiskScoreWeight().multiply(BigDecimal.valueOf(10));
                totalRiskScore = totalRiskScore.add(ruleScore);
                flags.add(rule.getFlag());
                triggeredRules.add(rule.getRuleName());
                details.append(String.format("Rule '%s' triggered. ", rule.getRuleName()));

                if (rule.isAutoBlock()) {
                    shouldAutoBlock = true;
                }

                log.debug("Fraud rule '{}' triggered for transaction {} - Score: {}",
                    rule.getRuleName(), transaction.getTransactionNumber(), ruleScore);
            }
        }

        // Normalize risk score (0-100)
        BigDecimal normalizedScore = totalRiskScore.min(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);

        RiskAssessment.RiskLevel riskLevel = determineRiskLevel(normalizedScore);

        return new FraudAssessmentResult(
            normalizedScore,
            riskLevel,
            flags,
            details.toString(),
            shouldAutoBlock,
            triggeredRules
        );
    }

    private boolean evaluateRule(FraudRule rule, SalesTransaction transaction) {
        return switch (rule.getRuleType()) {
            case HIGH_AMOUNT_TRANSACTION -> evaluateHighAmountRule(rule, transaction);
            case HIGH_FREQUENCY_TRANSACTIONS -> evaluateHighFrequencyRule(rule, transaction);
            case UNUSUAL_TIME_TRANSACTION -> evaluateUnusualTimeRule(rule, transaction);
            case RAPID_SUCCESSIVE_TRANSACTIONS -> evaluateRapidSuccessiveRule(rule, transaction);
            case VELOCITY_CHECK -> evaluateVelocityRule(rule, transaction);
            default -> false; // Other rules need more complex implementation
        };
    }

    private boolean evaluateHighAmountRule(FraudRule rule, SalesTransaction transaction) {
        return rule.getThresholdAmount() != null &&
            transaction.getTotalAmount().compareTo(rule.getThresholdAmount()) > 0;
    }

    private boolean evaluateHighFrequencyRule(FraudRule rule, SalesTransaction transaction) {
        if (rule.getThresholdCount() == null || rule.getTimeWindowMinutes() == null) {
            return false;
        }

        LocalDateTime windowStart = transaction.getTransactionDate()
            .minusMinutes(rule.getTimeWindowMinutes());

        long transactionCount = salesTransactionRepository.countTransactionsByShopAndPeriod(
            transaction.getShop().getId(),
            windowStart,
            transaction.getTransactionDate()
        );

        return transactionCount > rule.getThresholdCount();
    }

    private boolean evaluateUnusualTimeRule(FraudRule rule, SalesTransaction transaction) {
        LocalTime transactionTime = transaction.getTransactionDate().toLocalTime();

        // Consider unusual times as before 6 AM or after 11 PM
        return transactionTime.isBefore(LocalTime.of(6, 0)) ||
            transactionTime.isAfter(LocalTime.of(23, 0));
    }

    private boolean evaluateRapidSuccessiveRule(FraudRule rule, SalesTransaction transaction) {
        if (rule.getThresholdCount() == null || rule.getTimeWindowMinutes() == null) {
            return false;
        }

        // Check for rapid successive transactions from the same cashier
        LocalDateTime windowStart = transaction.getTransactionDate()
            .minusMinutes(rule.getTimeWindowMinutes());

        // This would require a more specific query in the repository
        List<SalesTransaction> recentTransactions = salesTransactionRepository
            .findByShopAndDateRange(
                transaction.getShop().getId(),
                windowStart,
                transaction.getTransactionDate()
            );

        long sameUserTransactions = recentTransactions.stream()
            .filter(t -> t.getCashier().getId().equals(transaction.getCashier().getId()))
            .count();

        return sameUserTransactions > rule.getThresholdCount();
    }

    private boolean evaluateVelocityRule(FraudRule rule, SalesTransaction transaction) {
        if (rule.getThresholdAmount() == null || rule.getTimeWindowMinutes() == null) {
            return false;
        }

        LocalDateTime windowStart = transaction.getTransactionDate()
            .minusMinutes(rule.getTimeWindowMinutes());

        List<SalesTransaction> largeTransactions = salesTransactionRepository
            .findLargeTransactionsSince(
                transaction.getShop().getId(),
                rule.getThresholdAmount(),
                windowStart
            );

        // Check if current transaction plus recent large transactions exceed velocity threshold
        BigDecimal totalAmount = largeTransactions.stream()
            .map(SalesTransaction::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(transaction.getTotalAmount());

        // If total amount in time window exceeds 5x threshold, flag as suspicious
        return totalAmount.compareTo(rule.getThresholdAmount().multiply(BigDecimal.valueOf(5))) > 0;
    }

    private RiskAssessment.RiskLevel determineRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return RiskAssessment.RiskLevel.CRITICAL;
        } else if (riskScore.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return RiskAssessment.RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(20)) >= 0) {
            return RiskAssessment.RiskLevel.MEDIUM;
        } else {
            return RiskAssessment.RiskLevel.LOW;
        }
    }

    private void blockTransaction(SalesTransaction transaction, RiskAssessment assessment) {
        transaction.setStatus(SalesTransaction.TransactionStatus.CANCELLED);
        transaction.setVoidReason("Automatically blocked due to fraud risk assessment");
        transaction.setVoidedBy("FRAUD_DETECTION_SYSTEM");
        transaction.setVoidedAt(LocalDateTime.now());
        salesTransactionRepository.save(transaction);

        assessment.setResolutionAction(RiskAssessment.ResolutionAction.BLOCK_TRANSACTION);
        riskAssessmentRepository.save(assessment);

        log.warn("Transaction {} automatically blocked due to fraud risk", transaction.getTransactionNumber());

        auditService.logSecurityEvent(
            transaction.getShop(),
            "SYSTEM",
            "fraud-detection",
            AuditLog.ActionType.CANCEL,
            String.format("Transaction %s automatically blocked due to fraud risk",
                transaction.getTransactionNumber()),
            null,
            true
        );
    }

    public List<RiskAssessment> getHighRiskAssessments() {
        return riskAssessmentRepository.findByRiskLevelAndStatus(
            RiskAssessment.RiskLevel.HIGH,
            RiskAssessment.AssessmentStatus.PENDING
        );
    }

    public List<SalesTransaction> getHighRiskTransactions() {
        return salesTransactionRepository.findHighRiskTransactions();
    }

    public List<FraudAlert> getActiveFraudAlerts() {
        return fraudAlertRepository.findByTenantIdAndStatusIn(
            com.princely.shopmanager.auth.context.TenantContext.getCurrentTenant(),
            List.of(FraudAlert.AlertStatus.ACTIVE, FraudAlert.AlertStatus.ACKNOWLEDGED)
        );
    }

    @Transactional
    public void approveRiskAssessment(String assessmentId, String reviewedBy, String reviewNotes) {
        RiskAssessment assessment = riskAssessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Risk assessment not found: " + assessmentId));

        assessment.approve(reviewedBy, reviewNotes);
        riskAssessmentRepository.save(assessment);

        log.info("Risk assessment {} approved by {}", assessmentId, reviewedBy);
    }

    @Transactional
    public void rejectRiskAssessment(String assessmentId, String reviewedBy, String reviewNotes,
                                   RiskAssessment.ResolutionAction action) {
        RiskAssessment assessment = riskAssessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new IllegalArgumentException("Risk assessment not found: " + assessmentId));

        assessment.reject(reviewedBy, reviewNotes, action);
        riskAssessmentRepository.save(assessment);

        log.info("Risk assessment {} rejected by {} with action {}", assessmentId, reviewedBy, action);
    }

    @Transactional
    public void acknowledgeFraudAlert(String alertId, String acknowledgedBy) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Fraud alert not found: " + alertId));

        alert.acknowledge(acknowledgedBy);
        fraudAlertRepository.save(alert);

        log.info("Fraud alert {} acknowledged by {}", alertId, acknowledgedBy);
    }

    @Transactional
    public void resolveFraudAlert(String alertId, String resolvedBy, String resolutionNotes) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Fraud alert not found: " + alertId));

        alert.resolve(resolvedBy, resolutionNotes);
        fraudAlertRepository.save(alert);

        log.info("Fraud alert {} resolved by {}", alertId, resolvedBy);
    }

    private String getCurrentTenantId() {
        try {
            return com.princely.shopmanager.auth.context.TenantContext.getCurrentTenant();
        } catch (Exception e) {
            log.warn("Unable to get current tenant, using default: {}", e.getMessage());
            return "default-tenant";
        }
    }

    private record FraudAssessmentResult(
        BigDecimal riskScore,
        RiskAssessment.RiskLevel riskLevel,
        List<String> flags,
        String details,
        boolean shouldAutoBlock,
        List<String> triggeredRules
    ) {}
}