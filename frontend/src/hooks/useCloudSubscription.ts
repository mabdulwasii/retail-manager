import { useMutation, useQuery, useQueryClient, UseQueryResult } from '@tanstack/react-query';
import cloudSubscriptionService, {
  TenantSubscription,
  BillingHistoryItem,
  UpgradeDowngradeRequest,
  SubscriptionUsageStats,
} from '@/services/cloudSubscriptionService';

/**
 * React Query hooks for Cloud Subscription management
 */

// ==================== Query Hooks ====================

/**
 * Fetch current subscription for a tenant
 */
export const useCurrentSubscription = (
  tenantId: string,
  options?: { enabled?: boolean }
): UseQueryResult<TenantSubscription, Error> => {
  return useQuery({
    queryKey: ['cloud-subscription', tenantId],
    queryFn: () => cloudSubscriptionService.getCurrentSubscription(tenantId),
    enabled: options?.enabled !== false && !!tenantId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Fetch billing history for a tenant
 */
export const useBillingHistory = (
  tenantId: string,
  options?: { enabled?: boolean }
): UseQueryResult<BillingHistoryItem[], Error> => {
  return useQuery({
    queryKey: ['cloud-billing-history', tenantId],
    queryFn: () => cloudSubscriptionService.getBillingHistory(tenantId),
    enabled: options?.enabled !== false && !!tenantId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Fetch subscription usage statistics
 */
export const useSubscriptionUsage = (
  tenantId: string,
  options?: { enabled?: boolean }
): UseQueryResult<SubscriptionUsageStats, Error> => {
  return useQuery({
    queryKey: ['cloud-subscription-usage', tenantId],
    queryFn: () => cloudSubscriptionService.getUsageStats(tenantId),
    enabled: options?.enabled !== false && !!tenantId,
    staleTime: 1 * 60 * 1000, // 1 minute
  });
};

// ==================== Mutation Hooks ====================

/**
 * Change subscription tier (upgrade/downgrade)
 */
export const useChangeSubscription = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: UpgradeDowngradeRequest) =>
      cloudSubscriptionService.changeSubscription(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-subscription', variables.tenantId],
      });
      queryClient.invalidateQueries({
        queryKey: ['cloud-subscription-usage', variables.tenantId],
      });
    },
  });
};

/**
 * Cancel subscription
 */
export const useCancelSubscription = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (tenantId: string) =>
      cloudSubscriptionService.cancelSubscription(tenantId),
    onSuccess: (_, tenantId) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-subscription', tenantId],
      });
    },
  });
};

/**
 * Reactivate subscription
 */
export const useReactivateSubscription = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (tenantId: string) =>
      cloudSubscriptionService.reactivateSubscription(tenantId),
    onSuccess: (_, tenantId) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-subscription', tenantId],
      });
    },
  });
};

/**
 * Update payment method
 */
export const useUpdatePaymentMethod = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ tenantId, paymentMethodId }: { tenantId: string; paymentMethodId: string }) =>
      cloudSubscriptionService.updatePaymentMethod(tenantId, paymentMethodId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-subscription', variables.tenantId],
      });
    },
  });
};
