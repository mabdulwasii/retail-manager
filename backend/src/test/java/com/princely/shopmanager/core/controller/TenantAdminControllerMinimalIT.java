package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

/**
 * Minimal integration test for TenantAdminController - Happy Path Only.
 *
 * Covers all 3 TenantAdminController endpoints with simple happy-path tests.
 * System admin operations for tenant registration approval workflow.
 */
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TenantAdmin Controller - Minimal Happy Path Integration Tests")
class TenantAdminControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @Order(1)
    @DisplayName("GET /admin/tenants/pending - Should list pending tenant registrations")
    void shouldListPendingRegistrations() {
        // Given - System admin context (no specific tenant)
        setTenantContext("system");

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/admin/tenants/pending",
            "system-admin",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Pending Tenant Corp");
    }

    @Test
    @Order(2)
    @DisplayName("GET /admin/tenants/{tenantId} - Should get tenant details")
    void shouldGetTenantDetails() {
        // Given - Use existing pending tenant from test-data.sql (TestConstants.TEST_TENANT_003)
        setTenantContext("system");

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/admin/tenants/" + TEST_TENANT_003,
            "system-admin",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Pending Tenant Corp");
    }

    @Test
    @Order(3)
    @DisplayName("POST /admin/tenants/{tenantId}/activate - Should activate pending tenant")
    void shouldActivateTenant() {
        // Given - Use existing pending tenant from test-data.sql (TestConstants.TEST_TENANT_003)
        setTenantContext("system");

        // Mock KeycloakUserService to avoid requiring actual Keycloak connection
        doNothing().when(keycloakUserService).updateUserStatus(anyString(), anyBoolean());

        TenantActivationRequest request = TenantActivationRequest.approve(
            TEST_TENANT_003,
            List.of(TEST_SHOP_004)
        );

        // When
        ResponseEntity<String> response = performAuthenticatedPost(
            "/admin/tenants/" + TEST_TENANT_003 + "/activate",
            request,
            "system-admin",
            String.class,
            "SYSTEM_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("approved");
    }
}
