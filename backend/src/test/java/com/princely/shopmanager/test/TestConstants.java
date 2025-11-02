package com.princely.shopmanager.test;

/**
 * Test constants for integration and unit testing.
 * These constants match the IDs and names defined in test-data.sql.
 *
 * Usage:
 * <pre>
 * assertEquals(TestConstants.TEST_TENANT_ID, tenant.getId());
 * assertEquals(TestConstants.EXPECTED_PRODUCT_COUNT, products.size());
 * </pre>
 */
public final class TestConstants {

    private TestConstants() {
        // Prevent instantiation
    }

    // ==========================================
    // TENANT IDs
    // ==========================================
    public static final String TEST_TENANT_001 = "test-tenant-001";
    public static final String TEST_TENANT_002 = "test-tenant-002";

    // ==========================================
    // SHOP IDs
    // ==========================================
    public static final String TEST_SHOP_001 = "test-shop-001";
    public static final String TEST_SHOP_002 = "test-shop-002";
    public static final String TEST_SHOP_003 = "test-shop-003";

    // ==========================================
    // USER IDs
    // ==========================================
    public static final String USER_ADMIN_001 = "user-admin-001";
    public static final String USER_OWNER_001 = "user-owner-001";
    public static final String USER_MANAGER_001 = "user-manager-001";
    public static final String USER_EMPLOYEE_001 = "user-employee-001";
    public static final String USER_INVESTOR_001 = "user-investor-001";

    // ==========================================
    // USER EMAILS
    // ==========================================
    public static final String ADMIN_EMAIL = "admin@testretail.com";
    public static final String OWNER_EMAIL = "owner@testretail.com";
    public static final String MANAGER_EMAIL = "manager@testretail.com";
    public static final String EMPLOYEE_EMAIL = "employee@testretail.com";
    public static final String INVESTOR_EMAIL = "investor@testretail.com";

    // ==========================================
    // KEYCLOAK IDs
    // ==========================================
    public static final String KC_ADMIN_001 = "kc-admin-001";
    public static final String KC_OWNER_001 = "kc-owner-001";
    public static final String KC_MANAGER_001 = "kc-manager-001";
    public static final String KC_EMPLOYEE_001 = "kc-employee-001";
    public static final String KC_INVESTOR_001 = "kc-investor-001";

    // ==========================================
    // CATEGORY IDs
    // ==========================================
    public static final String CAT_ELECTRONICS = "cat-001";
    public static final String CAT_CLOTHING = "cat-002";
    public static final String CAT_FOOD_BEVERAGE = "cat-003";

    // ==========================================
    // PRODUCT IDs
    // ==========================================
    public static final String PROD_WIRELESS_MOUSE = "prod-001";
    public static final String PROD_USB_KEYBOARD = "prod-002";
    public static final String PROD_COTTON_TSHIRT = "prod-003";
    public static final String PROD_ENERGY_DRINK = "prod-004";

    // ==========================================
    // PRODUCT SKUs
    // ==========================================
    public static final String SKU_WIRELESS_MOUSE = "MOUSE-001";
    public static final String SKU_USB_KEYBOARD = "KB-001";
    public static final String SKU_COTTON_TSHIRT = "TSHIRT-001";
    public static final String SKU_ENERGY_DRINK = "DRINK-001";

    // ==========================================
    // INVENTORY IDs
    // ==========================================
    public static final String INV_001 = "inv-001";
    public static final String INV_002 = "inv-002";
    public static final String INV_003 = "inv-003";
    public static final String INV_004 = "inv-004";

    // ==========================================
    // EXPENSE CATEGORY IDs
    // ==========================================
    public static final String EXP_CAT_UTILITIES = "exp-cat-001";
    public static final String EXP_CAT_RENT = "exp-cat-002";
    public static final String EXP_CAT_SALARIES = "exp-cat-003";

    // ==========================================
    // SALES TRANSACTION IDs
    // ==========================================
    public static final String TXN_001 = "txn-001";
    public static final String TXN_002 = "txn-002";
    public static final String TXN_003 = "txn-003";

    // ==========================================
    // TRANSACTION NUMBERS
    // ==========================================
    public static final String TXN_NUMBER_001 = "TXN-2024-001";
    public static final String TXN_NUMBER_002 = "TXN-2024-002";
    public static final String TXN_NUMBER_003 = "TXN-2024-003";

    // ==========================================
    // RECEIPT IDs
    // ==========================================
    public static final String RCP_001 = "rcp-001";
    public static final String RCP_002 = "rcp-002";

    // ==========================================
    // RECEIPT NUMBERS
    // ==========================================
    public static final String RCP_NUMBER_001 = "RCP-2024-001";
    public static final String RCP_NUMBER_002 = "RCP-2024-002";

    // ==========================================
    // EXPENSE IDs
    // ==========================================
    public static final String EXP_001 = "exp-001";
    public static final String EXP_002 = "exp-002";
    public static final String EXP_003 = "exp-003";

    // ==========================================
    // INVESTMENT IDs
    // ==========================================
    public static final String INVESTMENT_001 = "inv-001";
    public static final String INVESTMENT_002 = "inv-002";

    // ==========================================
    // INVESTOR SHARE IDs
    // ==========================================
    public static final String SHARE_001 = "share-001";
    public static final String SHARE_002 = "share-002";
    public static final String SHARE_003 = "share-003";
    public static final String SHARE_004 = "share-004";

    // ==========================================
    // RETURN IDs
    // ==========================================
    public static final String RET_001 = "ret-001";
    public static final String RET_ITEM_001 = "ret-item-001";

    // ==========================================
    // INVENTORY MOVEMENT IDs
    // ==========================================
    public static final String MOV_001 = "mov-001";
    public static final String MOV_002 = "mov-002";
    public static final String MOV_003 = "mov-003";
    public static final String MOV_004 = "mov-004";

    // ==========================================
    // AUDIT LOG IDs
    // ==========================================
    public static final String AUDIT_001 = "audit-001";
    public static final String AUDIT_002 = "audit-002";
    public static final String AUDIT_003 = "audit-003";

    // ==========================================
    // FEATURE FLAG IDs
    // ==========================================
    public static final String FLAG_ANALYTICS = "flag-001";
    public static final String FLAG_INVESTMENT = "flag-002";
    public static final String FLAG_FRAUD_DETECTION = "flag-003";

    // ==========================================
    // ENTITY COUNTS (for test assertions)
    // ==========================================
    public static final int EXPECTED_TENANT_COUNT = 2;
    public static final int EXPECTED_SHOP_COUNT = 3;
    public static final int EXPECTED_USER_COUNT = 5;
    public static final int EXPECTED_CATEGORY_COUNT = 3;
    public static final int EXPECTED_PRODUCT_COUNT = 4;
    public static final int EXPECTED_INVENTORY_COUNT = 4;
    public static final int EXPECTED_EXPENSE_CATEGORY_COUNT = 3;
    public static final int EXPECTED_TRANSACTION_COUNT = 3;
    public static final int EXPECTED_TRANSACTION_ITEM_COUNT = 3;
    public static final int EXPECTED_RECEIPT_COUNT = 2;
    public static final int EXPECTED_EXPENSE_COUNT = 3;
    public static final int EXPECTED_INVESTMENT_COUNT = 2;
    public static final int EXPECTED_INVESTOR_SHARE_COUNT = 4;
    public static final int EXPECTED_RETURN_COUNT = 1;
    public static final int EXPECTED_INVENTORY_MOVEMENT_COUNT = 4;
    public static final int EXPECTED_AUDIT_LOG_COUNT = 3;
    public static final int EXPECTED_FEATURE_FLAG_COUNT = 3;

    // ==========================================
    // PRODUCT NAMES
    // ==========================================
    public static final String PRODUCT_NAME_WIRELESS_MOUSE = "Wireless Mouse";
    public static final String PRODUCT_NAME_USB_KEYBOARD = "USB Keyboard";
    public static final String PRODUCT_NAME_COTTON_TSHIRT = "Cotton T-Shirt";
    public static final String PRODUCT_NAME_ENERGY_DRINK = "Energy Drink";

    // ==========================================
    // CATEGORY NAMES
    // ==========================================
    public static final String CATEGORY_NAME_ELECTRONICS = "Electronics";
    public static final String CATEGORY_NAME_CLOTHING = "Clothing";
    public static final String CATEGORY_NAME_FOOD_BEVERAGE = "Food & Beverage";

    // ==========================================
    // SHOP NAMES
    // ==========================================
    public static final String SHOP_NAME_DOWNTOWN = "Downtown Store";
    public static final String SHOP_NAME_UPTOWN = "Uptown Branch";
    public static final String SHOP_NAME_DEMO = "Demo Shop";

    // ==========================================
    // TENANT NAMES
    // ==========================================
    public static final String TENANT_NAME_TEST_RETAIL = "Test Retail Corp";
    public static final String TENANT_NAME_DEMO_ELECTRONICS = "Demo Electronics Ltd";

    // ==========================================
    // MOCK SECURITY CONTEXT DEFAULTS
    // ==========================================
    public static final String MOCK_USER_ID = "test-user-id";
    public static final String MOCK_USERNAME = "test-user";
    public static final String MOCK_TENANT_ID = TEST_TENANT_001;
    public static final String MOCK_SHOP_ID = TEST_SHOP_001;
}
