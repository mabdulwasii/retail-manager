package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.test.config.AbstractIntegrationTest;
import com.princely.shopmanager.auth.constants.SecurityRoles;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.ShopAwareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for shop-level access control.
 *
 * Tests the complete shop access control flow including:
 * - ShopAccessValidator for validating shop access
 * - FilterScope for role-based filtering
 * - Multi-tenant isolation
 * - Role-based access (SYSTEM_ADMIN, TENANT_ADMIN, OWNER, INVESTOR, MANAGER, EMPLOYEE)
 *
 * Access Control Rules:
 * - SYSTEM_ADMIN: Access to all shops across all tenants (system-wide)
 * - TENANT_ADMIN/OWNER/INVESTOR: Access to all shops within their tenant (tenant-wide)
 * - Other roles (MANAGER, EMPLOYEE, etc.): Access only to their assigned shop (shop-specific)
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Shop Access Control Integration Tests")
class ShopAccessControlIT extends AbstractIntegrationTest {

    @Autowired
    private ShopAccessValidator shopAccessValidator;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    private Tenant tenant1;
    private Tenant tenant2;
    private Shop shop1Tenant1;
    private Shop shop2Tenant1;
    private Shop shop1Tenant2;

    private JwtPrincipal systemAdminPrincipal;
    private JwtPrincipal tenantAdminPrincipal;
    private JwtPrincipal ownerPrincipal;
    private JwtPrincipal investorPrincipal;
    private JwtPrincipal managerPrincipal;
    private JwtPrincipal employeePrincipal;

    @BeforeEach
    void setUp() {
        // Create two tenants for testing
        tenant1 = createTenant("tenant1", "Tenant One");
        tenant2 = createTenant("tenant2", "Tenant Two");

        // Create shops for tenant1
        shop1Tenant1 = createShop("shop1-t1", "Shop 1 Tenant 1", tenant1);
        shop2Tenant1 = createShop("shop2-t1", "Shop 2 Tenant 1", tenant1);

        // Create shop for tenant2
        shop1Tenant2 = createShop("shop1-t2", "Shop 1 Tenant 2", tenant2);

        // Set tenant context to tenant1 for most tests
        setTenantContext(tenant1.getId());

        // Create test principals for different roles
        systemAdminPrincipal = createTestPrincipal("system-admin@test.com", SecurityRoles.SYSTEM_ADMIN, null, null);
        tenantAdminPrincipal = createTestPrincipal("tenant-admin@test.com", SecurityRoles.TENANT_ADMIN, tenant1.getId(), null);
        ownerPrincipal = createTestPrincipal("owner@test.com", SecurityRoles.OWNER, tenant1.getId(), null);
        investorPrincipal = createTestPrincipal("investor@test.com", SecurityRoles.INVESTOR, tenant1.getId(), null);
        managerPrincipal = createTestPrincipal("manager@test.com", SecurityRoles.MANAGER, tenant1.getId(), shop1Tenant1.getId());
        employeePrincipal = createTestPrincipal("employee@test.com", SecurityRoles.EMPLOYEE, tenant1.getId(), shop1Tenant1.getId());
    }

    // ==================== ShopAccessValidator Tests ====================

    @Test
    @DisplayName("SYSTEM_ADMIN should have access to all shops across all tenants")
    void systemAdminShouldHaveAccessToAllShops() {
        // When/Then - System admin can access shop in tenant1
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), systemAdminPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasNoAccessToShop(shop1Tenant1.getId(), systemAdminPrincipal)).isFalse();

        // When/Then - System admin can access shop in tenant2
        assertThat(shopAccessValidator.hasAccess(shop1Tenant2.getId(), systemAdminPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasNoAccessToShop(shop1Tenant2.getId(), systemAdminPrincipal)).isFalse();
    }

    @Test
    @DisplayName("TENANT_ADMIN should have access to all shops within their tenant")
    void tenantAdminShouldHaveAccessToAllShopsInTenant() {
        // When/Then - Tenant admin can access all shops in tenant1
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), tenantAdminPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasAccess(shop2Tenant1.getId(), tenantAdminPrincipal)).isTrue();

        // When/Then - Tenant admin cannot access shop in different tenant
        assertThat(shopAccessValidator.hasAccess(shop1Tenant2.getId(), tenantAdminPrincipal)).isFalse();
    }

    @Test
    @DisplayName("OWNER should have access to all shops within their tenant")
    void ownerShouldHaveAccessToAllShopsInTenant() {
        // When/Then - Owner can access all shops in tenant1
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), ownerPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasAccess(shop2Tenant1.getId(), ownerPrincipal)).isTrue();

        // When/Then - Owner cannot access shop in different tenant
        assertThat(shopAccessValidator.hasAccess(shop1Tenant2.getId(), ownerPrincipal)).isFalse();
    }

    @Test
    @DisplayName("INVESTOR should have access to all shops within their tenant")
    void investorShouldHaveAccessToAllShopsInTenant() {
        // When/Then - Investor can access all shops in tenant1
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), investorPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasAccess(shop2Tenant1.getId(), investorPrincipal)).isTrue();

        // When/Then - Investor cannot access shop in different tenant
        assertThat(shopAccessValidator.hasAccess(shop1Tenant2.getId(), investorPrincipal)).isFalse();
    }

    @Test
    @DisplayName("MANAGER should only have access to their assigned shop")
    void managerShouldOnlyHaveAccessToAssignedShop() {
        // When/Then - Manager can access their assigned shop
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), managerPrincipal)).isTrue();

        // When/Then - Manager cannot access other shops in same tenant
        assertThat(shopAccessValidator.hasAccess(shop2Tenant1.getId(), managerPrincipal)).isFalse();
        assertThat(shopAccessValidator.hasNoAccessToShop(shop2Tenant1.getId(), managerPrincipal)).isTrue();
    }

    @Test
    @DisplayName("EMPLOYEE should only have access to their assigned shop")
    void employeeShouldOnlyHaveAccessToAssignedShop() {
        // When/Then - Employee can access their assigned shop
        assertThat(shopAccessValidator.hasAccess(shop1Tenant1.getId(), employeePrincipal)).isTrue();

        // When/Then - Employee cannot access other shops
        assertThat(shopAccessValidator.hasAccess(shop2Tenant1.getId(), employeePrincipal)).isFalse();
        assertThat(shopAccessValidator.hasNoAccessToShop(shop2Tenant1.getId(), employeePrincipal)).isTrue();
    }

    @Test
    @DisplayName("hasTenantWideAccess should return true for TENANT_ADMIN, OWNER, SYSTEM_ADMIN")
    void hasTenantWideAccessShouldWorkCorrectly() {
        // When/Then - Tenant-wide roles
        assertThat(shopAccessValidator.hasTenantWideAccess(systemAdminPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasTenantWideAccess(tenantAdminPrincipal)).isTrue();
        assertThat(shopAccessValidator.hasTenantWideAccess(ownerPrincipal)).isTrue();

        // When/Then - Shop-specific roles
        assertThat(shopAccessValidator.hasTenantWideAccess(managerPrincipal)).isFalse();
        assertThat(shopAccessValidator.hasTenantWideAccess(employeePrincipal)).isFalse();
    }

    // ==================== FilterScope Tests ====================

    @Test
    @DisplayName("FilterScope for SYSTEM_ADMIN should be SYSTEM_WIDE")
    void filterScopeForSystemAdminShouldBeSystemWide() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(systemAdminPrincipal);

        // Then
        assertThat(filterScope.isSystemWide()).isTrue();
        assertThat(filterScope.isTenantWide()).isFalse();
        assertThat(filterScope.isShopSpecific()).isFalse();
        assertThat(filterScope.getLevel()).isEqualTo(ShopAwareService.FilterScope.FilterLevel.SYSTEM_WIDE);
    }

    @Test
    @DisplayName("FilterScope for TENANT_ADMIN should be TENANT_WIDE")
    void filterScopeForTenantAdminShouldBeTenantWide() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(tenantAdminPrincipal);

        // Then
        assertThat(filterScope.isTenantWide()).isTrue();
        assertThat(filterScope.isSystemWide()).isFalse();
        assertThat(filterScope.isShopSpecific()).isFalse();
        assertThat(filterScope.getTenantId()).isEqualTo(tenant1.getId());
        assertThat(filterScope.getLevel()).isEqualTo(ShopAwareService.FilterScope.FilterLevel.TENANT_WIDE);
    }

    @Test
    @DisplayName("FilterScope for OWNER should be TENANT_WIDE")
    void filterScopeForOwnerShouldBeTenantWide() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(ownerPrincipal);

        // Then
        assertThat(filterScope.isTenantWide()).isTrue();
        assertThat(filterScope.getTenantId()).isEqualTo(tenant1.getId());
    }

    @Test
    @DisplayName("FilterScope for INVESTOR should be TENANT_WIDE")
    void filterScopeForInvestorShouldBeTenantWide() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(investorPrincipal);

        // Then
        assertThat(filterScope.isTenantWide()).isTrue();
        assertThat(filterScope.getTenantId()).isEqualTo(tenant1.getId());
    }

    @Test
    @DisplayName("FilterScope for MANAGER should be SHOP_SPECIFIC")
    void filterScopeForManagerShouldBeShopSpecific() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(managerPrincipal);

        // Then
        assertThat(filterScope.isShopSpecific()).isTrue();
        assertThat(filterScope.isTenantWide()).isFalse();
        assertThat(filterScope.isSystemWide()).isFalse();
        assertThat(filterScope.getShopId()).isEqualTo(shop1Tenant1.getId());
        assertThat(filterScope.getLevel()).isEqualTo(ShopAwareService.FilterScope.FilterLevel.SHOP_SPECIFIC);
    }

    @Test
    @DisplayName("FilterScope for EMPLOYEE should be SHOP_SPECIFIC")
    void filterScopeForEmployeeShouldBeShopSpecific() {
        // Given
        TestShopAwareService service = new TestShopAwareService(shopAccessValidator, shopRepository);

        // When
        ShopAwareService.FilterScope filterScope = service.testGetFilterScope(employeePrincipal);

        // Then
        assertThat(filterScope.isShopSpecific()).isTrue();
        assertThat(filterScope.getShopId()).isEqualTo(shop1Tenant1.getId());
    }

    // ==================== Helper Methods ====================

    private Tenant createTenant(String id, String name) {
        Tenant tenant = Tenant.builder()
            .id(id)
            .name(name)
            .build();
        return tenantRepository.save(tenant);
    }

    private Shop createShop(String id, String name, Tenant tenant) {
        Shop shop = Shop.builder()
            .id(id)
            .name(name)
            .tenant(tenant)
            .address("Test Address")
            .phoneNumber("1234567890")
            .email("shop@test.com")
            .build();
        return shopRepository.save(shop);
    }

    private JwtPrincipal createTestPrincipal(String username, String role, String tenantId, String shopId) {
        return JwtPrincipal.builder()
            .subject("test-" + username)
            .preferredUsername(username)
            .roles(List.of(role))
            .tenantId(tenantId)
            .shopId(shopId)
            .build();
    }

    /**
     * Test implementation of ShopAwareService to expose protected methods for testing.
     */
    private static class TestShopAwareService extends ShopAwareService {
        protected TestShopAwareService(ShopAccessValidator shopAccessValidator,
                                       com.princely.shopmanager.core.repository.ShopRepository shopRepository) {
            super(shopAccessValidator, shopRepository);
        }

        public FilterScope testGetFilterScope(JwtPrincipal principal) {
            return getFilterScope(principal);
        }
    }
}
