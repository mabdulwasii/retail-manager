package com.princely.shopmanager.aggregator.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for creating a new API key.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiKeyRequest {

    @NotEmpty(message = "Tenant ID is required")
    private String tenantId;

    @NotEmpty(message = "Description is required")
    private String description;

    /**
     * Expiry in days. Null means never expires.
     */
    @Positive(message = "Expiry days must be positive")
    private Integer expiresInDays;

    @NotNull(message = "Permissions are required")
    @NotEmpty(message = "At least one permission is required")
    private Set<String> permissions;
}
