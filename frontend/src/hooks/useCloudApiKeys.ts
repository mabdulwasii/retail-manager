import { useMutation, useQuery, useQueryClient, UseQueryResult } from '@tanstack/react-query';
import cloudApiKeysService, {
  ApiKey,
  CreateApiKeyRequest,
  CreateApiKeyResponse,
  ApiKeyUsageStats,
} from '@/services/cloudApiKeysService';

/**
 * React Query hooks for Cloud API Keys management
 */

// ==================== Query Hooks ====================

/**
 * Fetch all API keys for a tenant
 */
export const useApiKeys = (
  tenantId: string,
  options?: { enabled?: boolean }
): UseQueryResult<ApiKey[], Error> => {
  return useQuery({
    queryKey: ['cloud-api-keys', tenantId],
    queryFn: () => cloudApiKeysService.getApiKeys(tenantId),
    enabled: options?.enabled !== false && !!tenantId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
};

/**
 * Fetch usage stats for a specific API key
 */
export const useApiKeyUsage = (
  tenantId: string,
  keyId: string,
  options?: { enabled?: boolean }
): UseQueryResult<ApiKeyUsageStats, Error> => {
  return useQuery({
    queryKey: ['cloud-api-key-usage', tenantId, keyId],
    queryFn: () => cloudApiKeysService.getApiKeyUsage(tenantId, keyId),
    enabled: options?.enabled !== false && !!tenantId && !!keyId,
    staleTime: 1 * 60 * 1000, // 1 minute
  });
};

// ==================== Mutation Hooks ====================

/**
 * Create a new API key
 */
export const useCreateApiKey = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateApiKeyRequest) =>
      cloudApiKeysService.createApiKey(request),
    onSuccess: (_, variables) => {
      // Invalidate API keys list
      queryClient.invalidateQueries({
        queryKey: ['cloud-api-keys', variables.tenantId],
      });
    },
  });
};

/**
 * Revoke an API key
 */
export const useRevokeApiKey = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ tenantId, keyId }: { tenantId: string; keyId: string }) =>
      cloudApiKeysService.revokeApiKey(tenantId, keyId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-api-keys', variables.tenantId],
      });
    },
  });
};

/**
 * Update API key description
 */
export const useUpdateApiKey = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      tenantId,
      keyId,
      description,
    }: {
      tenantId: string;
      keyId: string;
      description: string;
    }) => cloudApiKeysService.updateApiKey(tenantId, keyId, description),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-api-keys', variables.tenantId],
      });
    },
  });
};

/**
 * Regenerate an API key
 */
export const useRegenerateApiKey = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ tenantId, keyId }: { tenantId: string; keyId: string }) =>
      cloudApiKeysService.regenerateApiKey(tenantId, keyId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: ['cloud-api-keys', variables.tenantId],
      });
    },
  });
};
