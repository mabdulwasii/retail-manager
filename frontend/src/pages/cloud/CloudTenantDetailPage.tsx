import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  Building2,
  ArrowLeft,
  Ban,
  CheckCircle,
  RefreshCw,
  Key,
  Copy,
  Eye,
  EyeOff,
  Store,
  Mail,
  Calendar,
  MapPin,
  Phone,
  AlertCircle,
} from 'lucide-react';
import {
  useCloudTenant,
  useTenantShops,
  useSuspendTenant,
  useActivateTenant,
  useRegenerateApiKey,
} from '@/hooks/useCloudTenants';
import { CloudTenantStatus, SubscriptionTier, CloudShopStatus } from '@/services/cloudAggregatorService';
import { toast } from 'sonner';

/**
 * Cloud Tenant Detail Page
 * Phase 2: Full data integration with real-time updates
 */

export const CloudTenantDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [showApiKey, setShowApiKey] = useState(false);
  const [apiKeyValue, setApiKeyValue] = useState<string | null>(null);

  // Fetch tenant data and shops
  const { data: tenant, isLoading: tenantLoading, isError: tenantError, error: tenantErrorMsg } = useCloudTenant(id || '');
  const { data: shops, isLoading: shopsLoading, isError: shopsError } = useTenantShops(id || '');

  // Mutations
  const suspendTenant = useSuspendTenant();
  const activateTenant = useActivateTenant();
  const regenerateApiKey = useRegenerateApiKey();

  // Action handlers
  const handleSuspend = () => {
    if (!id) return;
    if (confirm('Are you sure you want to suspend this tenant? All linked shops will be affected.')) {
      suspendTenant.mutate(id);
    }
  };

  const handleActivate = () => {
    if (!id) return;
    activateTenant.mutate(id);
  };

  const handleRegenerateKey = () => {
    if (!id) return;
    if (
      confirm(
        'Are you sure you want to regenerate the API key? The old key will be immediately invalidated.'
      )
    ) {
      regenerateApiKey.mutate(id, {
        onSuccess: (data) => {
          setApiKeyValue(data.apiKey);
          setShowApiKey(true);
        },
      });
    }
  };

  const copyApiKey = () => {
    if (apiKeyValue) {
      navigator.clipboard.writeText(apiKeyValue);
      toast.success('API key copied to clipboard');
    }
  };

  // Loading state
  if (tenantLoading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-sm">
          <Button variant="ghost" size="sm" onClick={() => navigate('/cloud/tenants')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Tenants
          </Button>
        </div>
        <div className="flex flex-col items-center justify-center py-12">
          <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Loading tenant details...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (tenantError || !tenant) {
    return (
      <div className="space-y-6">
        <div className="flex items-center gap-2 text-sm">
          <Button variant="ghost" size="sm" onClick={() => navigate('/cloud/tenants')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Tenants
          </Button>
        </div>
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-16 w-16 text-destructive mb-4" />
          <h3 className="text-lg font-semibold mb-2">Failed to load tenant</h3>
          <p className="text-muted-foreground max-w-md mb-4">
            {tenantErrorMsg?.message || 'An error occurred while fetching tenant details'}
          </p>
          <Button onClick={() => navigate('/cloud/tenants')}>Back to Tenants</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm">
        <Button variant="ghost" size="sm" asChild>
          <Link to="/cloud/tenants">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Tenants
          </Link>
        </Button>
      </div>

      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Building2 className="h-8 w-8" />
            {tenant.tenantName}
          </h1>
          <p className="text-muted-foreground mt-2">{tenant.tenantEmail}</p>
        </div>
        <div className="flex gap-2">
          {tenant.status === CloudTenantStatus.ACTIVE ? (
            <Button
              variant="destructive"
              onClick={handleSuspend}
              disabled={suspendTenant.isPending}
            >
              <Ban className="h-4 w-4 mr-2" />
              Suspend Tenant
            </Button>
          ) : (
            <Button
              variant="default"
              onClick={handleActivate}
              disabled={activateTenant.isPending}
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Activate Tenant
            </Button>
          )}
        </div>
      </div>

      {/* Tenant Information */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Tenant Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-muted-foreground">Status</p>
                <span
                  className={`inline-flex mt-1 px-2 py-1 rounded-full text-xs font-medium ${
                    tenant.status === CloudTenantStatus.ACTIVE
                      ? 'bg-green-100 text-green-800'
                      : tenant.status === CloudTenantStatus.SUSPENDED
                      ? 'bg-red-100 text-red-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}
                >
                  {tenant.status}
                </span>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">Subscription Tier</p>
                <span
                  className={`inline-flex mt-1 px-2 py-1 rounded-full text-xs font-medium ${
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
              </div>
            </div>

            <div>
              <p className="text-sm font-medium text-muted-foreground flex items-center gap-1">
                <Mail className="h-4 w-4" />
                Email
              </p>
              <p className="mt-1">{tenant.tenantEmail}</p>
            </div>

            <div>
              <p className="text-sm font-medium text-muted-foreground flex items-center gap-1">
                <Store className="h-4 w-4" />
                Linked Shops
              </p>
              <p className="mt-1 text-2xl font-bold">{tenant.shopCount}</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-medium text-muted-foreground flex items-center gap-1">
                  <Calendar className="h-4 w-4" />
                  Created
                </p>
                <p className="mt-1 text-sm">{new Date(tenant.createdAt).toLocaleDateString()}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground flex items-center gap-1">
                  <Calendar className="h-4 w-4" />
                  Updated
                </p>
                <p className="mt-1 text-sm">{new Date(tenant.updatedAt).toLocaleDateString()}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* API Key Management */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Key className="h-5 w-5" />
              API Key Management
            </CardTitle>
            <CardDescription>Manage tenant authentication credentials</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm font-medium text-muted-foreground mb-2">API Key Hash</p>
              <div className="flex items-center gap-2">
                <code className="flex-1 px-3 py-2 bg-muted rounded text-xs font-mono">
                  {showApiKey && apiKeyValue
                    ? apiKeyValue
                    : tenant.apiKeyHash.substring(0, 20) + '...' + tenant.apiKeyHash.substring(tenant.apiKeyHash.length - 10)}
                </code>
                {apiKeyValue && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowApiKey(!showApiKey)}
                  >
                    {showApiKey ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </Button>
                )}
                {apiKeyValue && (
                  <Button variant="ghost" size="sm" onClick={copyApiKey}>
                    <Copy className="h-4 w-4" />
                  </Button>
                )}
              </div>
            </div>

            {apiKeyValue && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-3">
                <p className="text-xs text-yellow-800">
                  <strong>Important:</strong> Save this API key now. It won't be shown again.
                </p>
              </div>
            )}

            <Button
              variant="outline"
              onClick={handleRegenerateKey}
              disabled={regenerateApiKey.isPending}
              className="w-full"
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${regenerateApiKey.isPending ? 'animate-spin' : ''}`} />
              Regenerate API Key
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Linked Shops */}
      <Card>
        <CardHeader>
          <CardTitle>Linked Shops</CardTitle>
          <CardDescription>
            {shops?.length || 0} shop(s) registered under this tenant
          </CardDescription>
        </CardHeader>
        <CardContent>
          {shopsLoading && (
            <div className="flex flex-col items-center justify-center py-12">
              <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground mb-4" />
              <p className="text-muted-foreground">Loading shops...</p>
            </div>
          )}

          {shopsError && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <AlertCircle className="h-12 w-12 text-destructive mb-4" />
              <p className="text-muted-foreground">Failed to load shops</p>
            </div>
          )}

          {!shopsLoading && !shopsError && shops && shops.length === 0 && (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Store className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No shops linked</h3>
              <p className="text-muted-foreground max-w-md">
                This tenant hasn't linked any retail shops yet.
              </p>
            </div>
          )}

          {!shopsLoading && !shopsError && shops && shops.length > 0 && (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {shops.map((shop) => (
                <Card key={shop.id} className="hover:shadow-md transition-shadow">
                  <CardHeader className="pb-3">
                    <CardTitle className="text-base flex items-center justify-between">
                      <span className="flex items-center gap-2">
                        <Store className="h-4 w-4" />
                        {shop.shopName}
                      </span>
                      <span
                        className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                          shop.status === CloudShopStatus.ACTIVE
                            ? 'bg-green-100 text-green-800'
                            : shop.status === CloudShopStatus.SUSPENDED
                            ? 'bg-red-100 text-red-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {shop.status}
                      </span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-2 text-sm">
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <Mail className="h-3 w-3" />
                      <span className="truncate">{shop.shopEmail}</span>
                    </div>
                    {shop.address && (
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <MapPin className="h-3 w-3" />
                        <span className="truncate">{shop.address}</span>
                      </div>
                    )}
                    {shop.city && (
                      <div className="text-muted-foreground">
                        {shop.city}
                        {shop.country && `, ${shop.country}`}
                      </div>
                    )}
                    {shop.phoneNumber && (
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <Phone className="h-3 w-3" />
                        <span>{shop.phoneNumber}</span>
                      </div>
                    )}
                    <div className="pt-2 text-xs text-muted-foreground">
                      Created {new Date(shop.createdAt).toLocaleDateString()}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default CloudTenantDetailPage;
