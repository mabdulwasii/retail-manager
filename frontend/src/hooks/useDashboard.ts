import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/context/ManualAuthContext'
import { shopService } from '@/services/shopService'
import { analyticsService } from '@/services/analyticsService'
import { inventoryService } from '@/services/inventoryService'
import { expenseService } from '@/services/expenseService'

export type TimePeriod = 'today' | 'week' | 'month' | 'year'

const getDateRange = (period: TimePeriod = 'month') => {
  const now = new Date()
  let startDate: Date

  switch (period) {
    case 'today':
      startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate())
      break
    case 'week':
      startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      break
    case 'year':
      startDate = new Date(now.getFullYear(), 0, 1)
      break
    case 'month':
    default:
      startDate = new Date(now.getFullYear(), now.getMonth(), 1)
      break
  }

  const formatDate = (date: Date): string => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
  }

  return {
    startDate: formatDate(startDate),
    endDate: formatDate(now)
  }
}

// Custom hooks for dashboard data
export const useActiveShops = () => {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: ['shops', 'active'],
    queryFn: () => shopService.getActiveShops(),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 2
  })
}

export const useAllShops = (page = 0, size = 20) => {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: ['shops', 'all', page, size],
    queryFn: async () => {
      const response = await shopService.getShops({ page, size })
      return response.content || []
    },
    enabled: isAuthenticated,
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 2
  })
}

export const useSalesSummary = (shopId?: string, period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'sales-summary', targetShopId, period],
    queryFn: () => analyticsService.getSalesSummary(targetShopId!, startDate, endDate),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 1
  })
}

export const useInvestmentROI = (shopId?: string, period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'investment-roi', targetShopId, period],
    queryFn: () => analyticsService.getInvestmentROI(targetShopId!, startDate, endDate),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('OWNER') || user.roles.includes('INVESTOR') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 1
  })
}

export const useRevenueAnalytics = (shopId?: string, period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'revenue-analytics', targetShopId, period],
    queryFn: () => analyticsService.getRevenueAnalytics(targetShopId!, startDate, endDate),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 1
  })
}

export const useFraudStatistics = (shopId?: string, period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'fraud-statistics', targetShopId, period],
    queryFn: () => analyticsService.getFraudStatistics(targetShopId!, startDate, endDate),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1
  })
}

export const useInventorySummary = (shopId?: string) => {
  const { isAuthenticated, user } = useAuth()

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['inventory', 'summary', targetShopId],
    queryFn: () => inventoryService.getInventorySummary(targetShopId!),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 1
  })
}

export const useExpenseSummary = (shopId?: string, period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['expenses', 'summary', targetShopId, period],
    queryFn: () => expenseService.getExpenseSummary(targetShopId!, startDate, endDate),
    enabled: !!(isAuthenticated && targetShopId && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN') || user.roles.includes('ACCOUNTANT'))),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 1
  })
}

export const useAlerts = () => {
  const { isAuthenticated, user } = useAuth()

  return useQuery({
    queryKey: ['alerts'],
    queryFn: () => {
      // Mock data for alerts since we don't have a backend endpoint yet
      return Promise.resolve([
        {
          id: '1',
          type: 'warning',
          message: 'Unusual transaction pattern detected in Electronics department',
          timestamp: new Date().toISOString(),
          severity: 'medium'
        },
        {
          id: '2',
          type: 'error',
          message: 'Failed payment processing attempt',
          timestamp: new Date().toISOString(),
          severity: 'high'
        }
      ])
    },
    enabled: !!(isAuthenticated && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('MANAGER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 1
  })
}

// Hook to get performance data for all shops
export const useAllShopsPerformance = (period: TimePeriod = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { data: shops } = useActiveShops()
  const { startDate, endDate } = getDateRange(period)

  return useQuery({
    queryKey: ['analytics', 'all-shops-performance', period],
    queryFn: async () => {
      if (!shops || shops.length === 0) return []

      // Fetch analytics for each shop in parallel
      const performancePromises = shops.map(async (shop) => {
        try {
          const [salesSummary, revenueAnalytics] = await Promise.all([
            analyticsService.getSalesSummary(shop.id, startDate, endDate),
            analyticsService.getRevenueAnalytics(shop.id, startDate, endDate)
          ])

          return {
            shopId: shop.id,
            shopName: shop.name,
            revenue: salesSummary.totalRevenue,
            transactions: salesSummary.totalTransactions,
            averageTransaction: salesSummary.averageTransactionValue,
            growthRate: revenueAnalytics.growthRate,
            status: shop.status
          }
        } catch (error) {
          console.error(`Failed to fetch performance for shop ${shop.id}:`, error)
          return {
            shopId: shop.id,
            shopName: shop.name,
            revenue: 0,
            transactions: 0,
            averageTransaction: 0,
            growthRate: 0,
            status: shop.status
          }
        }
      })

      return Promise.all(performancePromises)
    },
    enabled: !!(isAuthenticated && shops && shops.length > 0 && user?.roles && (user.roles.includes('MANAGER') || user.roles.includes('OWNER') || user.roles.includes('TENANT_ADMIN'))),
    staleTime: 3 * 60 * 1000, // 3 minutes
    retry: 1
  })
}

// Combined dashboard data hook for easy consumption
export const useDashboardData = (period: TimePeriod = 'month', shopId?: string) => {
  const { user } = useAuth()
  const shops = useActiveShops()
  const salesSummary = useSalesSummary(shopId, period)
  const investmentROI = useInvestmentROI(shopId, period)
  const revenueAnalytics = useRevenueAnalytics(shopId, period)
  const fraudStatistics = useFraudStatistics(shopId, period)
  const inventorySummary = useInventorySummary(shopId)
  const expenseSummary = useExpenseSummary(shopId, period)

  const isLoading = shops.isLoading || salesSummary.isLoading ||
                   investmentROI.isLoading || revenueAnalytics.isLoading ||
                   fraudStatistics.isLoading || inventorySummary.isLoading ||
                   expenseSummary.isLoading

  const hasError = shops.error || salesSummary.error ||
                   investmentROI.error || revenueAnalytics.error ||
                   fraudStatistics.error || inventorySummary.error ||
                   expenseSummary.error

  return {
    user,
    shops: shops.data || [],
    salesSummary: salesSummary.data,
    investmentROI: investmentROI.data,
    revenueAnalytics: revenueAnalytics.data,
    fraudStatistics: fraudStatistics.data,
    inventorySummary: inventorySummary.data,
    expenseSummary: expenseSummary.data,
    isLoading,
    hasError,
    refetch: () => {
      shops.refetch()
      salesSummary.refetch()
      investmentROI.refetch()
      revenueAnalytics.refetch()
      fraudStatistics.refetch()
      inventorySummary.refetch()
      expenseSummary.refetch()
    }
  }
}