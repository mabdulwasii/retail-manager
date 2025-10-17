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

export const RoleBasedDashboard: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  const [searchParams] = useSearchParams();
  const viewType = searchParams.get("view");

  // Debug logging
  console.log("RoleBasedDashboard - isAuthenticated:", isAuthenticated);
  console.log("RoleBasedDashboard - user:", user);

  if (viewType) {
    switch (viewType) {
      case "admin":
        return <AdminDashboard />;
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
        break;
    }
  }

  if (!isAuthenticated) {
    return <div>Not authenticated</div>;
  }

  if (!user) {
    // If authenticated but no user profile, show default dashboard
    console.log("Authenticated but no user profile, showing default dashboard");
    return <OwnerManagerDashboard />;
  }

  if (!user.roles || user.roles.length === 0) {
    // If user exists but no roles, show default dashboard
    console.log("User exists but no roles, showing default dashboard");
    return <OwnerManagerDashboard />;
  }

  // Get the highest priority role for dashboard selection
  const roles = user.roles.map((role) => role.replace("ROLE_", ""));

  // Priority order for dashboard selection (highest first)
  const rolePriority = [
    "SYSTEM_ADMIN",
    "SUPER_ADMIN",
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
