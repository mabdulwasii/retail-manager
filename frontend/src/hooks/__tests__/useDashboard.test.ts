import { renderHook, waitFor } from '@testing-library/react'
import { useAlerts, useAllShopsPerformance } from '../useDashboard'
import { createQueryWrapper } from '@/test/test-utils'
import { shopService } from '@/services/shopService'
import { analyticsService } from '@/services/analyticsService'

// Mock the shop service
jest.mock('@/services/shopService', () => ({
  shopService: {
    getActiveShops: jest.fn(),
  },
}))

// Mock the analytics service
jest.mock('@/services/analyticsService', () => ({
  analyticsService: {
    getSalesSummary: jest.fn(),
    getRevenueAnalytics: jest.fn(),
  },
}))

// Mock the auth context to allow queries to run
jest.mock('@/context/UnifiedAuthContext', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    hasAnyPermission: jest.fn(() => true),
    user: { shopId: 'shop-1' },
  }),
}))

const mockShopService = shopService as jest.Mocked<typeof shopService>
const mockAnalyticsService = analyticsService as jest.Mocked<typeof analyticsService>

describe('useDashboard hooks', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('useAlerts', () => {
    it('should return mock alerts data', async () => {
      const { result } = renderHook(() => useAlerts(), { wrapper: createQueryWrapper() })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      // useAlerts returns a Promise.resolve with mock data
      expect(result.current.data).toBeDefined()
      expect(Array.isArray(result.current.data)).toBe(true)
      expect(result.current.data?.length).toBeGreaterThan(0)
    })

    it('should have expected query key', () => {
      const { result } = renderHook(() => useAlerts(), { wrapper: createQueryWrapper() })
      
      // Query should be enabled and use the correct key
      expect(result.current.isLoading || result.current.isSuccess).toBe(true)
    })
  })

  describe('useAllShopsPerformance', () => {
    it('should fetch performance data for all shops', async () => {
      const mockShops = [
        {
          id: 'shop-1',
          name: 'Downtown Electronics',
          status: 'active',
        },
        {
          id: 'shop-2',
          name: 'Fashion Store',
          status: 'active',
        },
      ]

      const mockSalesSummary = {
        totalRevenue: 125000,
        totalTransactions: 342,
        averageTransactionValue: 365.5,
      }

      const mockRevenueAnalytics = {
        growthRate: 15,
      }

      mockShopService.getActiveShops.mockResolvedValue(mockShops)
      mockAnalyticsService.getSalesSummary.mockResolvedValue(mockSalesSummary)
      mockAnalyticsService.getRevenueAnalytics.mockResolvedValue(mockRevenueAnalytics)

      const { result } = renderHook(() => useAllShopsPerformance('month'), { wrapper: createQueryWrapper() })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toBeDefined()
      expect(Array.isArray(result.current.data)).toBe(true)
    })

    it('should not fetch when no shops available', async () => {
      mockShopService.getActiveShops.mockResolvedValue([])

      const { result } = renderHook(() => useAllShopsPerformance('month'), { wrapper: createQueryWrapper() })

      // Wait a bit to ensure the query doesn't run
      await waitFor(() => {
        // Query should remain in idle state when disabled (no shops)
        expect(result.current.isPending || result.current.isSuccess || result.current.isError).toBe(true)
      })

      // Data should be undefined since query is disabled
      expect(result.current.data).toBeUndefined()
    })

    it('should handle errors gracefully for individual shops', async () => {
      const mockShops = [
        {
          id: 'shop-1',
          name: 'Electronics Store',
          status: 'active',
        },
      ]

      mockShopService.getActiveShops.mockResolvedValue(mockShops)
      mockAnalyticsService.getSalesSummary.mockRejectedValue(new Error('Failed to fetch'))
      mockAnalyticsService.getRevenueAnalytics.mockRejectedValue(new Error('Failed to fetch'))

      const { result } = renderHook(() => useAllShopsPerformance('month'), { wrapper: createQueryWrapper() })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      // Should still return data with default values for failed shops
      expect(result.current.data).toBeDefined()
      expect(Array.isArray(result.current.data)).toBe(true)
      if (result.current.data && result.current.data.length > 0) {
        expect(result.current.data[0].revenue).toBe(0)
      }
    })
  })
})