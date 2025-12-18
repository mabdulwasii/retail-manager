package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for UserController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("User Controller - Minimal Happy Path Integration Tests")
class UserControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /profile - Should get user profile")
    void shouldGetUserProfile() {
        // Given
        String tenantId = "tenant-profile";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/users/profile",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /users/{userId} - Should get user by ID")
    void shouldGetUserById() {
        // Given
        String tenantId = "tenant-user-get";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String userId = testData.get("testUser").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/users/" + userId,
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /users - Should list users")
    void shouldListUsers() {
        // Given
        String tenantId = "tenant-user-list";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/users",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
