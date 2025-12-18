package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
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
 * Minimal integration test for ShopController - Happy Path Only.
 *
 * Covers key ShopController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in ShopServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing endpoints work end-to-end.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Shop Controller - Minimal Happy Path Integration Tests")
class ShopControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops - Should create shop")
    void shouldCreateShop() {
        // Given
        String tenantId = "tenant-create-shop";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

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
        assertThat(response.getBody().getName()).isEqualTo("Test Shop");
    }

    @Test
    @DisplayName("GET /shops/{shopId} - Should get shop by ID")
    void shouldGetShopById() {
        // Given
        String tenantId = "tenant-get-shop";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedGet(
            "/shops/" + shopId,
            "manager",
            ShopResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(shopId);
    }

    @Test
    @DisplayName("GET /shops - Should list shops (paginated)")
    void shouldListShops() {
        // Given
        String tenantId = "tenant-list-shops";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

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
        // Given
        String tenantId = "tenant-active-shops";
        setTenantContext(tenantId);
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops/active",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PUT /shops/{shopId} - Should update shop")
    void shouldUpdateShop() {
        // Given
        String tenantId = "tenant-update-shop";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        ShopUpdateRequest request = createSampleShopUpdateRequest("Updated Description");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPut(
            "/shops/" + shopId,
            request,
            "manager",
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
        // Given
        String tenantId = "tenant-status-shop";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPatch(
            "/shops/" + shopId + "/status?status=SUSPENDED",
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
        // Given
        String tenantId = "tenant-users-shop";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops/" + shopId + "/users",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
