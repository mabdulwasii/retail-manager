package com.princely.shopmanager.embedded.dto;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for cloud sync configuration.
 * Used to expose configuration without sensitive data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudSyncConfigDto {

    private String tenantId;
    private String cloudTenantId;
    private String cloudApiUrl;
    private Boolean syncEnabled;
    private LocalDateTime lastSyncAt;
    private String syncStatus;
    private String lastError;
    private Boolean configured;

    public static CloudSyncConfigDto fromEntity(CloudSyncConfig config) {
        return CloudSyncConfigDto.builder()
                .tenantId(config.getTenantId())
                .cloudTenantId(config.getCloudTenantId())
                .cloudApiUrl(config.getCloudApiUrl())
                .syncEnabled(config.getSyncEnabled())
                .lastSyncAt(config.getLastSyncAt())
                .syncStatus(config.getSyncStatus().name())
                .lastError(config.getLastError())
                .configured(config.isConfigured())
                .build();
    }

    public static CloudSyncConfigDto notConfigured() {
        return CloudSyncConfigDto.builder()
                .configured(false)
                .syncEnabled(false)
                .syncStatus("NOT_CONFIGURED")
                .build();
    }
}
