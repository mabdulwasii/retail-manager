package com.princely.shopmanager.analytics.service;

import com.princely.shopmanager.analytics.domain.AnalyticsCache;
import com.princely.shopmanager.analytics.dto.FraudStatisticsDto;
import com.princely.shopmanager.analytics.dto.InvestmentRoiDto;
import com.princely.shopmanager.analytics.dto.SalesSummaryDto;
import com.princely.shopmanager.analytics.repository.AnalyticsCacheRepository;
import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.investment.domain.InvestorDistribution;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.fraud.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

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

    @Mock
    private ShopAccessValidator shopAccessValidator;

    @Mock
    private ShopRepository shopRepository;

    private AnalyticsService analyticsService;

    private String testShopId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private JwtPrincipal testPrincipal;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        testShopId = "shop-1";
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();

        testShop = new Shop();
        testShop.setId(testShopId);
        testShop.setName("Test Shop");

        testPrincipal = JwtPrincipal.builder()
            .subject("test-user")
            .preferredUsername("testuser")
            .roles(List.of("MANAGER"))
            .tenantId("tenant-1")
            .shopId(testShopId)
            .build();

        // Mock shop repository for shop existence check (lenient to avoid unnecessary stubbing errors)
        lenient().when(shopRepository.existsById(testShopId)).thenReturn(true);

        analyticsService = new AnalyticsService(
            shopAccessValidator,
            shopRepository,
            salesTransactionRepository,
            investmentRepository,
            distributionRepository,
            analyticsCacheRepository,
            objectMapper
        );
    }

    @Test
    void testGetSalesSummary_FreshData() throws Exception {
        // Given
        BigDecimal totalRevenue = BigDecimal.valueOf(100000);
        long totalTransactions = 500;

        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(
            eq(testShopId), eq(AnalyticsCache.AnalyticsType.SALES_SUMMARY), anyString()))
            .thenReturn(Optional.empty());

        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(testShopId, startDate, endDate))
            .thenReturn(Optional.of(totalRevenue));

        when(salesTransactionRepository.countTransactionsByShopAndPeriod(testShopId, startDate, endDate))
            .thenReturn(totalTransactions);

        when(objectMapper.writeValueAsString(any(SalesSummaryDto.class)))
            .thenReturn("{\"shopId\":\"shop-1\"}");

        when(analyticsCacheRepository.save(any(AnalyticsCache.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(shopAccessValidator.hasNoAccessToShop(testShopId, testPrincipal)).thenReturn(false);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary(testShopId, startDate, endDate, testPrincipal);

        // Then
        assertNotNull(result);
        assertEquals(testShopId, result.shopId());
        assertEquals(startDate, result.periodStart());
        assertEquals(endDate, result.periodEnd());
        assertEquals(totalRevenue, result.totalRevenue());
        assertEquals(totalTransactions, result.totalTransactions());
        assertEquals(BigDecimal.valueOf(200.0).setScale(2), result.averageTransactionValue()); // 100000/500

        verify(analyticsCacheRepository).save(any(AnalyticsCache.class));
    }

    @Test
    void testGetSalesSummary_CachedData() throws Exception {
        // Given
        SalesSummaryDto cachedData = new SalesSummaryDto(
            testShopId,
            startDate,
            endDate,
            BigDecimal.valueOf(100000),
            500,
            BigDecimal.valueOf(200),
            LocalDateTime.now()
        );

        AnalyticsCache cache = AnalyticsCache.builder()
            .cacheData("{\"shopId\":\"shop-1\",\"totalRevenue\":100000}")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(
            eq(testShopId), eq(AnalyticsCache.AnalyticsType.SALES_SUMMARY), anyString()))
            .thenReturn(Optional.of(cache));

        when(objectMapper.readValue(anyString(), eq(SalesSummaryDto.class)))
            .thenReturn(cachedData);

        when(shopAccessValidator.hasNoAccessToShop(testShopId, testPrincipal)).thenReturn(false);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary(testShopId, startDate, endDate, testPrincipal);

        // Then
        assertNotNull(result);
        assertEquals(cachedData.shopId(), result.shopId());
        assertEquals(cachedData.totalRevenue(), result.totalRevenue());
        assertEquals(cachedData.totalTransactions(), result.totalTransactions());

        // Verify no fresh calculation was done
        verify(salesTransactionRepository, never()).getTotalRevenueByShopAndPeriod(anyString(), any(), any());
        verify(salesTransactionRepository, never()).countTransactionsByShopAndPeriod(anyString(), any(), any());
    }

    @Test
    void testGetInvestmentROI() throws Exception {
        // Given
        BigDecimal totalInvestmentAmount = BigDecimal.valueOf(50000);
        BigDecimal totalDistributions = BigDecimal.valueOf(10000);

        InvestorDistribution distribution1 = InvestorDistribution.builder()
            .distributionAmount(BigDecimal.valueOf(6000))
            .build();
        InvestorDistribution distribution2 = InvestorDistribution.builder()
            .distributionAmount(BigDecimal.valueOf(4000))
            .build();

        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(
            eq(testShopId), eq(AnalyticsCache.AnalyticsType.INVESTMENT_ROI), anyString()))
            .thenReturn(Optional.empty());

        when(investmentRepository.getTotalActiveInvestmentAmount(testShopId))
            .thenReturn(Optional.of(totalInvestmentAmount));

        when(distributionRepository.findByShopAndStatus(testShopId, InvestorDistribution.DistributionStatus.PAID))
            .thenReturn(List.of(distribution1, distribution2));

        when(objectMapper.writeValueAsString(any(InvestmentRoiDto.class)))
            .thenReturn("{\"shopId\":\"shop-1\"}");

        when(analyticsCacheRepository.save(any(AnalyticsCache.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(shopAccessValidator.hasNoAccessToShop(testShopId, testPrincipal)).thenReturn(false);

        // When
        InvestmentRoiDto result = analyticsService.getInvestmentROI(testShopId, startDate, endDate, testPrincipal);

        // Then
        assertNotNull(result);
        assertEquals(testShopId, result.shopId());
        assertEquals(totalInvestmentAmount, result.totalInvestmentAmount());
        assertEquals(totalDistributions, result.totalDistributions());
        assertEquals(0, BigDecimal.valueOf(20.0).compareTo(result.roiPercentage())); // (10000/50000)*100
    }

    @Test
    void testGetFraudStatistics() throws Exception {
        // Given
        long totalLowRisk = 100;
        long totalMediumRisk = 50;
        long totalHighRisk = 30;
        long totalCriticalRisk = 5;

        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(
            eq(testShopId), eq(AnalyticsCache.AnalyticsType.FRAUD_STATISTICS), anyString()))
            .thenReturn(Optional.empty());

        // No risk assessment mocking needed since fraud detection is disabled in tests

        when(objectMapper.writeValueAsString(any(FraudStatisticsDto.class)))
            .thenReturn("{\"shopId\":\"shop-1\"}");

        when(analyticsCacheRepository.save(any(AnalyticsCache.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(shopAccessValidator.hasNoAccessToShop(testShopId, testPrincipal)).thenReturn(false);

        // When
        FraudStatisticsDto result = analyticsService.getFraudStatistics(testShopId, startDate, endDate, testPrincipal);

        // Then
        assertNotNull(result);
        assertEquals(testShopId, result.shopId());
        assertEquals(0L, result.totalAssessments()); // No assessments in test since fraud is disabled
        assertEquals(0L, result.highRiskCount()); // No assessments in test since fraud is disabled
        assertEquals(0L, result.criticalRiskCount()); // No assessments in test since fraud is disabled
        assertEquals(0, BigDecimal.ZERO.compareTo(result.riskRate())); // No assessments means 0% risk rate
    }

    @Test
    void testClearExpiredCache() {
        // Given
        when(analyticsCacheRepository.deleteExpiredCache(any(LocalDateTime.class)))
            .thenReturn(15L);

        // When
        analyticsService.clearExpiredCache();

        // Then
        verify(analyticsCacheRepository).deleteExpiredCache(any(LocalDateTime.class));
    }

    @Test
    void testClearCacheForShop() {
        // Given
        String shopId = "shop-1";

        when(shopAccessValidator.hasNoAccessToShop(shopId, testPrincipal)).thenReturn(false);

        // When
        analyticsService.clearCacheForShop(shopId, testPrincipal);

        // Then
        verify(analyticsCacheRepository).deleteByShopId(shopId);
    }

    @Test
    void testGetSalesSummary_ZeroTransactions() throws Exception {
        // Given
        when(analyticsCacheRepository.findByShopIdAndTypeAndKey(
            eq(testShopId), eq(AnalyticsCache.AnalyticsType.SALES_SUMMARY), anyString()))
            .thenReturn(Optional.empty());

        when(salesTransactionRepository.getTotalRevenueByShopAndPeriod(testShopId, startDate, endDate))
            .thenReturn(Optional.of(BigDecimal.ZERO));

        when(salesTransactionRepository.countTransactionsByShopAndPeriod(testShopId, startDate, endDate))
            .thenReturn(0L);

        when(objectMapper.writeValueAsString(any(SalesSummaryDto.class)))
            .thenReturn("{\"shopId\":\"shop-1\"}");

        when(analyticsCacheRepository.save(any(AnalyticsCache.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(shopAccessValidator.hasNoAccessToShop(testShopId, testPrincipal)).thenReturn(false);

        // When
        SalesSummaryDto result = analyticsService.getSalesSummary(testShopId, startDate, endDate, testPrincipal);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0L, result.totalTransactions());
        assertEquals(BigDecimal.ZERO, result.averageTransactionValue());
    }
}