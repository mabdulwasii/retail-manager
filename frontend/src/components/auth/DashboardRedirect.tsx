import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/ManualAuthContext";
import { UserRole } from "@/types/roles";

export function DashboardRedirect() {
  const { user, isAuthenticated, hasRole, isInitialized, login } = useAuth();

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
    if (hasRole(UserRole.SYSTEM_ADMIN) || hasRole(UserRole.SUPER_ADMIN)) {
      return "/dashboard?view=admin";
    } else if (hasRole(UserRole.TENANT_ADMIN)) {
      return "/dashboard?view=multi-shop";
    } else if (hasRole(UserRole.SHOP_OWNER)) {
      return "/dashboard?view=business";
    } else if (hasRole(UserRole.MANAGER) || hasRole(UserRole.SALES_MANAGER)) {
      return "/dashboard?view=operations";
    } else if (hasRole(UserRole.INVESTOR)) {
      return "/dashboard?view=investor";
    } else if (hasRole(UserRole.ACCOUNTANT)) {
      return "/dashboard?view=financial";
    } else if (hasRole(UserRole.CASHIER)) {
      return "/sales";
    } else if (hasRole(UserRole.INVENTORY_MANAGER) || hasRole(UserRole.EMPLOYEE)) {
      return "/inventory";
    }

    return "/dashboard";
  };

  return <Navigate to={getDashboardPath()} replace />;
}
