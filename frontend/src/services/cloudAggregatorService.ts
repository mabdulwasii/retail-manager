import api from "@/lib/axios";

/**
 * Cloud Aggregator Service
 * API client for RetailHQ Cloud Aggregator endpoints
 */

// ==================== Types ====================

export enum SubscriptionTier {
  FREE = "FREE",
  BASIC = "BASIC",
  PREMIUM = "PREMIUM",
  ENTERPRISE = "ENTERPRISE",
}

export enum CloudTenantStatus {
  ACTIVE = "ACTIVE",
  SUSPENDED = "SUSPENDED",
  INACTIVE = "INACTIVE",
}

export enum CloudShopStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE",
  SUSPENDED = "SUSPENDED",
}

export interface CloudTenant {
  id: string;
  tenantName: string;
  tenantEmail: string;
  apiKeyHash: string;
  status: CloudTenantStatus;
  subscriptionTier: SubscriptionTier;
  shopCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CloudShop {
  id: string;
  cloudTenantId: string;
  shopName: string;
  shopEmail: string;
  status: CloudShopStatus;
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TenantRegistrationRequest {
  tenantName: string;
  tenantEmail: string;
  subscriptionTier: SubscriptionTier;
  shops: ShopLinkRequest[];
}

export interface ShopLinkRequest {
  shopName: string;
  shopEmail: string;
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
}

export interface TenantRegistrationResponse {
  tenant: CloudTenant;
  apiKey: string; // Only returned on registration
  shops: CloudShop[];
}

export interface ApiKeyResponse {
  apiKey: string;
  message: string;
}

export interface TenantFilters {
  status?: CloudTenantStatus;
  subscriptionTier?: SubscriptionTier;
  search?: string;
  page?: number;
  size?: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ==================== API Service ====================

class CloudAggregatorService {
  private readonly baseUrl = "/registration";

  /**
   * Register a new cloud tenant with shops
   * Public endpoint - no authentication required
   */
  async registerTenant(
    request: TenantRegistrationRequest
  ): Promise<TenantRegistrationResponse> {
    try {
      const response = await api.post<TenantRegistrationResponse>(
        `${this.baseUrl}/tenants`,
        request
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to register tenant");
    }
  }

  /**
   * Link additional shop to existing tenant
   * Requires X-API-Key header (set via api client interceptor)
   */
  async linkShop(
    tenantId: string,
    shopRequest: ShopLinkRequest
  ): Promise<CloudShop> {
    try {
      const response = await api.post<CloudShop>(
        `${this.baseUrl}/shops`,
        shopRequest,
        {
          headers: {
            "X-Tenant-Id": tenantId,
          },
        }
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to link shop");
    }
  }

  /**
   * Unregister tenant (soft delete)
   * Requires X-API-Key header
   */
  async unregisterTenant(tenantId: string): Promise<void> {
    try {
      await api.delete(`${this.baseUrl}/tenants/${tenantId}`);
    } catch (error: any) {
      throw this.handleError(error, "Failed to unregister tenant");
    }
  }

  /**
   * Get all cloud tenants (admin only)
   * Filtered and paginated
   */
  async listTenants(
    filters?: TenantFilters
  ): Promise<PagedResponse<CloudTenant>> {
    try {
      const response = await api.get<PagedResponse<CloudTenant>>(
        `${this.baseUrl}/tenants`,
        {
          params: filters,
        }
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to fetch tenants");
    }
  }

  /**
   * Get tenant by ID
   */
  async getTenantById(id: string): Promise<CloudTenant> {
    try {
      const response = await api.get<CloudTenant>(
        `${this.baseUrl}/tenants/${id}`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to fetch tenant details");
    }
  }

  /**
   * Get shops for a tenant
   */
  async getShopsByTenant(tenantId: string): Promise<CloudShop[]> {
    try {
      const response = await api.get<CloudShop[]>(
        `${this.baseUrl}/tenants/${tenantId}/shops`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to fetch tenant shops");
    }
  }

  /**
   * Suspend tenant (admin only)
   */
  async suspendTenant(id: string): Promise<CloudTenant> {
    try {
      const response = await api.patch<CloudTenant>(
        `${this.baseUrl}/tenants/${id}/suspend`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to suspend tenant");
    }
  }

  /**
   * Activate tenant (admin only)
   */
  async activateTenant(id: string): Promise<CloudTenant> {
    try {
      const response = await api.patch<CloudTenant>(
        `${this.baseUrl}/tenants/${id}/activate`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to activate tenant");
    }
  }

  /**
   * Regenerate API key for tenant
   */
  async regenerateApiKey(id: string): Promise<ApiKeyResponse> {
    try {
      const response = await api.post<ApiKeyResponse>(
        `${this.baseUrl}/tenants/${id}/regenerate-key`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Failed to regenerate API key");
    }
  }

  /**
   * Health check endpoint
   */
  async healthCheck(): Promise<{ status: string; message: string }> {
    try {
      const response = await api.get<{ status: string; message: string }>(
        `${this.baseUrl}/health`
      );
      return response.data;
    } catch (error: any) {
      throw this.handleError(error, "Health check failed");
    }
  }

  /**
   * Centralized error handling
   */
  private handleError(error: any, defaultMessage: string): Error {
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;

      if (status === 401) {
        return new Error("Unauthorized: Invalid or missing API key");
      }

      if (status === 403) {
        return new Error("Forbidden: Insufficient permissions");
      }

      if (status === 404) {
        return new Error("Resource not found");
      }

      if (status === 409) {
        return new Error(data?.message || "Conflict: Resource already exists");
      }

      if (status >= 400 && status < 500) {
        return new Error(data?.message || defaultMessage);
      }

      if (status >= 500) {
        return new Error("Server error: Please try again later");
      }
    }

    if (error.request) {
      // Request made but no response
      return new Error("Network error: Unable to reach server");
    }

    // Other errors
    return new Error(error.message || defaultMessage);
  }
}

// Export singleton instance
export const cloudAggregatorService = new CloudAggregatorService();
export default cloudAggregatorService;
