/**
 * Test Data: Cloud Tenants
 * Mock cloud tenant and shop data for testing
 */

import {
  CloudTenant,
  CloudShop,
  CloudTenantStatus,
  CloudShopStatus,
  SubscriptionTier,
  PagedResponse,
  TenantRegistrationResponse,
} from "@/services/cloudAggregatorService";

// ==================== Cloud Tenant Mocks ====================

export const getMockCloudTenant = (overrides: Partial<CloudTenant> = {}): CloudTenant => ({
  id: "tenant1",
  tenantName: "Tech Retail Co",
  tenantEmail: "admin@techretail.com",
  apiKeyHash: "$2a$10$hashedkey...", // BCrypt hash (not real)
  status: CloudTenantStatus.ACTIVE,
  subscriptionTier: SubscriptionTier.BASIC,
  shopCount: 2,
  createdAt: new Date("2025-01-01").toISOString(),
  updatedAt: new Date("2025-01-01").toISOString(),
  ...overrides,
});

export const getMockCloudTenantsList = (): CloudTenant[] => [
  getMockCloudTenant(),
  getMockCloudTenant({
    id: "tenant2",
    tenantName: "Fashion Empire",
    tenantEmail: "contact@fashionempire.com",
    subscriptionTier: SubscriptionTier.PREMIUM,
    shopCount: 5,
    createdAt: new Date("2024-12-15").toISOString(),
  }),
  getMockCloudTenant({
    id: "tenant3",
    tenantName: "Grocery Chain Inc",
    tenantEmail: "support@grocerychain.com",
    subscriptionTier: SubscriptionTier.ENTERPRISE,
    shopCount: 15,
    createdAt: new Date("2024-11-20").toISOString(),
  }),
  getMockCloudTenant({
    id: "tenant4",
    tenantName: "Startup Shop",
    tenantEmail: "hello@startupshop.com",
    subscriptionTier: SubscriptionTier.FREE,
    status: CloudTenantStatus.SUSPENDED,
    shopCount: 1,
    createdAt: new Date("2025-01-05").toISOString(),
  }),
];

export const getMockPagedTenants = (
  page = 0,
  size = 20
): PagedResponse<CloudTenant> => {
  const allTenants = getMockCloudTenantsList();
  const start = page * size;
  const end = start + size;

  return {
    content: allTenants.slice(start, end),
    totalElements: allTenants.length,
    totalPages: Math.ceil(allTenants.length / size),
    number: page,
    size,
  };
};

// ==================== Cloud Shop Mocks ====================

export const getMockCloudShop = (overrides: Partial<CloudShop> = {}): CloudShop => ({
  id: "shop1",
  cloudTenantId: "tenant1",
  shopName: "Tech Retail Downtown",
  shopEmail: "downtown@techretail.com",
  status: CloudShopStatus.ACTIVE,
  address: "123 Main Street",
  city: "Lagos",
  country: "Nigeria",
  phoneNumber: "+234-xxx-xxx-xxxx",
  createdAt: new Date("2025-01-01").toISOString(),
  updatedAt: new Date("2025-01-01").toISOString(),
  ...overrides,
});

export const getMockCloudShopsList = (tenantId = "tenant1"): CloudShop[] => [
  getMockCloudShop({ cloudTenantId: tenantId }),
  getMockCloudShop({
    id: "shop2",
    cloudTenantId: tenantId,
    shopName: "Tech Retail Mall Branch",
    shopEmail: "mall@techretail.com",
    address: "456 Shopping Plaza",
    city: "Abuja",
  }),
];

// ==================== Registration Response Mocks ====================

export const getMockRegistrationResponse = (): TenantRegistrationResponse => ({
  tenant: getMockCloudTenant(),
  apiKey: "rhq_test1234567890abcdefghijklmnopqrstuvwxyz", // Example API key
  shops: getMockCloudShopsList(),
});

// ==================== Filter Helpers ====================

export const filterTenantsByStatus = (
  tenants: CloudTenant[],
  status?: CloudTenantStatus
): CloudTenant[] => {
  if (!status) return tenants;
  return tenants.filter((t) => t.status === status);
};

export const filterTenantsByTier = (
  tenants: CloudTenant[],
  tier?: SubscriptionTier
): CloudTenant[] => {
  if (!tier) return tenants;
  return tenants.filter((t) => t.subscriptionTier === tier);
};

export const searchTenants = (
  tenants: CloudTenant[],
  query?: string
): CloudTenant[] => {
  if (!query) return tenants;
  const lowerQuery = query.toLowerCase();
  return tenants.filter(
    (t) =>
      t.tenantName.toLowerCase().includes(lowerQuery) ||
      t.tenantEmail.toLowerCase().includes(lowerQuery)
  );
};
