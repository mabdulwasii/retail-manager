import api from "@/lib/axios";

/**
 * Cloud Analytics Service
 * API client for cloud analytics endpoints
 */

// ==================== Types ====================

export interface DateRange {
  startDate: string; // ISO format
  endDate: string;   // ISO format
}

export enum DateRangePeriod {
  LAST_7_DAYS = "LAST_7_DAYS",
  LAST_30_DAYS = "LAST_30_DAYS",
  LAST_90_DAYS = "LAST_90_DAYS",
  CUSTOM = "CUSTOM",
}

export interface RevenueDataPoint {
  date: string;
  revenue: number;
  transactionCount: number;
}

export interface RevenueAnalytics {
  dataPoints: RevenueDataPoint[];
  totalRevenue: number;
  totalTransactions: number;
  averageOrderValue: number;
  previousPeriodRevenue: number;
  growthPercentage: number;
}

export interface SalesMetrics {
  totalSales: number;
  totalRevenue: number;
  averageOrderValue: number;
  topSellingDay: string;
  peakHour: number;
  previousPeriodSales: number;
  salesGrowth: number;
}

export interface TopProduct {
  productId: string;
  productName: string;
  category: string;
  quantitySold: number;
  revenue: number;
  averagePrice: number;
}

export interface TopProductsAnalytics {
  products: TopProduct[];
  totalProducts: number;
}

export interface ShopPerformance {
  shopId: string;
  shopName: string;
  revenue: number;
  transactionCount: number;
  averageOrderValue: number;
  topProduct: string;
  growthPercentage: number;
}

export interface ShopPerformanceAnalytics {
  shops: ShopPerformance[];
  totalShops: number;
  bestPerformingShop: ShopPerformance | null;
  worstPerformingShop: ShopPerformance | null;
}

export interface AnalyticsFilters {
  tenantId: string;
  dateRange?: DateRange;
  period?: DateRangePeriod;
  shopIds?: string[];
}

// ==================== Service ====================

class CloudAnalyticsService {
  private readonly BASE_PATH = "/api/cloud/analytics";

  /**
   * Get revenue analytics for a tenant
   */
  async getRevenueAnalytics(filters: AnalyticsFilters): Promise<RevenueAnalytics> {
    const { data } = await api.get<RevenueAnalytics>(`${this.BASE_PATH}/revenue`, {
      params: {
        tenantId: filters.tenantId,
        period: filters.period,
        startDate: filters.dateRange?.startDate,
        endDate: filters.dateRange?.endDate,
        shopIds: filters.shopIds?.join(','),
      },
    });
    return data;
  }

  /**
   * Get sales metrics for a tenant
   */
  async getSalesMetrics(filters: AnalyticsFilters): Promise<SalesMetrics> {
    const { data } = await api.get<SalesMetrics>(`${this.BASE_PATH}/sales`, {
      params: {
        tenantId: filters.tenantId,
        period: filters.period,
        startDate: filters.dateRange?.startDate,
        endDate: filters.dateRange?.endDate,
        shopIds: filters.shopIds?.join(','),
      },
    });
    return data;
  }

  /**
   * Get top selling products for a tenant
   */
  async getTopProducts(
    filters: AnalyticsFilters,
    limit: number = 10
  ): Promise<TopProductsAnalytics> {
    const { data } = await api.get<TopProductsAnalytics>(`${this.BASE_PATH}/top-products`, {
      params: {
        tenantId: filters.tenantId,
        period: filters.period,
        startDate: filters.dateRange?.startDate,
        endDate: filters.dateRange?.endDate,
        shopIds: filters.shopIds?.join(','),
        limit,
      },
    });
    return data;
  }

  /**
   * Get shop performance comparison for a tenant
   */
  async getShopPerformance(filters: AnalyticsFilters): Promise<ShopPerformanceAnalytics> {
    const { data } = await api.get<ShopPerformanceAnalytics>(
      `${this.BASE_PATH}/shop-performance`,
      {
        params: {
          tenantId: filters.tenantId,
          period: filters.period,
          startDate: filters.dateRange?.startDate,
          endDate: filters.dateRange?.endDate,
        },
      }
    );
    return data;
  }

  /**
   * Export analytics data to CSV
   */
  async exportToCSV(filters: AnalyticsFilters): Promise<Blob> {
    const { data } = await api.get(`${this.BASE_PATH}/export/csv`, {
      params: {
        tenantId: filters.tenantId,
        period: filters.period,
        startDate: filters.dateRange?.startDate,
        endDate: filters.dateRange?.endDate,
        shopIds: filters.shopIds?.join(','),
      },
      responseType: 'blob',
    });
    return data;
  }

  /**
   * Get date range for a period
   */
  getDateRangeForPeriod(period: DateRangePeriod): DateRange {
    const endDate = new Date();
    const startDate = new Date();

    switch (period) {
      case DateRangePeriod.LAST_7_DAYS:
        startDate.setDate(endDate.getDate() - 7);
        break;
      case DateRangePeriod.LAST_30_DAYS:
        startDate.setDate(endDate.getDate() - 30);
        break;
      case DateRangePeriod.LAST_90_DAYS:
        startDate.setDate(endDate.getDate() - 90);
        break;
      default:
        // For CUSTOM, caller should provide custom dateRange
        break;
    }

    return {
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0],
    };
  }
}

const cloudAnalyticsService = new CloudAnalyticsService();
export default cloudAnalyticsService;
