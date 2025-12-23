package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.FeatureFlag;
import com.princely.shopmanager.shared.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    // Error message constants
    private static final String ERROR_FLAG_NOT_FOUND = "Feature flag not found: ";

    private final FeatureFlagRepository featureFlagRepository;
    private final ShopRepository shopRepository;

    /**
     * Determines if a specific feature is enabled for a given shop.
     *
     * This method implements a hierarchical feature flag resolution:
     * 1. First checks for shop-specific feature flag override
     * 2. Falls back to global feature flag if no shop-specific flag exists
     * 3. Returns false if no feature flag is found (fail-safe default)
     *
     * The result is cached for performance optimization.
     *
     * @param shopId The ID of the shop to check (null for global check)
     * @param featureName The name of the feature to check
     * @return true if the feature is enabled and effective, false otherwise
     */
    @Cacheable(value = "feature-flags", key = "#shopId + '_' + #featureName")
    public boolean isFeatureEnabled(String shopId, String featureName) {
        // Check shop-specific feature flag first
        if (shopId != null) {
            Optional<Shop> shop = shopRepository.findById(shopId);
            if (shop.isPresent()) {
                Optional<FeatureFlag> shopFlag = featureFlagRepository.findByShopAndFeatureName(shop.get(), featureName);
                if (shopFlag.isPresent()) {
                    boolean enabled = shopFlag.get().isEffective();
                    log.debug("Shop-specific feature flag '{}' for shop '{}': {}", featureName, shopId, enabled);
                    return enabled;
                }
            }
        }

        // Fall back to global feature flag
        Optional<FeatureFlag> globalFlag = featureFlagRepository.findGlobalFeatureFlag(featureName);
        if (globalFlag.isPresent()) {
            boolean enabled = globalFlag.get().isEffective();
            log.debug("Global feature flag '{}': {}", featureName, enabled);
            return enabled;
        }

        // Default to false if no feature flag is found
        log.debug("Feature flag '{}' not found, defaulting to false", featureName);
        return false;
    }

    /**
     * Checks if a global feature is enabled across the entire system.
     *
     * This is a convenience method for checking global features without
     * specifying a shop context. It's equivalent to calling
     * isFeatureEnabled(null, featureName).
     *
     * @param featureName The name of the global feature to check
     * @return true if the global feature is enabled, false otherwise
     */
    @Cacheable(value = "feature-flags", key = "#featureName")
    public boolean isGlobalFeatureEnabled(String featureName) {
        return isFeatureEnabled(null, featureName);
    }

    /**
     * Creates a new feature flag for a shop or globally.
     *
     * This method creates a new feature flag with the specified parameters.
     * The flag can be shop-specific (if shopId is provided) or global
     * (if shopId is null). Creating a feature flag invalidates the
     * entire feature flag cache to ensure consistency.
     *
     * @param shopId The shop ID for shop-specific flags, null for global flags
     * @param featureName Unique name for the feature flag
     * @param enabled Initial enabled state of the feature
     * @param description Human-readable description of the feature
     * @param createdBy Username of the person creating the flag
     * @return The created FeatureFlag entity
     * @throws IllegalArgumentException if shop is not found or flag already exists
     */
    @Transactional
    @CacheEvict(value = "feature-flags", allEntries = true)
    public FeatureFlag createFeatureFlag(String shopId, String featureName, boolean enabled,
                                       String description, String createdBy) {
        Shop shop = null;
        if (shopId != null) {
            shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));
        }

        FeatureFlag featureFlag = FeatureFlag.builder()
            .shop(shop)
            .featureName(featureName)
            .enabled(enabled)
            .description(description)
            .createdBy(createdBy)
            .lastModifiedBy(createdBy)
            .build();

        featureFlag = featureFlagRepository.save(featureFlag);
        log.info("Created feature flag '{}' for shop '{}': {}", featureName, shopId, enabled);
        return featureFlag;
    }

    @Transactional
    @CacheEvict(value = "feature-flags", allEntries = true)
    public FeatureFlag updateFeatureFlag(String featureFlagId, boolean enabled, String modifiedBy) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_FLAG_NOT_FOUND + featureFlagId));

        featureFlag.setEnabled(enabled);
        featureFlag.setLastModifiedBy(modifiedBy);

        featureFlag = featureFlagRepository.save(featureFlag);
        log.info("Updated feature flag '{}': {}", featureFlag.getFeatureName(), enabled);
        return featureFlag;
    }

    @Transactional
    @CacheEvict(value = "feature-flags", allEntries = true)
    public FeatureFlag updateFeatureFlagSchedule(String featureFlagId, LocalDateTime effectiveFrom,
                                                LocalDateTime effectiveUntil, String modifiedBy) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_FLAG_NOT_FOUND + featureFlagId));

        featureFlag.setEffectiveFrom(effectiveFrom);
        featureFlag.setEffectiveUntil(effectiveUntil);
        featureFlag.setLastModifiedBy(modifiedBy);

        featureFlag = featureFlagRepository.save(featureFlag);
        log.info("Updated schedule for feature flag '{}': {} to {}",
            featureFlag.getFeatureName(), effectiveFrom, effectiveUntil);
        return featureFlag;
    }

    @Transactional
    @CacheEvict(value = "feature-flags", allEntries = true)
    public FeatureFlag updateFeatureFlagConfiguration(String featureFlagId, Map<String, String> configuration,
                                                     String modifiedBy) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_FLAG_NOT_FOUND + featureFlagId));

        featureFlag.getConfiguration().clear();
        featureFlag.getConfiguration().putAll(configuration);
        featureFlag.setLastModifiedBy(modifiedBy);

        featureFlag = featureFlagRepository.save(featureFlag);
        log.info("Updated configuration for feature flag '{}'", featureFlag.getFeatureName());
        return featureFlag;
    }

    public List<FeatureFlag> getFeatureFlagsForShop(String shopId) {
        if (shopId == null) {
            return featureFlagRepository.findGlobalFeatureFlags();
        }

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        return featureFlagRepository.findByShop(shop);
    }

    public List<FeatureFlag> getAllFeatureFlags() {
        return featureFlagRepository.findAll();
    }

    public String getFeatureFlagConfig(String shopId, String featureName, String configKey) {
        return getFeatureFlagConfig(shopId, featureName, configKey, null);
    }

    public String getFeatureFlagConfig(String shopId, String featureName, String configKey, String defaultValue) {
        // Check shop-specific feature flag first
        if (shopId != null) {
            Optional<Shop> shop = shopRepository.findById(shopId);
            if (shop.isPresent()) {
                Optional<FeatureFlag> shopFlag = featureFlagRepository.findByShopAndFeatureName(shop.get(), featureName);
                if (shopFlag.isPresent() && shopFlag.get().hasConfig(configKey)) {
                    return shopFlag.get().getConfig(configKey);
                }
            }
        }

        // Fall back to global feature flag
        Optional<FeatureFlag> globalFlag = featureFlagRepository.findGlobalFeatureFlag(featureName);
        if (globalFlag.isPresent() && globalFlag.get().hasConfig(configKey)) {
            return globalFlag.get().getConfig(configKey);
        }

        return defaultValue;
    }

    @Transactional
    @CacheEvict(value = "feature-flags", allEntries = true)
    public void deleteFeatureFlag(String featureFlagId) {
        FeatureFlag featureFlag = featureFlagRepository.findById(featureFlagId)
            .orElseThrow(() -> new IllegalArgumentException(ERROR_FLAG_NOT_FOUND + featureFlagId));

        featureFlagRepository.delete(featureFlag);
        log.info("Deleted feature flag '{}' for shop '{}'",
            featureFlag.getFeatureName(),
            featureFlag.getShop() != null ? featureFlag.getShop().getId() : "GLOBAL");
    }

    // Convenience methods for common feature flags
    public boolean isInvestmentEnabled(String shopId) {
        return isFeatureEnabled(shopId, FeatureFlag.INVESTMENT_ENABLED);
    }

    public boolean isAnalyticsEnabled(String shopId) {
        return isFeatureEnabled(shopId, FeatureFlag.ANALYTICS_ENABLED);
    }

    public boolean isFraudDetectionEnabled(String shopId) {
        return isFeatureEnabled(shopId, FeatureFlag.FRAUD_ENABLED);
    }

    public boolean isAdvancedReportingEnabled(String shopId) {
        return isFeatureEnabled(shopId, FeatureFlag.ADVANCED_REPORTING);
    }
}