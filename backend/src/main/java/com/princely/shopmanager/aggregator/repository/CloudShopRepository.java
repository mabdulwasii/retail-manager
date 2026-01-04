package com.princely.shopmanager.aggregator.repository;

import com.princely.shopmanager.aggregator.domain.CloudShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CloudShop entity.
 */
@Repository
public interface CloudShopRepository extends JpaRepository<CloudShop, String> {

    /**
     * Find all shops for a specific cloud tenant.
     *
     * @param cloudTenantId Cloud tenant ID
     * @return List of CloudShop
     */
    List<CloudShop> findByCloudTenantId(String cloudTenantId);

    /**
     * Count shops for a specific cloud tenant.
     *
     * @param cloudTenantId Cloud tenant ID
     * @return Shop count
     */
    long countByCloudTenantId(String cloudTenantId);

    /**
     * Find shops by email.
     *
     * @param shopEmail Shop email
     * @return List of CloudShop
     */
    List<CloudShop> findByShopEmail(String shopEmail);

    /**
     * Find all shops for a cloud tenant (alternative method name).
     *
     * @param tenantId Cloud tenant ID
     * @return List of CloudShop
     */
    List<CloudShop> findByCloudTenant_Id(String tenantId);
}