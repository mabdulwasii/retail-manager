package com.princely.shopmanager.fraud.event;

import com.princely.shopmanager.fraud.domain.RiskAssessment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RiskAssessmentCreatedEvent Tests")
class RiskAssessmentCreatedEventTest {

    private RiskAssessment assessment;
    private Object source;

    @BeforeEach
    void setUp() {
        source = new Object();
        assessment = RiskAssessment.builder()
            .id("assessment-1")
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION_FRAUD)
            .riskLevel(RiskAssessment.RiskLevel.HIGH)
            .riskScore(BigDecimal.valueOf(85.0))
            .assessmentDate(LocalDateTime.now())
            .details("High velocity transaction detected")
            .build();
    }

    @Test
    @DisplayName("Should create event with all fields")
    void shouldCreateEventWithAllFields() {
        // When
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, assessment, "tenant-1", "shop-1");

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getAssessment()).isEqualTo(assessment);
        assertThat(event.getTenantId()).isEqualTo("tenant-1");
        assertThat(event.getShopId()).isEqualTo("shop-1");
        assertThat(event.getSource()).isEqualTo(source);
    }

    @Test
    @DisplayName("Should format toString() correctly")
    void shouldFormatToStringCorrectly() {
        // Given
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, assessment, "tenant-1", "shop-1");

        // When
        String result = event.toString();

        // Then
        assertThat(result).contains("RiskAssessmentCreatedEvent")
                .contains("assessmentId='assessment-1'")
                .contains("riskLevel='HIGH'")
                .contains("tenantId='tenant-1'")
                .contains("shopId='shop-1'");
    }

    @Test
    @DisplayName("Should expose assessment details")
    void shouldExposeAssessmentDetails() {
        // Given
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, assessment, "tenant-1", "shop-1");

        // When
        RiskAssessment retrievedAssessment = event.getAssessment();

        // Then
        assertThat(retrievedAssessment.getId()).isEqualTo("assessment-1");
        assertThat(retrievedAssessment.getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.HIGH);
        assertThat(retrievedAssessment.getRiskScore()).isEqualTo(BigDecimal.valueOf(85.0));
        assertThat(retrievedAssessment.getAssessmentType()).isEqualTo(RiskAssessment.AssessmentType.TRANSACTION_FRAUD);
    }

    @Test
    @DisplayName("Should handle different risk levels")
    void shouldHandleDifferentRiskLevels() {
        // Test CRITICAL risk level
        RiskAssessment criticalAssessment = RiskAssessment.builder()
            .id("assessment-2")
            .assessmentType(RiskAssessment.AssessmentType.INVESTMENT_RISK)
            .riskLevel(RiskAssessment.RiskLevel.CRITICAL)
            .riskScore(BigDecimal.valueOf(95.0))
            .build();

        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, criticalAssessment, "tenant-1", "shop-1");

        assertThat(event.toString()).contains("riskLevel='CRITICAL'");
        assertThat(event.getAssessment().getRiskLevel()).isEqualTo(RiskAssessment.RiskLevel.CRITICAL);
    }

    @Test
    @DisplayName("Should handle different assessment types")
    void shouldHandleDifferentAssessmentTypes() {
        // Test COMPLIANCE_CHECK assessment type
        RiskAssessment complianceAssessment = RiskAssessment.builder()
            .id("assessment-3")
            .assessmentType(RiskAssessment.AssessmentType.COMPLIANCE_CHECK)
            .riskLevel(RiskAssessment.RiskLevel.MEDIUM)
            .riskScore(BigDecimal.valueOf(50.0))
            .build();

        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, complianceAssessment, "tenant-1", "shop-1");

        assertThat(event.getAssessment().getAssessmentType()).isEqualTo(RiskAssessment.AssessmentType.COMPLIANCE_CHECK);
    }

    @Test
    @DisplayName("Should be instance of ApplicationEvent")
    void shouldBeInstanceOfApplicationEvent() {
        // Given
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, assessment, "tenant-1", "shop-1");

        // When/Then
        assertThat(event).isInstanceOf(org.springframework.context.ApplicationEvent.class);
        assertThat(event.getTimestamp()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should preserve all event metadata")
    void shouldPreserveAllEventMetadata() {
        // Given
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(source, assessment, "tenant-123", "shop-456");

        // When/Then
        assertThat(event.getTenantId()).isEqualTo("tenant-123");
        assertThat(event.getShopId()).isEqualTo("shop-456");
        assertThat(event.getAssessment().getId()).isEqualTo("assessment-1");
    }
}
