package com.princely.shopmanager.embedded.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Cloud sync configuration entity for embedded mode.
 * Stores cloud tenant credentials and sync state for each local tenant.
 */
@Entity
@Table(name = "cloud_sync_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
public class CloudSyncConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false, unique = true)
    private String tenantId;

    @Column(name = "cloud_tenant_id")
    private String cloudTenantId;

    @Column(name = "cloud_api_key", length = 500)
    private String cloudApiKey;

    @Builder.Default
    @Column(name = "cloud_api_url", length = 500)
    private String cloudApiUrl = "https://cloud.shopmanager.com/api";

    @Builder.Default
    @Column(name = "sync_enabled")
    private Boolean syncEnabled = false;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 50)
    private SyncStatus syncStatus = SyncStatus.NOT_CONFIGURED;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public enum SyncStatus {
        NOT_CONFIGURED,
        CONFIGURED,
        SYNCING,
        ERROR
    }

    /**
     * Check if cloud sync is configured
     */
    public boolean isConfigured() {
        return cloudTenantId != null && cloudApiKey != null
                && syncStatus != SyncStatus.NOT_CONFIGURED;
    }

    /**
     * Check if cloud sync is enabled and configured
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(syncEnabled) && isConfigured();
    }

    /**
     * Mark sync as successful
     */
    public void markSyncSuccess() {
        this.lastSyncAt = LocalDateTime.now();
        this.syncStatus = SyncStatus.CONFIGURED;
        this.lastError = null;
    }

    /**
     * Mark sync as failed
     */
    public void markSyncFailed(String errorMessage) {
        this.syncStatus = SyncStatus.ERROR;
        this.lastError = errorMessage;
    }
}
