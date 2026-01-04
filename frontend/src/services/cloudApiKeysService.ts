import api from "@/lib/axios";

/**
 * Cloud API Keys Service
 * API client for managing tenant API keys
 */

// ==================== Types ====================

export interface ApiKey {
  id: string;
  tenantId: string;
  keyPrefix: string; // First 8 chars for display (e.g., "a1b2c3d4...")
  maskedKey: string; // Masked version: "a1b2c3d4...xyz9"
  description: string;
  createdAt: string;
  lastUsedAt: string | null;
  expiresAt: string | null;
  isActive: boolean;
  usageCount: number;
  permissions: string[]; // e.g., ["READ", "WRITE", "SYNC"]
}

export interface CreateApiKeyRequest {
  tenantId: string;
  description: string;
  expiresInDays?: number; // Optional expiry (null = never expires)
  permissions: string[];
}

export interface CreateApiKeyResponse {
  apiKey: ApiKey;
  fullKey: string; // Only returned once on creation
  warning: string; // "Store this key securely. It will not be shown again."
}

export interface ApiKeyUsageStats {
  totalRequests: number;
  last24Hours: number;
  last7Days: number;
  last30Days: number;
  lastUsedEndpoint: string | null;
  lastUsedAt: string | null;
}

// ==================== Service ====================

class CloudApiKeysService {
  /**
   * Get all API keys for a tenant
   */
  async getApiKeys(tenantId: string): Promise<ApiKey[]> {
    const { data } = await api.get<ApiKey[]>(`/api/cloud/tenants/${tenantId}/api-keys`);
    return data;
  }

  /**
   * Create a new API key
   */
  async createApiKey(request: CreateApiKeyRequest): Promise<CreateApiKeyResponse> {
    const { data } = await api.post<CreateApiKeyResponse>(
      `/api/cloud/tenants/${request.tenantId}/api-keys`,
      request
    );
    return data;
  }

  /**
   * Revoke (deactivate) an API key
   */
  async revokeApiKey(tenantId: string, keyId: string): Promise<void> {
    await api.delete(`/api/cloud/tenants/${tenantId}/api-keys/${keyId}`);
  }

  /**
   * Update API key description
   */
  async updateApiKey(
    tenantId: string,
    keyId: string,
    description: string
  ): Promise<ApiKey> {
    const { data } = await api.patch<ApiKey>(
      `/api/cloud/tenants/${tenantId}/api-keys/${keyId}`,
      { description }
    );
    return data;
  }

  /**
   * Get usage statistics for an API key
   */
  async getApiKeyUsage(tenantId: string, keyId: string): Promise<ApiKeyUsageStats> {
    const { data } = await api.get<ApiKeyUsageStats>(
      `/api/cloud/tenants/${tenantId}/api-keys/${keyId}/usage`
    );
    return data;
  }

  /**
   * Regenerate an API key (revokes old, creates new with same permissions)
   */
  async regenerateApiKey(tenantId: string, keyId: string): Promise<CreateApiKeyResponse> {
    const { data } = await api.post<CreateApiKeyResponse>(
      `/api/cloud/tenants/${tenantId}/api-keys/${keyId}/regenerate`
    );
    return data;
  }
}

export default new CloudApiKeysService();
