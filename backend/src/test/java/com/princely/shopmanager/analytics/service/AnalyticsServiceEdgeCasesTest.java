package com.princely.shopmanager.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.analytics.domain.AnalyticsCache;
import com.princely.shopmanager.analytics.dto.FraudStatisticsDto;
import com.princely.shopmanager.analytics.dto.InvestmentRoiDto;
import com.princely.shopmanager.analytics.dto.RevenueAnalyticsDto;
import com.princely.shopmanager.analytics.dto.SalesSummaryDto;
import com.princely.shopmanager.analytics.repository.AnalyticsCacheRepository;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Edge Cases Tests")
class AnalyticsServiceEdgeCasesTest {

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private InvestorDistributionRepository distributionRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private AnalyticsCacheRepository analyticsCacheRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AnalyticsService analyticsService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
    }

    @Test
    @DisplayName("Should handle zero revenue gracefully")
    void shouldHandleZeroRevenueGracefully() {
        // Given
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.empty());
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(Optional.of(BigDecimal.ZERO));
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(0L);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0L, result.totalTransactions());
        assertEquals(BigDecimal.ZERO, result.averageTransactionValue());
    }

    @Test
    @DisplayName("Should handle null revenue from repository")
    void shouldHandleNullRevenueFromRepository() {
        // Given
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.empty());
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(Optional.empty());
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(10L);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalRevenue()));
        assertEquals(10L, result.totalTransactions());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.averageTransactionValue()));
    }

    @Test
    @DisplayName("Should handle zero investment amount for ROI")
    void shouldHandleZeroInvestmentAmountForROI() {
        // Given
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.empty());
        when(investmentRepository.getTotalActiveInvestmentAmount(anyString()))
            .thenReturn(Optional.of(BigDecimal.ZERO));
        when(distributionRepository.findByShopAndStatus(anyString(), eq(InvestorDistribution.DistributionStatus.PAID)))
            .thenReturn(Collections.emptyList());

        // When
        InvestmentRoiDto result = analyticsService.getInvestmentROI("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalInvestmentAmount());
        assertEquals(BigDecimal.ZERO, result.totalDistributions());
        assertEquals(BigDecimal.ZERO, result.roiPercentage());
    }

    @Test
    @DisplayName("Should handle zero fraud assessments")
    void shouldHandleZeroFraudAssessments() {
        // Given
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.empty());
        when(riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(anyString(), any(), any(), any()))
            .thenReturn(0L);

        // When
        FraudStatisticsDto result = analyticsService.getFraudStatistics("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.totalAssessments());
        assertEquals(0L, result.highRiskCount());
        assertEquals(0L, result.criticalRiskCount());
        assertEquals(BigDecimal.ZERO, result.riskRate());
    }

    @Test
    @DisplayName("Should handle revenue analytics with zero previous revenue")
    void shouldHandleRevenueAnalyticsWithZeroPreviousRevenue() {
        // Given - Current period has revenue, previous period has zero
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.empty());

        // Mock for current period (startDate to endDate)
        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(eq("shop-123"), eq(startDate), eq(endDate)))
            .thenReturn(Optional.of(new BigDecimal("1000.00")));
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(eq("shop-123"), eq(startDate), eq(endDate)))
            .thenReturn(10L);

        // Mock for previous period - calculate the expected period dates
        long periodDays = startDate.until(endDate, java.time.temporal.ChronoUnit.DAYS);
        LocalDateTime prevPeriodStart = startDate.minusDays(periodDays);
        LocalDateTime prevPeriodEnd = startDate.minusDays(1);

        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(eq("shop-123"), eq(prevPeriodStart), eq(prevPeriodEnd)))
            .thenReturn(Optional.of(BigDecimal.ZERO));
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(eq("shop-123"), eq(prevPeriodStart), eq(prevPeriodEnd)))
            .thenReturn(0L);

        // When
        RevenueAnalyticsDto result = analyticsService.getRevenueAnalytics("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.currentRevenue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.previousRevenue()));
        // The growth rate should be zero when dividing by zero
        assertEquals(0, BigDecimal.ZERO.compareTo(result.growthRate()));
        assertEquals(10L, result.currentTransactions());
        assertEquals(0L, result.previousTransactions());
    }

    @Test
    @DisplayName("Should handle cache expiry cleanup")
    void shouldHandleCacheExpiryCleanup() {
        // Given
        when(analyticsCacheRepository.deleteExpiredCache(any(LocalDateTime.class)))
            .thenReturn(5L);

        // When
        analyticsService.clearExpiredCache();

        // Then
        verify(analyticsCacheRepository).deleteExpiredCache(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle cache clear for shop")
    void shouldHandleCacheClearForShop() {
        // When
        analyticsService.clearCacheForShop("shop-123");

        // Then
        verify(analyticsCacheRepository).deleteByShopId("shop-123");
    }

    @Test
    @DisplayName("Should handle JSON processing exception during cache retrieval")
    void shouldHandleJsonProcessingExceptionDuringCacheRetrieval() throws Exception {
        // Given
        AnalyticsCache expiredCache = mock(AnalyticsCache.class);
        when(expiredCache.isExpired()).thenReturn(false);
        when(expiredCache.getCacheData()).thenReturn("{invalid-json}");

        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(anyString(), any(), anyString()))
            .thenReturn(Optional.of(expiredCache));
        when(objectMapper.readValue(anyString(), eq(SalesSummaryDto.class)))
            .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});

        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(Optional.of(new BigDecimal("100.00")));
        when(salesTransactionRepository.countTransactionsByShopAndPeriod(anyString(), any(), any()))
            .thenReturn(1L);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary("shop-123", startDate, endDate);

        // Then
        assertNotNull(result);
        verify(objectMapper).readValue(anyString(), eq(SalesSummaryDto.class));
        // Should fall back to calculating fresh data
        assertEquals(0, new BigDecimal("100.00").compareTo(result.totalRevenue()));
    }
}