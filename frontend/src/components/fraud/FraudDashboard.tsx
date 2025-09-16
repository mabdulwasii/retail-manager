import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useAuth } from '@/contexts/AuthContext'
import {
  useFraudDetection,
  FraudStatistics,
  AlertSeverity,
  RiskLevel
} from '@/hooks/useFraudDetection'
import {
  ShieldAlertIcon,
  AlertTriangleIcon,
  TrendingUpIcon,
  ActivityIcon,
  EyeIcon,
  BarChart3Icon,
  FilterIcon,
  RefreshCwIcon,
  AlertCircleIcon,
  CheckCircleIcon,
  XCircleIcon
} from 'lucide-react'
import { FraudAlertList } from './FraudAlertList'
import { RiskAssessmentList } from './RiskAssessmentList'
import { FraudRuleList } from './FraudRuleList'

interface FraudDashboardProps {
  shopId?: string
  viewMode?: 'shop' | 'admin'
}

export const FraudDashboard: React.FC<FraudDashboardProps> = ({
  shopId,
  viewMode: propViewMode
}) => {
  const { user } = useAuth()
  const { getFraudStatistics, isLoading } = useFraudDetection()

  const getViewMode = () => {
    if (propViewMode) return propViewMode
    if (!user) return 'admin'

    const roles = user.roles || []
    if (roles.includes('TENANT_ADMIN')) return 'admin'
    if (roles.includes('SHOP_OWNER') || roles.includes('SHOP_MANAGER')) return 'shop'
    return 'admin'
  }

  const viewMode = getViewMode()
  const [statistics, setStatistics] = useState<FraudStatistics | null>(null)
  const [activeTab, setActiveTab] = useState('overview')
  const [isRefreshing, setIsRefreshing] = useState(false)

  useEffect(() => {
    fetchStatistics()
  }, [shopId])

  const fetchStatistics = async () => {
    try {
      setIsRefreshing(true)
      const stats = await getFraudStatistics(shopId)
      if (stats) {
        setStatistics(stats)
      }
    } catch (error) {
      console.error('Failed to fetch fraud statistics:', error)
    } finally {
      setIsRefreshing(false)
    }
  }

  const getSeverityColor = (severity: string) => {
    switch (severity.toLowerCase()) {
      case 'critical': return 'bg-red-100 text-red-800 border-red-200'
      case 'high': return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'medium': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'low': return 'bg-blue-100 text-blue-800 border-blue-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const getRiskLevelColor = (level: string) => {
    switch (level.toLowerCase()) {
      case 'critical': return 'text-red-600'
      case 'high': return 'text-orange-600'
      case 'medium': return 'text-yellow-600'
      case 'low': return 'text-green-600'
      default: return 'text-gray-600'
    }
  }

  const getPageTitle = () => {
    switch (viewMode) {
      case 'admin': return 'Fraud Detection Center'
      case 'shop': return 'Shop Security Dashboard'
      default: return 'Fraud Detection Dashboard'
    }
  }

  const getPageDescription = () => {
    switch (viewMode) {
      case 'admin': return 'Monitor fraud alerts and risk assessments across all shops'
      case 'shop': return 'Track security alerts and risk assessments for your shop'
      default: return 'Fraud detection and risk management platform'
    }
  }

  if (isLoading && !statistics) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold flex items-center space-x-2">
            <ShieldAlertIcon className="h-8 w-8 text-red-600" />
            <span>{getPageTitle()}</span>
          </h1>
          <p className="text-gray-600 mt-1">{getPageDescription()}</p>
        </div>
        <Button
          variant="outline"
          onClick={fetchStatistics}
          disabled={isRefreshing}
        >
          <RefreshCwIcon className={`h-4 w-4 mr-2 ${isRefreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Statistics Overview */}
      {statistics && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Total Alerts */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">
                Active Alerts
              </CardTitle>
              <AlertTriangleIcon className="h-4 w-4 text-orange-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-orange-600">
                {statistics.alerts.total}
              </div>
              <div className="flex items-center space-x-2 mt-2">
                <Badge className="bg-red-100 text-red-800 text-xs">
                  {statistics.alerts.critical} Critical
                </Badge>
                <Badge className="bg-orange-100 text-orange-800 text-xs">
                  {statistics.alerts.highSeverity} High
                </Badge>
              </div>
            </CardContent>
          </Card>

          {/* Pending Risk Assessments */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">
                Pending Reviews
              </CardTitle>
              <EyeIcon className="h-4 w-4 text-blue-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-blue-600">
                {statistics.riskAssessments.pending}
              </div>
              <div className="text-xs text-gray-500 mt-1">
                {statistics.riskAssessments.underReview} under review
              </div>
            </CardContent>
          </Card>

          {/* Active Rules */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">
                Active Rules
              </CardTitle>
              <ActivityIcon className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-green-600">
                {statistics.rules.total}
              </div>
              <div className="text-xs text-gray-500 mt-1">
                Fraud detection rules
              </div>
            </CardContent>
          </Card>

          {/* System Status */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-gray-600">
                System Status
              </CardTitle>
              <CheckCircleIcon className="h-4 w-4 text-green-600" />
            </CardHeader>
            <CardContent>
              <div className="text-lg font-semibold text-green-600 mb-2">
                Operational
              </div>
              <div className="text-xs text-gray-500">
                All systems running normally
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Main Content Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="overview" className="flex items-center space-x-2">
            <BarChart3Icon className="h-4 w-4" />
            <span>Overview</span>
          </TabsTrigger>
          <TabsTrigger value="alerts" className="flex items-center space-x-2">
            <AlertTriangleIcon className="h-4 w-4" />
            <span>Alerts</span>
          </TabsTrigger>
          <TabsTrigger value="assessments" className="flex items-center space-x-2">
            <EyeIcon className="h-4 w-4" />
            <span>Risk Assessments</span>
          </TabsTrigger>
          <TabsTrigger value="rules" className="flex items-center space-x-2">
            <FilterIcon className="h-4 w-4" />
            <span>Rules</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-6">
          {statistics ? (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Alert Type Distribution */}
              <Card>
                <CardHeader>
                  <CardTitle>Alert Distribution by Type</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {statistics.alerts.byType.map((item, index) => (
                      <div key={index} className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                          <span className="text-sm font-medium">{item.type}</span>
                        </div>
                        <Badge variant="outline">{item.count}</Badge>
                      </div>
                    ))}
                    {statistics.alerts.byType.length === 0 && (
                      <div className="text-center py-8 text-gray-500">
                        <AlertCircleIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                        <p>No alerts in selected period</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Risk Level Distribution */}
              <Card>
                <CardHeader>
                  <CardTitle>Risk Assessment Levels</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {statistics.riskAssessments.byRiskLevel.map((item, index) => (
                      <div key={index} className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className={`w-3 h-3 rounded-full ${
                            item.level.toLowerCase() === 'critical' ? 'bg-red-500' :
                            item.level.toLowerCase() === 'high' ? 'bg-orange-500' :
                            item.level.toLowerCase() === 'medium' ? 'bg-yellow-500' : 'bg-green-500'
                          }`}></div>
                          <span className={`text-sm font-medium ${getRiskLevelColor(item.level)}`}>
                            {item.level}
                          </span>
                        </div>
                        <Badge variant="outline">{item.count}</Badge>
                      </div>
                    ))}
                    {statistics.riskAssessments.byRiskLevel.length === 0 && (
                      <div className="text-center py-8 text-gray-500">
                        <TrendingUpIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                        <p>No risk assessments in selected period</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Rule Type Distribution */}
              <Card>
                <CardHeader>
                  <CardTitle>Rule Distribution by Type</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {statistics.rules.byType.map((item, index) => (
                      <div key={index} className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="w-3 h-3 rounded-full bg-green-500"></div>
                          <span className="text-sm font-medium">{item.type}</span>
                        </div>
                        <Badge variant="outline">{item.count}</Badge>
                      </div>
                    ))}
                    {statistics.rules.byType.length === 0 && (
                      <div className="text-center py-8 text-gray-500">
                        <FilterIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                        <p>No fraud rules configured</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* System Information */}
              <Card>
                <CardHeader>
                  <CardTitle>System Information</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Date Range:</span>
                      <span className="text-sm font-medium">
                        {new Date(statistics.dateRange.startDate).toLocaleDateString()} - {' '}
                        {new Date(statistics.dateRange.endDate).toLocaleDateString()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Total Active Rules:</span>
                      <span className="text-sm font-medium">{statistics.rules.total}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-600">Last Updated:</span>
                      <span className="text-sm font-medium">
                        {new Date().toLocaleTimeString()}
                      </span>
                    </div>
                    <div className="pt-4 border-t">
                      <div className="flex items-center space-x-2">
                        <CheckCircleIcon className="h-4 w-4 text-green-600" />
                        <span className="text-sm text-green-600 font-medium">
                          Fraud detection system operational
                        </span>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          ) : (
            <Card>
              <CardContent className="text-center py-12">
                <BarChart3Icon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">No Statistics Available</h3>
                <p className="text-gray-600">
                  Unable to load fraud detection statistics. Please try refreshing the page.
                </p>
                <Button variant="outline" className="mt-4" onClick={fetchStatistics}>
                  <RefreshCwIcon className="h-4 w-4 mr-2" />
                  Retry
                </Button>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="alerts">
          <FraudAlertList shopId={shopId} viewMode={viewMode} />
        </TabsContent>

        <TabsContent value="assessments">
          <RiskAssessmentList shopId={shopId} viewMode={viewMode} />
        </TabsContent>

        <TabsContent value="rules">
          <FraudRuleList shopId={shopId} viewMode={viewMode} />
        </TabsContent>
      </Tabs>
    </div>
  )
}