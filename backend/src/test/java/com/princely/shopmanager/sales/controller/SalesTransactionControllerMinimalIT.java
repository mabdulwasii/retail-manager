package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.sales.dto.SalesTransactionResponse;
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
 * Minimal integration test for SalesTransactionController - Happy Path Only.
 *
 * Covers all 6 SalesTransactionController endpoints with simple happy-path tests.
 * Comprehensive business logic tests (FEFO inventory deduction) are in SalesTransactionServiceTest.
 * Comprehensive RBAC tests are in RBACIntegrationTest.
 *
 * Purpose: API documentation showing all endpoints work end-to-end.
 *
 * PASSING (6/6):
 * - POST /sales - Create ✓
 * - GET /sales/{id} - Get by ID ✓
 * - GET /sales - List transactions ✓
 * - GET /sales/{id}/receipt - Get receipt ✓
 * - GET /sales/by-date-range - Get by date range ✓
 * - POST /sales/{id}/void - Void transaction ✓
 */
@Transactional
@DisplayName("SalesTransaction Controller - Minimal Happy Path Integration Tests")
class SalesTransactionControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("POST /sales - Should create sales transaction")
    void shouldCreateSalesTransaction() {
        // Given - Use existing shop and products from test-data.sql
        setTenantContext(TEST_TENANT_001);

        SalesTransactionCreateRequest.LineItemRequest lineItem = SalesTransactionCreateRequest.LineItemRequest.builder()
            .productId(PROD_WIRELESS_MOUSE)
            .quantity(1)
            .unitPrice(new BigDecimal("25.99"))
            .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(TEST_SHOP_001)
            .customerName("Test Customer")
            .lineItems(List.of(lineItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When
        ResponseEntity<SalesTransactionResponse> response = performAuthenticatedPostWithShop(
            "/sales",
            request,
            "manager@testretail.com",
            TEST_SHOP_001,
            SalesTransactionResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getShopId()).isEqualTo(TEST_SHOP_001);
        assertThat(response.getBody().getLineItems()).hasSize(1);
        assertThat(response.getBody().getCustomerName()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("GET /sales/{id} - Should get sales transaction by ID")
    void shouldGetSalesTransactionById() {
        // Given - Use existing sales transaction from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<SalesTransactionResponse> response = performAuthenticatedGetWithShop(
            "/sales/" + SALES_TXN_001,
            "manager@testretail.com",
            TEST_SHOP_001,
            SalesTransactionResponse.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(SALES_TXN_001);
        assertThat(response.getBody().getCustomerName()).isEqualTo("John Customer");
    }

    @Test
    @DisplayName("GET /sales/{transactionId}/receipt - Should get transaction receipt")
    void shouldGetTransactionReceipt() {
        // Given - Use existing transaction from test-data.sql (has auto-generated receipt)
        setTenantContext(TEST_TENANT_001);

        // When - Get the receipt for existing transaction
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/sales/" + SALES_TXN_001 + "/receipt",
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("TXN-2024-001");
    }

    @Test
    @DisplayName("GET /sales - Should list sales transactions")
    void shouldListSalesTransactions() {
        // Given - Use existing shop from test-data.sql (has sales transactions)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/sales?shopId=" + TEST_SHOP_001,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("TXN-2024-001");
    }

    @Test
    @DisplayName("GET /sales/by-date-range - Should get sales by date range")
    void shouldGetSalesByDateRange() {
        // Given - Use existing shop from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When - Get last 7 days (using ISO DateTime format)
        String startDate = java.time.LocalDate.now().minusDays(7).atStartOfDay().toString();
        String endDate = java.time.LocalDate.now().atTime(23, 59, 59).toString();

        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/sales/by-date-range?shopId=" + TEST_SHOP_001 + "&startDate=" + startDate + "&endDate=" + endDate,
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("TXN-2024"); // Should return transactions
    }

    @Test
    @DisplayName("POST /sales/{id}/void - Should void sales transaction")
    void shouldVoidSalesTransaction() {
        // Given - Use existing voidable transaction from test-data.sql (COMPLETED status)
        setTenantContext(TEST_TENANT_001);

        // When - Void the existing transaction
        ResponseEntity<Void> response = performAuthenticatedPostWithShop(
            "/sales/" + TXN_VOIDABLE + "/void?reason=Customer refund request",
            null,
            "owner@testretail.com",
            TEST_SHOP_001,
            Void.class,
            "OWNER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
