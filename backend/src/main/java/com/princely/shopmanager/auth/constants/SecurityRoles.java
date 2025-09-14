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
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    // Shop management roles
    public static final String ROLE_SHOP_OWNER = "ROLE_SHOP_OWNER";
    public static final String ROLE_SHOP_MANAGER = "ROLE_SHOP_MANAGER";
    public static final String ROLE_SHOP_EMPLOYEE = "ROLE_SHOP_EMPLOYEE";

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
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SHOP_OWNER = "SHOP_OWNER";
    public static final String SHOP_MANAGER = "SHOP_MANAGER";
    public static final String SHOP_EMPLOYEE = "SHOP_EMPLOYEE";
    public static final String CASHIER = "CASHIER";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String SALES_MANAGER = "SALES_MANAGER";
    public static final String INVESTOR = "INVESTOR";
    public static final String ACCOUNTANT = "ACCOUNTANT";
    public static final String AUDITOR = "AUDITOR";
    public static final String CUSTOMER = "CUSTOMER";
    public static final String GUEST = "GUEST";
}