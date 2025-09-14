import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Store, Package, ShoppingCart, TrendingUp, Users, AlertCircle } from 'lucide-react'

export const DashboardPage: React.FC = () => {
  const { user } = useAuth()

  // Mock data - in real app, this would come from API
  const stats = [
    {
      title: 'Total Shops',
      value: '3',
      description: 'Active shops',
      icon: Store,
      trend: '+2 this month',
    },
    {
      title: 'Products',
      value: '1,234',
      description: 'Total products',
      icon: Package,
      trend: '+12% from last month',
    },
    {
      title: 'Sales Today',
      value: '$2,543',
      description: 'Revenue today',
      icon: ShoppingCart,
      trend: '+8% from yesterday',
    },
    {
      title: 'Active Investments',
      value: '$45,210',
      description: 'Total invested',
      icon: TrendingUp,
      trend: '12.5% ROI',
    },
  ]

  const recentActivities = [
    {
      type: 'sale',
      description: 'New sale transaction completed',
      amount: '$125.50',
      time: '2 minutes ago',
    },
    {
      type: 'product',
      description: 'Product inventory updated',
      amount: '50 units',
      time: '15 minutes ago',
    },
    {
      type: 'investment',
      description: 'Investment return processed',
      amount: '$2,340.00',
      time: '1 hour ago',
    },
    {
      type: 'shop',
      description: 'New shop created',
      amount: 'Electronics Store',
      time: '3 hours ago',
    },
  ]

  const alerts = [
    {
      type: 'warning',
      message: 'Low stock alert: iPhone 15 Pro (5 units remaining)',
      time: '30 minutes ago',
    },
    {
      type: 'info',
      message: 'Monthly analytics report is ready',
      time: '2 hours ago',
    },
  ]

  return (
    <div className="space-y-6">
      {/* Welcome Message */}
      <div>
        <h1 className="text-3xl font-bold">
          Welcome back, {user?.firstName || user?.username}!
        </h1>
        <p className="text-muted-foreground">
          Here's what's happening with your shops today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <Card key={index}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className="h-4 w-4 text-muted-foreground" />
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

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        {/* Recent Activities */}
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Recent Activities</CardTitle>
            <CardDescription>
              Latest activities across your shops
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentActivities.map((activity, index) => (
                <div key={index} className="flex items-center space-x-4">
                  <div className="w-2 h-2 bg-primary rounded-full"></div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {activity.description}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {activity.time}
                    </p>
                  </div>
                  <div className="text-sm font-medium">
                    {activity.amount}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Alerts & Notifications */}
        <Card className="col-span-3">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertCircle className="h-5 w-5" />
              <span>Alerts</span>
            </CardTitle>
            <CardDescription>
              Important notifications and alerts
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {alerts.map((alert, index) => (
                <div key={index} className="p-3 rounded-lg border bg-background">
                  <div className="flex items-start space-x-3">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      alert.type === 'warning' ? 'bg-yellow-500' : 'bg-blue-500'
                    }`}></div>
                    <div className="flex-1">
                      <p className="text-sm">{alert.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {alert.time}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}