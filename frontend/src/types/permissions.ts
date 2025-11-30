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
  // System Admin Permissions
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  
  // Shop Permissions
  SHOP_CREATE = 'SHOP_CREATE',
  SHOP_LIST = 'SHOP_LIST',
  SHOP_UPDATE = 'SHOP_UPDATE',
  SHOP_DELETE = 'SHOP_DELETE',
  SHOP_MANAGE = 'SHOP_MANAGE',
  
  // Product Permissions
  PRODUCT_CREATE = 'PRODUCT_CREATE',
  PRODUCT_LIST = 'PRODUCT_LIST',
  PRODUCT_UPDATE = 'PRODUCT_UPDATE',
  PRODUCT_DELETE = 'PRODUCT_DELETE',

  // Category Permissions
  CATEGORY_CREATE = 'CATEGORY_CREATE',
  CATEGORY_LIST = 'CATEGORY_LIST',
  CATEGORY_UPDATE = 'CATEGORY_UPDATE',
  CATEGORY_DELETE = 'CATEGORY_DELETE',
  
  // Inventory Permissions
  INVENTORY_CREATE = 'INVENTORY_CREATE',
  INVENTORY_LIST = 'INVENTORY_LIST',
  INVENTORY_UPDATE = 'INVENTORY_UPDATE',
  INVENTORY_DELETE = 'INVENTORY_DELETE',
  
  // Sales Permissions
  SALES_CREATE = 'SALES_CREATE',
  SALES_READ = 'SALES_READ',
  SALES_UPDATE = 'SALES_UPDATE',
  SALES_DELETE = 'SALES_DELETE',
  
  // Investment Permissions
  INVESTMENT_CREATE = 'INVESTMENT_CREATE',
  INVESTMENT_LIST = 'INVESTMENT_LIST',
  INVESTMENT_UPDATE = 'INVESTMENT_UPDATE',
  INVESTMENT_DELETE = 'INVESTMENT_DELETE',
  
  // Receipt Permissions
  RECEIPT_CREATE = 'RECEIPT_CREATE',
  RECEIPT_LIST = 'RECEIPT_LIST',
  
  // Expense Permissions
  EXPENSE_CREATE = 'EXPENSE_CREATE',
  EXPENSE_LIST = 'EXPENSE_LIST',
  EXPENSE_UPDATE = 'EXPENSE_UPDATE',
  EXPENSE_DELETE = 'EXPENSE_DELETE',
  EXPENSE_APPROVE = 'EXPENSE_APPROVE',
  
  // Analytics Permissions
  ANALYTICS_VIEW = 'ANALYTICS_VIEW', // Legacy - kept for backward compatibility
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
  USER_CREATE = 'USER_CREATE',
  USER_UPDATE = 'USER_UPDATE',
  USER_DELETE = 'USER_DELETE',
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
    [Permission.SHOP_CREATE]: 'Create Shop',
    [Permission.SHOP_LIST]: 'View Shops',
    [Permission.SHOP_UPDATE]: 'Update Shop',
    [Permission.SHOP_DELETE]: 'Delete Shop',
    [Permission.PRODUCT_CREATE]: 'Create Product',
    [Permission.PRODUCT_LIST]: 'View Products',
    [Permission.PRODUCT_UPDATE]: 'Update Product',
    [Permission.PRODUCT_DELETE]: 'Delete Product',
    [Permission.INVENTORY_CREATE]: 'Create Inventory',
    [Permission.INVENTORY_LIST]: 'View Inventory',
    [Permission.INVENTORY_UPDATE]: 'Update Inventory',
    [Permission.INVENTORY_DELETE]: 'Delete Inventory',
    [Permission.SALES_CREATE]: 'Create Sale',
    [Permission.SALES_READ]: 'View Sales',
    [Permission.SALES_UPDATE]: 'Update Sale',
    [Permission.SALES_DELETE]: 'Delete Sale',
    [Permission.INVESTMENT_CREATE]: 'Create Investment',
    [Permission.INVESTMENT_LIST]: 'View Investments',
    [Permission.INVESTMENT_UPDATE]: 'Update Investment',
    [Permission.INVESTMENT_DELETE]: 'Delete Investment',
    [Permission.RECEIPT_CREATE]: 'Create Receipt',
    [Permission.RECEIPT_LIST]: 'View Receipts',
    [Permission.EXPENSE_CREATE]: 'Create Expense',
    [Permission.EXPENSE_LIST]: 'View Expenses',
    [Permission.EXPENSE_UPDATE]: 'Update Expense',
    [Permission.EXPENSE_DELETE]: 'Delete Expense',
    [Permission.EXPENSE_APPROVE]: 'Approve Expense',
    [Permission.ANALYTICS_VIEW]: 'View Analytics',
    [Permission.ANALYTICS_SALES_VIEW]: 'View Sales Analytics',
    [Permission.ANALYTICS_INVESTMENT_VIEW]: 'View Investment Analytics',
    [Permission.ANALYTICS_MANAGE]: 'Manage Analytics',
    [Permission.FRAUD_DETECTION_VIEW]: 'View Fraud Detection',
    [Permission.FRAUD_VIEW]: 'View Fraud Alerts',
    [Permission.FRAUD_LIST]: 'List Fraud Alerts',
    [Permission.FRAUD_INVESTIGATE]: 'Investigate Fraud',
    [Permission.FRAUD_RESOLVE]: 'Resolve Fraud Alerts',
    [Permission.FRAUD_DETECT]: 'Detect Fraud',
    [Permission.FRAUD_MANAGE]: 'Manage Fraud Detection',
    [Permission.USER_MANAGE]: 'Manage Users',
    [Permission.USER_LIST]: 'View Users',
    [Permission.USER_CREATE]: 'Create User',
    [Permission.USER_UPDATE]: 'Update User',
    [Permission.USER_DELETE]: 'Delete User',
  }
  
  return displayNames[permission] || permission
}
