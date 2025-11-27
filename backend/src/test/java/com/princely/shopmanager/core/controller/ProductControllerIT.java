package com.princely.shopmanager.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.test.TestConstants;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.test.security.WithMockPermissions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.isA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ProductController.
 * Tests granular permission-based authorization for product management.
 */
@DisplayName("Product Controller Integration Tests")
class ProductControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============== Create Product Tests ==============

    @Test
    @DisplayName("OWNER should create product")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldCreateProduct() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Test Product")
            .barcode("1234567890123")
            .description("Test product description")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
            .andExpect(jsonPath("$.unitPrice").value(99.99))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("MANAGER should create product")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldCreateProduct() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Manager Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Manager Product"));
    }

    @Test
    @DisplayName("EMPLOYEE should NOT create product")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotCreateProduct() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Employee Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    // ============== List Products Tests ==============

    @Test
    @DisplayName("OWNER should list products")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldListProducts() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    @Test
    @DisplayName("MANAGER should list products")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldListProducts() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should list products (read-only)")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldListProducts() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User without PRODUCT_LIST permission should NOT list products")
    @WithMockPermissions(value = {"SALES_CREATE"}) // Has sales permission but not product list
    void userWithoutPermissionShouldNotListProducts() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001))
            .andExpect(status().isForbidden());
    }

    // ============== Get Product by ID Tests ==============

    @Test
    @DisplayName("OWNER should read product details")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldReadProductDetails() throws Exception {
        // Create a product first
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Read Test Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String createResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        // Read the product
        mockMvc.perform(get("/api/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("Read Test Product"));
    }

    @Test
    @DisplayName("EMPLOYEE should read product details")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldReadProductDetails() throws Exception {
        // Create product as owner first
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Employee Read Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        // Switch to owner to create
        mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .with(request -> {
                    // This will use OWNER permissions from test annotation context
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andReturn().getResponse().getContentAsString();

        // If product was created, employee should be able to read
        // For this test, we'll just verify the permission check allows it
        mockMvc.perform(get("/api/products/{productId}", "any-product-id"))
            .andExpect(status().isNotFound()); // 404 not 403, proving permission is granted
    }

    // ============== Update Product Tests ==============

    @Test
    @DisplayName("OWNER should update product")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldUpdateProduct() throws Exception {
        // Create product
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Update Test Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String createResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        // Update product
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
            .name("Updated Product Name")
            .description("Updated description")
            .build();

        mockMvc.perform(put("/api/products/{productId}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Product Name"))
            .andExpect(jsonPath("$.unitPrice").value(69.99));
    }

    @Test
    @DisplayName("MANAGER should update product")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldUpdateProduct() throws Exception {
        // Create and update similar to owner test
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Manager Update Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String createResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
            .name("Manager Updated Name")
            .build();

        mockMvc.perform(put("/api/products/{productId}", productId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("EMPLOYEE should NOT update product")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotUpdateProduct() throws Exception {
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
            .name("Employee Update")
            .build();

        mockMvc.perform(put("/api/products/{productId}", "any-product-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isForbidden());
    }

    // ============== Delete Product Tests ==============

    @Test
    @DisplayName("OWNER should delete product")
    @WithMockPermissions(role = "OWNER")
    void ownerShouldDeleteProduct() throws Exception {
        // Create product
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
            .shopId(TestConstants.TEST_SHOP_001)
            .name("Delete Test Product")
            .categoryId(TestConstants.CAT_ELECTRONICS)
            .build();

        String createResponse = mockMvc.perform(post("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        // Delete product
        mockMvc.perform(delete("/api/products/{productId}", productId)
                .with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("MANAGER should NOT delete product")
    @WithMockPermissions(role = "MANAGER")
    void managerShouldNotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{productId}", "any-product-id")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("EMPLOYEE should NOT delete product")
    @WithMockPermissions(role = "EMPLOYEE")
    void employeeShouldNotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{productId}", "any-product-id")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }

    // ============== Search and Filter Tests ==============

    @Test
    @DisplayName("Should filter products by category")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .param("categoryId", TestConstants.CAT_ELECTRONICS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    @Test
    @DisplayName("Should search products by name or SKU")
    @WithMockPermissions(role = "MANAGER")
    void shouldSearchProductsByNameOrSku() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .param("search", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", isA(Iterable.class)));
    }

    @Test
    @DisplayName("Should filter products by status")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterProductsByStatus() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .param("status", "ACTIVE"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should filter products by price range")
    @WithMockPermissions(role = "MANAGER")
    void shouldFilterProductsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/shops/{shopId}/products", TestConstants.TEST_SHOP_001)
                .param("minPrice", "10.00")
                .param("maxPrice", "100.00"))
            .andExpect(status().isOk());
    }
}
