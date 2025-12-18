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
 * Minimal integration test for PermissionController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Permission Controller - Minimal Happy Path Integration Tests")
class PermissionControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /permissions - Should list all permissions")
    void shouldListPermissions() {
        // Given
        String tenantId = "tenant-perm-list";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/permissions",
            "owner",
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
