import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart3 } from 'lucide-react';

/**
 * Cross-Shop Analytics Dashboard
 * Aggregated analytics across all shops for a tenant
 *
 * Phase 1: Empty state
 * Phase 2: Revenue charts, sales metrics, inventory insights
 */

export const CrossShopAnalyticsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <BarChart3 className="h-8 w-8" />
          Cross-Shop Analytics
        </h1>
        <p className="text-muted-foreground mt-2">
          Aggregated insights across all your retail locations
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Analytics Dashboard</CardTitle>
          <CardDescription>Revenue, sales, and inventory metrics</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <BarChart3 className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Analytics Coming Soon</h3>
            <p className="text-muted-foreground max-w-md">
              Cross-shop analytics with charts, KPIs, and export features
              will be available in Phase 3.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CrossShopAnalyticsPage;
