package com.princely.shopmanager.aggregator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for registering a tenant from local embedded installation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationRequest {

    @NotEmpty(message = "Tenant name is required")
    private String tenantName;

    @NotEmpty(message = "Tenant email is required")
    @Email(message = "Valid email is required")
    private String tenantEmail;

    private String companyRegistration;

    private String taxId;

    private String address;

    private String city;

    private String state;

    private String country;

    private String phoneNumber;

    @NotNull(message = "At least one shop is required")
    @Valid
    private List<ShopRegistrationDto> shops;
}