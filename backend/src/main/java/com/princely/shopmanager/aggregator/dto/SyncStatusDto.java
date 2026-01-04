package com.princely.shopmanager.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for shop sync status and history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusDto {

    private String shopId;
    private String shopName;
    private LocalDateTime lastSyncAt;
    private String lastSyncStatus;
    private Long daysSinceLastSync;
    private String syncHealth; // HEALTHY, WARNING, CRITICAL
}
