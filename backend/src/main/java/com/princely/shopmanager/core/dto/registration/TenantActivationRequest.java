package com.princely.shopmanager.core.dto.registration;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for activating a pending tenant (super admin action)
 */
public record TenantActivationRequest(
    @NotEmpty(message = "Tenant ID is required")
    String tenantId,

    @NotNull(message = "Approval status is required")
    boolean approved,

    String rejectionReason,

    List<String> shopIdsToActivate,

    String adminComments
) {
    /**
     * Create an approval request for all shops
     */
    public static TenantActivationRequest approve(String tenantId, List<String> shopIds) {
        return new TenantActivationRequest(tenantId, true, null, shopIds, null);
    }

    /**
     * Create a rejection request
     */
    public static TenantActivationRequest reject(String tenantId, String reason) {
        return new TenantActivationRequest(tenantId, false, reason, null, null);
    }
}