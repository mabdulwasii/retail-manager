package com.princely.shopmanager.shared.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for AuditLogController - Happy Path Only.
 */
@Transactional
@DisplayName("Audit Log Controller - Minimal Happy Path Integration Tests")
class AuditLogControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /api/shops/{shopId}/audit-logs - Should return 200 OK")
    void shouldGetAuditLogs() {
        // Given - Use existing shop from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/api/shops/" + TEST_SHOP_001 + "/audit-logs",
            "admin@testretail.com",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/shops/{shopId}/audit-logs - Should filter by action type")
    void shouldFilterByActionType() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/api/shops/" + TEST_SHOP_001 + "/audit-logs?actionType=CREATE",
            "admin@testretail.com",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/shops/{shopId}/audit-logs/export - Should export as CSV")
    void shouldExportAuditLogsAsCsv() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/api/shops/" + TEST_SHOP_001 + "/audit-logs/export",
            "admin@testretail.com",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).hasToString("text/csv");
    }

    @Test
    @DisplayName("GET /audit-logs/export - Should return 403 for EMPLOYEE role")
    void shouldReturnForbiddenForEmployeeExport() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/api/shops/" + TEST_SHOP_001 + "/audit-logs/export",
            "employee@testretail.com",
            String.class,
            "EMPLOYEE"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
