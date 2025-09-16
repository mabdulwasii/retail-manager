import { renderHook, act } from '@testing-library/react'
import { useAnalytics } from '../useAnalytics'
import { api } from '@/services/api'

// Mock the API service
jest.mock('@/services/api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn()
  }
}))

const mockApi = api as jest.Mocked<typeof api>

describe('useAnalytics', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('getSalesSummary', () => {
    it('should fetch sales summary successfully', async () => {
      const mockData = {
        shopId: 'shop-1',
        periodStart: '2024-01-01T00:00:00',
        periodEnd: '2024-01-31T23:59:59',
        totalRevenue: 50000,
        totalTransactions: 250,
        averageTransactionValue: 200,
        calculatedAt: '2024-01-31T12:00:00'
      }

      mockApi.get.mockResolvedValueOnce({ data: mockData })

      const { result } = renderHook(() => useAnalytics())

      let salesSummary
      await act(async () => {
        salesSummary = await result.current.getSalesSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(salesSummary).toEqual(mockData)
      expect(mockApi.get).toHaveBeenCalledWith(
        '/analytics/sales-summary?shopId=shop-1&startDate=2024-01-01T00%3A00%3A00&endDate=2024-01-31T23%3A59%3A59'
      )
      expect(result.current.isLoading).toBe(false)
      expect(result.current.error).toBeNull()
    })

    it('should handle errors properly', async () => {
      const errorMessage = 'Network error'
      mockApi.get.mockRejectedValueOnce(new Error(errorMessage))

      const { result } = renderHook(() => useAnalytics())

      let salesSummary
      await act(async () => {
        salesSummary = await result.current.getSalesSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(salesSummary).toBeNull()
      expect(result.current.error).toBe(errorMessage)
      expect(result.current.isLoading).toBe(false)
    })
  })

  describe('getRevenueAnalytics', () => {
    it('should fetch revenue analytics successfully', async () => {
      const mockData = {
        shopId: 'shop-1',
        periodStart: '2024-01-01T00:00:00',
        periodEnd: '2024-01-31T23:59:59',
        currentRevenue: 50000,
        previousRevenue: 40000,
        growthRate: 25.0,
        currentTransactions: 250,
        previousTransactions: 200,
        calculatedAt: '2024-01-31T12:00:00'
      }

      mockApi.get.mockResolvedValueOnce({ data: mockData })

      const { result } = renderHook(() => useAnalytics())

      let revenueAnalytics
      await act(async () => {
        revenueAnalytics = await result.current.getRevenueAnalytics('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(revenueAnalytics).toEqual(mockData)
      expect(mockApi.get).toHaveBeenCalledWith(
        '/analytics/revenue-analytics?shopId=shop-1&startDate=2024-01-01T00%3A00%3A00&endDate=2024-01-31T23%3A59%3A59'
      )
    })
  })

  describe('getInvestmentRoi', () => {
    it('should fetch investment ROI successfully', async () => {
      const mockData = {
        shopId: 'shop-1',
        periodStart: '2024-01-01T00:00:00',
        periodEnd: '2024-01-31T23:59:59',
        totalInvestmentAmount: 100000,
        totalDistributions: 15000,
        roiPercentage: 15.0,
        calculatedAt: '2024-01-31T12:00:00'
      }

      mockApi.get.mockResolvedValueOnce({ data: mockData })

      const { result } = renderHook(() => useAnalytics())

      let investmentRoi
      await act(async () => {
        investmentRoi = await result.current.getInvestmentRoi('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(investmentRoi).toEqual(mockData)
      expect(mockApi.get).toHaveBeenCalledWith(
        '/analytics/investment-roi?shopId=shop-1&startDate=2024-01-01T00%3A00%3A00&endDate=2024-01-31T23%3A59%3A59'
      )
    })
  })

  describe('getFraudStatistics', () => {
    it('should fetch fraud statistics successfully', async () => {
      const mockData = {
        shopId: 'shop-1',
        periodStart: '2024-01-01T00:00:00',
        periodEnd: '2024-01-31T23:59:59',
        totalAssessments: 1000,
        highRiskCount: 50,
        criticalRiskCount: 5,
        riskRate: 0.055,
        calculatedAt: '2024-01-31T12:00:00'
      }

      mockApi.get.mockResolvedValueOnce({ data: mockData })

      const { result } = renderHook(() => useAnalytics())

      let fraudStatistics
      await act(async () => {
        fraudStatistics = await result.current.getFraudStatistics('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(fraudStatistics).toEqual(mockData)
      expect(mockApi.get).toHaveBeenCalledWith(
        '/analytics/fraud-statistics?shopId=shop-1&startDate=2024-01-01T00%3A00%3A00&endDate=2024-01-31T23%3A59%3A59'
      )
    })
  })

  describe('clearAnalyticsCache', () => {
    it('should clear cache successfully', async () => {
      mockApi.post.mockResolvedValueOnce({})

      const { result } = renderHook(() => useAnalytics())

      let success
      await act(async () => {
        success = await result.current.clearAnalyticsCache('shop-1')
      })

      expect(success).toBe(true)
      expect(mockApi.post).toHaveBeenCalledWith('/analytics/clear-cache/shop-1')
      expect(result.current.error).toBeNull()
    })

    it('should handle cache clear errors', async () => {
      const errorMessage = 'Failed to clear cache'
      mockApi.post.mockRejectedValueOnce(new Error(errorMessage))

      const { result } = renderHook(() => useAnalytics())

      let success
      await act(async () => {
        success = await result.current.clearAnalyticsCache('shop-1')
      })

      expect(success).toBe(false)
      expect(result.current.error).toBe(errorMessage)
    })
  })

  describe('getAnalyticsSummary', () => {
    it('should fetch all analytics data in parallel', async () => {
      const mockSalesSummary = {
        shopId: 'shop-1',
        totalRevenue: 50000,
        totalTransactions: 250,
        averageTransactionValue: 200
      }

      const mockRevenueAnalytics = {
        shopId: 'shop-1',
        currentRevenue: 50000,
        previousRevenue: 40000,
        growthRate: 25.0
      }

      const mockInvestmentRoi = {
        shopId: 'shop-1',
        totalInvestmentAmount: 100000,
        totalDistributions: 15000,
        roiPercentage: 15.0
      }

      const mockFraudStatistics = {
        shopId: 'shop-1',
        totalAssessments: 1000,
        highRiskCount: 50,
        criticalRiskCount: 5,
        riskRate: 0.055
      }

      // Mock all API calls
      mockApi.get
        .mockResolvedValueOnce({ data: mockSalesSummary })
        .mockResolvedValueOnce({ data: mockRevenueAnalytics })
        .mockResolvedValueOnce({ data: mockInvestmentRoi })
        .mockResolvedValueOnce({ data: mockFraudStatistics })

      const { result } = renderHook(() => useAnalytics())

      let summary
      await act(async () => {
        summary = await result.current.getAnalyticsSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(summary).toEqual({
        salesSummary: mockSalesSummary,
        revenueAnalytics: mockRevenueAnalytics,
        investmentRoi: mockInvestmentRoi,
        fraudStatistics: mockFraudStatistics
      })

      // Verify all API calls were made
      expect(mockApi.get).toHaveBeenCalledTimes(4)
    })

    it('should handle partial failures gracefully', async () => {
      const mockSalesSummary = { shopId: 'shop-1', totalRevenue: 50000 }

      // Mock some successful and some failed calls
      mockApi.get
        .mockResolvedValueOnce({ data: mockSalesSummary })
        .mockRejectedValueOnce(new Error('Revenue analytics failed'))
        .mockResolvedValueOnce({ data: null })
        .mockRejectedValueOnce(new Error('Fraud statistics failed'))

      const { result } = renderHook(() => useAnalytics())

      let summary
      await act(async () => {
        summary = await result.current.getAnalyticsSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(summary).toEqual({
        salesSummary: mockSalesSummary,
        revenueAnalytics: null,
        investmentRoi: null,
        fraudStatistics: null
      })
    })
  })

  describe('loading state', () => {
    it('should manage loading state correctly', async () => {
      mockApi.get.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

      const { result } = renderHook(() => useAnalytics())

      expect(result.current.isLoading).toBe(false)

      act(() => {
        result.current.getSalesSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(result.current.isLoading).toBe(true)

      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 150))
      })

      expect(result.current.isLoading).toBe(false)
    })
  })
})