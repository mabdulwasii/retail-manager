import { renderHook, act } from '@testing-library/react'
import { useAnalytics } from '../useAnalytics'
import { api } from '@/services/api'

// Mock the API service
jest.mock('@/services/api', () => ({
  api: {
    get: jest.fn()
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

      mockApi.get.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useAnalytics())

      let salesSummary
      await act(async () => {
        salesSummary = await result.current.getSalesSummary('shop-1', {
          startDate: '2024-01-01T00:00:00',
          endDate: '2024-01-31T23:59:59'
        })
      })

      expect(salesSummary).toEqual(mockData)
      expect(result.current.isLoading).toBe(false)
      expect(result.current.error).toBeNull()
    })
  })

  describe('getAnalyticsSummary', () => {
    it('should fetch all analytics data in parallel', async () => {
      const mockSales = { shopId: 'shop-1', totalRevenue: 50000 }
      const mockRevenue = { currentRevenue: 50000, growthRate: 25 }
      const mockInventory = { totalAssessments: 1000 }
      const mockFraud = { riskRate: 0.055 }

      // Mock all parallel API calls
      mockApi.get
        .mockResolvedValueOnce(mockSales)
        .mockResolvedValueOnce(mockRevenue)
        .mockResolvedValueOnce(mockInventory)
        .mockResolvedValueOnce(mockFraud)

      const { result } = renderHook(() => useAnalytics())

      let summary
      await act(async () => {
        summary = await result.current.getAnalyticsSummary()
      })

      expect(summary).toEqual({
        sales: mockSales,
        revenue: mockRevenue,
        inventory: mockInventory,
        fraud: mockFraud
      })

      expect(mockApi.get).toHaveBeenCalledTimes(4)
    })
  })
})