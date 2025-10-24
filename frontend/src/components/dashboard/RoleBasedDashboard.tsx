import { useAuth } from "@/context/ManualAuthContext";
import React from "react";
import { useSearchParams } from "react-router-dom";
import { UserRole } from "@/types/roles";
import { AccountantDashboard } from "./AccountantDashboard";
import { AdminDashboard } from "./AdminDashboard";
import { AuditorDashboard } from "./AuditorDashboard";
import { CashierDashboard } from "./CashierDashboard";
import { CustomerDashboard } from "./CustomerDashboard";
import { EmployeeDashboard } from "./EmployeeDashboard";
import { InvestorDashboard } from "./InvestorDashboard";
import { OwnerManagerDashboard } from "./OwnerManagerDashboard";

// Helper function to check if user has permission for a specific view
const hasPermissionForView = (viewType: string, userRoles: string[]): boolean => {
  const roles = userRoles.map((role) => role.replace("ROLE_", ""));
  
  const viewPermissions: Record<string, UserRole[]> = {
    "admin": [UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "tenant": [UserRole.TENANT_ADMIN, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "multi-shop": [UserRole.TENANT_ADMIN, UserRole.SHOP_OWNER, UserRole.MANAGER, UserRole.SALES_MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "business": [UserRole.SHOP_OWNER, UserRole.MANAGER, UserRole.SALES_MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "operations": [UserRole.SHOP_OWNER, UserRole.MANAGER, UserRole.SALES_MANAGER, UserRole.INVENTORY_MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "financial": [UserRole.ACCOUNTANT, UserRole.SHOP_OWNER, UserRole.MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "cashier": [UserRole.CASHIER, UserRole.MANAGER, UserRole.SALES_MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "pos": [UserRole.CASHIER, UserRole.MANAGER, UserRole.SALES_MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "investor": [UserRole.INVESTOR, UserRole.SHOP_OWNER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "investments": [UserRole.INVESTOR, UserRole.SHOP_OWNER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "employee": [UserRole.EMPLOYEE, UserRole.INVENTORY_MANAGER, UserRole.MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "inventory": [UserRole.INVENTORY_MANAGER, UserRole.EMPLOYEE, UserRole.MANAGER, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "audit": [UserRole.AUDITOR, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "compliance": [UserRole.AUDITOR, UserRole.SYSTEM_ADMIN, UserRole.SUPER_ADMIN],
    "customer": [UserRole.CUSTOMER]
  };

  const allowedRoles = viewPermissions[viewType];
  if (!allowedRoles) return false;
  
  return roles.some(role => allowedRoles.includes(role as UserRole));
};

export const RoleBasedDashboard: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
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
    return <OwnerManagerDashboard />;
  }

  if (!user.roles || user.roles.length === 0) {
    console.log("User exists but no roles, showing default dashboard");
    return <OwnerManagerDashboard />;
  }

  if (viewType) {
    if (!hasPermissionForView(viewType, user.roles)) {
      console.warn(`User attempted to access unauthorized view: ${viewType}`);
    } else {
      switch (viewType) {
        case "admin":
          return <AdminDashboard />;
        case "tenant":
        case "multi-shop":
        case "business":
        case "operations":
          return <OwnerManagerDashboard />;
        case "financial":
          return <AccountantDashboard />;
        case "cashier":
        case "pos":
          return <CashierDashboard />;
        case "investor":
        case "investments":
          return <InvestorDashboard />;
        case "employee":
        case "inventory":
          return <EmployeeDashboard />;
        case "audit":
        case "compliance":
          return <AuditorDashboard />;
        case "customer":
          return <CustomerDashboard />;
        default:
          console.warn(`Unknown view type: ${viewType}`);
          break;
      }
    }
  }

  // Get the highest priority role for dashboard selection
  const roles = user.roles.map((role) => role.replace("ROLE_", "") as UserRole);

  // Priority order for dashboard selection (highest first)
  const rolePriority: UserRole[] = [
    UserRole.SYSTEM_ADMIN,
    UserRole.SUPER_ADMIN,
    UserRole.TENANT_ADMIN,
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.SALES_MANAGER,
    UserRole.INVENTORY_MANAGER,
    UserRole.ACCOUNTANT,
    UserRole.AUDITOR,
    UserRole.INVESTOR,
    UserRole.CASHIER,
    UserRole.EMPLOYEE,
    UserRole.CUSTOMER,
  ];

  const primaryRole =
    rolePriority.find((role) => roles.includes(role)) || UserRole.CUSTOMER;

  switch (primaryRole) {
    case UserRole.SYSTEM_ADMIN:
    case UserRole.SUPER_ADMIN:
      return <AdminDashboard />;

    case UserRole.TENANT_ADMIN:
    case UserRole.SHOP_OWNER:
    case UserRole.MANAGER:
    case UserRole.SALES_MANAGER:
      return <OwnerManagerDashboard />;

    case UserRole.INVENTORY_MANAGER:
      return <EmployeeDashboard />;

    case UserRole.CASHIER:
      return <CashierDashboard />;

    case UserRole.INVESTOR:
      return <InvestorDashboard />;

    case UserRole.ACCOUNTANT:
      return <AccountantDashboard />;

    case UserRole.AUDITOR:
      return <AuditorDashboard />;

    case UserRole.EMPLOYEE:
      return <EmployeeDashboard />;

    case UserRole.CUSTOMER:
    default:
      return <CustomerDashboard />;
  }
};
