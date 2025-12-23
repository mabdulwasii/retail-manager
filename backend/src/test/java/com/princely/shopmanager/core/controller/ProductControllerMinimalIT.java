package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for ProductController - Happy Path Only.
 * Covers all 10 ProductController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in ProductServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 * Purpose: API documentation showing all endpoints work end-to-end.
 * All tests use existing test-data.sql fixtures for optimal performance.

 * ENABLED (10/10):
 * - POST /shops/{shopId}/products - Create product
 * - GET /shops/{shopId}/products - List products
 * - GET /products/{productId} - Get by ID
 * - GET /products/search - Search by barcode
 * - PUT /products/{productId} - Update product
 * - PATCH /products/{productId} - Partial update
 * - DELETE /products/{productId} - Delete product
 * - GET /products/{productId}/inventory-summary - Inventory summary
 * - GET /shops/{shopId}/products/low-stock - Low stock products
 * - GET /shops/{shopId}/products/out-of-stock - Out of stock products
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Product Controller - Minimal Happy Path Integration Tests")
class ProductControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops/{shopId}/products - Should create product")
    void shouldCreateProduct() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .name("Coca Cola 500ml")
            .barcode("5449000000996")
            .unit("bottle")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPostWithShop(
            "/shops/" + TEST_SHOP_001 + "/products",
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            ProductResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Coca Cola 500ml");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products - Should list products")
    void shouldListProducts() {
        // Given - Use existing shop from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPaginationAndShop(
            "/shops/" + TEST_SHOP_001 + "/products",
            0,
            20,
            "owner@testretail.com",
            TEST_SHOP_001,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Wireless Mouse");
    }

    @Test
    @DisplayName("GET /products/{productId} - Should get product by ID")
    void shouldGetProductById() {
        // Given - Use existing product from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedGetWithShop(
            "/products/" + PROD_WIRELESS_MOUSE,
            "owner@testretail.com",
            TEST_SHOP_001,
            ProductResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getId()).isEqualTo(PROD_WIRELESS_MOUSE);
        assertThat(response.getBody().getName()).contains("Wireless Mouse");
    }

    @Test
    @DisplayName("GET /products/search - Should search products by barcode")
    void shouldSearchProductsByBarcode() {
        // Given - Use existing product from test-data.sql (TestConstants.BARCODE_WIRELESS_MOUSE)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedGetWithShop(
            "/products/search?barcode=" + BARCODE_WIRELESS_MOUSE + "&shopId=" + TEST_SHOP_001,
            "owner@testretail.com",
            TEST_SHOP_001,
            ProductResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getBarcode()).isEqualTo(BARCODE_WIRELESS_MOUSE);
    }

    @Test
    @DisplayName("PUT /products/{productId} - Should update product")
    void shouldUpdateProduct() {
        // Given - Use existing product from test-data.sql (TestConstants.PROD_USB_KEYBOARD)
        setTenantContext(TEST_TENANT_001);

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .name("Updated Keyboard Name")
            .description("Updated Description")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPutWithShop(
            "/products/" + PROD_USB_KEYBOARD,
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            ProductResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getName()).isEqualTo("Updated Keyboard Name");
    }

    @Test
    @DisplayName("PATCH /products/{productId} - Should partial update product")
    void shouldPartialUpdateProduct() {
        // Given - Use existing product from test-data.sql (TestConstants.PROD_COTTON_TSHIRT)
        setTenantContext(TEST_TENANT_001);

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .description("Patched Description for T-Shirt")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPatchWithShop(
            "/products/" + PROD_COTTON_TSHIRT,
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            ProductResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().getDescription()).isEqualTo("Patched Description for T-Shirt");
    }

    @Test
    @DisplayName("DELETE /products/{productId} - Should delete product")
    void shouldDeleteProduct() {
        // Given - Use existing product from test-data.sql (TestConstants.PROD_ENERGY_DRINK)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/products/" + PROD_ENERGY_DRINK,
            "owner@testretail.com",
            TEST_SHOP_001,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /products/{productId}/inventory-summary - Should get inventory summary")
    void shouldGetInventorySummary() {
        // Given - Use existing product from test-data.sql (TestConstants.PROD_WIRELESS_MOUSE)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/products/" + PROD_WIRELESS_MOUSE + "/inventory-summary",
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("totalStock");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products/low-stock - Should get low stock products")
    void shouldGetLowStockProducts() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/products/low-stock",
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products/out-of-stock - Should get out of stock products")
    void shouldGetOutOfStockProducts() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/products/out-of-stock",
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
