package com.princely.shopmanager.analytics.service;

import com.princely.shopmanager.analytics.domain.AnalyticsCache;
import com.princely.shopmanager.analytics.dto.SalesSummaryDto;
import com.princely.shopmanager.analytics.dto.InvestmentRoiDto;
import com.princely.shopmanager.analytics.dto.FraudStatisticsDto;
import com.princely.shopmanager.analytics.dto.RevenueAnalyticsDto;
import com.princely.shopmanager.analytics.repository.AnalyticsCacheRepository;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.investment.repository.InvestmentRepository;
import com.princely.shopmanager.investment.repository.InvestorDistributionRepository;
import com.princely.shopmanager.investment.repository.RiskAssessmentRepository;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.features.analytics.enabled", havingValue = "true")
public class AnalyticsService {

    private final SalesTransactionRepository salesTransactionRepository;
    private final InvestmentRepository investmentRepository;
    private final InvestorDistributionRepository distributionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AnalyticsCacheRepository analyticsCacheRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public SalesSummaryDto getSalesSummary(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        return calculateSalesSummaryInternal(shopId, startDate, endDate);
    }

    private SalesSummaryDto calculateSalesSummaryInternal(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String cacheKey = AnalyticsCache.generateCacheKey("sales_summary", shopId, startDate, endDate);

        Optional<AnalyticsCache> cached = getCachedAnalytics(shopId, AnalyticsCache.AnalyticsType.SALES_SUMMARY, cacheKey);
        if (cached.isPresent() && !cached.get().isExpired()) {
            try {
                return objectMapper.readValue(cached.get().getCacheData(), SalesSummaryDto.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached sales summary, recalculating", e);
            }
        }

        // Calculate fresh data
        BigDecimal totalRevenue = salesTransactionRepository
            .getTotalRevenueByShopAndPeriod(shopId, startDate, endDate)
            .orElse(BigDecimal.ZERO);

        long totalTransactions = salesTransactionRepository
            .countTransactionsByShopAndPeriod(shopId, startDate, endDate);

        BigDecimal averageTransactionValue = totalTransactions > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        SalesSummaryDto summary = new SalesSummaryDto(
            shopId,
            startDate,
            endDate,
            totalRevenue,
            totalTransactions,
            averageTransactionValue,
            LocalDateTime.now()
        );

        // Cache the result
        cacheAnalytics(shopId, AnalyticsCache.AnalyticsType.SALES_SUMMARY, cacheKey, summary);

        return summary;
    }

    @Transactional(readOnly = true)
    public InvestmentRoiDto getInvestmentROI(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String cacheKey = AnalyticsCache.generateCacheKey("investment_roi", shopId, startDate, endDate);

        Optional<AnalyticsCache> cached = getCachedAnalytics(shopId, AnalyticsCache.AnalyticsType.INVESTMENT_ROI, cacheKey);
        if (cached.isPresent() && !cached.get().isExpired()) {
            try {
                return objectMapper.readValue(cached.get().getCacheData(), InvestmentRoiDto.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached investment ROI, recalculating", e);
            }
        }

        // Calculate fresh data
        BigDecimal totalInvestmentAmount = investmentRepository
            .getTotalActiveInvestmentAmount(shopId)
            .orElse(BigDecimal.ZERO);

        BigDecimal totalDistributions = distributionRepository
            .findByShopAndStatus(shopId, com.princely.shopmanager.investment.domain.InvestorDistribution.DistributionStatus.PAID)
            .stream()
            .map(dist -> dist.getDistributionAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal roiPercentage = totalInvestmentAmount.compareTo(BigDecimal.ZERO) > 0 ?
            totalDistributions.divide(totalInvestmentAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;

        InvestmentRoiDto roi = new InvestmentRoiDto(
            shopId,
            startDate,
            endDate,
            totalInvestmentAmount,
            totalDistributions,
            roiPercentage,
            LocalDateTime.now()
        );

        // Cache the result
        cacheAnalytics(shopId, AnalyticsCache.AnalyticsType.INVESTMENT_ROI, cacheKey, roi);

        return roi;
    }

    @Transactional(readOnly = true)
    public FraudStatisticsDto getFraudStatistics(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        String cacheKey = AnalyticsCache.generateCacheKey("fraud_stats", shopId, startDate, endDate);

        Optional<AnalyticsCache> cached = getCachedAnalytics(shopId, AnalyticsCache.AnalyticsType.FRAUD_STATISTICS, cacheKey);
        if (cached.isPresent() && !cached.get().isExpired()) {
            try {
                return objectMapper.readValue(cached.get().getCacheData(), FraudStatisticsDto.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached fraud statistics, recalculating", e);
            }
        }

        // Calculate fresh data
        long totalAssessments = riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
            shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.LOW, startDate, endDate) +
            riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
                shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.MEDIUM, startDate, endDate) +
            riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
                shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.HIGH, startDate, endDate) +
            riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
                shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.CRITICAL, startDate, endDate);

        long highRiskAssessments = riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
            shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.HIGH, startDate, endDate);

        long criticalRiskAssessments = riskAssessmentRepository.countByShopAndRiskLevelAndDateRange(
            shopId, com.princely.shopmanager.investment.domain.RiskAssessment.RiskLevel.CRITICAL, startDate, endDate);

        BigDecimal riskRate = totalAssessments > 0 ?
            BigDecimal.valueOf(highRiskAssessments + criticalRiskAssessments)
                .divide(BigDecimal.valueOf(totalAssessments), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;

        FraudStatisticsDto fraudStats = new FraudStatisticsDto(
            shopId,
            startDate,
            endDate,
            totalAssessments,
            highRiskAssessments,
            criticalRiskAssessments,
            riskRate,
            LocalDateTime.now()
        );

        // Cache the result
        cacheAnalytics(shopId, AnalyticsCache.AnalyticsType.FRAUD_STATISTICS, cacheKey, fraudStats);

        return fraudStats;
    }

    @Transactional(readOnly = true)
    public RevenueAnalyticsDto getRevenueAnalytics(String shopId, LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate periods first
        long periodDays = startDate.until(endDate, java.time.temporal.ChronoUnit.DAYS);
        LocalDateTime prevPeriodStart = startDate.minusDays(periodDays);
        LocalDateTime prevPeriodEnd = startDate.minusDays(1);

        // Get sales data for both periods - call internal methods to avoid transaction proxy issues
        SalesSummaryDto currentPeriod = calculateSalesSummaryInternal(shopId, startDate, endDate);
        SalesSummaryDto previousPeriod = calculateSalesSummaryInternal(shopId, prevPeriodStart, prevPeriodEnd);

        BigDecimal growthRate = previousPeriod.totalRevenue().compareTo(BigDecimal.ZERO) > 0 ?
            currentPeriod.totalRevenue()
                .subtract(previousPeriod.totalRevenue())
                .divide(previousPeriod.totalRevenue(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;

        return new RevenueAnalyticsDto(
            shopId,
            startDate,
            endDate,
            currentPeriod.totalRevenue(),
            previousPeriod.totalRevenue(),
            growthRate,
            currentPeriod.totalTransactions(),
            previousPeriod.totalTransactions(),
            LocalDateTime.now()
        );
    }

    private Optional<AnalyticsCache> getCachedAnalytics(String shopId, AnalyticsCache.AnalyticsType type, String cacheKey) {
        return analyticsCacheRepository.findByShopIdAndTypeAndKey(shopId, type, cacheKey);
    }

    private void cacheAnalytics(String shopId, AnalyticsCache.AnalyticsType type, String cacheKey, Object data) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // Cache for 1 hour

            Optional<AnalyticsCache> existing = getCachedAnalytics(shopId, type, cacheKey);
            if (existing.isPresent()) {
                existing.get().refresh(jsonData, expiresAt);
                analyticsCacheRepository.save(existing.get());
            } else {
                Shop shop = new Shop();
                shop.setId(shopId);

                AnalyticsCache cache = AnalyticsCache.builder()
                    .shop(shop)
                    .analyticsType(type)
                    .cacheKey(cacheKey)
                    .cacheData(jsonData)
                    .cacheDate(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .build();

                analyticsCacheRepository.save(cache);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to cache analytics data", e);
        }
    }

    @Transactional
    public void clearExpiredCache() {
        long deletedCount = analyticsCacheRepository.deleteExpiredCache(LocalDateTime.now());
        log.info("Cleared {} expired analytics cache entries", deletedCount);
    }

    @Transactional
    public void clearCacheForShop(String shopId) {
        analyticsCacheRepository.deleteByShopId(shopId);
        log.info("Cleared all analytics cache for shop {}", shopId);
    }
}