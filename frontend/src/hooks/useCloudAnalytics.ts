import { useQuery, UseQueryResult } from '@tanstack/react-query';
import cloudAnalyticsService, {
  RevenueAnalytics,
  SalesMetrics,
  TopProductsAnalytics,
  ShopPerformanceAnalytics,
  AnalyticsFilters,
  DateRangePeriod,
} from '@/services/cloudAnalyticsService';

/**
 * Custom hooks for cloud analytics data fetching
 */

const ANALYTICS_KEYS = {
  revenue: (filters: AnalyticsFilters) => ['cloud-analytics', 'revenue', filters] as const,
  sales: (filters: AnalyticsFilters) => ['cloud-analytics', 'sales', filters] as const,
  topProducts: (filters: AnalyticsFilters, limit: number) =>
    ['cloud-analytics', 'top-products', filters, limit] as const,
  shopPerformance: (filters: AnalyticsFilters) =>
    ['cloud-analytics', 'shop-performance', filters] as const,
};

/**
 * Hook to fetch revenue analytics
 */
export const useRevenueAnalytics = (
  filters: AnalyticsFilters,
  options?: { enabled?: boolean }
): UseQueryResult<RevenueAnalytics, Error> => {
  return useQuery({
    queryKey: ANALYTICS_KEYS.revenue(filters),
    queryFn: () => cloudAnalyticsService.getRevenueAnalytics(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: options?.enabled !== false && !!filters.tenantId,
  });
};

/**
 * Hook to fetch sales metrics
 */
export const useSalesMetrics = (
  filters: AnalyticsFilters,
  options?: { enabled?: boolean }
): UseQueryResult<SalesMetrics, Error> => {
  return useQuery({
    queryKey: ANALYTICS_KEYS.sales(filters),
    queryFn: () => cloudAnalyticsService.getSalesMetrics(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: options?.enabled !== false && !!filters.tenantId,
  });
};

/**
 * Hook to fetch top products
 */
export const useTopProducts = (
  filters: AnalyticsFilters,
  limit: number = 10,
  options?: { enabled?: boolean }
): UseQueryResult<TopProductsAnalytics, Error> => {
  return useQuery({
    queryKey: ANALYTICS_KEYS.topProducts(filters, limit),
    queryFn: () => cloudAnalyticsService.getTopProducts(filters, limit),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: options?.enabled !== false && !!filters.tenantId,
  });
};

/**
 * Hook to fetch shop performance
 */
export const useShopPerformance = (
  filters: AnalyticsFilters,
  options?: { enabled?: boolean }
): UseQueryResult<ShopPerformanceAnalytics, Error> => {
  return useQuery({
    queryKey: ANALYTICS_KEYS.shopPerformance(filters),
    queryFn: () => cloudAnalyticsService.getShopPerformance(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: options?.enabled !== false && !!filters.tenantId,
  });
};

/**
 * Hook to get date range for a period
 */
export const useDateRangeForPeriod = (period: DateRangePeriod) => {
  return cloudAnalyticsService.getDateRangeForPeriod(period);
};

export default {
  useRevenueAnalytics,
  useSalesMetrics,
  useTopProducts,
  useShopPerformance,
  useDateRangeForPeriod,
};
