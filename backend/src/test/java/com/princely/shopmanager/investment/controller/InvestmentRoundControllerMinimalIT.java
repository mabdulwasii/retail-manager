package com.princely.shopmanager.investment.controller;

import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import com.princely.shopmanager.investment.dto.InvestmentRoundResponse;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.princely.shopmanager.test.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Minimal integration test for InvestmentRoundController - Happy Path Only.
 *
 * Covers InvestmentRoundController endpoints with simple happy-path tests.
 * Comprehensive business logic tests are in InvestmentRoundServiceTest (unit tests).
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing endpoints work end-to-end.
 * Tests use existing test-data.sql fixtures for optimal performance.
 *
 * ENABLED (7/8):
 * - POST /shops/{shopId}/investment-rounds - Create investment round
 * - GET /shops/{shopId}/investment-rounds - List investment rounds
 * - GET /investment-rounds/{roundId} - Get by ID
 * - PUT /investment-rounds/{roundId} - Update investment round
 * - PATCH /investment-rounds/{roundId} - Patch investment round
 * - POST /investment-rounds/{roundId}/close - Close investment round
 * - POST /investment-rounds/{roundId}/investors - Add investor to round
 *
 * DISABLED (1/8):
 * - DELETE /investment-rounds/{roundId} - Disabled: Business rule prevents deletion with investments
 */
@Transactional
@DisplayName("InvestmentRound Controller - Minimal Happy Path Integration Tests")
class InvestmentRoundControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /shops/{shopId}/investment-rounds - Should create investment round")
    void shouldCreateInvestmentRound() {
        // Given
        setTenantContext(TEST_TENANT_001);

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .investmentType(com.princely.shopmanager.investment.domain.Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(com.princely.shopmanager.investment.domain.Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .notes("Test round for MinimalIT")
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId(USER_INVESTOR_001)
                    .amount(new BigDecimal("1000.00"))
                    .notes("Test investment")
                    .build()
            ))
            .build();

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedPostWithShop(
            "/shops/" + TEST_SHOP_001 + "/investment-rounds",
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getShopId()).isEqualTo(TEST_SHOP_001);
    }

    @Test
    @DisplayName("GET /shops/{shopId}/investment-rounds - Should list investment rounds")
    void shouldListInvestmentRounds() {
        // Given
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/shops/" + TEST_SHOP_001 + "/investment-rounds",
            "owner@testretail.com",
            TEST_SHOP_001,
            String.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /investment-rounds/{roundId} - Should get investment round by ID")
    void shouldGetInvestmentRoundById() {
        // Given - Use existing investment round from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedGetWithShop(
            "/investment-rounds/" + INVESTMENT_ROUND_001,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(INVESTMENT_ROUND_001);
        assertThat(response.getBody().getRoundNumber()).isEqualTo(ROUND_NUMBER_001);
    }

    @Test
    @DisplayName("PUT /investment-rounds/{roundId} - Should update investment round")
    void shouldUpdateInvestmentRound() {
        // Given - Use existing investment round from test-data.sql with COMPLETE request
        setTenantContext(TEST_TENANT_001);

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .investmentType(com.princely.shopmanager.investment.domain.Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(com.princely.shopmanager.investment.domain.Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .notes("Updated notes for investment round")
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId(USER_INVESTOR_001)
                    .amount(new BigDecimal("10000.00"))
                    .build()
            ))
            .build();

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedPutWithShop(
            "/investment-rounds/" + INVESTMENT_ROUND_001,
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(INVESTMENT_ROUND_001);
    }

    @Test
    @DisplayName("PATCH /investment-rounds/{roundId} - Should patch investment round")
    void shouldPatchInvestmentRound() {
        // Given - For PATCH, also need complete request due to validation
        setTenantContext(TEST_TENANT_001);

        InvestmentRoundCreateRequest request = InvestmentRoundCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .investmentType(com.princely.shopmanager.investment.domain.Investment.InvestmentType.SHOP_WIDE)
            .profitSharingModel(com.princely.shopmanager.investment.domain.Investment.ProfitSharingModel.PROPORTIONAL_BY_AMOUNT)
            .notes("Patched notes for investment round")
            .investors(List.of(
                InvestmentRoundCreateRequest.InvestorInput.builder()
                    .investorId(USER_INVESTOR_001)
                    .amount(new BigDecimal("10000.00"))
                    .build()
            ))
            .build();

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedPatchWithShop(
            "/investment-rounds/" + INVESTMENT_ROUND_002,
            request,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(INVESTMENT_ROUND_002);
    }

    @Test
    @org.junit.jupiter.api.Disabled("DELETE endpoint exists but cannot succeed in happy path: " +
        "Investment rounds require at least one investor (@NotEmpty validation), " +
        "and deleting rounds with investments violates business rules. " +
        "This endpoint is primarily for cleanup of invalid/draft rounds. " +
        "Comprehensive business logic testing in InvestmentRoundServiceTest.")
    @DisplayName("DELETE /investment-rounds/{roundId} - Delete not testable in happy path")
    void shouldDeleteInvestmentRound() {
        // This test is disabled because:
        // 1. InvestmentRoundCreateRequest.investors is @NotEmpty - cannot create rounds without investors
        // 2. Business rule prevents deletion of rounds with active investments
        // 3. MinimalIT tests are for happy path only, not error scenarios
        // 4. The DELETE endpoint exists for cleanup scenarios not applicable to minimal tests
    }

    @Test
    @DisplayName("POST /investment-rounds/{roundId}/close - Should close investment round")
    void shouldCloseInvestmentRound() {
        // Given - Use existing investment round from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedPostWithShop(
            "/investment-rounds/" + INVESTMENT_ROUND_001 + "/close",
            null,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(INVESTMENT_ROUND_001);
    }

    @Test
    @DisplayName("POST /investment-rounds/{roundId}/investors - Should add investor to round")
    void shouldAddInvestorToRound() {
        // Given - Use existing investment round from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // Add a different investor to existing round
        InvestmentRoundCreateRequest.InvestorInput investorInput =
            InvestmentRoundCreateRequest.InvestorInput.builder()
                .investorId(USER_OWNER_001)  // Add owner as investor
                .amount(new BigDecimal("25000.00"))
                .notes("Additional investment from owner")
                .build();

        // When
        ResponseEntity<InvestmentRoundResponse> response = performAuthenticatedPostWithShop(
            "/investment-rounds/" + INVESTMENT_ROUND_001 + "/investors",
            investorInput,
            "owner@testretail.com",
            TEST_SHOP_001,
            InvestmentRoundResponse.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(INVESTMENT_ROUND_001);
    }
}
