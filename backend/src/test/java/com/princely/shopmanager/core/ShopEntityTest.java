package com.princely.shopmanager.core;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.ShopConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Disabled("Testcontainers tests disabled for CI/CD - use ShopEntitySimpleTest instead")
class ShopEntityTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15-alpine"))
        .withDatabaseName("shopdb_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

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
        assertThat(savedShop.getProducts()).isNotNull();
        assertThat(savedShop.getProducts()).isEmpty();
        assertThat(savedShop.getUsers()).isNotNull();
        assertThat(savedShop.getUsers()).isEmpty();
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