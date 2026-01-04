package com.princely.shopmanager.aggregator.repository;

import com.princely.shopmanager.aggregator.domain.CloudTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CloudTenant entity.
 */
@Repository
public interface CloudTenantRepository extends JpaRepository<CloudTenant, String> {

    /**
     * Find cloud tenant by email.
     *
     * @param tenantEmail Tenant email
     * @return Optional CloudTenant
     */
    Optional<CloudTenant> findByTenantEmail(String tenantEmail);

    /**
     * Check if tenant with email exists.
     *
     * @param tenantEmail Tenant email
     * @return true if exists
     */
    boolean existsByTenantEmail(String tenantEmail);

    /**
     * Find cloud tenant by API key hash (for authentication).
     *
     * @param apiKeyHash Hashed API key
     * @return Optional CloudTenant
     */
    Optional<CloudTenant> findByApiKeyHash(String apiKeyHash);

    /**
     * Find all tenants by status.
     *
     * @param status Tenant status
     * @return List of CloudTenant
     */
    java.util.List<CloudTenant> findByStatus(CloudTenant.Status status);
}