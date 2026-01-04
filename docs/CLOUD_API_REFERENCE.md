# Cloud API Reference

**Version:** 1.1.0
**Base URL:** `https://api.shopmanager.com` or `http://localhost:8081` (development)
**Last Updated:** January 2026

---

## Table of Contents

- [Authentication](#authentication)
- [Tenant Management](#tenant-management)
- [Shop Management](#shop-management)
- [API Key Management](#api-key-management)
- [Subscription Management](#subscription-management)
- [Analytics](#analytics)
- [Audit Logs](#audit-logs)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)

---

## Authentication

All Cloud API endpoints require authentication using **JWT tokens** or **API Keys**.

### JWT Authentication

**Obtain Token:**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "your_password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

**Using JWT:**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     https://api.shopmanager.com/api/cloud/tenants/{tenantId}
```

### API Key Authentication

**Using API Key:**
```bash
curl -H "X-API-Key: sk_live_1234567890abcdef..." \
     https://api.shopmanager.com/api/cloud/tenants/{tenantId}/shops
```

---

## Tenant Management

### Get Tenant Settings

**Endpoint:** `GET /api/cloud/tenants/{tenantId}`

**Description:** Retrieve tenant configuration and settings.

**Path Parameters:**
- `tenantId` (string, required): Tenant identifier

**Response:**
```json
{
  "id": "tenant-123",
  "name": "Acme Retail Corp",
  "email": "admin@acmeretail.com",
  "registrationNumber": "BN-123456",
  "taxId": "VAT-GB-987654321",
  "address": {
    "street": "123 Main Street",
    "city": "London",
    "state": "Greater London",
    "postalCode": "SW1A 1AA",
    "country": "United Kingdom"
  },
  "phone": "+44 20 7946 0958",
  "timezone": "Europe/London",
  "locale": "en-GB",
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2026-01-04T10:30:00Z"
}
```

**curl Example:**
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  https://api.shopmanager.com/api/cloud/tenants/tenant-123
```

---

### Update Tenant Settings

**Endpoint:** `PUT /api/cloud/tenants/{tenantId}`

**Description:** Update tenant configuration.

**Request Body:**
```json
{
  "name": "Acme Retail Corp",
  "email": "admin@acmeretail.com",
  "registrationNumber": "BN-123456",
  "taxId": "VAT-GB-987654321",
  "address": {
    "street": "456 New Street",
    "city": "Manchester",
    "state": "Greater Manchester",
    "postalCode": "M1 1AA",
    "country": "United Kingdom"
  },
  "phone": "+44 161 123 4567",
  "timezone": "Europe/London",
  "locale": "en-GB"
}
```

**Response:** `200 OK` with updated tenant object

**curl Example:**
```bash
curl -X PUT \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Retail Corp",
    "email": "admin@acmeretail.com",
    "timezone": "Europe/London"
  }' \
  https://api.shopmanager.com/api/cloud/tenants/tenant-123
```

---

## Shop Management

### List All Shops

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/shops`

**Description:** Retrieve all shops for a tenant.

**Query Parameters:**
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 20, max: 100)
- `status` (string, optional): Filter by status (ACTIVE, INACTIVE)
- `search` (string, optional): Search by shop name

**Response:**
```json
{
  "content": [
    {
      "id": "shop-123",
      "name": "Downtown Store",
      "email": "downtown@acmeretail.com",
      "phone": "+44 161 123 4567",
      "address": {
        "street": "456 High Street",
        "city": "Manchester",
        "state": "Greater Manchester",
        "postalCode": "M1 1AA",
        "country": "United Kingdom"
      },
      "status": "ACTIVE",
      "tenantId": "tenant-123",
      "createdAt": "2025-06-01T00:00:00Z",
      "updatedAt": "2026-01-04T10:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 12,
  "totalPages": 1
}
```

**curl Example:**
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "https://api.shopmanager.com/api/cloud/tenants/tenant-123/shops?page=0&size=20&status=ACTIVE"
```

---

### Get Shop Details

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/shops/{shopId}`

**Response:** Single shop object (same structure as list item)

---

### Create Shop

**Endpoint:** `POST /api/cloud/tenants/{tenantId}/shops`

**Request Body:**
```json
{
  "name": "Airport Branch",
  "email": "airport@acmeretail.com",
  "phone": "+44 161 999 8888",
  "address": {
    "street": "Terminal 1",
    "city": "Manchester",
    "postalCode": "M90 1QX",
    "country": "United Kingdom"
  }
}
```

**Response:** `201 Created` with shop object

**curl Example:**
```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Airport Branch",
    "email": "airport@acmeretail.com"
  }' \
  https://api.shopmanager.com/api/cloud/tenants/tenant-123/shops
```

---

### Update Shop

**Endpoint:** `PUT /api/cloud/tenants/{tenantId}/shops/{shopId}`

**Request Body:** Same as create (all fields optional for update)

**Response:** `200 OK` with updated shop object

---

### Activate/Deactivate Shop

**Endpoint:** `PATCH /api/cloud/tenants/{tenantId}/shops/{shopId}/status`

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

**Response:** `200 OK` with updated shop object

**curl Example:**
```bash
curl -X PATCH \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "INACTIVE"}' \
  https://api.shopmanager.com/api/cloud/tenants/tenant-123/shops/shop-123/status
```

---

## API Key Management

### List API Keys

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/api-keys`

**Response:**
```json
{
  "content": [
    {
      "id": "key-123",
      "name": "Mobile App - Production",
      "keyPrefix": "sk_live_1234...5678",
      "permissions": ["READ_WRITE"],
      "status": "ACTIVE",
      "lastUsedAt": "2026-01-04T09:15:00Z",
      "expiresAt": "2026-12-31T23:59:59Z",
      "createdAt": "2026-01-01T00:00:00Z"
    }
  ],
  "totalElements": 3
}
```

---

### Generate API Key

**Endpoint:** `POST /api/cloud/tenants/{tenantId}/api-keys`

**Request Body:**
```json
{
  "name": "Mobile App - Production",
  "permissions": ["READ_WRITE"],
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

**Response:**
```json
{
  "id": "key-123",
  "name": "Mobile App - Production",
  "key": "sk_live_1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcd",
  "permissions": ["READ_WRITE"],
  "status": "ACTIVE",
  "expiresAt": "2026-12-31T23:59:59Z",
  "createdAt": "2026-01-04T10:30:00Z"
}
```

**Important:** The `key` field is only returned once. Store it securely.

---

### Revoke API Key

**Endpoint:** `DELETE /api/cloud/tenants/{tenantId}/api-keys/{keyId}`

**Response:** `204 No Content`

**curl Example:**
```bash
curl -X DELETE \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  https://api.shopmanager.com/api/cloud/tenants/tenant-123/api-keys/key-123
```

---

## Subscription Management

### Get Current Subscription

**Endpoint:** `GET /api/cloud/subscriptions/{tenantId}`

**Response:**
```json
{
  "id": "sub-123",
  "tenantId": "tenant-123",
  "tier": "PRO",
  "status": "ACTIVE",
  "billingCycle": "MONTHLY",
  "currentPeriodStart": "2026-01-01T00:00:00Z",
  "currentPeriodEnd": "2026-02-01T00:00:00Z",
  "limits": {
    "maxShops": 20,
    "maxUsers": 50,
    "maxStorageGB": 50,
    "maxApiCallsPerMonth": 50000
  },
  "usage": {
    "shops": 12,
    "users": 35,
    "storageGB": 28.5,
    "apiCallsThisMonth": 15234
  }
}
```

---

### Get Billing History

**Endpoint:** `GET /api/cloud/billing/{tenantId}/invoices`

**Query Parameters:**
- `startDate` (ISO 8601, optional): Filter from date
- `endDate` (ISO 8601, optional): Filter to date

**Response:**
```json
{
  "invoices": [
    {
      "id": "inv-123",
      "invoiceNumber": "INV-2026-01-001",
      "date": "2026-01-01T00:00:00Z",
      "amount": 99.00,
      "currency": "USD",
      "status": "PAID",
      "pdfUrl": "https://api.shopmanager.com/invoices/inv-123.pdf"
    }
  ],
  "totalAmount": 1188.00
}
```

---

## Analytics

### Get Tenant Analytics

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/analytics`

**Query Parameters:**
- `periodStart` (ISO 8601, required): Start of analysis period
- `periodEnd` (ISO 8601, required): End of analysis period

**Response:**
```json
{
  "period": {
    "start": "2025-12-01T00:00:00Z",
    "end": "2026-01-01T00:00:00Z"
  },
  "summary": {
    "totalRevenue": 125000.00,
    "totalSales": 1250,
    "totalShops": 12,
    "activeShops": 11,
    "totalUsers": 35
  },
  "shopPerformance": [
    {
      "shopId": "shop-123",
      "shopName": "Downtown Store",
      "revenue": 45000.00,
      "sales": 450,
      "averageOrderValue": 100.00
    }
  ]
}
```

**curl Example:**
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "https://api.shopmanager.com/api/cloud/tenants/tenant-123/analytics?periodStart=2025-12-01T00:00:00Z&periodEnd=2026-01-01T00:00:00Z"
```

---

### Get Shop Sync Status

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/analytics/sync-status`

**Response:**
```json
{
  "shops": [
    {
      "shopId": "shop-123",
      "shopName": "Downtown Store",
      "lastSyncAt": "2026-01-04T10:25:00Z",
      "syncStatus": "SUCCESS",
      "recordsSynced": 1234
    }
  ],
  "lastGlobalSync": "2026-01-04T10:30:00Z"
}
```

---

## Audit Logs

### Get Audit Logs

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/audit-logs`

**Query Parameters:**
- `page` (integer, optional): Page number (default: 0)
- `size` (integer, optional): Page size (default: 50, max: 200)
- `action` (string, optional): Filter by action (CREATE, UPDATE, DELETE, LOGIN, SYNC)
- `entityType` (string, optional): Filter by entity (SHOP, USER, PRODUCT, etc.)
- `startDate` (ISO 8601, optional): Filter from date
- `endDate` (ISO 8601, optional): Filter to date
- `userId` (string, optional): Filter by user
- `shopId` (string, optional): Filter by shop

**Response:**
```json
{
  "content": [
    {
      "id": "log-123",
      "timestamp": "2026-01-04T10:30:00Z",
      "action": "CREATE",
      "entityType": "SHOP",
      "entityId": "shop-123",
      "entityName": "Downtown Store",
      "userId": "user-456",
      "username": "john.doe@acmeretail.com",
      "ipAddress": "192.168.1.100",
      "details": "Created new shop: Downtown Store",
      "changes": {
        "name": "Downtown Store",
        "email": "downtown@acmeretail.com",
        "status": "ACTIVE"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 1250,
  "totalPages": 25
}
```

**curl Example:**
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "https://api.shopmanager.com/api/cloud/tenants/tenant-123/audit-logs?action=CREATE&entityType=SHOP&page=0&size=50"
```

---

### Export Audit Logs

**Endpoint:** `GET /api/cloud/tenants/{tenantId}/audit-logs/export`

**Query Parameters:** Same as Get Audit Logs

**Response:** CSV file download

**Headers:**
```
Content-Type: text/csv
Content-Disposition: attachment; filename="audit-logs-2026-01-04.csv"
```

**curl Example:**
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  "https://api.shopmanager.com/api/cloud/tenants/tenant-123/audit-logs/export?startDate=2026-01-01T00:00:00Z&endDate=2026-01-04T23:59:59Z" \
  -o audit-logs.csv
```

---

## Error Handling

### HTTP Status Codes

| Code | Description |
|------|-------------|
| **200** | OK - Request successful |
| **201** | Created - Resource created |
| **204** | No Content - Delete successful |
| **400** | Bad Request - Invalid parameters |
| **401** | Unauthorized - Missing/invalid authentication |
| **403** | Forbidden - Insufficient permissions |
| **404** | Not Found - Resource doesn't exist |
| **409** | Conflict - Duplicate resource |
| **429** | Too Many Requests - Rate limit exceeded |
| **500** | Internal Server Error |
| **503** | Service Unavailable |

### Error Response Format

```json
{
  "timestamp": "2026-01-04T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Shop name is required",
  "path": "/api/cloud/tenants/tenant-123/shops",
  "errors": [
    {
      "field": "name",
      "message": "must not be blank",
      "rejectedValue": null
    }
  ]
}
```

### Common Error Scenarios

**Invalid JWT Token:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired JWT token"
}
```

**Insufficient Permissions:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "User does not have permission to access this resource"
}
```

**Resource Not Found:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Shop with ID 'shop-999' not found"
}
```

**Rate Limit Exceeded:**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Retry after 60 seconds.",
  "retryAfter": 60
}
```

---

## Rate Limiting

### Limits by Subscription Tier

| Tier | Requests/Minute | Requests/Hour | Requests/Day |
|------|-----------------|---------------|--------------|
| **FREE** | 10 | 500 | 10,000 |
| **BASIC** | 60 | 3,000 | 50,000 |
| **PRO** | 300 | 15,000 | 250,000 |
| **ENTERPRISE** | 1000 | 50,000 | 1,000,000 |

### Rate Limit Headers

All API responses include rate limit headers:

```
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 275
X-RateLimit-Reset: 1704365460
```

### Handling Rate Limits

**Recommended Approach:**
1. Monitor `X-RateLimit-Remaining` header
2. Implement exponential backoff when approaching limits
3. Use bulk endpoints where available
4. Cache responses when appropriate
5. Implement retry logic with delays

**Example Retry Logic:**
```python
import time
import requests

def call_api_with_retry(url, headers, max_retries=3):
    for attempt in range(max_retries):
        response = requests.get(url, headers=headers)

        if response.status_code == 429:
            retry_after = int(response.headers.get('Retry-After', 60))
            print(f"Rate limited. Retrying after {retry_after} seconds...")
            time.sleep(retry_after)
            continue

        return response

    raise Exception("Max retries exceeded")
```

---

## Best Practices

### 1. Use Pagination

Always use pagination for list endpoints:
```bash
GET /api/cloud/tenants/{tenantId}/shops?page=0&size=20
```

### 2. Filter at the API Level

Use query parameters to reduce payload size:
```bash
GET /api/cloud/tenants/{tenantId}/shops?status=ACTIVE&search=downtown
```

### 3. Cache Responses

Cache analytics and read-only data:
- Tenant settings: 5 minutes
- Shop list: 1 minute
- Analytics: 1 hour
- Subscription: 5 minutes

### 4. Use Bulk Operations

When available, use bulk endpoints instead of multiple single requests.

### 5. Monitor Rate Limits

Check headers and implement backoff before hitting limits.

### 6. Secure API Keys

- Store in environment variables
- Never commit to version control
- Rotate regularly (every 90 days)
- Use different keys for dev/prod

### 7. Handle Errors Gracefully

Implement proper error handling for all status codes.

### 8. Use Compression

Enable gzip compression for large responses:
```bash
curl -H "Accept-Encoding: gzip" ...
```

---

## Postman Collection

Import the [Cloud Portal API Postman Collection](../postman/Cloud-Portal-API.postman_collection.json) for easy testing.

**Includes:**
- Pre-configured requests for all 17 endpoints
- Environment variables for local/dev/prod
- Example requests with test data
- Automated authentication

---

## Support

- **API Status**: [https://status.shopmanager.com](https://status.shopmanager.com)
- **Documentation**: [https://docs.shopmanager.com](https://docs.shopmanager.com)
- **Support**: support@shopmanager.com
- **Community**: [https://community.shopmanager.com](https://community.shopmanager.com)

---

**Version**: 1.1.0
**Last Updated**: January 2026
**Maintained by**: Shop Manager API Team
