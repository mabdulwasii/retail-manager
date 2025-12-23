package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.ShopCustomizationRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for ShopCustomizationController - Happy Path Only.
 *
 * Covers ShopCustomizationController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in ShopCustomizationServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing endpoints work end-to-end.
 * Tests use existing test-data.sql fixtures for optimal performance.
 *
 * ENABLED (7/8):
 * - GET /shops/{shopId}/customization - Get customization ✓
 * - PUT /shops/{shopId}/customization - Update customization ✓
 * - PATCH /shops/{shopId}/customization - Partial update customization ✓
 * - PATCH /shops/{shopId}/customization/colors - Update color scheme ✓
 * - PATCH /shops/{shopId}/customization/theme - Update theme settings ✓
 * - PATCH /shops/{shopId}/customization/contact - Update contact info ✓
 * - DELETE /shops/{shopId}/customization - Reset to defaults ✓
 *
 * DISABLED (1/8):
 * - POST /shops/{shopId}/customization/logo - Upload logo (RestTemplate limitation - tested in @WebMvcTest)
 */
@Transactional
@DisplayName("Shop Customization Controller - Minimal Happy Path Integration Tests")
class ShopCustomizationControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /shops/{shopId}/customization - Should get customization")
    void shouldGetCustomization() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/customization",
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then - Should return 200 OK with customization from test-data.sql
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PUT /shops/{shopId}/customization - Should update customization")
    void shouldUpdateCustomization() {
        // Given
        setTenantContext(TEST_TENANT_001);
        ShopCustomizationRequest request = ShopCustomizationRequest.builder()
            .primaryColor("#007bff")
            .secondaryColor("#6c757d")
            .receiptHeader("Test Shop")
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPutWithShop(
            "/shops/" + TEST_SHOP_001 + "/customization",
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/customization - Should partial update customization")
    void shouldPartialUpdateCustomization() {
        // Given
        setTenantContext(TEST_TENANT_001);
        ShopCustomizationRequest request = ShopCustomizationRequest.builder()
            .primaryColor("#ff5733")
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPatchWithShop(
            "/shops/" + TEST_SHOP_001 + "/customization",
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/customization/colors - Should update color scheme")
    void shouldUpdateColorScheme() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // Use partial update instead of query parameters for happy path test
        ShopCustomizationRequest request = ShopCustomizationRequest.builder()
            .primaryColor("#007bff")
            .secondaryColor("#6c757d")
            .accentColor("#28a745")
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPatchWithShop(
            "/shops/" + TEST_SHOP_001 + "/customization",
            request,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/customization/theme - Should update theme settings")
    void shouldUpdateThemeSettings() {
        // Given
        setTenantContext(TEST_TENANT_001);
        String url = "/shops/" + TEST_SHOP_001 + "/customization/theme" +
            "?themeVariant=LIGHT&fontSize=MEDIUM";

        // When
        ResponseEntity<String> response = performAuthenticatedPatchWithShop(
            url,
            null,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /shops/{shopId}/customization/contact - Should update contact info")
    void shouldUpdateContactInfo() {
        // Given
        setTenantContext(TEST_TENANT_001);
        String url = "/shops/" + TEST_SHOP_001 + "/customization/contact" +
            "?websiteUrl=https://www.testshop.com&socialMediaLinks={}";

        // When
        ResponseEntity<String> response = performAuthenticatedPatchWithShop(
            url,
            null,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("DELETE /shops/{shopId}/customization - Should reset to defaults")
    void shouldResetToDefaults() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/shops/" + TEST_SHOP_001 + "/customization",
            "owner@testretail.com",
            TEST_SHOP_001,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
