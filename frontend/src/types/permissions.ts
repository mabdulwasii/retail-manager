/**
 * Permission Enums
 * Centralized permission definitions for consistent usage across the application
 * 
 * Usage:
 * import { Permission } from '@/types/permissions'
 * hasPermission(Permission.SHOP_UPDATE)
 */

/**
 * System-wide permissions
 * These match the permissions defined in the backend
 */
export enum Permission {
  // System Admin Permissions (Legacy - roles, not actual permissions)
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  
  // Shop Permissions
  SHOP_CREATE = 'SHOP_CREATE',
  SHOP_LIST = 'SHOP_LIST',
  SHOP_LIST_ALL = 'SHOP_LIST_ALL',
  SHOP_READ = 'SHOP_READ',
  SHOP_UPDATE = 'SHOP_UPDATE',
  SHOP_DELETE = 'SHOP_DELETE',
  SHOP_MANAGE = 'SHOP_MANAGE',
  
  // Product Permissions
  PRODUCT_CREATE = 'PRODUCT_CREATE',
  PRODUCT_LIST = 'PRODUCT_LIST',
  PRODUCT_READ = 'PRODUCT_READ',
  PRODUCT_UPDATE = 'PRODUCT_UPDATE',
  PRODUCT_DELETE = 'PRODUCT_DELETE',
  PRODUCT_MANAGE = 'PRODUCT_MANAGE',

  // Category Permissions
  CATEGORY_CREATE = 'CATEGORY_CREATE',
  CATEGORY_LIST = 'CATEGORY_LIST',
  CATEGORY_READ = 'CATEGORY_READ',
  CATEGORY_UPDATE = 'CATEGORY_UPDATE',
  CATEGORY_DELETE = 'CATEGORY_DELETE',
  
  // Inventory Permissions
  INVENTORY_CREATE = 'INVENTORY_CREATE',
  INVENTORY_LIST = 'INVENTORY_LIST',
  INVENTORY_READ = 'INVENTORY_READ',
  INVENTORY_UPDATE = 'INVENTORY_UPDATE',
  INVENTORY_DELETE = 'INVENTORY_DELETE',
  INVENTORY_ADJUST = 'INVENTORY_ADJUST',
  INVENTORY_RESERVE = 'INVENTORY_RESERVE',
  INVENTORY_HISTORY = 'INVENTORY_HISTORY',
  INVENTORY_FORECAST = 'INVENTORY_FORECAST',
  
  // Sales Permissions
  SALES_CREATE = 'SALES_CREATE',
  SALES_LIST = 'SALES_LIST',
  SALES_READ = 'SALES_READ',
  SALES_UPDATE = 'SALES_UPDATE',
  SALES_DELETE = 'SALES_DELETE',
  SALES_VOID = 'SALES_VOID',
  
  // Investment Permissions
  INVESTMENT_CREATE = 'INVESTMENT_CREATE',
  INVESTMENT_LIST = 'INVESTMENT_LIST',
  INVESTMENT_VIEW = 'INVESTMENT_VIEW',
  INVESTMENT_READ = 'INVESTMENT_READ',
  INVESTMENT_UPDATE = 'INVESTMENT_UPDATE',
  INVESTMENT_DELETE = 'INVESTMENT_DELETE',
  INVESTMENT_CLOSE = 'INVESTMENT_CLOSE',
  INVESTMENT_PROFIT_DISTRIBUTE = 'INVESTMENT_PROFIT_DISTRIBUTE',
  
  // Receipt Permissions
  RECEIPT_CREATE = 'RECEIPT_CREATE',
  RECEIPT_LIST = 'RECEIPT_LIST',
  RECEIPT_READ = 'RECEIPT_READ',
  RECEIPT_SEND = 'RECEIPT_SEND',
  RECEIPT_EMAIL = 'RECEIPT_EMAIL',
  
  // Expense Permissions
  EXPENSE_CREATE = 'EXPENSE_CREATE',
  EXPENSE_LIST = 'EXPENSE_LIST',
  EXPENSE_READ = 'EXPENSE_READ',
  EXPENSE_UPDATE = 'EXPENSE_UPDATE',
  EXPENSE_DELETE = 'EXPENSE_DELETE',
  EXPENSE_APPROVE = 'EXPENSE_APPROVE',
  EXPENSE_SUMMARY = 'EXPENSE_SUMMARY',
  
  // Expense Category Permissions
  EXPENSE_CATEGORY_CREATE = 'EXPENSE_CATEGORY_CREATE',
  EXPENSE_CATEGORY_LIST = 'EXPENSE_CATEGORY_LIST',
  EXPENSE_CATEGORY_READ = 'EXPENSE_CATEGORY_READ',
  EXPENSE_CATEGORY_UPDATE = 'EXPENSE_CATEGORY_UPDATE',
  EXPENSE_CATEGORY_DELETE = 'EXPENSE_CATEGORY_DELETE',
  
  // Analytics Permissions
  ANALYTICS_VIEW = 'ANALYTICS_VIEW', // Legacy - kept for backward compatibility
  ANALYTICS_VIEW_SHOP = 'ANALYTICS_VIEW_SHOP',
  ANALYTICS_VIEW_TENANT = 'ANALYTICS_VIEW_TENANT',
  ANALYTICS_SALES_VIEW = 'ANALYTICS_SALES_VIEW',
  ANALYTICS_INVESTMENT_VIEW = 'ANALYTICS_INVESTMENT_VIEW',
  ANALYTICS_MANAGE = 'ANALYTICS_MANAGE',
  
  // Fraud Detection Permissions
  FRAUD_DETECTION_VIEW = 'FRAUD_DETECTION_VIEW', // Legacy - kept for backward compatibility
  FRAUD_VIEW = 'FRAUD_VIEW',
  FRAUD_LIST = 'FRAUD_LIST',
  FRAUD_INVESTIGATE = 'FRAUD_INVESTIGATE',
  FRAUD_RESOLVE = 'FRAUD_RESOLVE',
  FRAUD_DETECT = 'FRAUD_DETECT',
  FRAUD_MANAGE = 'FRAUD_MANAGE',
  
  // User Management Permissions
  USER_MANAGE = 'USER_MANAGE',
  USER_LIST = 'USER_LIST',
  USER_LIST_ALL = 'USER_LIST_ALL',
  USER_READ = 'USER_READ',
  USER_CREATE = 'USER_CREATE',
  USER_UPDATE = 'USER_UPDATE',
  USER_DELETE = 'USER_DELETE',
  
  // Role Management Permissions
  ROLE_CREATE = 'ROLE_CREATE',
  ROLE_LIST = 'ROLE_LIST',
  ROLE_READ = 'ROLE_READ',
  ROLE_UPDATE = 'ROLE_UPDATE',
  ROLE_DELETE = 'ROLE_DELETE',
  ROLE_ASSIGN = 'ROLE_ASSIGN',
  ROLE_PERMISSION_ADD = 'ROLE_PERMISSION_ADD',
  ROLE_PERMISSION_REMOVE = 'ROLE_PERMISSION_REMOVE',
  
  // Permission Management Permissions
  PERMISSION_LIST = 'PERMISSION_LIST',
  PERMISSION_READ = 'PERMISSION_READ',
  
  // Tenant Management Permissions
  TENANT_MANAGE = 'TENANT_MANAGE',
  TENANT_CREATE = 'TENANT_CREATE',
  TENANT_LIST = 'TENANT_LIST',
  TENANT_READ = 'TENANT_READ',
  TENANT_UPDATE = 'TENANT_UPDATE',
  TENANT_DELETE = 'TENANT_DELETE',
  
  // Tenant Configuration Permissions
  TENANT_CONFIG_CREATE = 'TENANT_CONFIG_CREATE',
  TENANT_CONFIG_READ = 'TENANT_CONFIG_READ',
  TENANT_CONFIG_UPDATE = 'TENANT_CONFIG_UPDATE',
  TENANT_CONFIG_DELETE = 'TENANT_CONFIG_DELETE',
  
  // Product Return Permissions
  RETURN_CREATE = 'RETURN_CREATE',
  RETURN_LIST = 'RETURN_LIST',
  RETURN_READ = 'RETURN_READ',
  RETURN_UPDATE = 'RETURN_UPDATE',
  RETURN_DELETE = 'RETURN_DELETE',
  RETURN_APPROVE = 'RETURN_APPROVE',
  
  // Audit Log Permissions
  AUDIT_LOG_VIEW = 'AUDIT_LOG_VIEW',
  AUDIT_LOG_LIST = 'AUDIT_LOG_LIST',
  AUDIT_LOG_EXPORT = 'AUDIT_LOG_EXPORT',
  AUDIT_LOG_VIEW_SHOP = 'AUDIT_LOG_VIEW_SHOP',
  AUDIT_LOG_VIEW_TENANT = 'AUDIT_LOG_VIEW_TENANT',

  // System Settings Permissions (Embedded Mode Only)
  SYSTEM_SETTING_VIEW = 'SYSTEM_SETTING_VIEW',
  SYSTEM_SETTING_UPDATE = 'SYSTEM_SETTING_UPDATE',
  SYSTEM_SETTING_MANAGE = 'SYSTEM_SETTING_MANAGE',
}

/**
 * Permission Groups for common access patterns
 */
export const PermissionGroups = {
  SHOP_MANAGEMENT: [
    Permission.SHOP_CREATE,
    Permission.SHOP_LIST,
    Permission.SHOP_UPDATE,
    Permission.SHOP_DELETE,
  ],
  
  PRODUCT_MANAGEMENT: [
    Permission.PRODUCT_CREATE,
    Permission.PRODUCT_LIST,
    Permission.PRODUCT_UPDATE,
    Permission.PRODUCT_DELETE,
  ],
  
  INVENTORY_MANAGEMENT: [
    Permission.INVENTORY_CREATE,
    Permission.INVENTORY_LIST,
    Permission.INVENTORY_UPDATE,
    Permission.INVENTORY_DELETE,
  ],
  
  SALES_MANAGEMENT: [
    Permission.SALES_CREATE,
    Permission.SALES_READ,
    Permission.SALES_UPDATE,
    Permission.SALES_DELETE,
  ],
  
  ADMIN_PERMISSIONS: [
    Permission.SYSTEM_ADMIN,
    Permission.TENANT_ADMIN,
  ],
}

/**
 * Check if a permission is an admin permission
 */
export const isAdminPermission = (permission: string): boolean => {
  return PermissionGroups.ADMIN_PERMISSIONS.some(
    adminPerm => adminPerm === permission
  )
}

/**
 * Get display name for a permission
 */
export const getPermissionDisplayName = (permission: string): string => {
  const displayNames: Record<string, string> = {
    [Permission.SYSTEM_ADMIN]: 'System Administrator',
    [Permission.TENANT_ADMIN]: 'Tenant Administrator',
    
    // Shop
    [Permission.SHOP_CREATE]: 'Create Shop',
    [Permission.SHOP_LIST]: 'List Shops',
    [Permission.SHOP_LIST_ALL]: 'List All Shops Across Tenant',
    [Permission.SHOP_READ]: 'View Shop Details',
    [Permission.SHOP_UPDATE]: 'Update Shop',
    [Permission.SHOP_DELETE]: 'Delete Shop',
    [Permission.SHOP_MANAGE]: 'Manage Shop',
    
    // Product
    [Permission.PRODUCT_CREATE]: 'Create Product',
    [Permission.PRODUCT_LIST]: 'List Products',
    [Permission.PRODUCT_READ]: 'View Product Details',
    [Permission.PRODUCT_UPDATE]: 'Update Product',
    [Permission.PRODUCT_DELETE]: 'Delete Product',
    [Permission.PRODUCT_MANAGE]: 'Manage Products',
    
    // Category
    [Permission.CATEGORY_CREATE]: 'Create Category',
    [Permission.CATEGORY_LIST]: 'List Categories',
    [Permission.CATEGORY_READ]: 'View Category Details',
    [Permission.CATEGORY_UPDATE]: 'Update Category',
    [Permission.CATEGORY_DELETE]: 'Delete Category',
    
    // Inventory
    [Permission.INVENTORY_CREATE]: 'Create Inventory',
    [Permission.INVENTORY_LIST]: 'List Inventory',
    [Permission.INVENTORY_READ]: 'View Inventory Details',
    [Permission.INVENTORY_UPDATE]: 'Update Inventory',
    [Permission.INVENTORY_DELETE]: 'Delete Inventory',
    [Permission.INVENTORY_ADJUST]: 'Adjust Inventory Stock Levels',
    [Permission.INVENTORY_RESERVE]: 'Reserve Inventory Stock',
    [Permission.INVENTORY_HISTORY]: 'View Inventory History',
    [Permission.INVENTORY_FORECAST]: 'View Inventory Forecasts',
    
    // Sales
    [Permission.SALES_CREATE]: 'Create Sale',
    [Permission.SALES_LIST]: 'List Sales',
    [Permission.SALES_READ]: 'View Sale Details',
    [Permission.SALES_UPDATE]: 'Update Sale',
    [Permission.SALES_DELETE]: 'Delete Sale',
    [Permission.SALES_VOID]: 'Void Sales Transactions',
    
    // Investment
    [Permission.INVESTMENT_CREATE]: 'Create Investment',
    [Permission.INVESTMENT_LIST]: 'List Investments',
    [Permission.INVESTMENT_VIEW]: 'View Investment Analytics',
    [Permission.INVESTMENT_READ]: 'View Investment Details',
    [Permission.INVESTMENT_UPDATE]: 'Update Investment',
    [Permission.INVESTMENT_DELETE]: 'Delete Investment',
    [Permission.INVESTMENT_CLOSE]: 'Close Investments',
    [Permission.INVESTMENT_PROFIT_DISTRIBUTE]: 'Distribute Investment Profits',
    
    // Receipt
    [Permission.RECEIPT_CREATE]: 'Create Receipt',
    [Permission.RECEIPT_LIST]: 'List Receipts',
    [Permission.RECEIPT_READ]: 'View Receipt Details',
    [Permission.RECEIPT_SEND]: 'Send Receipt',
    [Permission.RECEIPT_EMAIL]: 'Email Receipts',
    
    // Expense
    [Permission.EXPENSE_CREATE]: 'Create Expense',
    [Permission.EXPENSE_LIST]: 'List Expenses',
    [Permission.EXPENSE_READ]: 'View Expense Details',
    [Permission.EXPENSE_UPDATE]: 'Update Expense',
    [Permission.EXPENSE_DELETE]: 'Delete Expense',
    [Permission.EXPENSE_APPROVE]: 'Approve Expense',
    [Permission.EXPENSE_SUMMARY]: 'View Expense Summary',
    
    // Expense Category
    [Permission.EXPENSE_CATEGORY_CREATE]: 'Create Expense Category',
    [Permission.EXPENSE_CATEGORY_LIST]: 'List Expense Categories',
    [Permission.EXPENSE_CATEGORY_READ]: 'View Expense Category Details',
    [Permission.EXPENSE_CATEGORY_UPDATE]: 'Update Expense Category',
    [Permission.EXPENSE_CATEGORY_DELETE]: 'Delete Expense Category',
    
    // Analytics
    [Permission.ANALYTICS_VIEW]: 'View Analytics',
    [Permission.ANALYTICS_VIEW_SHOP]: 'View Shop-Level Analytics',
    [Permission.ANALYTICS_VIEW_TENANT]: 'View Tenant-Level Analytics',
    [Permission.ANALYTICS_SALES_VIEW]: 'View Sales Analytics',
    [Permission.ANALYTICS_INVESTMENT_VIEW]: 'View Investment Analytics',
    [Permission.ANALYTICS_MANAGE]: 'Manage Analytics',
    
    // Fraud
    [Permission.FRAUD_DETECTION_VIEW]: 'View Fraud Detection',
    [Permission.FRAUD_VIEW]: 'View Fraud Alerts',
    [Permission.FRAUD_LIST]: 'List Fraud Alerts',
    [Permission.FRAUD_INVESTIGATE]: 'Investigate Fraud',
    [Permission.FRAUD_RESOLVE]: 'Resolve Fraud Alerts',
    [Permission.FRAUD_DETECT]: 'Detect Fraud',
    [Permission.FRAUD_MANAGE]: 'Manage Fraud Detection',
    
    // User
    [Permission.USER_MANAGE]: 'Manage Users',
    [Permission.USER_LIST]: 'List Users',
    [Permission.USER_LIST_ALL]: 'List All Users Across All Tenants',
    [Permission.USER_READ]: 'View User Details',
    [Permission.USER_CREATE]: 'Create User',
    [Permission.USER_UPDATE]: 'Update User',
    [Permission.USER_DELETE]: 'Delete User',
    
    // Role
    [Permission.ROLE_CREATE]: 'Create Role',
    [Permission.ROLE_LIST]: 'List Roles',
    [Permission.ROLE_READ]: 'View Role Details',
    [Permission.ROLE_UPDATE]: 'Update Role',
    [Permission.ROLE_DELETE]: 'Delete Role',
    [Permission.ROLE_ASSIGN]: 'Assign Roles to Users',
    [Permission.ROLE_PERMISSION_ADD]: 'Add Permissions to Roles',
    [Permission.ROLE_PERMISSION_REMOVE]: 'Remove Permissions from Roles',
    
    // Permission
    [Permission.PERMISSION_LIST]: 'List Permissions',
    [Permission.PERMISSION_READ]: 'View Permission Details',
    
    // Tenant
    [Permission.TENANT_MANAGE]: 'Manage Tenant',
    [Permission.TENANT_CREATE]: 'Create Tenant',
    [Permission.TENANT_LIST]: 'List Tenants',
    [Permission.TENANT_READ]: 'View Tenant Details',
    [Permission.TENANT_UPDATE]: 'Update Tenant',
    [Permission.TENANT_DELETE]: 'Delete Tenant',
    
    // Tenant Config
    [Permission.TENANT_CONFIG_CREATE]: 'Create Tenant Configuration',
    [Permission.TENANT_CONFIG_READ]: 'View Tenant Configuration',
    [Permission.TENANT_CONFIG_UPDATE]: 'Update Tenant Configuration',
    [Permission.TENANT_CONFIG_DELETE]: 'Delete Tenant Configuration',
    
    // Product Return
    [Permission.RETURN_CREATE]: 'Create Return',
    [Permission.RETURN_LIST]: 'List Returns',
    [Permission.RETURN_READ]: 'View Return Details',
    [Permission.RETURN_UPDATE]: 'Update Return',
    [Permission.RETURN_DELETE]: 'Delete Return',
    [Permission.RETURN_APPROVE]: 'Approve Product Returns',
    
    // Audit Log
    [Permission.AUDIT_LOG_VIEW]: 'View Audit Logs',
    [Permission.AUDIT_LOG_LIST]: 'List Audit Logs',
    [Permission.AUDIT_LOG_EXPORT]: 'Export Audit Logs',
    [Permission.AUDIT_LOG_VIEW_SHOP]: 'View Shop Audit Logs',
    [Permission.AUDIT_LOG_VIEW_TENANT]: 'View Tenant Audit Logs',

    // System Settings
    [Permission.SYSTEM_SETTING_VIEW]: 'View System Settings',
    [Permission.SYSTEM_SETTING_UPDATE]: 'Update System Settings',
    [Permission.SYSTEM_SETTING_MANAGE]: 'Manage All System Settings',
  }

  return displayNames[permission] || permission
}
