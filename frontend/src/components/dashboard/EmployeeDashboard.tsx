import React, { useState, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { useInventorySummary, TimePeriod } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Package,
  AlertCircle,
  AlertTriangle,
  XCircle,
  Clock,
  TrendingUp,
  Loader2,
  RefreshCw
} from 'lucide-react'
import { Link } from 'react-router-dom'
import { ShopSelector } from '@/components/ui/shop-selector'


export const EmployeeDashboard: React.FC = () => {
  const { user } = useAuth()
  const permissions = usePermissions()
  const [period, setPeriod] = useState<TimePeriod>('today')
  const [selectedShopId, setSelectedShopId] = useState<string | undefined>(undefined)

  // Set selectedShopId once user is loaded
  useEffect(() => {
    if (user?.shopId && !selectedShopId) {
      setSelectedShopId(user.shopId)
    }
  }, [user?.shopId, selectedShopId])

  const { data: inventorySummary, isLoading, refetch } = useInventorySummary(selectedShopId)

  // Calculate stats from real inventory data
  const totalItems = inventorySummary?.totalItems || 0
  const lowStockItems = inventorySummary?.lowStockItems || 0
  const expiredItems = inventorySummary?.expiredItems || 0
  const expiringSoonItems = inventorySummary?.expiringSoonItems || 0
  const totalAlerts = lowStockItems + expiredItems + expiringSoonItems

  const inventoryStats = [
    {
      title: 'Total Items',
      value: totalItems.toString(),
      description: 'In inventory',
      icon: Package,
      color: 'text-blue-600'
    },
    {
      title: 'Low Stock',
      value: lowStockItems.toString(),
      description: 'Needs restocking',
      icon: AlertCircle,
      color: 'text-orange-600'
    },
    {
      title: 'Expired Items',
      value: expiredItems.toString(),
      description: 'Needs removal',
      icon: XCircle,
      color: 'text-red-600'
    },
    {
      title: 'Expiring Soon',
      value: expiringSoonItems.toString(),
      description: 'Within 30 days',
      icon: Clock,
      color: 'text-yellow-600'
    }
  ]

  // Priority tasks based on real data
  const priorityTasks = [
    ...(lowStockItems > 0 ? [{
      id: 'low-stock',
      title: 'Restock Low Inventory',
      description: `${lowStockItems} item(s) running low on stock`,
      priority: 'high' as const,
      icon: AlertCircle,
      link: '/inventory?filter=lowStock',
      color: 'border-orange-200 bg-orange-50'
    }] : []),
    ...(expiredItems > 0 ? [{
      id: 'expired',
      title: 'Remove Expired Items',
      description: `${expiredItems} expired item(s) need attention`,
      priority: 'high' as const,
      icon: XCircle,
      link: '/inventory?filter=expired',
      color: 'border-red-200 bg-red-50'
    }] : []),
    ...(expiringSoonItems > 0 ? [{
      id: 'expiring',
      title: 'Monitor Expiring Items',
      description: `${expiringSoonItems} item(s) expiring in 30 days`,
      priority: 'medium' as const,
      icon: Clock,
      link: '/inventory?filter=expiringSoon',
      color: 'border-yellow-200 bg-yellow-50'
    }] : [])
  ]


  const quickActions = [
    {
      title: 'View Inventory',
      description: 'Check all inventory',
      icon: Package,
      href: '/inventory',
      show: permissions.canViewInventory()
    },
    {
      title: 'View Products',
      description: 'Browse product catalog',
      icon: Package,
      href: '/products',
      show: permissions.canViewProducts()
    },
    {
      title: 'Low Stock Items',
      description: 'Items need restocking',
      icon: AlertCircle,
      href: '/inventory?filter=lowStock',
      show: permissions.canViewInventory() && lowStockItems > 0
    },
    {
      title: 'Expired Items',
      description: 'Remove expired stock',
      icon: XCircle,
      href: '/inventory?filter=expired',
      show: permissions.canViewInventory() && expiredItems > 0
    }
  ].filter(action => action.show)


  if (!selectedShopId || isLoading) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <div className="text-center">
              <Loader2 className="h-12 w-12 text-blue-500 mx-auto mb-4 animate-spin" />
              <h3 className="text-lg font-semibold mb-2">Loading Dashboard</h3>
              <p className="text-muted-foreground">
                {!selectedShopId ? 'Initializing...' : 'Fetching inventory data...'}
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
            Inventory Management
          </h1>
          <p className="text-muted-foreground">
            Welcome, {user?.firstName || user?.username}. Monitor and manage stock levels.
          </p>
        </div>
        <div className="flex space-x-2">
          <ShopSelector 
            value={selectedShopId || ''}
            onValueChange={setSelectedShopId}
            className="w-[200px]"
          />
          <Button variant="outline" onClick={() => refetch()} disabled={isLoading}>
            {isLoading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <RefreshCw className="mr-2 h-4 w-4" />
            )}
            Refresh
          </Button>
        </div>
      </div>

      {/* Inventory Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {inventoryStats.map((stat, index) => (
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

      {/* Priority Alerts */}
      {totalAlerts > 0 && (
        <Card className="border-orange-200 bg-orange-50">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2 text-orange-800">
              <AlertTriangle className="h-5 w-5" />
              <span>Action Required ({totalAlerts} items)</span>
            </CardTitle>
            <CardDescription>Inventory items that need your attention</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {priorityTasks.map((task) => (
              <div key={task.id} className={`flex items-center justify-between p-3 border rounded-lg ${task.color}`}>
                <div className="flex items-center space-x-3">
                  <task.icon className="h-5 w-5" />
                  <div>
                    <p className="text-sm font-medium">{task.title}</p>
                    <p className="text-xs text-muted-foreground">{task.description}</p>
                  </div>
                </div>
                <Button size="sm" variant="outline" asChild>
                  <Link to={task.link}>Review</Link>
                </Button>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* Quick Actions */}
      {quickActions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Common inventory tasks</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              {quickActions.map((action, index) => (
                <Button
                  key={index}
                  variant="outline"
                  className="h-20 flex-col hover:bg-primary/5 hover:border-primary/50 transition-all"
                  asChild
                >
                  <Link to={action.href}>
                    <action.icon className="h-6 w-6 mb-2" />
                    <span className="font-semibold">{action.title}</span>
                  </Link>
                </Button>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Inventory Health Summary */}
      {inventorySummary && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <TrendingUp className="h-5 w-5" />
              <span>Inventory Health</span>
            </CardTitle>
            <CardDescription>
              Overview of your inventory status
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <div className="text-center p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg">
                <div className="text-2xl font-bold text-blue-700">
                  {totalItems > 0 ? ((totalItems - lowStockItems - expiredItems) / totalItems * 100).toFixed(1) : 0}%
                </div>
                <div className="text-sm text-blue-600">Healthy Stock</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {totalItems - lowStockItems - expiredItems} items
                </div>
              </div>
              <div className="text-center p-4 bg-gradient-to-br from-orange-50 to-orange-100 rounded-lg">
                <div className="text-2xl font-bold text-orange-700">
                  {totalItems > 0 ? (lowStockItems / totalItems * 100).toFixed(1) : 0}%
                </div>
                <div className="text-sm text-orange-600">Low Stock</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {lowStockItems} items
                </div>
              </div>
              <div className="text-center p-4 bg-gradient-to-br from-red-50 to-red-100 rounded-lg">
                <div className="text-2xl font-bold text-red-700">
                  {expiredItems + expiringSoonItems}
                </div>
                <div className="text-sm text-red-600">Needs Attention</div>
                <div className="text-xs text-muted-foreground mt-1">
                  {expiredItems} expired, {expiringSoonItems} expiring
                </div>
              </div>
            </div>
            
            {permissions.canViewInventory() && (
              <div className="mt-4 flex space-x-2">
                <Button variant="outline" className="flex-1" asChild>
                  <Link to="/inventory">View All Inventory</Link>
                </Button>
                {permissions.canViewProducts() && (
                  <Button variant="outline" className="flex-1" asChild>
                    <Link to="/products">View Products</Link>
                  </Button>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}