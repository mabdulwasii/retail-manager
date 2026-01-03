package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.aggregator.dto.ApiKeyDto;
import com.princely.shopmanager.aggregator.dto.ApiKeyUsageStats;
import com.princely.shopmanager.aggregator.dto.CreateApiKeyRequest;
import com.princely.shopmanager.aggregator.dto.CreateApiKeyResponse;
import com.princely.shopmanager.aggregator.service.CloudApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API Keys Controller.
 * Manages API keys for cloud tenants.
 */
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/api-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "API Keys", description = "Manage tenant API keys")
public class ApiKeysController {

    private final CloudApiKeyService cloudApiKeyService;

    /**
     * List all API keys for a tenant.
     *
     * GET /api/cloud/tenants/{tenantId}/api-keys
     *
     * @param tenantId Tenant ID
     * @return List of API keys
     */
    @GetMapping
    @Operation(summary = "List API keys",
            description = "Get all API keys for a tenant (without full keys)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved API keys"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<List<ApiKeyDto>> listApiKeys(@PathVariable String tenantId) {
        log.info("Listing API keys for tenant: {}", tenantId);
        List<ApiKeyDto> apiKeys = cloudApiKeyService.listApiKeys(tenantId);
        return ResponseEntity.ok(apiKeys);
    }

    /**
     * Create a new API key.
     *
     * POST /api/cloud/tenants/{tenantId}/api-keys
     *
     * @param tenantId Tenant ID
     * @param request API key creation request
     * @return Created API key with full key (shown only once)
     */
    @PostMapping
    @Operation(summary = "Create API key",
            description = "Create a new API key for a tenant. The full key is returned only once.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API key successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicate description"),
            @ApiResponse(responseCode = "404", description = "Tenant not found")
    })
    public ResponseEntity<CreateApiKeyResponse> createApiKey(
            @PathVariable String tenantId,
            @Valid @RequestBody CreateApiKeyRequest request) {

        log.info("Creating API key for tenant: {}", tenantId);

        // Ensure tenant ID in request matches path variable
        request.setTenantId(tenantId);

        CreateApiKeyResponse response = cloudApiKeyService.createApiKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Revoke an API key.
     *
     * DELETE /api/cloud/tenants/{tenantId}/api-keys/{keyId}
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @return Success response
     */
    @DeleteMapping("/{keyId}")
    @Operation(summary = "Revoke API key",
            description = "Revoke an API key. It cannot be used after revocation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "API key successfully revoked"),
            @ApiResponse(responseCode = "404", description = "API key not found"),
            @ApiResponse(responseCode = "400", description = "API key already revoked")
    })
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable String tenantId,
            @PathVariable String keyId) {

        log.info("Revoking API key: {} for tenant: {}", keyId, tenantId);
        cloudApiKeyService.revokeApiKey(tenantId, keyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Regenerate an API key.
     *
     * POST /api/cloud/tenants/{tenantId}/api-keys/{keyId}/regenerate
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @return New API key with full key (shown only once)
     */
    @PostMapping("/{keyId}/regenerate")
    @Operation(summary = "Regenerate API key",
            description = "Regenerate an API key. Old key is revoked, new key created with same permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "API key successfully regenerated"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<CreateApiKeyResponse> regenerateApiKey(
            @PathVariable String tenantId,
            @PathVariable String keyId) {

        log.info("Regenerating API key: {} for tenant: {}", keyId, tenantId);
        CreateApiKeyResponse response = cloudApiKeyService.regenerateApiKey(tenantId, keyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get usage statistics for an API key.
     *
     * GET /api/cloud/tenants/{tenantId}/api-keys/{keyId}/usage
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @return Usage statistics
     */
    @GetMapping("/{keyId}/usage")
    @Operation(summary = "Get API key usage statistics",
            description = "Get detailed usage statistics for an API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved usage stats"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyUsageStats> getUsageStats(
            @PathVariable String tenantId,
            @PathVariable String keyId) {

        log.debug("Getting usage stats for API key: {}", keyId);
        ApiKeyUsageStats stats = cloudApiKeyService.getUsageStats(tenantId, keyId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Update API key description.
     *
     * PATCH /api/cloud/tenants/{tenantId}/api-keys/{keyId}
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @param request Update request with new description
     * @return Updated API key
     */
    @PatchMapping("/{keyId}")
    @Operation(summary = "Update API key description",
            description = "Update the description of an API key")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API key successfully updated"),
            @ApiResponse(responseCode = "404", description = "API key not found")
    })
    public ResponseEntity<ApiKeyDto> updateApiKey(
            @PathVariable String tenantId,
            @PathVariable String keyId,
            @RequestBody UpdateApiKeyRequest request) {

        log.info("Updating API key: {} for tenant: {}", keyId, tenantId);
        ApiKeyDto updated = cloudApiKeyService.updateApiKey(tenantId, keyId, request.getDescription());
        return ResponseEntity.ok(updated);
    }

    /**
     * Simple request for updating API key description.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApiKeyRequest {
        private String description;
    }
}
