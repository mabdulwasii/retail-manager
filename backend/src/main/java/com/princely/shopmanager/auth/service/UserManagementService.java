package com.princely.shopmanager.auth.service;

import com.princely.shopmanager.auth.dto.CreateKeycloakUserRequest;

/**
 * Interface for user management operations.
 *
 * <p>This abstraction allows different implementations for cloud (Keycloak-based)
 * and embedded (local database) deployments.
 */
public interface UserManagementService {

    /**
     * Generate a secure random password
     */
    String generatePassword();

    /**
     * Create a new user in the authentication system
     *
     * @param request User creation request
     * @return User ID in the authentication system
     */
    String createUser(CreateKeycloakUserRequest request);

    /**
     * Update user's enabled/disabled status
     *
     * @param userId User ID
     * @param enabled True to enable, false to disable
     */
    void updateUserStatus(String userId, boolean enabled);

    /**
     * Check if a user exists by email
     *
     * @param email Email to check
     * @return True if user exists
     */
    boolean userExistsByEmail(String email);

    /**
     * Check if a user exists by username
     *
     * @param username Username to check
     * @return True if user exists
     */
    boolean userExistsByUsername(String username);
}
