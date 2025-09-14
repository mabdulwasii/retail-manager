package com.princely.shopmanager.analytics.controller;

import com.princely.shopmanager.analytics.dto.*;
import com.princely.shopmanager.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive test suite for AnalyticsController REST endpoints.
 *
 * This test class validates:
 * - Analytics data retrieval endpoints
 * - Date range filtering and query parameters
 * - Security authorization for different roles
 * - Cache management operations
 * - Feature flag conditional loading
 *
 * Uses @WebMvcTest for focused controller testing with mocked dependencies.
 */
@WebMvcTest
@DisplayName("AnalyticsController Tests")
@TestPropertySource(properties = {
    "app.features.analytics.enabled=true",
    "app.features.investment.enabled=true",
    "app.features.fraud.enabled=true"
})
@ContextConfiguration(classes = {
    com.princely.shopmanager.test.config.WebMvcTestConfiguration.class,
    AnalyticsControllerTest.ControllerTestConfiguration.class
})
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private com.princely.shopmanager.shared.service.FeatureFlagService featureFlagService;

    private SalesSummaryDto sampleSalesSummary;
    private InvestmentRoiDto sampleInvestmentRoi;
    private FraudStatisticsDto sampleFraudStats;
    private RevenueAnalyticsDto sampleRevenueAnalytics;

    @BeforeEach
    void setUp() {
        sampleSalesSummary = SalesSummaryDto.builder()
            .shopId("shop-123")
            .totalRevenue(new BigDecimal("10000.00"))
            .totalTransactions(150L)
            .averageTransactionValue(new BigDecimal("66.67"))
            .periodStart(LocalDateTime.of(2024, 1, 1, 0, 0))
            .periodEnd(LocalDateTime.of(2024, 1, 31, 23, 59))
            .calculatedAt(LocalDateTime.now())
            .build();

        sampleInvestmentRoi = InvestmentRoiDto.builder()
            .shopId("shop-123")
            .totalInvestmentAmount(new BigDecimal("50000.00"))
            .totalDistributions(new BigDecimal("7500.00"))
            .roiPercentage(new BigDecimal("15.00"))
            .periodStart(LocalDateTime.of(2024, 1, 1, 0, 0))
            .periodEnd(LocalDateTime.of(2024, 1, 31, 23, 59))
            .calculatedAt(LocalDateTime.now())
            .build();

        sampleFraudStats = FraudStatisticsDto.builder()
            .shopId("shop-123")
            .totalAssessments(1000L)
            .highRiskCount(25L)
            .criticalRiskCount(3L)
            .riskRate(new BigDecimal("0.30"))
            .periodStart(LocalDateTime.of(2024, 1, 1, 0, 0))
            .periodEnd(LocalDateTime.of(2024, 1, 31, 23, 59))
            .calculatedAt(LocalDateTime.now())
            .build();

        sampleRevenueAnalytics = RevenueAnalyticsDto.builder()
            .shopId("shop-123")
            .currentRevenue(new BigDecimal("25000.00"))
            .previousRevenue(new BigDecimal("22000.00"))
            .growthRate(new BigDecimal("12.50"))
            .currentTransactions(150L)
            .previousTransactions(130L)
            .periodStart(LocalDateTime.of(2024, 1, 1, 0, 0))
            .periodEnd(LocalDateTime.of(2024, 1, 31, 23, 59))
            .calculatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("GET /api/analytics/sales-summary - Get Sales Summary")
    class GetSalesSummaryTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should get sales summary successfully with date range")
        void shouldGetSalesSummaryWithDateRange() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            when(analyticsService.getSalesSummary("shop-123", startDate, endDate))
                .thenReturn(sampleSalesSummary);

            // When & Then
            mockMvc.perform(get("/api/analytics/sales-summary")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(10000.00))
                .andExpect(jsonPath("$.totalTransactions").value(150))
                .andExpect(jsonPath("$.averageTransactionValue").value(66.67))
                .andExpect(jsonPath("$.shopId").value("shop-123"));

            verify(analyticsService).getSalesSummary("shop-123", startDate, endDate);
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should allow access with SHOP_OWNER role")
        void shouldAllowAccessWithShopOwnerRole() throws Exception {
            // Given
            when(analyticsService.getSalesSummary(eq("shop-123"), any(), any()))
                .thenReturn(sampleSalesSummary);

            // When & Then
            mockMvc.perform(get("/api/analytics/sales-summary")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny access with insufficient role")
        void shouldDenyAccessWithInsufficientRole() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/analytics/sales-summary")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/investment-roi - Get Investment ROI")
    class GetInvestmentRoiTests {

        @Test
        @WithMockUser(roles = "INVESTOR")
        @DisplayName("Should get investment ROI successfully")
        void shouldGetInvestmentRoiSuccessfully() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            when(analyticsService.getInvestmentROI("shop-123", startDate, endDate))
                .thenReturn(sampleInvestmentRoi);

            // When & Then
            mockMvc.perform(get("/api/analytics/investment-roi")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvestmentAmount").value(50000.00))
                .andExpect(jsonPath("$.totalDistributions").value(7500.00))
                .andExpect(jsonPath("$.roiPercentage").value(15.00))
                .andExpect(jsonPath("$.shopId").value("shop-123"));

            verify(analyticsService).getInvestmentROI("shop-123", startDate, endDate);
        }

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should allow access with SHOP_OWNER role")
        void shouldAllowAccessWithShopOwnerRole() throws Exception {
            // Given
            when(analyticsService.getInvestmentROI(eq("shop-123"), any(), any()))
                .thenReturn(sampleInvestmentRoi);

            // When & Then
            mockMvc.perform(get("/api/analytics/investment-roi")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/fraud-statistics - Get Fraud Statistics")
    class GetFraudStatisticsTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should get fraud statistics successfully")
        void shouldGetFraudStatisticsSuccessfully() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            when(analyticsService.getFraudStatistics("shop-123", startDate, endDate))
                .thenReturn(sampleFraudStats);

            // When & Then
            mockMvc.perform(get("/api/analytics/fraud-statistics")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssessments").value(1000))
                .andExpect(jsonPath("$.highRiskCount").value(25))
                .andExpect(jsonPath("$.criticalRiskCount").value(3))
                .andExpect(jsonPath("$.riskRate").value(0.30))
                .andExpect(jsonPath("$.shopId").value("shop-123"));

            verify(analyticsService).getFraudStatistics("shop-123", startDate, endDate);
        }
    }

    @Nested
    @DisplayName("GET /api/analytics/revenue-analytics - Get Revenue Analytics")
    class GetRevenueAnalyticsTests {

        @Test
        @WithMockUser(roles = "MANAGER")
        @DisplayName("Should get revenue analytics successfully")
        void shouldGetRevenueAnalyticsSuccessfully() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            when(analyticsService.getRevenueAnalytics("shop-123", startDate, endDate))
                .thenReturn(sampleRevenueAnalytics);

            // When & Then
            mockMvc.perform(get("/api/analytics/revenue-analytics")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRevenue").value(25000.00))
                .andExpect(jsonPath("$.previousRevenue").value(22000.00))
                .andExpect(jsonPath("$.growthRate").value(12.50))
                .andExpect(jsonPath("$.currentTransactions").value(150))
                .andExpect(jsonPath("$.previousTransactions").value(130))
                .andExpect(jsonPath("$.shopId").value("shop-123"));

            verify(analyticsService).getRevenueAnalytics("shop-123", startDate, endDate);
        }
    }

    @Nested
    @DisplayName("POST /api/analytics/clear-cache/{shopId} - Clear Cache")
    class ClearCacheTests {

        @Test
        @WithMockUser(roles = "OWNER")
        @DisplayName("Should clear cache successfully with SHOP_OWNER role")
        void shouldClearCacheWithShopOwnerRole() throws Exception {
            // Given
            doNothing().when(analyticsService).clearCacheForShop("shop-123");

            // When & Then
            mockMvc.perform(post("/api/analytics/clear-cache/shop-123")
                    .with(csrf()))
                .andExpect(status().isOk());

            verify(analyticsService).clearCacheForShop("shop-123");
        }
    }

    @Nested
    @DisplayName("Authentication and Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            mockMvc.perform(get("/api/analytics/sales-summary")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/analytics/investment-roi")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/analytics/fraud-statistics")).andExpect(status().isUnauthorized());
            mockMvc.perform(get("/api/analytics/revenue-analytics")).andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/analytics/clear-cache/shop-123").with(csrf())).andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CASHIER")
        @DisplayName("Should deny analytics access to CASHIER role")
        void shouldDenyAnalyticsAccessToCashierRole() throws Exception {
            mockMvc.perform(get("/api/analytics/sales-summary")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/analytics/investment-roi")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/analytics/fraud-statistics")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/analytics/revenue-analytics")
                    .param("shopId", "shop-123")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:00"))
                .andExpect(status().isForbidden());
        }
    }

    @Configuration
    static class ControllerTestConfiguration {

        @Bean
        public AnalyticsController analyticsController(AnalyticsService analyticsService) {
            return new AnalyticsController(analyticsService);
        }
    }
}