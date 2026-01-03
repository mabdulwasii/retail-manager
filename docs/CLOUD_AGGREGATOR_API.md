# Cloud Aggregator API Documentation

API documentation for RetailHQ Cloud Aggregator - central registration and analytics service for local retail installations.

---

## Overview

The **Cloud Aggregator API** provides centralized registration, tracking, and analytics for RetailHQ local installations. It enables:

- **Tenant Registration**: Register retail businesses with multiple shops
- **API Key Management**: Secure authentication for shop-to-cloud communication
- **Shop Linking**: Add/remove shops from registered tenants
- **Subscription Management**: Track subscription tiers and usage
- **Analytics Foundation**: Collect data for business intelligence

**Base URL**: `https://api.retailhq.app`

---

## Architecture

### Deployment Model

```
┌─────────────────────────────────────────────────┐
│         RetailHQ Cloud (PaaS Model)             │
│                                                  │
│   ┌──────────────────────────────────────────┐  │
│   │      Cloud Aggregator API                │  │
│   │  https://api.retailhq.app                │  │
│   │                                          │  │
│   │  - Tenant registration                   │  │
│   │  - API key management                    │  │
│   │  - Shop tracking                         │  │
│   │  - Subscription tiers                    │  │
│   │  - Analytics & reporting (future)        │  │
│   └──────────────────────────────────────────┘  │
│                      ▲                           │
│                      │                           │
│   ┌──────────────────┴──────────────────────┐   │
│   │       PostgreSQL Database               │   │
│   │  - cloud_tenants                        │   │
│   │  - cloud_shops                          │   │
│   └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
              ▲            ▲            ▲
              │            │            │
    ┌─────────┴─┐    ┌─────┴────┐    ┌─┴─────────┐
    │  Store 1  │    │ Store 2  │    │  Store 3  │
    │ (Windows) │    │ (Docker) │    │ (macOS)   │
    │ Embedded  │    │  Lite    │    │ Embedded  │
    └───────────┘    └──────────┘    └───────────┘
```

### Key Concepts

- **Cloud Tenant**: A retail business (company) registered in the cloud
- **Cloud Shop**: A physical store/location belonging to a cloud tenant
- **API Key**: Secure token for shop-to-cloud authentication (`rhq_` prefix)
- **Subscription Tier**: Usage level (FREE, BASIC, PREMIUM, ENTERPRISE)
- **Status**: Tenant/shop status (ACTIVE, SUSPENDED, INACTIVE)

---

## Authentication

### API Key Authentication

Most endpoints require API key authentication via the `X-API-Key` header.

**Format**: `rhq_` + 32 random alphanumeric characters

**Example**: `rhq_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`

**Usage**:

```bash
curl -X POST https://api.retailhq.app/api/registration/shops \
  -H "X-API-Key: rhq_your_api_key_here" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### Public Endpoints

The following endpoints do **NOT** require authentication:

- `POST /api/registration/tenants` - Tenant registration (returns API key)
- `GET /api/registration/health` - Health check

---

## API Endpoints

### 1. Tenant Registration

Register a new retail business (tenant) with one or more shops.

**Endpoint**: `POST /api/registration/tenants`

**Authentication**: None (public endpoint)

**Request Body**:

```json
{
  "tenantName": "ABC Retail Group",
  "tenantEmail": "contact@abcretail.com",
  "companyRegistration": "REG123456",
  "taxId": "TAX789012",
  "address": "123 Business Ave",
  "city": "New York",
  "country": "USA",
  "phoneNumber": "+1-555-123-4567",
  "shops": [
    {
      "shopName": "ABC Downtown Store",
      "shopEmail": "downtown@abcretail.com",
      "address": "456 Main St",
      "city": "New York",
      "country": "USA",
      "phoneNumber": "+1-555-987-6543"
    },
    {
      "shopName": "ABC Uptown Branch",
      "shopEmail": "uptown@abcretail.com",
      "address": "789 Park Ave",
      "city": "New York",
      "phoneNumber": "+1-555-555-1234"
    }
  ]
}
```

**Required Fields**:
- `tenantName` (string, 1-200 chars): Business name
- `tenantEmail` (string, valid email): Contact email (must be unique)
- `shops` (array, min 1 shop): List of shops to register

**Optional Fields**:
- `companyRegistration` (string): Company registration number
- `taxId` (string): Tax identification number
- `address`, `city`, `country`, `phoneNumber`: Tenant contact info

**Shop Fields**:
- `shopName` (required): Name of the store
- `shopEmail` (optional): Shop-specific email
- `address`, `city`, `country`, `phoneNumber`: Shop location info

**Success Response** (201 Created):

```json
{
  "cloudTenantId": "ct_1234567890abcdef",
  "apiKey": "rhq_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6",
  "registeredShopsCount": 2,
  "message": "Tenant successfully registered with 2 shop(s). Please save your API key securely."
}
```

**Important**: Store the API key securely - it cannot be retrieved later!

**Error Responses**:

```json
// 400 Bad Request - Validation error
{
  "status": 400,
  "error": "Bad Request",
  "message": "At least one shop must be provided",
  "path": "/api/registration/tenants"
}

// 409 Conflict - Duplicate email
{
  "status": 409,
  "error": "Conflict",
  "message": "Tenant with email contact@abcretail.com already registered",
  "path": "/api/registration/tenants"
}
```

**cURL Example**:

```bash
curl -X POST https://api.retailhq.app/api/registration/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "My Retail Business",
    "tenantEmail": "owner@myretail.com",
    "companyRegistration": "REG12345",
    "taxId": "TAX67890",
    "address": "123 Main St",
    "city": "Springfield",
    "country": "USA",
    "phoneNumber": "555-0123",
    "shops": [
      {
        "shopName": "Main Store",
        "shopEmail": "mainstore@myretail.com",
        "address": "456 Store Ave",
        "city": "Springfield",
        "phoneNumber": "555-0456"
      }
    ]
  }'
```

---

### 2. Link Additional Shop

Add a new shop to an existing tenant.

**Endpoint**: `POST /api/registration/shops`

**Authentication**: Required (`X-API-Key` header)

**Request Body**:

```json
{
  "cloudTenantId": "ct_1234567890abcdef",
  "shop": {
    "shopName": "ABC Westside Store",
    "shopEmail": "westside@abcretail.com",
    "address": "321 West Blvd",
    "city": "Los Angeles",
    "country": "USA",
    "phoneNumber": "+1-555-222-3333"
  }
}
```

**Required Fields**:
- `cloudTenantId` (string): ID of the tenant (from registration response)
- `shop.shopName` (string): Name of the new shop

**Success Response** (201 Created):

```json
{
  "message": "Shop successfully linked to tenant"
}
```

**Error Responses**:

```json
// 401 Unauthorized - Missing API key
{
  "status": 401,
  "error": "Unauthorized",
  "message": "API key is required",
  "path": "/api/registration/shops"
}

// 401 Unauthorized - Invalid API key
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid API key",
  "path": "/api/registration/shops"
}

// 404 Not Found - Tenant not found
{
  "status": 404,
  "error": "Not Found",
  "message": "Cloud tenant not found with ID: ct_invalid",
  "path": "/api/registration/shops"
}
```

**cURL Example**:

```bash
curl -X POST https://api.retailhq.app/api/registration/shops \
  -H "X-API-Key: rhq_your_api_key_here" \
  -H "Content-Type: application/json" \
  -d '{
    "cloudTenantId": "ct_1234567890abcdef",
    "shop": {
      "shopName": "New Branch Store",
      "shopEmail": "branch@myretail.com",
      "address": "789 Branch Rd",
      "city": "Springfield",
      "phoneNumber": "555-9999"
    }
  }'
```

---

### 3. Unregister Tenant

Remove a tenant and all associated shops from the cloud.

**Endpoint**: `DELETE /api/registration/tenants/{cloudTenantId}`

**Authentication**: Required (`X-API-Key` header)

**Path Parameters**:
- `cloudTenantId` (string): ID of the tenant to unregister

**Success Response** (200 OK):

```json
{
  "message": "Tenant and associated shops successfully unregistered"
}
```

**Error Responses**:

```json
// 401 Unauthorized - Invalid API key
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid API key for the specified tenant",
  "path": "/api/registration/tenants/ct_1234567890abcdef"
}

// 404 Not Found - Tenant not found
{
  "status": 404,
  "error": "Not Found",
  "message": "Cloud tenant not found with ID: ct_invalid",
  "path": "/api/registration/tenants/ct_invalid"
}
```

**cURL Example**:

```bash
curl -X DELETE https://api.retailhq.app/api/registration/tenants/ct_1234567890abcdef \
  -H "X-API-Key: rhq_your_api_key_here"
```

---

### 4. Health Check

Check API availability and status.

**Endpoint**: `GET /api/registration/health`

**Authentication**: None (public endpoint)

**Success Response** (200 OK):

```json
{
  "status": "operational",
  "message": "Cloud Aggregator API is running",
  "version": "1.0.0",
  "timestamp": "2026-01-03T12:00:00Z"
}
```

**cURL Example**:

```bash
curl https://api.retailhq.app/api/registration/health
```

---

## Data Models

### CloudTenant

Represents a retail business registered in the cloud.

```json
{
  "id": "ct_1234567890abcdef",
  "tenantName": "ABC Retail Group",
  "tenantEmail": "contact@abcretail.com",
  "companyRegistration": "REG123456",
  "taxId": "TAX789012",
  "address": "123 Business Ave",
  "city": "New York",
  "country": "USA",
  "phoneNumber": "+1-555-123-4567",
  "apiKeyHash": "$2a$10$...", // BCrypt hash (not exposed via API)
  "status": "ACTIVE",
  "subscriptionTier": "FREE",
  "shopCount": 3,
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-03T12:00:00Z"
}
```

**Status Values**:
- `ACTIVE`: Tenant is active and operational
- `SUSPENDED`: Temporarily suspended (billing issues, policy violations)
- `INACTIVE`: Deactivated (can be reactivated)

**Subscription Tiers**:
- `FREE`: Up to 3 shops, basic features
- `BASIC`: Up to 10 shops, standard features
- `PREMIUM`: Up to 50 shops, advanced features
- `ENTERPRISE`: Unlimited shops, full features

### CloudShop

Represents a physical store location.

```json
{
  "id": "cs_abcdef1234567890",
  "cloudTenantId": "ct_1234567890abcdef",
  "shopName": "ABC Downtown Store",
  "shopEmail": "downtown@abcretail.com",
  "address": "456 Main St",
  "city": "New York",
  "country": "USA",
  "phoneNumber": "+1-555-987-6543",
  "status": "ACTIVE",
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-01T10:00:00Z"
}
```

**Status Values**:
- `ACTIVE`: Shop is operational
- `INACTIVE`: Temporarily closed or deactivated

---

## Usage Examples

### Complete Registration Flow

```bash
# Step 1: Register tenant with shops
RESPONSE=$(curl -s -X POST https://api.retailhq.app/api/registration/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Sunshine Retail",
    "tenantEmail": "owner@sunshine.com",
    "companyRegistration": "REG999",
    "taxId": "TAX888",
    "address": "100 Sunny Ave",
    "city": "Miami",
    "country": "USA",
    "phoneNumber": "305-555-0100",
    "shops": [
      {
        "shopName": "Sunshine Beach Store",
        "shopEmail": "beach@sunshine.com",
        "address": "200 Beach Rd",
        "city": "Miami Beach",
        "phoneNumber": "305-555-0200"
      }
    ]
  }')

# Extract tenant ID and API key
TENANT_ID=$(echo $RESPONSE | jq -r '.cloudTenantId')
API_KEY=$(echo $RESPONSE | jq -r '.apiKey')

echo "Tenant ID: $TENANT_ID"
echo "API Key: $API_KEY"

# Save API key securely (example: to .env file)
echo "RETAILHQ_CLOUD_API_KEY=$API_KEY" >> ~/.retailhq.env
echo "RETAILHQ_CLOUD_TENANT_ID=$TENANT_ID" >> ~/.retailhq.env

# Step 2: Add another shop later
curl -X POST https://api.retailhq.app/api/registration/shops \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{
    \"cloudTenantId\": \"$TENANT_ID\",
    \"shop\": {
      \"shopName\": \"Sunshine Downtown Store\",
      \"shopEmail\": \"downtown@sunshine.com\",
      \"address\": \"300 Main St\",
      \"city\": \"Miami\",
      \"phoneNumber\": \"305-555-0300\"
    }
  }"

# Step 3: Verify health
curl https://api.retailhq.app/api/registration/health

# Step 4: Unregister (if needed)
# curl -X DELETE https://api.retailhq.app/api/registration/tenants/$TENANT_ID \
#   -H "X-API-Key: $API_KEY"
```

### Integration with Local Installation

```yaml
# Local installation config (application-embedded.yml or .env)
application:
  sync:
    enabled: true
    cloud-endpoint: https://api.retailhq.app
    api-key: rhq_your_api_key_here
    tenant-id: ct_1234567890abcdef
    schedule-cron: "0 0 * * * ?" # Daily at midnight
```

---

## Rate Limits

| Endpoint | Rate Limit | Burst |
|----------|-----------|-------|
| `POST /api/registration/tenants` | 10 requests/hour | 5 |
| `POST /api/registration/shops` | 100 requests/hour | 20 |
| `DELETE /api/registration/tenants/{id}` | 10 requests/hour | 5 |
| `GET /api/registration/health` | Unlimited | - |

**Rate Limit Headers**:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1641052800
```

**Rate Limit Exceeded Response** (429 Too Many Requests):

```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 3600 seconds.",
  "path": "/api/registration/shops"
}
```

---

## Error Handling

### Standard Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: tenantEmail must be a valid email",
  "path": "/api/registration/tenants",
  "timestamp": "2026-01-03T12:00:00Z"
}
```

### HTTP Status Codes

| Code | Meaning | Common Causes |
|------|---------|---------------|
| `200` | OK | Request successful |
| `201` | Created | Resource created successfully |
| `400` | Bad Request | Invalid input, validation errors |
| `401` | Unauthorized | Missing or invalid API key |
| `404` | Not Found | Tenant or shop not found |
| `409` | Conflict | Duplicate tenant email |
| `429` | Too Many Requests | Rate limit exceeded |
| `500` | Internal Server Error | Server-side error |

---

## Security Best Practices

### API Key Management

✅ **DO**:
- Store API keys in environment variables or secure vaults (HashiCorp Vault, AWS Secrets Manager)
- Use HTTPS for all API requests
- Rotate API keys periodically (contact support for key rotation)
- Monitor API key usage for anomalies

❌ **DON'T**:
- Hardcode API keys in source code
- Commit API keys to version control
- Share API keys between different environments (dev/staging/prod)
- Expose API keys in client-side code or public repositories

### Example Secure Storage

```bash
# .env file (not committed to git)
RETAILHQ_CLOUD_API_KEY=rhq_your_api_key_here
RETAILHQ_CLOUD_TENANT_ID=ct_1234567890abcdef

# Add to .gitignore
echo ".env" >> .gitignore
echo "*.env" >> .gitignore
```

```java
// Java: Read from environment
String apiKey = System.getenv("RETAILHQ_CLOUD_API_KEY");
String tenantId = System.getenv("RETAILHQ_CLOUD_TENANT_ID");
```

---

## Testing

### Sandbox Environment

**Base URL**: `https://sandbox.api.retailhq.app` (coming soon)

Use the sandbox environment for development and testing without affecting production data.

### Postman Collection

Download our Postman collection: [RetailHQ Cloud Aggregator API.postman_collection.json](https://api.retailhq.app/docs/postman)

### Health Check Test

```bash
# Simple health check
curl -f https://api.retailhq.app/api/registration/health || echo "API is down"
```

---

## Frontend Integration

### Unified Frontend Architecture

The Cloud Aggregator API integrates with a **single unified frontend application** that serves both:

1. **Local Shop Mode** (`localhost:3001`, `shop.myretail.com`) - Shop operations
2. **Cloud Portal Mode** (`cloud.retailhq.app`) - Multi-shop aggregation and analytics

```
┌───────────────────────────────────────────────────────┐
│         Single Frontend App (shop-manager/frontend/)   │
├───────────────────────────────────────────────────────┤
│  Runtime Mode Detection:                              │
│  - isEmbeddedMode  → Local standalone deployment      │
│  - isCloudMode     → Cloud portal (cloud.retailhq.app)│
│                                                        │
│  Existing Features (All Modes):                       │
│  ✓ Dashboard, Products, Inventory                     │
│  ✓ Sales & POS                                        │
│  ✓ Investments, Users, Shops                          │
│  ✓ Analytics, Receipts                                │
│                                                        │
│  Cloud-Specific Features (Cloud Mode Only):           │
│  ⏳ Cloud Tenants Management (Planned)                │
│  ⏳ Cross-Shop Analytics (Planned)                    │
│  ⏳ Subscription Management (Planned)                 │
│  ⏳ API Key Management UI (Planned)                   │
└───────────────────────────────────────────────────────┘
```

### Domain Configuration

| Domain | Frontend Mode | Backend API | Purpose |
|--------|--------------|-------------|---------|
| `localhost:3001` | Embedded | `localhost:8081` | Local development |
| `shop.myretail.com` | LocalShop | `api.myretail.com` | Single shop deployment |
| `cloud.retailhq.app` | Cloud | `api.retailhq.app` | Cloud portal (HQ) |

### Cloud Portal Features (Upcoming)

The following cloud-specific pages will be added to the existing frontend:

1. **Cloud Tenants** (`/cloud/tenants`)
   - List all registered cloud tenants
   - Tenant details (name, email, shop count, subscription)
   - Manage tenant status (ACTIVE, SUSPENDED, INACTIVE)

2. **Cross-Shop Analytics** (`/cloud/analytics`)
   - Aggregated sales across all shops
   - Top performing shops
   - Product trends and insights
   - Revenue forecasting

3. **Subscription Management** (`/cloud/subscriptions`)
   - View subscription tiers (FREE, BASIC, PREMIUM, ENTERPRISE)
   - Upgrade/downgrade flows
   - Billing history
   - Payment management

4. **API Key Management** (`/cloud/api-keys`)
   - View API key usage
   - Rotate API keys
   - Revoke compromised keys

### JavaScript/TypeScript Integration

```typescript
// Frontend API service for Cloud Aggregator
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://api.retailhq.app/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Register tenant
export const registerTenant = async (data: TenantRegistrationRequest) => {
  const response = await api.post('/registration/tenants', data);
  return response.data; // { cloudTenantId, apiKey, registeredShopsCount, message }
};

// Link shop (requires API key)
export const linkShop = async (
  cloudTenantId: string,
  shop: ShopData,
  apiKey: string
) => {
  const response = await api.post('/registration/shops',
    { cloudTenantId, shop },
    { headers: { 'X-API-Key': apiKey } }
  );
  return response.data;
};

// Get cloud tenants (for cloud portal dashboard)
export const getCloudTenants = async () => {
  const response = await api.get('/aggregator/tenants'); // Future endpoint
  return response.data;
};
```

### React Component Example

```tsx
// Cloud Tenants Management Page (Planned)
import { useQuery } from '@tanstack/react-query';
import { cloudAggregatorService } from '@/services/cloudAggregatorService';

export const CloudTenantsPage: React.FC = () => {
  const { data: tenants, isLoading } = useQuery({
    queryKey: ['cloud-tenants'],
    queryFn: cloudAggregatorService.listTenants,
  });

  if (isLoading) return <LoadingSpinner />;

  return (
    <div>
      <h1>Cloud Tenants</h1>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Tenant Name</TableHead>
            <TableHead>Email</TableHead>
            <TableHead>Shops</TableHead>
            <TableHead>Tier</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {tenants?.map((tenant) => (
            <TableRow key={tenant.id}>
              <TableCell>{tenant.tenantName}</TableCell>
              <TableCell>{tenant.tenantEmail}</TableCell>
              <TableCell>{tenant.shopCount}</TableCell>
              <TableCell>{tenant.subscriptionTier}</TableCell>
              <TableCell>{tenant.status}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};
```

---

## Support & Resources

- **Documentation**: [docs.retailhq.app](https://docs.retailhq.app)
- **API Status**: [status.retailhq.app](https://status.retailhq.app)
- **Support Email**: support@retailhq.app
- **GitHub Issues**: [github.com/retailhq/issues](https://github.com/yourorg/shop-manager/issues)
- **Changelog**: [RELEASE_NOTES.md](../RELEASE_NOTES_STANDALONE.md)

---

## Changelog

### v1.0.0 (2026-01-03)

**Backend (API)**:
- ✅ Initial release of Cloud Aggregator API
- ✅ Tenant registration endpoint
- ✅ Shop linking endpoint
- ✅ Tenant unregistration endpoint
- ✅ API key authentication (BCrypt hashing)
- ✅ Health check endpoint
- ✅ Support for multiple subscription tiers
- ✅ Database schema (cloud_tenants, cloud_shops tables)
- ✅ Comprehensive tests (28 tests, 99.6% passing)

**Frontend (Cloud Portal)**:
- ⏳ Cloud tenants management UI (Planned)
- ⏳ Cross-shop analytics dashboard (Planned)
- ⏳ Subscription management interface (Planned)
- ⏳ API key management UI (Planned)

---

**Last Updated**: January 2026
**API Version**: 1.0.0
**Frontend Integration**: Planned for v1.1.0
