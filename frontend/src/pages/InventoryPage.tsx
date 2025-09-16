import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { InventoryList } from '@/components/inventory/InventoryList'
import { InventoryForm } from '@/components/inventory/InventoryForm'
import { InventoryFilters } from '@/components/inventory/InventoryFilters'
import { InventorySummaryCards } from '@/components/inventory/InventorySummaryCards'
import { StockAdjustmentModal } from '@/components/inventory/StockAdjustmentModal'
import { useInventory, InventoryFilter, InventoryItem } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
import {
  PlusIcon,
  PackageIcon,
  AlertTriangleIcon,
  TrendingDownIcon,
  ClockIcon,
  DownloadIcon,
  RefreshCwIcon,
  BarChart3Icon,
  XCircleIcon
} from 'lucide-react'

export const InventoryPage: React.FC = () => {
  const {
    inventory,
    summary,
    isLoading,
    error,
    canManageInventory,
    canViewInventory,
    canAdjustStock,
    fetchInventory,
    fetchInventorySummary,
    exportInventory,
    getLowStockAlerts,
    getExpiringItems,
    clearError
  } = useInventory()

  const { formatCurrency } = useCurrency()
  const [activeTab, setActiveTab] = useState<'overview' | 'list' | 'alerts' | 'analytics'>('overview')
  const [showInventoryForm, setShowInventoryForm] = useState(false)
  const [showStockAdjustment, setShowStockAdjustment] = useState(false)
  const [selectedInventoryId, setSelectedInventoryId] = useState<string | null>(null)
  const [filter, setFilter] = useState<InventoryFilter>({})
  const [lowStockItems, setLowStockItems] = useState<InventoryItem[]>([])
  const [expiringItems, setExpiringItems] = useState<InventoryItem[]>([])

  // Mock shop ID - in real app this would come from context/props
  const shopId = '550e8400-e29b-41d4-a716-446655440001'

  useEffect(() => {
    if (canViewInventory) {
      fetchInventory(shopId)
      fetchInventorySummary(shopId)
    }
  }, [canViewInventory, fetchInventory, fetchInventorySummary, shopId])

  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => {
        clearError()
      }, 5000)
      return () => clearTimeout(timer)
    }
  }, [error, clearError])

  const handleFilterChange = (newFilter: InventoryFilter) => {
    setFilter(newFilter)
    fetchInventory(shopId, newFilter)
  }

  const handleExport = async (format: 'csv' | 'excel' = 'csv') => {
    const url = await exportInventory(shopId, format)
    if (url) {
      const link = document.createElement('a')
      link.href = url
      link.download = `inventory-${new Date().toISOString().split('T')[0]}.${format === 'csv' ? 'csv' : 'xlsx'}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }
  }

  const handleRefresh = () => {
    fetchInventory(shopId, filter)
    fetchInventorySummary(shopId)
  }

  const loadAlerts = async () => {
    const [lowStock, expiring] = await Promise.all([
      getLowStockAlerts(shopId),
      getExpiringItems(shopId, 30)
    ])
    setLowStockItems(lowStock)
    setExpiringItems(expiring)
  }

  const handleStockAdjustment = (inventoryId: string) => {
    setSelectedInventoryId(inventoryId)
    setShowStockAdjustment(true)
  }

  const handleStockAdjustmentComplete = () => {
    setShowStockAdjustment(false)
    setSelectedInventoryId(null)
    handleRefresh()
  }

  if (!canViewInventory) {
    return (
      <div className="container mx-auto p-6">
        <div className="text-center py-8">
          <PackageIcon className="h-12 w-12 mx-auto mb-4 text-gray-400" />
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Access Denied</h2>
          <p className="text-gray-600">You don't have permission to view inventory.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Inventory Management</h1>
          <p className="text-gray-600">Track stock levels, manage products, and monitor inventory health</p>
        </div>
        <div className="flex space-x-2">
          {canManageInventory && (
            <Button
              onClick={() => setShowInventoryForm(true)}
              className="flex items-center space-x-2"
            >
              <PlusIcon className="h-4 w-4" />
              <span>Add Product</span>
            </Button>
          )}
          <Button
            variant="outline"
            onClick={handleRefresh}
            disabled={isLoading}
          >
            <RefreshCwIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            onClick={() => handleExport('csv')}
            disabled={isLoading}
          >
            <DownloadIcon className="h-4 w-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <XCircleIcon className="h-5 w-5 text-red-400" />
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error}</p>
            </div>
            <div className="ml-auto pl-3">
              <button
                onClick={clearError}
                className="text-red-400 hover:text-red-600"
              >
                <span className="sr-only">Dismiss</span>
                <XCircleIcon className="h-5 w-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg">
        <Button
          variant={activeTab === 'overview' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('overview')}
          className="flex-1"
        >
          <PackageIcon className="h-4 w-4 mr-2" />
          Overview
        </Button>
        <Button
          variant={activeTab === 'list' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('list')}
          className="flex-1"
        >
          All Items
        </Button>
        <Button
          variant={activeTab === 'alerts' ? 'default' : 'ghost'}
          onClick={() => {
            setActiveTab('alerts')
            loadAlerts()
          }}
          className="flex-1 relative"
        >
          <AlertTriangleIcon className="h-4 w-4 mr-2" />
          Alerts
          {summary && (summary.lowStockItems + summary.expiredItems + summary.expiringSoonItems) > 0 && (
            <Badge className="absolute -top-2 -right-2 bg-red-500 text-white text-xs">
              {summary.lowStockItems + summary.expiredItems + summary.expiringSoonItems}
            </Badge>
          )}
        </Button>
        <Button
          variant={activeTab === 'analytics' ? 'default' : 'ghost'}
          onClick={() => setActiveTab('analytics')}
          className="flex-1"
        >
          <BarChart3Icon className="h-4 w-4 mr-2" />
          Analytics
        </Button>
      </div>

      {/* Content based on active tab */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          {/* Summary Cards */}
          <InventorySummaryCards summary={summary} isLoading={isLoading} />

          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Low Stock</p>
                    <p className="text-2xl font-bold text-orange-600">
                      {summary?.lowStockItems || 0}
                    </p>
                  </div>
                  <TrendingDownIcon className="h-8 w-8 text-orange-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Out of Stock</p>
                    <p className="text-2xl font-bold text-red-600">
                      {summary?.outOfStockItems || 0}
                    </p>
                  </div>
                  <XCircleIcon className="h-8 w-8 text-red-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Expiring Soon</p>
                    <p className="text-2xl font-bold text-yellow-600">
                      {summary?.expiringSoonItems || 0}
                    </p>
                  </div>
                  <ClockIcon className="h-8 w-8 text-yellow-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Expired</p>
                    <p className="text-2xl font-bold text-red-600">
                      {summary?.expiredItems || 0}
                    </p>
                  </div>
                  <AlertTriangleIcon className="h-8 w-8 text-red-600" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Recent Activity */}
          <Card>
            <CardHeader>
              <CardTitle>Recent Inventory Items</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : inventory.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <PackageIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No inventory items found</p>
                  {canManageInventory && (
                    <Button
                      onClick={() => setShowInventoryForm(true)}
                      className="mt-2"
                    >
                      Add First Item
                    </Button>
                  )}
                </div>
              ) : (
                <InventoryList
                  items={inventory.slice(0, 10)}
                  onStockAdjustment={canAdjustStock ? handleStockAdjustment : undefined}
                  showActions={canManageInventory}
                  compact={true}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'list' && (
        <div className="space-y-6">
          {/* Filters */}
          <InventoryFilters
            onFilterChange={handleFilterChange}
            currentFilter={filter}
          />

          {/* Inventory List */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>All Inventory Items</CardTitle>
              <div className="flex items-center space-x-2">
                <Badge variant="secondary">{inventory.length} items</Badge>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleExport('excel')}
                  disabled={isLoading}
                >
                  <DownloadIcon className="h-4 w-4 mr-1" />
                  Excel
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <LoadingSpinner size="md" />
                </div>
              ) : (
                <InventoryList
                  items={inventory}
                  onStockAdjustment={canAdjustStock ? handleStockAdjustment : undefined}
                  showActions={canManageInventory}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'alerts' && (
        <div className="space-y-6">
          {/* Low Stock Items */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <TrendingDownIcon className="h-5 w-5 text-orange-600" />
                <span>Low Stock Items</span>
                <Badge className="bg-orange-100 text-orange-800">
                  {lowStockItems.length}
                </Badge>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {lowStockItems.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <PackageIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No low stock items</p>
                  <p className="text-sm">All items are well stocked</p>
                </div>
              ) : (
                <InventoryList
                  items={lowStockItems}
                  onStockAdjustment={canAdjustStock ? handleStockAdjustment : undefined}
                  showActions={canManageInventory}
                  highlightLowStock={true}
                />
              )}
            </CardContent>
          </Card>

          {/* Expiring Items */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <ClockIcon className="h-5 w-5 text-yellow-600" />
                <span>Expiring Items (Next 30 days)</span>
                <Badge className="bg-yellow-100 text-yellow-800">
                  {expiringItems.length}
                </Badge>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {expiringItems.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <ClockIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                  <p>No expiring items</p>
                  <p className="text-sm">All items have good shelf life</p>
                </div>
              ) : (
                <InventoryList
                  items={expiringItems}
                  onStockAdjustment={canAdjustStock ? handleStockAdjustment : undefined}
                  showActions={canManageInventory}
                  highlightExpiring={true}
                />
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === 'analytics' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Inventory Analytics</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8 text-gray-500">
                <BarChart3Icon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                <p>Analytics dashboard coming soon</p>
                <p className="text-sm">Charts and reports will be available here</p>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Inventory Form Modal */}
      {showInventoryForm && (
        <InventoryForm
          isOpen={showInventoryForm}
          onClose={() => setShowInventoryForm(false)}
          shopId={shopId}
          onInventoryCreated={() => {
            handleRefresh()
            setShowInventoryForm(false)
          }}
        />
      )}

      {/* Stock Adjustment Modal */}
      {showStockAdjustment && selectedInventoryId && (
        <StockAdjustmentModal
          isOpen={showStockAdjustment}
          onClose={() => setShowStockAdjustment(false)}
          inventoryId={selectedInventoryId}
          onAdjustmentComplete={handleStockAdjustmentComplete}
        />
      )}
    </div>
  )
}

export default InventoryPage