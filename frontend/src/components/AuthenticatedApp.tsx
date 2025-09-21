import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { useAuth } from '@/context/KeycloakAuthContext'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import { Layout } from '@/components/layout/Layout'
import { LoginPage } from '@/pages/auth/LoginPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { ShopsPage } from '@/pages/shops/ShopsPage'
import { ShopDetailPage } from '@/pages/shops/ShopDetailPage'
import { CreateShopPage } from '@/pages/shops/CreateShopPage'
import { ProductsPage } from '@/pages/products/ProductsPage'
import { SalesPage } from '@/pages/sales/SalesPage'
import { ReceiptsPage } from '@/pages/receipts/ReceiptsPage'
import { InvestmentsPage } from '@/pages/investments/InvestmentsPage'
import { AnalyticsPage } from '@/pages/analytics/AnalyticsPage'
import { AuditPage } from '@/pages/audit/AuditPage'
import { InventoryPage } from '@/pages/inventory/InventoryPage'
import { NotFoundPage } from '@/pages/NotFoundPage'

export const AuthenticatedApp: React.FC = () => {
  const { isLoading, isAuthenticated } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <LoginPage />
  }

  return (
    <Routes>
      {/* Dashboard Route */}
      <Route path="/dashboard" element={<Layout><DashboardPage /></Layout>} />

      {/* Shop Management */}
      <Route
        path="/shops"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ShopsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/create"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER']}>
              <CreateShopPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/shops/:shopId"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ShopDetailPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Products & Inventory */}
      <Route
        path="/products"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ProductsPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/inventory"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'INVENTORY_MANAGER']}>
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
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER', 'SALES_MANAGER']}>
              <SalesPage />
            </ProtectedRoute>
          </Layout>
        }
      />
      <Route
        path="/receipts"
        element={
          <Layout>
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER']}>
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
            <ProtectedRoute roles={['SHOP_OWNER', 'INVESTOR']}>
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
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'ACCOUNTANT']}>
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
            <ProtectedRoute roles={['SHOP_OWNER', 'SYSTEM_ADMIN', 'AUDITOR']}>
              <AuditPage />
            </ProtectedRoute>
          </Layout>
        }
      />

      {/* Default redirect or 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
