import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Layout } from "@/components/layout/Layout";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { useAuth } from "@/context/ManualAuthContext";
import { AnalyticsPage } from "@/pages/analytics/AnalyticsPage";
import { AuditPage } from "@/pages/audit/AuditPage";
import { DashboardPage } from "@/pages/dashboard/DashboardPage";
import { InventoryPage } from "@/pages/inventory/InventoryPage";
import { InvestmentsPage } from "@/pages/investments/InvestmentsPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { ProductsPage } from "@/pages/products/ProductsPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { ReceiptsPage } from "@/pages/receipts/ReceiptsPage";
import { SalesPage } from "@/pages/sales/SalesPage";
import { CreateShopPage } from "@/pages/shops/CreateShopPage";
import { EditShopPage } from "@/pages/shops/EditShopPage";
import { ShopDetailPage } from "@/pages/shops/ShopDetailPage";
import { ShopSettingsPage } from "@/pages/shops/ShopSettingsPage";
import { ShopsPage } from "@/pages/shops/ShopsPage";
import React from "react";
import { Route, Routes } from "react-router-dom";

export const AuthenticatedApp: React.FC = () => {
  const { isInitialized, isAuthenticated, user, login } = useAuth();

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
        <p className="ml-2">Initializing...</p>
      </div>
    );
  }

  if (!isAuthenticated && !user) {
    login();
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
        <p className="ml-2">Redirecting to login...</p>
      </div>
    );
  }

  return (
    <Routes>
      {/* Dashboard Routes */}
      <Route
        path="/dashboard"
        element={
          <Layout>
            <DashboardPage />
          </Layout>
        }
      />
      <Route
        path="/profile"
        element={
          <Layout>
            <ProfilePage />
          </Layout>
        }
      />

      {/* Shop Management */}
      <Route
        path="/shops"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER"]}>
              <ShopsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/create"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER"]}>
              <CreateShopPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER"]}>
              <ShopDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId/edit"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER"]}>
              <EditShopPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId/settings"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "MANAGER"]}>
              <ShopSettingsPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Products & Inventory */}
      <Route
        path="/products"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "MANAGER"]}>
              <ProductsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory"
        element={
          <Layout>
            <ProtectedRoute
              roles={["SHOP_OWNER", "MANAGER", "INVENTORY_MANAGER"]}
            >
              <InventoryPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Sales & Receipts */}
      <Route
        path="/sales"
        element={
          <Layout>
            <ProtectedRoute
              roles={["SHOP_OWNER", "MANAGER", "CASHIER", "SALES_MANAGER"]}
            >
              <SalesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/receipts"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "MANAGER", "CASHIER"]}>
              <ReceiptsPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Investments */}
      <Route
        path="/investments"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "INVESTOR"]}>
              <InvestmentsPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Analytics */}
      <Route
        path="/analytics"
        element={
          <Layout>
            <ProtectedRoute
              roles={["SHOP_OWNER", "MANAGER", "ACCOUNTANT"]}
            >
              <AnalyticsPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Audit Logs */}
      <Route
        path="/audit"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "SYSTEM_ADMIN", "AUDITOR"]}>
              <AuditPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Default redirect or 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
