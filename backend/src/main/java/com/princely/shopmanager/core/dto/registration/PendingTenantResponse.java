package com.princely.shopmanager.core.dto.registration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for pending tenant registrations (for super admin review)
 */
public record PendingTenantResponse(
    String tenantId,
    String tenantName,
    String description,
    String contactEmail,
    String contactUserName,
    String contactUserEmail,
    String contactPhone,
    String primaryAddress,
    String city,
    String state,
    String country,
    String postalCode,
    List<PendingShopInfo> shops,
    LocalDateTime submittedAt,
    String status,
    String companyRegistration,
    String taxId
) {
    public record PendingShopInfo(
        String shopId,
        String name,
        String description,
        String address,
        String city,
        String state,
        String country,
        String email,
        String phoneNumber
    ) {}
}