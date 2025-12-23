package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for PermissionController - Happy Path Only.
 */
@Transactional
@DisplayName("Permission Controller - Minimal Happy Path Integration Tests")
class PermissionControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /permissions - Should list all permissions")
    void shouldListPermissions() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/permissions",
            "owner@testretail.com",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /permissions/grouped - Should list permissions grouped by resource")
    void shouldListPermissionsGrouped() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/permissions/grouped",
            "owner@testretail.com",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
