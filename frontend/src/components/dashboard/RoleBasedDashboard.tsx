import { useAuth } from "@/context/UnifiedAuthContext";
import React from "react";
import { useSearchParams } from "react-router-dom";
import { Permission } from "@/types/permissions";
import { AccountantDashboard } from "./AccountantDashboard";
import { AdminDashboard } from "./AdminDashboard";
import { AuditorDashboard } from "./AuditorDashboard";
import { CashierDashboard } from "./CashierDashboard";
import { CustomerDashboard } from "./CustomerDashboard";
import { EmployeeDashboard } from "./EmployeeDashboard";
import { InvestorDashboard } from "./InvestorDashboard";
import { OwnerManagerDashboard } from "./OwnerManagerDashboard";

// Helper function to check if user has permission for a specific view
const hasPermissionForView = (
  viewType: string,
  hasAnyPermission: (permissions: Permission[]) => boolean
): boolean => {
  const viewPermissions: Record<string, Permission[]> = {
    "admin": [Permission.SYSTEM_ADMIN, Permission.TENANT_MANAGE],
    "tenant": [Permission.TENANT_MANAGE, Permission.TENANT_LIST],
    "multi-shop": [Permission.SHOP_MANAGE, Permission.SHOP_LIST],
    "business": [Permission.SHOP_MANAGE, Permission.ANALYTICS_VIEW],
    "operations": [Permission.SHOP_MANAGE, Permission.INVENTORY_UPDATE, Permission.SALES_CREATE],
    "financial": [Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW, Permission.EXPENSE_LIST],
    "cashier": [Permission.SALES_CREATE, Permission.RECEIPT_CREATE],
    "pos": [Permission.SALES_CREATE, Permission.RECEIPT_CREATE],
    "investor": [Permission.INVESTMENT_VIEW, Permission.INVESTMENT_LIST],
    "investments": [Permission.INVESTMENT_VIEW, Permission.INVESTMENT_LIST],
    "employee": [Permission.INVENTORY_LIST, Permission.PRODUCT_LIST],
    "inventory": [Permission.INVENTORY_LIST, Permission.INVENTORY_UPDATE],
    "audit": [Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_LIST, Permission.AUDIT_LOG_VIEW_TENANT],
    "compliance": [Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_LIST],
    "customer": [] // No specific permissions required for customer view
  };

  const requiredPermissions = viewPermissions[viewType];
  if (!requiredPermissions) return false;
  if (requiredPermissions.length === 0) return true; // Customer view
  
  return hasAnyPermission(requiredPermissions);
};

export const RoleBasedDashboard: React.FC = () => {
  const { user, isAuthenticated, hasAnyPermission } = useAuth();
  const [searchParams] = useSearchParams();
  const viewType = searchParams.get("view");

  // Debug logging
  console.log("RoleBasedDashboard - isAuthenticated:", isAuthenticated);
  console.log("RoleBasedDashboard - user:", user);

  if (!isAuthenticated) {
    return <div>Not authenticated</div>;
  }

  if (!user) {
    console.log("Authenticated but no user profile, showing default dashboard");
    return <div>No user profile found</div>;
  }

  // if (viewType) {
  //   if (!hasPermissionForView(viewType, hasAnyPermission)) {
  //     console.warn(`User attempted to access unauthorized view: ${viewType}`);
  //   } else {
  //     switch (viewType) {
  //       case "admin":
  //         return <AdminDashboard />;
  //       case "tenant":
  //       case "multi-shop":
  //       case "business":
  //       case "operations":
  //         return <OwnerManagerDashboard />;
  //       case "financial":
  //         return <AccountantDashboard />;
  //       case "cashier":
  //       case "pos":
  //         return <CashierDashboard />;
  //       case "investor":
  //       case "investments":
  //         return <InvestorDashboard />;
  //       case "employee":
  //       case "inventory":
  //         return <EmployeeDashboard />;
  //       case "audit":
  //       case "compliance":
  //         return <AuditorDashboard />;
  //       case "customer":
  //         return <CustomerDashboard />;
  //       default:
  //         console.warn(`Unknown view type: ${viewType}`);
  //         break;
  //     }
  //   }
  // }

  // Determine which dashboard to show based on permissions
  // Priority order: most privileged to least privileged
  
  // System/Tenant Admin Dashboard
  if (hasAnyPermission([Permission.SYSTEM_ADMIN, Permission.TENANT_MANAGE, Permission.TENANT_LIST])) {
    return <AdminDashboard />;
  }

  // Shop Owner/Manager Dashboard
  if (hasAnyPermission([Permission.SHOP_MANAGE, Permission.SHOP_LIST, Permission.ANALYTICS_VIEW])) {
    return <OwnerManagerDashboard />;
  }

  // Financial/Accountant Dashboard
  if (hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW, Permission.EXPENSE_SUMMARY])) {
    return <AccountantDashboard />;
  }
  
  // Auditor Dashboard
  if (hasAnyPermission([Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_VIEW_TENANT, Permission.AUDIT_LOG_VIEW_SHOP])) {
    return <AuditorDashboard />;
  }
  
  // Investor Dashboard
  if (hasAnyPermission([Permission.INVESTMENT_VIEW, Permission.INVESTMENT_LIST])) {
    return <InvestorDashboard />;
  }
  
  // Inventory/Employee Dashboard
  if (hasAnyPermission([Permission.INVENTORY_LIST, Permission.INVENTORY_UPDATE, Permission.PRODUCT_LIST])) {
    return <EmployeeDashboard />;
  }
  
  // Cashier/POS Dashboard
  if (hasAnyPermission([Permission.SALES_CREATE, Permission.RECEIPT_CREATE])) {
    return <CashierDashboard />;
  }
  
  // Default: Customer Dashboard (or fallback for users with minimal permissions)
  return <CustomerDashboard />;
}
