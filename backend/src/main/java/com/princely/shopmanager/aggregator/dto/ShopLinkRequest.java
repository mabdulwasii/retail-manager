package com.princely.shopmanager.aggregator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for linking an additional shop to an existing cloud tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopLinkRequest {

    @NotEmpty(message = "Cloud tenant ID is required")
    private String cloudTenantId;

    @NotNull(message = "Shop information is required")
    @Valid
    private ShopRegistrationDto shop;
}