# Cloud Frontend Features - Implementation Plan

**Status**: Planning Phase
**Last Updated**: 2026-01-03
**Target**: RetailHQ Cloud Portal (`cloud.retailhq.app`)

---

## Overview

This document outlines the implementation plan for cloud-specific frontend features in the RetailHQ platform. The frontend will use a **unified application** approach, serving both local shop operations and cloud portal features through mode detection.

---

## Architecture

### Unified Frontend Strategy

**Single React Application** serving dual deployment modes:

```
Frontend App (shop-manager/frontend/)
├─ Local Mode (localhost:3001, shop.myretail.com)
│  └─ Features: Products, Sales, POS, Inventory, Investments, Users
│
└─ Cloud Mode (cloud.retailhq.app)
   ├─ All local features (reused for cloud tenant operations)
   └─ Cloud-specific features:
      ├─ /cloud/tenants - Tenant management
      ├─ /cloud/analytics - Cross-shop analytics
      ├─ /cloud/subscriptions - Billing management
      └─ /cloud/api-keys - API key management
```

**Rationale**:
- ✅ Code reuse (components, design system, API services)
- ✅ Consistent UX across deployment modes
- ✅ Single codebase maintenance
- ✅ Already supports multi-shop operations

---

## Mode Detection

### Runtime Configuration

**Environment Detection**:
```typescript
// src/config/runtime-config.ts
export interface RuntimeConfig {
  apiBaseUrl: string;
  environment: 'local' | 'cloud-portal' | 'embedded';
  cloudMode: boolean;
  embeddedMode: boolean;
}

export const getRuntimeConfig = (): RuntimeConfig => {
  const hostname = window.location.hostname;
  const isCloudMode = hostname.includes('cloud.retailhq.app');
  const isEmbeddedMode = import.meta.env.VITE_EMBEDDED_MODE === 'true';

  return {
    apiBaseUrl: import.meta.env.VITE_API_BASE_URL,
    environment: isCloudMode ? 'cloud-portal' : isEmbeddedMode ? 'embedded' : 'local',
    cloudMode: isCloudMode,
    embeddedMode: isEmbeddedMode
  };
};
```

### Environment Files

**Cloud Portal** (`.env.cloud`):
```env
VITE_API_BASE_URL=https://api.retailhq.app/api
VITE_APP_ENVIRONMENT=cloud-portal
VITE_CLOUD_MODE=true
VITE_EMBEDDED_MODE=false
```

**Local Shop** (`.env.local`):
```env
VITE_API_BASE_URL=http://localhost:8081/api
VITE_APP_ENVIRONMENT=local
VITE_CLOUD_MODE=false
VITE_EMBEDDED_MODE=false
```

**Embedded Mode** (`.env.embedded`):
```env
VITE_API_BASE_URL=http://localhost:8082/api
VITE_APP_ENVIRONMENT=embedded
VITE_CLOUD_MODE=false
VITE_EMBEDDED_MODE=true
```

---

## Cloud-Specific Features

### 1. Cloud Tenants Management

**Page**: `/cloud/tenants`
**Purpose**: Manage registered retail businesses (tenants)
**User Roles**: SYSTEM_ADMIN (cloud platform administrators)

**Features**:
- List all registered tenants with pagination and search
- View tenant details (name, email, subscription tier, shop count)
- Suspend/unsuspend tenants
- View tenant API keys (masked, with regenerate option)
- Tenant activity logs

**UI Components**:
```tsx
// src/pages/cloud/CloudTenantsPage.tsx
export const CloudTenantsPage: React.FC = () => {
  // Tenant list with filters (status, subscription tier)
  // Actions: View details, Suspend, Regenerate API key
};

// src/pages/cloud/CloudTenantDetailPage.tsx
export const CloudTenantDetailPage: React.FC = () => {
  // Tenant overview (name, email, subscription, status)
  // Linked shops list
  // API key management
  // Activity timeline
};
```

**API Integration**:
```typescript
// src/services/cloudAggregatorService.ts
export const cloudAggregatorService = {
  listTenants: (filters?: TenantFilters) =>
    api.get<CloudTenant[]>('/registration/tenants', { params: filters }),

  getTenantById: (id: string) =>
    api.get<CloudTenant>(`/registration/tenants/${id}`),

  suspendTenant: (id: string) =>
    api.patch(`/registration/tenants/${id}/suspend`),

  regenerateApiKey: (id: string) =>
    api.post<ApiKeyResponse>(`/registration/tenants/${id}/regenerate-key`)
};
```

---

### 2. Cross-Shop Analytics Dashboard

**Page**: `/cloud/analytics`
**Purpose**: Aggregated analytics across all shops for a tenant
**User Roles**: SYSTEM_ADMIN, TENANT_ADMIN (cloud users)

**Features**:
- **Revenue Analytics**:
  - Total revenue across all shops (daily, weekly, monthly)
  - Revenue by shop (comparison charts)
  - Revenue trends and growth rates

- **Sales Metrics**:
  - Total transactions count
  - Average transaction value
  - Top-selling products across all shops
  - Sales by category

- **Inventory Insights**:
  - Total stock value across shops
  - Low stock alerts aggregated
  - Products nearing expiry (FEFO tracking)

- **Performance Metrics**:
  - Shop performance ranking
  - Employee performance (if multi-shop employees)
  - Customer acquisition trends

**UI Components**:
```tsx
// src/pages/cloud/CrossShopAnalyticsPage.tsx
export const CrossShopAnalyticsPage: React.FC = () => {
  // Date range selector
  // Shop filter (select specific shops or all)
  // KPI cards (total revenue, transactions, inventory value)
  // Charts: Revenue trend, shop comparison, top products
  // Export to CSV/PDF button
};
```

**API Endpoints** (to be implemented in backend):
```typescript
// Future backend endpoints
GET /api/cloud/analytics/revenue?tenantId={id}&startDate={date}&endDate={date}
GET /api/cloud/analytics/sales-metrics?tenantId={id}
GET /api/cloud/analytics/top-products?tenantId={id}&limit=10
GET /api/cloud/analytics/shop-performance?tenantId={id}
```

---

### 3. Subscription Management

**Page**: `/cloud/subscriptions`
**Purpose**: Manage subscription tiers and billing
**User Roles**: TENANT_ADMIN (tenant owners)

**Features**:
- View current subscription tier (FREE, BASIC, PREMIUM, ENTERPRISE)
- Subscription tier comparison table
- Upgrade/downgrade subscription
- Billing history
- Invoice downloads
- Payment method management

**Subscription Tiers**:

| Tier | Price | Shops | Features |
|------|-------|-------|----------|
| **FREE** | $0/mo | 1 shop | Basic analytics, 1 user |
| **BASIC** | $29/mo | 3 shops | Cross-shop analytics, 5 users |
| **PREMIUM** | $99/mo | 10 shops | Advanced analytics, unlimited users, export |
| **ENTERPRISE** | Custom | Unlimited | Custom features, dedicated support, SLA |

**UI Components**:
```tsx
// src/pages/cloud/SubscriptionsPage.tsx
export const SubscriptionsPage: React.FC = () => {
  // Current plan overview
  // Tier comparison cards
  // Upgrade/downgrade actions
  // Billing history table
};
```

**API Integration**:
```typescript
// src/services/subscriptionService.ts
export const subscriptionService = {
  getCurrentSubscription: () =>
    api.get<Subscription>('/cloud/subscriptions/current'),

  upgradeTier: (tier: SubscriptionTier) =>
    api.post('/cloud/subscriptions/upgrade', { tier }),

  getBillingHistory: () =>
    api.get<Invoice[]>('/cloud/subscriptions/invoices'),

  downloadInvoice: (invoiceId: string) =>
    api.get(`/cloud/subscriptions/invoices/${invoiceId}/download`, { responseType: 'blob' })
};
```

---

### 4. API Key Management

**Page**: `/cloud/api-keys`
**Purpose**: Manage API keys for local shop sync
**User Roles**: TENANT_ADMIN

**Features**:
- Display current API key (masked: `rhq_••••••••••••••••1234`)
- Regenerate API key with confirmation
- Copy API key to clipboard
- API usage statistics (requests per day, last used)
- Integration instructions (how to configure local shops)

**UI Components**:
```tsx
// src/pages/cloud/ApiKeysPage.tsx
export const ApiKeysPage: React.FC = () => {
  // API key display with copy button
  // Regenerate button (with warning modal)
  // Usage statistics chart
  // Integration guide (code snippets for local shop config)
};
```

---

## Navigation Updates

### Conditional Navigation Menu

**Update**: `src/components/AuthenticatedApp.tsx`

```tsx
import { getRuntimeConfig } from '../config/runtime-config';

export const AuthenticatedApp: React.FC = () => {
  const { cloudMode } = getRuntimeConfig();

  return (
    <Layout>
      <Navigation>
        {/* Standard navigation items (always visible) */}
        <NavItem to="/dashboard">Dashboard</NavItem>
        <NavItem to="/shops">Shops</NavItem>
        <NavItem to="/products">Products</NavItem>

        {/* Cloud-specific navigation (only in cloud mode) */}
        {cloudMode && (
          <NavGroup label="Cloud Portal">
            <NavItem to="/cloud/tenants">Tenants</NavItem>
            <NavItem to="/cloud/analytics">Analytics</NavItem>
            <NavItem to="/cloud/subscriptions">Subscriptions</NavItem>
            <NavItem to="/cloud/api-keys">API Keys</NavItem>
          </NavGroup>
        )}
      </Navigation>
    </Layout>
  );
};
```

---

## Implementation Phases

### Phase 1: Foundation (Week 1)
**Priority**: HIGH
**Deliverables**:
- ✅ Update `runtime-config.ts` with mode detection
- ✅ Create `.env.cloud` configuration
- ✅ Update navigation with conditional cloud menu
- ✅ Create basic cloud page layouts (empty states)
- ✅ Create `cloudAggregatorService.ts` API client

**Estimated Effort**: 2-3 days

---

### Phase 2: Tenant Management (Week 2)
**Priority**: HIGH
**Deliverables**:
- ✅ Implement `CloudTenantsPage` with list view
- ✅ Implement `CloudTenantDetailPage` with full details
- ✅ Tenant actions: Suspend, unsuspend
- ✅ API key regeneration with masked display
- ✅ Search and filter functionality

**Estimated Effort**: 3-4 days

---

### Phase 3: Analytics Dashboard (Week 3)
**Priority**: MEDIUM
**Deliverables**:
- ✅ Implement `CrossShopAnalyticsPage` layout
- ✅ Backend API endpoints for aggregated analytics
- ✅ Chart components (revenue trend, shop comparison)
- ✅ KPI cards (total revenue, transactions, inventory)
- ✅ Date range filtering

**Dependencies**: Backend analytics API implementation

**Estimated Effort**: 4-5 days

---

### Phase 4: Subscriptions & Billing (Week 4)
**Priority**: LOW (MVP can use FREE tier only)
**Deliverables**:
- ✅ Implement `SubscriptionsPage` with tier comparison
- ✅ Backend subscription management endpoints
- ✅ Payment gateway integration (Stripe/PayPal)
- ✅ Invoice generation and download
- ✅ Automated billing cycle

**Dependencies**: Payment provider selection, backend billing logic

**Estimated Effort**: 5-7 days

---

### Phase 5: Polish & Testing (Week 5)
**Priority**: HIGH
**Deliverables**:
- ✅ End-to-end testing (Playwright/Cypress)
- ✅ Responsive design for mobile/tablet
- ✅ Accessibility audit (WCAG 2.1 AA)
- ✅ Performance optimization (lazy loading, code splitting)
- ✅ Error handling and loading states
- ✅ User documentation

**Estimated Effort**: 3-4 days

---

## Technical Specifications

### State Management

**Recommended**: TanStack Query (already in use)

```typescript
// src/hooks/useCloudTenants.ts
export const useCloudTenants = (filters?: TenantFilters) => {
  return useQuery({
    queryKey: ['cloudTenants', filters],
    queryFn: () => cloudAggregatorService.listTenants(filters),
    staleTime: 5 * 60 * 1000 // 5 minutes
  });
};

// src/hooks/useTenantMutations.ts
export const useSuspendTenant = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => cloudAggregatorService.suspendTenant(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cloudTenants'] });
    }
  });
};
```

### Styling

**Current**: Existing design system (check `frontend/src/` for current styling approach)

**Recommendations**:
- Maintain consistency with existing local shop UI
- Add cloud-specific theme variants if needed
- Use existing component library (Button, Card, Table, etc.)

### Testing

**Unit Tests**:
```typescript
// src/pages/cloud/__tests__/CloudTenantsPage.test.tsx
describe('CloudTenantsPage', () => {
  it('renders tenant list', async () => {
    // Test tenant list rendering
  });

  it('filters tenants by subscription tier', async () => {
    // Test filtering functionality
  });

  it('suspends tenant on action click', async () => {
    // Test suspend action
  });
});
```

**Integration Tests**:
- Test mode detection logic
- Test API service integration
- Test navigation visibility based on mode

**E2E Tests**:
- Complete tenant management workflow
- Analytics dashboard data loading
- Subscription upgrade flow

---

## Security Considerations

### Authentication

- **Cloud Portal**: Use Keycloak with cloud-specific realm (or separate roles)
- **API Keys**: Never expose full API keys in frontend (always masked)
- **Role-Based Access**: Enforce SYSTEM_ADMIN for tenant management

### Data Privacy

- **Tenant Isolation**: Ensure tenants can only see their own data in analytics
- **API Key Security**: Regeneration invalidates old keys immediately
- **Audit Logging**: Log all tenant management actions

---

## Build Configuration

### Vite Build Profiles

**Update**: `vite.config.ts`

```typescript
export default defineConfig(({ mode }) => ({
  // Shared config
  plugins: [react()],

  // Mode-specific overrides
  define: {
    'import.meta.env.VITE_CLOUD_MODE': mode === 'cloud-portal',
    'import.meta.env.VITE_EMBEDDED_MODE': mode === 'embedded'
  },

  build: {
    outDir: mode === 'cloud-portal' ? 'dist/cloud' : 'dist/local'
  }
}));
```

**Build Commands**:
```bash
# Build for local shops
npm run build

# Build for cloud portal
npm run build -- --mode cloud-portal

# Build for embedded
npm run build -- --mode embedded
```

---

## Deployment

### Cloud Portal Deployment

**Domain**: `cloud.retailhq.app`
**Hosting**: Oracle Cloud (same OCI instance as backend)
**Server**: Nginx serving static files

**Nginx Configuration**:
```nginx
server {
  listen 443 ssl http2;
  server_name cloud.retailhq.app;

  root /var/www/retailhq-cloud-frontend;
  index index.html;

  # SPA routing
  location / {
    try_files $uri $uri/ /index.html;
  }

  # API proxy
  location /api/ {
    proxy_pass https://api.retailhq.app/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }

  ssl_certificate /etc/letsencrypt/live/cloud.retailhq.app/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/cloud.retailhq.app/privkey.pem;
}
```

---

## Dependencies

### New NPM Packages (if needed)

```json
{
  "dependencies": {
    "recharts": "^2.10.0",  // For analytics charts (if not already installed)
    "date-fns": "^3.0.0"    // For date range handling
  },
  "devDependencies": {
    "@playwright/test": "^1.40.0"  // For E2E tests
  }
}
```

---

## Migration Path

### From Current State

1. ✅ No breaking changes to existing local shop frontend
2. ✅ Cloud features are additive (feature toggle approach)
3. ✅ Existing components can be reused (Products, Sales, etc.)
4. ✅ Navigation is conditional, not replaced

### Rollout Strategy

**Stage 1**: Internal testing with cloud mode enabled via environment variable
**Stage 2**: Beta testing with select cloud tenants
**Stage 3**: Production deployment to `cloud.retailhq.app`

---

## Success Metrics

### MVP Goals

- ✅ Cloud mode detection working correctly
- ✅ Tenant management UI functional (list, detail, suspend)
- ✅ API key management operational
- ✅ Basic cross-shop analytics (revenue, transactions)
- ✅ No regression in local shop functionality

### Performance Targets

- ✅ Page load time: <3 seconds (cloud portal)
- ✅ API response time: <500ms (tenant list)
- ✅ Analytics dashboard: <2 seconds (data load)

---

## Open Questions

1. **Authentication**: Should cloud portal use separate Keycloak realm or same realm with different roles?
2. **Payment Gateway**: Stripe or PayPal for subscription billing?
3. **Analytics Frequency**: Real-time vs. batch processing for cross-shop analytics?
4. **Export Formats**: CSV only or also PDF/Excel for analytics reports?
5. **Localization**: Multi-language support needed for cloud portal?

---

## Next Steps

1. ✅ Review and approve this plan
2. ✅ Set up cloud deployment environment (Oracle Cloud DNS, SSL)
3. ✅ Start Phase 1 implementation (mode detection, routing)
4. ✅ Create UI mockups for cloud pages (optional but recommended)
5. ✅ Backend team: Implement cloud analytics API endpoints

---

**Document Owner**: Development Team
**Last Reviewed**: 2026-01-03
**Next Review**: After Phase 1 completion
