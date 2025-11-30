import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/ManualAuthContext";
import { Permission } from "@/types/permissions";

export function DashboardRedirect() {
  const { user, isAuthenticated, hasAnyPermission, isInitialized, login } = useAuth();

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
        <p className="ml-2">Redirecting to dashboard...</p>
      </div>
    );
  }

  if (!isAuthenticated && !user) {
    login();
  }

  const getDashboardPath = () => {
    // System/Tenant Admin - Full admin dashboard
    if (hasAnyPermission([Permission.SYSTEM_ADMIN, Permission.TENANT_MANAGE, Permission.TENANT_LIST])) {
      return "/dashboard";
    }
    
    if (hasAnyPermission([Permission.SHOP_MANAGE, Permission.SHOP_LIST_ALL])) {
      return "/dashboard";
    }

    if (hasAnyPermission([Permission.INVESTMENT_VIEW, Permission.INVESTMENT_LIST])) {
      return "/dashboard";
    }
    
    if (hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW, Permission.EXPENSE_SUMMARY])) {
      return "/dashboard";
    }

    if (hasAnyPermission([Permission.SALES_CREATE, Permission.SALES_LIST])) {
      return "/sales";
    }
    
    if (hasAnyPermission([Permission.INVENTORY_UPDATE, Permission.INVENTORY_LIST])) {
      return "/inventory";
    }

    // Default dashboard for authenticated users
    return "/dashboard";
  };

  return <Navigate to={getDashboardPath()} replace />;
}
