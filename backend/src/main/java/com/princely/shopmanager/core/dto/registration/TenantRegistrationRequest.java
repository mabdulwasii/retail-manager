package com.princely.shopmanager.core.dto.registration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Main tenant registration request containing all required information
 * for creating a new tenant, contact user, and initial shops
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @Valid
    @NotNull(message = "Tenant information is required")
    private TenantInfoRequest tenantInfo;

    @Valid
    @NotNull(message = "Contact user information is required")
    private ContactUserRequest contactUser;

    @Valid
    @NotEmpty(message = "At least one shop is required")
    @Size(min = 1, max = 10, message = "Must provide between 1 and 10 shops")
    private List<ShopInfoRequest> shops;

    @Builder.Default
    private String agreementVersion = "1.0";

    @Builder.Default
    private boolean termsAccepted = false;

    @Builder.Default
    private boolean privacyPolicyAccepted = false;
}