package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.auth.constants.SecurityRoles;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShopAccessValidator Unit Tests")
class ShopAccessValidatorTest {

    private ShopAccessValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ShopAccessValidator();
        TenantContext.clear(); // Clear any existing tenant context
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should grant access to SYSTEM_ADMIN for any shop")
    void shouldGrantAccessToSystemAdminForAnyShop() {
        // Given
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-1")
            .preferredUsername("sysadmin@test.com")
            .tenantId("tenant-1")
            .roles(List.of(SecurityRoles.SYSTEM_ADMIN))
            .build();
        String shopId = "shop-1";

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isTrue();
        assertThat(hasNoAccess).isFalse();
    }

    @Test
    @DisplayName("Should grant access to TENANT_ADMIN for any shop in their tenant")
    void shouldGrantAccessToTenantAdminForAnyShopInTheirTenant() {
        // Given
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-2")
            .preferredUsername("admin@test.com")
            .tenantId("tenant-1")
            .roles(List.of(SecurityRoles.TENANT_ADMIN))
            .build();
        String shopId = "shop-1";

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isTrue();
        assertThat(hasNoAccess).isFalse();
    }

    @Test
    @DisplayName("Should grant access to OWNER for any shop in their tenant")
    void shouldGrantAccessToOwnerForAnyShopInTheirTenant() {
        // Given
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-3")
            .preferredUsername("owner@test.com")
            .tenantId("tenant-1")
            .roles(List.of(SecurityRoles.OWNER))
            .build();
        String shopId = "shop-1";

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isTrue();
        assertThat(hasNoAccess).isFalse();
    }

    @Test
    @DisplayName("Should grant access to MANAGER for their assigned shop")
    void shouldGrantAccessToManagerForTheirAssignedShop() {
        // Given
        String shopId = "shop-1";
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-4")
            .preferredUsername("manager@test.com")
            .tenantId("tenant-1")
            .shopId(shopId)
            .roles(List.of(SecurityRoles.MANAGER))
            .build();

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isTrue();
        assertThat(hasNoAccess).isFalse();
    }

    @Test
    @DisplayName("Should deny access to MANAGER for different shop")
    void shouldDenyAccessToManagerForDifferentShop() {
        // Given
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-5")
            .preferredUsername("manager@test.com")
            .tenantId("tenant-1")
            .shopId("shop-1")
            .roles(List.of(SecurityRoles.MANAGER))
            .build();
        String requestedShopId = "shop-2";

        // When
        boolean hasAccess = validator.hasAccess(requestedShopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(requestedShopId, principal);

        // Then
        assertThat(hasAccess).isFalse();
        assertThat(hasNoAccess).isTrue();
    }

    @Test
    @DisplayName("Should validate tenant context correctly when tenants match")
    void shouldValidateTenantContextWhenTenantsMatch() {
        // Given
        String tenantId = "tenant-1";
        TenantContext.setCurrentTenantId(tenantId);

        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-6")
            .preferredUsername("user@test.com")
            .tenantId(tenantId)
            .build();

        // When
        boolean isValid = validator.validateTenantContext(principal);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject validation when tenant contexts do not match")
    void shouldRejectValidationWhenTenantContextsDoNotMatch() {
        // Given
        TenantContext.setCurrentTenantId("tenant-1");

        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-7")
            .preferredUsername("user@test.com")
            .tenantId("tenant-2")
            .build();

        // When
        boolean isValid = validator.validateTenantContext(principal);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject validation when tenant context is null")
    void shouldRejectValidationWhenTenantContextIsNull() {
        // Given - no tenant context set
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-8")
            .preferredUsername("user@test.com")
            .tenantId("tenant-1")
            .build();

        // When
        boolean isValid = validator.validateTenantContext(principal);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject validation when principal has no tenant ID")
    void shouldRejectValidationWhenPrincipalHasNoTenantId() {
        // Given
        TenantContext.setCurrentTenantId("tenant-1");

        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-9")
            .preferredUsername("user@test.com")
            .tenantId(null)
            .build();

        // When
        boolean isValid = validator.validateTenantContext(principal);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify users with tenant-wide access")
    void shouldCorrectlyIdentifyUsersWithTenantWideAccess() {
        // Given - SYSTEM_ADMIN
        JwtPrincipal sysAdmin = JwtPrincipal.builder()
            .subject("user-10")
            .preferredUsername("sysadmin@test.com")
            .roles(List.of(SecurityRoles.SYSTEM_ADMIN))
            .build();

        // Given - TENANT_ADMIN
        JwtPrincipal tenantAdmin = JwtPrincipal.builder()
            .subject("user-11")
            .preferredUsername("admin@test.com")
            .roles(List.of(SecurityRoles.TENANT_ADMIN))
            .build();

        // Given - OWNER
        JwtPrincipal owner = JwtPrincipal.builder()
            .subject("user-12")
            .preferredUsername("owner@test.com")
            .roles(List.of(SecurityRoles.OWNER))
            .build();

        // Given - MANAGER (no tenant-wide access)
        JwtPrincipal manager = JwtPrincipal.builder()
            .subject("user-13")
            .preferredUsername("manager@test.com")
            .roles(List.of(SecurityRoles.MANAGER))
            .build();

        // When/Then
        assertThat(validator.hasTenantWideAccess(sysAdmin)).isTrue();
        assertThat(validator.hasTenantWideAccess(tenantAdmin)).isTrue();
        assertThat(validator.hasTenantWideAccess(owner)).isTrue();
        assertThat(validator.hasTenantWideAccess(manager)).isFalse();
    }

    @Test
    @DisplayName("Should correctly identify tenant admins (deprecated method)")
    void shouldCorrectlyIdentifyTenantAdminsDeprecatedMethod() {
        // Given - TENANT_ADMIN
        JwtPrincipal tenantAdmin = JwtPrincipal.builder()
            .subject("user-14")
            .preferredUsername("admin@test.com")
            .roles(List.of(SecurityRoles.TENANT_ADMIN))
            .build();

        // Given - MANAGER (not tenant admin)
        JwtPrincipal manager = JwtPrincipal.builder()
            .subject("user-15")
            .preferredUsername("manager@test.com")
            .roles(List.of(SecurityRoles.MANAGER))
            .build();

        // When/Then
        boolean tenantAdminResult = validator.hasTenantWideAccess(tenantAdmin);

        boolean managerResult = validator.hasTenantWideAccess(manager);

        assertThat(tenantAdminResult).isTrue();
        assertThat(managerResult).isFalse();
    }

    @Test
    @DisplayName("Should grant access when no shopId in principal but tenant context matches")
    void shouldGrantAccessWhenNoShopIdButTenantContextMatches() {
        // Given
        String tenantId = "tenant-1";
        TenantContext.setCurrentTenantId(tenantId);

        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-16")
            .preferredUsername("user@test.com")
            .tenantId(tenantId)
            .shopId(null)
            .roles(List.of(SecurityRoles.EMPLOYEE))
            .build();
        String shopId = "shop-1";

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isTrue();
        assertThat(hasNoAccess).isFalse();
    }

    @Test
    @DisplayName("Should deny access when no tenant context is available")
    void shouldDenyAccessWhenNoTenantContextAvailable() {
        // Given - no tenant context set
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject("user-17")
            .preferredUsername("user@test.com")
            .tenantId("tenant-1")
            .shopId(null)
            .roles(List.of(SecurityRoles.EMPLOYEE))
            .build();
        String shopId = "shop-1";

        // When
        boolean hasAccess = validator.hasAccess(shopId, principal);
        boolean hasNoAccess = validator.hasNoAccessToShop(shopId, principal);

        // Then
        assertThat(hasAccess).isFalse();
        assertThat(hasNoAccess).isTrue();
    }
}
