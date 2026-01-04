package com.princely.shopmanager.embedded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for cloud sync status information.
 * Used to display sync status in the UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudSyncStatusDto {

    private Boolean configured;
    private Boolean active;
    private String status;
    private String message;
    private LocalDateTime lastSyncAt;
    private String lastError;
    private String cloudApiUrl;
}
