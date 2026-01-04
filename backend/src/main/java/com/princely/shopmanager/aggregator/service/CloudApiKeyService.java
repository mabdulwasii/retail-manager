package com.princely.shopmanager.aggregator.service;

import com.princely.shopmanager.aggregator.domain.CloudApiKey;
import com.princely.shopmanager.aggregator.domain.CloudTenant;
import com.princely.shopmanager.aggregator.dto.ApiKeyDto;
import com.princely.shopmanager.aggregator.dto.ApiKeyUsageStats;
import com.princely.shopmanager.aggregator.dto.CreateApiKeyRequest;
import com.princely.shopmanager.aggregator.dto.CreateApiKeyResponse;
import com.princely.shopmanager.aggregator.repository.CloudApiKeyRepository;
import com.princely.shopmanager.aggregator.repository.CloudTenantRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing cloud API keys.
 * Handles creation, validation, revocation, and usage tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudApiKeyService {

    private final CloudApiKeyRepository apiKeyRepository;
    private final CloudTenantRepository cloudTenantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new API key for a tenant.
     *
     * @param request API key creation request
     * @return CreateApiKeyResponse with full key (shown only once)
     */
    @Transactional
    public CreateApiKeyResponse createApiKey(CreateApiKeyRequest request) {
        log.info("Creating API key for tenant: {}", request.getTenantId());

        // Validate tenant exists
        CloudTenant tenant = cloudTenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Cloud tenant not found: " + request.getTenantId()));

        // Check if tenant already has a key with the same description
        if (apiKeyRepository.existsByTenantIdAndDescription(request.getTenantId(), request.getDescription())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "API key with description '" + request.getDescription() + "' already exists");
        }

        // Validate permissions
        validatePermissions(request.getPermissions());

        // Generate API key
        String fullKey = generateApiKey();
        String keyPrefix = fullKey.substring(0, 8);
        String keyHash = passwordEncoder.encode(fullKey);
        String maskedKey = maskApiKey(fullKey);

        // Calculate expiry date
        LocalDateTime expiresAt = request.getExpiresInDays() != null
                ? LocalDateTime.now().plusDays(request.getExpiresInDays())
                : null;

        // Create CloudApiKey entity
        CloudApiKey apiKey = CloudApiKey.builder()
                .tenantId(request.getTenantId())
                .keyPrefix(keyPrefix)
                .maskedKey(maskedKey)
                .keyHash(keyHash)
                .description(request.getDescription())
                .expiresAt(expiresAt)
                .isActive(true)
                .usageCount(0L)
                .permissions(request.getPermissions())
                .build();

        CloudApiKey savedKey = apiKeyRepository.save(apiKey);
        log.info("API key created with ID: {} for tenant: {}", savedKey.getId(), request.getTenantId());

        // Build response
        return CreateApiKeyResponse.builder()
                .apiKey(toDto(savedKey))
                .fullKey(fullKey)
                .warning("Store this key securely. It will not be shown again.")
                .build();
    }

    /**
     * List all API keys for a tenant.
     *
     * @param tenantId Tenant ID
     * @return List of API keys (without full key)
     */
    public List<ApiKeyDto> listApiKeys(String tenantId) {
        log.debug("Listing API keys for tenant: {}", tenantId);

        // Validate tenant exists
        if (!cloudTenantRepository.existsById(tenantId)) {
            throw new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                    "Cloud tenant not found: " + tenantId);
        }

        List<CloudApiKey> keys = apiKeyRepository.findByTenantId(tenantId);
        return keys.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Revoke an API key.
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     */
    @Transactional
    public void revokeApiKey(String tenantId, String keyId) {
        log.info("Revoking API key: {} for tenant: {}", keyId, tenantId);

        CloudApiKey apiKey = apiKeyRepository.findByTenantIdAndId(tenantId, keyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "API key not found: " + keyId));

        if (!Boolean.TRUE.equals(apiKey.getIsActive())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "API key is already revoked");
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
        log.info("API key revoked: {}", keyId);
    }

    /**
     * Regenerate an API key (revokes old, creates new with same permissions).
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @return CreateApiKeyResponse with new key
     */
    @Transactional
    public CreateApiKeyResponse regenerateApiKey(String tenantId, String keyId) {
        log.info("Regenerating API key: {} for tenant: {}", keyId, tenantId);

        CloudApiKey oldKey = apiKeyRepository.findByTenantIdAndId(tenantId, keyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "API key not found: " + keyId));

        // Revoke old key
        oldKey.revoke();
        apiKeyRepository.save(oldKey);

        // Create new key with same description and permissions
        CreateApiKeyRequest request = CreateApiKeyRequest.builder()
                .tenantId(tenantId)
                .description(oldKey.getDescription() + " (regenerated)")
                .expiresInDays(oldKey.getExpiresAt() != null
                        ? (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), oldKey.getExpiresAt())
                        : null)
                .permissions(oldKey.getPermissions())
                .build();

        return createApiKey(request);
    }

    /**
     * Get usage statistics for an API key.
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @return Usage statistics
     */
    public ApiKeyUsageStats getUsageStats(String tenantId, String keyId) {
        log.debug("Getting usage stats for API key: {}", keyId);

        CloudApiKey apiKey = apiKeyRepository.findByTenantIdAndId(tenantId, keyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "API key not found: " + keyId));

        // TODO: Implement detailed usage tracking with audit logs
        // For now, return basic stats from the entity
        return ApiKeyUsageStats.builder()
                .totalRequests(apiKey.getUsageCount())
                .last24Hours(0L)  // TODO: Query audit logs
                .last7Days(0L)    // TODO: Query audit logs
                .last30Days(0L)   // TODO: Query audit logs
                .lastUsedEndpoint(null)  // TODO: Query audit logs
                .lastUsedAt(apiKey.getLastUsedAt())
                .build();
    }

    /**
     * Validate an API key (for authentication).
     *
     * @param keyPrefix First 8 characters of the key
     * @param keyPlaintext Full key in plaintext
     * @return True if valid
     */
    public boolean validateApiKey(String keyPrefix, String keyPlaintext) {
        CloudApiKey apiKey = apiKeyRepository.findByKeyPrefix(keyPrefix)
                .orElse(null);

        if (apiKey == null) {
            return false;
        }

        // Check if key is valid (active and not expired)
        if (!apiKey.isValid()) {
            return false;
        }

        // Verify key hash
        return passwordEncoder.matches(keyPlaintext, apiKey.getKeyHash());
    }

    /**
     * Record usage of an API key.
     *
     * @param keyId API Key ID
     */
    @Transactional
    public void recordUsage(String keyId) {
        apiKeyRepository.findById(keyId).ifPresent(apiKey -> {
            apiKey.recordUsage();
            apiKeyRepository.save(apiKey);
        });
    }

    /**
     * Update API key description.
     *
     * @param tenantId Tenant ID
     * @param keyId API Key ID
     * @param description New description
     * @return Updated API key
     */
    @Transactional
    public ApiKeyDto updateApiKey(String tenantId, String keyId, String description) {
        log.info("Updating API key description: {} for tenant: {}", keyId, tenantId);

        CloudApiKey apiKey = apiKeyRepository.findByTenantIdAndId(tenantId, keyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND,
                        "API key not found: " + keyId));

        apiKey.setDescription(description);
        CloudApiKey updated = apiKeyRepository.save(apiKey);
        return toDto(updated);
    }

    // ==================== Helper Methods ====================

    /**
     * Generate a random API key (64 character hex string).
     *
     * @return Generated API key
     */
    private String generateApiKey() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Mask an API key for display.
     * Format: "a1b2c3d4...xyz9" (first 8 chars + "..." + last 4 chars)
     *
     * @param fullKey Full API key
     * @return Masked key
     */
    private String maskApiKey(String fullKey) {
        if (fullKey.length() < 12) {
            return fullKey;
        }
        String prefix = fullKey.substring(0, 8);
        String suffix = fullKey.substring(fullKey.length() - 4);
        return prefix + "..." + suffix;
    }

    /**
     * Validate permissions.
     *
     * @param permissions Set of permissions
     */
    private void validatePermissions(Set<String> permissions) {
        Set<String> validPermissions = Set.of(
                CloudApiKey.Permissions.READ,
                CloudApiKey.Permissions.WRITE,
                CloudApiKey.Permissions.DELETE,
                CloudApiKey.Permissions.SYNC,
                CloudApiKey.Permissions.ADMIN
        );

        for (String permission : permissions) {
            if (!validPermissions.contains(permission)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "Invalid permission: " + permission);
            }
        }
    }

    /**
     * Convert CloudApiKey entity to DTO.
     *
     * @param apiKey CloudApiKey entity
     * @return ApiKeyDto
     */
    private ApiKeyDto toDto(CloudApiKey apiKey) {
        return ApiKeyDto.builder()
                .id(apiKey.getId())
                .tenantId(apiKey.getTenantId())
                .keyPrefix(apiKey.getKeyPrefix())
                .maskedKey(apiKey.getMaskedKey())
                .description(apiKey.getDescription())
                .createdAt(apiKey.getCreatedAt())
                .lastUsedAt(apiKey.getLastUsedAt())
                .expiresAt(apiKey.getExpiresAt())
                .isActive(apiKey.getIsActive())
                .usageCount(apiKey.getUsageCount())
                .permissions(apiKey.getPermissions())
                .build();
    }
}
