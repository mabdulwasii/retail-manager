import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { DollarSign, ShoppingCart, TrendingUp, Store } from 'lucide-react';
import { RevenueAnalytics, SalesMetrics } from '@/services/cloudAnalyticsService';

interface AnalyticsKPICardsProps {
  revenueData?: RevenueAnalytics;
  salesData?: SalesMetrics;
  shopCount?: number;
  isLoading?: boolean;
}

/**
 * Analytics KPI Cards
 * Display key performance indicators for cloud analytics dashboard
 */
export const AnalyticsKPICards: React.FC<AnalyticsKPICardsProps> = ({
  revenueData,
  salesData,
  shopCount = 0,
  isLoading = false,
}) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatPercentage = (value: number) => {
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(1)}%`;
  };

  const getGrowthColor = (growth: number) => {
    if (growth > 0) return 'text-green-600';
    if (growth < 0) return 'text-red-600';
    return 'text-gray-600';
  };

  if (isLoading) {
    return (
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {[1, 2, 3, 4].map((i) => (
          <Card key={i}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                <div className="h-4 w-24 bg-gray-200 rounded animate-pulse"></div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="h-8 w-32 bg-gray-200 rounded animate-pulse mb-2"></div>
              <div className="h-3 w-20 bg-gray-200 rounded animate-pulse"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    );
  }

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {/* Total Revenue */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
          <DollarSign className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">
            {revenueData ? formatCurrency(revenueData.totalRevenue) : '$0'}
          </div>
          {revenueData && (
            <p className={`text-xs ${getGrowthColor(revenueData.growthPercentage)}`}>
              {formatPercentage(revenueData.growthPercentage)} from previous period
            </p>
          )}
        </CardContent>
      </Card>

      {/* Total Sales */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Total Sales</CardTitle>
          <ShoppingCart className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">
            {salesData ? salesData.totalSales.toLocaleString() : '0'}
          </div>
          {salesData && (
            <p className={`text-xs ${getGrowthColor(salesData.salesGrowth)}`}>
              {formatPercentage(salesData.salesGrowth)} from previous period
            </p>
          )}
        </CardContent>
      </Card>

      {/* Average Order Value */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Avg. Order Value</CardTitle>
          <TrendingUp className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">
            {revenueData ? formatCurrency(revenueData.averageOrderValue) : '$0'}
          </div>
          <p className="text-xs text-muted-foreground">
            Across {revenueData?.totalTransactions.toLocaleString() || 0} transactions
          </p>
        </CardContent>
      </Card>

      {/* Active Shops */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">Active Shops</CardTitle>
          <Store className="h-4 w-4 text-muted-foreground" />
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{shopCount}</div>
          <p className="text-xs text-muted-foreground">
            Reporting locations
          </p>
        </CardContent>
      </Card>
    </div>
  );
};

export default AnalyticsKPICards;
