package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
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
 * Minimal integration test for ProductController - Happy Path Only.
 *
 * Covers all 10 ProductController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in ProductServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing all endpoints work end-to-end.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Product Controller - Minimal Happy Path Integration Tests")
class ProductControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops/{shopId}/products - Should create product")
    void shouldCreateProduct() {
        // Given
        String tenantId = "tenant-create";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(shopId)
            .name("Coca Cola 500ml")
            .barcode("5449000000996")
            .unit("bottle")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPost(
            "/shops/" + shopId + "/products",
            request,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Coca Cola 500ml");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products - Should list products")
    void shouldListProducts() {
        // Given
        String tenantId = "tenant-list";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPagination(
            "/shops/" + shopId + "/products",
            0,
            10,
            "manager",
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"content\"");
    }

    @Test
    @DisplayName("GET /products/{productId} - Should get product by ID")
    void shouldGetProductById() {
        // Given
        String tenantId = "tenant-get";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String productId = createTestProduct(testData.get("testShop").toString(), "Test Product");

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedGet(
            "/products/" + productId,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("GET /products/search - Should search products by barcode")
    void shouldSearchProductsByBarcode() {
        // Given
        String tenantId = "tenant-search";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();
        createTestProduct(shopId, "Search Product", "BARCODE123");

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedGet(
            "/products/search?barcode=BARCODE123&shopId=" + shopId,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getBarcode()).isEqualTo("BARCODE123");
    }

    @Test
    @DisplayName("PUT /products/{productId} - Should update product")
    void shouldUpdateProduct() {
        // Given
        String tenantId = "tenant-update";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String productId = createTestProduct(testData.get("testShop").toString(), "Original Name");

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .name("Updated Name")
            .description("Updated Description")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPut(
            "/products/" + productId,
            request,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("PATCH /products/{productId} - Should partial update product")
    void shouldPartialUpdateProduct() {
        // Given
        String tenantId = "tenant-patch";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String productId = createTestProduct(testData.get("testShop").toString(), "Patch Test");

        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .description("Patched Description")
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPatch(
            "/products/" + productId,
            request,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDescription()).isEqualTo("Patched Description");
    }

    @Test
    @DisplayName("DELETE /products/{productId} - Should delete product")
    void shouldDeleteProduct() {
        // Given
        String tenantId = "tenant-delete";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String productId = createTestProduct(testData.get("testShop").toString(), "Delete Me");

        // When
        ResponseEntity<Void> response = performAuthenticatedDelete(
            "/products/" + productId,
            "manager",
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /products/{productId}/inventory-summary - Should get inventory summary")
    void shouldGetInventorySummary() {
        // Given
        String tenantId = "tenant-inventory";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String productId = createTestProduct(testData.get("testShop").toString(), "Inventory Test");

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/products/" + productId + "/inventory-summary",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("totalStock");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products/low-stock - Should get low stock products")
    void shouldGetLowStockProducts() {
        // Given
        String tenantId = "tenant-lowstock";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops/" + shopId + "/products/low-stock",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/products/out-of-stock - Should get out of stock products")
    void shouldGetOutOfStockProducts() {
        // Given
        String tenantId = "tenant-outofstock";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops/" + shopId + "/products/out-of-stock",
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Helper method to create test product
    private String createTestProduct(String shopId, String name) {
        return createTestProduct(shopId, name, null);
    }

    private String createTestProduct(String shopId, String name, String barcode) {
        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(shopId)
            .name(name)
            .barcode(barcode)
            .unit("piece")
            .build();

        ResponseEntity<ProductResponse> response = performAuthenticatedPost(
            "/shops/" + shopId + "/products",
            request,
            "manager",
            ProductResponse.class,
            "MANAGER"
        );

        return response.getBody().getId();
    }
}
