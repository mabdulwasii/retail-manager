package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    Optional<Tenant> findByName(String name);

    @Query("SELECT t FROM Tenant t WHERE t.status = :status")
    List<Tenant> findByStatus(@Param("status") Tenant.TenantStatus status);

    @Query("SELECT t FROM Tenant t WHERE t.contactEmail = :email")
    Optional<Tenant> findByContactEmail(@Param("email") String email);

    @Query("SELECT t FROM Tenant t WHERE t.taxId = :taxId")
    Optional<Tenant> findByTaxId(@Param("taxId") String taxId);

    @Query("SELECT COUNT(s) FROM Shop s WHERE s.tenant.id = :tenantId")
    Long countShopsByTenantId(@Param("tenantId") String tenantId);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByContactEmail(String contactEmail);

    boolean existsByTaxId(String taxId);
}