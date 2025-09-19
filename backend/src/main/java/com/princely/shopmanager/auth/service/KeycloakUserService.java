package com.princely.shopmanager.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import java.security.SecureRandom;
import java.util.*;

/**
 * Service for managing users in Keycloak using the admin client
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserService {

    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm:shop-manager}")
    private String realm;

    private RealmResource realmResource;

    @PostConstruct
    public void initialize() {
        this.realmResource = keycloakAdmin.realm(realm);
        log.info("KeycloakUserService initialized for realm: {}", realm);
    }

    /**
     * Create a new user in Keycloak
     */
    public String createUser(CreateKeycloakUserRequest request) {
        log.info("Creating user in Keycloak: {}", request.username());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(request.enabled());
        user.setEmailVerified(false);

        // Set user attributes for tenant information
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("tenantId", List.of(request.tenantId()));
        if (request.shopId() != null) {
            attributes.put("shopId", List.of(request.shopId()));
        }
        attributes.put("phoneNumber", List.of(request.phoneNumber()));
        user.setAttributes(attributes);

        try {
            UsersResource usersResource = realmResource.users();
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation().toString());
                log.info("User created successfully with ID: {}", userId);

                // Set password
                if (request.password() != null) {
                    setUserPassword(userId, request.password(), request.temporaryPassword());
                }

                // Assign roles
                if (request.roles() != null && !request.roles().isEmpty()) {
                    assignRolesToUser(userId, request.roles());
                }

                return userId;
            } else {
                throw new KeycloakUserException("Failed to create user. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", request.username(), e);
            throw new KeycloakUserException("Failed to create user: " + e.getMessage(), e);
        }
    }

    /**
     * Update user status (enable/disable)
     */
    public void updateUserStatus(String userId, boolean enabled) {
        log.info("Updating user status: {} to enabled: {}", userId, enabled);

        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(enabled);
            userResource.update(user);
            log.info("User status updated successfully: {}", userId);
        } catch (NotFoundException e) {
            throw new KeycloakUserException("User not found: " + userId, e);
        } catch (Exception e) {
            log.error("Error updating user status: {}", userId, e);
            throw new KeycloakUserException("Failed to update user status: " + e.getMessage(), e);
        }
    }

    /**
     * Assign roles to user
     */
    public void assignRolesToUser(String userId, List<String> roleNames) {
        log.info("Assigning roles to user {}: {}", userId, roleNames);

        try {
            UserResource userResource = realmResource.users().get(userId);
            List<RoleRepresentation> roles = new ArrayList<>();

            for (String roleName : roleNames) {
                try {
                    RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
                    roles.add(role);
                } catch (NotFoundException e) {
                    log.warn("Role not found: {}, skipping", roleName);
                }
            }

            if (!roles.isEmpty()) {
                userResource.roles().realmLevel().add(roles);
                log.info("Roles assigned successfully to user: {}", userId);
            }
        } catch (NotFoundException e) {
            throw new KeycloakUserException("User not found: " + userId, e);
        } catch (Exception e) {
            log.error("Error assigning roles to user: {}", userId, e);
            throw new KeycloakUserException("Failed to assign roles: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a secure random password
     */
    public String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Set user password
     */
    private void setUserPassword(String userId, String password, boolean temporary) {
        try {
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(temporary);

            userResource.resetPassword(credential);
            log.info("Password set for user: {}", userId);
        } catch (Exception e) {
            log.error("Error setting password for user: {}", userId, e);
            throw new KeycloakUserException("Failed to set password: " + e.getMessage(), e);
        }
    }

    /**
     * Extract user ID from location header
     */
    private String extractUserIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * Check if user exists by email
     */
    public boolean userExistsByEmail(String email) {
        try {
            List<UserRepresentation> users = realmResource.users().search(null, null, null, email, 0, 1);
            return !users.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if user exists by email: {}", email, e);
            return false;
        }
    }

    /**
     * Check if user exists by username
     */
    public boolean userExistsByUsername(String username) {
        try {
            List<UserRepresentation> users = realmResource.users().search(username, 0, 1);
            return !users.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if user exists by username: {}", username, e);
            return false;
        }
    }

    /**
     * Get user by ID
     */
    public Optional<UserRepresentation> getUserById(String userId) {
        try {
            UserRepresentation user = realmResource.users().get(userId).toRepresentation();
            return Optional.of(user);
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", userId, e);
            return Optional.empty();
        }
    }
}