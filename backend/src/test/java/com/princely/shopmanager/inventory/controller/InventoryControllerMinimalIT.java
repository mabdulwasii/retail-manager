package com.princely.shopmanager.inventory.controller;

import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for InventoryController - Happy Path Only.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Inventory Controller - Minimal Happy Path Integration Tests")
class InventoryControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops/{shopId}/inventory - Should create inventory")
    void shouldCreateInventory() {
        // Given - Use existing shop and product from test-data.sql (TestConstants.TEST_SHOP_001, PROD_WIRELESS_MOUSE)
        setTenantContext(TEST_TENANT_001);

        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(PROD_WIRELESS_MOUSE)
            .currentStock(100)
            .minimumStock(10)
            .costPrice(BigDecimal.valueOf(10.00))
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        // When
        ResponseEntity<String> response = performAuthenticatedPostWithShop(
            "/shops/" + TEST_SHOP_001 + "/inventory",
            request,
            "manager",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/inventory - Should list inventory")
    void shouldListInventory() {
        // Given - Use existing shop from test-data.sql (TestConstants.TEST_SHOP_001)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPaginationAndShop(
            "/shops/" + TEST_SHOP_001 + "/inventory",
            0,
            10,
            "manager",
            TEST_SHOP_001,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
