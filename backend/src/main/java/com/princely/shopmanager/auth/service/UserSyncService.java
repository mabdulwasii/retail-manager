package com.princely.shopmanager.auth.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
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
    private final ShopRepository shopRepository;

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

        // Get shop from database if shopId is provided
        Shop shop = null;
        if (principal.getShopId() != null && !principal.getShopId().trim().isEmpty()) {
            shop = shopRepository.findById(principal.getShopId()).orElse(null);
            if (shop == null) {
                log.warn("Shop {} not found for user {}. User will be created without shop assignment.",
                    principal.getShopId(), principal.getUsername());
            }
        } else {
            log.debug("No shop ID in JWT for user {}. User will be created without shop assignment.",
                principal.getUsername());
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
            .shop(shop)
            .status(User.UserStatus.ACTIVE)
            .roles(new HashSet<>())
            .build();

        // Sync roles from JWT
        syncRolesFromJwt(user, principal.getRoles());

        user = userRepository.save(user);
        log.info("Created new user in database: {} (Keycloak ID: {}, Shop: {})",
            user.getUsername(), user.getKeycloakId(), shop != null ? shop.getId() : "none");

        return user;
    }

    /**
     * Updates an existing user from JWT principal.
     */
    private User updateUserFromJwt(User user, JwtPrincipal principal) {
        boolean updated = false;

        updated |= updateBasicProfile(user, principal);
        updated |= updateName(user, principal);
        updated |= updatePhoneNumber(user, principal);
        updated |= updateStatus(user);
        updated |= syncRolesFromJwt(user, principal.getRoles());

        if (updated) {
            user = userRepository.save(user);
            log.debug("Updated user in database: {}", user.getUsername());
        } else {
            log.debug("User data is already up-to-date: {}", user.getUsername());
        }

        return user;
    }

    /**
     * Updates basic profile fields (email, username).
     * Validates email uniqueness before updating to prevent constraint violations.
     *
     * @throws UserSyncException if email update would violate uniqueness constraint
     */
    private boolean updateBasicProfile(User user, JwtPrincipal principal) {
        boolean updated = false;

        // Validate and update email
        if (!principal.getEmail().equals(user.getEmail())) {
            // Check if the new email is already taken by another user
            User existingUserWithEmail = userRepository.findByEmail(principal.getEmail()).orElse(null);

            if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(user.getId())) {
                // Log detailed technical information for administrators
                String technicalDetails = String.format(
                    "User sync failed - duplicate email detected. " +
                    "Attempting user: '%s' (Keycloak ID: %s, DB ID: %s), " +
                    "Email from Keycloak: '%s', " +
                    "Email already registered to: '%s' (DB ID: %s). " +
                    "Resolution required: (1) Update email in Keycloak to match database, " +
                    "(2) Remove duplicate user from Keycloak or database, or " +
                    "(3) Merge the user accounts if they represent the same person.",
                    user.getUsername(),
                    principal.getSubject(),
                    user.getId(),
                    principal.getEmail(),
                    existingUserWithEmail.getUsername(),
                    existingUserWithEmail.getId()
                );
                log.error(technicalDetails);

                // Return generic user-friendly message (no internal details)
                String userMessage = "Unable to complete authentication due to account configuration issues. " +
                    "Please contact your system administrator for assistance.";
                throw new UserSyncException(userMessage, null);
            }

            user.setEmail(principal.getEmail());
            updated = true;
        }

        // Update username
        if (!principal.getUsername().equals(user.getUsername())) {
            user.setUsername(principal.getUsername());
            updated = true;
        }

        return updated;
    }

    /**
     * Updates name fields (firstName, lastName).
     * Only updates if JWT has a value, or if DB value is null/empty.
     */
    private boolean updateName(User user, JwtPrincipal principal) {
        boolean updated = false;

        // Update firstName
        if (principal.getFirstName() != null && !principal.getFirstName().isEmpty()
            && !principal.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(principal.getFirstName());
            updated = true;
        } else if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            user.setFirstName(principal.getFirstName() != null ? principal.getFirstName() : "");
            updated = true;
        }

        // Update lastName
        if (principal.getLastName() != null && !principal.getLastName().isEmpty()
            && !principal.getLastName().equals(user.getLastName())) {
            user.setLastName(principal.getLastName());
            updated = true;
        } else if (user.getLastName() == null || user.getLastName().isEmpty()) {
            user.setLastName(principal.getLastName() != null ? principal.getLastName() : "");
            updated = true;
        }

        return updated;
    }

    /**
     * Updates phone number field.
     * Only updates if JWT has a value, or if DB value is null/empty/N/A.
     */
    private boolean updatePhoneNumber(User user, JwtPrincipal principal) {
        if (principal.getPhoneNumber() != null && !principal.getPhoneNumber().isEmpty()
            && !principal.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(principal.getPhoneNumber());
            return true;
        } else if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber("N/A");
            return true;
        }
        return false;
    }

    /**
     * Ensures user status is ACTIVE.
     */
    private boolean updateStatus(User user) {
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            user.setStatus(User.UserStatus.ACTIVE);
            return true;
        }
        return false;
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
