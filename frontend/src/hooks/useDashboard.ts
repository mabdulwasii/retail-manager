import { useMemo } from 'react'
import { useApi } from './useApi'
import { api } from '@/services/api'

export interface DashboardStats {
  totalRevenue: number
  totalShops: number
  totalProducts: number
  totalSales: number
  investmentROI: number
  activeUsers: number
  systemHealth: number
  revenueGrowth: number
}

export interface ShopPerformance {
  id: string
  name: string
  revenue: number
  salesCount: number
  growth: number
  status: 'excellent' | 'good' | 'attention' | 'poor'
}

export interface RecentActivity {
  id: string
  type: 'sale' | 'investment' | 'inventory' | 'analytics' | 'security' | 'system'
  description: string
  shop?: string
  amount?: string
  time: string
  severity?: 'info' | 'warning' | 'error' | 'success'
}

export interface Alert {
  id: string
  type: 'info' | 'warning' | 'error'
  message: string
  time: string
  action?: string
  actionUrl?: string
}

export function useDashboardStats() {
  const [state, refetch] = useApi<DashboardStats>(
    () => api.get('/api/analytics/dashboard-stats').then(res => res.data)
  )

  return {
    stats: state.data,
    loading: state.loading,
    error: state.error,
    refetch
  }
}

export function useShopPerformance() {
  const [state, refetch] = useApi<ShopPerformance[]>(
    () => api.get('/api/analytics/shop-performance').then(res => res.data)
  )

  return {
    shops: state.data || [],
    loading: state.loading,
    error: state.error,
    refetch
  }
}

export function useRecentActivities(limit = 10) {
  const [state, refetch] = useApi<RecentActivity[]>(
    () => api.get(`/api/activities/recent?limit=${limit}`).then(res => res.data)
  )

  return {
    activities: state.data || [],
    loading: state.loading,
    error: state.error,
    refetch
  }
}

export function useAlerts() {
  const [state, refetch] = useApi<Alert[]>(
    () => api.get('/api/alerts/active').then(res => res.data)
  )

  const alertsByType = useMemo(() => {
    if (!state.data) return { errors: [], warnings: [], infos: [] }

    return state.data.reduce(
      (acc, alert) => {
        acc[`${alert.type}s`].push(alert)
        return acc
      },
      { errors: [] as Alert[], warnings: [] as Alert[], infos: [] as Alert[] }
    )
  }, [state.data])

  return {
    alerts: state.data || [],
    alertsByType,
    loading: state.loading,
    error: state.error,
    refetch
  }
}