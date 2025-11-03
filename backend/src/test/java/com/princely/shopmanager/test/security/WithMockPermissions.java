package com.princely.shopmanager.test.security;

import com.princely.shopmanager.test.TestConstants;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom security annotation for testing with granular permissions.
 *
 * This annotation allows tests to mock authentication with specific permissions
 * instead of roles, matching the production authorization model.
 *
 * Uses TestConstants for default tenant/shop IDs to match test-data.sql.
 *
 * Example usage:
 * <pre>
 * {@code @WithMockPermissions({"ANALYTICS_SALES_VIEW", "ANALYTICS_MANAGE"})}
 * void testMethod() {
 *     // Test code
 * }
 * </pre>
 *
 * For convenience, predefined role permissions are available:
 * <pre>
 * {@code @WithMockPermissions(role = "MANAGER")}
 * void testMethod() {
 *     // Grants all MANAGER permissions
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockPermissionsSecurityContextFactory.class)
public @interface WithMockPermissions {

    /**
     * List of permission authorities to grant.
     * Example: {"ANALYTICS_SALES_VIEW", "PRODUCT_CREATE"}
     */
    String[] value() default {};

    /**
     * Predefined role to use. Grants all permissions for that role.
     * Options: SYSTEM_ADMIN, SUPER_ADMIN, OWNER, MANAGER, EMPLOYEE, CASHIER, INVESTOR
     */
    String role() default "";

    /**
     * Username for the mock user.
     */
    String username() default TestConstants.MOCK_USERNAME;

    /**
     * Tenant ID for multi-tenant context (defaults to test-tenant-001 from test-data.sql).
     */
    String tenantId() default TestConstants.MOCK_TENANT_ID;

    /**
     * Shop ID for shop-scoped operations (defaults to test-shop-001 from test-data.sql).
     */
    String shopId() default TestConstants.MOCK_SHOP_ID;
}
