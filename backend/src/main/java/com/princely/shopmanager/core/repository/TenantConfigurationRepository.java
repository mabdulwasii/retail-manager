package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.TenantConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigurationRepository extends JpaRepository<TenantConfiguration, String> {

    /**
     * Find all configurations for a specific tenant.
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenant.id = :tenantId")
    List<TenantConfiguration> findByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find all active configurations for a specific tenant.
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenant.id = :tenantId AND tc.active = true")
    List<TenantConfiguration> findByTenantIdAndActiveTrue(@Param("tenantId") String tenantId);

    /**
     * Find all configurations for a specific tenant and category.
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenant.id = :tenantId AND tc.category = :category")
    List<TenantConfiguration> findByTenantIdAndCategory(
        @Param("tenantId") String tenantId,
        @Param("category") TenantConfiguration.ConfigCategory category
    );

    /**
     * Find a specific configuration by tenant and key.
     */
    @Query("SELECT tc FROM TenantConfiguration tc WHERE tc.tenant.id = :tenantId AND tc.key = :key")
    Optional<TenantConfiguration> findByTenantIdAndKey(
        @Param("tenantId") String tenantId,
        @Param("key") String key
    );

    /**
     * Check if a configuration key exists for a tenant.
     */
    @Query("SELECT CASE WHEN COUNT(tc) > 0 THEN true ELSE false END FROM TenantConfiguration tc " +
           "WHERE tc.tenant.id = :tenantId AND tc.key = :key")
    boolean existsByTenantIdAndKey(@Param("tenantId") String tenantId, @Param("key") String key);

    /**
     * Delete a configuration by tenant and key.
     */
    @Query("DELETE FROM TenantConfiguration tc WHERE tc.tenant.id = :tenantId AND tc.key = :key")
    void deleteByTenantIdAndKey(@Param("tenantId") String tenantId, @Param("key") String key);
}
