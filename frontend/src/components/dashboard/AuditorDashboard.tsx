import React, { useState } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { useAlerts } from '@/hooks/useDashboard'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Shield,
  Eye,
  FileText,
  AlertTriangle,
  CheckCircle,
  Clock,
  TrendingUp,
  RefreshCw
} from 'lucide-react'
import { Link } from 'react-router-dom'

interface AuditLog {
  id: string
  action: string
  user: string
  timestamp: string
  resource: string
  status: 'success' | 'warning' | 'error'
  details: string
  ipAddress?: string
}

interface ComplianceItem {
  id: string
  category: string
  requirement: string
  status: 'compliant' | 'non_compliant' | 'needs_review'
  lastChecked: string
  nextReview: string
  severity?: 'high' | 'medium' | 'low'
}

export const AuditorDashboard: React.FC = () => {
  const { user } = useAuth()
  const permissions = usePermissions()
  const { data: alerts, isLoading: alertsLoading } = useAlerts()
  const [isRefreshing, setIsRefreshing] = useState(false)

  const handleRefresh = () => {
    setIsRefreshing(true)
    setTimeout(() => setIsRefreshing(false), 1000)
  }

  const auditStats = [
    {
      title: 'Audit Logs',
      value: '2,847',
      description: 'This month',
      icon: FileText,
      color: 'text-blue-600'
    },
    {
      title: 'Compliance Score',
      value: '94%',
      description: 'Overall rating',
      icon: Shield,
      color: 'text-green-600'
    },
    {
      title: 'Active Issues',
      value: '3',
      description: 'Require attention',
      icon: AlertTriangle,
      color: 'text-red-600'
    },
    {
      title: 'Reviews Pending',
      value: '12',
      description: 'Awaiting review',
      icon: Clock,
      color: 'text-orange-600'
    }
  ]

  const recentAuditLogs: AuditLog[] = [
    {
      id: '1',
      action: 'Financial Transaction',
      user: 'john.doe@company.com',
      timestamp: new Date(Date.now() - 30 * 60000).toISOString(),
      resource: 'Sales Transaction #ST-2024-001',
      status: 'success',
      details: 'Large sale transaction completed successfully',
      ipAddress: '192.168.1.100'
    },
    {
      id: '2',
      action: 'User Permission Change',
      user: 'admin@company.com',
      timestamp: new Date(Date.now() - 75 * 60000).toISOString(),
      resource: 'User: jane.smith@company.com',
      status: 'warning',
      details: 'Elevated user permissions granted',
      ipAddress: '192.168.1.50'
    },
    {
      id: '3',
      action: 'Data Export',
      user: 'manager@company.com',
      timestamp: new Date(Date.now() - 140 * 60000).toISOString(),
      resource: 'Customer Database',
      status: 'success',
      details: 'Customer data exported for analysis'
    },
    {
      id: '4',
      action: 'Failed Login Attempt',
      user: 'unknown',
      timestamp: '2024-01-15T11:15:00Z',
      resource: 'Authentication System',
      status: 'error',
      details: 'Multiple failed login attempts detected'
    }
  ]

  const complianceStatus: ComplianceItem[] = [
    {
      id: '1',
      category: 'Data Protection',
      requirement: 'GDPR Compliance',
      status: 'compliant',
      lastChecked: new Date(Date.now() - 5 * 24 * 60 * 60000).toISOString().split('T')[0],
      nextReview: new Date(Date.now() + 85 * 24 * 60 * 60000).toISOString().split('T')[0],
      severity: 'high'
    },
    {
      id: '2',
      category: 'Financial',
      requirement: 'SOX Compliance',
      status: 'compliant',
      lastChecked: new Date(Date.now() - 10 * 24 * 60 * 60000).toISOString().split('T')[0],
      nextReview: new Date(Date.now() + 50 * 24 * 60 * 60000).toISOString().split('T')[0],
      severity: 'high'
    },
    {
      id: '3',
      category: 'Security',
      requirement: 'ISO 27001',
      status: 'needs_review',
      lastChecked: new Date(Date.now() - 25 * 24 * 60 * 60000).toISOString().split('T')[0],
      nextReview: new Date(Date.now() + 5 * 24 * 60 * 60000).toISOString().split('T')[0],
      severity: 'high'
    },
    {
      id: '4',
      category: 'Payment Processing',
      requirement: 'PCI DSS',
      status: 'compliant',
      lastChecked: new Date(Date.now() - 3 * 24 * 60 * 60000).toISOString().split('T')[0],
      nextReview: new Date(Date.now() + 87 * 24 * 60 * 60000).toISOString().split('T')[0],
      severity: 'high'
    }
  ]

  const quickActions = [
    {
      title: 'View Audit Logs',
      description: 'Browse audit logs',
      icon: Eye,
      href: '/audit',
      show: permissions.canViewAuditLogs()
    },
    {
      title: 'User Management',
      description: 'Review user activities',
      icon: FileText,
      href: '/users',
      show: permissions.canViewUsers()
    },
    {
      title: 'Role Management',
      description: 'Review roles',
      icon: Shield,
      href: '/admin/roles',
      show: permissions.canViewRoles()
    },
    {
      title: 'Sales Review',
      description: 'Review transactions',
      icon: AlertTriangle,
      href: '/sales',
      show: permissions.canViewSales()
    }
  ].filter(action => action.show)

  const riskAssessments = [
    {
      category: 'Financial Risk',
      level: 'Low',
      score: 2.1,
      trend: 'stable',
      color: 'text-green-600'
    },
    {
      category: 'Operational Risk',
      level: 'Medium',
      score: 4.5,
      trend: 'increasing',
      color: 'text-yellow-600'
    },
    {
      category: 'Compliance Risk',
      level: 'Low',
      score: 1.8,
      trend: 'decreasing',
      color: 'text-green-600'
    },
    {
      category: 'Security Risk',
      level: 'High',
      score: 7.2,
      trend: 'increasing',
      color: 'text-red-600'
    }
  ]

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Audit & Compliance
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Monitor system compliance and security.
          </p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" onClick={handleRefresh} disabled={isRefreshing}>
            <RefreshCw className={`mr-2 h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          {permissions.canViewAuditLogs() && (
            <Button variant="outline" asChild>
              <Link to="/audit">
                <Eye className="mr-2 h-4 w-4" />
                View Logs
              </Link>
            </Button>
          )}
          {permissions.canViewUsers() && (
            <Button asChild>
              <Link to="/users">
                <FileText className="mr-2 h-4 w-4" />
                View Users
              </Link>
            </Button>
          )}
        </div>
      </div>

      {/* Audit Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {auditStats.map((stat, index) => (
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
          <CardDescription>Common audit and compliance tasks</CardDescription>
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

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Recent Audit Logs */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Recent Audit Logs</CardTitle>
            <CardDescription>
              Latest system activities and events
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentAuditLogs.map((log) => (
                <div key={log.id} className="flex items-start space-x-4 p-4 border rounded-lg">
                  <div className={`w-3 h-3 rounded-full mt-2 ${
                    log.status === 'success' ? 'bg-green-500' :
                    log.status === 'warning' ? 'bg-yellow-500' :
                    'bg-red-500'
                  }`}></div>
                  <div className="flex-1 space-y-1">
                    <div className="flex justify-between items-start">
                      <p className="font-medium text-sm">{log.action}</p>
                      <span className="text-xs text-muted-foreground">
                        {new Date(log.timestamp).toLocaleString()}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">{log.details}</p>
                    <div className="flex justify-between text-xs">
                      <span>User: {log.user}</span>
                      <span>Resource: {log.resource}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            {permissions.canViewAuditLogs() && (
              <Button variant="outline" className="w-full mt-4" asChild>
                <Link to="/audit">View All Logs</Link>
              </Button>
            )}
          </CardContent>
        </Card>

        {/* System Alerts */}
        <Card>
          <CardHeader>
            <CardTitle>Security Alerts</CardTitle>
            <CardDescription>
              Critical security notifications
            </CardDescription>
          </CardHeader>
          <CardContent>
            {alertsLoading ? (
              <div className="flex items-center justify-center py-8">
                <LoadingSpinner />
              </div>
            ) : (
              <div className="space-y-4">
                {(alerts && alerts.length > 0) ? (
                  alerts.slice(0, 5).map((alert) => (
                    <div key={alert.id} className="p-3 rounded-lg border bg-background">
                      <div className="flex items-start space-x-3">
                        <div className={`w-2 h-2 rounded-full mt-2 ${
                          alert.type === 'error' ? 'bg-red-500' :
                          alert.type === 'warning' ? 'bg-yellow-500' :
                          'bg-blue-500'
                        }`}></div>
                        <div className="flex-1">
                          <p className="text-sm font-medium">{alert.message}</p>
                          <p className="text-xs text-muted-foreground mt-1">
                            {new Date(alert.timestamp).toLocaleString()}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-center text-muted-foreground py-4">
                    No active alerts
                  </p>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Compliance Status */}
      <Card>
        <CardHeader>
          <CardTitle>Compliance Status</CardTitle>
          <CardDescription>
            Current compliance requirements and their status
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-4">
            {complianceStatus.map((item) => (
              <div key={item.id} className="p-4 border rounded-lg">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="font-medium">{item.requirement}</h4>
                  <div className="flex items-center space-x-1">
                    {item.status === 'compliant' ? (
                      <CheckCircle className="h-4 w-4 text-green-600" />
                    ) : item.status === 'needs_review' ? (
                      <Clock className="h-4 w-4 text-yellow-600" />
                    ) : (
                      <AlertTriangle className="h-4 w-4 text-red-600" />
                    )}
                    <span className={`text-xs px-2 py-1 rounded-full ${
                      item.status === 'compliant' ? 'bg-green-100 text-green-800' :
                      item.status === 'needs_review' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {item.status.replace('_', ' ')}
                    </span>
                  </div>
                </div>
                <p className="text-sm text-muted-foreground mb-3">{item.category}</p>
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>Last checked: {new Date(item.lastChecked).toLocaleDateString()}</span>
                  <span>Next review: {new Date(item.nextReview).toLocaleDateString()}</span>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Risk Assessment */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <TrendingUp className="h-5 w-5" />
            <span>Risk Assessment</span>
          </CardTitle>
          <CardDescription>
            Current risk levels across different categories
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid md:grid-cols-2 gap-6">
            {riskAssessments.map((risk, index) => (
              <div key={index} className="p-4 bg-gray-50 rounded-lg">
                <div className="flex justify-between items-center mb-2">
                  <h4 className="font-medium">{risk.category}</h4>
                  <span className={`font-semibold ${risk.color}`}>
                    {risk.level}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="w-full bg-gray-200 rounded-full h-2 mr-4">
                      <div
                        className={`h-2 rounded-full ${
                          risk.level === 'Low' ? 'bg-green-500' :
                          risk.level === 'Medium' ? 'bg-yellow-500' :
                          'bg-red-500'
                        }`}
                        style={{ width: `${(risk.score / 10) * 100}%` }}
                      ></div>
                    </div>
                  </div>
                  <div className="text-sm">
                    <span className="font-medium">{risk.score}/10</span>
                    <span className={`ml-2 text-xs ${
                      risk.trend === 'increasing' ? 'text-red-600' :
                      risk.trend === 'decreasing' ? 'text-green-600' :
                      'text-gray-600'
                    }`}>
                      {risk.trend}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}