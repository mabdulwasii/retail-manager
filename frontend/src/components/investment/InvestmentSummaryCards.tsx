import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import {
  DollarSignIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  PieChartIcon,
  WalletIcon,
  BarChart3Icon,
  AlertCircleIcon,
  CheckCircleIcon
} from 'lucide-react'

interface InvestmentSummary {
  totalInvested: number
  totalProfitEarned: number
  totalWithdrawn: number
  availableBalance: number
  activeInvestments: number
  totalInvestments: number
  pendingDistributions: number
  totalROI: number
}

interface InvestmentSummaryCardsProps {
  summary: InvestmentSummary | null
  isLoading: boolean
}

export const InvestmentSummaryCards: React.FC<InvestmentSummaryCardsProps> = ({
  summary,
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

  if (!summary) {
    return (
      <div className="text-center py-8 text-gray-500">
        <AlertCircleIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
        <p>Unable to load investment summary</p>
      </div>
    )
  }

  const formatPercentage = (value: number) => {
    return `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`
  }

  const getROIColor = (roi: number) => {
    if (roi >= 15) return 'text-green-600'
    if (roi >= 5) return 'text-blue-600'
    if (roi >= 0) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getROIIcon = (roi: number) => {
    if (roi >= 0) return <TrendingUpIcon className="h-4 w-4" />
    return <TrendingDownIcon className="h-4 w-4" />
  }

  return (
    <div className="space-y-6">
      {/* Main Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Invested */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Invested
            </CardTitle>
            <DollarSignIcon className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {formatCurrency(summary.totalInvested)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Across {summary.totalInvestments} investment{summary.totalInvestments !== 1 ? 's' : ''}
            </div>
          </CardContent>
        </Card>

        {/* Total Profit Earned */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Profit
            </CardTitle>
            <TrendingUpIcon className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {formatCurrency(summary.totalProfitEarned)}
            </div>
            <div className="flex items-center space-x-2 mt-1">
              {getROIIcon(summary.totalROI)}
              <span className={`text-xs ${getROIColor(summary.totalROI)}`}>
                {formatPercentage(summary.totalROI)} ROI
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Available Balance */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Available Balance
            </CardTitle>
            <WalletIcon className="h-4 w-4 text-purple-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">
              {formatCurrency(summary.availableBalance)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Ready for withdrawal
            </div>
          </CardContent>
        </Card>

        {/* Active Investments */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Active Investments
            </CardTitle>
            <PieChartIcon className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {summary.activeInvestments}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Currently earning
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Secondary Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Investment Performance */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <BarChart3Icon className="h-5 w-5" />
              <span>Performance Overview</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Total Invested:</span>
                <span className="font-medium">{formatCurrency(summary.totalInvested)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Profit Earned:</span>
                <span className="font-medium text-green-600">{formatCurrency(summary.totalProfitEarned)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Total Withdrawn:</span>
                <span className="font-medium">{formatCurrency(summary.totalWithdrawn)}</span>
              </div>
              <div className="flex justify-between items-center pt-2 border-t">
                <span className="text-sm font-medium">ROI:</span>
                <Badge className={`${getROIColor(summary.totalROI)} bg-transparent border`}>
                  {formatPercentage(summary.totalROI)}
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Investment Status */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <CheckCircleIcon className="h-5 w-5" />
              <span>Investment Status</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Total Investments:</span>
                <span className="font-medium">{summary.totalInvestments}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Active:</span>
                <span className="font-medium text-green-600">{summary.activeInvestments}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Other Status:</span>
                <span className="font-medium">{summary.totalInvestments - summary.activeInvestments}</span>
              </div>
              <div className="flex justify-between items-center pt-2 border-t">
                <span className="text-sm font-medium">Active Rate:</span>
                <Badge className="bg-green-100 text-green-800">
                  {summary.totalInvestments > 0
                    ? ((summary.activeInvestments / summary.totalInvestments) * 100).toFixed(0)
                    : 0}%
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Pending Actions */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertCircleIcon className="h-5 w-5" />
              <span>Pending Actions</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Pending Distributions:</span>
                <Badge className={summary.pendingDistributions > 0 ? 'bg-orange-100 text-orange-800' : 'bg-gray-100 text-gray-800'}>
                  {summary.pendingDistributions}
                </Badge>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-gray-600">Available for Withdrawal:</span>
                <span className="font-medium text-purple-600">
                  {formatCurrency(summary.availableBalance)}
                </span>
              </div>
              {summary.pendingDistributions > 0 && (
                <div className="pt-2 border-t">
                  <p className="text-xs text-orange-600">
                    You have pending distributions to review
                  </p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* ROI Performance Indicator */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <TrendingUpIcon className="h-5 w-5" />
            <span>ROI Performance</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center">
            <div className={`text-4xl font-bold mb-2 ${getROIColor(summary.totalROI)}`}>
              {formatPercentage(summary.totalROI)}
            </div>
            <p className="text-gray-600 mb-4">
              Overall Return on Investment
            </p>
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <div className="font-medium">Excellent</div>
                <div className="text-gray-500">≥15%</div>
              </div>
              <div>
                <div className="font-medium">Good</div>
                <div className="text-gray-500">5-15%</div>
              </div>
              <div>
                <div className="font-medium">Fair</div>
                <div className="text-gray-500">0-5%</div>
              </div>
            </div>
            <div className="mt-4 text-xs text-gray-500">
              {summary.totalROI >= 15
                ? "🎉 Excellent performance! Your investments are generating strong returns."
                : summary.totalROI >= 5
                ? "👍 Good performance! Your investments are performing well."
                : summary.totalROI >= 0
                ? "📈 Fair performance. Consider reviewing your investment strategy."
                : "⚠️ Negative returns. Please review your investments carefully."
              }
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}