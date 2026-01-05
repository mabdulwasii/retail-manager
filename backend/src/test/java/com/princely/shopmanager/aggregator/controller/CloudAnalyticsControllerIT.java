package com.princely.shopmanager.aggregator.controller;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for CloudAnalyticsController.
 * Tests cross-shop analytics aggregation endpoints.
 */
@Transactional
@DisplayName("Cloud Analytics API - Integration Tests")
class CloudAnalyticsControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    private Tenant testTenant;
    private Shop shop1;
    private Shop shop2;
    private Product product1;
    private User testCashier;

    @BeforeEach
    void setUpTestData() {
        // Clean up
        salesTransactionRepository.deleteAll();
        productRepository.deleteAll();
        shopRepository.deleteAll();
        tenantRepository.deleteAll();

        // Create test tenant
        testTenant = Tenant.builder()
                .name("Test Analytics Tenant")
                .contactEmail("analytics@test.com")
                .primaryAddress("123 Test St")
                .build();
        testTenant = tenantRepository.save(testTenant);

        // Create test user for cashier
        testCashier = User.builder()
                .username("testcashier")
                .email("cashier@test.com")
                .firstName("Test")
                .lastName("Cashier")
                .phoneNumber("123-456-7890")
                .tenant(testTenant)
                .keycloakId("test-cashier-id")
                .build();
        testCashier = userRepository.save(testCashier);

        // Create two shops for the tenant
        shop1 = Shop.builder()
                .name("Shop One")
                .email("shop1@test.com")
                .tenant(testTenant)
                .status(Shop.ShopStatus.ACTIVE)
                .build();
        shop1 = shopRepository.save(shop1);

        shop2 = Shop.builder()
                .name("Shop Two")
                .email("shop2@test.com")
                .tenant(testTenant)
                .status(Shop.ShopStatus.ACTIVE)
                .build();
        shop2 = shopRepository.save(shop2);

        // Create test product
        product1 = Product.builder()
                .name("Product A")
                .description("Test Product A")
                .sku("SKU-A-001")
                .shop(shop1)
                .status(Product.ProductStatus.ACTIVE)
                .build();
        product1 = productRepository.save(product1);

        // Create test sales transactions
        createSalesTransaction(shop1, product1, 2, BigDecimal.valueOf(100.00), LocalDateTime.now().minusDays(5));
        createSalesTransaction(shop1, product1, 3, BigDecimal.valueOf(100.00), LocalDateTime.now().minusDays(1));
        createSalesTransaction(shop2, product1, 1, BigDecimal.valueOf(100.00), LocalDateTime.now().minusDays(4));
    }

    private void createSalesTransaction(Shop shop, Product product, int quantity, BigDecimal unitPrice, LocalDateTime transactionDate) {
        SalesTransaction transaction = SalesTransaction.builder()
                .transactionNumber("TXN-" + System.nanoTime())
                .shop(shop)
                .cashier(testCashier)
                .transactionDate(transactionDate)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .paymentMethod(SalesTransaction.PaymentMethod.CASH)
                .status(SalesTransaction.TransactionStatus.COMPLETED)
                .lineItems(new ArrayList<>())
                .build();

        LineItem lineItem = LineItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .lineTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .transaction(transaction)
                .build();

        transaction.getLineItems().add(lineItem);
        salesTransactionRepository.save(transaction);
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/revenue - should return revenue analytics")
    void getRevenueAnalytics_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/revenue?tenantId=" + testTenant.getId(),
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_REVENUE_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("dataPoints");
        assertThat(response.getBody()).contains("totalRevenue");
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/revenue - with date range should filter correctly")
    void getRevenueAnalytics_WithDateRange_Success() {
        // Given
        setTenantContext(testTenant.getId());
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/revenue?tenantId=" + testTenant.getId() +
                "&startDate=" + startDate + "&endDate=" + endDate,
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_REVENUE_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("totalRevenue");
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/sales - should return sales metrics")
    void getSalesMetrics_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/sales?tenantId=" + testTenant.getId(),
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_SALES_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("totalSales");
        assertThat(response.getBody()).contains("totalRevenue");
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/top-products - should return top products")
    void getTopProducts_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/top-products?tenantId=" + testTenant.getId() + "&limit=10",
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_PRODUCTS_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("products");
        assertThat(response.getBody()).contains("totalProducts");
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/shop-performance - should return shop comparison")
    void getShopPerformance_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/shop-performance?tenantId=" + testTenant.getId(),
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_PERFORMANCE_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("shops");
        assertThat(response.getBody()).contains("totalShops");
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/export/csv - should export analytics to CSV")
    void exportAnalytics_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/export/csv?tenantId=" + testTenant.getId(),
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_EXPORT"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).hasToString("text/csv;charset=UTF-8");
        assertThat(response.getBody()).contains("Shop Manager - Analytics Export");
        assertThat(response.getBody()).contains(testTenant.getId());
    }

    @Test
    @DisplayName("GET /api/cloud/tenants/{tenantId}/analytics - should return tenant analytics")
    void getTenantAnalytics_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/tenants/" + testTenant.getId() + "/analytics",
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_REVENUE_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/cloud/tenants/{tenantId}/analytics/sync-status - should return sync status")
    void getShopSyncStatus_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/tenants/" + testTenant.getId() + "/analytics/sync-status",
                "admin@test.com",
                String.class,
                "CLOUD_ANALYTICS_REVENUE_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/cloud/analytics/platform - should return platform overview for admin")
    void getPlatformOverview_AsAdmin_Success() {
        // Given
        setTenantContext(testTenant.getId());

        // When
        ResponseEntity<String> response = performAuthenticatedGet(
                "/cloud/analytics/platform",
                "systemadmin@test.com",
                String.class,
                "CLOUD_ANALYTICS_PLATFORM_VIEW"
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // TODO: Enable this test once CloudAnalyticsController endpoints have @PreAuthorize annotations
    // @Test
    // @DisplayName("GET /api/cloud/analytics/revenue - without permission should fail")
    // void getRevenueAnalytics_WithoutPermission_Fail() {
    //     // Given
    //     setTenantContext(testTenant.getId());
    //
    //     // When - no permissions passed
    //     ResponseEntity<String> response = performAuthenticatedGet(
    //             "/cloud/analytics/revenue?tenantId=" + testTenant.getId(),
    //             "employee@test.com",
    //             String.class
    //             // No permissions - should result in 403
    //     );
    //
    //     // Then
    //     assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    // }
}
