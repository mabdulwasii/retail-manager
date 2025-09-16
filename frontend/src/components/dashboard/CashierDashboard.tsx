import React, { useState } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useSalesSummary } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  ShoppingCart,
  Receipt,
  DollarSign,
  Package,
  Clock,
  TrendingUp,
  Users,
  Scan,
  Loader2
} from 'lucide-react'
import { Link } from 'react-router-dom'

export const CashierDashboard: React.FC = () => {
  const { user } = useAuth()
  const { data: salesSummary, isLoading: salesLoading } = useSalesSummary(undefined, 'today')

  const todayStats = [
    {
      title: 'Sales Today',
      value: salesSummary ? `${salesSummary.totalSales || 0}` : '0',
      description: 'Transactions completed',
      icon: ShoppingCart,
      color: 'text-blue-600'
    },
    {
      title: 'Revenue Today',
      value: salesSummary ? `$${(salesSummary.totalRevenue || 0).toLocaleString()}` : '$0',
      description: 'Total earnings',
      icon: DollarSign,
      color: 'text-green-600'
    },
    {
      title: 'Top Products',
      value: salesSummary?.topProducts ? `${salesSummary.topProducts.length}` : '0',
      description: 'Product varieties',
      icon: Package,
      color: 'text-purple-600'
    },
    {
      title: 'Avg. Transaction',
      value: salesSummary ? `$${(salesSummary.averageOrderValue || 0).toFixed(2)}` : '$0.00',
      description: 'Per sale',
      icon: TrendingUp,
      color: 'text-orange-600'
    }
  ]

  const recentActivities = [
    {
      id: '1',
      type: 'sale',
      description: 'Completed sale #1234',
      time: '2 minutes ago',
      amount: '$45.99'
    },
    {
      id: '2',
      type: 'inventory',
      description: 'Product scan completed',
      time: '15 minutes ago'
    },
    {
      id: '3',
      type: 'sale',
      description: 'Processed customer return',
      time: '1 hour ago',
      amount: '-$12.50'
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
      <div>
        <h1 className="text-3xl font-bold text-gray-900">
          Welcome, {user?.firstName || user?.username}!
        </h1>
        <p className="text-muted-foreground">
          Ready to serve customers and process sales.
        </p>
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
            {quickActions.map((action, index) => (
              <Button
                key={index}
                variant="outline"
                className="h-24 flex-col space-y-2 p-4"
                asChild
              >
                <Link to={action.href}>
                  <action.icon className="h-8 w-8" />
                  <div className="text-center">
                    <div className="font-medium text-sm">{action.title}</div>
                    <div className="text-xs text-muted-foreground">
                      {action.description}
                    </div>
                  </div>
                </Link>
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        {/* Recent Sales Activity */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>
              Your latest transactions and actions
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
                      {activity.amount && (
                        <p className={`text-sm font-semibold ${
                          activity.amount.startsWith('-') ? 'text-red-600' : 'text-green-600'
                        }`}>
                          {activity.amount}
                        </p>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-center text-muted-foreground py-4">
                  No recent activity
                </p>
              )}
            </div>
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

      {/* Current Shift Info */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Clock className="h-5 w-5" />
            <span>Shift Information</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-3 gap-4">
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">8:00 AM</div>
              <div className="text-sm text-muted-foreground">Shift Started</div>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">6:00 PM</div>
              <div className="text-sm text-muted-foreground">Shift Ends</div>
            </div>
            <div className="text-center p-4 bg-gray-50 rounded-lg">
              <div className="text-2xl font-bold text-purple-600">
                {new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
              <div className="text-sm text-muted-foreground">Current Time</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}