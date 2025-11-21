import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/ManualAuthContext";

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
    if (hasRole("SYSTEM_ADMIN") || hasRole("SUPER_ADMIN")) {
      return "/dashboard?view=admin";
    } else if (hasRole("TENANT_ADMIN")) {
      return "/dashboard?view=multi-shop";
    } else if (hasRole("SHOP_OWNER")) {
      return "/dashboard?view=business";
    } else if (hasRole("MANAGER") || hasRole("SALES_MANAGER")) {
      return "/dashboard?view=operations";
    } else if (hasRole("INVESTOR")) {
      return "/investments";
    } else if (hasRole("ACCOUNTANT")) {
      return "/dashboard?view=financial";
    } else if (hasRole("CASHIER")) {
      return "/sales";
    } else if (hasRole("INVENTORY_MANAGER") || hasRole("EMPLOYEE")) {
      return "/inventory";
    }

    return "/dashboard";
  };

  return <Navigate to={getDashboardPath()} replace />;
}
