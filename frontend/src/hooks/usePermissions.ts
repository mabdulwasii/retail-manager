import { useAuth } from '@/context/ManualAuthContext'
import { Permission } from '@/types/permissions'

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
    canCreateShop: () => hasPermission(Permission.SHOP_CREATE),
    canViewShops: () => hasPermission(Permission.SHOP_LIST),
    canEditShop: () => hasPermission(Permission.SHOP_UPDATE),
    canDeleteShop: () => hasPermission(Permission.SHOP_DELETE),
    
    canCreateProduct: () => hasPermission(Permission.PRODUCT_CREATE),
    canViewProducts: () => hasPermission(Permission.PRODUCT_LIST),
    canEditProduct: () => hasPermission(Permission.PRODUCT_UPDATE),
    canDeleteProduct: () => hasPermission(Permission.PRODUCT_DELETE),

    canCreateCategory: () => hasPermission(Permission.CATEGORY_CREATE),
    canViewCategories: () => hasPermission(Permission.CATEGORY_LIST),
    canEditCategory: () => hasPermission(Permission.CATEGORY_UPDATE),
    canDeleteCategory: () => hasPermission(Permission.CATEGORY_DELETE),
    
    canCreateSale: () => hasPermission(Permission.SALES_CREATE),
    canViewSales: () => hasPermission(Permission.SALES_READ),
    canEditSale: () => hasPermission(Permission.SALES_UPDATE),
    canDeleteSale: () => hasPermission(Permission.SALES_DELETE),
    
    canViewInventory: () => hasPermission(Permission.INVENTORY_LIST),
    canUpdateInventory: () => hasPermission(Permission.INVENTORY_UPDATE),
    
    canCreateInvestment: () => hasPermission(Permission.INVESTMENT_CREATE),
    canViewInvestments: () => hasPermission(Permission.INVESTMENT_LIST),
    canEditInvestment: () => hasPermission(Permission.INVESTMENT_UPDATE),
    canDeleteInvestment: () => hasPermission(Permission.INVESTMENT_DELETE),
    
    canViewReceipts: () => hasPermission(Permission.RECEIPT_LIST),
    canCreateReceipt: () => hasPermission(Permission.RECEIPT_CREATE),
    
    canViewExpenses: () => hasPermission(Permission.EXPENSE_LIST),
    canApproveExpenses: () => hasPermission(Permission.EXPENSE_APPROVE),
    
    canViewAnalytics: () => hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW, Permission.ANALYTICS_VIEW]),
    canViewFraudDetection: () => hasAnyPermission([Permission.FRAUD_VIEW, Permission.FRAUD_LIST, Permission.FRAUD_DETECTION_VIEW]),
    
    canManageUsers: () => hasPermission(Permission.USER_MANAGE),
    canViewUsers: () => hasPermission(Permission.USER_LIST),
    
    canViewRoles: () => hasPermission(Permission.ROLE_LIST),
    canManageRoles: () => hasAnyPermission([Permission.ROLE_CREATE, Permission.ROLE_UPDATE, Permission.ROLE_DELETE, Permission.ROLE_ASSIGN]),
    
    canViewAuditLogs: () => hasAnyPermission([Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_LIST, Permission.AUDIT_LOG_VIEW_SHOP, Permission.AUDIT_LOG_VIEW_TENANT]),
  }
}
