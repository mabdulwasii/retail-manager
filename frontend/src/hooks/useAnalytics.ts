import { useState, useCallback } from 'react'
import { api } from '@/services/api'

export interface SalesSummary {
  shopId: string
  periodStart: string
  periodEnd: string
  totalRevenue: number
  totalTransactions: number
  averageTransactionValue: number
  calculatedAt: string
}

export interface RevenueAnalytics {
  shopId: string
  periodStart: string
  periodEnd: string
  currentRevenue: number
  previousRevenue: number
  growthRate: number
  currentTransactions: number
  previousTransactions: number
  calculatedAt: string
}

export interface InvestmentRoi {
  shopId: string
  periodStart: string
  periodEnd: string
  totalInvestmentAmount: number
  totalDistributions: number
  roiPercentage: number
  calculatedAt: string
}

export interface FraudStatistics {
  shopId: string
  periodStart: string
  periodEnd: string
  totalAssessments: number
  highRiskCount: number
  criticalRiskCount: number
  riskRate: number
  calculatedAt: string
}

export interface DateRange {
  startDate: string
  endDate: string
}

export const useAnalytics = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const getSalesSummary = useCallback(async (shopId: string, dateRange: DateRange): Promise<SalesSummary | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({
        shopId,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })

      const response = await api.get(`/analytics/sales-summary?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch sales summary')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getRevenueAnalytics = useCallback(async (shopId: string, dateRange: DateRange): Promise<RevenueAnalytics | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({
        shopId,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })

      const response = await api.get(`/analytics/revenue-analytics?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch revenue analytics')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getInvestmentRoi = useCallback(async (shopId: string, dateRange: DateRange): Promise<InvestmentRoi | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({
        shopId,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })

      const response = await api.get(`/analytics/investment-roi?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch investment ROI')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getFraudStatistics = useCallback(async (shopId: string, dateRange: DateRange): Promise<FraudStatistics | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = new URLSearchParams({
        shopId,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })

      const response = await api.get(`/analytics/fraud-statistics?${params}`)
      return response.data
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch fraud statistics')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const clearAnalyticsCache = useCallback(async (shopId: string): Promise<boolean> => {
    try {
      setIsLoading(true)
      setError(null)

      await api.post(`/analytics/clear-cache/${shopId}`)
      return true
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to clear analytics cache')
      return false
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getAnalyticsSummary = useCallback(async (shopId: string, dateRange: DateRange) => {
    try {
      setIsLoading(true)
      setError(null)

      // Fetch all analytics data in parallel
      const [salesSummary, revenueAnalytics, investmentRoi, fraudStats] = await Promise.allSettled([
        getSalesSummary(shopId, dateRange),
        getRevenueAnalytics(shopId, dateRange),
        getInvestmentRoi(shopId, dateRange),
        getFraudStatistics(shopId, dateRange)
      ])

      return {
        salesSummary: salesSummary.status === 'fulfilled' ? salesSummary.value : null,
        revenueAnalytics: revenueAnalytics.status === 'fulfilled' ? revenueAnalytics.value : null,
        investmentRoi: investmentRoi.status === 'fulfilled' ? investmentRoi.value : null,
        fraudStatistics: fraudStats.status === 'fulfilled' ? fraudStats.value : null
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch analytics summary')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [getSalesSummary, getRevenueAnalytics, getInvestmentRoi, getFraudStatistics])

  return {
    isLoading,
    error,
    getSalesSummary,
    getRevenueAnalytics,
    getInvestmentRoi,
    getFraudStatistics,
    clearAnalyticsCache,
    getAnalyticsSummary
  }
}