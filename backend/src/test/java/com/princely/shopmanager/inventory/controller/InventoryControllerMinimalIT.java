package com.princely.shopmanager.inventory.controller;

import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for InventoryController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Inventory Controller - Minimal Happy Path Integration Tests")
class InventoryControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops/{shopId}/inventory - Should create inventory")
    void shouldCreateInventory() {
        // Given
        String tenantId = "tenant-inv-create";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();
        String productId = createTestProduct(shopId, "Inventory Product");

        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(productId)
            .currentStock(100)
            .minimumStock(10)
            .costPrice(BigDecimal.valueOf(10.00))
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPost(
            "/shops/" + shopId + "/inventory",
            request,
            "manager",
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/inventory - Should list inventory")
    void shouldListInventory() {
        // Given
        String tenantId = "tenant-inv-list";
        setTenantContext(tenantId);
        Map<String, Object> testData = setupTenantTestData(tenantId);
        String shopId = testData.get("testShop").toString();

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPagination(
            "/shops/" + shopId + "/inventory",
            0,
            10,
            "manager",
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Helper method to create test product
    private String createTestProduct(String shopId, String name) {
        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(shopId)
            .name(name)
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
