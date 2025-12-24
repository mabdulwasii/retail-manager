package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.shared.dto.EmailTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for sending various types of notifications (in-app, SMS, push).
 * Placeholder implementation for fraud alert notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String FIELD_TENANT_NAME = "tenantName";

    private final EmailService emailService;

    /**
     * Sends an in-app notification to a user.
     *
     * @param userId User ID to send notification to
     * @param type Notification type
     * @param message Notification message
     * @param metadata Additional metadata
     */
    public void sendNotification(String userId, String type, String message, Map<String, Object> metadata) {
        log.info("Sending in-app notification to user: {} of type: {}", userId, type);
        log.debug("Message: {}", message);
        log.debug("Metadata: {}", metadata);

        // In a real implementation, this would:
        // - Store notification in database
        // - Send via WebSocket to connected clients
        // - Queue for push notification if user is offline

        logNotification("IN-APP", userId, type, message, metadata);
    }

    /**
     * Sends an SMS notification.
     *
     * @param phoneNumber Phone number to send SMS to
     * @param message SMS message
     */
    public void sendSms(String phoneNumber, String message) {
        log.info("Sending SMS to: {}", phoneNumber);
        log.debug("SMS message: {}", message);

        // In a real implementation, this would integrate with SMS providers
        // like Twilio, Amazon SNS, or other SMS gateways

        logNotification("SMS", phoneNumber, "SMS", message, Map.of());
    }

    /**
     * Sends a push notification to a user's devices.
     *
     * @param userId User ID
     * @param title Notification title
     * @param message Notification message
     * @param metadata Additional metadata
     */
    public void sendPushNotification(String userId, String title, String message, Map<String, Object> metadata) {
        log.info("Sending push notification to user: {}", userId);
        log.debug("Title: {}, Message: {}", title, message);
        log.debug("Metadata: {}", metadata);

        // In a real implementation, this would integrate with:
        // - Firebase Cloud Messaging (FCM)
        // - Apple Push Notification Service (APNS)
        // - Web Push Protocol

        logNotification("PUSH", userId, "PUSH", title + ": " + message, metadata);
    }

    private void logNotification(String channel, String recipient, String type, String message, Map<String, Object> metadata) {
        StringBuilder content = new StringBuilder();
        content.append("=== NOTIFICATION SENT ===\n");
        content.append("Channel: ").append(channel).append("\n");
        content.append("Recipient: ").append(recipient).append("\n");
        content.append("Type: ").append(type).append("\n");
        content.append("Message: ").append(message).append("\n");

        if (!metadata.isEmpty()) {
            content.append("Metadata:\n");
            metadata.forEach((key, value) ->
                content.append("  ").append(key).append(": ").append(value).append("\n")
            );
        }

        content.append("========================");
        log.info(content.toString());
    }

    /**
     * Send tenant registration confirmation to tenant contact user
     */
    public void sendTenantRegistrationConfirmation(String tenantName, String contactEmail, String contactName) {
        log.info("Sending registration confirmation to tenant: {} at {}", tenantName, contactEmail);

        try {
            EmailTemplate template = EmailTemplate.builder()
                .to(contactEmail)
                .subject("Registration Submitted - " + tenantName)
                .templateName("tenant-registration-confirmation")
                .variables(Map.of(
                    "tenantName", tenantName,
                    "contactName", contactName,
                    "supportEmail", "support@shopmanager.com"
                ))
                .build();

            emailService.sendTemplatedEmail(template);
            log.info("Registration confirmation sent successfully to: {}", contactEmail);

        } catch (Exception e) {
            log.error("Failed to send registration confirmation to: {}", contactEmail, e);
            // Don't throw exception - notification failure shouldn't break registration
        }
    }

    /**
     * Send new tenant registration alert to all super admins
     */
    public void sendNewTenantRegistrationAlert(String tenantId, String tenantName, List<User> superAdmins) {
        log.info("Sending new tenant registration alert for: {} to {} super admins", tenantName, superAdmins.size());

        for (User admin : superAdmins) {
            try {
                EmailTemplate template = EmailTemplate.builder()
                    .to(admin.getEmail())
                    .subject("New Tenant Registration - " + tenantName)
                    .templateName("super-admin-registration-alert")
                    .variables(Map.of(
                        "adminName", admin.getFullName(),
                        "tenantId", tenantId,
                        "tenantName", tenantName,
                        "reviewUrl", generateReviewUrl(tenantId),
                        "dashboardUrl", "https://admin.shopmanager.com"
                    ))
                    .build();

                emailService.sendTemplatedEmail(template);
                log.debug("Registration alert sent to super admin: {}", admin.getEmail());

            } catch (Exception e) {
                log.error("Failed to send registration alert to super admin: {}", admin.getEmail(), e);
                // Continue sending to other admins even if one fails
            }
        }

        log.info("Completed sending registration alerts for tenant: {}", tenantName);
    }

    /**
     * Send tenant activation notification to tenant contact user
     */
    public void sendTenantActivationNotification(String tenantName, String contactEmail, String contactName,
                                               boolean approved, String rejectionReason) {
        log.info("Sending activation notification to tenant: {} (approved: {})", tenantName, approved);

        try {
            String subject = approved ? "Tenant Activated - " + tenantName : "Tenant Registration Update - " + tenantName;
            String templateName = approved ? "tenant-activation-approved" : "tenant-activation-rejected";

            Map<String, Object> variables = Map.of(
                "tenantName", tenantName,
                "contactName", contactName,
                "approved", approved,
                "rejectionReason", rejectionReason != null ? rejectionReason : "",
                "loginUrl", "https://app.shopmanager.com/login",
                "supportEmail", "support@shopmanager.com"
            );

            EmailTemplate template = EmailTemplate.builder()
                .to(contactEmail)
                .subject(subject)
                .templateName(templateName)
                .variables(variables)
                .build();

            emailService.sendTemplatedEmail(template);
            log.info("Activation notification sent successfully to: {}", contactEmail);

        } catch (Exception e) {
            log.error("Failed to send activation notification to: {}", contactEmail, e);
        }
    }

    private String generateReviewUrl(String tenantId) {
        return String.format("https://admin.shopmanager.com/tenants/review/%s", tenantId);
    }
}