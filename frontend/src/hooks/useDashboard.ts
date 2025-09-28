import { useQuery } from '@tanstack/react-query'
import { apiService } from '@/services/api'
import { useAuth } from '@/context/KeycloakAuthContext'

// Helper function to get date ranges
const getDateRange = (period: 'today' | 'week' | 'month' | 'year' = 'month') => {
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

  return {
    startDate: startDate.toISOString(),
    endDate: now.toISOString()
  }
}

// Custom hooks for dashboard data
export const useActiveShops = () => {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: ['shops', 'active'],
    queryFn: () => apiService.getActiveShops(),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 2
  })
}

export const useAllShops = (page = 0, size = 20) => {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: ['shops', 'all', page, size],
    queryFn: () => apiService.getShops(page, size),
    enabled: isAuthenticated,
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 2
  })
}

export const useSalesSummary = (shopId?: string, period: 'today' | 'week' | 'month' | 'year' = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  // Use first active shop if shopId not provided
  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'sales-summary', targetShopId, period],
    queryFn: () => targetShopId ? apiService.getSalesSummary(targetShopId, startDate, endDate) : null,
    enabled: isAuthenticated && !!targetShopId && (user?.roles?.includes('MANAGER') || user?.roles?.includes('OWNER') || user?.roles?.includes('SHOP_MANAGER') || user?.roles?.includes('TENANT_ADMIN')),
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 1
  })
}

export const useInvestmentROI = (shopId?: string, period: 'today' | 'week' | 'month' | 'year' = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  // Use first active shop if shopId not provided
  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'investment-roi', targetShopId, period],
    queryFn: () => targetShopId ? apiService.getInvestmentROI(targetShopId, startDate, endDate) : null,
    enabled: isAuthenticated && !!targetShopId && (user?.roles?.includes('OWNER') || user?.roles?.includes('INVESTOR') || user?.roles?.includes('TENANT_ADMIN')),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 1
  })
}

export const useRevenueAnalytics = (shopId?: string, period: 'today' | 'week' | 'month' | 'year' = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  // Use first active shop if shopId not provided
  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'revenue-analytics', targetShopId, period],
    queryFn: () => targetShopId ? apiService.getRevenueAnalytics(targetShopId, startDate, endDate) : null,
    enabled: isAuthenticated && !!targetShopId && (user?.roles?.includes('MANAGER') || user?.roles?.includes('OWNER') || user?.roles?.includes('SHOP_MANAGER') || user?.roles?.includes('TENANT_ADMIN')),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 1
  })
}

export const useFraudStatistics = (shopId?: string, period: 'today' | 'week' | 'month' | 'year' = 'month') => {
  const { isAuthenticated, user } = useAuth()
  const { startDate, endDate } = getDateRange(period)

  // Use first active shop if shopId not provided
  const { data: shops } = useActiveShops()
  const targetShopId = shopId || (shops && shops.length > 0 ? shops[0].id : null)

  return useQuery({
    queryKey: ['analytics', 'fraud-statistics', targetShopId, period],
    queryFn: () => targetShopId ? apiService.getFraudStatistics(targetShopId, startDate, endDate) : null,
    enabled: isAuthenticated && !!targetShopId && (user?.roles?.includes('MANAGER') || user?.roles?.includes('OWNER') || user?.roles?.includes('SHOP_MANAGER') || user?.roles?.includes('TENANT_ADMIN')),
    staleTime: 5 * 60 * 1000, // 5 minutes
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
    enabled: isAuthenticated && (user?.roles?.includes('MANAGER') || user?.roles?.includes('OWNER') || user?.roles?.includes('SHOP_MANAGER') || user?.roles?.includes('TENANT_ADMIN')),
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 1
  })
}

// Combined dashboard data hook for easy consumption
export const useDashboardData = (period: 'today' | 'week' | 'month' | 'year' = 'month') => {
  const { user } = useAuth()
  const shops = useActiveShops()
  const salesSummary = useSalesSummary(undefined, period)
  const investmentROI = useInvestmentROI(undefined, period)
  const revenueAnalytics = useRevenueAnalytics(undefined, period)
  const fraudStatistics = useFraudStatistics(undefined, period)

  const isLoading = shops.isLoading || salesSummary.isLoading ||
                   investmentROI.isLoading || revenueAnalytics.isLoading ||
                   fraudStatistics.isLoading

  const hasError = shops.error || salesSummary.error ||
                   investmentROI.error || revenueAnalytics.error ||
                   fraudStatistics.error

  return {
    user,
    shops: shops.data || [],
    salesSummary: salesSummary.data,
    investmentROI: investmentROI.data,
    revenueAnalytics: revenueAnalytics.data,
    fraudStatistics: fraudStatistics.data,
    isLoading,
    hasError,
    refetch: () => {
      shops.refetch()
      salesSummary.refetch()
      investmentROI.refetch()
      revenueAnalytics.refetch()
      fraudStatistics.refetch()
    }
  }
}