import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Layout } from "@/components/layout/Layout";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { useAuth } from "@/context/ManualAuthContext";
import { AnalyticsPage } from "@/pages/analytics/AnalyticsPage";
import { AuditPage } from "@/pages/audit/AuditPage";
import { DashboardPage } from "@/pages/dashboard/DashboardPage";
import { 
  InventoryPage,
  InventoryDetailPage,
  LowStockReportPage,
  ExpiringItemsPage
} from "@/pages/inventory";
import { InvestmentsPage } from "@/pages/investments/InvestmentsPage";
import { InvestmentDetailPage } from "@/pages/investments/InvestmentDetailPage";
import { CreateInvestmentPage } from "@/pages/investments/CreateInvestmentPage";
import { DistributionListPage } from "@/pages/investments/DistributionListPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { ProductsPage } from "@/pages/products/ProductsPage";
import { CreateProductPage } from "@/pages/products/CreateProductPage";
import { EditProductPage } from "@/pages/products/EditProductPage";
import { ProductDetailPage } from "@/pages/products/ProductDetailPage";
import { CategoriesPage } from "@/pages/categories";
import { ProfilePage } from "@/pages/ProfilePage";
import { ReceiptsPage } from "@/pages/receipts/ReceiptsPage";
import { RolesPage } from "@/pages/admin/RolesPage";
import { CreateRolePage } from "@/pages/admin/CreateRolePage";
import { EditRolePage } from "@/pages/admin/EditRolePage";
import { PermissionsMatrixPage } from "@/pages/admin/PermissionsMatrixPage";
import { SalesPage, TransactionDetailPage } from "@/pages/sales";
import { POSPage } from "@/pages/pos/POSPage";
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
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE"]}>
              <ProductsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/create"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER"]}>
              <CreateProductPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/:productId"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "INVENTORY_MANAGER"]}>
              <ProductDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/:productId/edit"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER"]}>
              <EditProductPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Categories Management */}
      <Route
        path="/categories"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE"]}>
              <CategoriesPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      <Route
        path="/inventory"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "INVENTORY_MANAGER"]}
            >
              <InventoryPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/low-stock"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "INVENTORY_MANAGER"]}
            >
              <LowStockReportPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/expiring"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "INVENTORY_MANAGER"]}
            >
              <ExpiringItemsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/:inventoryId"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "INVENTORY_MANAGER"]}
            >
              <InventoryDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Point of Sale & Sales */}
      <Route
        path="/pos"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "CASHIER"]}>
              <POSPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/sales"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "CASHIER", "SALES_MANAGER"]}
            >
              <SalesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/sales/:transactionId"
        element={
          <Layout>
            <ProtectedRoute
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "CASHIER", "SALES_MANAGER"]}
            >
              <TransactionDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/receipts"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "EMPLOYEE", "CASHIER"]}>
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
            <ProtectedRoute roles={["SHOP_OWNER", "INVESTOR", "MANAGER", "TENANT_ADMIN"]}>
              <InvestmentsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/create"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "INVESTOR", "TENANT_ADMIN"]}>
              <CreateInvestmentPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/distributions"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "MANAGER", "TENANT_ADMIN", "INVESTOR"]}>
              <DistributionListPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/:id"
        element={
          <Layout>
            <ProtectedRoute roles={["SHOP_OWNER", "INVESTOR", "MANAGER", "TENANT_ADMIN"]}>
              <InvestmentDetailPage />
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
              roles={["TENANT_ADMIN", "SHOP_OWNER", "MANAGER", "ACCOUNTANT"]}
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
            <ProtectedRoute roles={["TENANT_ADMIN", "SHOP_OWNER", "SYSTEM_ADMIN", "AUDITOR"]}>
              <AuditPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Admin - Role & Permission Management */}
      <Route
        path="/admin/roles"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"]}>
              <RolesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/create"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"]}>
              <CreateRolePage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/:roleId/edit"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"]}>
              <EditRolePage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/:roleId"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"]}>
              <EditRolePage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/permissions"
        element={
          <Layout>
            <ProtectedRoute roles={["TENANT_ADMIN", "SYSTEM_ADMIN", "SUPER_ADMIN"]}>
              <PermissionsMatrixPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Default redirect or 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
