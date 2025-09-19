package com.princely.shopmanager.core.dto.activation;

import java.time.LocalDateTime;

/**
 * Response for tenant activation/rejection
 */
public record TenantActivationResponse(
    String tenantId,
    String tenantName,
    boolean approved,
    String message,
    LocalDateTime processedAt,
    String processedBy
) {

    public static TenantActivationResponse approved(String tenantId, String tenantName, String processedBy) {
        return new TenantActivationResponse(
            tenantId,
            tenantName,
            true,
            "Tenant approved and activated successfully",
            LocalDateTime.now(),
            processedBy
        );
    }

    public static TenantActivationResponse rejected(String tenantId, String tenantName, String reason, String processedBy) {
        return new TenantActivationResponse(
            tenantId,
            tenantName,
            false,
            "Tenant registration rejected: " + reason,
            LocalDateTime.now(),
            processedBy
        );
    }
}