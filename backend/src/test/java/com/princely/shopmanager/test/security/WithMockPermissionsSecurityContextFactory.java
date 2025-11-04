package com.princely.shopmanager.test.security;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.test.TestConstants;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Security context factory for {@link WithMockPermissions} annotation.
 * Creates a mock security context with granular permissions for testing.
 */
public class WithMockPermissionsSecurityContextFactory implements WithSecurityContextFactory<WithMockPermissions> {

    @Override
    public SecurityContext createSecurityContext(WithMockPermissions annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add explicit permissions
        if (annotation.value().length > 0) {
            Arrays.stream(annotation.value())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        }

        // Add role-based permissions
        if (!annotation.role().isEmpty()) {
            getRolePermissions(annotation.role())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        }

        // Create JwtPrincipal to match controller expectations
        JwtPrincipal principal = JwtPrincipal.builder()
            .subject(TestConstants.MOCK_USER_ID)
            .preferredUsername(annotation.username())
            .email(annotation.username() + "@example.com")
            .firstName(annotation.username())
            .lastName("User")
            .tenantId(annotation.tenantId())
            .shopId(annotation.shopId())
            .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            "password",
            authorities
        );

        context.setAuthentication(auth);
        return context;
    }

    /**
     * Returns all permissions for a given role.
     * This should match your actual role-permission mappings from the database.
     */
    private Stream<String> getRolePermissions(String role) {
        return switch (role.toUpperCase()) {
            case "SYSTEM_ADMIN", "SUPER_ADMIN" -> Stream.of(
                // All permissions
                "SYSTEM_ADMIN",
                // Tenant
                "TENANT_CREATE", "TENANT_READ", "TENANT_LIST", "TENANT_UPDATE", "TENANT_DELETE",
                // Shop
                "SHOP_CREATE", "SHOP_READ", "SHOP_LIST", "SHOP_LIST_ALL", "SHOP_UPDATE", "SHOP_DELETE",
                // User
                "USER_CREATE", "USER_READ", "USER_LIST", "USER_UPDATE", "USER_DELETE",
                // Role
                "ROLE_CREATE", "ROLE_READ", "ROLE_LIST", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_ASSIGN",
                // Permission
                "PERMISSION_READ", "PERMISSION_LIST",
                // Product
                "PRODUCT_CREATE", "PRODUCT_READ", "PRODUCT_LIST", "PRODUCT_UPDATE", "PRODUCT_DELETE",
                // Category
                "CATEGORY_CREATE", "CATEGORY_READ", "CATEGORY_LIST", "CATEGORY_UPDATE", "CATEGORY_DELETE",
                // Inventory
                "INVENTORY_CREATE", "INVENTORY_READ", "INVENTORY_LIST", "INVENTORY_UPDATE", "INVENTORY_DELETE",
                "INVENTORY_ADJUST", "INVENTORY_RESERVE", "INVENTORY_HISTORY", "INVENTORY_HISTORY_VIEW", "INVENTORY_FORECAST",
                // Sales
                "SALES_CREATE", "SALES_READ", "SALES_LIST", "SALES_UPDATE", "SALES_DELETE", "SALES_VOID",
                // Receipt
                "RECEIPT_CREATE", "RECEIPT_READ", "RECEIPT_LIST", "RECEIPT_SEND", "RECEIPT_EMAIL",
                // Expense
                "EXPENSE_CREATE", "EXPENSE_READ", "EXPENSE_LIST", "EXPENSE_UPDATE", "EXPENSE_DELETE", "EXPENSE_APPROVE",
                "EXPENSE_CATEGORY_CREATE", "EXPENSE_CATEGORY_READ", "EXPENSE_CATEGORY_LIST", "EXPENSE_CATEGORY_UPDATE", "EXPENSE_CATEGORY_DELETE",
                // Investment
                "INVESTMENT_CREATE", "INVESTMENT_READ", "INVESTMENT_LIST", "INVESTMENT_UPDATE", "INVESTMENT_DELETE",
                "INVESTMENT_CLOSE", "INVESTMENT_PROFIT_DISTRIBUTE",
                // Return
                "RETURN_CREATE", "RETURN_READ", "RETURN_LIST", "RETURN_UPDATE", "RETURN_DELETE", "RETURN_APPROVE",
                // Audit
                "AUDIT_LOG_VIEW_SHOP", "AUDIT_LOG_VIEW_TENANT",
                // Analytics
                "ANALYTICS_SALES_VIEW", "ANALYTICS_INVESTMENT_VIEW", "ANALYTICS_MANAGE",
                // Fraud
                "FRAUD_VIEW", "FRAUD_MANAGE", "FRAUD_LIST", "FRAUD_INVESTIGATE", "FRAUD_RESOLVE", "FRAUD_DETECT"
            );

            case "OWNER" -> Stream.of(
                // Tenant
                "TENANT_READ",
                // Shop
                "SHOP_CREATE", "SHOP_READ", "SHOP_LIST", "SHOP_LIST_ALL", "SHOP_UPDATE", "SHOP_DELETE",
                // User
                "USER_CREATE", "USER_READ", "USER_LIST", "USER_UPDATE", "USER_DELETE",
                // Role
                "ROLE_CREATE", "ROLE_READ", "ROLE_LIST", "ROLE_UPDATE", "ROLE_DELETE", "ROLE_ASSIGN",
                // Permission
                "PERMISSION_READ", "PERMISSION_LIST",
                // Product
                "PRODUCT_CREATE", "PRODUCT_READ", "PRODUCT_LIST", "PRODUCT_UPDATE", "PRODUCT_DELETE",
                // Category
                "CATEGORY_CREATE", "CATEGORY_READ", "CATEGORY_LIST", "CATEGORY_UPDATE", "CATEGORY_DELETE",
                // Inventory
                "INVENTORY_CREATE", "INVENTORY_READ", "INVENTORY_LIST", "INVENTORY_UPDATE", "INVENTORY_DELETE",
                "INVENTORY_ADJUST", "INVENTORY_RESERVE", "INVENTORY_HISTORY", "INVENTORY_HISTORY_VIEW", "INVENTORY_FORECAST",
                // Sales
                "SALES_CREATE", "SALES_READ", "SALES_LIST", "SALES_UPDATE", "SALES_DELETE", "SALES_VOID",
                // Receipt
                "RECEIPT_CREATE", "RECEIPT_READ", "RECEIPT_LIST", "RECEIPT_SEND", "RECEIPT_EMAIL",
                // Expense
                "EXPENSE_CREATE", "EXPENSE_READ", "EXPENSE_LIST", "EXPENSE_UPDATE", "EXPENSE_DELETE", "EXPENSE_APPROVE",
                "EXPENSE_CATEGORY_CREATE", "EXPENSE_CATEGORY_READ", "EXPENSE_CATEGORY_LIST", "EXPENSE_CATEGORY_UPDATE", "EXPENSE_CATEGORY_DELETE",
                // Investment
                "INVESTMENT_CREATE", "INVESTMENT_READ", "INVESTMENT_LIST", "INVESTMENT_UPDATE", "INVESTMENT_DELETE",
                "INVESTMENT_CLOSE", "INVESTMENT_PROFIT_DISTRIBUTE",
                // Return
                "RETURN_CREATE", "RETURN_READ", "RETURN_LIST", "RETURN_UPDATE", "RETURN_DELETE", "RETURN_APPROVE",
                // Audit
                "AUDIT_LOG_VIEW_SHOP", "AUDIT_LOG_VIEW_TENANT",
                // Analytics
                "ANALYTICS_SALES_VIEW", "ANALYTICS_INVESTMENT_VIEW", "ANALYTICS_MANAGE",
                // Fraud
                "FRAUD_VIEW", "FRAUD_MANAGE", "FRAUD_LIST", "FRAUD_INVESTIGATE", "FRAUD_RESOLVE", "FRAUD_DETECT"
            );

            case "MANAGER" -> Stream.of(
                // Shop
                "SHOP_READ", "SHOP_LIST",
                // User
                "USER_CREATE", "USER_READ", "USER_LIST", "USER_UPDATE",
                // Role
                "ROLE_READ", "ROLE_LIST",
                // Product
                "PRODUCT_CREATE", "PRODUCT_READ", "PRODUCT_LIST", "PRODUCT_UPDATE",
                // Category
                "CATEGORY_CREATE", "CATEGORY_READ", "CATEGORY_LIST", "CATEGORY_UPDATE",
                // Inventory
                "INVENTORY_CREATE", "INVENTORY_READ", "INVENTORY_LIST", "INVENTORY_UPDATE", "INVENTORY_DELETE",
                "INVENTORY_ADJUST", "INVENTORY_RESERVE", "INVENTORY_HISTORY", "INVENTORY_HISTORY_VIEW", "INVENTORY_FORECAST",
                // Sales
                "SALES_CREATE", "SALES_READ", "SALES_LIST", "SALES_UPDATE", "SALES_VOID",
                // Receipt
                "RECEIPT_CREATE", "RECEIPT_READ", "RECEIPT_LIST", "RECEIPT_SEND", "RECEIPT_EMAIL",
                // Expense
                "EXPENSE_CREATE", "EXPENSE_READ", "EXPENSE_LIST", "EXPENSE_UPDATE", "EXPENSE_APPROVE",
                "EXPENSE_CATEGORY_CREATE", "EXPENSE_CATEGORY_READ", "EXPENSE_CATEGORY_LIST", "EXPENSE_CATEGORY_UPDATE",
                // Investment
                "INVESTMENT_CREATE", "INVESTMENT_READ", "INVESTMENT_LIST", "INVESTMENT_UPDATE",
                // Return
                "RETURN_CREATE", "RETURN_READ", "RETURN_LIST", "RETURN_UPDATE", "RETURN_APPROVE",
                // Audit
                "AUDIT_LOG_VIEW_SHOP",
                // Analytics
                "ANALYTICS_SALES_VIEW", "ANALYTICS_INVESTMENT_VIEW",
                // Fraud
                "FRAUD_VIEW", "FRAUD_LIST", "FRAUD_INVESTIGATE"
            );

            case "EMPLOYEE", "CASHIER" -> Stream.of(
                // Product
                "PRODUCT_READ", "PRODUCT_LIST",
                // Category
                "CATEGORY_READ", "CATEGORY_LIST",
                // Inventory
                "INVENTORY_CREATE", "INVENTORY_READ", "INVENTORY_LIST", "INVENTORY_HISTORY",
                // Sales
                "SALES_CREATE", "SALES_READ", "SALES_LIST",
                // Receipt
                "RECEIPT_CREATE", "RECEIPT_READ", "RECEIPT_LIST", "RECEIPT_SEND", "RECEIPT_EMAIL",
                // Expense
                "EXPENSE_CREATE", "EXPENSE_READ", "EXPENSE_LIST",
                "EXPENSE_CATEGORY_READ", "EXPENSE_CATEGORY_LIST",
                // Return
                "RETURN_CREATE", "RETURN_READ", "RETURN_LIST"
            );

            case "INVESTOR" -> Stream.of(
                // Investment
                "INVESTMENT_READ", "INVESTMENT_LIST",
                // Analytics
                "ANALYTICS_INVESTMENT_VIEW"
            );

            default -> Stream.empty();
        };
    }
}
