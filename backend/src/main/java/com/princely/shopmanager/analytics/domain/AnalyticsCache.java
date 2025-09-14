package com.princely.shopmanager.analytics.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_cache", indexes = {
    @Index(name = "idx_analytics_shop", columnList = "shop_id"),
    @Index(name = "idx_analytics_key", columnList = "cache_key"),
    @Index(name = "idx_analytics_type", columnList = "analytics_type"),
    @Index(name = "idx_analytics_date", columnList = "cache_date"),
    @Index(name = "idx_analytics_expiry", columnList = "expires_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_analytics_cache", columnNames = {"shop_id", "cache_key", "analytics_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop"})
public class AnalyticsCache extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "analytics_type", nullable = false)
    private AnalyticsType analyticsType;

    @Column(name = "cache_key", nullable = false)
    private String cacheKey;

    @Column(name = "cache_data", nullable = false, columnDefinition = "TEXT")
    private String cacheData;

    @Column(name = "cache_date", nullable = false)
    private LocalDateTime cacheDate;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "cache_version")
    private String cacheVersion;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    public enum AnalyticsType {
        SALES_SUMMARY,
        PRODUCT_PERFORMANCE,
        CATEGORY_PERFORMANCE,
        INVESTMENT_ROI,
        FRAUD_STATISTICS,
        REVENUE_TREND,
        CUSTOMER_ANALYTICS,
        INVENTORY_ANALYTICS
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void refresh(String newData, LocalDateTime newExpiresAt) {
        this.cacheData = newData;
        this.cacheDate = LocalDateTime.now();
        this.expiresAt = newExpiresAt;
    }

    public static String generateCacheKey(String prefix, Object... params) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object param : params) {
            key.append("_").append(param);
        }
        return key.toString();
    }
}