package com.princely.shopmanager.auth.constants;

/**
 * Security role constants used throughout the application.
 *
 * This class centralizes all role definitions to avoid duplication
 * and ensure consistency across the codebase.
 */
public final class SecurityRoles {

    private SecurityRoles() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // System-level roles
    public static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";
    public static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";

    // Shop management roles - Simplified names
    public static final String ROLE_OWNER = "ROLE_OWNER";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";

    // Operations roles
    public static final String ROLE_CASHIER = "ROLE_CASHIER";
    public static final String ROLE_INVENTORY_MANAGER = "ROLE_INVENTORY_MANAGER";
    public static final String ROLE_SALES_MANAGER = "ROLE_SALES_MANAGER";

    // Financial roles
    public static final String ROLE_INVESTOR = "ROLE_INVESTOR";
    public static final String ROLE_ACCOUNTANT = "ROLE_ACCOUNTANT";
    public static final String ROLE_AUDITOR = "ROLE_AUDITOR";

    // Customer roles
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_GUEST = "ROLE_GUEST";

    // Role names without prefix (for use in @PreAuthorize)
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String TENANT_ADMIN = "TENANT_ADMIN";
    public static final String OWNER = "OWNER";
    public static final String MANAGER = "MANAGER";
    public static final String EMPLOYEE = "EMPLOYEE";
    public static final String CASHIER = "CASHIER";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String SALES_MANAGER = "SALES_MANAGER";
    public static final String INVESTOR = "INVESTOR";
    public static final String ACCOUNTANT = "ACCOUNTANT";
    public static final String AUDITOR = "AUDITOR";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String GUEST = "GUEST";
}