# Backend API Implementation Plan

Implementation roadmap for Options A-D (Backend → Frontend Phase 6.1 → Testing → Deployment)

---

## Current State Assessment

### ✅ Already Implemented (Phase 3)

**Aggregator Module (`backend/src/main/java/com/princely/shopmanager/aggregator/`):**

1. **Entities:**
   - `CloudTenant` - Tenant registration with single API key hash
   - `CloudShop` - Shop linking to cloud tenants

2. **Endpoints (AggregatorController):**
   - `POST /api/registration/tenants` - Register tenant (returns API key once)
   - `POST /api/registration/shops` - Link shop (requires X-API-Key header)
   - `DELETE /api/registration/tenants/{id}` - Unregister tenant
   - `GET /api/registration/health` - Health check

3. **Services:**
   - `CloudTenantService` - Registration, shop linking, unregistration
   - Password encoder for API key hashing (bcrypt)

4. **Repositories:**
   - `CloudTenantRepository`
   - `CloudShopRepository`

---

## Option A: Backend API Implementation

### Phase A.1: Multi-Key API Management

**New Entity:** `CloudApiKey` ✅ CREATED
- ID, tenant_id, key_prefix, masked_key, key_hash
- description, last_used_at, expires_at, is_active
- usage_count, permissions (READ, WRITE, DELETE, SYNC, ADMIN)

**Repository:**
```java
// backend/src/main/java/com/princely/shopmanager/aggregator/repository/CloudApiKeyRepository.java
public interface CloudApiKeyRepository extends JpaRepository<CloudApiKey, String> {
    List<CloudApiKey> findByTenantId(String tenantId);
    List<CloudApiKey> findByTenantIdAndIsActive(String tenantId, Boolean isActive);
    Optional<CloudApiKey> findByKeyPrefix(String keyPrefix);
    boolean existsByTenantIdAndDescription(String tenantId, String description);
}
```

**Service:**
```java
// backend/src/main/java/com/princely/shopmanager/aggregator/service/CloudApiKeyService.java
@Service
public class CloudApiKeyService {
    // createApiKey(CreateApiKeyRequest) → CreateApiKeyResponse
    // listApiKeys(tenantId) → List<ApiKeyDto>
    // revokeApiKey(tenantId, keyId)
    // regenerateApiKey(tenantId, keyId) → CreateApiKeyResponse
    // getUsageStats(tenantId, keyId) → ApiKeyUsageStats
    // validateApiKey(keyPrefix, keyPlaintext) → boolean
    // recordUsage(keyId)
}
```

**DTOs:**
```java
// dto/CreateApiKeyRequest.java
record CreateApiKeyRequest(
    String tenantId,
    String description,
    Integer expiresInDays,
    Set<String> permissions
) {}

// dto/CreateApiKeyResponse.java
record CreateApiKeyResponse(
    ApiKeyDto apiKey,
    String fullKey,  // Only returned once
    String warning
) {}

// dto/ApiKeyDto.java
record ApiKeyDto(
    String id,
    String tenantId,
    String keyPrefix,
    String maskedKey,
    String description,
    LocalDateTime createdAt,
    LocalDateTime lastUsedAt,
    LocalDateTime expiresAt,
    Boolean isActive,
    Long usageCount,
    Set<String> permissions
) {}

// dto/ApiKeyUsageStats.java
record ApiKeyUsageStats(
    Long totalRequests,
    Long last24Hours,
    Long last7Days,
    Long last30Days,
    String lastUsedEndpoint,
    LocalDateTime lastUsedAt
) {}
```

**Controller:**
```java
// controller/ApiKeysController.java
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/api-keys")
public class ApiKeysController {

    @GetMapping
    public List<ApiKeyDto> listApiKeys(@PathVariable String tenantId);

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> createApiKey(
        @PathVariable String tenantId,
        @Valid @RequestBody CreateApiKeyRequest request);

    @DeleteMapping("/{keyId}")
    public void revokeApiKey(
        @PathVariable String tenantId,
        @PathVariable String keyId);

    @PostMapping("/{keyId}/regenerate")
    public ResponseEntity<CreateApiKeyResponse> regenerateApiKey(
        @PathVariable String tenantId,
        @PathVariable String keyId);

    @GetMapping("/{keyId}/usage")
    public ApiKeyUsageStats getUsageStats(
        @PathVariable String tenantId,
        @PathVariable String keyId);
}
```

---

### Phase A.2: Subscription & Billing Management

**New Entities:**

```java
// domain/CloudSubscription.java
@Entity
@Table(name = "cloud_subscriptions")
public class CloudSubscription extends BaseEntity {
    @Id
    private String id;

    private String tenantId;  // One-to-one with CloudTenant

    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier;  // FREE, BASIC, PREMIUM, ENTERPRISE

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;  // ACTIVE, PAST_DUE, CANCELED, TRIALING

    @Enumerated(EnumType.STRING)
    private BillingPeriod billingPeriod;  // MONTHLY, YEARLY

    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;

    private Integer shopLimit;
    private Integer currentShopCount;
    private List<String> features;

    private BigDecimal monthlyPrice;
    private String currency;
}

// domain/BillingInvoice.java
@Entity
@Table(name = "billing_invoices")
public class BillingInvoice extends BaseEntity {
    @Id
    private String id;

    private String tenantId;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;

    private BigDecimal amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;  // PAID, PENDING, FAILED

    private SubscriptionTier tier;
    private String period;
    private String pdfUrl;
}
```

**Service:**
```java
// service/CloudSubscriptionService.java
@Service
public class CloudSubscriptionService {
    // getCurrentSubscription(tenantId) → SubscriptionDto
    // changeSubscription(UpgradeDowngradeRequest) → SubscriptionDto
    // cancelSubscription(tenantId) → SubscriptionDto
    // reactivateSubscription(tenantId) → SubscriptionDto
    // getBillingHistory(tenantId) → List<InvoiceDto>
    // getUsageStats(tenantId) → SubscriptionUsageStats
    // generateInvoice(tenantId, period) → InvoiceDto
}
```

**DTOs:**
```java
// dto/SubscriptionDto.java
record SubscriptionDto(
    String id,
    String tenantId,
    SubscriptionTier tier,
    SubscriptionStatus status,
    BillingPeriod billingPeriod,
    LocalDateTime currentPeriodStart,
    LocalDateTime currentPeriodEnd,
    Boolean cancelAtPeriodEnd,
    Integer shopLimit,
    Integer currentShopCount,
    List<String> features,
    BigDecimal monthlyPrice,
    String currency
) {}

// dto/UpgradeDowngradeRequest.java
record UpgradeDowngradeRequest(
    String tenantId,
    SubscriptionTier newTier,
    BillingPeriod billingPeriod
) {}

// dto/SubscriptionUsageStats.java
record SubscriptionUsageStats(
    Integer shopsUsed,
    Integer shopsLimit,
    Long storageUsedMB,
    Long storageLimitMB,
    Long apiCallsThisMonth,
    Long apiCallsLimit,
    Integer usersCount,
    Integer usersLimit
) {}
```

**Controller:**
```java
// controller/SubscriptionsController.java
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/subscription")
public class SubscriptionsController {

    @GetMapping
    public SubscriptionDto getCurrentSubscription(@PathVariable String tenantId);

    @PostMapping("/change")
    public SubscriptionDto changeSubscription(
        @PathVariable String tenantId,
        @Valid @RequestBody UpgradeDowngradeRequest request);

    @PostMapping("/cancel")
    public SubscriptionDto cancelSubscription(@PathVariable String tenantId);

    @PostMapping("/reactivate")
    public SubscriptionDto reactivateSubscription(@PathVariable String tenantId);

    @GetMapping("/usage")
    public SubscriptionUsageStats getUsageStats(@PathVariable String tenantId);
}

// controller/BillingController.java
@RestController
@RequestMapping("/api/cloud/tenants/{tenantId}/billing")
public class BillingController {

    @GetMapping("/history")
    public List<InvoiceDto> getBillingHistory(@PathVariable String tenantId);

    @GetMapping("/invoices/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
        @PathVariable String tenantId,
        @PathVariable String invoiceId);

    @PostMapping("/payment-method")
    public void updatePaymentMethod(
        @PathVariable String tenantId,
        @RequestBody PaymentMethodRequest request);
}
```

---

### Phase A.3: Analytics Aggregation

**Service:**
```java
// service/CloudAnalyticsService.java
@Service
public class CloudAnalyticsService {

    // Get revenue analytics aggregated across all shops for a tenant
    public RevenueAnalytics getRevenueAnalytics(AnalyticsFilters filters) {
        // Query all shops for tenant
        // Aggregate sales data by date
        // Calculate totals, averages, growth percentages
        // Return time-series data for charts
    }

    // Get sales metrics
    public SalesMetrics getSalesMetrics(AnalyticsFilters filters) {
        // Total sales count
        // Revenue
        // AOV
        // Top selling day/hour
        // Growth metrics
    }

    // Get top products across all shops
    public TopProductsAnalytics getTopProducts(AnalyticsFilters filters, int limit) {
        // Aggregate product sales across shops
        // Group by product name
        // Sort by revenue/quantity
        // Return top N
    }

    // Get shop performance comparison
    public ShopPerformanceAnalytics getShopPerformance(AnalyticsFilters filters) {
        // Revenue per shop
        // Transactions per shop
        // AOV per shop
        // Identify best/worst performers
    }

    // Export to CSV
    public byte[] exportToCSV(AnalyticsFilters filters) {
        // Generate CSV with all analytics data
        // Include headers
        // Format for Excel compatibility
    }
}
```

**DTOs:**
```java
// dto/AnalyticsFilters.java
record AnalyticsFilters(
    String tenantId,
    DateRangePeriod period,
    DateRange dateRange
) {}

// dto/RevenueAnalytics.java
record RevenueAnalytics(
    List<RevenueDataPoint> dataPoints,
    BigDecimal totalRevenue,
    Long totalTransactions,
    BigDecimal averageOrderValue,
    BigDecimal previousPeriodRevenue,
    Double growthPercentage
) {}

// dto/RevenueDataPoint.java
record RevenueDataPoint(
    String date,
    BigDecimal revenue,
    Long transactionCount
) {}

// Similar DTOs for SalesMetrics, TopProductsAnalytics, ShopPerformanceAnalytics
```

**Controller:**
```java
// controller/AnalyticsController.java
@RestController
@RequestMapping("/api/cloud/analytics")
public class AnalyticsController {

    @GetMapping("/revenue")
    public RevenueAnalytics getRevenueAnalytics(
        @RequestParam String tenantId,
        @RequestParam DateRangePeriod period,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate);

    @GetMapping("/sales")
    public SalesMetrics getSalesMetrics(...);

    @GetMapping("/top-products")
    public TopProductsAnalytics getTopProducts(
        @RequestParam String tenantId,
        @RequestParam DateRangePeriod period,
        @RequestParam(defaultValue = "10") int limit);

    @GetMapping("/shop-performance")
    public ShopPerformanceAnalytics getShopPerformance(...);

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportToCSV(...);
}
```

---

### Phase A.4: Database Migrations

**Flyway Migration Scripts:**

```sql
-- V21__create_cloud_api_keys.sql
CREATE TABLE cloud_api_keys (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    key_prefix VARCHAR(16) NOT NULL,
    masked_key VARCHAR(100),
    key_hash VARCHAR(500) NOT NULL,
    description VARCHAR(500) NOT NULL,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    usage_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    CONSTRAINT fk_api_key_tenant FOREIGN KEY (tenant_id)
        REFERENCES cloud_tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_cloud_api_key_tenant ON cloud_api_keys(tenant_id);
CREATE INDEX idx_cloud_api_key_prefix ON cloud_api_keys(key_prefix);
CREATE INDEX idx_cloud_api_key_status ON cloud_api_keys(is_active);

CREATE TABLE cloud_api_key_permissions (
    api_key_id VARCHAR(255) NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (api_key_id, permission),
    CONSTRAINT fk_api_key_permission FOREIGN KEY (api_key_id)
        REFERENCES cloud_api_keys(id) ON DELETE CASCADE
);

-- V22__create_cloud_subscriptions.sql
CREATE TABLE cloud_subscriptions (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL UNIQUE,
    tier VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    billing_period VARCHAR(50) NOT NULL,
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT false,
    shop_limit INTEGER NOT NULL,
    current_shop_count INTEGER NOT NULL DEFAULT 0,
    monthly_price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_subscription_tenant FOREIGN KEY (tenant_id)
        REFERENCES cloud_tenants(id) ON DELETE CASCADE
);

-- V23__create_billing_invoices.sql
CREATE TABLE billing_invoices (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    invoice_number VARCHAR(100) NOT NULL UNIQUE,
    invoice_date TIMESTAMP NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    period VARCHAR(100),
    pdf_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_tenant FOREIGN KEY (tenant_id)
        REFERENCES cloud_tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_invoice_tenant ON billing_invoices(tenant_id);
CREATE INDEX idx_invoice_number ON billing_invoices(invoice_number);
CREATE INDEX idx_invoice_status ON billing_invoices(status);

-- V24__initialize_default_subscriptions.sql
-- Create default FREE subscription for all existing cloud tenants
INSERT INTO cloud_subscriptions (id, tenant_id, tier, status, billing_period,
    current_period_start, current_period_end, shop_limit, monthly_price, currency)
SELECT
    uuid_generate_v4()::text,
    id,
    'FREE',
    'ACTIVE',
    'MONTHLY',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    1,  -- Free tier: 1 shop
    0.00,
    'USD'
FROM cloud_tenants
WHERE id NOT IN (SELECT tenant_id FROM cloud_subscriptions);
```

---

### Phase A.5: Integration Tests

**Test Coverage:**

```java
// CloudApiKeyServiceIT.java
@SpringBootTest
class CloudApiKeyServiceIT {
    @Test void shouldCreateApiKey();
    @Test void shouldListApiKeysForTenant();
    @Test void shouldRevokeApiKey();
    @Test void shouldRegenerateApiKey();
    @Test void shouldGetUsageStats();
    @Test void shouldValidateApiKey();
    @Test void shouldRejectExpiredKey();
    @Test void shouldRecordUsage();
}

// CloudSubscriptionServiceIT.java
@SpringBootTest
class CloudSubscriptionServiceIT {
    @Test void shouldGetCurrentSubscription();
    @Test void shouldUpgradeSubscription();
    @Test void shouldDowngradeSubscription();
    @Test void shouldCancelSubscription();
    @Test void shouldReactivateSubscription();
    @Test void shouldGetUsageStats();
    @Test void shouldEnforceShopLimits();
}

// CloudAnalyticsServiceIT.java
@SpringBootTest
class CloudAnalyticsServiceIT {
    @Test void shouldAggregateRevenueAcrossShops();
    @Test void shouldCalculateGrowthPercentages();
    @Test void shouldGetTopProducts();
    @Test void shouldCompareShopPerformance();
    @Test void shouldExportToCSV();
    @Test void shouldFilterByDateRange();
}
```

---

## Option B: Complete Phase 6.1 Frontend

### Tenant Settings Page

**Full Implementation:**
- Company information form
- Timezone selector (dropdown with major timezones)
- Locale settings (language, date format, currency)
- Notification preferences (email, SMS checkboxes)
- Feature flags management (per-tenant toggles)
- Security settings (2FA, session timeout)

**Components to Create:**
- `TenantInfoForm.tsx` - Company details editor
- `TimezoneSelector.tsx` - Timezone dropdown with search
- `NotificationPreferences.tsx` - Email/SMS toggles
- `FeatureFlagsTable.tsx` - Feature toggle management
- `SecuritySettings.tsx` - 2FA and session controls

---

### Shop Management Page

**Full Implementation:**
- Shop list table with pagination
- Search/filter by name, city, status
- Create new shop modal
- Edit shop modal
- Deactivate/reactivate shops
- User assignment (assign managers/employees to shops)
- Shop-level feature flags

**Components to Create:**
- `ShopTable.tsx` - DataTable with actions
- `CreateShopDialog.tsx` - New shop form
- `EditShopDialog.tsx` - Edit shop form
- `UserAssignmentDialog.tsx` - Assign users to shops
- `ShopStatusBadge.tsx` - Active/Inactive badge

---

### Audit Logs Page

**Full Implementation:**
- Audit log table with virtual scrolling (large datasets)
- Filter by:
  - Date range (date picker)
  - User (dropdown)
  - Action type (CREATE, UPDATE, DELETE, etc.)
  - Entity type (Tenant, Shop, User, etc.)
- Search by description
- Export logs to CSV
- Detailed log view modal

**Components to Create:**
- `AuditLogsTable.tsx` - Virtualized table
- `AuditLogFilters.tsx` - Filter controls
- `AuditLogDetailDialog.tsx` - View log details
- `AuditLogExport.tsx` - CSV export button

---

## Option C: Testing & Quality

### E2E Tests (Playwright)

```typescript
// tests/e2e/tenant-registration.spec.ts
test('should complete web tenant registration', async ({ page }) => {
  await page.goto('/cloud/register');
  await page.fill('[name="tenantName"]', 'Test Retail Co.');
  await page.fill('[name="tenantEmail"]', 'test@example.com');
  // ... fill all fields
  await page.click('button[type="submit"]');
  await expect(page.locator('.success-message')).toBeVisible();
  await expect(page.locator('.api-key')).toBeVisible();
});

// tests/e2e/installer-wizard.spec.ts
test('should complete installer setup wizard', async ({ page }) => {
  // Mock first-run detection
  await page.addInitScript(() => {
    localStorage.removeItem('retailhq_setup_complete');
  });

  await page.goto('/');
  await expect(page).toHaveURL('/setup');

  // Step through wizard
  await page.click('text=Get Started');
  await page.click('text=Cloud-enabled');
  await page.click('text=Next');

  // API key entry
  await page.fill('[name="apiKey"]', 'a1b2c3d4e5f6...');
  await page.click('text=Next');

  // Shop selection
  await page.click('text=Main Street Store');
  await page.click('text=Next');

  // Complete
  await expect(page.locator('text=Setup Complete')).toBeVisible();
  await page.click('text=Go to Dashboard');
  await expect(page).toHaveURL('/dashboard');
});

// tests/e2e/api-keys-management.spec.ts
// tests/e2e/subscription-management.spec.ts
// tests/e2e/analytics-dashboard.spec.ts
```

---

### Load Testing (k6)

```javascript
// load-tests/registration-spike.js
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '1m', target: 50 },   // Ramp up
    { duration: '5m', target: 100 },  // Sustained load
    { duration: '1m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95% under 500ms
    http_req_failed: ['rate<0.01'],    // <1% errors
  },
};

export default function () {
  const payload = JSON.stringify({
    tenantName: `Test Tenant ${__VU}-${__ITER}`,
    tenantEmail: `test${__VU}${__ITER}@example.com`,
    // ... other fields
  });

  const res = http.post('http://localhost:8081/api/registration/tenants', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'has api key': (r) => r.json('apiKey') !== undefined,
  });
}

// load-tests/analytics-dashboard.js
// load-tests/api-key-validation.js
```

---

## Option D: Documentation & Deployment

### API Documentation (OpenAPI/Swagger)

```java
// Add to controllers
@Tag(name = "API Keys", description = "Manage tenant API keys")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "401", description = "Unauthorized"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
```

### Deployment Guides

1. **Update DEPLOYMENT_GUIDE.md:**
   - New environment variables for cloud mode
   - Database migration procedures
   - Monitoring setup (Prometheus metrics)

2. **Create Kubernetes manifests:**
   - Update helm-chart with new services
   - ConfigMaps for cloud configuration
   - Secrets for API keys, payment gateway

3. **CI/CD Pipeline:**
   - GitHub Actions workflow
   - Build, test, deploy stages
   - Automatic rollback on failure

---

## Execution Timeline

**Week 1-2: Option A (Backend)**
- Days 1-3: API Keys implementation
- Days 4-6: Subscriptions & Billing
- Days 7-10: Analytics Aggregation
- Days 11-14: Integration tests + bug fixes

**Week 3: Option B (Frontend Phase 6.1)**
- Days 15-17: Tenant Settings page
- Days 18-20: Shop Management page
- Days 21: Audit Logs page

**Week 4: Option C (Testing)**
- Days 22-24: E2E tests (Playwright)
- Days 25-26: Load testing (k6)
- Day 27: Security testing

**Week 5: Option D (Documentation & Deployment)**
- Days 28-29: API docs, deployment guides
- Day 30: Staging deployment
- Day 31-35: Production deployment + monitoring

---

## Next Steps

1. **Review this plan** with the team
2. **Start with Phase A.1** (API Keys) - lowest risk, immediate value
3. **Create feature branch** for each phase
4. **Write tests first** (TDD) for backend services
5. **Deploy to staging** after each phase completes
6. **Production rollout** after all options complete

---

**Document Version:** 1.0
**Last Updated:** 2026-01-04
**Author:** Shop Manager Development Team
