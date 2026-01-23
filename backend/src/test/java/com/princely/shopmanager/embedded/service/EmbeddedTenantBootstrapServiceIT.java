package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for EmbeddedTenantBootstrapService.
 * Tests tenant and shop bootstrapping in embedded mode.
 */
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@SpringBootTest
@ActiveProfiles("embedded")
@TestPropertySource(properties = {
    "app.keycloak.enabled=false",
    "app.update-check.enabled=true",
    "app.bootstrap.tenant.enabled=true",
    "app.bootstrap.tenant.name=Test Tenant Bootstrap",
    "app.bootstrap.tenant.email=test-bootstrap@test.com",
    "app.bootstrap.tenant.address=123 Test Street",
    "app.bootstrap.tenant.city=Test City",
    "app.bootstrap.tenant.country=Test Country",
    "app.bootstrap.shop.name=Test Shop Bootstrap",
    "app.bootstrap.shop.address=456 Shop Street",
    "app.bootstrap.shop.city=Shop City",
    "app.bootstrap.shop.country=Shop Country",
    "app.bootstrap.shop.phone=1-800-TEST-SHOP",
    "app.bootstrap.shop.email=test-shop@test.com",
    "embedded.postgres.data-dir=./target/test-postgres-bootstrap",
    "embedded.postgres.port=5437"
})
@DisplayName("Embedded Tenant Bootstrap Service - Integration Tests")
class EmbeddedTenantBootstrapServiceIT {

    @Autowired
    private EmbeddedTenantBootstrapService bootstrapService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @MockBean
    private AuditService auditService;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        shopRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        // Clean up test data
        shopRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    // ============================================================================
    // Bootstrap Tests
    // ============================================================================

    @Test
    @DisplayName("Should create default tenant and shop when none exist")
    @Transactional
    void shouldCreateDefaultTenantAndShop() {
        // Given
        assertThat(tenantRepository.count()).isZero();
        assertThat(shopRepository.count()).isZero();

        // When
        bootstrapService.bootstrapTenantAndShop();

        // Then
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(shopRepository.count()).isEqualTo(1);

        Tenant tenant = tenantRepository.findAll().get(0);
        assertThat(tenant.getName()).isEqualTo("Test Tenant Bootstrap");
        assertThat(tenant.getContactEmail()).isEqualTo("test-bootstrap@test.com");
        assertThat(tenant.getPrimaryAddress()).isEqualTo("123 Test Street");
        assertThat(tenant.getCity()).isEqualTo("Test City");
        assertThat(tenant.getCountry()).isEqualTo("Test Country");
        assertThat(tenant.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        assertThat(tenant.getDescription()).contains("embedded mode");

        Shop shop = shopRepository.findAll().get(0);
        assertThat(shop.getName()).isEqualTo("Test Shop Bootstrap");
        assertThat(shop.getAddress()).isEqualTo("456 Shop Street");
        assertThat(shop.getCity()).isEqualTo("Shop City");
        assertThat(shop.getCountry()).isEqualTo("Shop Country");
        assertThat(shop.getPhoneNumber()).isEqualTo("1-800-TEST-SHOP");
        assertThat(shop.getEmail()).isEqualTo("test-shop@test.com");
        assertThat(shop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
        assertThat(shop.getTenant()).isEqualTo(tenant);

        // Verify audit log was created
        verify(auditService).logEvent(
            eq("EMBEDDED_TENANT_BOOTSTRAP"),
            anyString(),
            argThat(map -> map.containsKey("tenantId") && map.containsKey("shopId"))
        );
    }

    @Test
    @DisplayName("Should skip bootstrap when tenant already exists")
    @Transactional
    void shouldSkipBootstrapWhenTenantExists() {
        // Given - Create existing tenant
        Tenant existingTenant = Tenant.builder()
                .name("Existing Tenant")
                .contactEmail("existing@test.com")
                .primaryAddress("Existing Address")
                .city("Existing City")
                .country("Existing Country")
                .status(Tenant.TenantStatus.ACTIVE)
                .build();
        tenantRepository.save(existingTenant);

        long initialCount = tenantRepository.count();

        // When
        bootstrapService.bootstrapTenantAndShop();

        // Then - No new tenant created
        assertThat(tenantRepository.count()).isEqualTo(initialCount);
        verify(auditService, never()).logEvent(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should handle bootstrap errors gracefully without throwing")
    @Transactional
    void shouldHandleBootstrapErrorsGracefully() {
        // Given - Clear any previous invocations and stub audit service to throw exception
        org.mockito.Mockito.clearInvocations(auditService);
        doThrow(new RuntimeException("Audit service error"))
                .when(auditService).logEvent(anyString(), anyString(), anyMap());

        // When - Should not throw exception
        assertThatCode(() -> bootstrapService.bootstrapTenantAndShop())
                .doesNotThrowAnyException();

        // Then - Tenant and shop should still be created (audit failure is logged, not fatal)
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(shopRepository.count()).isEqualTo(1);
    }

    // ============================================================================
    // Get or Create Default Tenant Tests
    // ============================================================================

    @Test
    @DisplayName("Should return existing tenant when calling getOrCreateDefaultTenant")
    @Transactional
    void shouldReturnExistingTenant() {
        // Given - Bootstrap first
        bootstrapService.bootstrapTenantAndShop();
        Tenant existingTenant = tenantRepository.findByName("Test Tenant Bootstrap").orElseThrow();

        // When
        Tenant result = bootstrapService.getOrCreateDefaultTenant();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingTenant.getId());
        assertThat(result.getName()).isEqualTo("Test Tenant Bootstrap");
        assertThat(tenantRepository.count()).isEqualTo(1); // No duplicate created
    }

    @Test
    @DisplayName("Should create tenant when calling getOrCreateDefaultTenant and none exists")
    @Transactional
    void shouldCreateTenantWhenNoneExists() {
        // Given
        assertThat(tenantRepository.count()).isZero();

        // When
        Tenant result = bootstrapService.getOrCreateDefaultTenant();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Tenant Bootstrap");
        assertThat(tenantRepository.count()).isEqualTo(1);
        assertThat(shopRepository.count()).isEqualTo(1); // Shop also created
    }

    // ============================================================================
    // Get or Create Default Shop Tests
    // ============================================================================

    @Test
    @DisplayName("Should return existing shop when calling getOrCreateDefaultShop")
    @Transactional
    void shouldReturnExistingShop() {
        // Given - Bootstrap first
        bootstrapService.bootstrapTenantAndShop();
        Shop existingShop = shopRepository.findByName("Test Shop Bootstrap").orElseThrow();

        // When
        Shop result = bootstrapService.getOrCreateDefaultShop();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingShop.getId());
        assertThat(result.getName()).isEqualTo("Test Shop Bootstrap");
        assertThat(shopRepository.count()).isEqualTo(1); // No duplicate created
    }

    @Test
    @DisplayName("Should create shop when calling getOrCreateDefaultShop and none exists")
    @Transactional
    void shouldCreateShopWhenNoneExists() {
        // Given
        assertThat(shopRepository.count()).isZero();

        // When
        Shop result = bootstrapService.getOrCreateDefaultShop();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Shop Bootstrap");
        assertThat(shopRepository.count()).isEqualTo(1);
        assertThat(tenantRepository.count()).isEqualTo(1); // Tenant also created
    }

    // ============================================================================
    // Bootstrap Required Tests
    // ============================================================================

    @Test
    @DisplayName("Should return true when bootstrap is required")
    @Transactional
    void shouldReturnTrueWhenBootstrapRequired() {
        // Given
        assertThat(tenantRepository.count()).isZero();

        // When
        boolean result = bootstrapService.isBootstrapRequired();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when bootstrap is not required")
    @Transactional
    void shouldReturnFalseWhenBootstrapNotRequired() {
        // Given - Create tenant
        bootstrapService.bootstrapTenantAndShop();

        // When
        boolean result = bootstrapService.isBootstrapRequired();

        // Then
        assertThat(result).isFalse();
    }

    // ============================================================================
    // Configuration Tests
    // ============================================================================

    @Test
    @DisplayName("Should respect tenant configuration properties")
    @Transactional
    void shouldRespectTenantConfigurationProperties() {
        // When
        bootstrapService.bootstrapTenantAndShop();

        // Then
        Tenant tenant = tenantRepository.findByName("Test Tenant Bootstrap").orElseThrow();
        assertThat(tenant.getName()).isEqualTo("Test Tenant Bootstrap");
        assertThat(tenant.getContactEmail()).isEqualTo("test-bootstrap@test.com");
        assertThat(tenant.getPrimaryAddress()).isEqualTo("123 Test Street");
        assertThat(tenant.getCity()).isEqualTo("Test City");
        assertThat(tenant.getCountry()).isEqualTo("Test Country");
    }

    @Test
    @DisplayName("Should respect shop configuration properties")
    @Transactional
    void shouldRespectShopConfigurationProperties() {
        // When
        bootstrapService.bootstrapTenantAndShop();

        // Then
        Shop shop = shopRepository.findByName("Test Shop Bootstrap").orElseThrow();
        assertThat(shop.getName()).isEqualTo("Test Shop Bootstrap");
        assertThat(shop.getAddress()).isEqualTo("456 Shop Street");
        assertThat(shop.getCity()).isEqualTo("Shop City");
        assertThat(shop.getCountry()).isEqualTo("Shop Country");
        assertThat(shop.getPhoneNumber()).isEqualTo("1-800-TEST-SHOP");
        assertThat(shop.getEmail()).isEqualTo("test-shop@test.com");
    }
}
