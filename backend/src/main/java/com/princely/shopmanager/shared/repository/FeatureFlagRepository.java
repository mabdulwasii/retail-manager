package com.princely.shopmanager.shared.repository;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, String> {

    List<FeatureFlag> findByShop(Shop shop);

    Optional<FeatureFlag> findByShopAndFeatureName(Shop shop, String featureName);

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.shop IS NULL")
    List<FeatureFlag> findGlobalFeatureFlags();

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.shop IS NULL AND ff.featureName = :featureName")
    Optional<FeatureFlag> findGlobalFeatureFlag(@Param("featureName") String featureName);

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.enabled = true " +
           "AND (ff.effectiveFrom IS NULL OR ff.effectiveFrom <= CURRENT_TIMESTAMP) " +
           "AND (ff.effectiveUntil IS NULL OR ff.effectiveUntil > CURRENT_TIMESTAMP)")
    List<FeatureFlag> findActiveFeatureFlags();

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.shop = :shop " +
           "AND ff.enabled = true " +
           "AND (ff.effectiveFrom IS NULL OR ff.effectiveFrom <= CURRENT_TIMESTAMP) " +
           "AND (ff.effectiveUntil IS NULL OR ff.effectiveUntil > CURRENT_TIMESTAMP)")
    List<FeatureFlag> findActiveFeatureFlagsForShop(@Param("shop") Shop shop);

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.shop IS NULL " +
           "AND ff.enabled = true " +
           "AND (ff.effectiveFrom IS NULL OR ff.effectiveFrom <= CURRENT_TIMESTAMP) " +
           "AND (ff.effectiveUntil IS NULL OR ff.effectiveUntil > CURRENT_TIMESTAMP)")
    List<FeatureFlag> findActiveGlobalFeatureFlags();

    List<FeatureFlag> findByFeatureName(String featureName);

    @Query("SELECT COUNT(ff) FROM FeatureFlag ff WHERE ff.shop IS NULL")
    long countGlobalFeatureFlags();

    @Query("SELECT COUNT(ff) FROM FeatureFlag ff WHERE ff.shop = :shop")
    long countFeatureFlagsForShop(@Param("shop") Shop shop);

    boolean existsByShopAndFeatureName(Shop shop, String featureName);

    @Query("SELECT ff FROM FeatureFlag ff WHERE ff.shop IS NULL AND ff.featureName = :featureName " +
           "OR ff.shop = :shop AND ff.featureName = :featureName " +
           "ORDER BY ff.shop NULLS FIRST")
    List<FeatureFlag> findApplicableFeatureFlags(@Param("shop") Shop shop, @Param("featureName") String featureName);
}