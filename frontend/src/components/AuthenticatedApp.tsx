import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { Layout } from "@/components/layout/Layout";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { useAuth } from "@/context/UnifiedAuthContext";
import { Permission } from "@/types/permissions";
import { AnalyticsPage } from "@/pages/analytics/AnalyticsPage";
import { AuditPage } from "@/pages/audit/AuditPage";
import { DashboardPage } from "@/pages/dashboard/DashboardPage";
import { 
  InventoryPage,
  InventoryDetailPage,
  InventoryEditPage,
  CreateInventoryPage,
  LowStockReportPage,
  ExpiringItemsPage
} from "@/pages/inventory";
import { InvestmentsPage } from "@/pages/investments/InvestmentsPage";
import { InvestmentDetailPage } from "@/pages/investments/InvestmentDetailPage";
import { CreateInvestmentPage } from "@/pages/investments/CreateInvestmentPage";
import { DistributionListPage } from "@/pages/investments/DistributionListPage";
import { InvestmentRoundsPage } from "@/pages/investments/InvestmentRoundsPage";
import { InvestmentRoundDetailPage } from "@/pages/investments/InvestmentRoundDetailPage";
import { CreateInvestmentRoundPage } from "@/pages/investments/CreateInvestmentRoundPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { ProductsPage } from "@/pages/products/ProductsPage";
import { CreateProductPage } from "@/pages/products/CreateProductPage";
import { EditProductPage } from "@/pages/products/EditProductPage";
import { ProductDetailPage } from "@/pages/products/ProductDetailPage";
import { CategoriesPage } from "@/pages/categories";
import { ProfilePage } from "@/pages/ProfilePage";
import { SystemSettingsPage } from "@/pages/settings/SystemSettingsPage";
import { ReceiptsPage } from "@/pages/receipts/ReceiptsPage";
import { RolesPage } from "@/pages/admin/RolesPage";
import { CreateRolePage } from "@/pages/admin/CreateRolePage";
import { EditRolePage } from "@/pages/admin/EditRolePage";
import { RoleDetailPage } from "@/pages/admin/RoleDetailPage";
import { PermissionsMatrixPage } from "@/pages/admin/PermissionsMatrixPage";
import { UsersPage, CreateUserPage, EditUserPage } from "@/pages/users";
import { UnauthorizedPage } from "@/pages/UnauthorizedPage";
import { SalesPage, TransactionDetailPage } from "@/pages/sales";
import { POSPage } from "@/pages/pos/POSPage";
import { CreateShopPage } from "@/pages/shops/CreateShopPage";
import { EditShopPage } from "@/pages/shops/EditShopPage";
import { ShopDetailPage } from "@/pages/shops/ShopDetailPage";
import { ShopSettingsPage } from "@/pages/shops/ShopSettingsPage";
import { ShopsPage } from "@/pages/shops/ShopsPage";
import {
  CloudTenantsPage,
  CloudTenantDetailPage,
  CrossShopAnalyticsPage,
  SubscriptionsPage,
  ApiKeysPage,
  ShopManagementPage,
  TenantSettingsPage,
  AuditLogsPage
} from "@/pages/cloud";
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

      {/* System Settings (Embedded Mode Only) */}
      <Route
        path="/settings/system"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SYSTEM_SETTING_VIEW, Permission.SYSTEM_SETTING_UPDATE, Permission.SYSTEM_SETTING_MANAGE]}>
              <SystemSettingsPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Shop Management */}
      <Route
        path="/shops"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SHOP_LIST, Permission.SHOP_LIST_ALL]}>
              <ShopsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SHOP_CREATE]}>
              <CreateShopPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SHOP_READ, Permission.SHOP_LIST]}>
              <ShopDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId/edit"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SHOP_UPDATE]}>
              <EditShopPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId/settings"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SHOP_MANAGE, Permission.SHOP_UPDATE]}>
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
            <ProtectedRoute permissions={[Permission.PRODUCT_LIST, Permission.PRODUCT_READ]}>
              <ProductsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.PRODUCT_CREATE]}>
              <CreateProductPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/:productId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.PRODUCT_READ, Permission.PRODUCT_LIST]}>
              <ProductDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/products/:productId/edit"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.PRODUCT_UPDATE]}>
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
            <ProtectedRoute permissions={[Permission.CATEGORY_LIST, Permission.CATEGORY_READ]}>
              <CategoriesPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      <Route
        path="/inventory"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_LIST, Permission.INVENTORY_READ]}>
              <InventoryPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_CREATE]}>
              <CreateInventoryPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/low-stock"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_LIST, Permission.INVENTORY_READ]}>
              <LowStockReportPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/expiring"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_LIST, Permission.INVENTORY_READ]}>
              <ExpiringItemsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/:inventoryId/edit"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_UPDATE]}>
              <InventoryEditPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory/:inventoryId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVENTORY_READ, Permission.INVENTORY_LIST]}>
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
            <ProtectedRoute permissions={[Permission.SALES_CREATE]}>
              <POSPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/sales"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SALES_READ, Permission.SALES_LIST]}>
              <SalesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/sales/:transactionId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.SALES_READ, Permission.SALES_LIST]}>
              <TransactionDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/receipts"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.RECEIPT_LIST, Permission.RECEIPT_READ]}>
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
            <ProtectedRoute permissions={[Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW]}>
              <InvestmentsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_CREATE]}>
              <CreateInvestmentPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/distributions"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW]}>
              <DistributionListPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/rounds"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW]}>
              <InvestmentRoundsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/rounds/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_CREATE]}>
              <CreateInvestmentRoundPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/rounds/:id"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW]}>
              <InvestmentRoundDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/investments/:id"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW]}>
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
            <ProtectedRoute permissions={[Permission.ANALYTICS_VIEW, Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW]}>
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
            <ProtectedRoute permissions={[Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_LIST, Permission.AUDIT_LOG_VIEW_SHOP, Permission.AUDIT_LOG_VIEW_TENANT]}>
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
            <ProtectedRoute permissions={[Permission.ROLE_LIST, Permission.ROLE_READ]}>
              <RolesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.ROLE_CREATE]}>
              <CreateRolePage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/:roleId/edit"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.ROLE_UPDATE]}>
              <EditRolePage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/roles/:roleId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.ROLE_READ, Permission.ROLE_LIST]}>
              <RoleDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/admin/permissions"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.PERMISSION_LIST]}>
              <PermissionsMatrixPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Users */}
      <Route
        path="/users"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.USER_LIST, Permission.USER_READ]}>
              <UsersPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/users/create"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.USER_CREATE]}>
              <CreateUserPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/users/edit/:userId"
        element={
          <Layout>
            <ProtectedRoute permissions={[Permission.USER_UPDATE]}>
              <EditUserPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Cloud Portal Routes (Phase 1: Empty states) */}
      <Route
        path="/cloud/tenants"
        element={
          <Layout>
            <CloudTenantsPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/tenants/:id"
        element={
          <Layout>
            <CloudTenantDetailPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/analytics"
        element={
          <Layout>
            <CrossShopAnalyticsPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/subscriptions"
        element={
          <Layout>
            <SubscriptionsPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/api-keys"
        element={
          <Layout>
            <ApiKeysPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/shops"
        element={
          <Layout>
            <ShopManagementPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/settings"
        element={
          <Layout>
            <TenantSettingsPage />
          </Layout>
        }
      />
      <Route
        path="/cloud/audit-logs"
        element={
          <Layout>
            <AuditLogsPage />
          </Layout>
        }
      />

      {/* Unauthorized */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />

      {/* Default redirect or 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
