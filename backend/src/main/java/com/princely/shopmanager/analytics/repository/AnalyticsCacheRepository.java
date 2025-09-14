package com.princely.shopmanager.analytics.repository;

import com.princely.shopmanager.analytics.domain.AnalyticsCache;
import com.princely.shopmanager.core.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalyticsCacheRepository extends JpaRepository<AnalyticsCache, String> {

    Optional<AnalyticsCache> findByShopAndAnalyticsTypeAndCacheKey(
        Shop shop, AnalyticsCache.AnalyticsType analyticsType, String cacheKey
    );

    @Query("SELECT ac FROM AnalyticsCache ac WHERE ac.shop.id = :shopId " +
           "AND ac.analyticsType = :type AND ac.cacheKey = :key")
    Optional<AnalyticsCache> findByShopIdAndTypeAndKey(
        @Param("shopId") String shopId,
        @Param("type") AnalyticsCache.AnalyticsType type,
        @Param("key") String key
    );

    List<AnalyticsCache> findByShop(Shop shop);

    List<AnalyticsCache> findByAnalyticsType(AnalyticsCache.AnalyticsType analyticsType);

    @Query("SELECT ac FROM AnalyticsCache ac WHERE ac.expiresAt <= :now")
    List<AnalyticsCache> findExpiredCache(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AnalyticsCache ac WHERE ac.expiresAt <= :now")
    long deleteExpiredCache(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AnalyticsCache ac WHERE ac.shop.id = :shopId")
    void deleteByShopId(@Param("shopId") String shopId);

    @Query("SELECT COUNT(ac) FROM AnalyticsCache ac WHERE ac.shop.id = :shopId")
    long countByShop(@Param("shopId") String shopId);

    @Query("SELECT ac FROM AnalyticsCache ac WHERE ac.shop.id = :shopId " +
           "AND ac.analyticsType = :type ORDER BY ac.cacheDate DESC")
    List<AnalyticsCache> findByShopAndTypeOrderByDateDesc(
        @Param("shopId") String shopId,
        @Param("type") AnalyticsCache.AnalyticsType type
    );

    boolean existsByShopAndAnalyticsTypeAndCacheKey(
        Shop shop, AnalyticsCache.AnalyticsType analyticsType, String cacheKey
    );
}