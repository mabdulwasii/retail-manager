package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.dto.FraudAlertResponse;
import com.princely.shopmanager.fraud.dto.FraudRuleRequest;
import com.princely.shopmanager.fraud.dto.RiskAssessmentResponse;
import com.princely.shopmanager.fraud.repository.FraudAlertRepository;
import com.princely.shopmanager.fraud.repository.FraudRuleRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.fraud.event.FraudAlertCreatedEvent;
import com.princely.shopmanager.fraud.event.RiskAssessmentCreatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FraudManagementService {

    private final FraudAlertRepository fraudAlertRepository;
    private final FraudRuleRepository fraudRuleRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Page<FraudAlertResponse> getFraudAlerts(
            String shopId,
            FraudAlert.AlertStatus status,
            FraudAlert.AlertSeverity severity,
            FraudAlert.AlertType alertType,
            Pageable pageable) {

        String tenantId = TenantContext.getCurrentTenant();
        Page<FraudAlert> alerts;

        // Apply filters
        if (shopId != null && status != null && severity != null && alertType != null) {
            alerts = fraudAlertRepository.findByTenantIdAndStatusIn(tenantId, List.of(status))
                .stream()
                .filter(alert -> alert.getShop() != null && alert.getShop().getId().equals(shopId))
                .filter(alert -> alert.getSeverity().equals(severity))
                .filter(alert -> alert.getAlertType().equals(alertType))
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new PageImpl<>(
                        list.subList(
                            Math.min((int) pageable.getOffset(), list.size()),
                            Math.min((int) pageable.getOffset() + pageable.getPageSize(), list.size())
                        ),
                        pageable,
                        list.size()
                    )
                ));
        } else if (status != null) {
            alerts = fraudAlertRepository.findByTenantIdAndStatusOrderByDetectionTimestampDesc(tenantId, status, pageable);
        } else if (severity != null) {
            alerts = fraudAlertRepository.findByTenantIdAndSeverityOrderByDetectionTimestampDesc(tenantId, severity, pageable);
        } else if (alertType != null) {
            alerts = fraudAlertRepository.findByTenantIdAndAlertTypeOrderByDetectionTimestampDesc(tenantId, alertType, pageable);
        } else if (shopId != null) {
            alerts = fraudAlertRepository.findByShopIdOrderByDetectionTimestampDesc(shopId, pageable);
        } else {
            alerts = fraudAlertRepository.findByTenantIdOrderByDetectionTimestampDesc(tenantId, pageable);
        }

        return alerts.map(this::mapToAlertResponse);
    }

    @Transactional(readOnly = true)
    public FraudAlertResponse getFraudAlertById(String alertId) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new EntityNotFoundException("Fraud alert not found: " + alertId));
        return mapToAlertResponse(alert);
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentResponse> getRiskAssessments(
            String shopId,
            RiskAssessment.RiskLevel riskLevel,
            RiskAssessment.AssessmentStatus status,
            RiskAssessment.AssessmentType assessmentType,
            Pageable pageable) {

        Page<RiskAssessment> assessments;

        if (shopId != null) {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + shopId));

            if (status != null) {
                assessments = riskAssessmentRepository.findByShopAndStatusIn(shop, List.of(status), pageable);
            } else {
                assessments = riskAssessmentRepository.findByShopOrderByAssessmentDateDesc(shop, pageable);
            }
        } else if (riskLevel != null && status != null) {
            assessments = riskAssessmentRepository.findByRiskLevelInAndStatus(List.of(riskLevel), status)
                .stream()
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new PageImpl<>(
                        list.subList(
                            Math.min((int) pageable.getOffset(), list.size()),
                            Math.min((int) pageable.getOffset() + pageable.getPageSize(), list.size())
                        ),
                        pageable,
                        list.size()
                    )
                ));
        } else {
            assessments = riskAssessmentRepository.findAll(pageable);
        }

        return assessments.map(this::mapToAssessmentResponse);
    }

    @Transactional(readOnly = true)
    public RiskAssessmentResponse getRiskAssessmentById(String assessmentId) {
        RiskAssessment assessment = riskAssessmentRepository.findById(assessmentId)
            .orElseThrow(() -> new EntityNotFoundException("Risk assessment not found: " + assessmentId));
        return mapToAssessmentResponse(assessment);
    }

    @Transactional(readOnly = true)
    public Page<FraudRule> getFraudRules(
            String shopId,
            FraudRule.FraudRuleType ruleType,
            Boolean enabled,
            Pageable pageable) {

        if (shopId != null) {
            Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + shopId));

            if (enabled != null) {
                return fraudRuleRepository.findByShopAndEnabled(shop, enabled)
                    .stream()
                    .filter(rule -> ruleType == null || rule.getRuleType().equals(ruleType))
                    .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new PageImpl<>(
                            list.subList(
                                Math.min((int) pageable.getOffset(), list.size()),
                                Math.min((int) pageable.getOffset() + pageable.getPageSize(), list.size())
                            ),
                            pageable,
                            list.size()
                        )
                    ));
            }
        }

        if (enabled != null) {
            return fraudRuleRepository.findByEnabledOrderByRuleName(enabled, pageable);
        }

        return fraudRuleRepository.findAll(pageable);
    }

    public FraudRule createFraudRule(FraudRuleRequest request) {
        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + request.getShopId()));
        }

        // Check for duplicate rule names within the same scope
        if (fraudRuleRepository.existsByRuleNameAndShop(request.getRuleName(), shop)) {
            throw new IllegalArgumentException("Fraud rule with this name already exists for the specified scope");
        }

        FraudRule rule = FraudRule.builder()
            .shop(shop)
            .ruleName(request.getRuleName())
            .ruleType(request.getRuleType())
            .description(request.getDescription())
            .enabled(request.getEnabled())
            .thresholdAmount(request.getThresholdAmount())
            .thresholdCount(request.getThresholdCount())
            .timeWindowMinutes(request.getTimeWindowMinutes())
            .riskScoreWeight(request.getRiskScoreWeight())
            .severity(request.getSeverity())
            .autoBlock(request.getAutoBlock())
            .requiresManualReview(request.getRequiresManualReview())
            .ruleConfiguration(request.getRuleConfiguration())
            .build();

        rule = fraudRuleRepository.save(rule);

        auditService.logEntityCreation("FraudRule", rule.getId(),
            String.format("Created fraud rule '%s' of type %s", rule.getRuleName(), rule.getRuleType()));

        log.info("Fraud rule created: {} ({})", rule.getRuleName(), rule.getId());
        return rule;
    }

    public FraudRule updateFraudRule(String ruleId, FraudRuleRequest request) {
        FraudRule rule = fraudRuleRepository.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Fraud rule not found: " + ruleId));

        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + request.getShopId()));
        }

        // Check for duplicate rule names (excluding current rule)
        if (!rule.getRuleName().equals(request.getRuleName()) &&
            fraudRuleRepository.existsByRuleNameAndShop(request.getRuleName(), shop)) {
            throw new IllegalArgumentException("Fraud rule with this name already exists for the specified scope");
        }

        rule.setShop(shop);
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setDescription(request.getDescription());
        rule.setEnabled(request.getEnabled());
        rule.setThresholdAmount(request.getThresholdAmount());
        rule.setThresholdCount(request.getThresholdCount());
        rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        rule.setRiskScoreWeight(request.getRiskScoreWeight());
        rule.setSeverity(request.getSeverity());
        rule.setAutoBlock(request.getAutoBlock());
        rule.setRequiresManualReview(request.getRequiresManualReview());
        rule.setRuleConfiguration(request.getRuleConfiguration());

        rule = fraudRuleRepository.save(rule);

        auditService.logEntityModification("FraudRule", rule.getId(),
            String.format("Updated fraud rule '%s'", rule.getRuleName()));

        log.info("Fraud rule updated: {} ({})", rule.getRuleName(), rule.getId());
        return rule;
    }

    public void deleteFraudRule(String ruleId) {
        FraudRule rule = fraudRuleRepository.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Fraud rule not found: " + ruleId));

        fraudRuleRepository.delete(rule);

        auditService.logEntityDeletion("FraudRule", rule.getId(),
            String.format("Deleted fraud rule '%s'", rule.getRuleName()));

        log.info("Fraud rule deleted: {} ({})", rule.getRuleName(), rule.getId());
    }

    public FraudRule updateRuleStatus(String ruleId, boolean enabled) {
        FraudRule rule = fraudRuleRepository.findById(ruleId)
            .orElseThrow(() -> new EntityNotFoundException("Fraud rule not found: " + ruleId));

        rule.setEnabled(enabled);
        rule = fraudRuleRepository.save(rule);

        auditService.logEntityModification("FraudRule", rule.getId(),
            String.format("Fraud rule '%s' %s", rule.getRuleName(), enabled ? "enabled" : "disabled"));

        log.info("Fraud rule status updated: {} - enabled: {}", rule.getRuleName(), enabled);
        return rule;
    }

    public void markAlertAsFalsePositive(String alertId, String userId, String reason) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
            .orElseThrow(() -> new EntityNotFoundException("Fraud alert not found: " + alertId));

        alert.markAsFalsePositive(userId, reason);
        fraudAlertRepository.save(alert);

        // Update rule false positive count if applicable
        if (alert.getDetectionRule() != null) {
            String[] ruleNames = alert.getDetectionRule().split(",");
            for (String ruleName : ruleNames) {
                List<FraudRule> rules = fraudRuleRepository.findAllApplicableRules(alert.getShop());
                rules.stream()
                    .filter(rule -> rule.getRuleName().equals(ruleName.trim()))
                    .findFirst()
                    .ifPresent(rule -> {
                        // This would require adding false positive tracking to FraudRule
                        // For now, we'll just log it
                        log.info("False positive recorded for rule: {}", ruleName);
                    });
            }
        }

        auditService.logEntityModification("FraudAlert", alert.getId(),
            String.format("Alert %s marked as false positive: %s", alert.getAlertNumber(), reason));

        log.info("Fraud alert marked as false positive: {} by user: {}", alert.getAlertNumber(), userId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFraudStatistics(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();
        String tenantId = TenantContext.getCurrentTenant();

        // Set default date range if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // Alert statistics
        long totalAlerts = fraudAlertRepository.countByTenantIdAndStatus(tenantId, FraudAlert.AlertStatus.ACTIVE);
        long highSeverityAlerts = fraudAlertRepository.countByTenantIdAndSeverityAndStatusIn(
            tenantId,
            FraudAlert.AlertSeverity.HIGH,
            List.of(FraudAlert.AlertStatus.ACTIVE, FraudAlert.AlertStatus.ACKNOWLEDGED)
        );
        long criticalAlerts = fraudAlertRepository.countByTenantIdAndSeverityAndStatusIn(
            tenantId,
            FraudAlert.AlertSeverity.CRITICAL,
            List.of(FraudAlert.AlertStatus.ACTIVE, FraudAlert.AlertStatus.ACKNOWLEDGED)
        );

        // Risk assessment statistics
        long pendingAssessments = riskAssessmentRepository.countByStatus(RiskAssessment.AssessmentStatus.PENDING);
        long highRiskAssessments = riskAssessmentRepository.countByStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);

        // Rule statistics
        long totalRules = fraudRuleRepository.countEnabledRules();
        List<Object[]> rulesByType = fraudRuleRepository.countEnabledRulesByType();

        // Time-based statistics
        List<Object[]> alertsByType = fraudAlertRepository.countByAlertTypeSince(tenantId, startDate);
        List<Object[]> riskLevelCounts = riskAssessmentRepository.countByRiskLevelSince(startDate);

        stats.put("alerts", Map.of(
            "total", totalAlerts,
            "highSeverity", highSeverityAlerts,
            "critical", criticalAlerts,
            "byType", alertsByType
        ));

        stats.put("riskAssessments", Map.of(
            "pending", pendingAssessments,
            "underReview", highRiskAssessments,
            "byRiskLevel", riskLevelCounts
        ));

        stats.put("rules", Map.of(
            "total", totalRules,
            "byType", rulesByType
        ));

        stats.put("dateRange", Map.of(
            "startDate", startDate,
            "endDate", endDate
        ));

        return stats;
    }

    private FraudAlertResponse mapToAlertResponse(FraudAlert alert) {
        return FraudAlertResponse.builder()
            .id(alert.getId())
            .alertNumber(alert.getAlertNumber())
            .alertType(alert.getAlertType())
            .severity(alert.getSeverity())
            .status(alert.getStatus())
            .title(alert.getTitle())
            .description(alert.getDescription())
            .shopId(alert.getShop() != null ? alert.getShop().getId() : null)
            .shopName(alert.getShop() != null ? alert.getShop().getName() : null)
            .userId(alert.getUser() != null ? alert.getUser().getId() : null)
            .userName(alert.getUser() != null ? alert.getUser().getName() : null)
            .transactionId(alert.getTransactionId())
            .investmentId(alert.getInvestmentId())
            .riskScore(alert.getRiskScore())
            .confidenceLevel(alert.getConfidenceLevel())
            .evidence(alert.getEvidence())
            .detectionRule(alert.getDetectionRule())
            .detectionTimestamp(alert.getDetectionTimestamp())
            .acknowledgedBy(alert.getAcknowledgedBy())
            .acknowledgedAt(alert.getAcknowledgedAt())
            .resolvedBy(alert.getResolvedBy())
            .resolvedAt(alert.getResolvedAt())
            .resolutionNotes(alert.getResolutionNotes())
            .falsePositive(alert.getFalsePositive())
            .createdAt(alert.getCreatedAt())
            .updatedAt(alert.getUpdatedAt())
            .build();
    }

    private RiskAssessmentResponse mapToAssessmentResponse(RiskAssessment assessment) {
        return RiskAssessmentResponse.builder()
            .id(assessment.getId())
            .shopId(assessment.getShop() != null ? assessment.getShop().getId() : null)
            .shopName(assessment.getShop() != null ? assessment.getShop().getName() : null)
            .transactionId(assessment.getTransaction() != null ? assessment.getTransaction().getId() : null)
            .transactionNumber(assessment.getTransaction() != null ? assessment.getTransaction().getTransactionNumber() : null)
            .assessmentType(assessment.getAssessmentType())
            .riskLevel(assessment.getRiskLevel())
            .riskScore(assessment.getRiskScore())
            .assessmentDate(assessment.getAssessmentDate())
            .flags(assessment.getFlags())
            .details(assessment.getDetails())
            .status(assessment.getStatus())
            .reviewedBy(assessment.getReviewedBy())
            .reviewedAt(assessment.getReviewedAt())
            .reviewNotes(assessment.getReviewNotes())
            .resolutionAction(assessment.getResolutionAction())
            .createdAt(assessment.getCreatedAt())
            .updatedAt(assessment.getUpdatedAt())
            .build();
    }
}