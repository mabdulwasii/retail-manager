package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.auth.constants.SecurityRoles;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility service for validating shop access permissions.
 *
 * This service provides centralized logic for determining whether a user
 * has access to a specific shop based on their JWT claims and role.
 *
 * Access Rules:
 * - SYSTEM_ADMIN: Access to all shops across all tenants
 * - TENANT_ADMIN: Access to all shops within their tenant
 * - Regular users: Access only to their assigned shop (User.shopId)
 *
 * NOTE: Multi-shop support limitation
 * Currently, users can only be assigned to ONE shop (User.shop is ManyToOne).
 * Future enhancement: Support ManyToMany relationship for multi-shop users.
 */
@Component
@Slf4j
public class ShopAccessValidator {

    /**
     * Validates if the user has access to the specified shop.
     *
     * @param shopId    The shop ID to validate access for
     * @param principal The JWT principal containing user information
     * @return true if user has access, false otherwise
     */
    public boolean hasAccess(String shopId, JwtPrincipal principal) {
        return !hasNoAccessToShop(shopId, principal);
    }

    /**
     * Validates if the user has NO access to the specified shop.
     *
     * @param shopId    The shop ID to validate access for
     * @param principal The JWT principal containing user information
     * @return true if user has NO access, false if user has access
     */
    public boolean hasNoAccessToShop(String shopId, JwtPrincipal principal) {
        // Validate that tenant context is set
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            log.warn("No tenant context found for shop access validation");
            return true;
        }

        // Validate that user's tenant matches the current tenant context
        if (principal.getTenantId() != null && !principal.getTenantId().equals(tenantId)) {
            log.warn("User tenant {} does not match context tenant {}", principal.getTenantId(), tenantId);
            return true;
        }

        // TENANT_ADMIN and SYSTEM_ADMIN have access to all shops within their tenant
        // Skip shop-specific validation for these roles
        if (principal.hasRole(SecurityRoles.TENANT_ADMIN) || principal.hasRole(SecurityRoles.SYSTEM_ADMIN)) {
            log.debug("User with TENANT_ADMIN or SYSTEM_ADMIN role has access to shop {}", shopId);
            return false;
        }

        // For regular users, validate that their assigned shop matches the requested shop
        // If user has shop_id in JWT, validate it matches the requested shop
        if (principal.getShopId() != null && !principal.getShopId().equals(shopId)) {
            log.warn("User shop {} does not match requested shop {}", principal.getShopId(), shopId);
            return true;
        }

        // NOTE: Multi-shop support limitation
        // Currently, users can only be assigned to ONE shop (User.shop is ManyToOne)
        // Future enhancement: Change to ManyToMany and update JwtPrincipal.shopId to List<String>
        return false;
    }

    /**
     * Validates that the user's tenant matches the tenant context.
     *
     * @param principal The JWT principal containing user information
     * @return true if tenant matches, false otherwise
     */
    public boolean validateTenantContext(JwtPrincipal principal) {
        String tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null) {
            log.warn("No tenant context found for validation");
            return false;
        }

        if (principal.getTenantId() == null) {
            log.warn("User has no tenant ID in JWT");
            return false;
        }

        boolean matches = principal.getTenantId().equals(tenantId);
        if (!matches) {
            log.warn("User tenant {} does not match context tenant {}", principal.getTenantId(), tenantId);
        }

        return matches;
    }

    /**
     * Checks if the user has tenant-wide administrative privileges.
     *
     * @param principal The JWT principal containing user information
     * @return true if user is TENANT_ADMIN or SYSTEM_ADMIN
     */
    public boolean isTenantAdmin(JwtPrincipal principal) {
        return principal.hasRole(SecurityRoles.TENANT_ADMIN) ||
               principal.hasRole(SecurityRoles.SYSTEM_ADMIN);
    }
}
