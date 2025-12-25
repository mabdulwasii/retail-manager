package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service for validating transactions against cloud sync requirements.
 * Enforces cloud sync configuration before allowing transactions in embedded mode.
 */
@Service
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class TransactionValidationService {

    private final CloudSyncConfigurationService cloudSyncConfigurationService;

    @Value("${application.cloud.sync-required:true}")
    private boolean cloudSyncRequired;

    @Value("${application.cloud.allow-offline-mode:false}")
    private boolean allowOfflineMode;

    /**
     * Validate that cloud sync is configured before allowing a transaction
     *
     * @param tenantId Tenant ID performing the transaction
     * @throws BusinessException if cloud sync is required but not configured
     */
    public void validateCloudSyncForTransaction(String tenantId) {
        if (!cloudSyncRequired) {
            log.debug("Cloud sync not required, skipping validation for tenant: {}", tenantId);
            return;
        }

        // Check if cloud sync is configured
        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantId(tenantId)
                .orElse(null);

        if (config == null) {
            // Cloud sync not configured at all
            if (allowOfflineMode) {
                log.warn("Cloud sync not configured for tenant {}, but offline mode is allowed", tenantId);
                return;
            }

            throw new BusinessException(ErrorCode.CLOUD_SYNC_REQUIRED,
                    "Cloud sync must be configured before performing transactions. " +
                    "Please register with the cloud aggregator.");
        }

        if (!config.isConfigured()) {
            // Partially configured
            throw new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED,
                    "Cloud sync configuration is incomplete. " +
                    "Please complete the cloud aggregator setup.");
        }

        if (!config.getSyncEnabled()) {
            // Configured but disabled
            if (allowOfflineMode) {
                log.warn("Cloud sync is disabled for tenant {}, but offline mode is allowed", tenantId);
                return;
            }

            throw new BusinessException(ErrorCode.CLOUD_SYNC_REQUIRED,
                    "Cloud sync is currently disabled. " +
                    "Please enable cloud sync to perform transactions.");
        }

        // Check for error status
        if (config.getSyncStatus() == CloudSyncConfig.SyncStatus.ERROR) {
            log.warn("Cloud sync is in ERROR state for tenant {}: {}",
                    tenantId, config.getLastError());

            if (!allowOfflineMode) {
                throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                        "Cloud sync is experiencing errors. " +
                        "Please resolve sync issues before performing transactions. " +
                        "Error: " + config.getLastError());
            }
        }

        log.debug("Cloud sync validation passed for tenant: {}", tenantId);
    }

    /**
     * Validate cloud sync for multiple transactions
     */
    public void validateCloudSyncForBatch(String tenantId, int transactionCount) {
        validateCloudSyncForTransaction(tenantId);
        log.debug("Cloud sync validation passed for {} transactions from tenant: {}",
                transactionCount, tenantId);
    }

    /**
     * Check if cloud sync is configured for tenant (non-throwing)
     */
    public boolean isCloudSyncConfigured(String tenantId) {
        return cloudSyncConfigurationService.isConfigured(tenantId);
    }

    /**
     * Check if cloud sync is active for tenant (non-throwing)
     */
    public boolean isCloudSyncActive(String tenantId) {
        return cloudSyncConfigurationService.isActive(tenantId);
    }

    /**
     * Get cloud sync status summary for tenant
     */
    public CloudSyncStatus getCloudSyncStatus(String tenantId) {
        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantId(tenantId)
                .orElse(null);

        if (config == null) {
            return new CloudSyncStatus(false, false, "NOT_CONFIGURED",
                    "Cloud sync has not been set up");
        }

        boolean configured = config.isConfigured();
        boolean active = config.isActive();
        String status = config.getSyncStatus().name();
        String message = getStatusMessage(config);

        return new CloudSyncStatus(configured, active, status, message);
    }

    /**
     * Get human-readable status message
     */
    private String getStatusMessage(CloudSyncConfig config) {
        return switch (config.getSyncStatus()) {
            case NOT_CONFIGURED -> "Cloud sync setup is incomplete";
            case CONFIGURED -> config.getSyncEnabled()
                    ? "Cloud sync is active and ready"
                    : "Cloud sync is configured but disabled";
            case SYNCING -> "Cloud sync is currently in progress";
            case ERROR -> "Cloud sync error: " + config.getLastError();
        };
    }

    /**
     * Cloud sync status summary
     */
    public record CloudSyncStatus(
            boolean configured,
            boolean active,
            String status,
            String message
    ) {}
}
