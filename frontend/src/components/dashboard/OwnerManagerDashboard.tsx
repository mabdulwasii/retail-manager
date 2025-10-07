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
  Activity
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { useDashboardData, useFraudStatistics } from '@/hooks/useDashboard'

export const OwnerManagerDashboard: React.FC = () => {
  const { user } = useAuth()
  const [period, setPeriod] = useState<'today' | 'week' | 'month' | 'year'>('month')

  const {
    shops,
    salesSummary,
    investmentROI,
    revenueAnalytics,
    isLoading,
    hasError,
    refetch
  } = useDashboardData(period)

  const { data: fraudStats } = useFraudStatistics(undefined, period)

  // Calculate business stats from real data
  const businessStats = [
    {
      title: 'Total Revenue',
      value: revenueAnalytics ? `$${(revenueAnalytics.totalRevenue || 0).toLocaleString()}` : '$0',
      description: `This ${period}`,
      icon: DollarSign,
      trend: revenueAnalytics ? `${revenueAnalytics.revenueGrowth > 0 ? '+' : ''}${revenueAnalytics.revenueGrowth?.toFixed(1) || 0}%` : '0%',
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
      title: 'Products Sold',
      value: salesSummary ? (salesSummary.totalSales?.toString() || '0') : '0',
      description: `This ${period}`,
      icon: Package,
      trend: salesSummary ? `Avg: $${salesSummary.averageOrderValue?.toFixed(2) || '0.00'}` : 'No sales',
      color: 'text-purple-600'
    },
    {
      title: 'Investment ROI',
      value: investmentROI ? `${investmentROI.roi?.toFixed(1) || 0}%` : '0%',
      description: `${period} return`,
      icon: TrendingUp,
      trend: investmentROI ? `$${investmentROI.totalReturn?.toLocaleString() || '0'} earned` : 'No returns',
      color: 'text-emerald-600'
    }
  ]

  const shopPerformance = [
    {
      name: 'Downtown Electronics',
      revenue: '$12,450',
      sales: 342,
      growth: '+15%',
      status: 'excellent'
    },
    {
      name: 'Mall Fashion Store',
      revenue: '$9,870',
      sales: 287,
      growth: '+8%',
      status: 'good'
    },
    {
      name: 'Grocery Express',
      revenue: '$8,230',
      sales: 156,
      growth: '+22%',
      status: 'excellent'
    },
    {
      name: 'Sports Center',
      revenue: '$5,440',
      sales: 98,
      growth: '-3%',
      status: 'attention'
    }
  ]

  const recentActivities = [
    {
      type: 'sale',
      shop: 'Downtown Electronics',
      description: 'Large sale completed: iPhone 15 Pro Max',
      amount: '$1,299.00',
      time: '5 minutes ago'
    },
    {
      type: 'investment',
      shop: 'Investment Pool',
      description: 'Quarterly profit distribution processed',
      amount: '$8,450.00',
      time: '2 hours ago'
    },
    {
      type: 'inventory',
      shop: 'Mall Fashion Store',
      description: 'Low stock alert: Designer Jeans',
      amount: '12 units left',
      time: '4 hours ago'
    },
    {
      type: 'analytics',
      shop: 'Grocery Express',
      description: 'Best performing product: Organic Bananas',
      amount: '145 sold today',
      time: '6 hours ago'
    }
  ]

  const investmentSummary = [
    {
      name: 'Tech Product Line',
      invested: '$25,000',
      returns: '$31,250',
      roi: '25%',
      status: 'active'
    },
    {
      name: 'Fashion Inventory',
      invested: '$18,500',
      returns: '$22,200',
      roi: '20%',
      status: 'active'
    },
    {
      name: 'Seasonal Products',
      invested: '$12,000',
      returns: '$13,560',
      roi: '13%',
      status: 'completed'
    }
  ]

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
            <div className="space-y-4">
              {shopPerformance.map((shop, index) => (
                <div key={index} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex items-center space-x-4">
                    <div className={`w-3 h-3 rounded-full ${
                      shop.status === 'excellent' ? 'bg-green-500' :
                      shop.status === 'good' ? 'bg-blue-500' :
                      'bg-yellow-500'
                    }`}></div>
                    <div>
                      <p className="font-medium">{shop.name}</p>
                      <p className="text-sm text-muted-foreground">
                        {shop.sales} sales this month
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
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4" asChild>
              <Link to="/shops">View All Shops</Link>
            </Button>
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
            <div className="space-y-4">
              {recentActivities.map((activity, index) => (
                <div key={index} className="flex items-start space-x-3">
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    activity.type === 'sale' ? 'bg-green-500' :
                    activity.type === 'investment' ? 'bg-blue-500' :
                    activity.type === 'inventory' ? 'bg-yellow-500' :
                    'bg-purple-500'
                  }`}></div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {activity.description}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {activity.shop} • {activity.time}
                    </p>
                    <p className="text-sm font-semibold text-green-600">
                      {activity.amount}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Investment Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Investment Portfolio</CardTitle>
          <CardDescription>
            Your investment performance and returns
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            {investmentSummary.map((investment, index) => (
              <div key={index} className="p-4 border rounded-lg">
                <div className="flex justify-between items-start mb-2">
                  <h4 className="font-medium">{investment.name}</h4>
                  <span className={`px-2 py-1 text-xs rounded-full ${
                    investment.status === 'active' ? 'bg-green-100 text-green-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {investment.status}
                  </span>
                </div>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Invested:</span>
                    <span>{investment.invested}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Current Value:</span>
                    <span className="font-semibold">{investment.returns}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">ROI:</span>
                    <span className="text-green-600 font-semibold">+{investment.roi}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
          <Button variant="outline" className="w-full mt-4" asChild>
            <Link to="/investments">View Full Portfolio</Link>
          </Button>
        </CardContent>
      </Card>

      {/* Risk Alerts */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Shield className="h-5 w-5" />
            <span>Risk Management</span>
          </CardTitle>
          <CardDescription>
            Security alerts and fraud detection
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div className="flex items-center space-x-3">
                <AlertCircle className="h-5 w-5 text-yellow-600" />
                <div>
                  <p className="text-sm font-medium">Unusual Sales Pattern Detected</p>
                  <p className="text-xs text-muted-foreground">
                    Downtown Electronics - Large electronics sale outside normal hours
                  </p>
                </div>
              </div>
              <Button size="sm" variant="outline">
                Review
              </Button>
            </div>
            <div className="flex items-center justify-between p-3 bg-green-50 border border-green-200 rounded-lg">
              <div className="flex items-center space-x-3">
                <Shield className="h-5 w-5 text-green-600" />
                <div>
                  <p className="text-sm font-medium">All Systems Secure</p>
                  <p className="text-xs text-muted-foreground">
                    No security threats detected in the last 24 hours
                  </p>
                </div>
              </div>
              <Button size="sm" variant="outline" asChild>
                <Link to="/security">View Details</Link>
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}