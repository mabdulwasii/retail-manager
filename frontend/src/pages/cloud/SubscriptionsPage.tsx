import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { CreditCard } from 'lucide-react';

/**
 * Subscriptions Management Page
 * Manage subscription tiers and billing
 *
 * Phase 1: Empty state
 * Phase 2: Tier comparison, upgrade/downgrade, billing history
 */

export const SubscriptionsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <CreditCard className="h-8 w-8" />
          Subscriptions
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage your subscription tier and billing
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Subscription Management</CardTitle>
          <CardDescription>Upgrade, downgrade, or manage billing</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <CreditCard className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Subscriptions Coming Soon</h3>
            <p className="text-muted-foreground max-w-md">
              Subscription tier management, billing history, and payment settings
              will be available in Phase 4.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default SubscriptionsPage;
