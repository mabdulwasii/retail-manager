package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name LIKE %:namePattern%")
    List<Role> findByNameContaining(@Param("namePattern") String namePattern);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNameIn(@Param("names") List<String> names);

    /**
     * Find all system roles and tenant-specific roles for a given tenant.
     * System roles (is_system = true) are available to all tenants.
     * Custom roles are tenant-specific and filtered by tenant_id.
     *
     * @param tenantId The tenant ID to filter custom roles
     * @return List of roles (system + tenant-specific)
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = true OR r.tenant.id = :tenantId")
    List<Role> findSystemAndTenantRoles(@Param("tenantId") String tenantId);

    /**
     * Find all system roles.
     *
     * @return List of system roles
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = true")
    List<Role> findSystemRoles();

    /**
     * Find tenant-specific (custom) roles.
     *
     * @param tenantId The tenant ID
     * @return List of custom roles for the tenant
     */
    @Query("SELECT r FROM Role r WHERE r.isSystem = false AND r.tenant.id = :tenantId")
    List<Role> findTenantRoles(@Param("tenantId") String tenantId);
}