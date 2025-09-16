import { renderHook, waitFor } from '@testing-library/react'
import { useDashboardStats, useShopPerformance, useRecentActivities, useAlerts } from '../useDashboard'
import { api } from '@/services/api'

// Mock the API service
jest.mock('@/services/api', () => ({
  api: {
    get: jest.fn(),
  },
}))

const mockApi = api as jest.Mocked<typeof api>

describe('useDashboard hooks', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('useDashboardStats', () => {
    it('should fetch dashboard stats successfully', async () => {
      const mockStats = {
        totalRevenue: 150000,
        totalShops: 5,
        totalProducts: 1200,
        totalSales: 450,
        investmentROI: 25.5,
        activeUsers: 125,
        systemHealth: 99.9,
        revenueGrowth: 12.5,
      }

      mockApi.get.mockResolvedValue({ data: mockStats })

      const { result } = renderHook(() => useDashboardStats())

      expect(result.current.loading).toBe(true)
      expect(result.current.stats).toBeNull()

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.stats).toEqual(mockStats)
        expect(result.current.error).toBeNull()
      })

      expect(mockApi.get).toHaveBeenCalledWith('/api/analytics/dashboard-stats')
    })

    it('should handle dashboard stats fetch error', async () => {
      const errorMessage = 'Failed to fetch stats'
      mockApi.get.mockRejectedValue(new Error(errorMessage))

      const { result } = renderHook(() => useDashboardStats())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.stats).toBeNull()
        expect(result.current.error).toBe('An unexpected error occurred')
      })
    })

    it('should provide refetch function', async () => {
      const mockStats = { totalRevenue: 150000 }
      mockApi.get.mockResolvedValue({ data: mockStats })

      const { result } = renderHook(() => useDashboardStats())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(mockApi.get).toHaveBeenCalledTimes(1)

      // Test refetch
      await result.current.refetch()
      expect(mockApi.get).toHaveBeenCalledTimes(2)
    })
  })

  describe('useShopPerformance', () => {
    it('should fetch shop performance data successfully', async () => {
      const mockShops = [
        {
          id: '1',
          name: 'Downtown Electronics',
          revenue: 125000,
          salesCount: 342,
          growth: 15,
          status: 'excellent' as const,
        },
        {
          id: '2',
          name: 'Fashion Store',
          revenue: 85000,
          salesCount: 287,
          growth: 8,
          status: 'good' as const,
        },
      ]

      mockApi.get.mockResolvedValue({ data: mockShops })

      const { result } = renderHook(() => useShopPerformance())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.shops).toEqual(mockShops)
        expect(result.current.error).toBeNull()
      })

      expect(mockApi.get).toHaveBeenCalledWith('/api/analytics/shop-performance')
    })

    it('should return empty array when no shops data', async () => {
      mockApi.get.mockResolvedValue({ data: null })

      const { result } = renderHook(() => useShopPerformance())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.shops).toEqual([])
      })
    })
  })

  describe('useRecentActivities', () => {
    it('should fetch recent activities with default limit', async () => {
      const mockActivities = [
        {
          id: '1',
          type: 'sale' as const,
          description: 'New sale completed',
          shop: 'Electronics Store',
          amount: '₦125,000',
          time: '5 minutes ago',
        },
        {
          id: '2',
          type: 'inventory' as const,
          description: 'Stock updated',
          shop: 'Fashion Store',
          amount: '50 units',
          time: '1 hour ago',
        },
      ]

      mockApi.get.mockResolvedValue({ data: mockActivities })

      const { result } = renderHook(() => useRecentActivities())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.activities).toEqual(mockActivities)
      })

      expect(mockApi.get).toHaveBeenCalledWith('/api/activities/recent?limit=10')
    })

    it('should fetch recent activities with custom limit', async () => {
      const mockActivities = []
      mockApi.get.mockResolvedValue({ data: mockActivities })

      const { result } = renderHook(() => useRecentActivities(5))

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(mockApi.get).toHaveBeenCalledWith('/api/activities/recent?limit=5')
    })

    it('should return empty array when no activities data', async () => {
      mockApi.get.mockResolvedValue({ data: null })

      const { result } = renderHook(() => useRecentActivities())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.activities).toEqual([])
      })
    })
  })

  describe('useAlerts', () => {
    it('should fetch alerts and categorize them', async () => {
      const mockAlerts = [
        {
          id: '1',
          type: 'error' as const,
          message: 'System error detected',
          time: '10 minutes ago',
          action: 'Investigate',
        },
        {
          id: '2',
          type: 'warning' as const,
          message: 'Low stock alert',
          time: '30 minutes ago',
          action: 'Restock',
        },
        {
          id: '3',
          type: 'info' as const,
          message: 'Maintenance scheduled',
          time: '1 hour ago',
        },
        {
          id: '4',
          type: 'error' as const,
          message: 'Another error',
          time: '2 hours ago',
        },
      ]

      mockApi.get.mockResolvedValue({ data: mockAlerts })

      const { result } = renderHook(() => useAlerts())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.alerts).toEqual(mockAlerts)

        // Check categorization
        expect(result.current.alertsByType.errors).toHaveLength(2)
        expect(result.current.alertsByType.warnings).toHaveLength(1)
        expect(result.current.alertsByType.infos).toHaveLength(1)
      })

      expect(mockApi.get).toHaveBeenCalledWith('/api/alerts/active')
    })

    it('should handle empty alerts response', async () => {
      mockApi.get.mockResolvedValue({ data: [] })

      const { result } = renderHook(() => useAlerts())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.alerts).toEqual([])
        expect(result.current.alertsByType.errors).toEqual([])
        expect(result.current.alertsByType.warnings).toEqual([])
        expect(result.current.alertsByType.infos).toEqual([])
      })
    })

    it('should handle null alerts response', async () => {
      mockApi.get.mockResolvedValue({ data: null })

      const { result } = renderHook(() => useAlerts())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.alerts).toEqual([])
      })
    })

    it('should handle alerts fetch error', async () => {
      mockApi.get.mockRejectedValue(new Error('Network error'))

      const { result } = renderHook(() => useAlerts())

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
        expect(result.current.alerts).toEqual([])
        expect(result.current.error).toBe('An unexpected error occurred')
      })
    })
  })
})