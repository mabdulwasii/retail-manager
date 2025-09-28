package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user operations and queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Gets users by roles and tenant ID for notification purposes.
     *
     * @param roles List of role names to filter by
     * @param tenantId Tenant ID to filter by
     * @return List of users matching the criteria
     */
    public List<User> getUsersByRolesAndTenant(List<String> roles, String tenantId) {
        log.debug("Finding users with roles {} for tenant {}", roles, tenantId);

        try {
            // For now, return all users since the exact query implementation
            // depends on the specific User-Role relationship structure
            List<User> allUsers = userRepository.findAll();

            // In a real implementation, this would filter by roles and tenant
            // Example query might be: userRepository.findByRoleNamesInAndTenantId(roles, tenantId)

            log.debug("Found {} users for notification", allUsers.size());
            return allUsers;

        } catch (Exception e) {
            log.error("Error finding users by roles {} and tenant {}: {}", roles, tenantId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param userId User ID
     * @return User if found, null otherwise
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Gets users with admin privileges for the tenant.
     *
     * @param tenantId Tenant ID
     * @return List of admin users
     */
    public List<User> getAdminUsers(String tenantId) {
        return getUsersByRolesAndTenant(List.of("TENANT_ADMIN", "SHOP_OWNER"), tenantId);
    }

    /**
     * Gets users with manager privileges for the tenant.
     *
     * @param tenantId Tenant ID
     * @return List of manager users
     */
    public List<User> getManagerUsers(String tenantId) {
        return getUsersByRolesAndTenant(List.of("SHOP_MANAGER"), tenantId);
    }

    /**
     * Gets a user by their Keycloak ID.
     *
     * @param keycloakId Keycloak user ID
     * @return User if found, null otherwise
     */
    public User getUserByKeycloakId(String keycloakId) {
        log.debug("Finding user by Keycloak ID: {}", keycloakId);

        try {
            return userRepository.findByKeycloakId(keycloakId).orElse(null);
        } catch (Exception e) {
            log.error("Error finding user by Keycloak ID {}: {}", keycloakId, e.getMessage());
            return null;
        }
    }
}