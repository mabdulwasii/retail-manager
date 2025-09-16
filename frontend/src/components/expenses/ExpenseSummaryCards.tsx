import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Badge } from '@/components/ui/badge'
import { ExpenseSummary } from '@/hooks/useExpenses'
import { useCurrency } from '@/hooks/useCurrency'
import {
  TrendingUpIcon,
  TrendingDownIcon,
  DollarSignIcon,
  CalendarIcon,
  PieChartIcon,
  AlertTriangleIcon
} from 'lucide-react'

interface ExpenseSummaryCardsProps {
  summary: ExpenseSummary | null
  isLoading: boolean
}

export const ExpenseSummaryCards: React.FC<ExpenseSummaryCardsProps> = ({
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
        <AlertTriangleIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
        <p>Unable to load expense summary</p>
      </div>
    )
  }

  // Calculate percentage of approved expenses
  const approvalRate = summary.totalExpenses > 0
    ? (summary.approvedExpenses / summary.totalExpenses) * 100
    : 0

  // Get top spending category
  const topCategory = summary.categoryBreakdown.length > 0
    ? summary.categoryBreakdown.reduce((prev, current) =>
        prev.amount > current.amount ? prev : current
      )
    : null

  return (
    <div className="space-y-6">
      {/* Main Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Total Expenses
            </CardTitle>
            <DollarSignIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{summary.totalExpenses}</div>
            <div className="text-xs text-gray-500 mt-1">
              {formatCurrency(summary.totalAmount)} total value
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Monthly Total
            </CardTitle>
            <CalendarIcon className="h-4 w-4 text-gray-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">
              {formatCurrency(summary.monthlyTotal)}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Current month expenses
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Pending Approval
            </CardTitle>
            <AlertTriangleIcon className="h-4 w-4 text-orange-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-orange-600">
              {summary.pendingApproval}
            </div>
            <div className="text-xs text-gray-500 mt-1">
              Awaiting approval
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-gray-600">
              Approval Rate
            </CardTitle>
            {approvalRate >= 80 ? (
              <TrendingUpIcon className="h-4 w-4 text-green-600" />
            ) : (
              <TrendingDownIcon className="h-4 w-4 text-red-600" />
            )}
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${
              approvalRate >= 80 ? 'text-green-600' : 'text-red-600'
            }`}>
              {approvalRate.toFixed(1)}%
            </div>
            <div className="text-xs text-gray-500 mt-1">
              {summary.approvedExpenses} of {summary.totalExpenses} approved
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Category Breakdown */}
      {summary.categoryBreakdown.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <PieChartIcon className="h-5 w-5" />
              <span>Spending by Category</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* Top Category Highlight */}
              {topCategory && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-medium text-blue-900">
                        Top Spending Category
                      </h3>
                      <p className="text-sm text-blue-700">
                        {topCategory.categoryName}
                      </p>
                    </div>
                    <div className="text-right">
                      <div className="text-xl font-bold text-blue-900">
                        {formatCurrency(topCategory.amount)}
                      </div>
                      <div className="text-sm text-blue-700">
                        {topCategory.count} expense{topCategory.count !== 1 ? 's' : ''}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Category List */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                {summary.categoryBreakdown
                  .sort((a, b) => b.amount - a.amount)
                  .map((category) => {
                    const percentage = (category.amount / summary.totalAmount) * 100
                    return (
                      <div
                        key={category.categoryId}
                        className="border rounded-lg p-3"
                      >
                        <div className="flex items-center justify-between mb-2">
                          <h4 className="font-medium text-sm truncate">
                            {category.categoryName}
                          </h4>
                          <Badge variant="secondary" className="text-xs">
                            {category.count}
                          </Badge>
                        </div>

                        <div className="space-y-1">
                          <div className="flex justify-between text-sm">
                            <span className="font-medium">
                              {formatCurrency(category.amount)}
                            </span>
                            <span className="text-gray-500">
                              {percentage.toFixed(1)}%
                            </span>
                          </div>

                          {/* Progress bar */}
                          <div className="w-full bg-gray-200 rounded-full h-2">
                            <div
                              className="bg-blue-600 h-2 rounded-full"
                              style={{ width: `${Math.min(percentage, 100)}%` }}
                            />
                          </div>
                        </div>
                      </div>
                    )
                  })}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

export default ExpenseSummaryCards