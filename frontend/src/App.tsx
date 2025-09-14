import { Routes, Route } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
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

function App() {
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
    <Layout>
      <Routes>
        <Route path="/" element={<DashboardPage />} />

        {/* Shop Management */}
        <Route
          path="/shops"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ShopsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/shops/create"
          element={
            <ProtectedRoute roles={['SHOP_OWNER']}>
              <CreateShopPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/shops/:shopId"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ShopDetailPage />
            </ProtectedRoute>
          }
        />

        {/* Products & Inventory */}
        <Route
          path="/products"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <ProductsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/inventory"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <InventoryPage />
            </ProtectedRoute>
          }
        />

        {/* Sales & Receipts */}
        <Route
          path="/sales"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER']}>
              <SalesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/receipts"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER']}>
              <ReceiptsPage />
            </ProtectedRoute>
          }
        />

        {/* Investments */}
        <Route
          path="/investments"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'INVESTOR']}>
              <InvestmentsPage />
            </ProtectedRoute>
          }
        />

        {/* Analytics */}
        <Route
          path="/analytics"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SHOP_MANAGER']}>
              <AnalyticsPage />
            </ProtectedRoute>
          }
        />

        {/* Audit Logs */}
        <Route
          path="/audit"
          element={
            <ProtectedRoute roles={['SHOP_OWNER', 'SYSTEM_ADMIN']}>
              <AuditPage />
            </ProtectedRoute>
          }
        />

        {/* 404 Page */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  )
}

export default App