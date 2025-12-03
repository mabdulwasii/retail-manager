import { useState } from 'react'
import { api } from '@/services/api'

interface DateRange {
  startDate: string
  endDate: string
}

export const useAnalytics = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const getSalesSummary = async (shopId: string, dateRange: DateRange) => {
    setIsLoading(true)
    setError(null)
    try {
      const params = new URLSearchParams({
        shopId,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate
      })
      const data = await api.get(`/analytics/sales-summary?${params}`)
      setIsLoading(false)
      return data
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to fetch sales summary'
      setError(errorMessage)
      setIsLoading(false)
      throw new Error(errorMessage)
    }
  }

  const getAnalyticsSummary = async () => {
    setIsLoading(true)
    setError(null)
    try {
      // Fetch all analytics data in parallel
      const [sales, revenue, inventory, fraud] = await Promise.all([
        api.get('/analytics/sales-summary'),
        api.get('/analytics/revenue'),
        api.get('/analytics/inventory'),
        api.get('/analytics/fraud-statistics')
      ])

      setIsLoading(false)
      return {
        sales,
        revenue,
        inventory,
        fraud
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Failed to fetch analytics summary'
      setError(errorMessage)
      setIsLoading(false)
      throw new Error(errorMessage)
    }
  }

  return {
    getSalesSummary,
    getAnalyticsSummary,
    isLoading,
    error
  }
}
