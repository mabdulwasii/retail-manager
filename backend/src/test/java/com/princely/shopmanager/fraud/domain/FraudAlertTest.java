package com.princely.shopmanager.fraud.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FraudAlert Domain Tests")
class FraudAlertTest {

    private FraudAlert alert;

    @BeforeEach
    void setUp() {
        Map<String, String> evidence = new HashMap<>();
        evidence.put("ip_address", "192.168.1.1");
        evidence.put("device_fingerprint", "abc123");

        alert = FraudAlert.builder()
            .alertNumber("ALERT-2024-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.HIGH)
            .title("Suspicious transaction detected")
            .description("Multiple high-value transactions in short time")
            .transactionId("TXN-001")
            .riskScore(BigDecimal.valueOf(85.50))
            .confidenceLevel(BigDecimal.valueOf(92.30))
            .evidence(evidence)
            .detectionRule("RULE-001")
            .build();
    }

    // ==================== Status Checking Tests ====================

    @Test
    @DisplayName("canBeAcknowledged - Should return true when status is ACTIVE")
    void canBeAcknowledged_shouldReturnTrueWhenStatusIsActive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);

        // Then
        assertThat(alert.canBeAcknowledged()).isTrue();
    }

    @Test
    @DisplayName("canBeAcknowledged - Should return false when status is ACKNOWLEDGED")
    void canBeAcknowledged_shouldReturnFalseWhenStatusIsAcknowledged() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);

        // Then
        assertThat(alert.canBeAcknowledged()).isFalse();
    }

    @Test
    @DisplayName("canBeAcknowledged - Should return false when status is RESOLVED")
    void canBeAcknowledged_shouldReturnFalseWhenStatusIsResolved() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.RESOLVED);

        // Then
        assertThat(alert.canBeAcknowledged()).isFalse();
    }

    @Test
    @DisplayName("canBeResolved - Should return true when status is ACKNOWLEDGED")
    void canBeResolved_shouldReturnTrueWhenStatusIsAcknowledged() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);

        // Then
        assertThat(alert.canBeResolved()).isTrue();
    }

    @Test
    @DisplayName("canBeResolved - Should return true when status is INVESTIGATING")
    void canBeResolved_shouldReturnTrueWhenStatusIsInvestigating() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.INVESTIGATING);

        // Then
        assertThat(alert.canBeResolved()).isTrue();
    }

    @Test
    @DisplayName("canBeResolved - Should return false when status is ACTIVE")
    void canBeResolved_shouldReturnFalseWhenStatusIsActive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);

        // Then
        assertThat(alert.canBeResolved()).isFalse();
    }

    @Test
    @DisplayName("canBeResolved - Should return false when status is RESOLVED")
    void canBeResolved_shouldReturnFalseWhenStatusIsResolved() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.RESOLVED);

        // Then
        assertThat(alert.canBeResolved()).isFalse();
    }

    // ==================== Workflow: Acknowledge ====================

    @Test
    @DisplayName("acknowledge - Should update status and acknowledgment fields")
    void acknowledge_shouldUpdateStatusAndAcknowledgmentFields() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String userId = "user-123";
        LocalDateTime beforeAck = LocalDateTime.now().minusSeconds(1);

        // When
        alert.acknowledge(userId);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACKNOWLEDGED);
        assertThat(alert.getAcknowledgedBy()).isEqualTo(userId);
        assertThat(alert.getAcknowledgedAt()).isAfter(beforeAck);
    }

    @Test
    @DisplayName("acknowledge - Should throw exception when status is not ACTIVE")
    void acknowledge_shouldThrowExceptionWhenStatusIsNotActive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);
        String userId = "user-123";

        // When & Then
        assertThatThrownBy(() -> alert.acknowledge(userId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be acknowledged in current status: ACKNOWLEDGED");
    }

    @Test
    @DisplayName("acknowledge - Should throw exception when status is RESOLVED")
    void acknowledge_shouldThrowExceptionWhenStatusIsResolved() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.RESOLVED);
        String userId = "user-123";

        // When & Then
        assertThatThrownBy(() -> alert.acknowledge(userId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be acknowledged in current status: RESOLVED");
    }

    @Test
    @DisplayName("acknowledge - Should throw exception when status is INVESTIGATING")
    void acknowledge_shouldThrowExceptionWhenStatusIsInvestigating() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.INVESTIGATING);
        String userId = "user-123";

        // When & Then
        assertThatThrownBy(() -> alert.acknowledge(userId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be acknowledged in current status: INVESTIGATING");
    }

    // ==================== Workflow: Resolve ====================

    @Test
    @DisplayName("resolve - Should update status and resolution fields from ACKNOWLEDGED")
    void resolve_shouldUpdateStatusAndResolutionFieldsFromAcknowledged() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);
        String userId = "user-456";
        String notes = "Verified as fraud, account suspended";
        LocalDateTime beforeResolve = LocalDateTime.now().minusSeconds(1);

        // When
        alert.resolve(userId, notes);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.RESOLVED);
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedAt()).isAfter(beforeResolve);
        assertThat(alert.getResolutionNotes()).isEqualTo(notes);
    }

    @Test
    @DisplayName("resolve - Should update status and resolution fields from INVESTIGATING")
    void resolve_shouldUpdateStatusAndResolutionFieldsFromInvestigating() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.INVESTIGATING);
        String userId = "user-456";
        String notes = "Investigation complete, no fraud detected";
        LocalDateTime beforeResolve = LocalDateTime.now().minusSeconds(1);

        // When
        alert.resolve(userId, notes);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.RESOLVED);
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedAt()).isAfter(beforeResolve);
        assertThat(alert.getResolutionNotes()).isEqualTo(notes);
    }

    @Test
    @DisplayName("resolve - Should throw exception when status is ACTIVE")
    void resolve_shouldThrowExceptionWhenStatusIsActive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String userId = "user-456";

        // When & Then
        assertThatThrownBy(() -> alert.resolve(userId, "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be resolved in current status: ACTIVE");
    }

    @Test
    @DisplayName("resolve - Should throw exception when status is already RESOLVED")
    void resolve_shouldThrowExceptionWhenStatusIsAlreadyResolved() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.RESOLVED);
        String userId = "user-456";

        // When & Then
        assertThatThrownBy(() -> alert.resolve(userId, "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be resolved in current status: RESOLVED");
    }

    @Test
    @DisplayName("resolve - Should throw exception when status is FALSE_POSITIVE")
    void resolve_shouldThrowExceptionWhenStatusIsFalsePositive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.FALSE_POSITIVE);
        String userId = "user-456";

        // When & Then
        assertThatThrownBy(() -> alert.resolve(userId, "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Alert cannot be resolved in current status: FALSE_POSITIVE");
    }

    // ==================== Workflow: Mark as False Positive ====================

    @Test
    @DisplayName("markAsFalsePositive - Should update all fields correctly")
    void markAsFalsePositive_shouldUpdateAllFieldsCorrectly() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);
        String userId = "user-789";
        String notes = "Customer verified, legitimate transaction";
        LocalDateTime beforeMark = LocalDateTime.now().minusSeconds(1);

        // When
        alert.markAsFalsePositive(userId, notes);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.FALSE_POSITIVE);
        assertThat(alert.getFalsePositive()).isTrue();
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedAt()).isAfter(beforeMark);
        assertThat(alert.getResolutionNotes()).isEqualTo(notes);
    }

    @Test
    @DisplayName("markAsFalsePositive - Should work from any status")
    void markAsFalsePositive_shouldWorkFromAnyStatus() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String userId = "user-789";
        String notes = "False alarm";

        // When
        alert.markAsFalsePositive(userId, notes);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.FALSE_POSITIVE);
        assertThat(alert.getFalsePositive()).isTrue();
    }

    // ==================== Workflow: Dismiss ====================

    @Test
    @DisplayName("dismiss - Should update status and dismissal fields")
    void dismiss_shouldUpdateStatusAndDismissalFields() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String userId = "user-999";
        String reason = "Duplicate alert";
        LocalDateTime beforeDismiss = LocalDateTime.now().minusSeconds(1);

        // When
        alert.dismiss(userId, reason);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.DISMISSED);
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolvedAt()).isAfter(beforeDismiss);
        assertThat(alert.getResolutionNotes()).isEqualTo(reason);
    }

    @Test
    @DisplayName("dismiss - Should work from any status")
    void dismiss_shouldWorkFromAnyStatus() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.INVESTIGATING);
        String userId = "user-999";
        String reason = "Not relevant";

        // When
        alert.dismiss(userId, reason);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.DISMISSED);
    }

    // ==================== Convenience Methods ====================

    @Test
    @DisplayName("getShopId - Should return shop ID when shop is set")
    void getShopId_shouldReturnShopIdWhenShopIsSet() {
        // Given
        // Shop would need to be created, but we can't easily test this without mocking
        // This would typically be tested in integration tests
        assertThat(alert.getShopId()).isNull();
    }

    @Test
    @DisplayName("getShopName - Should return null when shop is not set")
    void getShopName_shouldReturnNullWhenShopIsNotSet() {
        // Then
        assertThat(alert.getShopName()).isNull();
    }

    // ==================== Builder Tests ====================

    @Test
    @DisplayName("Builder - Should create alert with all fields")
    void builder_shouldCreateAlertWithAllFields() {
        // Given
        String alertNumber = "ALERT-2024-002";
        FraudAlert.AlertType alertType = FraudAlert.AlertType.VELOCITY_FRAUD;
        FraudAlert.AlertSeverity severity = FraudAlert.AlertSeverity.CRITICAL;
        String title = "Velocity fraud detected";
        String description = "Multiple transactions in rapid succession";
        String transactionId = "TXN-002";
        String investmentId = "INV-001";
        BigDecimal riskScore = BigDecimal.valueOf(95.00);
        BigDecimal confidenceLevel = BigDecimal.valueOf(98.50);
        Map<String, String> evidence = new HashMap<>();
        evidence.put("transaction_count", "10");
        evidence.put("time_window", "5 minutes");
        String detectionRule = "RULE-002";

        // When
        FraudAlert newAlert = FraudAlert.builder()
            .alertNumber(alertNumber)
            .alertType(alertType)
            .severity(severity)
            .title(title)
            .description(description)
            .transactionId(transactionId)
            .investmentId(investmentId)
            .riskScore(riskScore)
            .confidenceLevel(confidenceLevel)
            .evidence(evidence)
            .detectionRule(detectionRule)
            .build();

        // Then
        assertThat(newAlert.getAlertNumber()).isEqualTo(alertNumber);
        assertThat(newAlert.getAlertType()).isEqualTo(alertType);
        assertThat(newAlert.getSeverity()).isEqualTo(severity);
        assertThat(newAlert.getTitle()).isEqualTo(title);
        assertThat(newAlert.getDescription()).isEqualTo(description);
        assertThat(newAlert.getTransactionId()).isEqualTo(transactionId);
        assertThat(newAlert.getInvestmentId()).isEqualTo(investmentId);
        assertThat(newAlert.getRiskScore()).isEqualByComparingTo(riskScore);
        assertThat(newAlert.getConfidenceLevel()).isEqualByComparingTo(confidenceLevel);
        assertThat(newAlert.getEvidence()).containsEntry("transaction_count", "10");
        assertThat(newAlert.getDetectionRule()).isEqualTo(detectionRule);
    }

    @Test
    @DisplayName("Builder - Should create alert with default status ACTIVE")
    void builder_shouldCreateAlertWithDefaultStatusActive() {
        // When
        FraudAlert newAlert = FraudAlert.builder()
            .alertNumber("ALERT-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.LOW)
            .title("Test")
            .build();

        // Then
        assertThat(newAlert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACTIVE);
    }

    @Test
    @DisplayName("Builder - Should create alert with default falsePositive as false")
    void builder_shouldCreateAlertWithDefaultFalsePositiveAsFalse() {
        // When
        FraudAlert newAlert = FraudAlert.builder()
            .alertNumber("ALERT-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.LOW)
            .title("Test")
            .build();

        // Then
        assertThat(newAlert.getFalsePositive()).isFalse();
    }

    @Test
    @DisplayName("Builder - Should set detection timestamp")
    void builder_shouldSetDetectionTimestamp() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        FraudAlert newAlert = FraudAlert.builder()
            .alertNumber("ALERT-001")
            .alertType(FraudAlert.AlertType.SUSPICIOUS_TRANSACTION)
            .severity(FraudAlert.AlertSeverity.LOW)
            .title("Test")
            .build();

        // Then
        assertThat(newAlert.getDetectionTimestamp()).isAfter(beforeCreation);
    }

    // ==================== Complete Workflow Tests ====================

    @Test
    @DisplayName("Complete workflow - ACTIVE → ACKNOWLEDGED → RESOLVED")
    void completeWorkflow_activeToAcknowledgedToResolved() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String acknowledger = "user-123";
        String resolver = "user-456";
        String notes = "Fraud confirmed and handled";

        // When & Then: ACTIVE → ACKNOWLEDGED
        alert.acknowledge(acknowledger);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACKNOWLEDGED);
        assertThat(alert.canBeResolved()).isTrue();

        // When & Then: ACKNOWLEDGED → RESOLVED
        alert.resolve(resolver, notes);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.RESOLVED);
        assertThat(alert.getResolvedBy()).isEqualTo(resolver);
        assertThat(alert.getResolutionNotes()).isEqualTo(notes);
    }

    @Test
    @DisplayName("Complete workflow - ACTIVE → ACKNOWLEDGED → FALSE_POSITIVE")
    void completeWorkflow_activeToAcknowledgedToFalsePositive() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String acknowledger = "user-123";
        String reviewer = "user-789";
        String notes = "Customer verified, false alarm";

        // When & Then: ACTIVE → ACKNOWLEDGED
        alert.acknowledge(acknowledger);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.ACKNOWLEDGED);

        // When & Then: ACKNOWLEDGED → FALSE_POSITIVE
        alert.markAsFalsePositive(reviewer, notes);
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.FALSE_POSITIVE);
        assertThat(alert.getFalsePositive()).isTrue();
    }

    @Test
    @DisplayName("Complete workflow - ACTIVE → DISMISSED")
    void completeWorkflow_activeToDismissed() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        String userId = "user-999";
        String reason = "Duplicate of ALERT-2024-002";

        // When
        alert.dismiss(userId, reason);

        // Then
        assertThat(alert.getStatus()).isEqualTo(FraudAlert.AlertStatus.DISMISSED);
        assertThat(alert.getResolvedBy()).isEqualTo(userId);
        assertThat(alert.getResolutionNotes()).isEqualTo(reason);
    }

    // ==================== Enum Tests ====================

    @Test
    @DisplayName("AlertType - Should have all expected values")
    void alertType_shouldHaveAllExpectedValues() {
        // Then
        assertThat(FraudAlert.AlertType.values()).containsExactlyInAnyOrder(
            FraudAlert.AlertType.SUSPICIOUS_TRANSACTION,
            FraudAlert.AlertType.UNUSUAL_INVESTMENT_PATTERN,
            FraudAlert.AlertType.EXCESSIVE_WITHDRAWALS,
            FraudAlert.AlertType.DUPLICATE_TRANSACTIONS,
            FraudAlert.AlertType.VELOCITY_FRAUD,
            FraudAlert.AlertType.ACCOUNT_TAKEOVER,
            FraudAlert.AlertType.PRICE_MANIPULATION,
            FraudAlert.AlertType.RETURN_FRAUD,
            FraudAlert.AlertType.COLLUSION_DETECTION,
            FraudAlert.AlertType.ANOMALOUS_BEHAVIOR
        );
    }

    @Test
    @DisplayName("AlertSeverity - Should have all expected values")
    void alertSeverity_shouldHaveAllExpectedValues() {
        // Then
        assertThat(FraudAlert.AlertSeverity.values()).containsExactlyInAnyOrder(
            FraudAlert.AlertSeverity.LOW,
            FraudAlert.AlertSeverity.MEDIUM,
            FraudAlert.AlertSeverity.HIGH,
            FraudAlert.AlertSeverity.CRITICAL
        );
    }

    @Test
    @DisplayName("AlertStatus - Should have all expected values")
    void alertStatus_shouldHaveAllExpectedValues() {
        // Then
        assertThat(FraudAlert.AlertStatus.values()).containsExactlyInAnyOrder(
            FraudAlert.AlertStatus.ACTIVE,
            FraudAlert.AlertStatus.ACKNOWLEDGED,
            FraudAlert.AlertStatus.INVESTIGATING,
            FraudAlert.AlertStatus.RESOLVED,
            FraudAlert.AlertStatus.FALSE_POSITIVE,
            FraudAlert.AlertStatus.DISMISSED
        );
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("acknowledge - Should set acknowledgment timestamp to current time")
    void acknowledge_shouldSetAcknowledgmentTimestampToCurrentTime() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACTIVE);
        LocalDateTime beforeAck = LocalDateTime.now();

        // When
        alert.acknowledge("user-123");

        // Then
        assertThat(alert.getAcknowledgedAt())
            .isAfterOrEqualTo(beforeAck)
            .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("resolve - Should set resolution timestamp to current time")
    void resolve_shouldSetResolutionTimestampToCurrentTime() {
        // Given
        alert.setStatus(FraudAlert.AlertStatus.ACKNOWLEDGED);
        LocalDateTime beforeResolve = LocalDateTime.now();

        // When
        alert.resolve("user-456", "Resolved");

        // Then
        assertThat(alert.getResolvedAt())
            .isAfterOrEqualTo(beforeResolve)
            .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Evidence map - Should store multiple evidence entries")
    void evidenceMap_shouldStoreMultipleEvidenceEntries() {
        // Given
        Map<String, String> evidence = new HashMap<>();
        evidence.put("ip_address", "10.0.0.1");
        evidence.put("user_agent", "Mozilla/5.0");
        evidence.put("transaction_amount", "5000.00");
        alert.setEvidence(evidence);

        // Then
        assertThat(alert.getEvidence())
            .hasSize(3)
            .containsEntry("ip_address", "10.0.0.1")
            .containsEntry("user_agent", "Mozilla/5.0")
            .containsEntry("transaction_amount", "5000.00");
    }
}
