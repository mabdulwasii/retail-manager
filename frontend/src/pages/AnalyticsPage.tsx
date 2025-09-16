import React, { useState, useEffect, useCallback } from 'react'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { AnalyticsSummaryCards } from '@/components/analytics/AnalyticsSummaryCards'
import { AnalyticsCharts } from '@/components/analytics/AnalyticsCharts'
import { AnalyticsFilters } from '@/components/analytics/AnalyticsFilters'
import { useAuth } from '@/contexts/AuthContext'
import { useAnalytics, DateRange } from '@/hooks/useAnalytics'
import {
  BarChart3Icon,
  AlertTriangleIcon,
  RefreshCwIcon,
  DownloadIcon
} from 'lucide-react'

export const AnalyticsPage: React.FC = () => {
  const { user } = useAuth()
  const {
    isLoading,
    error,
    getAnalyticsSummary,
    clearAnalyticsCache
  } = useAnalytics()

  // Initialize with last 30 days
  const [dateRange, setDateRange] = useState<DateRange>(() => {
    const now = new Date()
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)

    return {
      startDate: thirtyDaysAgo.toISOString().split('T')[0] + 'T00:00:00',
      endDate: now.toISOString().split('T')[0] + 'T23:59:59'
    }
  })

  const [analyticsData, setAnalyticsData] = useState<{
    salesSummary: any
    revenueAnalytics: any
    investmentRoi: any
    fraudStatistics: any
  } | null>(null)

  // Mock shop ID - in real app, this would come from route params or context
  const shopId = user?.tenantId || 'default-shop'

  const loadAnalyticsData = useCallback(async () => {
    try {
      const data = await getAnalyticsSummary(shopId, dateRange)
      if (data) {
        setAnalyticsData(data)
      }
    } catch (err) {
      console.error('Failed to load analytics data:', err)
    }
  }, [getAnalyticsSummary, shopId, dateRange])

  useEffect(() => {
    loadAnalyticsData()
  }, [loadAnalyticsData])

  const handleRefresh = useCallback(async () => {
    await loadAnalyticsData()
  }, [loadAnalyticsData])

  const handleClearCache = useCallback(async () => {
    const success = await clearAnalyticsCache(shopId)
    if (success) {
      await loadAnalyticsData()
    }
  }, [clearAnalyticsCache, shopId, loadAnalyticsData])

  const handleExport = useCallback((type: string) => {
    // Mock export functionality
    console.log(`Exporting analytics data as ${type}`)
    // In real app, this would generate and download the file
  }, [])

  const handleDateRangeChange = useCallback((newDateRange: DateRange) => {
    setDateRange(newDateRange)
  }, [])

  const handleApplyFilters = useCallback(() => {
    loadAnalyticsData()
  }, [loadAnalyticsData])

  const handleClearFilters = useCallback(() => {
    // Date range is already cleared in AnalyticsFilters component
    loadAnalyticsData()
  }, [loadAnalyticsData])

  // Check user permissions for analytics access
  const hasAnalyticsAccess = user?.roles?.some(role =>
    ['SHOP_MANAGER', 'SHOP_OWNER', 'TENANT_ADMIN', 'INVESTOR'].includes(role)
  )

  if (!hasAnalyticsAccess) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <AlertTriangleIcon className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">Access Denied</h2>
          <p className="text-gray-600">
            You don't have permission to access analytics data.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8 space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between space-y-4 md:space-y-0">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 flex items-center space-x-3">
            <BarChart3Icon className="h-8 w-8 text-blue-600" />
            <span>Analytics Dashboard</span>
          </h1>
          <p className="text-gray-600 mt-1">
            Comprehensive business analytics and insights
          </p>
        </div>

        <div className="flex space-x-2">
          <Button
            variant="outline"
            onClick={handleClearCache}
            disabled={isLoading}
          >
            <RefreshCwIcon className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
            Clear Cache
          </Button>
          <Button
            onClick={() => handleExport('pdf')}
            disabled={isLoading}
          >
            <DownloadIcon className="h-4 w-4 mr-2" />
            Export Report
          </Button>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center space-x-2">
            <AlertTriangleIcon className="h-5 w-5 text-red-600" />
            <div>
              <h3 className="font-medium text-red-800">Error Loading Analytics</h3>
              <p className="text-red-700 text-sm">{error}</p>
            </div>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefresh}
            className="mt-3"
          >
            Retry
          </Button>
        </div>
      )}

      {/* Filters */}
      <AnalyticsFilters
        dateRange={dateRange}
        onDateRangeChange={handleDateRangeChange}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        isLoading={isLoading}
      />

      {/* Loading State */}
      {isLoading && !analyticsData && (
        <div className="flex justify-center items-center py-12">
          <LoadingSpinner size="lg" />
          <span className="ml-3 text-lg text-gray-600">Loading analytics data...</span>
        </div>
      )}

      {/* Analytics Content */}
      {!isLoading && analyticsData && (
        <>
          {/* Summary Cards */}
          <AnalyticsSummaryCards
            salesSummary={analyticsData.salesSummary}
            revenueAnalytics={analyticsData.revenueAnalytics}
            investmentRoi={analyticsData.investmentRoi}
            fraudStatistics={analyticsData.fraudStatistics}
            isLoading={isLoading}
          />

          {/* Charts */}
          <AnalyticsCharts
            salesSummary={analyticsData.salesSummary}
            revenueAnalytics={analyticsData.revenueAnalytics}
            investmentRoi={analyticsData.investmentRoi}
            fraudStatistics={analyticsData.fraudStatistics}
            isLoading={isLoading}
            onRefresh={handleRefresh}
            onExport={handleExport}
          />
        </>
      )}

      {/* Empty State */}
      {!isLoading && !analyticsData && !error && (
        <div className="text-center py-12">
          <BarChart3Icon className="h-16 w-16 mx-auto text-gray-400 mb-4" />
          <h2 className="text-2xl font-semibold text-gray-900 mb-2">No Analytics Data</h2>
          <p className="text-gray-600 mb-4">
            No analytics data available for the selected period.
          </p>
          <Button onClick={handleRefresh}>
            <RefreshCwIcon className="h-4 w-4 mr-2" />
            Refresh Data
          </Button>
        </div>
      )}

      {/* Footer Info */}
      <div className="text-center text-xs text-gray-500 pt-6 border-t">
        <p>
          Analytics data is cached for performance. Use "Clear Cache" to fetch the latest data.
        </p>
        <p className="mt-1">
          Selected period: {new Date(dateRange.startDate).toLocaleDateString()} - {new Date(dateRange.endDate).toLocaleDateString()}
        </p>
      </div>
    </div>
  )
}