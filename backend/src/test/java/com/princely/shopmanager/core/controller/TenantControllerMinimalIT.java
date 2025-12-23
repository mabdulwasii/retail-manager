package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.TenantConfiguration;
import com.princely.shopmanager.core.dto.TenantConfigurationRequest;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Minimal integration test for TenantController - Happy Path Only.
 *
 * Covers all 10 TenantController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in TenantServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing all endpoints work end-to-end.
 * All tests use existing test-data.sql fixtures for optimal performance.
 *
 * ENABLED (10/10):
 * - POST /tenants/{tenantId}/users - Create user
 * - GET /tenants/{tenantId}/users - List users
 * - POST /tenants/{tenantId}/configurations - Create configuration
 * - GET /tenants/{tenantId}/configurations - List configurations
 * - GET /tenants/{tenantId}/configurations/category/{category} - Get by category
 * - GET /tenants/{tenantId}/configurations/{key} - Get by key
 * - PUT /tenants/{tenantId}/configurations/{key} - Update configuration
 * - PATCH /tenants/{tenantId}/configurations/{key} - Partial update
 * - DELETE /tenants/{tenantId}/configurations/{key} - Delete configuration
 * - POST /tenants/{tenantId}/configurations/bulk - Bulk upsert
 */
@Transactional
@DisplayName("Tenant Controller - Minimal Happy Path Integration Tests")
class TenantControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /tenants/{tenantId}/users - Should create user in tenant")
    void shouldCreateUserInTenant() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // Mock Keycloak user creation to return success
        when(keycloakUserService.createUser(any())).thenReturn("kc-new-user-id");

        UserCreateRequest request = UserCreateRequest.builder()
            .username("newuser" + System.currentTimeMillis())  // Unique username
            .email("newuser" + System.currentTimeMillis() + "@testretail.com")  // Unique email
            .password("Test@123456")
            .firstName("New")
            .lastName("User")
            .phoneNumber("+1234567890")  // Required field
            .shopId(TEST_SHOP_001)  // Required field
            .roles(Set.of("test-role-employee"))  // At least one role required
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPostWithShop(
            "/tenants/" + TEST_TENANT_001 + "/users",
            request,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("GET /tenants/{tenantId}/users - Should list tenant users")
    void shouldListTenantUsers() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/tenants/" + TEST_TENANT_001 + "/users",
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /tenants/{tenantId}/configurations - Should list all configurations")
    void shouldListAllConfigurations() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations",
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /tenants/{tenantId}/configurations/category/{category} - Should get by category")
    void shouldGetConfigurationsByCategory() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/category/BUSINESS",
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /tenants/{tenantId}/configurations/{key} - Should get configuration by key")
    void shouldGetConfigurationByKey() {
        // Given
        setTenantContext(TEST_TENANT_001);
        String configKey = "business.name";

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/" + configKey,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("POST /tenants/{tenantId}/configurations - Should create configuration")
    void shouldCreateConfiguration() {
        // Given
        setTenantContext(TEST_TENANT_001);
        TenantConfigurationRequest request = TenantConfigurationRequest.builder()
            .key("test.config")
            .value("test-value")
            .category(TenantConfiguration.ConfigCategory.BUSINESS)
            .description("Test configuration")
            .valueType(TenantConfiguration.ValueType.STRING)
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPostWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations",
            request,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("PUT /tenants/{tenantId}/configurations/{key} - Should update configuration")
    void shouldUpdateConfiguration() {
        // Given - Use existing configuration from test-data.sql
        setTenantContext(TEST_TENANT_001);
        String configKey = "business.name";
        TenantConfigurationRequest request = TenantConfigurationRequest.builder()
            .key(configKey)
            .value("Updated Business Name")
            .category(TenantConfiguration.ConfigCategory.BUSINESS)
            .valueType(TenantConfiguration.ValueType.STRING)
            .description("Updated business name")
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPutWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/" + configKey,
            request,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /tenants/{tenantId}/configurations/{key} - Should partial update configuration value")
    void shouldPartialUpdateConfigurationValue() {
        // Given - Use existing configuration from test-data.sql
        setTenantContext(TEST_TENANT_001);
        String configKey = "business.name";

        // When
        ResponseEntity<String> response = performAuthenticatedPatchWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/" + configKey,
            "\"New Value\"",
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("DELETE /tenants/{tenantId}/configurations/{key} - Should delete configuration")
    void shouldDeleteConfiguration() {
        // Given - First create a config, then delete it (isolation due to @DirtiesContext)
        setTenantContext(TEST_TENANT_001);

        // Create a deletable configuration
        TenantConfigurationRequest createRequest = TenantConfigurationRequest.builder()
            .key("deletable.config")
            .value("value-to-delete")
            .category(TenantConfiguration.ConfigCategory.BUSINESS)
            .valueType(TenantConfiguration.ValueType.STRING)
            .description("Config for deletion test")
            .build();

        performAuthenticatedPostWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations",
            createRequest,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // When - Delete it
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/deletable.config",
            "admin@testretail.com",
            TEST_SHOP_001,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("POST /tenants/{tenantId}/configurations/bulk - Should bulk upsert configurations")
    void shouldBulkUpsertConfigurations() {
        // Given
        setTenantContext(TEST_TENANT_001);
        List<TenantConfigurationRequest> requests = List.of(
            TenantConfigurationRequest.builder()
                .key("config1")
                .value("value1")
                .category(TenantConfiguration.ConfigCategory.BUSINESS)
                .valueType(TenantConfiguration.ValueType.STRING)
                .build(),
            TenantConfigurationRequest.builder()
                .key("config2")
                .value("value2")
                .category(TenantConfiguration.ConfigCategory.BUSINESS)
                .valueType(TenantConfiguration.ValueType.STRING)
                .build()
        );

        // When
        ResponseEntity<String> response = performAuthenticatedPostWithShop(
            "/tenants/" + TEST_TENANT_001 + "/configurations/bulk",
            requests,
            "admin@testretail.com",
            TEST_SHOP_001,
            String.class,
            "TENANT_ADMIN"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
