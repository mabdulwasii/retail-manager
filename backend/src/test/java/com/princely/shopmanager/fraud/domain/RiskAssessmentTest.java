package com.princely.shopmanager.fraud.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RiskAssessment Domain Tests")
class RiskAssessmentTest {

    private RiskAssessment assessment;

    @BeforeEach
    void setUp() {
        List<String> flags = new ArrayList<>();
        flags.add("HIGH_AMOUNT");
        flags.add("UNUSUAL_TIME");

        assessment = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .riskScore(BigDecimal.valueOf(85.50))
            .assessmentDate(LocalDateTime.now())
            .flags(flags)
            .details("Multiple risk indicators detected")
            .build();
    }

    // ==================== Flag Management Tests ====================

    @Test
    @DisplayName("addFlag - Should add new flag to list")
    void addFlag_shouldAddNewFlagToList() {
        // When
        assessment.addFlag("VELOCITY_CHECK");

        // Then
        assertThat(assessment.getFlags()).contains("VELOCITY_CHECK");
    }

    @Test
    @DisplayName("addFlag - Should not add duplicate flag")
    void addFlag_shouldNotAddDuplicateFlag() {
        // Given
        assessment.addFlag("HIGH_AMOUNT");
        int initialSize = assessment.getFlags().size();

        // When
        assessment.addFlag("HIGH_AMOUNT");

        // Then
        assertThat(assessment.getFlags()).hasSize(initialSize);
    }

    @Test
    @DisplayName("removeFlag - Should remove existing flag")
    void removeFlag_shouldRemoveExistingFlag() {
        // Given
        assessment.addFlag("GEOGRAPHIC_ANOMALY");

        // When
        assessment.removeFlag("GEOGRAPHIC_ANOMALY");

        // Then
        assertThat(assessment.getFlags()).doesNotContain("GEOGRAPHIC_ANOMALY");
    }

    @Test
    @DisplayName("removeFlag - Should handle non-existent flag gracefully")
    void removeFlag_shouldHandleNonExistentFlagGracefully() {
        // Given
        int initialSize = assessment.getFlags().size();

        // When
        assessment.removeFlag("NON_EXISTENT");

        // Then
        assertThat(assessment.getFlags()).hasSize(initialSize);
    }

    @Test
    @DisplayName("hasFlag - Should return true when flag exists")
    void hasFlag_shouldReturnTrueWhenFlagExists() {
        // Given
        assessment.addFlag("SUSPICIOUS_PATTERN");

        // Then
        assertThat(assessment.hasFlag("SUSPICIOUS_PATTERN")).isTrue();
    }

    @Test
    @DisplayName("hasFlag - Should return false when flag does not exist")
    void hasFlag_shouldReturnFalseWhenFlagDoesNotExist() {
        // Then
        assertThat(assessment.hasFlag("NON_EXISTENT")).isFalse();
    }

    // ==================== Workflow: Approve ====================

    @Test
    @DisplayName("approve - Should update all approval fields")
    void approve_shouldUpdateAllApprovalFields() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);
        String reviewerId = "reviewer-123";
        String notes = "Risk assessment approved, no fraud detected";
        LocalDateTime beforeApproval = LocalDateTime.now().minusSeconds(1);

        // When
        assessment.approve(reviewerId, notes);

        // Then
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.APPROVED);
        assertThat(assessment.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(assessment.getReviewedAt()).isAfter(beforeApproval);
        assertThat(assessment.getReviewNotes()).isEqualTo(notes);
        assertThat(assessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.NO_ACTION);
    }

    @Test
    @DisplayName("approve - Should set resolution action to NO_ACTION")
    void approve_shouldSetResolutionActionToNoAction() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);

        // When
        assessment.approve("reviewer-123", "Approved");

        // Then
        assertThat(assessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.NO_ACTION);
    }

    // ==================== Workflow: Reject ====================

    @Test
    @DisplayName("reject - Should update all rejection fields")
    void reject_shouldUpdateAllRejectionFields() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);
        String reviewerId = "reviewer-456";
        String notes = "High risk detected, blocking transaction";
        RiskAssessment.ResolutionAction action = RiskAssessment.ResolutionAction.BLOCK_TRANSACTION;
        LocalDateTime beforeRejection = LocalDateTime.now().minusSeconds(1);

        // When
        assessment.reject(reviewerId, notes, action);

        // Then
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.REJECTED);
        assertThat(assessment.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(assessment.getReviewedAt()).isAfter(beforeRejection);
        assertThat(assessment.getReviewNotes()).isEqualTo(notes);
        assertThat(assessment.getResolutionAction()).isEqualTo(action);
    }

    @Test
    @DisplayName("reject - Should accept different resolution actions")
    void reject_shouldAcceptDifferentResolutionActions() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);

        // When
        assessment.reject("reviewer-789", "Critical risk", RiskAssessment.ResolutionAction.SUSPEND_ACCOUNT);

        // Then
        assertThat(assessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.SUSPEND_ACCOUNT);
    }

    // ==================== Workflow: Escalate ====================

    @Test
    @DisplayName("escalate - Should update escalation fields")
    void escalate_shouldUpdateEscalationFields() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);
        String reviewerId = "reviewer-999";
        String notes = "Requires senior review";
        LocalDateTime beforeEscalation = LocalDateTime.now().minusSeconds(1);

        // When
        assessment.escalate(reviewerId, notes);

        // Then
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.ESCALATED);
        assertThat(assessment.getReviewedBy()).isEqualTo(reviewerId);
        assertThat(assessment.getReviewedAt()).isAfter(beforeEscalation);
        assertThat(assessment.getReviewNotes()).isEqualTo(notes);
    }

    // ==================== Risk Level Checking Tests ====================

    @Test
    @DisplayName("requiresReview - Should return true when risk level is HIGH")
    void requiresReview_shouldReturnTrueWhenRiskLevelIsHigh() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.HIGH);

        // Then
        assertThat(assessment.requiresReview()).isTrue();
    }

    @Test
    @DisplayName("requiresReview - Should return true when risk level is CRITICAL")
    void requiresReview_shouldReturnTrueWhenRiskLevelIsCritical() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.CRITICAL);

        // Then
        assertThat(assessment.requiresReview()).isTrue();
    }

    @Test
    @DisplayName("requiresReview - Should return false when risk level is LOW")
    void requiresReview_shouldReturnFalseWhenRiskLevelIsLow() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.LOW);

        // Then
        assertThat(assessment.requiresReview()).isFalse();
    }

    @Test
    @DisplayName("requiresReview - Should return false when risk level is MEDIUM")
    void requiresReview_shouldReturnFalseWhenRiskLevelIsMedium() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.MEDIUM);

        // Then
        assertThat(assessment.requiresReview()).isFalse();
    }

    @Test
    @DisplayName("isHighRisk - Should return true when risk level is HIGH")
    void isHighRisk_shouldReturnTrueWhenRiskLevelIsHigh() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.HIGH);

        // Then
        assertThat(assessment.isHighRisk()).isTrue();
    }

    @Test
    @DisplayName("isHighRisk - Should return true when risk level is CRITICAL")
    void isHighRisk_shouldReturnTrueWhenRiskLevelIsCritical() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.CRITICAL);

        // Then
        assertThat(assessment.isHighRisk()).isTrue();
    }

    @Test
    @DisplayName("isHighRisk - Should return false when risk level is LOW")
    void isHighRisk_shouldReturnFalseWhenRiskLevelIsLow() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.LOW);

        // Then
        assertThat(assessment.isHighRisk()).isFalse();
    }

    @Test
    @DisplayName("isHighRisk - Should return false when risk level is MEDIUM")
    void isHighRisk_shouldReturnFalseWhenRiskLevelIsMedium() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.MEDIUM);

        // Then
        assertThat(assessment.isHighRisk()).isFalse();
    }

    // ==================== Convenience Methods ====================

    @Test
    @DisplayName("getShopId - Should return null when shop is not set")
    void getShopId_shouldReturnNullWhenShopIsNotSet() {
        // Then
        assertThat(assessment.getShopId()).isNull();
    }

    @Test
    @DisplayName("getShopName - Should return null when shop is not set")
    void getShopName_shouldReturnNullWhenShopIsNotSet() {
        // Then
        assertThat(assessment.getShopName()).isNull();
    }

    // ==================== Builder Tests ====================

    @Test
    @DisplayName("Builder - Should create assessment with all fields")
    void builder_shouldCreateAssessmentWithAllFields() {
        // Given
        RiskAssessment.AssessmentType assessmentType = RiskAssessment.AssessmentType.COMPLIANCE_CHECK;
        RiskAssessment.RiskLevel riskLevel = RiskAssessment.RiskLevel.CRITICAL;
        BigDecimal riskScore = BigDecimal.valueOf(95.75);
        LocalDateTime assessmentDate = LocalDateTime.now();
        List<String> flags = List.of("COMPLIANCE_VIOLATION", "REGULATORY_ISSUE");
        String details = "Critical compliance issue detected";

        // When
        RiskAssessment newAssessment = RiskAssessment.builder()
            .assessmentType(assessmentType)
            .riskLevel(riskLevel)
            .riskScore(riskScore)
            .assessmentDate(assessmentDate)
            .flags(flags)
            .details(details)
            .build();

        // Then
        assertThat(newAssessment.getAssessmentType()).isEqualTo(assessmentType);
        assertThat(newAssessment.getRiskLevel()).isEqualTo(riskLevel);
        assertThat(newAssessment.getRiskScore()).isEqualByComparingTo(riskScore);
        assertThat(newAssessment.getAssessmentDate()).isEqualTo(assessmentDate);
        assertThat(newAssessment.getFlags()).containsExactlyInAnyOrder("COMPLIANCE_VIOLATION", "REGULATORY_ISSUE");
        assertThat(newAssessment.getDetails()).isEqualTo(details);
    }

    @Test
    @DisplayName("Builder - Should create assessment with default status PENDING")
    void builder_shouldCreateAssessmentWithDefaultStatusPending() {
        // When
        RiskAssessment newAssessment = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.LOW)
            .riskScore(BigDecimal.TEN)
            .assessmentDate(LocalDateTime.now())
            .build();

        // Then
        assertThat(newAssessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.PENDING);
    }

    @Test
    @DisplayName("Builder - Should create assessment with default empty flags list")
    void builder_shouldCreateAssessmentWithDefaultEmptyFlagsList() {
        // When
        RiskAssessment newAssessment = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.LOW)
            .riskScore(BigDecimal.TEN)
            .assessmentDate(LocalDateTime.now())
            .build();

        // Then
        assertThat(newAssessment.getFlags()).isNotNull().isEmpty();
    }

    // ==================== Complete Workflow Tests ====================

    @Test
    @DisplayName("Complete workflow - PENDING → UNDER_REVIEW → APPROVED")
    void completeWorkflow_pendingToUnderReviewToApproved() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);
        String initialReviewer = "reviewer-123";
        String finalReviewer = "reviewer-456";

        // When & Then: PENDING → UNDER_REVIEW
        assessment.setStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.UNDER_REVIEW);

        // When & Then: UNDER_REVIEW → APPROVED
        assessment.approve(finalReviewer, "Approved after review");
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.APPROVED);
        assertThat(assessment.getResolutionAction()).isEqualTo(RiskAssessment.ResolutionAction.NO_ACTION);
    }

    @Test
    @DisplayName("Complete workflow - PENDING → REJECTED with action")
    void completeWorkflow_pendingToRejectedWithAction() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);
        String reviewer = "reviewer-789";
        String notes = "High risk detected";
        RiskAssessment.ResolutionAction action = RiskAssessment.ResolutionAction.INVESTIGATE;

        // When
        assessment.reject(reviewer, notes, action);

        // Then
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.REJECTED);
        assertThat(assessment.getResolutionAction()).isEqualTo(action);
    }

    @Test
    @DisplayName("Complete workflow - PENDING → UNDER_REVIEW → ESCALATED")
    void completeWorkflow_pendingToUnderReviewToEscalated() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);

        // When & Then: PENDING → UNDER_REVIEW
        assessment.setStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.UNDER_REVIEW);

        // When & Then: UNDER_REVIEW → ESCALATED
        assessment.escalate("reviewer-999", "Requires senior management review");
        assertThat(assessment.getStatus()).isEqualTo(RiskAssessment.AssessmentStatus.ESCALATED);
    }

    // ==================== Enum Tests ====================

    @Test
    @DisplayName("AssessmentType - Should have all expected values")
    void assessmentType_shouldHaveAllExpectedValues() {
        // Then
        assertThat(RiskAssessment.AssessmentType.values()).containsExactlyInAnyOrder(
            RiskAssessment.AssessmentType.TRANSACTION_FRAUD,
            RiskAssessment.AssessmentType.INVESTMENT_RISK,
            RiskAssessment.AssessmentType.OPERATIONAL_RISK,
            RiskAssessment.AssessmentType.COMPLIANCE_CHECK
        );
    }

    @Test
    @DisplayName("RiskLevel - Should have all expected values")
    void riskLevel_shouldHaveAllExpectedValues() {
        // Then
        assertThat(RiskAssessment.RiskLevel.values()).containsExactlyInAnyOrder(
            RiskAssessment.RiskLevel.LOW,
            RiskAssessment.RiskLevel.MEDIUM,
            RiskAssessment.RiskLevel.HIGH,
            RiskAssessment.RiskLevel.CRITICAL
        );
    }

    @Test
    @DisplayName("AssessmentStatus - Should have all expected values")
    void assessmentStatus_shouldHaveAllExpectedValues() {
        // Then
        assertThat(RiskAssessment.AssessmentStatus.values()).containsExactlyInAnyOrder(
            RiskAssessment.AssessmentStatus.PENDING,
            RiskAssessment.AssessmentStatus.UNDER_REVIEW,
            RiskAssessment.AssessmentStatus.APPROVED,
            RiskAssessment.AssessmentStatus.REJECTED,
            RiskAssessment.AssessmentStatus.ESCALATED
        );
    }

    @Test
    @DisplayName("ResolutionAction - Should have all expected values")
    void resolutionAction_shouldHaveAllExpectedValues() {
        // Then
        assertThat(RiskAssessment.ResolutionAction.values()).containsExactlyInAnyOrder(
            RiskAssessment.ResolutionAction.NO_ACTION,
            RiskAssessment.ResolutionAction.MONITOR,
            RiskAssessment.ResolutionAction.INVESTIGATE,
            RiskAssessment.ResolutionAction.BLOCK_TRANSACTION,
            RiskAssessment.ResolutionAction.SUSPEND_ACCOUNT,
            RiskAssessment.ResolutionAction.REPORT_AUTHORITIES
        );
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Flag management - Should handle multiple add and remove operations")
    void flagManagement_shouldHandleMultipleAddAndRemoveOperations() {
        // Given
        RiskAssessment newAssessment = RiskAssessment.builder()
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.LOW)
            .riskScore(BigDecimal.TEN)
            .assessmentDate(LocalDateTime.now())
            .build();

        // When
        newAssessment.addFlag("FLAG_1");
        newAssessment.addFlag("FLAG_2");
        newAssessment.addFlag("FLAG_3");

        // Then
        assertThat(newAssessment.getFlags()).containsExactlyInAnyOrder("FLAG_1", "FLAG_2", "FLAG_3");

        // When
        newAssessment.removeFlag("FLAG_2");

        // Then
        assertThat(newAssessment.getFlags()).containsExactlyInAnyOrder("FLAG_1", "FLAG_3");
    }

    @Test
    @DisplayName("approve - Should set review timestamp to current time")
    void approve_shouldSetReviewTimestampToCurrentTime() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);
        LocalDateTime beforeApproval = LocalDateTime.now();

        // When
        assessment.approve("reviewer-123", "Approved");

        // Then
        assertThat(assessment.getReviewedAt())
            .isAfterOrEqualTo(beforeApproval)
            .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("reject - Should set review timestamp to current time")
    void reject_shouldSetReviewTimestampToCurrentTime() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.PENDING);
        LocalDateTime beforeRejection = LocalDateTime.now();

        // When
        assessment.reject("reviewer-456", "Rejected", RiskAssessment.ResolutionAction.MONITOR);

        // Then
        assertThat(assessment.getReviewedAt())
            .isAfterOrEqualTo(beforeRejection)
            .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("escalate - Should set review timestamp to current time")
    void escalate_shouldSetReviewTimestampToCurrentTime() {
        // Given
        assessment.setStatus(RiskAssessment.AssessmentStatus.UNDER_REVIEW);
        LocalDateTime beforeEscalation = LocalDateTime.now();

        // When
        assessment.escalate("reviewer-789", "Escalated");

        // Then
        assertThat(assessment.getReviewedAt())
            .isAfterOrEqualTo(beforeEscalation)
            .isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Risk score - Should handle decimal precision")
    void riskScore_shouldHandleDecimalPrecision() {
        // Given
        BigDecimal preciseScore = new BigDecimal("87.53");
        assessment.setRiskScore(preciseScore);

        // Then
        assertThat(assessment.getRiskScore()).isEqualByComparingTo(preciseScore);
    }

    @Test
    @DisplayName("High risk assessment - Should require review and be high risk")
    void highRiskAssessment_shouldRequireReviewAndBeHighRisk() {
        // Given
        assessment.setRiskLevel(RiskAssessment.RiskLevel.CRITICAL);

        // Then
        assertThat(assessment.requiresReview()).isTrue();
        assertThat(assessment.isHighRisk()).isTrue();
    }
}
