import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Store } from 'lucide-react';

/**
 * Shop Management Page
 * CRUD operations for managing shops within a tenant
 *
 * TODO Phase 6.1: Implement full shop management
 * - List all shops with filtering/search
 * - Create new shop
 * - Edit shop details
 * - Deactivate/reactivate shops
 * - Assign users to shops
 * - Shop-level settings
 */

export const ShopManagementPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <Store className="h-8 w-8" />
          Shop Management
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage all shop locations within your tenant
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Shop Management</CardTitle>
          <CardDescription>Create, edit, and manage your retail locations</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Store className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Shop Management Coming in Phase 6.1</h3>
            <p className="text-muted-foreground max-w-md">
              Shop management will include creating, editing, and deactivating shop locations,
              as well as assigning users and managing shop-level settings.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ShopManagementPage;
