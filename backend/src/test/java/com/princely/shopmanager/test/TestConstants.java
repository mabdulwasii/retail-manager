package com.princely.shopmanager.test;

/**
 * Test constants for integration and unit testing with UUID identifiers.
 * These constants match the IDs and names defined in test-data.sql.
 *
 * All IDs use UUID format with sequential patterns for easy identification:
 * - 550e84XX... = Tenants
 * - 650e84XX... = Shops
 * - 750e84XX... = Users
 * - 850e84XX... = Products
 * - 950e84XX... = Categories
 * - a50e84XX... = Inventory
 * - b50e84XX... = Expense Categories
 * - c50e84XX... = Sales Transactions
 * - d50e84XX... = Transaction Items
 * - e50e84XX... = Receipts
 * - f50e84XX... = Expenses
 * - 050e84XX... = Investments
 * - 150e84XX... = Investor Shares
 * - 250e84XX... = Returns
 * - 350e84XX... = Return Items
 * - 450e84XX... = Inventory Movements
 *
 * Usage:
 * <pre>
 * assertEquals(TestConstants.TEST_TENANT_001, tenant.getId());
 * assertEquals(TestConstants.EXPECTED_PRODUCT_COUNT, products.size());
 * </pre>
 */
public final class TestConstants {

    private TestConstants() {
        // Prevent instantiation
    }

    // ==========================================
    // TENANT UUIDs
    // ==========================================
    public static final String TEST_TENANT_001 = "550e8400-e29b-41d4-a716-446655440001";
    public static final String TEST_TENANT_002 = "550e8400-e29b-41d4-a716-446655440002";

    // ==========================================
    // SHOP UUIDs
    // ==========================================
    public static final String TEST_SHOP_001 = "650e8400-e29b-41d4-a716-446655440001";
    public static final String TEST_SHOP_002 = "650e8400-e29b-41d4-a716-446655440002";
    public static final String TEST_SHOP_003 = "650e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // USER UUIDs
    // ==========================================
    public static final String USER_ADMIN_001 = "750e8400-e29b-41d4-a716-446655440001";
    public static final String USER_OWNER_001 = "750e8400-e29b-41d4-a716-446655440002";
    public static final String USER_MANAGER_001 = "750e8400-e29b-41d4-a716-446655440003";
    public static final String USER_EMPLOYEE_001 = "750e8400-e29b-41d4-a716-446655440004";
    public static final String USER_INVESTOR_001 = "750e8400-e29b-41d4-a716-446655440005";

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
    // CATEGORY UUIDs
    // ==========================================
    public static final String CAT_ELECTRONICS = "950e8400-e29b-41d4-a716-446655440001";
    public static final String CAT_CLOTHING = "950e8400-e29b-41d4-a716-446655440002";
    public static final String CAT_FOOD_BEVERAGE = "950e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // PRODUCT UUIDs
    // ==========================================
    public static final String PROD_WIRELESS_MOUSE = "850e8400-e29b-41d4-a716-446655440001";
    public static final String PROD_USB_KEYBOARD = "850e8400-e29b-41d4-a716-446655440002";
    public static final String PROD_COTTON_TSHIRT = "850e8400-e29b-41d4-a716-446655440003";
    public static final String PROD_ENERGY_DRINK = "850e8400-e29b-41d4-a716-446655440004";

    // ==========================================
    // PRODUCT SKUs
    // ==========================================
    public static final String SKU_WIRELESS_MOUSE = "MOUSE-001";
    public static final String SKU_USB_KEYBOARD = "KB-001";
    public static final String SKU_COTTON_TSHIRT = "TSHIRT-001";
    public static final String SKU_ENERGY_DRINK = "DRINK-001";

    // ==========================================
    // INVENTORY UUIDs
    // ==========================================
    public static final String INV_001 = "a50e8400-e29b-41d4-a716-446655440001";
    public static final String INV_002 = "a50e8400-e29b-41d4-a716-446655440002";
    public static final String INV_003 = "a50e8400-e29b-41d4-a716-446655440003";
    public static final String INV_004 = "a50e8400-e29b-41d4-a716-446655440004";

    // ==========================================
    // EXPENSE CATEGORY UUIDs
    // ==========================================
    public static final String EXP_CAT_UTILITIES = "b50e8400-e29b-41d4-a716-446655440001";
    public static final String EXP_CAT_RENT = "b50e8400-e29b-41d4-a716-446655440002";
    public static final String EXP_CAT_SALARIES = "b50e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // SALES TRANSACTION UUIDs
    // ==========================================
    public static final String TXN_001 = "c50e8400-e29b-41d4-a716-446655440001";
    public static final String TXN_002 = "c50e8400-e29b-41d4-a716-446655440002";
    public static final String TXN_003 = "c50e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // TRANSACTION NUMBERS
    // ==========================================
    public static final String TXN_NUMBER_001 = "TXN-2024-001";
    public static final String TXN_NUMBER_002 = "TXN-2024-002";
    public static final String TXN_NUMBER_003 = "TXN-2024-003";

    // ==========================================
    // TRANSACTION ITEM UUIDs
    // ==========================================
    public static final String TXN_ITEM_001 = "d50e8400-e29b-41d4-a716-446655440001";
    public static final String TXN_ITEM_002 = "d50e8400-e29b-41d4-a716-446655440002";
    public static final String TXN_ITEM_003 = "d50e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // RECEIPT UUIDs
    // ==========================================
    public static final String RCP_001 = "e50e8400-e29b-41d4-a716-446655440001";
    public static final String RCP_002 = "e50e8400-e29b-41d4-a716-446655440002";

    // ==========================================
    // RECEIPT NUMBERS
    // ==========================================
    public static final String RCP_NUMBER_001 = "RCP-2024-001";
    public static final String RCP_NUMBER_002 = "RCP-2024-002";

    // ==========================================
    // EXPENSE UUIDs
    // ==========================================
    public static final String EXP_001 = "f50e8400-e29b-41d4-a716-446655440001";
    public static final String EXP_002 = "f50e8400-e29b-41d4-a716-446655440002";
    public static final String EXP_003 = "f50e8400-e29b-41d4-a716-446655440003";

    // ==========================================
    // INVESTMENT UUIDs
    // ==========================================
    public static final String INVESTMENT_001 = "050e8400-e29b-41d4-a716-446655440001";
    public static final String INVESTMENT_002 = "050e8400-e29b-41d4-a716-446655440002";

    // ==========================================
    // INVESTOR SHARE UUIDs
    // ==========================================
    public static final String SHARE_001 = "150e8400-e29b-41d4-a716-446655440001";
    public static final String SHARE_002 = "150e8400-e29b-41d4-a716-446655440002";
    public static final String SHARE_003 = "150e8400-e29b-41d4-a716-446655440003";
    public static final String SHARE_004 = "150e8400-e29b-41d4-a716-446655440004";

    // ==========================================
    // RETURN UUIDs
    // ==========================================
    public static final String RET_001 = "250e8400-e29b-41d4-a716-446655440001";
    public static final String RET_ITEM_001 = "350e8400-e29b-41d4-a716-446655440001";

    // ==========================================
    // INVENTORY MOVEMENT UUIDs
    // ==========================================
    public static final String MOV_001 = "450e8400-e29b-41d4-a716-446655440001";
    public static final String MOV_002 = "450e8400-e29b-41d4-a716-446655440002";
    public static final String MOV_003 = "450e8400-e29b-41d4-a716-446655440003";
    public static final String MOV_004 = "450e8400-e29b-41d4-a716-446655440004";

    // ==========================================
    // AUDIT LOG UUIDs
    // ==========================================
    public static final String AUDIT_001 = "550e8400-e29b-41d4-a716-446655440011";
    public static final String AUDIT_002 = "550e8400-e29b-41d4-a716-446655440012";
    public static final String AUDIT_003 = "550e8400-e29b-41d4-a716-446655440013";

    // ==========================================
    // FEATURE FLAG UUIDs
    // ==========================================
    public static final String FLAG_ANALYTICS = "650e8400-e29b-41d4-a716-446655440011";
    public static final String FLAG_INVESTMENT = "650e8400-e29b-41d4-a716-446655440012";
    public static final String FLAG_FRAUD_DETECTION = "650e8400-e29b-41d4-a716-446655440013";

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
    public static final String MOCK_USER_ID = "750e8400-e29b-41d4-a716-446655440000";
    public static final String MOCK_USERNAME = "test-user";
    public static final String MOCK_TENANT_ID = TEST_TENANT_001;
    public static final String MOCK_SHOP_ID = TEST_SHOP_001;

    // ==========================================
    // INVENTORY STOCK LEVELS (from test-data.sql)
    // ==========================================
    public static final int MOUSE_CURRENT_STOCK = 100;
    public static final int MOUSE_RESERVED_STOCK = 5;
    public static final int KEYBOARD_CURRENT_STOCK = 75;
    public static final int TSHIRT_CURRENT_STOCK = 200;
    public static final int DRINK_CURRENT_STOCK = 500;

    // ==========================================
    // TRANSACTION AMOUNTS (from test-data.sql)
    // ==========================================
    public static final double TXN_001_AMOUNT = 51.98;  // 2 mice
    public static final double TXN_002_AMOUNT = 25.99;  // 1 mouse
    public static final double TXN_003_AMOUNT = 19.99;  // 1 t-shirt
    public static final double TOTAL_SALES_AMOUNT = 97.96;

    // ==========================================
    // EXPENSE AMOUNTS (from test-data.sql)
    // ==========================================
    public static final double EXP_001_AMOUNT = 250.00;   // Utilities
    public static final double EXP_002_AMOUNT = 2000.00;  // Rent
    public static final double EXP_003_AMOUNT = 5000.00;  // Salaries
    public static final double TOTAL_APPROVED_EXPENSES = 5250.00; // Utilities + Salaries

    // ==========================================
    // INVESTMENT AMOUNTS (from test-data.sql)
    // ==========================================
    public static final double INV_001_AMOUNT = 50000.00;
    public static final double INV_002_AMOUNT = 25000.00;
}
