import api from "@/lib/axios";
import { SubscriptionTier } from "./cloudAggregatorService";

/**
 * Cloud Subscription Service
 * API client for managing tenant subscriptions
 */

// ==================== Types ====================

export interface TenantSubscription {
  id: string;
  tenantId: string;
  tier: SubscriptionTier;
  status: SubscriptionStatus;
  billingPeriod: BillingPeriod;
  currentPeriodStart: string;
  currentPeriodEnd: string;
  cancelAtPeriodEnd: boolean;
  shopLimit: number;
  currentShopCount: number;
  features: string[];
  monthlyPrice: number;
  currency: string;
}

export enum SubscriptionStatus {
  ACTIVE = "ACTIVE",
  PAST_DUE = "PAST_DUE",
  CANCELED = "CANCELED",
  TRIALING = "TRIALING",
  INCOMPLETE = "INCOMPLETE",
}

export enum BillingPeriod {
  MONTHLY = "MONTHLY",
  YEARLY = "YEARLY",
}

export interface BillingHistoryItem {
  id: string;
  invoiceNumber: string;
  date: string;
  amount: number;
  currency: string;
  status: 'paid' | 'pending' | 'failed';
  tier: SubscriptionTier;
  period: string;
  pdfUrl?: string;
}

export interface UpgradeDowngradeRequest {
  tenantId: string;
  newTier: SubscriptionTier;
  billingPeriod: BillingPeriod;
}

export interface SubscriptionUsageStats {
  shopsUsed: number;
  shopsLimit: number;
  storageUsedMB: number;
  storageLimitMB: number;
  apiCallsThisMonth: number;
  apiCallsLimit: number;
  usersCount: number;
  usersLimit: number;
}

// ==================== Service ====================

class CloudSubscriptionService {
  /**
   * Get current subscription for a tenant
   */
  async getCurrentSubscription(tenantId: string): Promise<TenantSubscription> {
    const { data } = await api.get<TenantSubscription>(
      `/api/cloud/tenants/${tenantId}/subscription`
    );
    return data;
  }

  /**
   * Upgrade or downgrade subscription
   */
  async changeSubscription(request: UpgradeDowngradeRequest): Promise<TenantSubscription> {
    const { data } = await api.post<TenantSubscription>(
      `/api/cloud/tenants/${request.tenantId}/subscription/change`,
      {
        newTier: request.newTier,
        billingPeriod: request.billingPeriod,
      }
    );
    return data;
  }

  /**
   * Cancel subscription (will cancel at end of current period)
   */
  async cancelSubscription(tenantId: string): Promise<TenantSubscription> {
    const { data } = await api.post<TenantSubscription>(
      `/api/cloud/tenants/${tenantId}/subscription/cancel`
    );
    return data;
  }

  /**
   * Reactivate a canceled subscription
   */
  async reactivateSubscription(tenantId: string): Promise<TenantSubscription> {
    const { data } = await api.post<TenantSubscription>(
      `/api/cloud/tenants/${tenantId}/subscription/reactivate`
    );
    return data;
  }

  /**
   * Get billing history
   */
  async getBillingHistory(tenantId: string): Promise<BillingHistoryItem[]> {
    const { data } = await api.get<BillingHistoryItem[]>(
      `/api/cloud/tenants/${tenantId}/billing/history`
    );
    return data;
  }

  /**
   * Get subscription usage statistics
   */
  async getUsageStats(tenantId: string): Promise<SubscriptionUsageStats> {
    const { data } = await api.get<SubscriptionUsageStats>(
      `/api/cloud/tenants/${tenantId}/subscription/usage`
    );
    return data;
  }

  /**
   * Download invoice PDF
   */
  async downloadInvoice(tenantId: string, invoiceId: string): Promise<Blob> {
    const { data } = await api.get<Blob>(
      `/api/cloud/tenants/${tenantId}/billing/invoices/${invoiceId}/pdf`,
      { responseType: 'blob' }
    );
    return data;
  }

  /**
   * Update payment method
   */
  async updatePaymentMethod(
    tenantId: string,
    paymentMethodId: string
  ): Promise<void> {
    await api.post(`/api/cloud/tenants/${tenantId}/billing/payment-method`, {
      paymentMethodId,
    });
  }
}

export default new CloudSubscriptionService();
