package com.princely.shopmanager.embedded.sync.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to track cloud sync operations
 */
@Entity
@Table(name = "cloud_sync_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudSyncLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String syncBatchId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SyncType syncType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SyncStatus status;

    @Column(nullable = false)
    private LocalDateTime syncStartTime;

    @Column
    private LocalDateTime syncEndTime;

    @Column(nullable = false)
    private Integer recordsProcessed;

    @Column
    private Integer recordsSynced;

    @Column
    private Integer recordsFailed;

    @Column(length = 2000)
    private String errorMessage;

    @Column(length = 5000)
    private String errorDetails;

    @Column
    private Integer retryAttempt;

    @Column
    private Long durationMs;

    public enum SyncType {
        SALES_TRANSACTIONS,
        INVENTORY_UPDATES,
        PRODUCT_CHANGES,
        FULL_SYNC
    }

    public enum SyncStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        PARTIAL_SUCCESS
    }
}
