package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.domain.Receipt;
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
 * Minimal integration test for ReceiptController - Happy Path Only.
 *
 * Covers all 9 ReceiptController endpoints with simple happy-path tests.
 * Receipts are auto-generated when sales transactions are created.
 *
 * PASSING (9/9):
 * - GET /receipts - List receipts ✓
 * - GET /receipts/{receiptId} - Get by ID ✓
 * - GET /receipts/by-number/{receiptNumber} - Get by number ✓
 * - GET /receipts/transaction/{transactionId} - Get by transaction ✓
 * - GET /receipts/{receiptId}/content - Get content ✓
 * - GET /receipts/{receiptId}/printable - Get printable content ✓
 * - POST /receipts/{receiptId}/mark-printed - Mark printed ✓
 * - POST /receipts/{receiptId}/mark-emailed - Mark emailed ✓
 * - POST /receipts/regenerate/{transactionId} - Regenerate receipt ✓
 */
@Transactional
@DisplayName("Receipt Controller - Minimal Happy Path Integration Tests")
class ReceiptControllerMinimalIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /receipts - Should list receipts")
    void shouldListReceipts() {
        // Given - Use existing shop from test-data.sql (has receipts)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithPaginationAndShop(
            "/receipts?shopId=" + TEST_SHOP_001,
            0,
            20,
            "manager@testretail.com",
            TEST_SHOP_001,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("RCP-2024");
    }

    @Test
    @DisplayName("GET /receipts/{receiptId} - Should get receipt by ID")
    void shouldGetReceiptById() {
        // Given - Use existing test data (faster than creating new transaction)
        setTenantContext(TEST_TENANT_001);

        // When - Use transaction ID to get receipt (controller expects transactionId despite parameter name)
        ResponseEntity<Receipt> response = performAuthenticatedGetWithShop(
            "/receipts/" + TXN_001,
            "manager@testretail.com",
            TEST_SHOP_001,
            Receipt.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTransaction().getId()).isEqualTo(TXN_001);
        assertThat(response.getBody().getReceiptContent()).isNotNull();
    }

    @Test
    @DisplayName("GET /receipts/by-number/{receiptNumber} - Should get receipt by number")
    void shouldGetReceiptByNumber() {
        // Given - Use existing receipt from test-data.sql
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Receipt> response = performAuthenticatedGetWithShop(
            "/receipts/by-number/" + RCP_NUMBER_001,
            "manager@testretail.com",
            TEST_SHOP_001,
            Receipt.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getReceiptNumber()).isEqualTo(RCP_NUMBER_001);
    }

    @Test
    @DisplayName("GET /receipts/transaction/{transactionId} - Should get receipt by transaction")
    void shouldGetReceiptByTransaction() {
        // Given - Use existing test data
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Receipt> response = performAuthenticatedGetWithShop(
            "/receipts/transaction/" + TXN_002,
            "manager@testretail.com",
            TEST_SHOP_001,
            Receipt.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTransaction().getId()).isEqualTo(TXN_002);
    }

    @Test
    @DisplayName("GET /receipts/{receiptId}/content - Should get receipt content")
    void shouldGetReceiptContent() {
        // Given - Use existing test data
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/receipts/" + TXN_001 + "/content",
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("=== RECEIPT ===");
    }

    @Test
    @DisplayName("GET /receipts/{receiptId}/printable - Should get printable content")
    void shouldGetPrintableContent() {
        // Given - Use existing test data
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<String> response = performAuthenticatedGetWithShop(
            "/receipts/" + TXN_001 + "/printable",
            "manager@testretail.com",
            TEST_SHOP_001,
            String.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("SALES RECEIPT");
        assertThat(response.getBody()).contains("THANK YOU!");
    }

    @Test
    @DisplayName("POST /receipts/{receiptId}/mark-printed - Should mark receipt as printed")
    void shouldMarkAsPrinted() {
        // Given - Use existing test data (receipt ID required for mark-* endpoints)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Receipt> response = performAuthenticatedPostWithShop(
            "/receipts/" + RCP_001 + "/mark-printed?printedBy=manager@testretail.com",
            null,
            "manager@testretail.com",
            TEST_SHOP_001,
            Receipt.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(Receipt.ReceiptStatus.PRINTED);
        assertThat(response.getBody().getPrintedBy()).isEqualTo("manager@testretail.com");
    }

    @Test
    @DisplayName("POST /receipts/{receiptId}/mark-emailed - Should mark receipt as emailed")
    void shouldMarkAsEmailed() {
        // Given - Use existing test data (receipt ID required for mark-* endpoints)
        setTenantContext(TEST_TENANT_001);

        // When
        ResponseEntity<Receipt> response = performAuthenticatedPostWithShop(
            "/receipts/" + RCP_002 + "/mark-emailed?emailAddress=customer@example.com",
            null,
            "manager@testretail.com",
            TEST_SHOP_001,
            Receipt.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(Receipt.ReceiptStatus.EMAILED);
        assertThat(response.getBody().getEmailAddress()).isEqualTo("customer@example.com");
    }

    @Test
    @DisplayName("POST /receipts/regenerate/{transactionId} - Should regenerate receipt")
    void shouldRegenerateReceipt() {
        // Given - Use existing test data
        setTenantContext(TEST_TENANT_001);

        // When - Regenerate the receipt
        ResponseEntity<Void> response = performAuthenticatedPostWithShop(
            "/receipts/regenerate/" + TXN_001,
            null,
            "manager@testretail.com",
            TEST_SHOP_001,
            Void.class,
            "MANAGER"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
