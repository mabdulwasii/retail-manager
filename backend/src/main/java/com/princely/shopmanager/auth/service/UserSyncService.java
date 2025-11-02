package com.princely.shopmanager.auth.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for synchronizing Keycloak users to the database.
 * Ensures users exist in the database upon login and profile access.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;

    /**
     * Syncs a user from Keycloak JWT to the database.
     * Creates the user if not exists, updates if exists.
     *
     * @param principal JWT principal from authentication
     * @return Synchronized user entity
     */
    @Transactional
    public User syncUserFromKeycloak(JwtPrincipal principal) {
        log.debug("Syncing user from Keycloak: {}", principal.getUsername());

        try {
            // Check if user already exists by Keycloak ID
            User user = userRepository.findByKeycloakId(principal.getSubject()).orElse(null);

            if (user != null) {
                // Update existing user
                log.debug("User found in database, updating: {}", user.getUsername());
                return updateUserFromJwt(user, principal);
            } else {
                // Create new user
                log.info("User not found in database, creating new user: {}", principal.getUsername());
                return createUserFromJwt(principal);
            }
        } catch (Exception e) {
            log.error("Error syncing user from Keycloak: {}", principal.getUsername(), e);
            throw new UserSyncException("Failed to sync user: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new user from JWT principal.
     */
    private User createUserFromJwt(JwtPrincipal principal) {
        // Get tenant from database
        Tenant tenant = getTenant(principal.getTenantId());

        if (tenant == null) {
            String error = String.format("Cannot create user %s: Tenant %s does not exist in database. " +
                "Please ensure the tenant exists before creating users.",
                principal.getUsername(), principal.getTenantId());
            log.error(error);
            throw new UserSyncException(error, null);
        }

        // Create user entity
        User user = User.builder()
            .keycloakId(principal.getSubject())
            .username(principal.getUsername())
            .email(principal.getEmail())
            .firstName(principal.getFirstName() != null ? principal.getFirstName() : "")
            .lastName(principal.getLastName() != null ? principal.getLastName() : "")
            .phoneNumber(principal.getPhoneNumber() != null ? principal.getPhoneNumber() : "N/A")
            .tenant(tenant)
            .status(User.UserStatus.ACTIVE)
            .isInvestor(false)
            .roles(new HashSet<>())
            .build();

        // Sync roles from JWT
        syncRolesFromJwt(user, principal.getRoles());

        user = userRepository.save(user);
        log.info("Created new user in database: {} (Keycloak ID: {})", user.getUsername(), user.getKeycloakId());

        return user;
    }

    /**
     * Updates an existing user from JWT principal.
     */
    private User updateUserFromJwt(User user, JwtPrincipal principal) {
        boolean updated = false;

        // Update basic profile information
        if (!principal.getEmail().equals(user.getEmail())) {
            user.setEmail(principal.getEmail());
            updated = true;
        }

        // Only update firstName if JWT has a value, or if DB value is null/empty
        if (principal.getFirstName() != null && !principal.getFirstName().isEmpty()
            && !principal.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(principal.getFirstName());
            updated = true;
        } else if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            user.setFirstName(principal.getFirstName() != null ? principal.getFirstName() : "");
            updated = true;
        }

        // Only update lastName if JWT has a value, or if DB value is null/empty
        if (principal.getLastName() != null && !principal.getLastName().isEmpty()
            && !principal.getLastName().equals(user.getLastName())) {
            user.setLastName(principal.getLastName());
            updated = true;
        } else if (user.getLastName() == null || user.getLastName().isEmpty()) {
            user.setLastName(principal.getLastName() != null ? principal.getLastName() : "");
            updated = true;
        }

        // Only update phoneNumber if JWT has a value, or if DB value is null/empty/N/A
        if (principal.getPhoneNumber() != null && !principal.getPhoneNumber().isEmpty()
            && !principal.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(principal.getPhoneNumber());
            updated = true;
        } else if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber("N/A");
            updated = true;
        }

        if (!principal.getUsername().equals(user.getUsername())) {
            user.setUsername(principal.getUsername());
            updated = true;
        }

        // Ensure user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            user.setStatus(User.UserStatus.ACTIVE);
            updated = true;
        }

        // Sync roles from JWT
        boolean rolesUpdated = syncRolesFromJwt(user, principal.getRoles());
        updated = updated || rolesUpdated;

        if (updated) {
            user = userRepository.save(user);
            log.debug("Updated user in database: {}", user.getUsername());
        } else {
            log.debug("User data is already up-to-date: {}", user.getUsername());
        }

        return user;
    }

    /**
     * Syncs roles from JWT to user entity.
     * Maps Keycloak role names to database Role entities.
     *
     * @return true if roles were updated, false otherwise
     */
    private boolean syncRolesFromJwt(User user, List<String> jwtRoles) {
        if (jwtRoles == null || jwtRoles.isEmpty()) {
            log.debug("No roles found in JWT for user: {}", user.getUsername());
            return false;
        }

        // Get existing roles from database that match JWT role names
        List<Role> dbRoles = roleRepository.findByNameIn(jwtRoles);

        Set<Role> currentRoles = new HashSet<>(user.getRoles());
        Set<Role> newRoles = new HashSet<>(dbRoles);

        // Check if roles have changed
        if (currentRoles.equals(newRoles)) {
            return false;
        }

        user.setRoles(newRoles);
        log.debug("Updated roles for user {}: {}", user.getUsername(),
            newRoles.stream().map(Role::getName).toList());

        return true;
    }

    /**
     * Gets a tenant by ID from the database.
     * Returns null if tenant doesn't exist.
     */
    private Tenant getTenant(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.warn("No tenant ID provided in JWT");
            return null;
        }

        return tenantRepository.findById(tenantId).orElse(null);
    }

    /**
     * Exception thrown when user sync fails.
     */
    public static class UserSyncException extends RuntimeException {
        public UserSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
