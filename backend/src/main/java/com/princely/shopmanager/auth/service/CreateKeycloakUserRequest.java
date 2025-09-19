package com.princely.shopmanager.auth.service;

import java.util.List;

/**
 * Request for creating a user in Keycloak
 */
public record CreateKeycloakUserRequest(
    String username,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String tenantId,
    String shopId,
    String password,
    boolean temporaryPassword,
    boolean enabled,
    List<String> roles
) {
    public static CreateKeycloakUserRequest forTenantAdmin(
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String tenantId,
            String password) {
        return new CreateKeycloakUserRequest(
                username,
                email,
                firstName,
                lastName,
                phoneNumber,
                tenantId,
                null, // shopId not required for tenant admin
                password,
                true, // temporary password - user must change on first login
                false, // disabled by default until tenant is approved
                List.of("TENANT_ADMIN")
        );
    }

    public static CreateKeycloakUserRequest forSuperAdmin(
            String username,
            String email,
            String firstName,
            String lastName,
            String phoneNumber,
            String password) {
        return new CreateKeycloakUserRequest(
                username,
                email,
                firstName,
                lastName,
                phoneNumber,
                "SYSTEM", // system tenant
                null,
                password,
                true,
                true, // enabled immediately
                List.of("SUPER_ADMIN")
        );
    }
}