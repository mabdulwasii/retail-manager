package com.princely.shopmanager.shared.constants;

/**
 * Constants for permission resources.
 * Each constant represents a protected resource in the system.
 */
public final class ResourceConstants {

    private ResourceConstants() {
        // Prevent instantiation
    }

    // Core Resources
    public static final String SYSTEM = "SYSTEM";
    public static final String TENANT = "TENANT";
    public static final String SHOP = "SHOP";
    public static final String USER = "USER";
    public static final String ROLE = "ROLE";
    public static final String PERMISSION = "PERMISSION";

    // Product & Inventory Resources
    public static final String PRODUCT = "PRODUCT";
    public static final String CATEGORY = "CATEGORY";
    public static final String INVENTORY = "INVENTORY";

    // Financial Resources
    public static final String SALES = "SALES";
    public static final String RECEIPT = "RECEIPT";
    public static final String EXPENSE = "EXPENSE";
    public static final String EXPENSE_CATEGORY = "EXPENSE_CATEGORY";
    public static final String INVESTMENT = "INVESTMENT";
    public static final String RETURN = "RETURN";

    // Audit & Analytics Resources
    public static final String AUDIT_LOG = "AUDIT_LOG";
    public static final String ANALYTICS = "ANALYTICS";
    public static final String FRAUD = "FRAUD";
}
