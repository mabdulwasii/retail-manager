import React, { useState, useMemo } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { LineChart, BarChart, PieChart, AreaChart } from '@/components/charts'
import { useCurrency } from '@/hooks/useCurrency'
import {
  SalesSummary,
  RevenueAnalytics,
  InvestmentRoi,
  FraudStatistics
} from '@/hooks/useAnalytics'
import {
  RefreshCwIcon,
  DownloadIcon,
  TrendingUpIcon,
  BarChart3Icon,
  PieChartIcon
} from 'lucide-react'

interface AnalyticsChartsProps {
  salesSummary?: SalesSummary | null
  revenueAnalytics?: RevenueAnalytics | null
  investmentRoi?: InvestmentRoi | null
  fraudStatistics?: FraudStatistics | null
  isLoading: boolean
  onRefresh?: () => void
  onExport?: (type: string) => void
}

export const AnalyticsCharts: React.FC<AnalyticsChartsProps> = ({
  salesSummary,
  revenueAnalytics,
  investmentRoi,
  fraudStatistics,
  isLoading,
  onRefresh,
  onExport
}) => {
  const { formatCurrency } = useCurrency()
  const [activeTab, setActiveTab] = useState('revenue')

  // Generate mock historical data for charts (in real app, this would come from API)
  const historicalRevenueData = useMemo(() => {
    if (!revenueAnalytics) return []

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
    const currentRevenue = revenueAnalytics.currentRevenue
    const previousRevenue = revenueAnalytics.previousRevenue

    return months.map((month) => ({
      month,
      current: currentRevenue * (0.8 + Math.random() * 0.4),
      previous: previousRevenue * (0.8 + Math.random() * 0.4),
      transactions: Math.floor(Math.random() * 500 + 200)
    }))
  }, [revenueAnalytics])

  const investmentBreakdownData = useMemo(() => {
    if (!investmentRoi) return []

    return [
      { name: 'Initial Investment', value: investmentRoi.totalInvestmentAmount, color: '#3b82f6' },
      { name: 'Returns Distributed', value: investmentRoi.totalDistributions, color: '#10b981' },
      {
        name: 'Reinvested',
        value: Math.max(0, investmentRoi.totalInvestmentAmount * 0.3),
        color: '#f59e0b'
      }
    ]
  }, [investmentRoi])

  const riskDistributionData = useMemo(() => {
    if (!fraudStatistics) return []

    const lowRisk = fraudStatistics.totalAssessments - fraudStatistics.highRiskCount - fraudStatistics.criticalRiskCount
    return [
      { name: 'Low Risk', value: lowRisk, color: '#10b981' },
      { name: 'High Risk', value: fraudStatistics.highRiskCount, color: '#f59e0b' },
      { name: 'Critical Risk', value: fraudStatistics.criticalRiskCount, color: '#ef4444' }
    ]
  }, [fraudStatistics])

  const salesTrendData = useMemo(() => {
    if (!salesSummary) return []

    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    const avgDaily = salesSummary.totalRevenue / 7

    return days.map(day => ({
      day,
      revenue: avgDaily * (0.7 + Math.random() * 0.6),
      transactions: Math.floor(salesSummary.totalTransactions / 7 * (0.7 + Math.random() * 0.6))
    }))
  }, [salesSummary])

  const handleExport = (type: string) => {
    onExport?.(type)
  }

  return (
    <div className="space-y-6">
      {/* Header Actions */}
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">Analytics Charts</h2>
        <div className="flex space-x-2">
          <Button
            variant="outline"
            size="sm"
            onClick={onRefresh}
            disabled={isLoading}
          >
            <RefreshCwIcon className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleExport('pdf')}
          >
            <DownloadIcon className="h-4 w-4 mr-2" />
            Export
          </Button>
        </div>
      </div>

      {/* Chart Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="revenue" className="flex items-center space-x-2">
            <TrendingUpIcon className="h-4 w-4" />
            <span>Revenue</span>
          </TabsTrigger>
          <TabsTrigger value="sales" className="flex items-center space-x-2">
            <BarChart3Icon className="h-4 w-4" />
            <span>Sales</span>
          </TabsTrigger>
          <TabsTrigger value="investment" className="flex items-center space-x-2">
            <PieChartIcon className="h-4 w-4" />
            <span>Investment</span>
          </TabsTrigger>
          <TabsTrigger value="risk" className="flex items-center space-x-2">
            <PieChartIcon className="h-4 w-4" />
            <span>Risk</span>
          </TabsTrigger>
        </TabsList>

        {/* Revenue Analytics Tab */}
        <TabsContent value="revenue" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Revenue Trend</CardTitle>
              </CardHeader>
              <CardContent>
                <AreaChart
                  data={historicalRevenueData}
                  dataKeys={['current', 'previous']}
                  colors={['#3b82f6', '#94a3b8']}
                  height={300}
                  currency={true}
                  xAxisKey="month"
                  stacked={false}
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Revenue Comparison</CardTitle>
              </CardHeader>
              <CardContent>
                <BarChart
                  data={historicalRevenueData}
                  dataKeys={['current', 'previous']}
                  colors={['#3b82f6', '#94a3b8']}
                  height={300}
                  currency={true}
                  xAxisKey="month"
                />
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Transaction Volume Over Time</CardTitle>
            </CardHeader>
            <CardContent>
              <LineChart
                data={historicalRevenueData}
                dataKeys={['transactions']}
                colors={['#10b981']}
                height={250}
                xAxisKey="month"
              />
            </CardContent>
          </Card>
        </TabsContent>

        {/* Sales Analytics Tab */}
        <TabsContent value="sales" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Daily Sales Revenue</CardTitle>
              </CardHeader>
              <CardContent>
                <BarChart
                  data={salesTrendData}
                  dataKeys={['revenue']}
                  colors={['#3b82f6']}
                  height={300}
                  currency={true}
                  xAxisKey="day"
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Daily Transaction Count</CardTitle>
              </CardHeader>
              <CardContent>
                <LineChart
                  data={salesTrendData}
                  dataKeys={['transactions']}
                  colors={['#ef4444']}
                  height={300}
                  xAxisKey="day"
                />
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Sales Performance Overview</CardTitle>
            </CardHeader>
            <CardContent>
              <AreaChart
                data={salesTrendData}
                dataKeys={['revenue', 'transactions']}
                colors={['#3b82f6', '#10b981']}
                height={250}
                currency={false}
                xAxisKey="day"
              />
            </CardContent>
          </Card>
        </TabsContent>

        {/* Investment Analytics Tab */}
        <TabsContent value="investment" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Investment Breakdown</CardTitle>
              </CardHeader>
              <CardContent>
                <PieChart
                  data={investmentBreakdownData}
                  height={350}
                  currency={true}
                  colors={['#3b82f6', '#10b981', '#f59e0b']}
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Investment vs Returns</CardTitle>
              </CardHeader>
              <CardContent>
                <BarChart
                  data={[
                    {
                      category: 'Investment',
                      invested: investmentRoi?.totalInvestmentAmount || 0,
                      returns: investmentRoi?.totalDistributions || 0
                    }
                  ]}
                  dataKeys={['invested', 'returns']}
                  colors={['#3b82f6', '#10b981']}
                  height={350}
                  currency={true}
                  xAxisKey="category"
                />
              </CardContent>
            </Card>
          </div>

          {investmentRoi && (
            <Card>
              <CardHeader>
                <CardTitle>ROI Performance</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-center">
                  <div className="text-4xl font-bold text-purple-600 mb-2">
                    {investmentRoi.roiPercentage.toFixed(1)}%
                  </div>
                  <p className="text-gray-600">
                    Return on Investment for the selected period
                  </p>
                  <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-gray-600">Total Investment:</span>
                      <div className="font-semibold">{formatCurrency(investmentRoi.totalInvestmentAmount)}</div>
                    </div>
                    <div>
                      <span className="text-gray-600">Total Returns:</span>
                      <div className="font-semibold">{formatCurrency(investmentRoi.totalDistributions)}</div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Risk Analytics Tab */}
        <TabsContent value="risk" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <Card>
              <CardHeader>
                <CardTitle>Risk Distribution</CardTitle>
              </CardHeader>
              <CardContent>
                <PieChart
                  data={riskDistributionData}
                  height={350}
                  colors={['#10b981', '#f59e0b', '#ef4444']}
                />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Risk Assessment Summary</CardTitle>
              </CardHeader>
              <CardContent>
                <BarChart
                  data={riskDistributionData}
                  dataKeys={['value']}
                  colors={['#ef4444']}
                  height={350}
                  xAxisKey="name"
                />
              </CardContent>
            </Card>
          </div>

          {fraudStatistics && (
            <Card>
              <CardHeader>
                <CardTitle>Risk Metrics</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                  <div>
                    <div className="text-2xl font-bold">{fraudStatistics.totalAssessments.toLocaleString()}</div>
                    <div className="text-sm text-gray-600">Total Assessments</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-orange-600">{fraudStatistics.highRiskCount.toLocaleString()}</div>
                    <div className="text-sm text-gray-600">High Risk</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold text-red-600">{fraudStatistics.criticalRiskCount.toLocaleString()}</div>
                    <div className="text-sm text-gray-600">Critical Risk</div>
                  </div>
                  <div>
                    <div className="text-2xl font-bold">{(fraudStatistics.riskRate * 100).toFixed(1)}%</div>
                    <div className="text-sm text-gray-600">Risk Rate</div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}