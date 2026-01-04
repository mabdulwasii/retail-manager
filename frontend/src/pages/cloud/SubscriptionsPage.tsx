import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Progress } from '@/components/ui/progress';
import {
  CreditCard,
  AlertCircle,
  Download,
  TrendingUp,
  CheckCircle,
  Calendar,
} from 'lucide-react';
import { SubscriptionTier } from '@/services/cloudAggregatorService';
import {
  useCurrentSubscription,
  useBillingHistory,
  useSubscriptionUsage,
  useChangeSubscription,
  useCancelSubscription,
  useReactivateSubscription,
} from '@/hooks/useCloudSubscription';
import { SubscriptionTierCard } from '@/components/cloud/SubscriptionTierCard';
import { BillingPeriod, SubscriptionStatus } from '@/services/cloudSubscriptionService';
import { format } from 'date-fns';

/**
 * Subscriptions Management Page
 * Full subscription tier management, billing history, and usage tracking
 */

export const SubscriptionsPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [selectedTier, setSelectedTier] = useState<SubscriptionTier | null>(null);
  const [selectedBillingPeriod, setSelectedBillingPeriod] = useState<BillingPeriod>(
    BillingPeriod.MONTHLY
  );

  // Fetch data
  const { data: subscription, isLoading: isLoadingSubscription } = useCurrentSubscription(tenantId);
  const { data: billingHistory, isLoading: isLoadingBilling } = useBillingHistory(tenantId);
  const { data: usage } = useSubscriptionUsage(tenantId);

  // Mutations
  const changeSubscription = useChangeSubscription();
  const cancelSubscription = useCancelSubscription();
  const reactivateSubscription = useReactivateSubscription();

  const handleUpgradeDowngrade = async () => {
    if (!selectedTier) return;

    try {
      await changeSubscription.mutateAsync({
        tenantId,
        newTier: selectedTier,
        billingPeriod: selectedBillingPeriod,
      });
      setSelectedTier(null);
    } catch (error) {
      console.error('Failed to change subscription:', error);
    }
  };

  const handleCancelSubscription = async () => {
    if (confirm('Are you sure you want to cancel your subscription?')) {
      try {
        await cancelSubscription.mutateAsync(tenantId);
      } catch (error) {
        console.error('Failed to cancel subscription:', error);
      }
    }
  };

  const handleReactivate = async () => {
    try {
      await reactivateSubscription.mutateAsync(tenantId);
    } catch (error) {
      console.error('Failed to reactivate subscription:', error);
    }
  };

  const getStatusBadge = (status: SubscriptionStatus) => {
    const variants: Record<SubscriptionStatus, { variant: any; label: string }> = {
      [SubscriptionStatus.ACTIVE]: { variant: 'default', label: 'Active' },
      [SubscriptionStatus.TRIALING]: { variant: 'secondary', label: 'Trial' },
      [SubscriptionStatus.PAST_DUE]: { variant: 'destructive', label: 'Past Due' },
      [SubscriptionStatus.CANCELED]: { variant: 'secondary', label: 'Canceled' },
      [SubscriptionStatus.INCOMPLETE]: { variant: 'secondary', label: 'Incomplete' },
    };
    const config = variants[status];
    return <Badge variant={config.variant}>{config.label}</Badge>;
  };

  if (isLoadingSubscription) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <CreditCard className="h-8 w-8" />
            Subscriptions
          </h1>
        </div>
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              <p className="text-muted-foreground mt-4">Loading subscription...</p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <CreditCard className="h-8 w-8" />
          Subscriptions
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage your subscription tier and billing
        </p>
      </div>

      {/* Current Subscription */}
      {subscription && (
        <Card>
          <CardHeader>
            <div className="flex items-start justify-between">
              <div>
                <CardTitle>Current Plan</CardTitle>
                <CardDescription>Your active subscription details</CardDescription>
              </div>
              {getStatusBadge(subscription.status)}
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-2xl font-bold">{subscription.tier} Plan</h3>
                  <p className="text-muted-foreground">
                    ${subscription.monthlyPrice}/{subscription.billingPeriod.toLowerCase()}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-muted-foreground">Renews on</p>
                  <p className="font-semibold">
                    {format(new Date(subscription.currentPeriodEnd), 'MMM d, yyyy')}
                  </p>
                </div>
              </div>

              {subscription.cancelAtPeriodEnd && (
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    Your subscription will be canceled on{' '}
                    {format(new Date(subscription.currentPeriodEnd), 'MMM d, yyyy')}.
                    <Button
                      variant="link"
                      className="p-0 h-auto ml-2"
                      onClick={handleReactivate}
                      disabled={reactivateSubscription.isPending}
                    >
                      Reactivate
                    </Button>
                  </AlertDescription>
                </Alert>
              )}

              {!subscription.cancelAtPeriodEnd && subscription.status === SubscriptionStatus.ACTIVE && (
                <Button
                  variant="outline"
                  onClick={handleCancelSubscription}
                  disabled={cancelSubscription.isPending}
                >
                  Cancel Subscription
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Usage Stats */}
      {usage && (
        <Card>
          <CardHeader>
            <CardTitle>Usage Statistics</CardTitle>
            <CardDescription>Current usage against your plan limits</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {/* Shops */}
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>Shops</span>
                  <span className="text-muted-foreground">
                    {usage.shopsUsed} / {usage.shopsLimit}
                  </span>
                </div>
                <Progress value={(usage.shopsUsed / usage.shopsLimit) * 100} />
              </div>

              {/* API Calls */}
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>API Calls (this month)</span>
                  <span className="text-muted-foreground">
                    {usage.apiCallsThisMonth.toLocaleString()} / {usage.apiCallsLimit.toLocaleString()}
                  </span>
                </div>
                <Progress value={(usage.apiCallsThisMonth / usage.apiCallsLimit) * 100} />
              </div>

              {/* Storage */}
              <div>
                <div className="flex justify-between text-sm mb-1">
                  <span>Storage</span>
                  <span className="text-muted-foreground">
                    {usage.storageUsedMB} MB / {usage.storageLimitMB} MB
                  </span>
                </div>
                <Progress value={(usage.storageUsedMB / usage.storageLimitMB) * 100} />
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Tier Selection */}
      <div>
        <h2 className="text-2xl font-bold mb-4">Available Plans</h2>
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4 mb-4">
          {Object.values(SubscriptionTier).map((tier) => (
            <SubscriptionTierCard
              key={tier}
              tier={tier}
              selected={selectedTier === tier}
              onSelect={setSelectedTier}
            />
          ))}
        </div>

        {selectedTier && selectedTier !== subscription?.tier && (
          <Card>
            <CardContent className="pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold">
                    Change to {selectedTier} Plan
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Billing period: {selectedBillingPeriod}
                  </p>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" onClick={() => setSelectedTier(null)}>
                    Cancel
                  </Button>
                  <Button
                    onClick={handleUpgradeDowngrade}
                    disabled={changeSubscription.isPending}
                  >
                    <TrendingUp className="h-4 w-4 mr-2" />
                    {changeSubscription.isPending ? 'Changing...' : 'Confirm Change'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Billing History */}
      <Card>
        <CardHeader>
          <CardTitle>Billing History</CardTitle>
          <CardDescription>Past invoices and payments</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoadingBilling ? (
            <div className="py-8 text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
            </div>
          ) : !billingHistory || billingHistory.length === 0 ? (
            <div className="py-8 text-center text-muted-foreground">
              No billing history available
            </div>
          ) : (
            <div className="space-y-2">
              {billingHistory.map((invoice) => (
                <div
                  key={invoice.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                >
                  <div className="flex items-center gap-4">
                    {invoice.status === 'paid' ? (
                      <CheckCircle className="h-5 w-5 text-green-600" />
                    ) : (
                      <AlertCircle className="h-5 w-5 text-orange-600" />
                    )}
                    <div>
                      <p className="font-medium">
                        {invoice.currency.toUpperCase()} ${invoice.amount}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Invoice #{invoice.invoiceNumber} • {format(new Date(invoice.date), 'MMM d, yyyy')}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge variant={invoice.status === 'paid' ? 'default' : 'secondary'}>
                      {invoice.status}
                    </Badge>
                    {invoice.pdfUrl && (
                      <Button variant="ghost" size="sm">
                        <Download className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default SubscriptionsPage;
