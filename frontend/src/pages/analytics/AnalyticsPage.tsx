import React, { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { useAuth } from '@/context/UnifiedAuthContext'
import { analyticsService, SalesSummary, RevenueAnalytics, InvestmentRoi, FraudStatistics } from '@/services/analyticsService'
import { Permission } from '@/types/permissions'
import { CalendarIcon, TrendingUp, DollarSign, ShoppingCart, AlertTriangle, RefreshCw } from 'lucide-react'
import { format, subDays } from 'date-fns'
import { cn } from '@/lib/utils'
import { useQuery } from '@tanstack/react-query'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Alert, AlertDescription } from '@/components/ui/alert'

export const AnalyticsPage: React.FC = () => {
  const { user, hasAnyPermission } = useAuth()
  const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
    from: subDays(new Date(), 30),
    to: new Date(),
  })

  const shopId = user?.shopId || ''

  // Fetch sales summary
  const { data: salesSummary, isLoading: loadingSales, error: salesError, refetch: refetchSales } = useQuery({
    queryKey: ['analytics', 'sales-summary', shopId, dateRange],
    queryFn: () => analyticsService.getSalesSummary(
      shopId,
      dateRange.from.toISOString(),
      dateRange.to.toISOString()
    ),
    enabled: !!shopId && hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_VIEW]),
  })

  // Fetch revenue analytics
  const { data: revenueAnalytics, isLoading: loadingRevenue, refetch: refetchRevenue } = useQuery({
    queryKey: ['analytics', 'revenue', shopId, dateRange],
    queryFn: () => analyticsService.getRevenueAnalytics(
      shopId,
      dateRange.from.toISOString(),
      dateRange.to.toISOString()
    ),
    enabled: !!shopId && hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_VIEW]),
  })

  // Fetch investment ROI
  const { data: investmentRoi, isLoading: loadingInvestment, refetch: refetchInvestment } = useQuery({
    queryKey: ['analytics', 'investment-roi', shopId, dateRange],
    queryFn: () => analyticsService.getInvestmentROI(
      shopId,
      dateRange.from.toISOString(),
      dateRange.to.toISOString()
    ),
    enabled: !!shopId && hasAnyPermission([Permission.ANALYTICS_INVESTMENT_VIEW, Permission.ANALYTICS_VIEW]),
  })

  // Fetch fraud statistics
  const { data: fraudStats, isLoading: loadingFraud, refetch: refetchFraud } = useQuery({
    queryKey: ['analytics', 'fraud', shopId, dateRange],
    queryFn: () => analyticsService.getFraudStatistics(
      shopId,
      dateRange.from.toISOString(),
      dateRange.to.toISOString()
    ),
    enabled: !!shopId && hasAnyPermission([Permission.FRAUD_VIEW]),
  })

  const handleRefreshAll = () => {
    refetchSales()
    refetchRevenue()
    refetchInvestment()
    refetchFraud()
  }

  if (!shopId) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Analytics</h1>
        <Alert>
          <AlertDescription>
            No shop assigned. Please contact your administrator.
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount)
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Analytics Dashboard</h1>
        <div className="flex gap-3">
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="outline" className={cn("justify-start text-left font-normal")}>
                <CalendarIcon className="mr-2 h-4 w-4" />
                {dateRange.from && dateRange.to
                  ? `${format(dateRange.from, 'MMM dd, yyyy')} - ${format(dateRange.to, 'MMM dd, yyyy')}`
                  : 'Select date range'}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="end">
              <div className="p-3 space-y-2">
                <div className="flex gap-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setDateRange({ from: subDays(new Date(), 7), to: new Date() })}
                  >
                    Last 7 days
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setDateRange({ from: subDays(new Date(), 30), to: new Date() })}
                  >
                    Last 30 days
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setDateRange({ from: subDays(new Date(), 90), to: new Date() })}
                  >
                    Last 90 days
                  </Button>
                </div>
              </div>
            </PopoverContent>
          </Popover>
          <Button onClick={handleRefreshAll} variant="outline" size="icon">
            <RefreshCw className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Sales Analytics Section */}
      {hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_VIEW]) && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {loadingSales ? (
                <LoadingSpinner size="sm" />
              ) : salesError ? (
                <p className="text-sm text-red-500">Error loading data</p>
              ) : (
                <div>
                  <div className="text-2xl font-bold">{formatCurrency(salesSummary?.totalRevenue || 0)}</div>
                  <p className="text-xs text-muted-foreground">
                    From {salesSummary?.totalTransactions || 0} transactions
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Average Transaction</CardTitle>
              <ShoppingCart className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {loadingSales ? (
                <LoadingSpinner size="sm" />
              ) : salesError ? (
                <p className="text-sm text-red-500">Error loading data</p>
              ) : (
                <div>
                  <div className="text-2xl font-bold">
                    {formatCurrency(salesSummary?.averageTransactionValue || 0)}
                  </div>
                  <p className="text-xs text-muted-foreground">Per transaction</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Growth Rate</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {loadingRevenue ? (
                <LoadingSpinner size="sm" />
              ) : (
                <div>
                  <div className={cn(
                    "text-2xl font-bold",
                    (revenueAnalytics?.growthRate || 0) >= 0 ? "text-green-600" : "text-red-600"
                  )}>
                    {revenueAnalytics?.growthRate?.toFixed(2) || 0}%
                  </div>
                  <p className="text-xs text-muted-foreground">
                    vs. previous period
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Revenue Comparison */}
      {hasAnyPermission([Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_VIEW]) && (
        <Card>
          <CardHeader>
            <CardTitle>Revenue Comparison</CardTitle>
            <CardDescription>Current period vs. previous period</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingRevenue ? (
              <LoadingSpinner />
            ) : (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Current Period</span>
                  <span className="text-lg font-bold">{formatCurrency(revenueAnalytics?.currentRevenue || 0)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Previous Period</span>
                  <span className="text-lg">{formatCurrency(revenueAnalytics?.previousRevenue || 0)}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">Transactions</span>
                  <span className="text-sm">
                    {revenueAnalytics?.currentTransactions || 0} vs {revenueAnalytics?.previousTransactions || 0}
                  </span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Investment ROI */}
      {hasAnyPermission([Permission.ANALYTICS_INVESTMENT_VIEW, Permission.ANALYTICS_VIEW]) && (
        <Card>
          <CardHeader>
            <CardTitle>Investment ROI</CardTitle>
            <CardDescription>Return on investment analysis</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingInvestment ? (
              <LoadingSpinner />
            ) : (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Total Investment</span>
                  <span className="text-lg font-bold">{formatCurrency(investmentRoi?.totalInvestmentAmount || 0)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Total Distributions</span>
                  <span className="text-lg">{formatCurrency(investmentRoi?.totalDistributions || 0)}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">ROI Percentage</span>
                  <span className={cn(
                    "text-lg font-bold",
                    (investmentRoi?.roiPercentage || 0) >= 0 ? "text-green-600" : "text-red-600"
                  )}>
                    {investmentRoi?.roiPercentage?.toFixed(2) || 0}%
                  </span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Fraud Statistics */}
      {hasAnyPermission([Permission.FRAUD_VIEW]) && (
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0">
            <div>
              <CardTitle>Fraud Detection</CardTitle>
              <CardDescription>Risk assessment overview</CardDescription>
            </div>
            <AlertTriangle className="h-5 w-5 text-yellow-500" />
          </CardHeader>
          <CardContent>
            {loadingFraud ? (
              <LoadingSpinner />
            ) : (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Total Assessments</span>
                  <span className="text-lg">{fraudStats?.totalAssessments || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium text-yellow-600">High Risk</span>
                  <span className="text-lg text-yellow-600 font-semibold">{fraudStats?.highRiskCount || 0}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium text-red-600">Critical Risk</span>
                  <span className="text-lg text-red-600 font-bold">{fraudStats?.criticalRiskCount || 0}</span>
                </div>
                <div className="flex justify-between items-center pt-2 border-t">
                  <span className="text-sm font-medium">Overall Risk Rate</span>
                  <span className={cn(
                    "text-lg font-bold",
                    (fraudStats?.riskRate || 0) > 10 ? "text-red-600" : "text-green-600"
                  )}>
                    {fraudStats?.riskRate?.toFixed(2) || 0}%
                  </span>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
