/**
 * TanStack Query Hooks for Cloud Tenants
 * Handles data fetching, caching, and mutations for cloud aggregator
 */

import { useQuery, useMutation, useQueryClient, UseQueryOptions } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
  cloudAggregatorService,
  CloudTenant,
  CloudShop,
  TenantFilters,
  PagedResponse,
  TenantRegistrationRequest,
  TenantRegistrationResponse,
  ShopLinkRequest,
  ApiKeyResponse,
} from '@/services/cloudAggregatorService';

// ==================== Query Keys ====================

export const cloudTenantKeys = {
  all: ['cloudTenants'] as const,
  lists: () => [...cloudTenantKeys.all, 'list'] as const,
  list: (filters?: TenantFilters) => [...cloudTenantKeys.lists(), filters] as const,
  details: () => [...cloudTenantKeys.all, 'detail'] as const,
  detail: (id: string) => [...cloudTenantKeys.details(), id] as const,
  shops: (tenantId: string) => [...cloudTenantKeys.detail(tenantId), 'shops'] as const,
};

// ==================== Query Hooks ====================

/**
 * Fetch paginated list of cloud tenants with filters
 */
export const useCloudTenants = (
  filters?: TenantFilters,
  options?: Omit<UseQueryOptions<PagedResponse<CloudTenant>>, 'queryKey' | 'queryFn'>
) => {
  return useQuery({
    queryKey: cloudTenantKeys.list(filters),
    queryFn: () => cloudAggregatorService.listTenants(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
    ...options,
  });
};

/**
 * Fetch single tenant by ID
 */
export const useCloudTenant = (
  id: string,
  options?: Omit<UseQueryOptions<CloudTenant>, 'queryKey' | 'queryFn'>
) => {
  return useQuery({
    queryKey: cloudTenantKeys.detail(id),
    queryFn: () => cloudAggregatorService.getTenantById(id),
    enabled: !!id,
    staleTime: 5 * 60 * 1000,
    ...options,
  });
};

/**
 * Fetch shops for a tenant
 */
export const useTenantShops = (
  tenantId: string,
  options?: Omit<UseQueryOptions<CloudShop[]>, 'queryKey' | 'queryFn'>
) => {
  return useQuery({
    queryKey: cloudTenantKeys.shops(tenantId),
    queryFn: () => cloudAggregatorService.getShopsByTenant(tenantId),
    enabled: !!tenantId,
    staleTime: 5 * 60 * 1000,
    ...options,
  });
};

/**
 * Health check query
 */
export const useCloudAggregatorHealth = () => {
  return useQuery({
    queryKey: ['cloudAggregator', 'health'],
    queryFn: () => cloudAggregatorService.healthCheck(),
    staleTime: 1 * 60 * 1000, // 1 minute
    retry: 1,
  });
};

// ==================== Mutation Hooks ====================

/**
 * Register new cloud tenant
 */
export const useRegisterTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: TenantRegistrationRequest) =>
      cloudAggregatorService.registerTenant(request),
    onSuccess: (data: TenantRegistrationResponse) => {
      // Invalidate tenant lists to refetch
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.lists() });

      // Optionally set the new tenant in cache
      queryClient.setQueryData(cloudTenantKeys.detail(data.tenant.id), data.tenant);

      toast.success('Tenant registered successfully', {
        description: `API Key: ${data.apiKey.substring(0, 20)}... (copy this now!)`,
      });
    },
    onError: (error: Error) => {
      toast.error('Failed to register tenant', {
        description: error.message,
      });
    },
  });
};

/**
 * Link additional shop to tenant
 */
export const useLinkShop = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ tenantId, shopRequest }: { tenantId: string; shopRequest: ShopLinkRequest }) =>
      cloudAggregatorService.linkShop(tenantId, shopRequest),
    onSuccess: (_data, variables) => {
      // Invalidate shops list for this tenant
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.shops(variables.tenantId) });

      // Invalidate tenant detail to update shop count
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.detail(variables.tenantId) });

      toast.success('Shop linked successfully');
    },
    onError: (error: Error) => {
      toast.error('Failed to link shop', {
        description: error.message,
      });
    },
  });
};

/**
 * Suspend tenant
 */
export const useSuspendTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => cloudAggregatorService.suspendTenant(id),
    onMutate: async (id: string) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: cloudTenantKeys.detail(id) });

      // Snapshot previous value (optimistic update)
      const previousTenant = queryClient.getQueryData<CloudTenant>(cloudTenantKeys.detail(id));

      // Optimistically update to suspended
      if (previousTenant) {
        queryClient.setQueryData(cloudTenantKeys.detail(id), {
          ...previousTenant,
          status: 'SUSPENDED',
        });
      }

      return { previousTenant };
    },
    onSuccess: (_data, id) => {
      // Invalidate and refetch
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.lists() });

      toast.success('Tenant suspended successfully');
    },
    onError: (error: Error, id, context) => {
      // Rollback optimistic update
      if (context?.previousTenant) {
        queryClient.setQueryData(cloudTenantKeys.detail(id), context.previousTenant);
      }

      toast.error('Failed to suspend tenant', {
        description: error.message,
      });
    },
  });
};

/**
 * Activate tenant
 */
export const useActivateTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => cloudAggregatorService.activateTenant(id),
    onMutate: async (id: string) => {
      await queryClient.cancelQueries({ queryKey: cloudTenantKeys.detail(id) });

      const previousTenant = queryClient.getQueryData<CloudTenant>(cloudTenantKeys.detail(id));

      if (previousTenant) {
        queryClient.setQueryData(cloudTenantKeys.detail(id), {
          ...previousTenant,
          status: 'ACTIVE',
        });
      }

      return { previousTenant };
    },
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.detail(id) });
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.lists() });

      toast.success('Tenant activated successfully');
    },
    onError: (error: Error, id, context) => {
      if (context?.previousTenant) {
        queryClient.setQueryData(cloudTenantKeys.detail(id), context.previousTenant);
      }

      toast.error('Failed to activate tenant', {
        description: error.message,
      });
    },
  });
};

/**
 * Regenerate API key for tenant
 */
export const useRegenerateApiKey = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => cloudAggregatorService.regenerateApiKey(id),
    onSuccess: (data: ApiKeyResponse, id) => {
      // Invalidate tenant detail
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.detail(id) });

      toast.success('API key regenerated', {
        description: `New Key: ${data.apiKey.substring(0, 20)}... (copy this now!)`,
        duration: 10000, // Show for 10 seconds
      });
    },
    onError: (error: Error) => {
      toast.error('Failed to regenerate API key', {
        description: error.message,
      });
    },
  });
};

/**
 * Unregister tenant (soft delete)
 */
export const useUnregisterTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => cloudAggregatorService.unregisterTenant(id),
    onSuccess: (_data, id) => {
      // Remove from cache
      queryClient.removeQueries({ queryKey: cloudTenantKeys.detail(id) });

      // Invalidate lists
      queryClient.invalidateQueries({ queryKey: cloudTenantKeys.lists() });

      toast.success('Tenant unregistered successfully');
    },
    onError: (error: Error) => {
      toast.error('Failed to unregister tenant', {
        description: error.message,
      });
    },
  });
};
