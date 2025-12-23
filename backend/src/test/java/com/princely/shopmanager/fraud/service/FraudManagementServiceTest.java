package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
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
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudManagementService Tests")
class FraudManagementServiceTest {

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FraudManagementService fraudManagementService;

    private Shop testShop;
    private Tenant testTenant;
    private Pageable pageable;

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

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should get fraud alerts with all filters")
    void shouldGetFraudAlertsWithAllFilters() {
        // Given
        String tenantId = "tenant-1";
        FraudAlert alert = createTestAlert();

        try (MockedStatic<com.princely.shopmanager.auth.context.TenantContext> mockedTenantContext =
                 mockStatic(com.princely.shopmanager.auth.context.TenantContext.class)) {

            mockedTenantContext.when(com.princely.shopmanager.auth.context.TenantContext::getCurrentTenant)
                .thenReturn(tenantId);

            when(fraudAlertRepository.findByTenantIdAndStatusIn(eq(tenantId), anyList()))
                .thenReturn(List.of(alert));

            // When
            Page<FraudAlertResponse> result = fraudManagementService.getFraudAlerts(
                "shop-1",
                FraudAlert.AlertStatus.ACTIVE,
                FraudAlert.AlertSeverity.HIGH,
                FraudAlert.AlertType.SUSPICIOUS_TRANSACTION,
                pageable
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getAlertNumber()).isEqualTo("ALERT-001");
        }
    }

    @Test
    @DisplayName("Should get fraud alerts by status only")
    void shouldGetFraudAlertsByStatus() {
        // Given
        String tenantId = "tenant-1";
        FraudAlert alert = createTestAlert();
        Page<FraudAlert> alertPage = new PageImpl<>(List.of(alert));

        try (MockedStatic<com.princely.shopmanager.auth.context.TenantContext> mockedTenantContext =
                 mockStatic(com.princely.shopmanager.auth.context.TenantContext.class)) {

            mockedTenantContext.when(com.princely.shopmanager.auth.context.TenantContext::getCurrentTenant)
                .thenReturn(tenantId);

            when(fraudAlertRepository.findByTenantIdAndStatusOrderByDetectionTimestampDesc(
                eq(tenantId),
                eq(FraudAlert.AlertStatus.ACTIVE),
                eq(pageable)
            )).thenReturn(alertPage);

            // When
            Page<FraudAlertResponse> result = fraudManagementService.getFraudAlerts(
                null, FraudAlert.AlertStatus.ACTIVE, null, null, pageable
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Test
    @DisplayName("Should get fraud alert by ID")
    void shouldGetFraudAlertById() {
        // Given
        FraudAlert alert = createTestAlert();
        when(fraudAlertRepository.findById("alert-1")).thenReturn(Optional.of(alert));

        // When
        FraudAlertResponse result = fraudManagementService.getFraudAlertById("alert-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("alert-1");
        assertThat(result.getAlertNumber()).isEqualTo("ALERT-001");
    }

    @Test
    @DisplayName("Should throw exception when fraud alert not found by ID")
    void shouldThrowExceptionWhenAlertNotFoundById() {
        // Given
        when(fraudAlertRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> fraudManagementService.getFraudAlertById("nonexistent"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Fraud alert not found");
    }

    @Test
    @DisplayName("Should get risk assessments by shop")
    void shouldGetRiskAssessmentsByShop() {
        // Given
        RiskAssessment assessment = createTestAssessment();
        Page<RiskAssessment> assessmentPage = new PageImpl<>(List.of(assessment));

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(riskAssessmentRepository.findByShopOrderByAssessmentDateDesc(testShop, pageable))
            .thenReturn(assessmentPage);

        // When
        Page<RiskAssessmentResponse> result = fraudManagementService.getRiskAssessments(
            "shop-1", null, null, null, pageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get risk assessments by risk level and status")
    void shouldGetRiskAssessmentsByRiskLevelAndStatus() {
        // Given
        RiskAssessment assessment = createTestAssessment();

        when(riskAssessmentRepository.findByRiskLevelInAndStatus(
            eq(List.of(RiskAssessment.RiskLevel.HIGH)),
            eq(RiskAssessment.AssessmentStatus.PENDING)
        )).thenReturn(List.of(assessment));

        // When
        Page<RiskAssessmentResponse> result = fraudManagementService.getRiskAssessments(
            null,
            RiskAssessment.RiskLevel.HIGH,
            RiskAssessment.AssessmentStatus.PENDING,
            null,
            pageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get risk assessment by ID")
    void shouldGetRiskAssessmentById() {
        // Given
        RiskAssessment assessment = createTestAssessment();
        when(riskAssessmentRepository.findById("assessment-1")).thenReturn(Optional.of(assessment));

        // When
        RiskAssessmentResponse result = fraudManagementService.getRiskAssessmentById("assessment-1");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("assessment-1");
        assertThat(result.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
    }

    @Test
    @DisplayName("Should create fraud rule successfully")
    void shouldCreateFraudRule() {
        // Given
        FraudRuleRequest request = new FraudRuleRequest();
        request.setShopId("shop-1");
        request.setRuleName("Test Rule");
        request.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        request.setDescription("Test fraud rule");
        request.setEnabled(true);
        request.setThresholdAmount(BigDecimal.valueOf(10000));
        request.setRiskScoreWeight(BigDecimal.valueOf(5.0));
        request.setSeverity(RiskAssessment.RiskLevel.HIGH);
        request.setAutoBlock(false);
        request.setRequiresManualReview(true);

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(fraudRuleRepository.existsByRuleNameAndShop("Test Rule", testShop)).thenReturn(false);
        when(fraudRuleRepository.save(any(FraudRule.class)))
            .thenAnswer(invocation -> {
                FraudRule rule = invocation.getArgument(0);
                rule.setId("rule-1");
                return rule;
            });

        // When
        FraudRule result = fraudManagementService.createFraudRule(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleName()).isEqualTo("Test Rule");
        assertThat(result.getRuleType()).isEqualTo(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        verify(auditService).logEntityCreation(eq("FraudRule"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should create global fraud rule when shopId is null")
    void shouldCreateGlobalFraudRule() {
        // Given
        FraudRuleRequest request = new FraudRuleRequest();
        request.setShopId(null);
        request.setRuleName("Global Rule");
        request.setRuleType(FraudRule.FraudRuleType.VELOCITY_CHECK);
        request.setEnabled(true);
        request.setRiskScoreWeight(BigDecimal.valueOf(6.0));

        when(fraudRuleRepository.existsByRuleNameAndShop("Global Rule", null)).thenReturn(false);
        when(fraudRuleRepository.save(any(FraudRule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FraudRule result = fraudManagementService.createFraudRule(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getShop()).isNull();
        assertThat(result.isGlobal()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when creating rule with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateRule() {
        // Given
        FraudRuleRequest request = new FraudRuleRequest();
        request.setShopId("shop-1");
        request.setRuleName("Duplicate Rule");
        request.setRuleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION);
        request.setEnabled(true);

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(fraudRuleRepository.existsByRuleNameAndShop("Duplicate Rule", testShop)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> fraudManagementService.createFraudRule(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should update fraud rule successfully")
    void shouldUpdateFraudRule() {
        // Given
        FraudRule existingRule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Old Name")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .shop(testShop)
            .build();

        FraudRuleRequest request = new FraudRuleRequest();
        request.setShopId("shop-1");
        request.setRuleName("Updated Rule");
        request.setRuleType(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        request.setEnabled(true);
        request.setThresholdCount(10);
        request.setTimeWindowMinutes(60);
        request.setRiskScoreWeight(BigDecimal.valueOf(4.0));

        when(fraudRuleRepository.findById("rule-1")).thenReturn(Optional.of(existingRule));
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(fraudRuleRepository.existsByRuleNameAndShop("Updated Rule", testShop)).thenReturn(false);
        when(fraudRuleRepository.save(any(FraudRule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FraudRule result = fraudManagementService.updateFraudRule("rule-1", request);

        // Then
        assertThat(result.getRuleName()).isEqualTo("Updated Rule");
        assertThat(result.getRuleType()).isEqualTo(FraudRule.FraudRuleType.HIGH_FREQUENCY_TRANSACTIONS);
        verify(auditService).logEntityModification(eq("FraudRule"), eq("rule-1"), anyString());
    }

    @Test
    @DisplayName("Should delete fraud rule successfully")
    void shouldDeleteFraudRule() {
        // Given
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Test Rule")
            .build();

        when(fraudRuleRepository.findById("rule-1")).thenReturn(Optional.of(rule));

        // When
        fraudManagementService.deleteFraudRule("rule-1");

        // Then
        verify(fraudRuleRepository).delete(rule);
        verify(auditService).logEntityDeletion(eq("FraudRule"), eq("rule-1"), anyString());
    }

    @Test
    @DisplayName("Should update rule status successfully")
    void shouldUpdateRuleStatus() {
        // Given
        FraudRule rule = FraudRule.builder()
            .id("rule-1")
            .ruleName("Test Rule")
            .enabled(true)
            .build();

        when(fraudRuleRepository.findById("rule-1")).thenReturn(Optional.of(rule));
        when(fraudRuleRepository.save(any(FraudRule.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FraudRule result = fraudManagementService.updateRuleStatus("rule-1", false);

        // Then
        assertThat(result.isEnabled()).isFalse();
        verify(auditService).logEntityModification(eq("FraudRule"), eq("rule-1"), contains("disabled"));
    }

    @Test
    @DisplayName("Should mark alert as false positive")
    void shouldMarkAlertAsFalsePositive() {
        // Given
        FraudAlert alert = FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALERT-001")
            .status(FraudAlert.AlertStatus.ACTIVE)
            .shop(testShop)
            .detectionRule("Rule1,Rule2")
            .build();

        when(fraudAlertRepository.findById("alert-1")).thenReturn(Optional.of(alert));
        when(fraudAlertRepository.save(any(FraudAlert.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudRuleRepository.findAllApplicableRules(testShop)).thenReturn(List.of());

        // When
        fraudManagementService.markAlertAsFalsePositive("alert-1", "admin", "Not actually fraud");

        // Then
        verify(fraudAlertRepository).save(alert);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.FALSE_POSITIVE);
        assertThat(alert.getFalsePositive()).isTrue();
        verify(auditService).logEntityModification(eq("FraudAlert"), eq("alert-1"), contains("false positive"));
    }

    @Test
    @DisplayName("Should get fraud statistics")
    void shouldGetFraudStatistics() {
        // Given
        String tenantId = "tenant-1";
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        try (MockedStatic<com.princely.shopmanager.auth.context.TenantContext> mockedTenantContext =
                 mockStatic(com.princely.shopmanager.auth.context.TenantContext.class)) {

            mockedTenantContext.when(com.princely.shopmanager.auth.context.TenantContext::getCurrentTenant)
                .thenReturn(tenantId);

            when(fraudAlertRepository.countByTenantIdAndStatus(tenantId, FraudAlert.AlertStatus.ACTIVE))
                .thenReturn(10L);
            when(fraudAlertRepository.countByTenantIdAndSeverityAndStatusIn(
                eq(tenantId),
                eq(FraudAlert.AlertSeverity.HIGH),
                anyList()
            )).thenReturn(5L);
            when(fraudAlertRepository.countByTenantIdAndSeverityAndStatusIn(
                eq(tenantId),
                eq(FraudAlert.AlertSeverity.CRITICAL),
                anyList()
            )).thenReturn(2L);
            when(riskAssessmentRepository.countByStatus(RiskAssessment.AssessmentStatus.PENDING))
                .thenReturn(8L);
            when(riskAssessmentRepository.countByStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW))
                .thenReturn(3L);
            when(fraudRuleRepository.countEnabledRules()).thenReturn(15L);
            when(fraudRuleRepository.countEnabledRulesByType()).thenReturn(List.of());
            when(fraudAlertRepository.countByAlertTypeSince(eq(tenantId), any(LocalDateTime.class)))
                .thenReturn(List.of());
            when(riskAssessmentRepository.countByRiskLevelSince(any(LocalDateTime.class)))
                .thenReturn(List.of());

            // When
            Map<String, Object> stats = fraudManagementService.getFraudStatistics("shop-1", startDate, endDate);

            // Then
            assertThat(stats).isNotNull();
            assertThat(stats).containsKeys("alerts", "riskAssessments", "rules", "dateRange");

            @SuppressWarnings("unchecked")
            Map<String, Object> alerts = (Map<String, Object>) stats.get("alerts");
            assertThat(alerts.get("total")).isEqualTo(10L);
            assertThat(alerts.get("highSeverity")).isEqualTo(5L);
            assertThat(alerts.get("critical")).isEqualTo(2L);

            @SuppressWarnings("unchecked")
            Map<String, Object> assessments = (Map<String, Object>) stats.get("riskAssessments");
            assertThat(assessments.get("pending")).isEqualTo(8L);
            assertThat(assessments.get("underReview")).isEqualTo(3L);

            @SuppressWarnings("unchecked")
            Map<String, Object> rules = (Map<String, Object>) stats.get("rules");
            assertThat(rules.get("total")).isEqualTo(15L);
        }
    }

    @Test
    @DisplayName("Should get fraud rules by shop and enabled status")
    void shouldGetFraudRulesByShopAndEnabled() {
        // Given
        FraudRule rule1 = FraudRule.builder()
            .id("rule-1")
            .ruleName("Rule 1")
            .ruleType(FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION)
            .enabled(true)
            .shop(testShop)
            .build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(fraudRuleRepository.findByShopAndEnabled(testShop, true))
            .thenReturn(List.of(rule1));

        // When
        Page<FraudRule> result = fraudManagementService.getFraudRules(
            "shop-1",
            FraudRule.FraudRuleType.HIGH_AMOUNT_TRANSACTION,
            true,
            pageable
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRuleName()).isEqualTo("Rule 1");
    }

    // Helper methods
    private FraudAlert createTestAlert() {
        return FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALERT-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.HIGH)
            .status(FraudAlert.AlertStatus.ACTIVE)
            .title("Test Alert")
            .description("Test alert description")
            .shop(testShop)
            .riskScore(BigDecimal.valueOf(75.50))
            .detectionTimestamp(LocalDateTime.now())
            .build();
    }

    private RiskAssessment createTestAssessment() {
        SalesTransaction transaction = SalesTransaction.builder()
            .id("txn-1")
            .transactionNumber("TXN-001")
            .build();

        return RiskAssessment.builder()
            .id("assessment-1")
            .shop(testShop)
            .transaction(transaction)
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .riskScore(BigDecimal.valueOf(70.00))
            .assessmentDate(LocalDateTime.now())
            .status(RiskAssessment.AssessmentStatus.PENDING)
            .build();
    }
}
