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
 * Minimal integration test for TenantAdminController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("TenantAdmin Controller - Minimal Happy Path Integration Tests")
class TenantAdminControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /admin/pending-registrations - Should list pending tenant registrations")
    void shouldListPendingRegistrations() {
        // Given - System admin context
        setTenantContext("system");
        setupTenantTestData("system");

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/admin/pending-registrations",
            "system-admin",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
