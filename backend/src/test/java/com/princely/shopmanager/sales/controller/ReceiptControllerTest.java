package com.princely.shopmanager.sales.controller;

import com.princely.shopmanager.sales.domain.Receipt;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.sales.service.ReceiptService;
import com.princely.shopmanager.test.security.WithMockPermissions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@DisplayName("ReceiptController Tests")
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    ReceiptControllerTest.ControllerTestConfiguration.class
})
class ReceiptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private SalesTransactionRepository salesTransactionRepository;

    @MockBean
    private com.princely.shopmanager.shared.service.FeatureFlagService featureFlagService;

    private Receipt sampleReceipt;
    private SalesTransaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = SalesTransaction.builder()
            .id("txn-123")
            .transactionNumber("TXN-2024-001")
            .totalAmount(new BigDecimal("10.00"))
            .transactionDate(LocalDateTime.of(2024, 1, 15, 10, 30))
            .build();

        sampleReceipt = Receipt.builder()
            .id("receipt-123")
            .receiptNumber("RCP-2024-001")
            .transaction(sampleTransaction)
            .receiptContent("=== RECEIPT ===\nDowntown Electronics\n123 Main St\n\nTransaction: TXN-2024-001\nDate: 2024-01-15 10:30:00\n\nItems:\n- Product A x1 - $10.00\n\nTotal: $10.00\nThank you!")
            .printableContent("=== RECEIPT (PRINTABLE) ===\nDowntown Electronics\n\nTransaction: TXN-2024-001\nTotal: $10.00")
            .generatedAt(LocalDateTime.of(2024, 1, 15, 10, 30))
            .status(Receipt.ReceiptStatus.GENERATED)
            .format(Receipt.ReceiptFormat.TEXT)
            .build();
    }

    @Nested
    @DisplayName("GET /api/receipts/{receiptId} - Get Receipt")
    class GetReceiptTests {

        @Test
        @WithMockPermissions(role = "CASHIER")
        @DisplayName("Should get receipt successfully")
        void shouldGetReceiptSuccessfully() throws Exception {
            // Given
            when(receiptService.getReceipt("receipt-123"))
                .thenReturn(Optional.of(sampleReceipt));

            // When & Then
            mockMvc.perform(get("/api/receipts/receipt-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("receipt-123"))
                .andExpect(jsonPath("$.receiptNumber").value("RCP-2024-001"));

            verify(receiptService).getReceipt("receipt-123");
        }
    }

    @Nested
    @DisplayName("Authentication and Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            mockMvc.perform(get("/api/receipts/receipt-123")).andExpect(status().isUnauthorized());
        }
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public ReceiptController receiptController(ReceiptService receiptService) {
            return new ReceiptController(receiptService);
        }
    }
}
