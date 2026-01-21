import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Badge } from '@/components/ui/badge'
import { InventorySummary } from '@/hooks/useInventory'
import { useCurrency } from '@/hooks/useCurrency'
import {
  PackageIcon,
  DollarSignIcon,
  TrendingUpIcon,
  AlertTriangleIcon,
  PieChartIcon,
  BarChart3Icon
} from 'lucide-react'

interface InventorySummaryCardsProps {
  summary: InventorySummary | null
  isLoading: boolean
}

export const InventorySummaryCards: React.FC<InventorySummaryCardsProps> = ({
  summary,
  isLoading
}) => {
  const { formatCurrency } = useCurrency()

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[...Array(3)].map((_, index) => (
          <Card key={index}>
            <CardContent className="p-6">
              <div className="flex items-center justify-center">
                <LoadingSpinner size="md" />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  if (!summary) {
    return (
      <div className="text-center py-8 text-gray-500">
        <AlertTriangleIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
        <p>Unable to load inventory summary</p>
      </div>
    )
  }

  // Calculate stock health percentage
  const totalAlerts = summary.lowStockItems + summary.expiredItems + summary.expiringSoonItems
  const stockHealthPercent = summary.totalItems > 0
    ? Math.round(((summary.totalItems - totalAlerts) / summary.totalItems) * 100)
    : 100

  return (
    <div className="space-y-6">
      {/* Main Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Items
            </CardTitle>
            <PackageIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{summary.totalItems}</div>
            <div className="text-xs text-gray-500 mt-1">
              Inventory products
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Cost
            </CardTitle>
            <DollarSignIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {formatCurrency(summary.totalInventoryCost || summary.totalValue || 0)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Inventory cost
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Projected Sales
            </CardTitle>
            <BarChart3Icon className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {formatCurrency(summary.projectedTotalSales || 0)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              If all sold
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Projected Profit
            </CardTitle>
            {(summary.projectedProfit || 0) >= 0 ? (
              <TrendingUpIcon className="h-4 w-4 text-green-600" />
            ) : (
              <AlertTriangleIcon className="h-4 w-4 text-red-600" />
            )}
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${
              (summary.projectedProfit || 0) >= 0 ? 'text-green-600' : 'text-red-600'
            }`}>
              {formatCurrency(summary.projectedProfit || 0)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              {summary.projectedProfitMargin
                ? `${summary.projectedProfitMargin.toFixed(1)}% margin`
                : 'Profit margin'}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Category Breakdown */}
      {summary.categoryBreakdown.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <PieChartIcon className="h-5 w-5" />
              <span>Inventory by Category</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* Top Category Highlight */}
              {summary.categoryBreakdown.length > 0 && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-medium text-blue-900">
                        Largest Category
                      </h3>
                      <p className="text-sm text-blue-700">
                        {summary.categoryBreakdown[0].category}
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xl font-bold text-blue-900">
                        {summary.categoryBreakdown[0].itemCount} items
                      </div>
                      <div className="text-sm text-blue-700">
                        {formatCurrency(summary.categoryBreakdown[0].totalValue)}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Category Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {summary.categoryBreakdown
                  .sort((a, b) => b.itemCount - a.itemCount)
                  .map((category, index) => {
                    const percentage = summary.totalItems > 0
                      ? (category.itemCount / summary.totalItems) * 100
                      : 0

                    return (
                      <div
                        key={category.category}
                        className="border rounded-lg p-3"
                      >
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-sm truncate">
                            {category.category}
                          </h4>
                          <Badge variant="secondary" className="text-xs">
                            {category.itemCount}
                          </Badge>
                        </div>

                        <div className="space-y-1">
                          <div className="flex justify-between text-sm">
                            <span className="font-medium">
                              {formatCurrency(category.totalValue)}
                            </span>
                            <span className="text-gray-500">
                              {percentage.toFixed(1)}%
                            </span>
                          </div>

                          {/* Progress bar */}
                          <div className="w-full bg-gray-200 rounded-full h-2">
                            <div
                              className="bg-blue-600 h-2 rounded-full"
                              style={{ width: `${Math.min(percentage, 100)}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    )
                  })}
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Alerts Summary */}
      {totalAlerts > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertTriangleIcon className="h-5 w-5 text-orange-600" />
              <span>Inventory Alerts</span>
              <Badge className="bg-orange-100 text-orange-800">
                {totalAlerts}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {summary.lowStockItems > 0 && (
                <div className="text-center p-4 bg-orange-50 rounded-lg">
                  <div className="text-2xl font-bold text-orange-600">
                    {summary.lowStockItems}
                  </div>
                  <div className="text-sm text-orange-700">
                    Low Stock Items
                  </div>
                </div>
              )}

              {summary.expiringSoonItems > 0 && (
                <div className="text-center p-4 bg-yellow-50 rounded-lg">
                  <div className="text-2xl font-bold text-yellow-600">
                    {summary.expiringSoonItems}
                  </div>
                  <div className="text-sm text-yellow-700">
                    Expiring Soon
                  </div>
                </div>
              )}

              {summary.expiredItems > 0 && (
                <div className="text-center p-4 bg-red-50 rounded-lg">
                  <div className="text-2xl font-bold text-red-600">
                    {summary.expiredItems}
                  </div>
                  <div className="text-sm text-red-700">
                    Expired Items
                  </div>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

export default InventorySummaryCards