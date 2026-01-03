import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Cloud, Building2, Search, Filter, Eye, Ban, CheckCircle, RefreshCw } from 'lucide-react';
import { useCloudTenants, useSuspendTenant, useActivateTenant } from '@/hooks/useCloudTenants';
import {
  CloudTenantStatus,
  SubscriptionTier,
  TenantFilters,
} from '@/services/cloudAggregatorService';

/**
 * Cloud Tenants Management Page
 * Phase 2: Full data integration with TanStack Query
 */

export const CloudTenantsPage: React.FC = () => {
  // State for filters
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<CloudTenantStatus | undefined>();
  const [tierFilter, setTierFilter] = useState<SubscriptionTier | undefined>();
  const [page, setPage] = useState(0);
  const [showFilters, setShowFilters] = useState(false);

  // Build filter object
  const filters: TenantFilters = {
    search: searchTerm || undefined,
    status: statusFilter,
    subscriptionTier: tierFilter,
    page,
    size: 20,
  };

  // Fetch tenants with filters
  const { data, isLoading, isError, error, refetch } = useCloudTenants(filters);

  // Mutations
  const suspendTenant = useSuspendTenant();
  const activateTenant = useActivateTenant();

  // Calculate stats
  const totalTenants = data?.totalElements || 0;
  const activeTenants = data?.content.filter((t) => t.status === CloudTenantStatus.ACTIVE).length || 0;
  const totalShops = data?.content.reduce((sum, t) => sum + t.shopCount, 0) || 0;

  // Handle tenant actions
  const handleSuspend = (id: string) => {
    if (confirm('Are you sure you want to suspend this tenant?')) {
      suspendTenant.mutate(id);
    }
  };

  const handleActivate = (id: string) => {
    activateTenant.mutate(id);
  };

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
        <Button variant="outline" onClick={() => setShowFilters(!showFilters)}>
          <Filter className="h-4 w-4 mr-2" />
          Filters
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Total Tenants</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalTenants}</div>
            <p className="text-xs text-muted-foreground">
              {activeTenants} active
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Active Subscriptions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{activeTenants}</div>
            <p className="text-xs text-muted-foreground">Across all tiers</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-medium">Total Shops</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalShops}</div>
            <p className="text-xs text-muted-foreground">Registered locations</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters Panel */}
      {showFilters && (
        <Card>
          <CardHeader>
            <CardTitle>Filters</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 md:grid-cols-2">
              {/* Status Filter */}
              <div>
                <label className="text-sm font-medium">Status</label>
                <select
                  className="w-full mt-1 px-3 py-2 border rounded-md"
                  value={statusFilter || ''}
                  onChange={(e) =>
                    setStatusFilter(e.target.value ? (e.target.value as CloudTenantStatus) : undefined)
                  }
                >
                  <option value="">All Statuses</option>
                  <option value={CloudTenantStatus.ACTIVE}>Active</option>
                  <option value={CloudTenantStatus.SUSPENDED}>Suspended</option>
                  <option value={CloudTenantStatus.INACTIVE}>Inactive</option>
                </select>
              </div>

              {/* Tier Filter */}
              <div>
                <label className="text-sm font-medium">Subscription Tier</label>
                <select
                  className="w-full mt-1 px-3 py-2 border rounded-md"
                  value={tierFilter || ''}
                  onChange={(e) =>
                    setTierFilter(e.target.value ? (e.target.value as SubscriptionTier) : undefined)
                  }
                >
                  <option value="">All Tiers</option>
                  <option value={SubscriptionTier.FREE}>Free</option>
                  <option value={SubscriptionTier.BASIC}>Basic</option>
                  <option value={SubscriptionTier.PREMIUM}>Premium</option>
                  <option value={SubscriptionTier.ENTERPRISE}>Enterprise</option>
                </select>
              </div>
            </div>

            <Button
              variant="outline"
              onClick={() => {
                setStatusFilter(undefined);
                setTierFilter(undefined);
                setSearchTerm('');
              }}
            >
              Clear Filters
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Search Bar */}
      <div className="flex gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="Search tenants by name or email..."
            className="w-full pl-10 pr-4 py-2 border rounded-md"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <Button variant="outline" onClick={() => refetch()} disabled={isLoading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      {/* Tenants List */}
      <Card>
        <CardHeader>
          <CardTitle>Cloud Tenants</CardTitle>
          <CardDescription>
            {data?.totalElements || 0} registered retail businesses
          </CardDescription>
        </CardHeader>
        <CardContent>
          {/* Loading State */}
          {isLoading && (
            <div className="flex flex-col items-center justify-center py-12">
              <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground mb-4" />
              <p className="text-muted-foreground">Loading tenants...</p>
            </div>
          )}

          {/* Error State */}
          {isError && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Building2 className="h-16 w-16 text-destructive mb-4" />
              <h3 className="text-lg font-semibold mb-2">Failed to load tenants</h3>
              <p className="text-muted-foreground max-w-md mb-4">
                {error?.message || 'An error occurred while fetching tenants'}
              </p>
              <Button onClick={() => refetch()}>Try Again</Button>
            </div>
          )}

          {/* Empty State */}
          {!isLoading && !isError && data?.content.length === 0 && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Building2 className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No tenants found</h3>
              <p className="text-muted-foreground max-w-md">
                {searchTerm || statusFilter || tierFilter
                  ? 'Try adjusting your filters'
                  : 'No retail businesses have registered yet'}
              </p>
            </div>
          )}

          {/* Tenants Table */}
          {!isLoading && !isError && data && data.content.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-3 px-4 font-medium">Tenant Name</th>
                    <th className="text-left py-3 px-4 font-medium">Email</th>
                    <th className="text-left py-3 px-4 font-medium">Tier</th>
                    <th className="text-left py-3 px-4 font-medium">Shops</th>
                    <th className="text-left py-3 px-4 font-medium">Status</th>
                    <th className="text-right py-3 px-4 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((tenant) => (
                    <tr key={tenant.id} className="border-b hover:bg-muted/50">
                      <td className="py-3 px-4 font-medium">{tenant.tenantName}</td>
                      <td className="py-3 px-4 text-muted-foreground">{tenant.tenantEmail}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                            tenant.subscriptionTier === SubscriptionTier.FREE
                              ? 'bg-gray-100 text-gray-800'
                              : tenant.subscriptionTier === SubscriptionTier.BASIC
                              ? 'bg-blue-100 text-blue-800'
                              : tenant.subscriptionTier === SubscriptionTier.PREMIUM
                              ? 'bg-purple-100 text-purple-800'
                              : 'bg-yellow-100 text-yellow-800'
                          }`}
                        >
                          {tenant.subscriptionTier}
                        </span>
                      </td>
                      <td className="py-3 px-4">{tenant.shopCount}</td>
                      <td className="py-3 px-4">
                        <span
                          className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                            tenant.status === CloudTenantStatus.ACTIVE
                              ? 'bg-green-100 text-green-800'
                              : tenant.status === CloudTenantStatus.SUSPENDED
                              ? 'bg-red-100 text-red-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {tenant.status}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex justify-end gap-2">
                          <Button variant="ghost" size="sm" asChild>
                            <Link to={`/cloud/tenants/${tenant.id}`}>
                              <Eye className="h-4 w-4" />
                            </Link>
                          </Button>
                          {tenant.status === CloudTenantStatus.ACTIVE ? (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleSuspend(tenant.id)}
                              disabled={suspendTenant.isPending}
                            >
                              <Ban className="h-4 w-4" />
                            </Button>
                          ) : (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleActivate(tenant.id)}
                              disabled={activateTenant.isPending}
                            >
                              <CheckCircle className="h-4 w-4" />
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination */}
              {data.totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t">
                  <div className="text-sm text-muted-foreground">
                    Page {data.number + 1} of {data.totalPages} ({data.totalElements} total)
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page === 0}
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={page >= data.totalPages - 1}
                      onClick={() => setPage((p) => p + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default CloudTenantsPage;
