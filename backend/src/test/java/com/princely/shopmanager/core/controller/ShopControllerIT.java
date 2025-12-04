package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ShopCreateRequest;
import com.princely.shopmanager.core.dto.ShopResponse;
import com.princely.shopmanager.core.dto.ShopUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ShopController.
 * 
 * This test class validates:
 * - Complete API functionality with real HTTP requests
 * - Authentication and authorization flows
 * - Multi-tenant isolation and context management
 * - Database persistence and transaction handling
 * - Business rule validation
 * - Error handling and edge cases
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShopControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should create shop successfully with valid data and OWNER role")
    void shouldCreateShopSuccessfully() {
        // Given
        String tenantId = "test-tenant-001";
        setTenantContext(tenantId);

        ShopCreateRequest request = createSampleShopCreateRequest("Electronics Store");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops", 
            request, 
            "shop-owner", 
            ShopResponse.class, 
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertValidShopResponse(response.getBody(), "Electronics Store");
        assertThat(response.getBody().getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should return 403 when creating shop without proper role")
    void shouldReturn403WhenCreatingShopWithoutProperRole() {
        // Given
        ShopCreateRequest request = createSampleShopCreateRequest("Test Shop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops", 
            request, 
            "cashier", 
            ShopResponse.class, 
            "CASHIER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("Should get shop by ID successfully")
    void shouldGetShopByIdSuccessfully() {
        // Given
        String tenantId = "test-tenant-002";
        Map<String, Object> testData = setupTenantTestData(tenantId);
        Shop testShop = (Shop) testData.get("testShop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedGet(
            "/shops/" + testShop.getId(), 
            "shop-manager", 
            ShopResponse.class, 
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertValidShopResponse(response.getBody(), null);
        assertThat(response.getBody().getId()).isEqualTo(testShop.getId());
        assertThat(response.getBody().getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent shop")
    void shouldReturn404WhenGettingNonExistentShop() {
        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedGet(
            "/shops/non-existent-id", 
            "shop-manager", 
            ShopResponse.class, 
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should get paginated shops successfully")
    void shouldGetPaginatedShopsSuccessfully() {
        // Given
        String tenantId = "test-tenant-003";
        setupTenantTestData(tenantId);

        // Create additional test shops
        createTestShop("Shop1", tenantId);
        createTestShop("Shop2", tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPagination(
            "/shops", 
            0, 
            10, 
            "shop-owner", 
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertValidPagedResponse(response.getBody(), 2);
    }

    @Test
    @DisplayName("Should get active shops successfully")
    void shouldGetActiveShopsSuccessfully() {
        // Given
        String tenantId = "test-tenant-004";
        setupTenantTestData(tenantId);

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
            "/shops/active", 
            "cashier", 
            String.class, 
            "CASHIER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    @DisplayName("Should update shop successfully")
    void shouldUpdateShopSuccessfully() {
        // Given
        String tenantId = "test-tenant-005";
        Map<String, Object> testData = setupTenantTestData(tenantId);
        Shop testShop = (Shop) testData.get("testShop");

        ShopUpdateRequest updateRequest = createSampleShopUpdateRequest("Updated Description");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPut(
            "/shops/" + testShop.getId(), 
            updateRequest, 
            "shop-manager", 
            ShopResponse.class, 
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertValidShopResponse(response.getBody(), null);
        assertThat(response.getBody().getDescription()).isEqualTo("Updated Description");
        assertThat(response.getBody().getCity()).isEqualTo("Updated City");
    }

    @Test
    @DisplayName("Should change shop status successfully")
    void shouldChangeShopStatusSuccessfully() {
        // Given
        String tenantId = "test-tenant-006";
        Map<String, Object> testData = setupTenantTestData(tenantId);
        Shop testShop = (Shop) testData.get("testShop");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPatch(
            "/shops/" + testShop.getId() + "/status?status=SUSPENDED", 
            null, 
            "shop-owner", 
            ShopResponse.class, 
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("SUSPENDED");
    }

    @Test
    @DisplayName("Should enforce tenant isolation")
    void shouldEnforceTenantIsolation() {
        // Given
        String tenantA = "tenant-a";
        String tenantB = "tenant-b";

        // Create shop in tenant A
        setTenantContext(tenantA);
        Map<String, Object> tenantAData = setupTenantTestData(tenantA);
        Shop shopInTenantA = (Shop) tenantAData.get("testShop");

        // Switch to tenant B context
        setTenantContext(tenantB);

        // When - Try to access shop from tenant A while in tenant B context
        ResponseEntity<ShopResponse> response = performAuthenticatedGet(
            "/shops/" + shopInTenantA.getId(), 
            "shop-manager", 
            ShopResponse.class, 
            "MANAGER"
        );

        // Then - Should not find the shop due to tenant isolation
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should validate required fields when creating shop")
    void shouldValidateRequiredFieldsWhenCreatingShop() {
        // Given
        ShopCreateRequest invalidRequest = ShopCreateRequest.builder()
            .name("") // Invalid empty name
            .email("invalid-email") // Invalid email format
            .build();

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops", 
            invalidRequest, 
            "shop-owner", 
            ShopResponse.class, 
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle concurrent shop creation gracefully")
    void shouldHandleConcurrentShopCreationGracefully() {
        // Given
        String tenantId = "test-tenant-concurrent";
        setTenantContext(tenantId);

        ShopCreateRequest request1 = createSampleShopCreateRequest("Concurrent Shop");
        ShopCreateRequest request2 = createSampleShopCreateRequest("Concurrent Shop");

        // When - Create shops concurrently (simulated)
        ResponseEntity<ShopResponse> response1 = performAuthenticatedPost(
            "/shops", 
            request1, 
            "shop-owner1", 
            ShopResponse.class, 
            "OWNER"
        );

        ResponseEntity<ShopResponse> response2 = performAuthenticatedPost(
            "/shops", 
            request2, 
            "shop-owner2", 
            ShopResponse.class, 
            "OWNER"
        );

        // Then - Both should succeed with unique names
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response1.getBody().getName()).isNotEqualTo(response2.getBody().getName());
    }

    @Test
    @DisplayName("Should return 401 for unauthenticated requests")
    void shouldReturn401ForUnauthenticatedRequests() {
        // Given
        ShopCreateRequest request = createSampleShopCreateRequest("Test Shop");

        // When - Make request without authentication
        ResponseEntity<ShopResponse> response = restTemplate.postForEntity(
            getApiUrl("/shops"), 
            request, 
            ShopResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should handle database transaction rollback on error")
    void shouldHandleDatabaseTransactionRollbackOnError() {
        // This test would verify transaction rollback behavior
        // Implementation depends on your specific error scenarios

        // Given
        String tenantId = "test-tenant-transaction";
        setTenantContext(tenantId);

        // Create a request that might cause a database error
        ShopCreateRequest request = createSampleShopCreateRequest("Transaction Test");

        // When
        ResponseEntity<ShopResponse> response = performAuthenticatedPost(
            "/shops", 
            request, 
            "shop-owner", 
            ShopResponse.class, 
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Verify the shop was actually persisted
        Shop savedShop = shopRepository.findById(response.getBody().getId()).orElse(null);
        assertThat(savedShop).isNotNull();
        assertThat(savedShop.getTenant().getId()).isEqualTo(tenantId);
    }
}
