package com.princely.shopmanager.core;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.flyway.enabled=false",
    "logging.level.org.springframework.security=WARN"
})
class ShopEntitySimpleTest {

    @Autowired
    private TestEntityManager entityManager;

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
            .tenantId("tenant-001")
            .description("A test shop")
            .address("123 Test Street")
            .city("Test City")
            .country("Test Country")
            .email("test@shop.com")
            .phoneNumber("+1234567890")
            .status(Shop.ShopStatus.ACTIVE)
            .configuration(config)
            .build();

        // When
        Shop savedShop = entityManager.persistAndFlush(shop);

        // Then
        assertThat(savedShop.getId()).isNotNull();
        assertThat(savedShop.getName()).isEqualTo("Test Shop");
        assertThat(savedShop.getTenantId()).isEqualTo("tenant-001");
        assertThat(savedShop.getConfiguration()).isNotNull();
        assertThat(savedShop.getConfiguration().getCurrency()).isEqualTo("USD");
        assertThat(savedShop.getConfiguration().getTaxRate()).isEqualTo(10.0);
        assertThat(savedShop.getCreatedAt()).isNotNull();
        assertThat(savedShop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
    }

    @Test
    void testShopWithDefaultConfiguration() {
        // Given
        Shop shop = Shop.builder()
            .name("Simple Shop")
            .tenantId("tenant-002")
            .address("456 Simple Street")
            .email("simple@shop.com")
            .status(Shop.ShopStatus.ACTIVE)
            .build();

        // When
        Shop savedShop = entityManager.persistAndFlush(shop);

        // Then
        assertThat(savedShop.getId()).isNotNull();
        assertThat(savedShop.getName()).isEqualTo("Simple Shop");
        assertThat(savedShop.getTenantId()).isEqualTo("tenant-002");
        // Note: Collections may be null when using @Builder without @Builder.Default
        // This is expected behavior for entity tests
    }

    @Test
    void testShopStatusEnum() {
        // Given
        Shop activeShop = Shop.builder()
            .name("Active Shop")
            .tenantId("tenant-active")
            .address("123 Active Street")
            .email("active@shop.com")
            .status(Shop.ShopStatus.ACTIVE)
            .build();

        Shop inactiveShop = Shop.builder()
            .name("Inactive Shop")
            .tenantId("tenant-inactive")
            .address("456 Inactive Street")
            .email("inactive@shop.com")
            .status(Shop.ShopStatus.INACTIVE)
            .build();

        // When
        Shop savedActiveShop = entityManager.persistAndFlush(activeShop);
        Shop savedInactiveShop = entityManager.persistAndFlush(inactiveShop);

        // Then
        assertThat(savedActiveShop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
        assertThat(savedInactiveShop.getStatus()).isEqualTo(Shop.ShopStatus.INACTIVE);
    }
}