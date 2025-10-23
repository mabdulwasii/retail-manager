import { useAuth } from "@/context/ManualAuthContext";
import React from "react";
import { useSearchParams } from "react-router-dom";
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
  
  const viewPermissions: Record<string, string[]> = {
    "admin": ["SYSTEM_ADMIN", "SUPER_ADMIN"],
    "tenant": ["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "multi-shop": ["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "SALES_MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "business": ["SHOP_OWNER", "MANAGER", "SALES_MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "operations": ["SHOP_OWNER", "MANAGER", "SALES_MANAGER", "INVENTORY_MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "financial": ["ACCOUNTANT", "SHOP_OWNER", "MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "cashier": ["CASHIER", "MANAGER", "SALES_MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "pos": ["CASHIER", "MANAGER", "SALES_MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "investor": ["INVESTOR", "SHOP_OWNER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "investments": ["INVESTOR", "SHOP_OWNER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "employee": ["EMPLOYEE", "INVENTORY_MANAGER", "MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "inventory": ["INVENTORY_MANAGER", "EMPLOYEE", "MANAGER", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "audit": ["AUDITOR", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "compliance": ["AUDITOR", "SYSTEM_ADMIN", "SUPER_ADMIN"],
    "customer": ["CUSTOMER"]
  };

  const allowedRoles = viewPermissions[viewType];
  if (!allowedRoles) return false;
  
  return roles.some(role => allowedRoles.includes(role));
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
  const roles = user.roles.map((role) => role.replace("ROLE_", ""));

  // Priority order for dashboard selection (highest first)
  const rolePriority = [
    "SYSTEM_ADMIN",
    "SUPER_ADMIN",
    "TENANT_ADMIN",
    "SHOP_OWNER",
    "MANAGER",
    "SALES_MANAGER",
    "INVENTORY_MANAGER",
    "ACCOUNTANT",
    "AUDITOR",
    "INVESTOR",
    "CASHIER",
    "EMPLOYEE",
    "CUSTOMER",
  ];

  const primaryRole =
    rolePriority.find((role) => roles.includes(role)) || "CUSTOMER";

  switch (primaryRole) {
    case "SYSTEM_ADMIN":
    case "SUPER_ADMIN":
      return <AdminDashboard />;

    case "TENANT_ADMIN":
    case "SHOP_OWNER":
    case "MANAGER":
    case "SALES_MANAGER":
      return <OwnerManagerDashboard />;

    case "INVENTORY_MANAGER":
      return <EmployeeDashboard />;

    case "CASHIER":
      return <CashierDashboard />;

    case "INVESTOR":
      return <InvestorDashboard />;

    case "ACCOUNTANT":
      return <AccountantDashboard />;

    case "AUDITOR":
      return <AuditorDashboard />;

    case "EMPLOYEE":
      return <EmployeeDashboard />;

    case "CUSTOMER":
    default:
      return <CustomerDashboard />;
  }
};
