import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Cloud, Building2, Search, Filter } from 'lucide-react';

/**
 * Cloud Tenants Management Page
 * List and manage all registered retail businesses (tenants)
 *
 * Phase 1: Empty state with skeleton
 * Phase 2: Will include TanStack Query integration, filters, pagination
 */

export const CloudTenantsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Cloud className="h-8 w-8" />
            Cloud Tenants
          </h1>
          <p className="text-muted-foreground mt-2">
            Manage registered retail businesses and their subscriptions
          </p>
        </div>
        <Button variant="outline">
          <Filter className="h-4 w-4 mr-2" />
          Filters
        </Button>
      </div>

      {/* Search Bar */}
      <div className="flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search tenants by name or email..."
            className="w-full pl-10 pr-4 py-2 border rounded-md"
            disabled
          />
        </div>
      </div>

      {/* Empty State (Phase 1) */}
      <Card>
        <CardHeader>
          <CardTitle>Cloud Tenants</CardTitle>
          <CardDescription>
            Registered retail businesses using RetailHQ cloud platform
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Building2 className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Tenant Management Coming Soon</h3>
            <p className="text-muted-foreground max-w-md">
              This feature is currently under development. Tenant list, filtering,
              and management capabilities will be available in Phase 2.
            </p>
            <div className="mt-6 flex gap-4">
              <Button variant="outline" disabled>
                View All Tenants
              </Button>
              <Button variant="outline" disabled>
                Register New Tenant
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Stats Cards (Placeholder) */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Total Tenants</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">-</div>
            <p className="text-xs text-muted-foreground">Data loading...</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Active Subscriptions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">-</div>
            <p className="text-xs text-muted-foreground">Data loading...</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Total Shops</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">-</div>
            <p className="text-xs text-muted-foreground">Data loading...</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default CloudTenantsPage;
