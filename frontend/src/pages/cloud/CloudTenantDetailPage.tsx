import React from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Building2, ArrowLeft } from 'lucide-react';

/**
 * Cloud Tenant Detail Page
 * View and manage individual tenant details, shops, and API keys
 *
 * Phase 1: Empty state with skeleton
 * Phase 2: Full tenant data, shop list, API key management
 */

export const CloudTenantDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <div className="space-y-6">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm">
        <Button variant="ghost" size="sm">
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Tenants
        </Button>
      </div>

      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Tenant Details</h1>
        <p className="text-muted-foreground mt-2">
          Tenant ID: {id || 'Loading...'}
        </p>
      </div>

      {/* Empty State */}
      <Card>
        <CardHeader>
          <CardTitle>Tenant Information</CardTitle>
          <CardDescription>Detailed view of tenant data and linked shops</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Building2 className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Tenant Details Coming Soon</h3>
            <p className="text-muted-foreground max-w-md">
              Full tenant management features including shop list, API key management,
              and subscription details will be available in Phase 2.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CloudTenantDetailPage;
