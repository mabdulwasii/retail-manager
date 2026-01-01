package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.repository.CloudSyncConfigRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing cloud sync configuration.
 * Handles CRUD operations for cloud tenant credentials and sync settings.
 */
@Service
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class CloudSyncConfigurationService {

    private final CloudSyncConfigRepository cloudSyncConfigRepository;
    private final TextEncryptor textEncryptor;

    /**
     * Get cloud sync configuration for a tenant
     */
    @Transactional(readOnly = true)
    public Optional<CloudSyncConfig> getConfigByTenantId(String tenantId) {
        return cloudSyncConfigRepository.findByTenantId(tenantId)
                .map(this::decryptApiKey);
    }

    /**
     * Get cloud sync configuration or throw exception
     * Note: Not @Transactional - participates in caller's transaction
     */
    public CloudSyncConfig getConfigByTenantIdOrThrow(String tenantId) {
        return cloudSyncConfigRepository.findByTenantId(tenantId)
                .map(this::decryptApiKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED));
    }

    /**
     * Check if cloud sync is configured for tenant
     */
    @Transactional(readOnly = true)
    public boolean isConfigured(String tenantId) {
        return cloudSyncConfigRepository.findByTenantId(tenantId)
                .map(CloudSyncConfig::isConfigured)
                .orElse(false);
    }

    /**
     * Check if cloud sync is active (enabled and configured) for tenant
     */
    @Transactional(readOnly = true)
    public boolean isActive(String tenantId) {
        return cloudSyncConfigRepository.findByTenantId(tenantId)
                .map(CloudSyncConfig::isActive)
                .orElse(false);
    }

    /**
     * Create or update cloud sync configuration
     */
    @Transactional
    public CloudSyncConfig saveConfiguration(CloudSyncConfig config) {
        log.info("Saving cloud sync configuration for tenant: {}", config.getTenantId());

        // Encrypt API key before saving
        if (config.getCloudApiKey() != null) {
            config.setCloudApiKey(textEncryptor.encrypt(config.getCloudApiKey()));
        }

        // Set status to CONFIGURED if credentials are provided
        if (config.getCloudTenantId() != null && config.getCloudApiKey() != null) {
            if (config.getSyncStatus() == CloudSyncConfig.SyncStatus.NOT_CONFIGURED) {
                config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);
            }
        }

        CloudSyncConfig saved = cloudSyncConfigRepository.save(config);
        log.info("Cloud sync configuration saved for tenant: {}", saved.getTenantId());

        return decryptApiKey(saved);
    }

    /**
     * Enable cloud sync for tenant
     */
    @Transactional
    public CloudSyncConfig enableSync(String tenantId) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);

        if (!config.isConfigured()) {
            throw new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);
        }

        config.setSyncEnabled(true);
        CloudSyncConfig saved = cloudSyncConfigRepository.save(config);
        log.info("Cloud sync enabled for tenant: {}", tenantId);

        return decryptApiKey(saved);
    }

    /**
     * Disable cloud sync for tenant
     */
    @Transactional
    public CloudSyncConfig disableSync(String tenantId) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);

        config.setSyncEnabled(false);
        CloudSyncConfig saved = cloudSyncConfigRepository.save(config);
        log.info("Cloud sync disabled for tenant: {}", tenantId);

        return decryptApiKey(saved);
    }

    /**
     * Update cloud API key
     */
    @Transactional
    public CloudSyncConfig updateApiKey(String tenantId, String newApiKey) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);

        config.setCloudApiKey(textEncryptor.encrypt(newApiKey));
        config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);

        CloudSyncConfig saved = cloudSyncConfigRepository.save(config);
        log.info("Cloud API key updated for tenant: {}", tenantId);

        return decryptApiKey(saved);
    }

    /**
     * Update cloud API URL
     */
    @Transactional
    public CloudSyncConfig updateApiUrl(String tenantId, String newApiUrl) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);

        config.setCloudApiUrl(newApiUrl);

        CloudSyncConfig saved = cloudSyncConfigRepository.save(config);
        log.info("Cloud API URL updated for tenant: {}", tenantId);

        return decryptApiKey(saved);
    }

    /**
     * Mark sync as successful
     */
    @Transactional
    public void markSyncSuccess(String tenantId) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);
        config.markSyncSuccess();
        cloudSyncConfigRepository.save(config);
        log.debug("Marked sync success for tenant: {}", tenantId);
    }

    /**
     * Mark sync as failed
     */
    @Transactional
    public void markSyncFailed(String tenantId, String errorMessage) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);
        config.markSyncFailed(errorMessage);
        cloudSyncConfigRepository.save(config);
        log.warn("Marked sync failed for tenant {}: {}", tenantId, errorMessage);
    }

    /**
     * Get all active cloud sync configurations
     */
    @Transactional(readOnly = true)
    public List<CloudSyncConfig> getAllActiveConfigurations() {
        return cloudSyncConfigRepository.findAllActive().stream()
                .map(this::decryptApiKey)
                .toList();
    }

    /**
     * Delete cloud sync configuration
     */
    @Transactional
    public void deleteConfiguration(String tenantId) {
        CloudSyncConfig config = getConfigByTenantIdOrThrow(tenantId);
        cloudSyncConfigRepository.delete(config);
        log.info("Cloud sync configuration deleted for tenant: {}", tenantId);
    }

    /**
     * Decrypt API key in config (for reading)
     */
    private CloudSyncConfig decryptApiKey(CloudSyncConfig config) {
        if (config.getCloudApiKey() != null) {
            try {
                config.setCloudApiKey(textEncryptor.decrypt(config.getCloudApiKey()));
            } catch (Exception e) {
                log.warn("Failed to decrypt API key for tenant: {}", config.getTenantId());
                config.setCloudApiKey(null); // Clear corrupted key
            }
        }
        return config;
    }
}
