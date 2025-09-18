package com.princely.shopmanager.core.dto.registration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response after successful tenant registration
 */
public record TenantRegistrationResponse(
    String tenantId,
    String tenantName,
    String contactUserId,
    String contactUserEmail,
    List<String> shopIds,
    String registrationStatus,
    LocalDateTime submittedAt,
    String message,
    boolean requiresApproval
) {
    public static TenantRegistrationResponse success(
            String tenantId,
            String tenantName,
            String contactUserId,
            String contactUserEmail,
            List<String> shopIds) {
        return new TenantRegistrationResponse(
                tenantId,
                tenantName,
                contactUserId,
                contactUserEmail,
                shopIds,
                "PENDING_APPROVAL",
                LocalDateTime.now(),
                "Your registration has been submitted successfully and is pending admin approval. " +
                        "You will receive an email notification once your account is reviewed.",
                true
        );
    }
}