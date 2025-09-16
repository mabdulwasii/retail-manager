import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import {
  SalesSummary,
  RevenueAnalytics,
  InvestmentRoi,
  FraudStatistics
} from '@/hooks/useAnalytics'
import {
  DollarSignIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  ShoppingCartIcon,
  PieChartIcon,
  AlertTriangleIcon,
  ShieldCheckIcon,
  BarChart3Icon
} from 'lucide-react'

interface AnalyticsSummaryCardsProps {
  salesSummary?: SalesSummary | null
  revenueAnalytics?: RevenueAnalytics | null
  investmentRoi?: InvestmentRoi | null
  fraudStatistics?: FraudStatistics | null
  isLoading: boolean
}

export const AnalyticsSummaryCards: React.FC<AnalyticsSummaryCardsProps> = ({
  salesSummary,
  revenueAnalytics,
  investmentRoi,
  fraudStatistics,
  isLoading
}) => {
  const { formatCurrency } = useCurrency()

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[...Array(4)].map((_, index) => (
          <Card key={index}>
            <CardContent className="p-6">
              <div className="flex items-center justify-center">
                <LoadingSpinner size="md" />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  const getGrowthColor = (rate: number) => {
    if (rate > 0) return 'text-green-600'
    if (rate < 0) return 'text-red-600'
    return 'text-gray-600'
  }

  const getGrowthIcon = (rate: number) => {
    if (rate > 0) return <TrendingUpIcon className="h-4 w-4 text-green-600" />
    if (rate < 0) return <TrendingDownIcon className="h-4 w-4 text-red-600" />
    return <BarChart3Icon className="h-4 w-4 text-gray-600" />
  }

  const formatPercentage = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`
  }

  const getRiskLevel = (riskRate: number) => {
    if (riskRate >= 0.8) return { level: 'Critical', color: 'bg-red-100 text-red-800' }
    if (riskRate >= 0.5) return { level: 'High', color: 'bg-orange-100 text-orange-800' }
    if (riskRate >= 0.3) return { level: 'Medium', color: 'bg-yellow-100 text-yellow-800' }
    return { level: 'Low', color: 'bg-green-100 text-green-800' }
  }

  return (
    <div className="space-y-6">
      {/* Main Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Revenue */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Revenue
            </CardTitle>
            <DollarSignIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {salesSummary ? formatCurrency(salesSummary.totalRevenue) : '—'}
            </div>
            {revenueAnalytics && (
              <div className="flex items-center space-x-2 mt-1">
                {getGrowthIcon(revenueAnalytics.growthRate)}
                <span className={`text-xs ${getGrowthColor(revenueAnalytics.growthRate)}`}>
                  {formatPercentage(revenueAnalytics.growthRate)} from last period
                </span>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Total Transactions */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Transactions
            </CardTitle>
            <ShoppingCartIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {salesSummary ? salesSummary.totalTransactions.toLocaleString() : '—'}
            </div>
            {revenueAnalytics && (
              <div className="text-xs text-gray-500 mt-1">
                Previous: {revenueAnalytics.previousTransactions.toLocaleString()}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Average Transaction Value */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Avg Transaction
            </CardTitle>
            <BarChart3Icon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {salesSummary ? formatCurrency(salesSummary.averageTransactionValue) : '—'}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Per transaction value
            </div>
          </CardContent>
        </Card>

        {/* Investment ROI */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Investment ROI
            </CardTitle>
            <PieChartIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">
              {investmentRoi ? `${investmentRoi.roiPercentage.toFixed(1)}%` : '—'}
            </div>
            {investmentRoi && (
              <div className="text-xs text-gray-500 mt-1">
                Total invested: {formatCurrency(investmentRoi.totalInvestmentAmount)}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Secondary Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Revenue Growth */}
        {revenueAnalytics && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <TrendingUpIcon className="h-5 w-5" />
                <span>Revenue Growth</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Current Period:</span>
                  <span className="font-medium">{formatCurrency(revenueAnalytics.currentRevenue)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Previous Period:</span>
                  <span className="font-medium">{formatCurrency(revenueAnalytics.previousRevenue)}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">Growth Rate:</span>
                  <Badge className={`${getGrowthColor(revenueAnalytics.growthRate)} bg-transparent border`}>
                    {formatPercentage(revenueAnalytics.growthRate)}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Investment Summary */}
        {investmentRoi && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <PieChartIcon className="h-5 w-5" />
                <span>Investment Summary</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Total Investment:</span>
                  <span className="font-medium">{formatCurrency(investmentRoi.totalInvestmentAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Total Returns:</span>
                  <span className="font-medium">{formatCurrency(investmentRoi.totalDistributions)}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">ROI:</span>
                  <Badge className="bg-purple-100 text-purple-800">
                    {investmentRoi.roiPercentage.toFixed(1)}%
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Fraud Risk Assessment */}
        {fraudStatistics && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <ShieldCheckIcon className="h-5 w-5" />
                <span>Risk Assessment</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Total Assessments:</span>
                  <span className="font-medium">{fraudStatistics.totalAssessments.toLocaleString()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">High Risk:</span>
                  <span className="font-medium">{fraudStatistics.highRiskCount.toLocaleString()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Critical Risk:</span>
                  <span className="font-medium text-red-600">{fraudStatistics.criticalRiskCount.toLocaleString()}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">Risk Level:</span>
                  <Badge className={getRiskLevel(fraudStatistics.riskRate).color}>
                    {getRiskLevel(fraudStatistics.riskRate).level}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Status Information */}
      <div className="text-xs text-gray-500 text-center">
        {salesSummary && (
          <p>Data calculated at: {new Date(salesSummary.calculatedAt).toLocaleString()}</p>
        )}
      </div>
    </div>
  )
}