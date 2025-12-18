package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for RoleController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Role Controller - Minimal Happy Path Integration Tests")
class RoleControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /roles - Should list all roles")
    void shouldListRoles() {
        // Given
        String tenantId = "tenant-role-list";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/roles",
            "owner",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /roles/{roleId} - Should get role by ID")
    void shouldGetRoleById() {
        // Given
        String tenantId = "tenant-role-get";
        setTenantContext(tenantId);
        var testData = setupTenantTestData(tenantId);
        String roleId = testData.get("testRole").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/roles/" + roleId,
            "owner",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /users/{userId}/roles - Should get user roles")
    void shouldGetUserRoles() {
        // Given
        String tenantId = "tenant-user-roles";
        setTenantContext(tenantId);
        var testData = setupTenantTestData(tenantId);
        String userId = testData.get("testUser").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/users/" + userId + "/roles",
            "owner",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
