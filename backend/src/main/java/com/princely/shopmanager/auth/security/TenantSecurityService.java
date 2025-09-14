package com.princely.shopmanager.auth.security;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantSecurityService {

    private final ShopRepository shopRepository;
    private final AuditService auditService;

    /**
     * Check if the current user has access to the specified shop/tenant
     */
    public boolean hasAccessToShop(String shopId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            log.warn("No tenant context available for access check");
            return false;
        }

        // System administrators have access to all shops
        if (hasSystemRole(auth)) {
            return true;
        }

        // Check if the requested shop matches the current tenant context
        return currentTenant.equals(shopId);
    }

    /**
     * Check if the current user has the specified role within their tenant context
     */
    public boolean hasRoleInCurrentTenant(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return false;
        }

        // Extract roles from JWT token
        if (auth.getPrincipal() instanceof Jwt jwt) {
            List<String> roles = jwt.getClaimAsStringList("roles");
            String tenantSpecificRole = role + "_" + currentTenant;
            return roles != null && (roles.contains(role) || roles.contains(tenantSpecificRole));
        }

        return auth.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role) ||
                authority.getAuthority().equals("ROLE_" + role + "_" + currentTenant));
    }

    /**
     * Enforce tenant isolation by checking if the user can access the specified resource
     */
    public void enforceShopAccess(String shopId) {
        if (!hasAccessToShop(shopId)) {
            String userId = TenantContext.getCurrentUserId();
            String userName = TenantContext.getCurrentUserName();

            auditService.logSecurityEvent(
                null,
                userId,
                userName,
                com.princely.shopmanager.shared.domain.AuditLog.ActionType.PERMISSION_DENIED,
                "Attempted unauthorized access to shop: " + shopId,
                null,
                false
            );

            throw new AccessDeniedException("Access denied to shop: " + shopId);
        }
    }

    /**
     * Get all shops the current user has access to
     */
    public List<Shop> getAccessibleShops() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return List.of();
        }

        // System administrators can access all shops
        if (hasSystemRole(auth)) {
            return shopRepository.findAll();
        }

        // Regular users can only access their assigned shops
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant != null) {
            Optional<Shop> shop = shopRepository.findById(currentTenant);
            return shop.map(List::of).orElse(List.of());
        }

        return List.of();
    }

    /**
     * Check if the user has system-level roles (admin, super-admin)
     */
    private boolean hasSystemRole(Authentication auth) {
        return auth.getAuthorities().stream()
            .anyMatch(authority ->
                authority.getAuthority().equals("ROLE_SYSTEM_ADMIN") ||
                authority.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /**
     * Validate that a shop exists and the user has access to it
     */
    public Shop validateAndGetShop(String shopId) {
        enforceShopAccess(shopId);

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + shopId));

        return shop;
    }

    /**
     * Check if the current user can perform the specified action on the resource
     */
    public boolean canPerformAction(String shopId, String action, String resourceType) {
        if (!hasAccessToShop(shopId)) {
            return false;
        }

        // Define permission matrix based on roles and actions
        return switch (action.toUpperCase()) {
            case "READ" -> hasRoleInCurrentTenant("CASHIER") ||
                          hasRoleInCurrentTenant("MANAGER") ||
                          hasRoleInCurrentTenant("OWNER");

            case "WRITE", "UPDATE" -> hasRoleInCurrentTenant("MANAGER") ||
                                     hasRoleInCurrentTenant("OWNER");

            case "DELETE" -> hasRoleInCurrentTenant("OWNER");

            case "APPROVE" -> resourceType.equals("INVESTMENT") ?
                            hasRoleInCurrentTenant("OWNER") :
                            hasRoleInCurrentTenant("MANAGER");

            case "ADMIN" -> hasRoleInCurrentTenant("OWNER") || hasSystemRole(SecurityContextHolder.getContext().getAuthentication());

            default -> false;
        };
    }

    /**
     * Log security events for audit purposes
     */
    public void logSecurityEvent(String action, String resource, boolean success) {
        String userId = TenantContext.getCurrentUserId();
        String userName = TenantContext.getCurrentUserName();
        String tenantId = TenantContext.getCurrentTenant();

        Shop shop = null;
        if (tenantId != null) {
            shop = shopRepository.findById(tenantId).orElse(null);
        }

        auditService.logSecurityEvent(
            shop,
            userId,
            userName,
            com.princely.shopmanager.shared.domain.AuditLog.ActionType.valueOf(action.toUpperCase()),
            String.format("Security action on %s", resource),
            null,
            success
        );
    }
}