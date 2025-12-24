package com.princely.shopmanager.embedded.sync.repository;

import com.princely.shopmanager.embedded.sync.domain.CloudSyncLog;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for cloud sync log operations
 */
@Repository
@Profile("embedded")
public interface CloudSyncLogRepository extends JpaRepository<CloudSyncLog, String> {

    /**
     * Find logs by store ID and status
     */
    List<CloudSyncLog> findByStoreIdAndStatus(String storeId, CloudSyncLog.SyncStatus status);

    /**
     * Find logs by store ID and sync type
     */
    List<CloudSyncLog> findByStoreIdAndSyncType(String storeId, CloudSyncLog.SyncType syncType);

    /**
     * Find latest successful sync
     */
    @Query("SELECT s FROM CloudSyncLog s WHERE s.storeId = :storeId " +
           "AND s.syncType = :syncType AND s.status = 'COMPLETED' " +
           "ORDER BY s.syncEndTime DESC LIMIT 1")
    Optional<CloudSyncLog> findLatestSuccessfulSync(
            @Param("storeId") String storeId,
            @Param("syncType") CloudSyncLog.SyncType syncType
    );

    /**
     * Find logs within date range
     */
    @Query("SELECT s FROM CloudSyncLog s WHERE s.storeId = :storeId " +
           "AND s.syncStartTime BETWEEN :startDate AND :endDate " +
           "ORDER BY s.syncStartTime DESC")
    List<CloudSyncLog> findLogsBetweenDates(
            @Param("storeId") String storeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find failed syncs that need retry
     */
    @Query("SELECT s FROM CloudSyncLog s WHERE s.storeId = :storeId " +
           "AND s.status = 'FAILED' AND s.retryAttempt < :maxRetries " +
           "ORDER BY s.syncStartTime ASC")
    List<CloudSyncLog> findFailedSyncsForRetry(
            @Param("storeId") String storeId,
            @Param("maxRetries") int maxRetries
    );

    /**
     * Count syncs by status
     */
    @Query("SELECT COUNT(s) FROM CloudSyncLog s WHERE s.storeId = :storeId " +
           "AND s.status = :status AND s.syncStartTime >= :since")
    long countByStatusSince(
            @Param("storeId") String storeId,
            @Param("status") CloudSyncLog.SyncStatus status,
            @Param("since") LocalDateTime since
    );
}
