import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { ShopPerformanceAnalytics } from '@/services/cloudAnalyticsService';
import { Store } from 'lucide-react';

interface ShopPerformanceChartProps {
  data?: ShopPerformanceAnalytics;
  isLoading?: boolean;
}

/**
 * Shop Performance Chart Component
 * Grouped bar chart comparing performance across shops
 */
export const ShopPerformanceChart: React.FC<ShopPerformanceChartProps> = ({
  data,
  isLoading = false,
}) => {
  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(value);
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Store className="h-5 w-5" />
            Shop Performance
          </CardTitle>
          <CardDescription>Comparison across all retail locations</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!data || data.shops.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Store className="h-5 w-5" />
            Shop Performance
          </CardTitle>
          <CardDescription>Comparison across all retail locations</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center text-muted-foreground">
            No shop performance data available
          </div>
        </CardContent>
      </Card>
    );
  }

  const chartData = data.shops.map((shop) => ({
    name: shop.shopName.length > 15
      ? shop.shopName.substring(0, 15) + '...'
      : shop.shopName,
    revenue: shop.revenue,
    transactions: shop.transactionCount,
    avgOrder: shop.averageOrderValue,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Store className="h-5 w-5" />
          Shop Performance
        </CardTitle>
        <CardDescription>Comparison across all retail locations</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis
              dataKey="name"
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              angle={-45}
              textAnchor="end"
              height={80}
            />
            <YAxis
              tickFormatter={formatCurrency}
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
            />
            <Tooltip
              formatter={(value: number, name: string) => {
                if (name === 'revenue' || name === 'avgOrder') {
                  return [formatCurrency(value), name === 'revenue' ? 'Revenue' : 'Avg Order'];
                }
                return [value, 'Transactions'];
              }}
              contentStyle={{
                backgroundColor: 'hsl(var(--background))',
                border: '1px solid hsl(var(--border))',
                borderRadius: '6px',
              }}
            />
            <Legend />
            <Bar dataKey="revenue" fill="hsl(var(--primary))" name="Revenue" radius={[4, 4, 0, 0]} />
            <Bar dataKey="transactions" fill="hsl(var(--chart-2))" name="Transactions" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>

        {/* Best/Worst Performers */}
        {data.bestPerformingShop && data.worstPerformingShop && (
          <div className="mt-4 grid grid-cols-2 gap-4 text-sm">
            <div className="p-3 bg-green-50 rounded-lg border border-green-200">
              <div className="font-semibold text-green-900">Best Performer</div>
              <div className="text-green-700">{data.bestPerformingShop.shopName}</div>
              <div className="text-xs text-green-600 mt-1">
                {formatCurrency(data.bestPerformingShop.revenue)} revenue
              </div>
            </div>
            <div className="p-3 bg-orange-50 rounded-lg border border-orange-200">
              <div className="font-semibold text-orange-900">Needs Attention</div>
              <div className="text-orange-700">{data.worstPerformingShop.shopName}</div>
              <div className="text-xs text-orange-600 mt-1">
                {formatCurrency(data.worstPerformingShop.revenue)} revenue
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default ShopPerformanceChart;
