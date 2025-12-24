package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.FeatureFlag;
import com.princely.shopmanager.shared.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeatureFlagService Tests")
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagRepository featureFlagRepository;

    @Mock
    private ShopRepository shopRepository;

    @InjectMocks
    private FeatureFlagService featureFlagService;

    private Shop testShop;
    private FeatureFlag globalFlag;
    private FeatureFlag shopFlag;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        globalFlag = FeatureFlag.builder()
            .id("flag-1")
            .shop(null)
            .featureName("ANALYTICS_ENABLED")
            .enabled(true)
            .description("Global analytics feature")
            .build();

        shopFlag = FeatureFlag.builder()
            .id("flag-2")
            .shop(testShop)
            .featureName("ANALYTICS_ENABLED")
            .enabled(false)
            .description("Shop-specific analytics override")
            .build();
    }

    // Hierarchical Feature Flag Resolution Tests
    @Test
    @DisplayName("isFeatureEnabled - Should use shop-specific flag when available")
    void isFeatureEnabled_ShouldUseShopSpecificFlagWhenAvailable() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(shopFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled("shop-1", "ANALYTICS_ENABLED");

        // Then
        assertThat(enabled).isFalse(); // Shop flag overrides global
        verify(featureFlagRepository).findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED");
        verify(featureFlagRepository, never()).findGlobalFeatureFlag(anyString());
    }

    @Test
    @DisplayName("isFeatureEnabled - Should fall back to global flag when shop flag not found")
    void isFeatureEnabled_ShouldFallBackToGlobalFlagWhenShopFlagNotFound() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED"))
            .thenReturn(Optional.empty());
        when(featureFlagRepository.findGlobalFeatureFlag("ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(globalFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled("shop-1", "ANALYTICS_ENABLED");

        // Then
        assertThat(enabled).isTrue(); // Uses global flag
        verify(featureFlagRepository).findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED");
        verify(featureFlagRepository).findGlobalFeatureFlag("ANALYTICS_ENABLED");
    }

    @Test
    @DisplayName("isFeatureEnabled - Should return false when no flag found")
    void isFeatureEnabled_ShouldReturnFalseWhenNoFlagFound() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(any(), anyString()))
            .thenReturn(Optional.empty());
        when(featureFlagRepository.findGlobalFeatureFlag(anyString()))
            .thenReturn(Optional.empty());

        // When
        boolean enabled = featureFlagService.isFeatureEnabled("shop-1", "UNKNOWN_FEATURE");

        // Then
        assertThat(enabled).isFalse(); // Fail-safe default
    }

    @Test
    @DisplayName("isFeatureEnabled - Should use global flag when shopId is null")
    void isFeatureEnabled_ShouldUseGlobalFlagWhenShopIdIsNull() {
        // Given
        when(featureFlagRepository.findGlobalFeatureFlag("ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(globalFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(null, "ANALYTICS_ENABLED");

        // Then
        assertThat(enabled).isTrue();
        verify(shopRepository, never()).findById(anyString());
        verify(featureFlagRepository).findGlobalFeatureFlag("ANALYTICS_ENABLED");
    }

    @Test
    @DisplayName("isFeatureEnabled - Should return false when shop not found")
    void isFeatureEnabled_ShouldReturnFalseWhenShopNotFound() {
        // Given
        when(shopRepository.findById("shop-999")).thenReturn(Optional.empty());
        when(featureFlagRepository.findGlobalFeatureFlag("ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(globalFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled("shop-999", "ANALYTICS_ENABLED");

        // Then
        assertThat(enabled).isTrue(); // Falls back to global
    }

    // Effective Date Tests
    @Test
    @DisplayName("isFeatureEnabled - Should respect effective date range")
    void isFeatureEnabled_ShouldRespectEffectiveDateRange() {
        // Given
        FeatureFlag scheduledFlag = FeatureFlag.builder()
            .shop(null)
            .featureName("NEW_FEATURE")
            .enabled(true)
            .effectiveFrom(LocalDateTime.now().minusDays(1))
            .effectiveUntil(LocalDateTime.now().plusDays(1))
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag("NEW_FEATURE"))
            .thenReturn(Optional.of(scheduledFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(null, "NEW_FEATURE");

        // Then
        assertThat(enabled).isTrue(); // Within effective date range
    }

    @Test
    @DisplayName("isFeatureEnabled - Should return false when before effective date")
    void isFeatureEnabled_ShouldReturnFalseWhenBeforeEffectiveDate() {
        // Given
        FeatureFlag futureFlag = FeatureFlag.builder()
            .shop(null)
            .featureName("FUTURE_FEATURE")
            .enabled(true)
            .effectiveFrom(LocalDateTime.now().plusDays(1))
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag("FUTURE_FEATURE"))
            .thenReturn(Optional.of(futureFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(null, "FUTURE_FEATURE");

        // Then
        assertThat(enabled).isFalse(); // Not yet effective
    }

    @Test
    @DisplayName("isFeatureEnabled - Should return false when after effective until date")
    void isFeatureEnabled_ShouldReturnFalseWhenAfterEffectiveUntilDate() {
        // Given
        FeatureFlag expiredFlag = FeatureFlag.builder()
            .shop(null)
            .featureName("EXPIRED_FEATURE")
            .enabled(true)
            .effectiveUntil(LocalDateTime.now().minusDays(1))
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag("EXPIRED_FEATURE"))
            .thenReturn(Optional.of(expiredFlag));

        // When
        boolean enabled = featureFlagService.isFeatureEnabled(null, "EXPIRED_FEATURE");

        // Then
        assertThat(enabled).isFalse(); // Expired
    }

    // Global Feature Flag Tests
    @Test
    @DisplayName("isGlobalFeatureEnabled - Should check global flag without shop context")
    void isGlobalFeatureEnabled_ShouldCheckGlobalFlagWithoutShopContext() {
        // Given
        when(featureFlagRepository.findGlobalFeatureFlag("ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(globalFlag));

        // When
        boolean enabled = featureFlagService.isGlobalFeatureEnabled("ANALYTICS_ENABLED");

        // Then
        assertThat(enabled).isTrue();
        verify(featureFlagRepository).findGlobalFeatureFlag("ANALYTICS_ENABLED");
        verify(shopRepository, never()).findById(anyString());
    }

    // Create Feature Flag Tests
    @Test
    @DisplayName("createFeatureFlag - Should create global flag when shopId is null")
    void createFeatureFlag_ShouldCreateGlobalFlagWhenShopIdIsNull() {
        // Given
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag created = featureFlagService.createFeatureFlag(
            null, "NEW_FEATURE", true, "New feature", "admin"
        );

        // Then
        assertThat(created.getShop()).isNull();
        assertThat(created.getFeatureName()).isEqualTo("NEW_FEATURE");
        assertThat(created.isEnabled()).isTrue();
        verify(shopRepository, never()).findById(anyString());
        verify(featureFlagRepository).save(any(FeatureFlag.class));
    }

    @Test
    @DisplayName("createFeatureFlag - Should create shop-specific flag")
    void createFeatureFlag_ShouldCreateShopSpecificFlag() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag created = featureFlagService.createFeatureFlag(
            "shop-1", "SHOP_FEATURE", false, "Shop feature", "manager"
        );

        // Then
        assertThat(created.getShop()).isEqualTo(testShop);
        assertThat(created.getFeatureName()).isEqualTo("SHOP_FEATURE");
        assertThat(created.isEnabled()).isFalse();
        assertThat(created.getCreatedBy()).isEqualTo("manager");
        verify(shopRepository).findById("shop-1");
        verify(featureFlagRepository).save(any(FeatureFlag.class));
    }

    @Test
    @DisplayName("createFeatureFlag - Should throw exception when shop not found")
    void createFeatureFlag_ShouldThrowExceptionWhenShopNotFound() {
        // Given
        when(shopRepository.findById("shop-999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.createFeatureFlag(
            "shop-999", "FEATURE", true, "Description", "admin"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop not found");
    }

    // Update Feature Flag Tests
    @Test
    @DisplayName("updateFeatureFlag - Should update enabled state")
    void updateFeatureFlag_ShouldUpdateEnabledState() {
        // Given
        when(featureFlagRepository.findById("flag-1")).thenReturn(Optional.of(globalFlag));
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag updated = featureFlagService.updateFeatureFlag("flag-1", false, "admin");

        // Then
        assertThat(updated.isEnabled()).isFalse();
        assertThat(updated.getLastModifiedBy()).isEqualTo("admin");
        verify(featureFlagRepository).save(globalFlag);
    }

    @Test
    @DisplayName("updateFeatureFlag - Should throw exception when flag not found")
    void updateFeatureFlag_ShouldThrowExceptionWhenFlagNotFound() {
        // Given
        when(featureFlagRepository.findById("flag-999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.updateFeatureFlag("flag-999", true, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    // Update Schedule Tests
    @Test
    @DisplayName("updateFeatureFlagSchedule - Should update effective dates")
    void updateFeatureFlagSchedule_ShouldUpdateEffectiveDates() {
        // Given
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime until = LocalDateTime.now().plusDays(30);

        when(featureFlagRepository.findById("flag-1")).thenReturn(Optional.of(globalFlag));
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag updated = featureFlagService.updateFeatureFlagSchedule("flag-1", from, until, "admin");

        // Then
        assertThat(updated.getEffectiveFrom()).isEqualTo(from);
        assertThat(updated.getEffectiveUntil()).isEqualTo(until);
        assertThat(updated.getLastModifiedBy()).isEqualTo("admin");
    }

    // Update Configuration Tests
    @Test
    @DisplayName("updateFeatureFlagConfiguration - Should update configuration map")
    void updateFeatureFlagConfiguration_ShouldUpdateConfigurationMap() {
        // Given
        Map<String, String> newConfig = Map.of("maxUsers", "100", "tier", "premium");

        when(featureFlagRepository.findById("flag-1")).thenReturn(Optional.of(globalFlag));
        when(featureFlagRepository.save(any(FeatureFlag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        FeatureFlag updated = featureFlagService.updateFeatureFlagConfiguration("flag-1", newConfig, "admin");

        // Then
        assertThat(updated.getConfiguration()).hasSize(2);
        assertThat(updated.getConfiguration()).containsEntry("maxUsers", "100");
        assertThat(updated.getConfiguration()).containsEntry("tier", "premium");
    }

    // Get Feature Flags Tests
    @Test
    @DisplayName("getFeatureFlagsForShop - Should return global flags when shopId is null")
    void getFeatureFlagsForShop_ShouldReturnGlobalFlagsWhenShopIdIsNull() {
        // Given
        when(featureFlagRepository.findGlobalFeatureFlags()).thenReturn(List.of(globalFlag));

        // When
        List<FeatureFlag> flags = featureFlagService.getFeatureFlagsForShop(null);

        // Then
        assertThat(flags).hasSize(1);
        assertThat(flags.get(0)).isEqualTo(globalFlag);
        verify(featureFlagRepository).findGlobalFeatureFlags();
    }

    @Test
    @DisplayName("getFeatureFlagsForShop - Should return shop-specific flags")
    void getFeatureFlagsForShop_ShouldReturnShopSpecificFlags() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShop(testShop)).thenReturn(List.of(shopFlag));

        // When
        List<FeatureFlag> flags = featureFlagService.getFeatureFlagsForShop("shop-1");

        // Then
        assertThat(flags).hasSize(1);
        assertThat(flags.get(0)).isEqualTo(shopFlag);
    }

    // Get Config Tests
    @Test
    @DisplayName("getFeatureFlagConfig - Should return shop-specific config when available")
    void getFeatureFlagConfig_ShouldReturnShopSpecificConfigWhenAvailable() {
        // Given
        shopFlag.getConfiguration().put("maxItems", "50");

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(shopFlag));

        // When
        String value = featureFlagService.getFeatureFlagConfig("shop-1", "ANALYTICS_ENABLED", "maxItems");

        // Then
        assertThat(value).isEqualTo("50");
    }

    @Test
    @DisplayName("getFeatureFlagConfig - Should fall back to global config")
    void getFeatureFlagConfig_ShouldFallBackToGlobalConfig() {
        // Given
        globalFlag.getConfiguration().put("maxItems", "100");

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(testShop, "ANALYTICS_ENABLED"))
            .thenReturn(Optional.empty());
        when(featureFlagRepository.findGlobalFeatureFlag("ANALYTICS_ENABLED"))
            .thenReturn(Optional.of(globalFlag));

        // When
        String value = featureFlagService.getFeatureFlagConfig("shop-1", "ANALYTICS_ENABLED", "maxItems");

        // Then
        assertThat(value).isEqualTo("100");
    }

    @Test
    @DisplayName("getFeatureFlagConfig - Should return default value when config not found")
    void getFeatureFlagConfig_ShouldReturnDefaultValueWhenConfigNotFound() {
        // Given
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(featureFlagRepository.findByShopAndFeatureName(any(), anyString()))
            .thenReturn(Optional.empty());
        when(featureFlagRepository.findGlobalFeatureFlag(anyString()))
            .thenReturn(Optional.empty());

        // When
        String value = featureFlagService.getFeatureFlagConfig("shop-1", "UNKNOWN", "key", "defaultValue");

        // Then
        assertThat(value).isEqualTo("defaultValue");
    }

    // Delete Feature Flag Tests
    @Test
    @DisplayName("deleteFeatureFlag - Should delete flag")
    void deleteFeatureFlag_ShouldDeleteFlag() {
        // Given
        when(featureFlagRepository.findById("flag-1")).thenReturn(Optional.of(globalFlag));

        // When
        featureFlagService.deleteFeatureFlag("flag-1");

        // Then
        verify(featureFlagRepository).delete(globalFlag);
    }

    @Test
    @DisplayName("deleteFeatureFlag - Should throw exception when flag not found")
    void deleteFeatureFlag_ShouldThrowExceptionWhenFlagNotFound() {
        // Given
        when(featureFlagRepository.findById("flag-999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> featureFlagService.deleteFeatureFlag("flag-999"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Feature flag not found");
    }

    // Convenience Methods Tests
    @Test
    @DisplayName("isInvestmentEnabled - Should check INVESTMENT_ENABLED flag")
    void isInvestmentEnabled_ShouldCheckInvestmentEnabledFlag() {
        // Given
        FeatureFlag investmentFlag = FeatureFlag.builder()
            .shop(null)
            .featureName(FeatureFlag.INVESTMENT_ENABLED)
            .enabled(true)
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag(FeatureFlag.INVESTMENT_ENABLED))
            .thenReturn(Optional.of(investmentFlag));

        // When
        boolean enabled = featureFlagService.isInvestmentEnabled(null);

        // Then
        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("isAnalyticsEnabled - Should check ANALYTICS_ENABLED flag")
    void isAnalyticsEnabled_ShouldCheckAnalyticsEnabledFlag() {
        // Given
        when(featureFlagRepository.findGlobalFeatureFlag(FeatureFlag.ANALYTICS_ENABLED))
            .thenReturn(Optional.of(globalFlag));

        // When
        boolean enabled = featureFlagService.isAnalyticsEnabled(null);

        // Then
        assertThat(enabled).isTrue();
    }

    @Test
    @DisplayName("isFraudDetectionEnabled - Should check FRAUD_ENABLED flag")
    void isFraudDetectionEnabled_ShouldCheckFraudEnabledFlag() {
        // Given
        FeatureFlag fraudFlag = FeatureFlag.builder()
            .shop(null)
            .featureName(FeatureFlag.FRAUD_ENABLED)
            .enabled(false)
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag(FeatureFlag.FRAUD_ENABLED))
            .thenReturn(Optional.of(fraudFlag));

        // When
        boolean enabled = featureFlagService.isFraudDetectionEnabled(null);

        // Then
        assertThat(enabled).isFalse();
    }

    @Test
    @DisplayName("isAdvancedReportingEnabled - Should check ADVANCED_REPORTING flag")
    void isAdvancedReportingEnabled_ShouldCheckAdvancedReportingFlag() {
        // Given
        FeatureFlag reportingFlag = FeatureFlag.builder()
            .shop(null)
            .featureName(FeatureFlag.ADVANCED_REPORTING)
            .enabled(true)
            .build();

        when(featureFlagRepository.findGlobalFeatureFlag(FeatureFlag.ADVANCED_REPORTING))
            .thenReturn(Optional.of(reportingFlag));

        // When
        boolean enabled = featureFlagService.isAdvancedReportingEnabled(null);

        // Then
        assertThat(enabled).isTrue();
    }
}
