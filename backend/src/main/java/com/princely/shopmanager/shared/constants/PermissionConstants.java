package com.princely.shopmanager.shared.constants;

/**
 * Constants for all granular permissions in the system.
 * Permissions follow the naming convention: {RESOURCE}_{ACTION}
 *
 * Example: PRODUCT_CREATE, PRODUCT_READ, PRODUCT_UPDATE, PRODUCT_DELETE
 *
 * This provides fine-grained access control compared to role-based checks.
 */
public final class PermissionConstants {

    private PermissionConstants() {
        // Prevent instantiation
    }

    // ==========================================
    // SYSTEM PERMISSIONS
    // ==========================================
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    // ==========================================
    // TENANT PERMISSIONS
    // ==========================================
    public static final String TENANT_CREATE = "TENANT_CREATE";
    public static final String TENANT_READ = "TENANT_READ";
    public static final String TENANT_LIST = "TENANT_LIST";
    public static final String TENANT_UPDATE = "TENANT_UPDATE";
    public static final String TENANT_DELETE = "TENANT_DELETE";

    // ==========================================
    // SHOP PERMISSIONS
    // ==========================================
    public static final String SHOP_CREATE = "SHOP_CREATE";
    public static final String SHOP_READ = "SHOP_READ";
    public static final String SHOP_LIST = "SHOP_LIST";
    public static final String SHOP_LIST_ALL = "SHOP_LIST_ALL";
    public static final String SHOP_UPDATE = "SHOP_UPDATE";
    public static final String SHOP_DELETE = "SHOP_DELETE";

    // ==========================================
    // USER PERMISSIONS
    // ==========================================
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_LIST = "USER_LIST";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    // ==========================================
    // ROLE PERMISSIONS
    // ==========================================
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_LIST = "ROLE_LIST";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    public static final String ROLE_ASSIGN = "ROLE_ASSIGN";

    // ==========================================
    // PERMISSION PERMISSIONS
    // ==========================================
    public static final String PERMISSION_READ = "PERMISSION_READ";
    public static final String PERMISSION_LIST = "PERMISSION_LIST";

    // ==========================================
    // PRODUCT PERMISSIONS
    // ==========================================
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_READ = "PRODUCT_READ";
    public static final String PRODUCT_LIST = "PRODUCT_LIST";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    // ==========================================
    // CATEGORY PERMISSIONS
    // ==========================================
    public static final String CATEGORY_CREATE = "CATEGORY_CREATE";
    public static final String CATEGORY_READ = "CATEGORY_READ";
    public static final String CATEGORY_LIST = "CATEGORY_LIST";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE = "CATEGORY_DELETE";

    // ==========================================
    // INVENTORY PERMISSIONS
    // ==========================================
    public static final String INVENTORY_CREATE = "INVENTORY_CREATE";
    public static final String INVENTORY_READ = "INVENTORY_READ";
    public static final String INVENTORY_LIST = "INVENTORY_LIST";
    public static final String INVENTORY_UPDATE = "INVENTORY_UPDATE";
    public static final String INVENTORY_DELETE = "INVENTORY_DELETE";
    public static final String INVENTORY_HISTORY_VIEW = "INVENTORY_HISTORY_VIEW";
    public static final String INVENTORY_ADJUST = "INVENTORY_ADJUST";
    public static final String INVENTORY_RESERVE = "INVENTORY_RESERVE";
    public static final String INVENTORY_HISTORY = "INVENTORY_HISTORY";
    public static final String INVENTORY_FORECAST = "INVENTORY_FORECAST";

    // ==========================================
    // SALES PERMISSIONS
    // ==========================================
    public static final String SALES_CREATE = "SALES_CREATE";
    public static final String SALES_READ = "SALES_READ";
    public static final String SALES_LIST = "SALES_LIST";
    public static final String SALES_UPDATE = "SALES_UPDATE";
    public static final String SALES_DELETE = "SALES_DELETE";
    public static final String SALES_VOID = "SALES_VOID";

    // ==========================================
    // RECEIPT PERMISSIONS
    // ==========================================
    public static final String RECEIPT_CREATE = "RECEIPT_CREATE";
    public static final String RECEIPT_READ = "RECEIPT_READ";
    public static final String RECEIPT_LIST = "RECEIPT_LIST";
    public static final String RECEIPT_SEND = "RECEIPT_SEND";
    public static final String RECEIPT_EMAIL = "RECEIPT_EMAIL";

    // ==========================================
    // EXPENSE PERMISSIONS
    // ==========================================
    public static final String EXPENSE_CREATE = "EXPENSE_CREATE";
    public static final String EXPENSE_READ = "EXPENSE_READ";
    public static final String EXPENSE_LIST = "EXPENSE_LIST";
    public static final String EXPENSE_UPDATE = "EXPENSE_UPDATE";
    public static final String EXPENSE_DELETE = "EXPENSE_DELETE";
    public static final String EXPENSE_APPROVE = "EXPENSE_APPROVE";

    // ==========================================
    // EXPENSE CATEGORY PERMISSIONS
    // ==========================================
    public static final String EXPENSE_CATEGORY_CREATE = "EXPENSE_CATEGORY_CREATE";
    public static final String EXPENSE_CATEGORY_READ = "EXPENSE_CATEGORY_READ";
    public static final String EXPENSE_CATEGORY_LIST = "EXPENSE_CATEGORY_LIST";
    public static final String EXPENSE_CATEGORY_UPDATE = "EXPENSE_CATEGORY_UPDATE";
    public static final String EXPENSE_CATEGORY_DELETE = "EXPENSE_CATEGORY_DELETE";

    // ==========================================
    // INVESTMENT PERMISSIONS
    // ==========================================
    public static final String INVESTMENT_CREATE = "INVESTMENT_CREATE";
    public static final String INVESTMENT_READ = "INVESTMENT_READ";
    public static final String INVESTMENT_LIST = "INVESTMENT_LIST";
    public static final String INVESTMENT_UPDATE = "INVESTMENT_UPDATE";
    public static final String INVESTMENT_DELETE = "INVESTMENT_DELETE";
    public static final String INVESTMENT_CLOSE = "INVESTMENT_CLOSE";
    public static final String INVESTMENT_PROFIT_DISTRIBUTE = "INVESTMENT_PROFIT_DISTRIBUTE";

    // ==========================================
    // RETURN PERMISSIONS
    // ==========================================
    public static final String RETURN_CREATE = "RETURN_CREATE";
    public static final String RETURN_READ = "RETURN_READ";
    public static final String RETURN_LIST = "RETURN_LIST";
    public static final String RETURN_UPDATE = "RETURN_UPDATE";
    public static final String RETURN_DELETE = "RETURN_DELETE";
    public static final String RETURN_APPROVE = "RETURN_APPROVE";

    // ==========================================
    // AUDIT LOG PERMISSIONS
    // ==========================================
    public static final String AUDIT_LOG_VIEW_SHOP = "AUDIT_LOG_VIEW_SHOP";
    public static final String AUDIT_LOG_VIEW_TENANT = "AUDIT_LOG_VIEW_TENANT";

    // ==========================================
    // ANALYTICS PERMISSIONS (Feature-based)
    // ==========================================
    public static final String ANALYTICS_SALES_VIEW = "ANALYTICS_SALES_VIEW";
    public static final String ANALYTICS_INVESTMENT_VIEW = "ANALYTICS_INVESTMENT_VIEW";
    public static final String ANALYTICS_MANAGE = "ANALYTICS_MANAGE";

    // ==========================================
    // FRAUD PERMISSIONS
    // ==========================================
    public static final String FRAUD_VIEW = "FRAUD_VIEW";
    public static final String FRAUD_MANAGE = "FRAUD_MANAGE";
    public static final String FRAUD_LIST = "FRAUD_LIST";
    public static final String FRAUD_INVESTIGATE = "FRAUD_INVESTIGATE";
    public static final String FRAUD_RESOLVE = "FRAUD_RESOLVE";
    public static final String FRAUD_DETECT = "FRAUD_DETECT";
}
