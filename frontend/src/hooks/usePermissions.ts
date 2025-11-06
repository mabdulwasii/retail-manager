import { useAuth } from '@/context/ManualAuthContext'

/**
 * Custom hook for checking permissions in components
 * Provides easy-to-use permission checking functions
 */
export const usePermissions = () => {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = useAuth()

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    
    // Specific permission checks for common actions
    canCreateShop: () => hasPermission('SHOP_CREATE'),
    canViewShops: () => hasPermission('SHOP_LIST'),
    canEditShop: () => hasPermission('SHOP_UPDATE'),
    canDeleteShop: () => hasPermission('SHOP_DELETE'),
    
    canCreateProduct: () => hasPermission('PRODUCT_CREATE'),
    canViewProducts: () => hasPermission('PRODUCT_LIST'),
    canEditProduct: () => hasPermission('PRODUCT_UPDATE'),
    canDeleteProduct: () => hasPermission('PRODUCT_DELETE'),
    
    canCreateSale: () => hasPermission('SALES_CREATE'),
    canViewSales: () => hasPermission('SALES_READ'),
    canEditSale: () => hasPermission('SALES_UPDATE'),
    canDeleteSale: () => hasPermission('SALES_DELETE'),
    
    canViewInventory: () => hasPermission('INVENTORY_LIST'),
    canUpdateInventory: () => hasPermission('INVENTORY_UPDATE'),
    
    canCreateInvestment: () => hasPermission('INVESTMENT_CREATE'),
    canViewInvestments: () => hasPermission('INVESTMENT_LIST'),
    canEditInvestment: () => hasPermission('INVESTMENT_UPDATE'),
    canDeleteInvestment: () => hasPermission('INVESTMENT_DELETE'),
    
    canViewReceipts: () => hasPermission('RECEIPT_LIST'),
    canCreateReceipt: () => hasPermission('RECEIPT_CREATE'),
    
    canViewExpenses: () => hasPermission('EXPENSE_LIST'),
    canApproveExpenses: () => hasPermission('EXPENSE_APPROVE'),
    
    canViewAnalytics: () => hasPermission('ANALYTICS_VIEW'),
    canViewFraudDetection: () => hasPermission('FRAUD_DETECTION_VIEW'),
    
    canManageUsers: () => hasPermission('USER_MANAGE'),
    canViewUsers: () => hasPermission('USER_LIST'),
  }
}
