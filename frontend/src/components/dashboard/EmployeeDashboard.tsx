import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { useDashboardStats, useRecentActivities } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Package,
  TrendingUp,
  Clock,
  CheckCircle,
  AlertTriangle,
  Users,
  ShoppingCart,
  Truck
} from 'lucide-react'
import { Link } from 'react-router-dom'

export const EmployeeDashboard: React.FC = () => {
  const { user } = useAuth()
  const { stats, loading: statsLoading } = useDashboardStats()
  const { activities, loading: activitiesLoading } = useRecentActivities(8)

  const taskStats = [
    {
      title: 'Tasks Completed',
      value: '12',
      description: 'Today',
      icon: CheckCircle,
      color: 'text-green-600'
    },
    {
      title: 'Pending Tasks',
      value: '5',
      description: 'To complete',
      icon: Clock,
      color: 'text-orange-600'
    },
    {
      title: 'Products Handled',
      value: stats?.totalProducts ? `${stats.totalProducts}` : '0',
      description: 'This week',
      icon: Package,
      color: 'text-blue-600'
    },
    {
      title: 'Performance',
      value: '92%',
      description: 'Efficiency rating',
      icon: TrendingUp,
      color: 'text-purple-600'
    }
  ]

  const todayTasks = [
    {
      id: '1',
      title: 'Restock Electronics Section',
      description: 'Add new iPhone and Samsung models to display',
      priority: 'high',
      status: 'pending',
      estimatedTime: '45 min'
    },
    {
      id: '2',
      title: 'Update Price Tags',
      description: 'Apply new pricing for winter collection',
      priority: 'medium',
      status: 'in_progress',
      estimatedTime: '30 min'
    },
    {
      id: '3',
      title: 'Inventory Count - Grocery',
      description: 'Count fresh produce and update system',
      priority: 'high',
      status: 'pending',
      estimatedTime: '60 min'
    },
    {
      id: '4',
      title: 'Customer Service Training',
      description: 'Complete monthly customer service module',
      priority: 'low',
      status: 'completed',
      estimatedTime: '20 min'
    }
  ]

  const quickActions = [
    {
      title: 'Stock Check',
      description: 'Verify inventory levels',
      icon: Package,
      href: '/inventory',
      color: 'bg-blue-500 hover:bg-blue-600'
    },
    {
      title: 'Receive Shipment',
      description: 'Process incoming delivery',
      icon: Truck,
      href: '/inventory/shipment',
      color: 'bg-green-500 hover:bg-green-600'
    },
    {
      title: 'Assist Customer',
      description: 'Help with product inquiry',
      icon: Users,
      href: '/customers/assist',
      color: 'bg-purple-500 hover:bg-purple-600'
    },
    {
      title: 'Report Issue',
      description: 'Log equipment or inventory issue',
      icon: AlertTriangle,
      href: '/reports/issue',
      color: 'bg-orange-500 hover:bg-orange-600'
    }
  ]

  const notifications = [
    {
      type: 'info',
      message: 'New training module available: "Customer Service Excellence"',
      time: '2 hours ago'
    },
    {
      type: 'warning',
      message: 'Low stock alert: Wireless headphones (8 units remaining)',
      time: '4 hours ago'
    },
    {
      type: 'success',
      message: 'Task completed: Yesterday\'s inventory count approved',
      time: '1 day ago'
    }
  ]

  if (statsLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">
          Good {new Date().getHours() < 12 ? 'Morning' : new Date().getHours() < 18 ? 'Afternoon' : 'Evening'}, {user?.firstName || user?.username}!
        </h1>
        <p className="text-muted-foreground">
          Here's your task overview and productivity metrics.
        </p>
      </div>

      {/* Performance Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {taskStats.map((stat, index) => (
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
          <CardDescription>Common tasks and functions</CardDescription>
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

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Today's Tasks */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Today's Tasks</CardTitle>
            <CardDescription>
              Your assigned tasks for today
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {todayTasks.map((task) => (
                <div key={task.id} className="flex items-start space-x-4 p-4 border rounded-lg">
                  <div className={`w-3 h-3 rounded-full mt-2 ${
                    task.status === 'completed' ? 'bg-green-500' :
                    task.status === 'in_progress' ? 'bg-blue-500' :
                    'bg-gray-400'
                  }`}></div>
                  <div className="flex-1 space-y-2">
                    <div className="flex items-center justify-between">
                      <h4 className="font-medium">{task.title}</h4>
                      <span className={`px-2 py-1 text-xs rounded-full ${
                        task.priority === 'high' ? 'bg-red-100 text-red-800' :
                        task.priority === 'medium' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {task.priority}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">
                      {task.description}
                    </p>
                    <div className="flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">
                        Est. {task.estimatedTime}
                      </span>
                      {task.status === 'pending' && (
                        <Button size="sm" variant="outline">
                          Start Task
                        </Button>
                      )}
                      {task.status === 'in_progress' && (
                        <Button size="sm">
                          Complete
                        </Button>
                      )}
                      {task.status === 'completed' && (
                        <CheckCircle className="h-4 w-4 text-green-600" />
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Notifications */}
        <Card>
          <CardHeader>
            <CardTitle>Notifications</CardTitle>
            <CardDescription>
              Important updates and alerts
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {notifications.map((notification, index) => (
                <div key={index} className="p-3 rounded-lg border bg-background">
                  <div className="flex items-start space-x-3">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      notification.type === 'warning' ? 'bg-yellow-500' :
                      notification.type === 'success' ? 'bg-green-500' :
                      'bg-blue-500'
                    }`}></div>
                    <div className="flex-1">
                      <p className="text-sm">{notification.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {notification.time}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <Button variant="outline" className="w-full mt-4">
              View All Notifications
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
          <CardDescription>
            Your recent actions and system updates
          </CardDescription>
        </CardHeader>
        <CardContent>
          {activitiesLoading ? (
            <div className="flex items-center justify-center py-8">
              <LoadingSpinner />
            </div>
          ) : (
            <div className="grid md:grid-cols-2 gap-4">
              {activities.length > 0 ? (
                activities.map((activity) => (
                  <div key={activity.id} className="flex items-start space-x-4 p-3 bg-gray-50 rounded-lg">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      activity.type === 'inventory' ? 'bg-blue-500' :
                      activity.type === 'sale' ? 'bg-green-500' :
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
                        <p className="text-sm font-semibold text-blue-600">
                          {activity.amount}
                        </p>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-center text-muted-foreground py-4 col-span-2">
                  No recent activity
                </p>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Performance Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Weekly Performance</CardTitle>
          <CardDescription>
            Your productivity metrics for this week
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-4 gap-4">
            <div className="text-center p-4 bg-blue-50 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">95%</div>
              <div className="text-sm text-muted-foreground">Task Completion</div>
            </div>
            <div className="text-center p-4 bg-green-50 rounded-lg">
              <div className="text-2xl font-bold text-green-600">4.8</div>
              <div className="text-sm text-muted-foreground">Average Rating</div>
            </div>
            <div className="text-center p-4 bg-purple-50 rounded-lg">
              <div className="text-2xl font-bold text-purple-600">42</div>
              <div className="text-sm text-muted-foreground">Tasks Completed</div>
            </div>
            <div className="text-center p-4 bg-orange-50 rounded-lg">
              <div className="text-2xl font-bold text-orange-600">38h</div>
              <div className="text-sm text-muted-foreground">Hours Worked</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}