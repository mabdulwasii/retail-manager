package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for ProductController - Happy Path Only.
 *
 * This test validates that the product creation API works end-to-end with real HTTP requests.
 * Comprehensive business logic tests are in ProductServiceTest (unit tests).
 * Comprehensive RBAC and permission tests have been converted to unit tests.
 *
 * Purpose: Documentation test showing the API works.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "/test-data-empty.sql")
@DisplayName("Product Controller - Happy Path Integration Test")
class ProductControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should create product successfully with valid data")
    void shouldCreateProductSuccessfully() {
        // Given
        String tenantId = "test-tenant-product";
        setTenantContext(tenantId);
        var testData = setupTenantTestData(tenantId);

        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(testData.get("testShop").toString())
            .name("Coca Cola 500ml")
            .description("Refreshing cola drink")
            .barcode("5449000000996")
            .unit("bottle")
            .isTaxable(true)
            .isDiscountable(true)
            .build();

        // When
        ResponseEntity<ProductResponse> response = performAuthenticatedPost(
            "/shops/" + testData.get("testShop") + "/products",
            request,
            "shop-manager",
            ProductResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Coca Cola 500ml");
        assertThat(response.getBody().getBarcode()).isEqualTo("5449000000996");
        assertThat(response.getBody().getSku()).isNotNull().matches(".*-GEN-\\d{8}-[A-Z0-9]{4}");
        assertThat(response.getBody().getStatus()).isEqualTo(Product.ProductStatus.ACTIVE);
        assertThat(response.getBody().getShopId()).isEqualTo(testData.get("testShop").toString());
    }
}
