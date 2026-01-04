package com.princely.shopmanager.aggregator.repository;

import com.princely.shopmanager.aggregator.domain.CloudApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CloudApiKey entity.
 * Manages API keys for cloud tenants.
 */
@Repository
public interface CloudApiKeyRepository extends JpaRepository<CloudApiKey, String> {

    /**
     * Find all API keys for a specific tenant.
     *
     * @param tenantId Tenant ID
     * @return List of API keys
     */
    List<CloudApiKey> findByTenantId(String tenantId);

    /**
     * Find active API keys for a specific tenant.
     *
     * @param tenantId Tenant ID
     * @param isActive Active status
     * @return List of active API keys
     */
    List<CloudApiKey> findByTenantIdAndIsActive(String tenantId, Boolean isActive);

    /**
     * Find API key by key prefix (for lookup and validation).
     *
     * @param keyPrefix First 8 characters of the API key
     * @return Optional CloudApiKey
     */
    Optional<CloudApiKey> findByKeyPrefix(String keyPrefix);

    /**
     * Check if a tenant already has an API key with the same description.
     *
     * @param tenantId Tenant ID
     * @param description Key description
     * @return True if exists
     */
    boolean existsByTenantIdAndDescription(String tenantId, String description);

    /**
     * Find an API key by tenant ID and key ID.
     *
     * @param tenantId Tenant ID
     * @param id API Key ID
     * @return Optional CloudApiKey
     */
    Optional<CloudApiKey> findByTenantIdAndId(String tenantId, String id);

    /**
     * Count active API keys for a tenant.
     *
     * @param tenantId Tenant ID
     * @param isActive Active status
     * @return Count of active keys
     */
    long countByTenantIdAndIsActive(String tenantId, Boolean isActive);
}
