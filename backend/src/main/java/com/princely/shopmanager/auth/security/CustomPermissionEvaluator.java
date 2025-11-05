package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom PermissionEvaluator that validates user permissions from the database.
 *
 * This evaluator is invoked on every API call protected with @PreAuthorize("hasPermission(...)").
 * It performs real-time permission validation by:
 * 1. Extracting user email from JWT authentication
 * 2. Loading user's roles and permissions from database
 * 3. Checking if user has the required permission
 * 4. Caching results for performance (short TTL for security)
 *
 * Benefits:
 * - Real-time permission validation on every request
 * - No JWT modification needed
 * - Supports dynamic permission changes
 * - Proper separation of authentication vs authorization
 * - Works with existing permission matrix in database
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;

    /**
     * Evaluate if the authentication has the specified permission.
     *
     * @param authentication The Authentication object containing user identity
     * @param targetDomainObject Not used in this implementation (always null)
     * @param permission The permission name to check (e.g., "SHOP_CREATE")
     * @return true if user has the permission, false otherwise
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            log.debug("Authentication or permission is null");
            return false;
        }

        String permissionName = permission.toString();
        if (permissionName.isEmpty()) {
            log.debug("Permission name is empty");
            return false;
        }

        // Extract user email from JWT principal
        String userEmail = extractUserEmail(authentication);
        if (userEmail == null) {
            log.debug("Unable to extract user email from authentication");
            return false;
        }

        // Check if user has the required permission
        boolean hasPermission = checkUserPermission(userEmail, permissionName);

        log.debug("Permission check for user '{}' on permission '{}': {}",
            userEmail, permissionName, hasPermission);

        return hasPermission;
    }

    /**
     * Evaluate permission with target ID and type.
     * Not implemented - returns false.
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        log.debug("hasPermission with targetId not implemented, returning false");
        return false;
    }

    /**
     * Extract user email from authentication principal.
     */
    private String extractUserEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.getEmail();
        }

        if (principal instanceof String) {
            return (String) principal;
        }

        log.warn("Unknown principal type: {}", principal.getClass().getName());
        return null;
    }

    /**
     * Check if user has the specified permission by querying the database.
     *
     * Performance optimizations:
     * - Uses JOIN FETCH query to avoid N+1 problem (1 query instead of 3+)
     * - Caches ALL user permissions at once (not per-permission)
     * - Cache key is user email only (shared across all permission checks)
     * - Cache reduces 50+ entries per user to 1 entry per user
     *
     * @param userEmail User's email address
     * @param permissionName Permission name to check
     * @return true if user has permission, false otherwise
     */
    private boolean checkUserPermission(String userEmail, String permissionName) {
        try {
            // Load all user permissions (cached)
            Set<String> userPermissions = loadUserPermissions(userEmail);

            if (userPermissions.isEmpty()) {
                log.warn("Access denied for user '{}' - Required: '{}' - User has NO permissions or user not found",
                    userEmail, permissionName);
                return false;
            }

            boolean hasPermission = userPermissions.contains(permissionName);

            // Detailed logging for access denials (helps debugging)
            if (!hasPermission) {
                log.warn("Access denied for user '{}' - Required: '{}' - User has: {}",
                    userEmail, permissionName, userPermissions);
            } else {
                log.debug("Access granted for user '{}' - Permission: '{}'",
                    userEmail, permissionName);
            }

            return hasPermission;

        } catch (Exception e) {
            log.error("Error checking permission for user '{}' and permission '{}': {}",
                userEmail, permissionName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Load all permissions for a user from the database.
     *
     * Uses JOIN FETCH query to load User → Roles → Permissions in a single query.
     * Results are cached for 5 minutes per user (not per permission).
     *
     * Cache strategy:
     * - Key: user email
     * - TTL: 5 minutes
     * - Eviction: Manual via RoleService/PermissionService on permission changes
     * - Only cache non-empty results (empty = user not found or no permissions)
     *
     * @param userEmail User's email address
     * @return Set of permission names the user has
     */
    @Cacheable(value = "userPermissions", key = "#userEmail", unless = "#result.isEmpty()")
    private Set<String> loadUserPermissions(String userEmail) {
        // Use JOIN FETCH query to avoid N+1 problem
        Optional<User> userOptional = userRepository.findByEmailWithPermissions(userEmail);

        if (userOptional.isEmpty()) {
            log.debug("User not found in database: {}", userEmail);
            return Set.of();
        }

        User user = userOptional.get();
        Set<Role> roles = user.getRoles();

        if (roles == null || roles.isEmpty()) {
            log.debug("User '{}' has no roles", userEmail);
            return Set.of();
        }

        // Extract all permissions from all roles
        Set<String> permissions = roles.stream()
            .flatMap(role -> {
                Set<Permission> rolePermissions = role.getPermissions();
                if (rolePermissions == null) {
                    return Set.<String>of().stream();
                }
                return rolePermissions.stream().map(Permission::getName);
            })
            .collect(Collectors.toSet());

        log.debug("Loaded {} permissions for user '{}' - Roles: {}",
            permissions.size(),
            userEmail,
            roles.stream().map(Role::getName).collect(Collectors.toList()));

        return permissions;
    }
}
