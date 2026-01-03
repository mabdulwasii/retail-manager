import React, { useState } from 'react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { BarChart3, AlertCircle } from 'lucide-react';
import { DateRangePeriod } from '@/services/cloudAnalyticsService';
import {
  useRevenueAnalytics,
  useSalesMetrics,
  useTopProducts,
  useShopPerformance,
} from '@/hooks/useCloudAnalytics';
import { AnalyticsKPICards } from '@/components/cloud/AnalyticsKPICards';
import { RevenueChart } from '@/components/cloud/RevenueChart';
import { TopProductsChart } from '@/components/cloud/TopProductsChart';
import { ShopPerformanceChart } from '@/components/cloud/ShopPerformanceChart';
import { AnalyticsDateFilter } from '@/components/cloud/AnalyticsDateFilter';
import cloudAnalyticsService from '@/services/cloudAnalyticsService';

/**
 * Cross-Shop Analytics Dashboard
 * Aggregated analytics across all shops for a tenant
 *
 * Features:
 * - Revenue trends with line charts
 * - Sales metrics and KPIs
 * - Top-selling products
 * - Shop performance comparison
 * - Date range filtering
 * - Export to CSV
 */

export const CrossShopAnalyticsPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [selectedPeriod, setSelectedPeriod] = useState<DateRangePeriod>(
    DateRangePeriod.LAST_30_DAYS
  );
  const [isExporting, setIsExporting] = useState(false);

  // Calculate date range for selected period
  const dateRange = cloudAnalyticsService.getDateRangeForPeriod(selectedPeriod);

  // Analytics filters
  const filters = {
    tenantId,
    period: selectedPeriod,
    dateRange,
  };

  // Fetch analytics data
  const { data: revenueData, isLoading: isLoadingRevenue, error: revenueError } = useRevenueAnalytics(filters);
  const { data: salesData, isLoading: isLoadingSales, error: salesError } = useSalesMetrics(filters);
  const { data: topProductsData, isLoading: isLoadingProducts, error: productsError } = useTopProducts(filters, 10);
  const { data: shopPerformanceData, isLoading: isLoadingShops, error: shopsError } = useShopPerformance(filters);

  const isLoading = isLoadingRevenue || isLoadingSales || isLoadingProducts || isLoadingShops;
  const hasError = revenueError || salesError || productsError || shopsError;

  const handleExport = async () => {
    setIsExporting(true);
    try {
      const blob = await cloudAnalyticsService.exportToCSV(filters);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `analytics-${selectedPeriod}-${new Date().toISOString().split('T')[0]}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <BarChart3 className="h-8 w-8" />
          Cross-Shop Analytics
        </h1>
        <p className="text-muted-foreground mt-2">
          Aggregated insights across all your retail locations
        </p>
      </div>

      {/* Date Range Filter */}
      <AnalyticsDateFilter
        selectedPeriod={selectedPeriod}
        onPeriodChange={setSelectedPeriod}
        onExport={handleExport}
        isExporting={isExporting}
      />

      {/* Error State */}
      {hasError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Failed to load analytics data. Please try again later.
          </AlertDescription>
        </Alert>
      )}

      {/* KPI Cards */}
      <AnalyticsKPICards
        revenueData={revenueData}
        salesData={salesData}
        shopCount={shopPerformanceData?.totalShops}
        isLoading={isLoading}
      />

      {/* Revenue Chart */}
      <RevenueChart data={revenueData} isLoading={isLoadingRevenue} />

      {/* Charts Grid */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Top Products */}
        <TopProductsChart data={topProductsData} isLoading={isLoadingProducts} />

        {/* Shop Performance */}
        <ShopPerformanceChart data={shopPerformanceData} isLoading={isLoadingShops} />
      </div>

      {/* Info Message */}
      <Alert>
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>
          <strong>Note:</strong> Analytics data is updated in real-time based on shop transactions.
          Export functionality allows you to download data for further analysis.
        </AlertDescription>
      </Alert>
    </div>
  );
};

export default CrossShopAnalyticsPage;
