package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful API key creation.
 * Contains full key (only shown once) and key metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiKeyResponse {

    private ApiKeyDto apiKey;

    /**
     * Full API key in plaintext.
     * This is the ONLY time the full key is returned.
     * Store it securely - it cannot be retrieved again.
     */
    private String fullKey;

    private String warning;
}
