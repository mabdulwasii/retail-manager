package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful tenant registration.
 * Contains cloud tenant ID and API key for future sync operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationResponse {

    private String cloudTenantId;

    private String apiKey;

    private String message;

    private Integer registeredShopsCount;
}