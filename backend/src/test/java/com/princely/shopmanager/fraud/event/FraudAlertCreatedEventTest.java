package com.princely.shopmanager.fraud.event;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FraudAlertCreatedEvent Tests")
class FraudAlertCreatedEventTest {

    private FraudAlert alert;
    private Object source;

    @BeforeEach
    void setUp() {
        source = new Object();
        alert = FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALT-12345")
            .severity(FraudAlert.AlertSeverity.HIGH)
            .title("Suspicious Transaction")
            .description("High value transaction detected")
            .riskScore(BigDecimal.valueOf(85.5))
            .confidenceLevel(BigDecimal.valueOf(90.0))
            .detectionTimestamp(LocalDateTime.now())
            .evidence(Map.of("amount", "5000.00"))
            .build();
    }

    @Test
    @DisplayName("Should create event with all fields")
    void shouldCreateEventWithAllFields() {
        // When
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(source, alert, "tenant-1", "shop-1");

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getAlert()).isEqualTo(alert);
        assertThat(event.getTenantId()).isEqualTo("tenant-1");
        assertThat(event.getShopId()).isEqualTo("shop-1");
        assertThat(event.getSource()).isEqualTo(source);
    }

    @Test
    @DisplayName("Should format toString() correctly")
    void shouldFormatToStringCorrectly() {
        // Given
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(source, alert, "tenant-1", "shop-1");

        // When
        String result = event.toString();

        // Then
        assertThat(result).contains("FraudAlertCreatedEvent")
                .contains("alertNumber='ALT-12345'")
                .contains("severity='HIGH'")
                .contains("tenantId='tenant-1'")
                .contains("shopId='shop-1'");
    }

    @Test
    @DisplayName("Should expose alert details")
    void shouldExposeAlertDetails() {
        // Given
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(source, alert, "tenant-1", "shop-1");

        // When
        FraudAlert retrievedAlert = event.getAlert();

        // Then
        assertThat(retrievedAlert.getAlertNumber()).isEqualTo("ALT-12345");
        assertThat(retrievedAlert.getSeverity()).isEqualTo(FraudAlert.AlertSeverity.HIGH);
        assertThat(retrievedAlert.getRiskScore()).isEqualTo(BigDecimal.valueOf(85.5));
    }

    @Test
    @DisplayName("Should handle different severity levels")
    void shouldHandleDifferentSeverityLevels() {
        // Test CRITICAL severity
        FraudAlert criticalAlert = FraudAlert.builder()
            .id("alert-2")
            .alertNumber("ALT-99999")
            .severity(FraudAlert.AlertSeverity.CRITICAL)
            .title("Critical Alert")
            .build();

        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(source, criticalAlert, "tenant-1", "shop-1");

        assertThat(event.toString()).contains("severity='CRITICAL'");
        assertThat(event.getAlert().getSeverity()).isEqualTo(FraudAlert.AlertSeverity.CRITICAL);
    }

    @Test
    @DisplayName("Should be instance of ApplicationEvent")
    void shouldBeInstanceOfApplicationEvent() {
        // Given
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(source, alert, "tenant-1", "shop-1");

        // When/Then
        assertThat(event).isInstanceOf(org.springframework.context.ApplicationEvent.class);
        assertThat(event.getTimestamp()).isGreaterThan(0);
    }
}
