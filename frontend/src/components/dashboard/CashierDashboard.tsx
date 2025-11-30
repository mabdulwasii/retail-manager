import React, { useState, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useSalesSummary, TimePeriod } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  ShoppingCart,
  Receipt,
  DollarSign,
  Package,
  Clock,
  TrendingUp,
  Users,
  Scan,
  Loader2,
  RefreshCw,
  Star
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { useCurrency } from '@/hooks/useCurrency'
import { ShopSelector } from '@/components/ui/shop-selector'

export const CashierDashboard: React.FC = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const [period, setPeriod] = useState<TimePeriod>('today')
  const [selectedShopId, setSelectedShopId] = useState<string | undefined>(undefined)
  const { data: salesSummary, isLoading: salesLoading, refetch } = useSalesSummary(selectedShopId, period)
  const [currentTime, setCurrentTime] = useState(new Date())
  
  // Set selectedShopId once user is loaded to prevent double API calls
  useEffect(() => {
    if (user?.shopId && !selectedShopId) {
      setSelectedShopId(user.shopId)
    }
  }, [user?.shopId, selectedShopId])

  // Update current time every minute
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 60000) // Update every minute
    return () => clearInterval(timer)
  }, [])

  const totalTransactions = salesSummary?.totalTransactions || 0
  const totalRevenue = salesSummary?.totalRevenue || 0
  const avgTransaction = salesSummary?.averageTransactionValue || 0
  //const topProductsCount = salesSummary?.topProducts?.length || 0
  
  const periodLabel = period === 'today' ? 'Today' : period === 'week' ? 'This Week' : period === 'month' ? 'This Month' : 'This Year'
  
  const todayStats = [
    {
      title: `Sales ${periodLabel}`,
      value: `${totalTransactions}`,
      description: 'Transactions completed',
      icon: ShoppingCart,
      color: 'text-blue-600'
    },
    {
      title: `Revenue ${periodLabel}`,
      value: formatCurrency(totalRevenue),
      description: 'Total earnings',
      icon: DollarSign,
      color: 'text-green-600'
    },
    // {
    //   title: 'Top Products',
    //   value: `${topProductsCount}`,
    //   description: 'Product varieties',
    //   icon: Package,
    //   color: 'text-purple-600'
    // },
    {
      title: 'Avg. Transaction',
      value: formatCurrency(avgTransaction),
      description: 'Per sale',
      icon: TrendingUp,
      color: 'text-orange-600'
    }
  ]

  // Generate activities from real sales data
  const recentActivities = totalTransactions > 0
    ? [
        {
          id: '1',
          type: 'sale',
          description: `Completed ${totalTransactions} transaction${totalTransactions !== 1 ? 's' : ''} today`,
          time: 'Today',
          amount: formatCurrency(totalRevenue)
        },
        // {
        //   id: '2',
        //   type: 'inventory',
        //   description: topProductsCount > 0 ? `${topProductsCount} products sold today` : 'No products sold yet',
        //   time: 'Today'
        // },
        {
          id: '3',
          type: 'sale',
          description: `Average transaction value: ${formatCurrency(avgTransaction)}`,
          time: 'Today',
          amount: formatCurrency(avgTransaction)
        }
      ]
    : [
        {
          id: '1',
          type: 'sale',
          description: 'No sales recorded today yet',
          time: 'Waiting for first sale',
          amount: undefined
        }
      ]

  const quickActions = [
    {
      title: 'New Sale',
      description: 'Process customer purchase',
      icon: ShoppingCart,
      href: '/sales',
      color: 'bg-blue-500 hover:bg-blue-600'
    },
    {
      title: 'Scan Product',
      description: 'Quick barcode scan',
      icon: Scan,
      href: '/sales/scan',
      color: 'bg-green-500 hover:bg-green-600'
    },
    {
      title: 'View Receipts',
      description: 'Recent transactions',
      icon: Receipt,
      href: '/receipts',
      color: 'bg-purple-500 hover:bg-purple-600'
    },
    {
      title: 'Check Inventory',
      description: 'Product availability',
      icon: Package,
      href: '/inventory',
      color: 'bg-orange-500 hover:bg-orange-600'
    }
  ]

  if (salesLoading) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <div className="text-center">
              <Loader2 className="h-12 w-12 text-blue-500 mx-auto mb-4 animate-spin" />
              <h3 className="text-lg font-semibold mb-2">Loading Your Dashboard</h3>
              <p className="text-muted-foreground">
                Fetching today's sales data...
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
            Welcome, {user?.firstName || user?.username}!
          </h1>
          <p className="text-muted-foreground">
            Ready to serve customers and process sales.
          </p>
        </div>
        <div className="flex space-x-2">
          <ShopSelector 
            value={selectedShopId || ''}
            onValueChange={setSelectedShopId}
            className="w-[200px]"
          />
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
          <Button 
            variant="outline" 
            onClick={() => refetch()}
            disabled={salesLoading}
          >
            {salesLoading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <RefreshCw className="mr-2 h-4 w-4" />
            )}
            Refresh
          </Button>
        </div>
      </div>

      {/* Today's Performance */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {todayStats.map((stat, index) => (
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
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common cashier tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {quickActions.map((action, index) => {
              const isNewSale = action.title === 'New Sale'
              return (
                <Button
                  key={index}
                  variant={isNewSale ? "default" : "outline"}
                  className={isNewSale 
                    ? "h-28 flex-col bg-gradient-to-br from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 text-white shadow-lg hover:shadow-xl transition-all duration-200 relative overflow-hidden group"
                    : "h-24 flex-col space-y-2 p-4"
                  }
                  asChild
                >
                  <Link to={action.href}>
                    {isNewSale && (
                      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700" />
                    )}
                    <action.icon className={isNewSale ? "h-8 w-8 mb-2 relative z-10" : "h-8 w-8"} />
                    <div className={isNewSale ? "text-center relative z-10" : "text-center"}>
                      <div className={isNewSale ? "font-bold text-base" : "font-medium text-sm"}>{action.title}</div>
                      <div className={isNewSale ? "text-xs opacity-90 mt-1" : "text-xs text-muted-foreground"}>
                        {action.description}
                      </div>
                    </div>
                  </Link>
                </Button>
              )
            })}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        {/* Today's Summary */}
        <Card>
          <CardHeader>
            <CardTitle>Today's Summary</CardTitle>
            <CardDescription>
              Your performance metrics for today
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentActivities.length > 0 ? (
                recentActivities.map((activity) => (
                  <div key={activity.id} className="flex items-start space-x-4">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      activity.type === 'sale' ? 'bg-green-500' :
                      activity.type === 'inventory' ? 'bg-blue-500' :
                      'bg-gray-500'
                    }`}></div>
                    <div className="flex-1 space-y-1">
                      <p className="text-sm font-medium leading-none">
                        {activity.description}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {activity.time}
                      </p>
                      {activity?.amount && (
                        <p className={`text-sm font-semibold ${
                          activity?.amount.startsWith('-') ? 'text-red-600' : 'text-green-600'
                        }`}>
                          {activity?.amount}
                        </p>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-center text-muted-foreground py-4">
                  No sales data available
                </p>
              )}
            </div>
            
            {/* Top Products Section */}
            {/* {salesSummary?.topProducts && salesSummary.topProducts.length > 0 && (
              <div className="mt-6 pt-4 border-t">
                <h4 className="text-sm font-semibold mb-3 flex items-center">
                  <Star className="h-4 w-4 mr-2 text-yellow-500" />
                  Top Selling Products Today
                </h4>
                <div className="space-y-2">
                  {salesSummary.topProducts.slice(0, 5).map((product, idx) => (
                    <div key={idx} className="flex justify-between items-center text-sm">
                      <span className="text-muted-foreground">{product.name || `Product ${idx + 1}`}</span>
                      <span className="font-medium">{product.quantity || product.count || 0} sold</span>
                    </div>
                  ))}
                </div>
              </div>
            )} */}
          </CardContent>
        </Card>

        {/* Sales Tips */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <TrendingUp className="h-5 w-5" />
              <span>Sales Tips</span>
            </CardTitle>
            <CardDescription>
              Boost your performance with these tips
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
                <h4 className="font-medium text-blue-900">Upselling Opportunity</h4>
                <p className="text-sm text-blue-800 mt-1">
                  Suggest accessories when customers buy electronics
                </p>
              </div>
              <div className="p-3 bg-green-50 border border-green-200 rounded-lg">
                <h4 className="font-medium text-green-900">Customer Service</h4>
                <p className="text-sm text-green-800 mt-1">
                  Always ask if they found everything they needed
                </p>
              </div>
              <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg">
                <h4 className="font-medium text-purple-900">Product Knowledge</h4>
                <p className="text-sm text-purple-800 mt-1">
                  Review new product features during downtime
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Current Shift Info & Performance */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Clock className="h-5 w-5" />
            <span>Current Session</span>
          </CardTitle>
          <CardDescription>
            Your work session and performance
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            <div className="text-center p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg">
              <div className="text-2xl font-bold text-blue-700">
                {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
              <div className="text-sm text-blue-600">Current Time</div>
            </div>
            <div className="text-center p-4 bg-gradient-to-br from-green-50 to-green-100 rounded-lg">
              <div className="text-2xl font-bold text-green-700">{totalTransactions}</div>
              <div className="text-sm text-green-600">Transactions Today</div>
            </div>
            <div className="text-center p-4 bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg">
              <div className="text-2xl font-bold text-purple-700">
                ${totalRevenue.toLocaleString()}
              </div>
              <div className="text-sm text-purple-600">Total Revenue</div>
            </div>
          </div>
          
          {/* Performance Badge */}
          {totalTransactions > 0 && (
            <div className="mt-4 p-4 bg-gradient-to-r from-yellow-50 to-orange-50 border border-yellow-200 rounded-lg text-center">
              <div className="flex items-center justify-center space-x-2">
                <Star className="h-5 w-5 text-yellow-600" />
                <span className="font-semibold text-yellow-900">
                  {totalTransactions >= 20 ? 'Outstanding Performance!' :
                   totalTransactions >= 10 ? 'Great Work Today!' :
                   totalTransactions >= 5 ? 'Keep It Up!' :
                   'Good Start!'}
                </span>
              </div>
              <p className="text-xs text-yellow-700 mt-1">
                {totalTransactions >= 20 ? 'You\'ve processed over 20 transactions!' :
                 totalTransactions >= 10 ? 'You\'re doing great with ' + totalTransactions + ' sales!' :
                 totalTransactions >= 5 ? totalTransactions + ' transactions and counting!' :
                 'First sale of the day!'}
              </p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}