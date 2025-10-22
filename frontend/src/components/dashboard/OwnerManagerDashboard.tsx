import React, { useState } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Store,
  Package,
  ShoppingCart,
  TrendingUp,
  DollarSign,
  Users,
  AlertCircle,
  Plus,
  BarChart3,
  Receipt,
  Coins,
  Shield,
  Loader2,
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock,
  XCircle,
  FileText
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { useDashboardData, useAllShopsPerformance } from '@/hooks/useDashboard'

export const OwnerManagerDashboard: React.FC = () => {
  const { user } = useAuth()
  const [period, setPeriod] = useState<'today' | 'week' | 'month' | 'year'>('month')

  const {
    shops,
    salesSummary,
    investmentROI,
    revenueAnalytics,
    inventorySummary,
    expenseSummary,
    fraudStatistics,
    isLoading,
    hasError,
    refetch
  } = useDashboardData(period)

  // Use fraudStatistics from useDashboardData instead of calling useFraudStatistics again
  const fraudStats = fraudStatistics
  const { data: shopsPerformance, isLoading: loadingPerformance } = useAllShopsPerformance(period)

  // Calculate business stats from real data
  const businessStats = [
    {
      title: 'Total Revenue',
      value: revenueAnalytics ? `$${(revenueAnalytics.currentRevenue || 0).toLocaleString()}` : '$0',
      description: `This ${period}`,
      icon: DollarSign,
      trend: revenueAnalytics ? `${revenueAnalytics.growthRate > 0 ? '+' : ''}${revenueAnalytics.growthRate?.toFixed(1) || 0}%` : '0%',
      color: 'text-green-600'
    },
    {
      title: 'Active Shops',
      value: shops.length.toString(),
      description: 'Across locations',
      icon: Store,
      trend: `${shops.filter(s => s.status === 'ACTIVE').length} operational`,
      color: 'text-blue-600'
    },
    {
      title: 'Transactions',
      value: salesSummary ? (salesSummary.totalTransactions?.toString() || '0') : '0',
      description: `This ${period}`,
      icon: Package,
      trend: salesSummary ? `Avg: $${salesSummary.averageTransactionValue?.toFixed(2) || '0.00'}` : 'No sales',
      color: 'text-purple-600'
    },
    {
      title: 'Investment ROI',
      value: investmentROI ? `${investmentROI.roiPercentage?.toFixed(1) || 0}%` : '0%',
      description: `${period} return`,
      icon: TrendingUp,
      trend: investmentROI ? `$${investmentROI.totalDistributions?.toLocaleString() || '0'} earned` : 'No returns',
      color: 'text-emerald-600'
    }
  ]

  // Format shop performance data from API
  const shopPerformance = (shopsPerformance || []).map((shop) => {
    const getPerformanceStatus = (growth: number) => {
      if (growth >= 15) return 'excellent'
      if (growth >= 5) return 'good'
      if (growth >= 0) return 'average'
      return 'attention'
    }

    return {
      shopId: shop.shopId,
      name: shop.shopName,
      revenue: `$${shop.revenue.toLocaleString()}`,
      sales: shop.transactions,
      growth: `${shop.growthRate > 0 ? '+' : ''}${shop.growthRate.toFixed(1)}%`,
      status: getPerformanceStatus(shop.growthRate)
    }
  })

  // Recent activities - using derived data from alerts and inventory
  const recentActivities = [
    ...(inventorySummary?.lowStockItems ? [{
      type: 'inventory' as const,
      shop: 'Inventory Alert',
      description: `${inventorySummary.lowStockItems} items running low on stock`,
      amount: `${inventorySummary.lowStockItems} items`,
      time: 'Current'
    }] : []),
    ...(inventorySummary?.expiredItems ? [{
      type: 'inventory' as const,
      shop: 'Inventory Alert',
      description: `${inventorySummary.expiredItems} expired items need attention`,
      amount: `${inventorySummary.expiredItems} items`,
      time: 'Current'
    }] : []),
    ...(expenseSummary?.pendingApproval ? [{
      type: 'expense' as const,
      shop: 'Expense Management',
      description: `${expenseSummary.pendingApproval} expenses awaiting approval`,
      amount: 'Pending',
      time: 'Current'
    }] : []),
    ...(fraudStats?.highRiskCount ? [{
      type: 'alert' as const,
      shop: 'Fraud Detection',
      description: `${fraudStats.highRiskCount} high-risk transactions detected`,
      amount: `${fraudStats.highRiskCount} flagged`,
      time: 'Recent'
    }] : [])
  ].slice(0, 4) // Show only top 4 activities

  // Calculate total alerts count
  const totalAlerts = (
    (inventorySummary?.lowStockItems || 0) +
    (inventorySummary?.expiredItems || 0) +
    (inventorySummary?.expiringSoonItems || 0) +
    (expenseSummary?.pendingApproval || 0) +
    (fraudStats?.highRiskCount || 0) +
    (fraudStats?.criticalRiskCount || 0)
  )

  // Handle loading state
  if (isLoading && !salesSummary && !revenueAnalytics && !investmentROI) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <div className="text-center">
              <Loader2 className="h-12 w-12 text-blue-500 mx-auto mb-4 animate-spin" />
              <h3 className="text-lg font-semibold mb-2">Loading Dashboard</h3>
              <p className="text-muted-foreground">
                Fetching your business analytics...
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Business Overview
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Here's how your business is performing.
          </p>
        </div>
        <div className="flex space-x-2">
          <Select value={period} onValueChange={(value: any) => setPeriod(value)}>
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Period" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="today">Today</SelectItem>
              <SelectItem value="week">This Week</SelectItem>
              <SelectItem value="month">This Month</SelectItem>
              <SelectItem value="year">This Year</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" onClick={() => refetch()} disabled={isLoading}>
            {isLoading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Activity className="mr-2 h-4 w-4" />
            )}
            Refresh
          </Button>
          <Button variant="outline" asChild>
            <Link to="/analytics">
              <BarChart3 className="mr-2 h-4 w-4" />
              Analytics
            </Link>
          </Button>
          <Button asChild>
            <Link to="/shops/create">
              <Plus className="mr-2 h-4 w-4" />
              Add Shop
            </Link>
          </Button>
        </div>
      </div>

      {/* Business Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {businessStats.map((stat, index) => (
          <Card key={index} className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
              <div className="text-xs text-green-600 mt-1">
                {stat.trend}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common business tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/sales">
                <ShoppingCart className="h-6 w-6 mb-2" />
                New Sale
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/products">
                <Package className="h-6 w-6 mb-2" />
                Add Product
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/investments">
                <Coins className="h-6 w-6 mb-2" />
                Track Investments
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/receipts">
                <Receipt className="h-6 w-6 mb-2" />
                View Receipts
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Shop Performance */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Shop Performance</CardTitle>
            <CardDescription>
              Revenue and sales metrics for each location
            </CardDescription>
          </CardHeader>
          <CardContent>
            {loadingPerformance ? (
              <div className="flex justify-center py-8">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : shopPerformance.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-muted-foreground">No shop performance data available</p>
                <Button variant="outline" className="mt-4" asChild>
                  <Link to="/shops/create">
                    <Plus className="mr-2 h-4 w-4" />
                    Create Your First Shop
                  </Link>
                </Button>
              </div>
            ) : (
              <>
                <div className="space-y-4">
                  {shopPerformance.map((shop, index) => (
                    <Link 
                      key={index} 
                      to={`/shops/${shop.shopId}`}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors cursor-pointer"
                    >
                      <div className="flex items-center space-x-4">
                        <div className={`w-3 h-3 rounded-full ${
                          shop.status === 'excellent' ? 'bg-green-500' :
                          shop.status === 'good' ? 'bg-blue-500' :
                          shop.status === 'average' ? 'bg-yellow-500' :
                          'bg-red-500'
                        }`}></div>
                        <div>
                          <p className="font-medium">{shop.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {shop.sales} transactions this {period}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold">{shop.revenue}</p>
                        <p className={`text-sm ${
                          shop.growth.startsWith('+') ? 'text-green-600' : 'text-red-600'
                        }`}>
                          {shop.growth}
                        </p>
                      </div>
                    </Link>
                  ))}
                </div>
                <Button variant="outline" className="w-full mt-4" asChild>
                  <Link to="/shops">View All Shops</Link>
                </Button>
              </>
            )}
          </CardContent>
        </Card>

        {/* Recent Activities */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Activities</CardTitle>
            <CardDescription>
              Latest updates across your business
            </CardDescription>
          </CardHeader>
          <CardContent>
            {recentActivities.length === 0 ? (
              <div className="text-center py-8">
                <Activity className="h-12 w-12 mx-auto text-muted-foreground mb-2" />
                <p className="text-muted-foreground">No recent activities to show</p>
                <p className="text-sm text-muted-foreground mt-1">
                  Activity alerts will appear here based on your business operations
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {recentActivities.map((activity, index) => (
                  <div key={index} className="flex items-start space-x-3">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      activity.type === 'inventory' ? 'bg-yellow-500' :
                      activity.type === 'expense' ? 'bg-blue-500' :
                      activity.type === 'alert' ? 'bg-red-500' :
                      'bg-purple-500'
                    }`}></div>
                    <div className="flex-1 space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {activity.description}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {activity.shop} • {activity.time}
                      </p>
                      <p className={`text-sm font-semibold ${
                        activity.type === 'alert' ? 'text-red-600' :
                        activity.type === 'inventory' ? 'text-yellow-600' :
                        'text-blue-600'
                      }`}>
                        {activity.amount}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Critical Alerts Section */}
      {totalAlerts > 0 && (
        <Card className="border-orange-200 bg-orange-50">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2 text-orange-800">
              <AlertTriangle className="h-5 w-5" />
              <span>Action Required ({totalAlerts} items)</span>
            </CardTitle>
            <CardDescription>Important items that need your attention</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Expense Approvals */}
            {expenseSummary && expenseSummary.pendingApproval > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-orange-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <FileText className="h-5 w-5 text-orange-600" />
                  <div>
                    <p className="text-sm font-medium">Expenses Awaiting Approval</p>
                    <p className="text-xs text-muted-foreground">
                      {expenseSummary.pendingApproval} expense(s) totaling ${expenseSummary.totalAmount?.toLocaleString() || '0'}
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/expenses?status=pending">Review</Link>
                </Button>
              </div>
            )}

            {/* Low Stock Items */}
            {inventorySummary && inventorySummary.lowStockItems > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-yellow-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <AlertCircle className="h-5 w-5 text-yellow-600" />
                  <div>
                    <p className="text-sm font-medium">Low Stock Alert</p>
                    <p className="text-xs text-muted-foreground">
                      {inventorySummary.lowStockItems} product(s) running low on inventory
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/inventory?filter=lowStock">Restock</Link>
                </Button>
              </div>
            )}

            {/* Expired Items */}
            {inventorySummary && inventorySummary.expiredItems > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-red-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <XCircle className="h-5 w-5 text-red-600" />
                  <div>
                    <p className="text-sm font-medium">Expired Items</p>
                    <p className="text-xs text-muted-foreground">
                      {inventorySummary.expiredItems} expired product(s) need to be removed
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/inventory?filter=expired">Remove</Link>
                </Button>
              </div>
            )}

            {/* Expiring Soon */}
            {inventorySummary && inventorySummary.expiringSoonItems > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-amber-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <Clock className="h-5 w-5 text-amber-600" />
                  <div>
                    <p className="text-sm font-medium">Items Expiring Soon</p>
                    <p className="text-xs text-muted-foreground">
                      {inventorySummary.expiringSoonItems} product(s) expiring in the next 30 days
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/inventory?filter=expiringSoon">Review</Link>
                </Button>
              </div>
            )}

            {/* High Risk Transactions */}
            {fraudStats && fraudStats.highRiskCount > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-orange-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <Shield className="h-5 w-5 text-orange-600" />
                  <div>
                    <p className="text-sm font-medium">High Risk Transactions</p>
                    <p className="text-xs text-muted-foreground">
                      {fraudStats.highRiskCount} transaction(s) flagged for review
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/fraud-detection?risk=high">Investigate</Link>
                </Button>
              </div>
            )}

            {/* Critical Risk Transactions */}
            {fraudStats && fraudStats.criticalRiskCount > 0 && (
              <div className="flex items-center justify-between p-3 bg-white border border-red-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <AlertTriangle className="h-5 w-5 text-red-600" />
                  <div>
                    <p className="text-sm font-medium text-red-800">Critical Risk Alert</p>
                    <p className="text-xs text-muted-foreground">
                      {fraudStats.criticalRiskCount} critical transaction(s) require immediate attention
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="destructive" asChild>
                  <Link to="/fraud-detection?risk=critical">Review Now</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Inventory & Expense Overview */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Inventory Summary */}
        {inventorySummary && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Package className="h-5 w-5" />
                <span>Inventory Overview</span>
              </CardTitle>
              <CardDescription>Stock levels and inventory health</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3 border rounded-lg">
                  <p className="text-xs text-muted-foreground">Total Items</p>
                  <p className="text-2xl font-bold">{inventorySummary.totalItems}</p>
                </div>
                <div className="p-3 border rounded-lg">
                  <p className="text-xs text-muted-foreground">Total Value</p>
                  <p className="text-2xl font-bold">${inventorySummary.totalValue?.toLocaleString() || '0'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center justify-between p-2 bg-yellow-50 rounded">
                  <span className="text-sm">Low Stock</span>
                  <span className="text-sm font-semibold text-yellow-700">{inventorySummary.lowStockItems}</span>
                </div>
                <div className="flex items-center justify-between p-2 bg-red-50 rounded">
                  <span className="text-sm">Expired</span>
                  <span className="text-sm font-semibold text-red-700">{inventorySummary.expiredItems}</span>
                </div>
                <div className="flex items-center justify-between p-2 bg-amber-50 rounded">
                  <span className="text-sm">Expiring Soon</span>
                  <span className="text-sm font-semibold text-amber-700">{inventorySummary.expiringSoonItems}</span>
                </div>
              </div>

              <Button variant="outline" className="w-full" asChild>
                <Link to="/inventory">Manage Inventory</Link>
              </Button>
            </CardContent>
          </Card>
        )}

        {/* Expense Summary */}
        {expenseSummary && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Receipt className="h-5 w-5" />
                <span>Expense Summary</span>
              </CardTitle>
              <CardDescription>Monthly expenses and approvals</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-3 border rounded-lg">
                  <p className="text-xs text-muted-foreground">Total Expenses</p>
                  <p className="text-2xl font-bold">{expenseSummary.totalExpenses}</p>
                </div>
                <div className="p-3 border rounded-lg">
                  <p className="text-xs text-muted-foreground">Total Amount</p>
                  <p className="text-2xl font-bold">${expenseSummary.totalAmount?.toLocaleString() || '0'}</p>
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center justify-between p-2 bg-orange-50 rounded">
                  <span className="text-sm">Pending Approval</span>
                  <span className="text-sm font-semibold text-orange-700">{expenseSummary.pendingApproval}</span>
                </div>
                <div className="flex items-center justify-between p-2 bg-green-50 rounded">
                  <span className="text-sm">Approved</span>
                  <span className="text-sm font-semibold text-green-700">{expenseSummary.approvedExpenses}</span>
                </div>
                <div className="flex items-center justify-between p-2 bg-blue-50 rounded">
                  <span className="text-sm">This Month</span>
                  <span className="text-sm font-semibold text-blue-700">${expenseSummary.monthlyTotal?.toLocaleString() || '0'}</span>
                </div>
              </div>

              <Button variant="outline" className="w-full" asChild>
                <Link to="/expenses">View All Expenses</Link>
              </Button>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Investment Summary */}
      {investmentROI && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Coins className="h-5 w-5" />
              <span>Investment Performance</span>
            </CardTitle>
            <CardDescription>
              Your investment returns for this {period}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <div className="p-4 border rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Total Invested</p>
                <p className="text-2xl font-bold">${investmentROI.totalInvestmentAmount?.toLocaleString() || '0'}</p>
              </div>
              <div className="p-4 border rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">Total Returns</p>
                <p className="text-2xl font-bold text-green-600">${investmentROI.totalDistributions?.toLocaleString() || '0'}</p>
              </div>
              <div className="p-4 border rounded-lg">
                <p className="text-sm text-muted-foreground mb-1">ROI</p>
                <p className="text-2xl font-bold text-emerald-600">+{investmentROI.roiPercentage?.toFixed(1) || '0'}%</p>
              </div>
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/investments">View Full Portfolio</Link>
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Fraud & Risk Analytics */}
      {fraudStats && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Shield className="h-5 w-5" />
              <span>Fraud Detection & Risk Management</span>
            </CardTitle>
            <CardDescription>
              Security analytics for this {period}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-4 gap-4 mb-4">
              <div className="p-3 border rounded-lg">
                <p className="text-xs text-muted-foreground">Total Assessments</p>
                <p className="text-2xl font-bold">{fraudStats.totalAssessments}</p>
              </div>
              <div className="p-3 border rounded-lg">
                <p className="text-xs text-muted-foreground">High Risk</p>
                <p className="text-2xl font-bold text-orange-600">{fraudStats.highRiskCount}</p>
              </div>
              <div className="p-3 border rounded-lg">
                <p className="text-xs text-muted-foreground">Critical Risk</p>
                <p className="text-2xl font-bold text-red-600">{fraudStats.criticalRiskCount}</p>
              </div>
              <div className="p-3 border rounded-lg">
                <p className="text-xs text-muted-foreground">Risk Rate</p>
                <p className="text-2xl font-bold">{fraudStats.riskRate?.toFixed(1) || '0'}%</p>
              </div>
            </div>

            {fraudStats.criticalRiskCount === 0 && fraudStats.highRiskCount === 0 ? (
              <div className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <CheckCircle2 className="h-5 w-5 text-green-600" />
                  <div>
                    <p className="text-sm font-medium">All Systems Secure</p>
                    <p className="text-xs text-muted-foreground">
                      No high-risk transactions detected this {period}
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to="/fraud-detection">View Details</Link>
                </Button>
              </div>
            ) : (
              <div className="flex items-center justify-between p-3 bg-red-50 border border-red-200 rounded-lg">
                <div className="flex items-center space-x-3">
                  <AlertTriangle className="h-5 w-5 text-red-600" />
                  <div>
                    <p className="text-sm font-medium text-red-800">Security Alerts Active</p>
                    <p className="text-xs text-muted-foreground">
                      {fraudStats.highRiskCount + fraudStats.criticalRiskCount} transaction(s) require review
                    </p>
                  </div>
                </div>
                <Button size="sm" variant="destructive" asChild>
                  <Link to="/fraud-detection">Review Now</Link>
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}