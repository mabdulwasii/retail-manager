package com.princely.shopmanager.aggregator.service;

import com.princely.shopmanager.aggregator.domain.CloudShop;
import com.princely.shopmanager.aggregator.domain.CloudSubscription;
import com.princely.shopmanager.aggregator.domain.CloudTenant;
import com.princely.shopmanager.aggregator.dto.ShopPerformanceDto;
import com.princely.shopmanager.aggregator.dto.SyncStatusDto;
import com.princely.shopmanager.aggregator.dto.TenantAnalyticsDto;
import com.princely.shopmanager.aggregator.repository.CloudShopRepository;
import com.princely.shopmanager.aggregator.repository.CloudSubscriptionRepository;
import com.princely.shopmanager.aggregator.repository.CloudTenantRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for aggregating analytics across cloud tenants and shops.
 * Provides cross-shop reporting and tenant-level insights.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudAnalyticsService {

    private final CloudTenantRepository tenantRepository;
    private final CloudShopRepository shopRepository;
    private final CloudSubscriptionRepository subscriptionRepository;

    /**
     * Get tenant-level analytics aggregated across all shops.
     */
    public TenantAnalyticsDto getTenantAnalytics(String tenantId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        log.info("Getting analytics for tenant: {} (period: {} to {})", tenantId, periodStart, periodEnd);

        // Validate tenant exists
        CloudTenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                        "Cloud tenant not found: " + tenantId));

        // Get all shops for this tenant
        List<CloudShop> shops = shopRepository.findByCloudTenant_Id(tenantId);

        // Get subscription
        CloudSubscription subscription = subscriptionRepository
                .findByTenantIdAndStatus(tenantId, CloudSubscription.Status.ACTIVE)
                .or(() -> subscriptionRepository.findByTenantIdAndStatus(tenantId, CloudSubscription.Status.TRIAL))
                .orElse(null);

        // Calculate shop metrics
        long totalShops = shops.size();
        long activeShops = shops.stream().filter(CloudShop::isActive).count();
        long suspendedShops = shops.stream().filter(shop -> !shop.isActive()).count();

        // Calculate API usage (from subscription)
        Long apiRequests = subscription != null ? subscription.getCurrentApiRequests() : 0L;
        Long apiRemaining = subscription != null && subscription.getMaxApiRequestsPerMonth() != null
                ? Math.max(0, subscription.getMaxApiRequestsPerMonth() - subscription.getCurrentApiRequests())
                : null;

        // Calculate storage usage (from subscription)
        BigDecimal storageUsed = subscription != null ? subscription.getCurrentStorageGb() : BigDecimal.ZERO;
        BigDecimal storageRemaining = subscription != null && subscription.getStorageLimitGb() != null
                ? new BigDecimal(subscription.getStorageLimitGb()).subtract(subscription.getCurrentStorageGb())
                : null;

        // Build shop performance list
        List<ShopPerformanceDto> shopPerformance = shops.stream()
                .map(shop -> ShopPerformanceDto.builder()
                        .shopId(shop.getId())
                        .shopName(shop.getShopName())
                        .status(shop.getStatus().name())
                        // Placeholder values - would come from shop-specific sync data
                        .revenue(BigDecimal.ZERO)
                        .profit(BigDecimal.ZERO)
                        .transactions(0L)
                        .averageTransactionValue(BigDecimal.ZERO)
                        .totalProducts(0L)
                        .inventoryValue(BigDecimal.ZERO)
                        .userCount(0)
                        .build())
                .collect(Collectors.toList());

        return TenantAnalyticsDto.builder()
                .tenantId(tenantId)
                .tenantName(tenant.getTenantName())
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                // Shop metrics
                .totalShops((int) totalShops)
                .activeShops((int) activeShops)
                .suspendedShops((int) suspendedShops)
                // Placeholder metrics (would aggregate from synced shop data)
                .totalRevenue(BigDecimal.ZERO)
                .totalProfit(BigDecimal.ZERO)
                .totalTransactions(0L)
                .averageTransactionValue(BigDecimal.ZERO)
                .totalProducts(0L)
                .totalInventoryBatches(0L)
                .totalInventoryValue(BigDecimal.ZERO)
                .totalInvestments(BigDecimal.ZERO)
                .totalInvestorShares(BigDecimal.ZERO)
                .totalDistributions(BigDecimal.ZERO)
                .totalUsers(0)
                .activeUsers(0)
                // API and storage usage
                .apiRequestsThisPeriod(apiRequests)
                .apiRequestsRemaining(apiRemaining)
                .storageUsedGb(storageUsed)
                .storageRemainingGb(storageRemaining)
                // Shop performance
                .topShopsByRevenue(shopPerformance)
                // Growth metrics (placeholder)
                .revenueGrowthPercentage(BigDecimal.ZERO)
                .profitGrowthPercentage(BigDecimal.ZERO)
                .build();
    }

    /**
     * Get sync status for all shops under a tenant.
     */
    public List<SyncStatusDto> getShopSyncStatus(String tenantId) {
        log.info("Getting sync status for all shops under tenant: {}", tenantId);

        // Validate tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new BusinessException(ErrorCode.CLOUD_TENANT_NOT_FOUND,
                    "Cloud tenant not found: " + tenantId);
        }

        // Get all shops
        List<CloudShop> shops = shopRepository.findByCloudTenant_Id(tenantId);

        // Build sync status for each shop
        return shops.stream()
                .map(shop -> {
                    // Calculate days since last sync (using createdAt as placeholder)
                    LocalDateTime lastSync = shop.getUpdatedAt() != null ? shop.getUpdatedAt() : shop.getCreatedAt();
                    long daysSinceSync = lastSync != null ? ChronoUnit.DAYS.between(lastSync, LocalDateTime.now()) : -1;

                    // Determine sync health
                    String syncHealth;
                    if (daysSinceSync < 0) {
                        syncHealth = "UNKNOWN";
                    } else if (daysSinceSync <= 1) {
                        syncHealth = "HEALTHY";
                    } else if (daysSinceSync <= 7) {
                        syncHealth = "WARNING";
                    } else {
                        syncHealth = "CRITICAL";
                    }

                    return SyncStatusDto.builder()
                            .shopId(shop.getId())
                            .shopName(shop.getShopName())
                            .lastSyncAt(lastSync)
                            .lastSyncStatus(shop.isActive() ? "SUCCESS" : "INACTIVE")
                            .daysSinceLastSync(daysSinceSync >= 0 ? daysSinceSync : null)
                            .syncHealth(syncHealth)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get overview statistics across all tenants (admin use).
     */
    public PlatformOverviewDto getPlatformOverview() {
        log.info("Getting platform-wide overview statistics");

        // Count tenants
        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.findByStatus(CloudTenant.Status.ACTIVE).size();

        // Count shops
        long totalShops = shopRepository.count();

        // Count subscriptions by tier
        List<Object[]> subscriptionCounts = subscriptionRepository.countActiveSubscriptionsByTier();

        return PlatformOverviewDto.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenants)
                .totalShops(totalShops)
                .subscriptionCountsByTier(subscriptionCounts)
                .build();
    }

    /**
     * DTO for platform-wide overview (internal use).
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PlatformOverviewDto {
        private Long totalTenants;
        private Long activeTenants;
        private Long totalShops;
        private List<Object[]> subscriptionCountsByTier;
    }
}
