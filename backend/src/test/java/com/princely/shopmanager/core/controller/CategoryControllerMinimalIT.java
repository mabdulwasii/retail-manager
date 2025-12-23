package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.CategoryCreateRequest;
import com.princely.shopmanager.core.dto.CategoryResponse;
import com.princely.shopmanager.core.dto.CategoryUpdateRequest;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for CategoryController - Happy Path Only.
 *
 * Covers all 6 CategoryController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in CategoryServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing all endpoints work end-to-end.
 */
@Transactional
@DisplayName("Category Controller - Minimal Happy Path Integration Tests")
class CategoryControllerMinimalIT extends AbstractIntegrationTest {

    public static final String TEST_OWNER_EMAIL = "owner@testretail.com";

    @Test
    @DisplayName("POST /shops/{shopId}/categories - Should create category")
    void shouldCreateCategory() {
        // Given - Use existing shop from test-data.sql
        setTenantContext(TEST_TENANT_001);

        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .name("Test Category")
            .description("Test category description")
            .displayOrder(1)
            .isActive(true)
            .build();

        // When
        ResponseEntity<CategoryResponse> response = performAuthenticatedPostWithShop(
            "/shops/" + TEST_SHOP_001 + "/categories",
            request,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            CategoryResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Test Category");
    }

    @Test
    @DisplayName("GET /shops/{shopId}/categories - Should list categories")
    void shouldListCategories() {
        // Given - Use existing shop from test-data.sql (has categories in test-data.sql)
        setTenantContext(TEST_TENANT_001);

        // When - Request flat list (tree=false) to avoid hierarchical tree complexity
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/categories?tree=false",
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Electronics");
    }

    @Test
    @DisplayName("GET /categories/{id} - Should get category by ID")
    void shouldGetCategoryById() {
        // Given - Use existing category from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<CategoryResponse> response = performAuthenticatedGetWithShop(
            "/categories/" + CAT_ELECTRONICS,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            CategoryResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("PUT /categories/{id} - Should update category")
    void shouldUpdateCategory() {
        // Given - Use existing category from test-data.sql
        setTenantContext(TEST_TENANT_001);

        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
            .name("Updated Electronics")
            .description("Updated description")
            .build();

        // When
        ResponseEntity<CategoryResponse> response = performAuthenticatedPutWithShop(
            "/categories/" + CAT_ELECTRONICS,
            request,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            CategoryResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Electronics");
    }

    @Test
    @DisplayName("PATCH /categories/{id} - Should partial update category")
    void shouldPartialUpdateCategory() {
        // Given - Use existing category from test-data.sql
        setTenantContext(TEST_TENANT_001);

        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
            .description("Patched description")
            .build();

        // When
        ResponseEntity<CategoryResponse> response = performAuthenticatedPatchWithShop(
            "/categories/" + CAT_CLOTHING,
            request,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            CategoryResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getDescription()).isEqualTo("Patched description");
    }

    @Test
    @DisplayName("DELETE /categories/{id} - Should delete category")
    void shouldDeleteCategory() {
        // Given - Create a category to delete (can't delete existing ones that have products)
        setTenantContext(TEST_TENANT_001);

        CategoryCreateRequest createRequest = CategoryCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .name("Category To Delete")
            .description("This will be deleted")
            .build();

        ResponseEntity<CategoryResponse> createResponse = performAuthenticatedPostWithShop(
            "/shops/" + TEST_SHOP_001 + "/categories",
            createRequest,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            CategoryResponse.class,
            "OWNER"
        );

        String categoryIdToDelete = createResponse.getBody().getId();

        // When
        ResponseEntity<Void> response = performAuthenticatedDeleteWithShop(
            "/categories/" + categoryIdToDelete,
                TEST_OWNER_EMAIL,
            TEST_SHOP_001,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
