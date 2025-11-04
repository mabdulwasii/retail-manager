package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity.
 * Provides data access methods for permission management.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    /**
     * Find a permission by its name.
     *
     * @param name Permission name (e.g., "PRODUCT_CREATE")
     * @return Optional containing the permission if found
     */
    Optional<Permission> findByName(String name);

    /**
     * Find all permissions for a specific resource.
     *
     * @param resource Resource name (e.g., "PRODUCT", "SALES")
     * @return List of permissions for the resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find all permissions by action.
     *
     * @param action Action name (e.g., "CREATE", "READ")
     * @return List of permissions with the action
     */
    List<Permission> findByAction(String action);
}
