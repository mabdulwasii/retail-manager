import React from 'react'
import { useAuth } from '@/context/AuthContext'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Building,
  Users,
  Shield,
  Database,
  TrendingUp,
  AlertTriangle,
  Settings,
  BarChart3,
  Globe,
  Server,
  Activity,
  Eye
} from 'lucide-react'
import { Link } from 'react-router-dom'

export const AdminDashboard: React.FC = () => {
  const { user } = useAuth()

  // Mock data - would come from API
  const systemStats = [
    {
      title: 'Total Tenants',
      value: '127',
      description: 'Active organizations',
      icon: Building,
      trend: '+8 this month',
      color: 'text-blue-600'
    },
    {
      title: 'Total Shops',
      value: '1,543',
      description: 'Across all tenants',
      icon: Globe,
      trend: '+45 this week',
      color: 'text-green-600'
    },
    {
      title: 'Active Users',
      value: '12,847',
      description: 'System-wide users',
      icon: Users,
      trend: '+234 today',
      color: 'text-purple-600'
    },
    {
      title: 'System Health',
      value: '99.9%',
      description: 'Uptime this month',
      icon: Activity,
      trend: '0 incidents',
      color: 'text-emerald-600'
    }
  ]

  const recentActivities = [
    {
      type: 'tenant',
      description: 'New tenant registered: "Mega Retail Corp"',
      time: '5 minutes ago',
      severity: 'info'
    },
    {
      type: 'security',
      description: 'Suspicious login attempt blocked from IP 192.168.1.100',
      time: '15 minutes ago',
      severity: 'warning'
    },
    {
      type: 'system',
      description: 'Database backup completed successfully',
      time: '1 hour ago',
      severity: 'success'
    },
    {
      type: 'tenant',
      description: 'Tenant "Fashion Forward" upgraded to Enterprise plan',
      time: '2 hours ago',
      severity: 'info'
    }
  ]

  const systemAlerts = [
    {
      type: 'warning',
      message: 'Server CPU usage above 80% on node-3',
      time: '10 minutes ago',
      action: 'Scale Resources'
    },
    {
      type: 'info',
      message: 'Scheduled maintenance window starts in 2 hours',
      time: '30 minutes ago',
      action: 'View Details'
    },
    {
      type: 'error',
      message: 'Failed payment notification for tenant "StartupShop"',
      time: '1 hour ago',
      action: 'Contact Tenant'
    }
  ]

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            System Administration
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Here's your system overview.
          </p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" asChild>
            <Link to="/audit">
              <Eye className="mr-2 h-4 w-4" />
              Audit Logs
            </Link>
          </Button>
          <Button asChild>
            <Link to="/system-settings">
              <Settings className="mr-2 h-4 w-4" />
              System Settings
            </Link>
          </Button>
        </div>
      </div>

      {/* System Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {systemStats.map((stat, index) => (
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
          <CardDescription>Common administrative tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/tenants">
                <Building className="h-6 w-6 mb-2" />
                Manage Tenants
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/users">
                <Users className="h-6 w-6 mb-2" />
                User Management
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/security">
                <Shield className="h-6 w-6 mb-2" />
                Security Center
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/system-monitor">
                <Server className="h-6 w-6 mb-2" />
                System Monitor
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Recent System Activities */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Recent System Activities</CardTitle>
            <CardDescription>
              Latest system events and changes
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentActivities.map((activity, index) => (
                <div key={index} className="flex items-start space-x-4">
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    activity.severity === 'warning' ? 'bg-yellow-500' :
                    activity.severity === 'error' ? 'bg-red-500' :
                    activity.severity === 'success' ? 'bg-green-500' :
                    'bg-blue-500'
                  }`}></div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {activity.description}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {activity.time}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* System Alerts */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5" />
              <span>System Alerts</span>
            </CardTitle>
            <CardDescription>
              Important notifications requiring attention
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {systemAlerts.map((alert, index) => (
                <div key={index} className="p-3 rounded-lg border bg-background">
                  <div className="flex items-start space-x-3">
                    <div className={`w-2 h-2 rounded-full mt-2 ${
                      alert.type === 'warning' ? 'bg-yellow-500' :
                      alert.type === 'error' ? 'bg-red-500' :
                      'bg-blue-500'
                    }`}></div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{alert.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {alert.time}
                      </p>
                      <Button size="sm" variant="outline" className="mt-2">
                        {alert.action}
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* System Performance Charts */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>System Performance</CardTitle>
            <CardDescription>Resource utilization over time</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
              Performance charts would be rendered here
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Tenant Growth</CardTitle>
            <CardDescription>New tenant registrations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
              Growth analytics charts would be rendered here
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}