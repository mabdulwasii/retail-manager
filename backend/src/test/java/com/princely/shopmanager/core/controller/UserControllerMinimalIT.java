package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.UserResponse;
import com.princely.shopmanager.core.dto.UserShopTransferRequest;
import com.princely.shopmanager.core.dto.UserUpdateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for UserController - Happy Path Only.
 */
@Transactional
@DisplayName("User Controller - Minimal Happy Path Integration Tests")
class UserControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /profile - Should get user profile")
    void shouldGetUserProfile() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001, TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/users/profile",
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /users/{userId} - Should get user by ID")
    void shouldGetUserById() {
        // Given - Use existing user from test-data.sql (TestConstants.USER_MANAGER_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/users/" + USER_MANAGER_001,
            "manager",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /users - Should list users")
    void shouldListUsers() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001, TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/users",
            "manager",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /users/{userId} - Should update user")
    void shouldUpdateUser() {
        // Given - Use existing user from test-data.sql (TestConstants.USER_MANAGER_001)
        setTenantContext(TEST_TENANT_001);

        UserUpdateRequest request = UserUpdateRequest.builder()
            .firstName("Updated")
            .lastName("Manager")
            .phoneNumber("+1234567890")
            .build();

        // When
        ResponseEntity<UserResponse> response = performAuthenticatedPatchWithShop(
            "/users/" + USER_MANAGER_001,
            request,
            "owner",
            TEST_SHOP_001,
            UserResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFirstName()).isEqualTo("Updated");
        assertThat(response.getBody().getLastName()).isEqualTo("Manager");
    }

    @Test
    @DisplayName("PATCH /users/{userId}/transfer-shop - Should transfer user to different shop")
    void shouldTransferUserToShop() {
        // Given - Use existing user and shops from test-data.sql
        setTenantContext(TEST_TENANT_001);

        UserShopTransferRequest request = UserShopTransferRequest.builder()
            .newShopId(TEST_SHOP_002)
            .build();

        // When
        ResponseEntity<UserResponse> response = performAuthenticatedPatchWithShop(
            "/users/" + USER_EMPLOYEE_001 + "/transfer-shop",
            request,
            "owner",
            TEST_SHOP_001,
            UserResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getShopId()).isEqualTo(TEST_SHOP_002);
    }

    @Test
    @DisplayName("DELETE /users/{userId} - Should delete user")
    void shouldDeleteUser() {
        // Given - Use existing user from test-data.sql (TestConstants.USER_INVESTOR_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/users/" + USER_INVESTOR_001,
            "owner",
            TEST_SHOP_001,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
