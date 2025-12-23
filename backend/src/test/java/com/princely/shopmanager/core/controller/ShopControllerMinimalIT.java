package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.ShopConfigurationRequest;
import com.princely.shopmanager.core.dto.ShopConfigurationResponse;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for ShopController - Happy Path Only.
 *
 * Covers key ShopController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in ShopServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing endpoints work end-to-end.
 */
@Transactional
@DisplayName("Shop Controller - Minimal Happy Path Integration Tests")
class ShopControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops - Should create shop")
    void shouldCreateShop() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        ShopCreateRequest request = createSampleShopCreateRequest("Test Shop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops",
            request,
            "owner",
            ShopResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).startsWith("Test Shop");
    }

    @Test
    @DisplayName("GET /shops/{shopId} - Should get shop by ID")
    void shouldGetShopById() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001 - Downtown Store)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001,
            "manager",
            TEST_SHOP_001,
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(TEST_SHOP_001);
    }

    @Test
    @DisplayName("GET /shops - Should list shops (paginated)")
    void shouldListShops() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPagination(
            "/shops",
            0,
            10,
            "owner",
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\"");
    }

    @Test
    @DisplayName("GET /shops/active - Should get active shops")
    void shouldGetActiveShops() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/active",
            "manager",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PUT /shops/{shopId} - Should update shop")
    void shouldUpdateShop() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        ShopUpdateRequest request = createSampleShopUpdateRequest("Updated Description");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPutWithShop(
            "/shops/" + TEST_SHOP_001,
            request,
            "manager",
            TEST_SHOP_001,
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/status - Should change shop status")
    void shouldChangeShopStatus() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_002 - Uptown Branch)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPatch(
            "/shops/" + TEST_SHOP_002 + "/status?status=SUSPENDED",
            null,
            "owner",
            ShopResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/users - Should get shop users")
    void shouldGetShopUsers() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/users",
            "manager",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /shops/all - Should list all shops")
    void shouldListAllShops() {
        // Given - Use existing tenant from test-data.sql (TestConstants.TEST_TENANT_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPagination(
            "/shops/all",
            0,
            10,
            "owner",
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\"");
    }

    @Test
    @DisplayName("PATCH /shops/{shopId} - Should partial update shop")
    void shouldPatchShop() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        ShopUpdateRequest request = ShopUpdateRequest.builder()
            .description("Patched Description Only")
            .build();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPatchWithShop(
            "/shops/" + TEST_SHOP_001,
            request,
            "manager",
            TEST_SHOP_001,
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDescription()).isEqualTo("Patched Description Only");
    }

    @Test
    @DisplayName("DELETE /shops/{shopId} - Should delete shop")
    void shouldDeleteShop() {
        // Given - Use existing deletable shop from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When - Delete the existing deletable shop
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/shops/" + SHOP_DELETABLE,
            "owner",
            SHOP_DELETABLE,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/configuration - Should get shop configuration")
    void shouldGetShopConfiguration() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<ShopConfigurationResponse> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/configuration",
            "manager",
            TEST_SHOP_001,
            ShopConfigurationResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("PUT /shops/{shopId}/configuration - Should update shop configuration")
    void shouldUpdateShopConfiguration() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        ShopConfigurationRequest request = ShopConfigurationRequest.builder()
            .investmentEnabled(false)
            .analyticsEnabled(true)
            .fraudDetectionEnabled(true)
            .autoBackupEnabled(false)
            .build();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPutWithShop(
            "/shops/" + TEST_SHOP_001 + "/configuration",
            request,
            "manager",
            TEST_SHOP_001,
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getConfiguration().isInvestmentEnabled()).isFalse();
        assertThat(response.getBody().getConfiguration().isAnalyticsEnabled()).isTrue();
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/configuration - Should partial update shop configuration")
    void shouldPatchShopConfiguration() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        ShopConfigurationRequest request = ShopConfigurationRequest.builder()
            .fraudDetectionEnabled(true)
            .build();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPatchWithShop(
            "/shops/" + TEST_SHOP_001 + "/configuration",
            request,
            "manager",
            TEST_SHOP_001,
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getConfiguration().isFraudDetectionEnabled()).isTrue();
    }
}
