package com.princely.shopmanager.embedded.repository;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloudSyncConfigRepository extends JpaRepository<CloudSyncConfig, String> {

    /**
     * Find cloud sync config by tenant ID
     */
    Optional<CloudSyncConfig> findByTenantId(String tenantId);

    /**
     * Find cloud sync config by cloud tenant ID
     */
    Optional<CloudSyncConfig> findByCloudTenantId(String cloudTenantId);

    /**
     * Check if tenant has cloud sync configured
     */
    boolean existsByTenantId(String tenantId);

    /**
     * Find all enabled cloud sync configurations
     */
    @Query("SELECT c FROM CloudSyncConfig c WHERE c.syncEnabled = true")
    List<CloudSyncConfig> findAllEnabled();

    /**
     * Find all configurations with specific sync status
     */
    List<CloudSyncConfig> findBySyncStatus(CloudSyncConfig.SyncStatus syncStatus);

    /**
     * Find all active (enabled and configured) sync configurations
     */
    @Query("SELECT c FROM CloudSyncConfig c WHERE c.syncEnabled = true " +
           "AND c.syncStatus = 'CONFIGURED' " +
           "AND c.cloudTenantId IS NOT NULL " +
           "AND c.cloudApiKey IS NOT NULL")
    List<CloudSyncConfig> findAllActive();
}
