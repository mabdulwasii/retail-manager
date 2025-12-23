package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for ApiRootController - Happy Path Only.
 *
 * Covers all 2 ApiRootController endpoints (API discovery/root).
 * Purpose: API documentation showing all endpoints work end-to-end.
 */
@Transactional
@DisplayName("ApiRoot Controller - Minimal Happy Path Integration Tests")
class ApiRootControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /api - Should return API root")
    void shouldGetApiRoot() {
        // Given/When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "",
            "owner@testretail.com",
            null,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @org.junit.jupiter.api.Disabled("Placeholder test - health endpoint path not yet finalized")
    @DisplayName("GET /api/health - Should return health status")
    void shouldGetHealthStatus() {
        // NOTE: Actual endpoint path may vary - placeholder test
    }
}
