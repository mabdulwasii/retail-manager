package com.princely.shopmanager.fraud.service;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.event.FraudAlertCreatedEvent;
import com.princely.shopmanager.fraud.event.RiskAssessmentCreatedEvent;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.service.EmailService;
import com.princely.shopmanager.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling fraud alert notifications via email, SMS, and in-app notifications.
 * Provides real-time alerts to relevant stakeholders based on severity and role permissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudAlertNotificationService {

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles fraud alert creation events and sends notifications to relevant users.
     */
    @ApplicationModuleListener
    @Async
    public void handleFraudAlertCreated(FraudAlertCreatedEvent event) {
        try {
            FraudAlert alert = event.getAlert();
            log.info("Processing fraud alert notification for alert: {}", alert.getAlertNumber());

            // Get notification recipients based on alert severity and shop
            List<User> recipients = getNotificationRecipients(alert);

            for (User recipient : recipients) {
                // Send email notification
                sendEmailNotification(recipient, alert);

                // Send in-app notification
                sendInAppNotification(recipient, alert);

                // Send SMS for critical alerts
                if (shouldSendSmsNotification(alert, recipient)) {
                    sendSmsNotification(recipient, alert);
                }
            }

            log.info("Fraud alert notifications sent successfully for alert: {}", alert.getAlertNumber());
        } catch (Exception e) {
            log.error("Failed to send fraud alert notifications for event: {}", event, e);
        }
    }

    /**
     * Handles risk assessment creation events and sends notifications for high-risk assessments.
     */
    @ApplicationModuleListener
    @Async
    public void handleRiskAssessmentCreated(RiskAssessmentCreatedEvent event) {
        try {
            RiskAssessment assessment = event.getAssessment();

            // Only notify for high and critical risk assessments
            if (assessment.getRiskLevel() == RiskAssessment.RiskLevel.HIGH ||
                assessment.getRiskLevel() == RiskAssessment.RiskLevel.CRITICAL) {

                log.info("Processing risk assessment notification for assessment: {}", assessment.getId());

                List<User> recipients = getRiskAssessmentRecipients(assessment);

                for (User recipient : recipients) {
                    sendRiskAssessmentEmailNotification(recipient, assessment);
                    sendRiskAssessmentInAppNotification(recipient, assessment);
                }

                log.info("Risk assessment notifications sent for assessment: {}", assessment.getId());
            }
        } catch (Exception e) {
            log.error("Failed to send risk assessment notifications for event: {}", event, e);
        }
    }

    /**
     * Determines notification recipients based on alert severity and user roles.
     */
    private List<User> getNotificationRecipients(FraudAlert alert) {
        String tenantId = extractTenantId(alert);
        String shopId = alert.getShopId();

        return switch (alert.getSeverity()) {
            case CRITICAL -> {
                // Critical alerts: Notify all admins, managers, and owners
                yield userService.getUsersByRolesAndTenant(
                    List.of("TENANT_ADMIN", "SHOP_OWNER", "SHOP_MANAGER"),
                    tenantId
                );
            }
            case HIGH -> {
                // High alerts: Notify admins and managers
                yield userService.getUsersByRolesAndTenant(
                    List.of("TENANT_ADMIN", "SHOP_MANAGER"),
                    tenantId
                );
            }
            case MEDIUM -> {
                // Medium alerts: Notify managers only
                yield userService.getUsersByRolesAndTenant(
                    List.of("SHOP_MANAGER"),
                    tenantId
                );
            }
            case LOW -> {
                // Low alerts: Log only, no notifications
                log.info("Low severity alert created, no notifications sent: {}", alert.getAlertNumber());
                yield List.of();
            }
        };
    }

    /**
     * Determines risk assessment notification recipients.
     */
    private List<User> getRiskAssessmentRecipients(RiskAssessment assessment) {
        String tenantId = extractTenantIdFromAssessment(assessment);

        // Risk assessments require review by admins and senior managers
        return userService.getUsersByRolesAndTenant(
            List.of("TENANT_ADMIN", "SHOP_OWNER"),
            tenantId
        );
    }

    /**
     * Sends email notification for fraud alerts.
     */
    private void sendEmailNotification(User recipient, FraudAlert alert) {
        try {
            String subject = String.format("🚨 Fraud Alert: %s - %s",
                alert.getSeverity(), alert.getTitle());

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("recipientName", recipient.getFirstName());
            templateVariables.put("alertNumber", alert.getAlertNumber());
            templateVariables.put("alertType", alert.getAlertType().name());
            templateVariables.put("severity", alert.getSeverity().name());
            templateVariables.put("title", alert.getTitle());
            templateVariables.put("description", alert.getDescription());
            templateVariables.put("riskScore", alert.getRiskScore());
            templateVariables.put("confidenceLevel", alert.getConfidenceLevel());
            templateVariables.put("detectionTime", alert.getDetectionTimestamp().format(FORMATTER));
            templateVariables.put("shopName", alert.getShopName());
            templateVariables.put("evidence", formatEvidence(alert.getEvidence()));

            emailService.sendTemplatedEmail(
                recipient.getEmail(),
                subject,
                "fraud-alert-notification",
                templateVariables
            );

            log.debug("Email notification sent to: {} for alert: {}",
                recipient.getEmail(), alert.getAlertNumber());
        } catch (Exception e) {
            log.error("Failed to send email notification to: {} for alert: {}",
                recipient.getEmail(), alert.getAlertNumber(), e);
        }
    }

    /**
     * Sends in-app notification for fraud alerts.
     */
    private void sendInAppNotification(User recipient, FraudAlert alert) {
        try {
            String message = String.format("New %s fraud alert: %s",
                alert.getSeverity().name().toLowerCase(), alert.getTitle());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("alertId", alert.getId());
            metadata.put("alertNumber", alert.getAlertNumber());
            metadata.put("alertType", alert.getAlertType().name());
            metadata.put("severity", alert.getSeverity().name());
            metadata.put("riskScore", alert.getRiskScore());
            metadata.put("shopId", alert.getShopId());

            notificationService.sendNotification(
                recipient.getId(),
                "FRAUD_ALERT",
                message,
                metadata
            );

            log.debug("In-app notification sent to user: {} for alert: {}",
                recipient.getId(), alert.getAlertNumber());
        } catch (Exception e) {
            log.error("Failed to send in-app notification to user: {} for alert: {}",
                recipient.getId(), alert.getAlertNumber(), e);
        }
    }

    /**
     * Sends SMS notification for critical fraud alerts.
     */
    private void sendSmsNotification(User recipient, FraudAlert alert) {
        try {
            if (recipient.getPhoneNumber() == null || recipient.getPhoneNumber().trim().isEmpty()) {
                log.warn("No phone number available for SMS notification to user: {}", recipient.getId());
                return;
            }

            String message = String.format("URGENT: %s fraud alert detected at %s. Alert #%s. Check your dashboard immediately.",
                alert.getSeverity().name(),
                alert.getShopName() != null ? alert.getShopName() : "your shop",
                alert.getAlertNumber());

            notificationService.sendSms(recipient.getPhoneNumber(), message);

            log.debug("SMS notification sent to: {} for critical alert: {}",
                recipient.getPhoneNumber(), alert.getAlertNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {} for alert: {}",
                recipient.getPhoneNumber(), alert.getAlertNumber(), e);
        }
    }

    /**
     * Sends email notification for risk assessments.
     */
    private void sendRiskAssessmentEmailNotification(User recipient, RiskAssessment assessment) {
        try {
            String subject = String.format("🔍 Risk Assessment Review Required - %s Risk",
                assessment.getRiskLevel());

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("recipientName", recipient.getFirstName());
            templateVariables.put("assessmentId", assessment.getId());
            templateVariables.put("assessmentType", assessment.getAssessmentType().name());
            templateVariables.put("riskLevel", assessment.getRiskLevel().name());
            templateVariables.put("riskScore", assessment.getRiskScore());
            templateVariables.put("assessmentDate", assessment.getAssessmentDate().format(FORMATTER));
            templateVariables.put("shopName", assessment.getShopName());
            templateVariables.put("details", assessment.getDetails());
            templateVariables.put("flags", String.join(", ", assessment.getFlags()));

            emailService.sendTemplatedEmail(
                recipient.getEmail(),
                subject,
                "risk-assessment-notification",
                templateVariables
            );

            log.debug("Risk assessment email sent to: {} for assessment: {}",
                recipient.getEmail(), assessment.getId());
        } catch (Exception e) {
            log.error("Failed to send risk assessment email to: {} for assessment: {}",
                recipient.getEmail(), assessment.getId(), e);
        }
    }

    /**
     * Sends in-app notification for risk assessments.
     */
    private void sendRiskAssessmentInAppNotification(User recipient, RiskAssessment assessment) {
        try {
            String message = String.format("New %s risk assessment requires review",
                assessment.getRiskLevel().name().toLowerCase());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("assessmentId", assessment.getId());
            metadata.put("assessmentType", assessment.getAssessmentType().name());
            metadata.put("riskLevel", assessment.getRiskLevel().name());
            metadata.put("riskScore", assessment.getRiskScore());
            metadata.put("shopId", assessment.getShopId());

            notificationService.sendNotification(
                recipient.getId(),
                "RISK_ASSESSMENT",
                message,
                metadata
            );

            log.debug("Risk assessment in-app notification sent to user: {} for assessment: {}",
                recipient.getId(), assessment.getId());
        } catch (Exception e) {
            log.error("Failed to send risk assessment in-app notification to user: {} for assessment: {}",
                recipient.getId(), assessment.getId(), e);
        }
    }

    /**
     * Determines if SMS notification should be sent based on alert severity and user preferences.
     */
    private boolean shouldSendSmsNotification(FraudAlert alert, User user) {
        // Send SMS only for critical alerts and if user has phone number
        return alert.getSeverity() == FraudAlert.AlertSeverity.CRITICAL &&
               user.getPhoneNumber() != null &&
               !user.getPhoneNumber().trim().isEmpty();
    }

    /**
     * Formats evidence map for display in notifications.
     */
    private String formatEvidence(Map<String, String> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return "No additional evidence available";
        }

        StringBuilder formatted = new StringBuilder();
        evidence.forEach((key, value) ->
            formatted.append(String.format("• %s: %s\n", key, value))
        );
        return formatted.toString().trim();
    }

    /**
     * Extracts tenant ID from fraud alert context.
     */
    private String extractTenantId(FraudAlert alert) {
        // In a real implementation, this would extract tenant ID from alert context
        // For now, return a default tenant ID
        return "default-tenant";
    }

    /**
     * Extracts tenant ID from risk assessment context.
     */
    private String extractTenantIdFromAssessment(RiskAssessment assessment) {
        // In a real implementation, this would extract tenant ID from assessment context
        // For now, return a default tenant ID
        return "default-tenant";
    }
}