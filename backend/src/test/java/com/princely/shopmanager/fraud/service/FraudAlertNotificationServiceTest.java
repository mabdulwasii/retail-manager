package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.event.FraudAlertCreatedEvent;
import com.princely.shopmanager.fraud.event.RiskAssessmentCreatedEvent;
import com.princely.shopmanager.shared.service.EmailService;
import com.princely.shopmanager.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FraudAlertNotificationService Tests")
class FraudAlertNotificationServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FraudAlertNotificationService fraudAlertNotificationService;

    private User testUser;
    private FraudAlert testAlert;
    private RiskAssessment testAssessment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id("user-1")
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phoneNumber("+1234567890")
            .build();
    }

    private FraudAlert createFraudAlert(FraudAlert.AlertSeverity severity) {
        return FraudAlert.builder()
            .id("alert-1")
            .alertNumber("ALT-001")
            .alertType(FraudAlert.AlertType.UNUSUAL_TRANSACTION)
            .severity(severity)
            .title("Suspicious Transaction Detected")
            .description("Large transaction outside normal hours")
            .riskScore(85.5)
            .confidenceLevel(90.0)
            .detectionTimestamp(LocalDateTime.now())
            .shopId("shop-1")
            .shopName("Test Shop")
            .evidence(Map.of("amount", "5000.00", "time", "02:00 AM"))
            .build();
    }

    private RiskAssessment createRiskAssessment(RiskAssessment.RiskLevel riskLevel) {
        return RiskAssessment.builder()
            .id("assessment-1")
            .assessmentType(RiskAssessment.AssessmentType.TRANSACTION)
            .riskLevel(riskLevel)
            .riskScore(85.0)
            .assessmentDate(LocalDateTime.now())
            .shopId("shop-1")
            .shopName("Test Shop")
            .details("High-risk transaction pattern detected")
            .flags(List.of("UNUSUAL_AMOUNT", "OFF_HOURS"))
            .build();
    }

    // Critical Alert Tests
    @Test
    @DisplayName("handleFraudAlertCreated - Critical severity should notify all roles and send SMS")
    void handleFraudAlertCreated_CriticalSeverity_ShouldNotifyAllRolesAndSendSms() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(userService).getUsersByRolesAndTenant(
            eq(List.of("TENANT_ADMIN", "OWNER", "MANAGER")),
            anyString()
        );
        verify(emailService).sendTemplatedEmail(
            eq(testUser.getEmail()),
            anyString(),
            eq("fraud-alert-notification"),
            any(Map.class)
        );
        verify(notificationService).sendNotification(
            eq(testUser.getId()),
            eq("FRAUD_ALERT"),
            anyString(),
            any(Map.class)
        );
        verify(notificationService).sendSms(eq(testUser.getPhoneNumber()), anyString());
    }

    @Test
    @DisplayName("handleFraudAlertCreated - HIGH severity should notify admins and managers only")
    void handleFraudAlertCreated_HighSeverity_ShouldNotifyAdminsAndManagers() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.HIGH);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(userService).getUsersByRolesAndTenant(
            eq(List.of("TENANT_ADMIN", "MANAGER")),
            anyString()
        );
        verify(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any(Map.class));
        verify(notificationService).sendNotification(anyString(), anyString(), anyString(), any(Map.class));
        verify(notificationService, never()).sendSms(anyString(), anyString()); // No SMS for HIGH
    }

    @Test
    @DisplayName("handleFraudAlertCreated - MEDIUM severity should notify managers only")
    void handleFraudAlertCreated_MediumSeverity_ShouldNotifyManagersOnly() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.MEDIUM);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(userService).getUsersByRolesAndTenant(
            eq(List.of("MANAGER")),
            anyString()
        );
        verify(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any(Map.class));
        verify(notificationService).sendNotification(anyString(), anyString(), anyString(), any(Map.class));
        verify(notificationService, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("handleFraudAlertCreated - LOW severity should not send any notifications")
    void handleFraudAlertCreated_LowSeverity_ShouldNotSendNotifications() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.LOW);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(userService, never()).getUsersByRolesAndTenant(any(), anyString());
        verify(emailService, never()).sendTemplatedEmail(anyString(), anyString(), anyString(), any());
        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString(), any());
        verify(notificationService, never()).sendSms(anyString(), anyString());
    }

    // Email Notification Tests
    @Test
    @DisplayName("Email notification should include all alert details")
    void emailNotification_ShouldIncludeAllAlertDetails() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        ArgumentCaptor<Map<String, Object>> templateVarsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(emailService).sendTemplatedEmail(
            eq(testUser.getEmail()),
            contains("Fraud Alert"),
            eq("fraud-alert-notification"),
            templateVarsCaptor.capture()
        );

        Map<String, Object> templateVars = templateVarsCaptor.getValue();
        assertThat(templateVars)
            .containsEntry("recipientName", "John")
            .containsEntry("alertNumber", "ALT-001")
            .containsEntry("severity", "CRITICAL")
            .containsEntry("title", "Suspicious Transaction Detected")
            .containsEntry("riskScore", 85.5)
            .containsEntry("shopName", "Test Shop")
            .containsKey("detectionTime")
            .containsKey("evidence");
    }

    @Test
    @DisplayName("Email notification should handle null evidence gracefully")
    void emailNotification_ShouldHandleNullEvidence() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.HIGH);
        testAlert.setEvidence(null);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then - Should not throw exception
        verify(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Email notification should continue on failure")
    void emailNotification_ShouldContinueOnFailure() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));
        doThrow(new RuntimeException("Email service unavailable"))
            .when(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any());

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then - Should still send in-app notification
        verify(notificationService).sendNotification(anyString(), anyString(), anyString(), any(Map.class));
    }

    // In-App Notification Tests
    @Test
    @DisplayName("In-app notification should include alert metadata")
    void inAppNotification_ShouldIncludeAlertMetadata() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.HIGH);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        ArgumentCaptor<Map<String, Object>> metadataCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(notificationService).sendNotification(
            eq(testUser.getId()),
            eq("FRAUD_ALERT"),
            contains("high fraud alert"),
            metadataCaptor.capture()
        );

        Map<String, Object> metadata = metadataCaptor.getValue();
        assertThat(metadata)
            .containsEntry("alertId", "alert-1")
            .containsEntry("alertNumber", "ALT-001")
            .containsEntry("severity", "HIGH")
            .containsEntry("riskScore", 85.5)
            .containsEntry("shopId", "shop-1");
    }

    // SMS Notification Tests
    @Test
    @DisplayName("SMS should only be sent for CRITICAL alerts")
    void sms_ShouldOnlyBeSentForCriticalAlerts() {
        // Given - Critical alert
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent criticalEvent = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(criticalEvent);

        // Then
        verify(notificationService).sendSms(eq(testUser.getPhoneNumber()), anyString());

        // Given - High alert
        reset(notificationService);
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.HIGH);
        FraudAlertCreatedEvent highEvent = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(highEvent);

        // Then
        verify(notificationService, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("SMS should not be sent if user has no phone number")
    void sms_ShouldNotBeSentIfUserHasNoPhoneNumber() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        testUser.setPhoneNumber(null);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(notificationService, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("SMS should not be sent if phone number is empty")
    void sms_ShouldNotBeSentIfPhoneNumberIsEmpty() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        testUser.setPhoneNumber("   ");
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(notificationService, never()).sendSms(anyString(), anyString());
    }

    @Test
    @DisplayName("SMS message should include alert details")
    void sms_MessageShouldIncludeAlertDetails() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(notificationService).sendSms(
            eq(testUser.getPhoneNumber()),
            messageCaptor.capture()
        );

        String message = messageCaptor.getValue();
        assertThat(message)
            .contains("URGENT")
            .contains("CRITICAL")
            .contains("Test Shop")
            .contains("ALT-001");
    }

    // Risk Assessment Tests
    @Test
    @DisplayName("handleRiskAssessmentCreated - HIGH risk should send notifications")
    void handleRiskAssessmentCreated_HighRisk_ShouldSendNotifications() {
        // Given
        testAssessment = createRiskAssessment(RiskAssessment.RiskLevel.HIGH);
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(this, testAssessment, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleRiskAssessmentCreated(event);

        // Then
        verify(userService).getUsersByRolesAndTenant(
            eq(List.of("TENANT_ADMIN", "OWNER")),
            anyString()
        );
        verify(emailService).sendTemplatedEmail(
            anyString(),
            contains("Risk Assessment"),
            eq("risk-assessment-notification"),
            any(Map.class)
        );
        verify(notificationService).sendNotification(
            anyString(),
            eq("RISK_ASSESSMENT"),
            anyString(),
            any(Map.class)
        );
    }

    @Test
    @DisplayName("handleRiskAssessmentCreated - CRITICAL risk should send notifications")
    void handleRiskAssessmentCreated_CriticalRisk_ShouldSendNotifications() {
        // Given
        testAssessment = createRiskAssessment(RiskAssessment.RiskLevel.CRITICAL);
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(this, testAssessment, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        // When
        fraudAlertNotificationService.handleRiskAssessmentCreated(event);

        // Then
        verify(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any(Map.class));
        verify(notificationService).sendNotification(anyString(), anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("handleRiskAssessmentCreated - MEDIUM risk should not send notifications")
    void handleRiskAssessmentCreated_MediumRisk_ShouldNotSendNotifications() {
        // Given
        testAssessment = createRiskAssessment(RiskAssessment.RiskLevel.MEDIUM);
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(this, testAssessment, "tenant-1", "shop-1");

        // When
        fraudAlertNotificationService.handleRiskAssessmentCreated(event);

        // Then
        verify(userService, never()).getUsersByRolesAndTenant(any(), anyString());
        verify(emailService, never()).sendTemplatedEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("handleRiskAssessmentCreated - LOW risk should not send notifications")
    void handleRiskAssessmentCreated_LowRisk_ShouldNotSendNotifications() {
        // Given
        testAssessment = createRiskAssessment(RiskAssessment.RiskLevel.LOW);
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(this, testAssessment, "tenant-1", "shop-1");

        // When
        fraudAlertNotificationService.handleRiskAssessmentCreated(event);

        // Then
        verify(userService, never()).getUsersByRolesAndTenant(any(), anyString());
        verify(emailService, never()).sendTemplatedEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Risk assessment email should include assessment details")
    void riskAssessmentEmail_ShouldIncludeAssessmentDetails() {
        // Given
        testAssessment = createRiskAssessment(RiskAssessment.RiskLevel.HIGH);
        RiskAssessmentCreatedEvent event = new RiskAssessmentCreatedEvent(this, testAssessment, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));

        ArgumentCaptor<Map<String, Object>> templateVarsCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        fraudAlertNotificationService.handleRiskAssessmentCreated(event);

        // Then
        verify(emailService).sendTemplatedEmail(
            anyString(),
            anyString(),
            eq("risk-assessment-notification"),
            templateVarsCaptor.capture()
        );

        Map<String, Object> templateVars = templateVarsCaptor.getValue();
        assertThat(templateVars)
            .containsEntry("recipientName", "John")
            .containsEntry("assessmentId", "assessment-1")
            .containsEntry("riskLevel", "HIGH")
            .containsEntry("riskScore", 85.0)
            .containsKey("assessmentDate");
    }

    // Error Handling Tests
    @Test
    @DisplayName("Event handling should continue on notification service failure")
    void eventHandling_ShouldContinueOnNotificationFailure() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser));
        doThrow(new RuntimeException("Service unavailable"))
            .when(notificationService).sendNotification(anyString(), anyString(), anyString(), any());

        // When - Should not throw exception
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(emailService).sendTemplatedEmail(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Multiple recipients should all receive notifications")
    void multipleRecipients_ShouldAllReceiveNotifications() {
        // Given
        testAlert = createFraudAlert(FraudAlert.AlertSeverity.CRITICAL);
        FraudAlertCreatedEvent event = new FraudAlertCreatedEvent(this, testAlert, "tenant-1", "shop-1");

        User user2 = User.builder()
            .id("user-2")
            .firstName("Jane")
            .email("jane@example.com")
            .phoneNumber("+9876543210")
            .build();

        when(userService.getUsersByRolesAndTenant(any(), anyString()))
            .thenReturn(List.of(testUser, user2));

        // When
        fraudAlertNotificationService.handleFraudAlertCreated(event);

        // Then
        verify(emailService, times(2)).sendTemplatedEmail(anyString(), anyString(), anyString(), any());
        verify(notificationService, times(2)).sendNotification(anyString(), anyString(), anyString(), any());
        verify(notificationService, times(2)).sendSms(anyString(), anyString());
    }
}
