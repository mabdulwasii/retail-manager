package com.princely.shopmanager.core;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopConfiguration;
import com.princely.shopmanager.core.domain.Tenant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit tests for Shop domain objects without Spring context
 */
class ShopDomainTest {

    private Tenant createTestTenant(String tenantId) {
        return Tenant.builder()
            .id(tenantId)
            .name("Test Tenant " + tenantId)
            .contactEmail("test@tenant.com")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
    }

    @Test
    void testCreateShopWithConfiguration() {
        // Given
        ShopConfiguration config = new ShopConfiguration();
        config.setCurrency("USD");
        config.setInvestmentEnabled(true);
        config.setAnalyticsEnabled(true);
        config.setTaxRate(10.0);

        Shop shop = Shop.builder()
            .name("Test Shop")
            .tenant(createTestTenant("tenant-001"))
            .description("A test shop")
            .address("123 Test Street")
            .city("Test City")
            .country("Test Country")
            .email("test@shop.com")
            .phoneNumber("+1234567890")
            .status(Shop.ShopStatus.ACTIVE)
            .configuration(config)
            .build();

        // Then
        assertThat(shop.getName()).isEqualTo("Test Shop");
        assertThat(shop.getTenant().getId()).isEqualTo("tenant-001");
        assertThat(shop.getConfiguration()).isNotNull();
        assertThat(shop.getConfiguration().getCurrency()).isEqualTo("USD");
        assertThat(shop.getConfiguration().getTaxRate()).isEqualTo(10.0);
        assertThat(shop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
        assertThat(shop.getConfiguration().isInvestmentEnabled()).isTrue();
        assertThat(shop.getConfiguration().isAnalyticsEnabled()).isTrue();
    }

    @Test
    void testShopWithDefaultValues() {
        // Given
        Shop shop = Shop.builder()
            .name("Simple Shop")
            .tenant(createTestTenant("tenant-002"))
            .address("456 Simple Street")
            .email("simple@shop.com")
            .status(Shop.ShopStatus.ACTIVE)
            .build();

        // Then
        assertThat(shop.getName()).isEqualTo("Simple Shop");
        assertThat(shop.getTenant().getId()).isEqualTo("tenant-002");
        assertThat(shop.getEmail()).isEqualTo("simple@shop.com");
        assertThat(shop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
    }

    @Test
    void testShopStatusEnum() {
        // Test all status values
        assertThat(Shop.ShopStatus.ACTIVE).isNotNull();
        assertThat(Shop.ShopStatus.INACTIVE).isNotNull();
        assertThat(Shop.ShopStatus.SUSPENDED).isNotNull();
        assertThat(Shop.ShopStatus.CLOSED).isNotNull();

        // Test status setting
        Shop shop = Shop.builder()
            .name("Status Test Shop")
            .tenant(createTestTenant("tenant-status"))
            .address("Status Street")
            .email("status@shop.com")
            .status(Shop.ShopStatus.SUSPENDED)
            .build();

        assertThat(shop.getStatus()).isEqualTo(Shop.ShopStatus.SUSPENDED);
    }

    @Test
    void testShopConfiguration() {
        // Given
        ShopConfiguration config = new ShopConfiguration();

        // Test default values
        assertThat(config.isInvestmentEnabled()).isTrue();
        assertThat(config.isAnalyticsEnabled()).isTrue();
        assertThat(config.isFraudDetectionEnabled()).isFalse();
        assertThat(config.isAutoBackupEnabled()).isTrue();
        assertThat(config.getCurrency()).isEqualTo("USD");
        assertThat(config.getTaxRate()).isEqualTo(0.0);
        assertThat(config.getMaxDiscountPercentage()).isEqualTo(50.0);

        // Test setting values
        config.setTaxRate(15.5);
        config.setMaxDiscountPercentage(25.0);
        config.setCurrency("EUR");
        config.setFraudDetectionEnabled(true);

        assertThat(config.getTaxRate()).isEqualTo(15.5);
        assertThat(config.getMaxDiscountPercentage()).isEqualTo(25.0);
        assertThat(config.getCurrency()).isEqualTo("EUR");
        assertThat(config.isFraudDetectionEnabled()).isTrue();
    }
}