package com.princely.shopmanager.test.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Custom security annotation for testing with granular permissions.
 *
 * This annotation allows tests to mock authentication with specific permissions
 * instead of roles, matching the production authorization model.
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
     * Options: SYSTEM_ADMIN, OWNER, MANAGER, EMPLOYEE, INVESTOR
     */
    String role() default "";

    /**
     * Username for the mock user.
     */
    String username() default "test-user";

    /**
     * Tenant ID for multi-tenant context.
     */
    String tenantId() default "test-tenant-001";

    /**
     * Shop ID for shop-scoped operations.
     */
    String shopId() default "test-shop-001";
}
