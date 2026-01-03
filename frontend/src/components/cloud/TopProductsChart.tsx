import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { TopProductsAnalytics } from '@/services/cloudAnalyticsService';
import { Package } from 'lucide-react';

interface TopProductsChartProps {
  data?: TopProductsAnalytics;
  isLoading?: boolean;
}

/**
 * Top Products Chart Component
 * Horizontal bar chart showing best-selling products
 */
export const TopProductsChart: React.FC<TopProductsChartProps> = ({ data, isLoading = false }) => {
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
            <Package className="h-5 w-5" />
            Top Products
          </CardTitle>
          <CardDescription>Best-selling products by revenue</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!data || data.products.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Package className="h-5 w-5" />
            Top Products
          </CardTitle>
          <CardDescription>Best-selling products by revenue</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="h-[300px] flex items-center justify-center text-muted-foreground">
            No product sales data available
          </div>
        </CardContent>
      </Card>
    );
  }

  const chartData = data.products.slice(0, 10).map((product) => ({
    name: product.productName.length > 20
      ? product.productName.substring(0, 20) + '...'
      : product.productName,
    revenue: product.revenue,
    quantity: product.quantitySold,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Package className="h-5 w-5" />
          Top Products
        </CardTitle>
        <CardDescription>Best-selling products by revenue</CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 30, left: 100, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis
              type="number"
              tickFormatter={formatCurrency}
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
            />
            <YAxis
              type="category"
              dataKey="name"
              className="text-xs"
              tick={{ fill: 'hsl(var(--muted-foreground))' }}
              width={90}
            />
            <Tooltip
              formatter={(value: number, name: string) => {
                if (name === 'revenue') return [formatCurrency(value), 'Revenue'];
                return [value, 'Quantity Sold'];
              }}
              contentStyle={{
                backgroundColor: 'hsl(var(--background))',
                border: '1px solid hsl(var(--border))',
                borderRadius: '6px',
              }}
            />
            <Bar
              dataKey="revenue"
              fill="hsl(var(--primary))"
              radius={[0, 4, 4, 0]}
              name="Revenue"
            />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default TopProductsChart;
