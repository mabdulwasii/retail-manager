# Shop Manager - Cloud Sync Architecture

This document describes the architecture, implementation, and data flow of Shop Manager's cloud sync feature for embedded installations.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Components](#components)
- [Registration Flow](#registration-flow)
- [Sync Mechanism](#sync-mechanism)
- [Data Flow](#data-flow)
- [API Specification](#api-specification)
- [Security](#security)
- [Offline Mode](#offline-mode)
- [Troubleshooting](#troubleshooting)

---

## Overview

Cloud sync enables embedded Shop Manager installations to sync transaction data to a cloud aggregator for multi-shop analytics and reporting.

### Use Cases

1. **Multi-Location Business**: Aggregate sales data from multiple shop locations
2. **Franchise Operations**: Centralized reporting across franchise locations
3. **Backup & Recovery**: Cloud backup of critical transaction data
4. **Business Intelligence**: Cross-location analytics and insights

### Key Principles

- **Offline-First**: Application functions fully without cloud connectivity
- **Periodic Sync**: Scheduled hourly sync (configurable)
- **Tenant Isolation**: Complete data isolation per tenant
- **API Key Authentication**: Secure key-based authentication
- **Idempotent**: Safe to retry sync operations

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    EMBEDDED INSTALLATION                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Frontend (React)                       │  │
│  │                                                            │  │
│  │  ┌─────────────────┐  ┌──────────────────────────────┐  │  │
│  │  │ Settings UI     │  │ Cloud Registration Modal     │  │  │
│  │  │ - Enable/Disable│  │ - Tenant Name                │  │  │
│  │  │ - Test Conn     │  │ - Contact Email              │  │  │
│  │  │ - Manual Sync   │  │ - Shop Info                  │  │  │
│  │  │ - View Status   │  │ - Auto-generate API Key      │  │  │
│  │  └─────────────────┘  └──────────────────────────────┘  │  │
│  │                              │                            │  │
│  └──────────────────────────────┼────────────────────────────┘  │
│                                 │ HTTP/REST                     │
│  ┌──────────────────────────────▼────────────────────────────┐  │
│  │                  Backend (Spring Boot)                     │  │
│  │                                                            │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │         CloudSyncController                        │  │  │
│  │  │  /api/cloud-sync/config                            │  │  │
│  │  │  /api/cloud-sync/status                            │  │  │
│  │  │  /api/cloud-sync/register (NEW)                    │  │  │
│  │  │  /api/cloud-sync/enable                            │  │  │
│  │  │  /api/cloud-sync/sync                              │  │  │
│  │  └────────────────┬───────────────────────────────────┘  │  │
│  │                   │                                       │  │
│  │  ┌────────────────▼───────────────────────────────────┐  │  │
│  │  │     CloudRegistrationService                       │  │  │
│  │  │  - registerTenant()                                │  │  │
│  │  │  - linkShop()                                      │  │  │
│  │  │  - unregisterTenant()                              │  │  │
│  │  └────────────────┬───────────────────────────────────┘  │  │
│  │                   │                                       │  │
│  │  ┌────────────────▼───────────────────────────────────┐  │  │
│  │  │     CloudSyncService                               │  │  │
│  │  │  - syncTransactions()                              │  │  │
│  │  │  - syncInventory()                                 │  │  │
│  │  │  - batchProcessing()                               │  │  │
│  │  └────────────────┬───────────────────────────────────┘  │  │
│  │                   │                                       │  │
│  │  ┌────────────────▼───────────────────────────────────┐  │  │
│  │  │     CloudSyncScheduler (@Scheduled)                │  │  │
│  │  │  - Hourly sync (configurable cron)                 │  │  │
│  │  │  - Retry with exponential backoff                  │  │  │
│  │  │  - Manual trigger support                          │  │  │
│  │  └────────────────┬───────────────────────────────────┘  │  │
│  │                   │                                       │  │
│  │  ┌────────────────▼───────────────────────────────────┐  │  │
│  │  │     CloudSyncConfig (Entity)                       │  │  │
│  │  │  - cloudTenantId                                   │  │  │
│  │  │  - cloudApiKey (encrypted)                         │  │  │
│  │  │  - cloudApiUrl                                     │  │  │
│  │  │  - syncEnabled                                     │  │  │
│  │  │  - lastSyncAt                                      │  │  │
│  │  └────────────────┬───────────────────────────────────┘  │  │
│  │                   │                                       │  │
│  │  ┌────────────────▼───────────────────────────────────┐  │  │
│  │  │     Embedded PostgreSQL                            │  │  │
│  │  │  - cloud_sync_config                               │  │  │
│  │  │  - cloud_sync_log                                  │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────────┘  │
└─────────────────────────┼───────────────────────────────────┘
                          │
                          │ HTTPS / REST API
                          │ X-API-Key: rhq_...
                          │
┌─────────────────────────▼───────────────────────────────────┐
│               CLOUD AGGREGATOR (api.retailhq.app)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          AggregatorController                        │  │
│  │  POST   /api/registration/tenants (public)           │  │
│  │  POST   /api/registration/shops (API key)            │  │
│  │  DELETE /api/registration/tenants/{id} (API key)     │  │
│  │  GET    /api/registration/latest-version (public)    │  │
│  │  GET    /api/registration/health (public)            │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                         │
│  ┌────────────────▼─────────────────────────────────────┐  │
│  │          CloudTenantService                          │  │
│  │  - registerTenant() → Generate API Key               │  │
│  │  - linkShop()                                        │  │
│  │  - validateApiKey()                                  │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                         │
│  ┌────────────────▼─────────────────────────────────────┐  │
│  │     CloudTenant, CloudShop (Entities)                │  │
│  │  - cloud_tenants                                     │  │
│  │  - cloud_shops                                       │  │
│  │  - api_keys (hashed with BCrypt)                    │  │
│  └────────────────┬─────────────────────────────────────┘  │
│                   │                                         │
│  ┌────────────────▼─────────────────────────────────────┐  │
│  │     PostgreSQL (Cloud)                               │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Components

### Embedded Installation Components

#### 1. CloudSyncController
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/controller/CloudSyncController.java`

**Responsibilities**:
- Expose REST API for cloud sync configuration
- Handle registration, enable/disable, manual sync
- Return sync status and configuration

**Key Endpoints**:
```java
GET    /api/cloud-sync/config       // Get configuration
GET    /api/cloud-sync/status       // Get sync status
POST   /api/cloud-sync/register     // Register with cloud
PUT    /api/cloud-sync/config       // Update configuration
POST   /api/cloud-sync/enable       // Enable sync
POST   /api/cloud-sync/disable      // Disable sync
POST   /api/cloud-sync/sync         // Manual sync trigger
DELETE /api/cloud-sync/unregister   // Unregister from cloud
```

#### 2. CloudRegistrationService
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/service/CloudRegistrationService.java`

**Responsibilities**:
- Register tenant with cloud aggregator
- Call cloud API to create account
- Store returned API key locally
- Link additional shops to existing cloud tenant

**Key Methods**:
```java
CloudSyncConfig registerTenant(String tenantId, String cloudApiUrl)
void linkShop(String tenantId, String shopId)
void unregisterTenant(String tenantId)
```

#### 3. CloudSyncService
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/sync/service/CloudSyncService.java`

**Responsibilities**:
- Execute sync operations
- Batch process transactions
- Handle errors and retries
- Update sync status

#### 4. CloudSyncScheduler
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/sync/service/CloudSyncScheduler.java`

**Responsibilities**:
- Scheduled sync execution (cron)
- Manual sync trigger
- Concurrent sync management per tenant

**Configuration**:
```properties
CLOUD_SYNC_CRON=0 0 * * * ?  # Hourly (minute 0, every hour)
```

#### 5. CloudSyncConfig (Entity)
**Location**: `backend/src/main/java/com/princely/shopmanager/embedded/domain/CloudSyncConfig.java`

**Schema**:
```sql
CREATE TABLE cloud_sync_config (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL UNIQUE,
    cloud_tenant_id VARCHAR(255),
    cloud_api_key VARCHAR(500),  -- Encrypted
    cloud_api_url VARCHAR(500),
    sync_enabled BOOLEAN DEFAULT false,
    sync_status VARCHAR(50),
    last_sync_at TIMESTAMP,
    last_error TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Cloud Aggregator Components

#### 1. AggregatorController
**Location**: `backend/src/main/java/com/princely/shopmanager/aggregator/controller/AggregatorController.java`

**Responsibilities**:
- Public registration endpoint (no auth)
- Shop linking endpoint (API key auth)
- Health check endpoint

#### 2. CloudTenantService
**Location**: Aggregator module

**Responsibilities**:
- Create cloud tenant records
- Generate and hash API keys (BCrypt)
- Validate API keys
- Link shops to tenants

#### 3. CloudTenant, CloudShop (Entities)
**Entities**: Registration records for embedded installations

---

## Registration Flow

### Scenario 1: New Account Registration (AUTO_REGISTER)

**Installer sets**:
```properties
CLOUD_API_KEY=AUTO_REGISTER
CLOUD_SYNC_REQUIRED=true
```

**On First Run**:
```
1. Application detects AUTO_REGISTER marker
2. Reads tenant + shop bootstrap data
3. Calls POST /api/cloud-sync/register
4. CloudRegistrationService builds registration request:
   {
     "cloudApiUrl": "https://api.retailhq.app",
     "tenantName": "Default Organization",
     "tenantEmail": "contact@retailhq.local",
     "shops": [
       {
         "shopName": "Main Shop",
         "shopEmail": "shop@retailhq.local",
         ...
       }
     ]
   }
5. Cloud API creates CloudTenant + CloudShop records
6. Cloud API generates API key (format: rhq_<random>)
7. Cloud API returns: { cloudTenantId, apiKey, message }
8. Local saves CloudSyncConfig with real API key
9. Replaces AUTO_REGISTER with actual key in .env (optional)
```

**Backend Code**:
```java
// CloudRegistrationService.registerTenant()
TenantRegistrationRequest request = TenantRegistrationRequest.builder()
    .tenantName(tenant.getName())
    .tenantEmail(tenant.getContactEmail())
    .shops(shops.stream().map(...).toList())
    .build();

TenantRegistrationResponse response = sendRegistrationRequest(cloudApiUrl, request);

CloudSyncConfig config = CloudSyncConfig.builder()
    .tenantId(tenantId)
    .cloudTenantId(response.cloudTenantId())
    .cloudApiKey(response.apiKey())
    .cloudApiUrl(cloudApiUrl)
    .syncEnabled(true)
    .build();
```

### Scenario 2: Existing API Key

**Installer sets**:
```properties
CLOUD_API_KEY=rhq_abc123def456...
CLOUD_SYNC_REQUIRED=true
```

**On First Run**:
```
1. Application detects valid API key (starts with rhq_)
2. Creates CloudSyncConfig with provided key
3. First sync validates key with cloud
4. If invalid, sets sync_status=FAILED, logs error
```

### Scenario 3: Settings UI Registration

**User Flow**:
```
1. User navigates to Settings → Cloud Sync
2. Clicks "Register New Account" button
3. Modal prompts for shop/tenant details (optional, uses defaults)
4. Frontend calls POST /api/cloud-sync/register
5. Backend follows same flow as Scenario 1
6. Frontend displays generated API key (copy button)
7. Config automatically saved
```

---

## Sync Mechanism

### Scheduled Sync

**Cron Expression**: `0 0 * * * ?` (every hour at minute 0)

**Process**:
```
1. CloudSyncScheduler.scheduledSync() triggered
2. For each tenant with syncEnabled=true:
   a. Check cloud connectivity
   b. Call CloudSyncService.syncTransactions()
   c. Batch process (default: 1000 records per batch)
   d. Update CloudSyncConfig.lastSyncAt
   e. Log sync result to cloud_sync_log table
```

**Configuration**:
```properties
CLOUD_SYNC_CRON=0 0 * * * ?          # Hourly
CLOUD_SYNC_BATCH_SIZE=1000           # Records per batch
```

### Manual Sync

**Trigger**:
```
POST /api/cloud-sync/sync
```

**Process**:
```
1. Validate user has SYSTEM_ADMIN, TENANT_ADMIN, or OWNER role
2. Call CloudSyncScheduler.triggerManualSyncForTenant(tenantId)
3. Execute sync immediately (async)
4. Return 200 OK { message: "Manual sync started", started: true }
```

### Retry Logic

**Exponential Backoff**:
```
Attempt 1: Immediate
Attempt 2: 30 seconds
Attempt 3: 60 seconds
Attempt 4: 120 seconds
Attempt 5: Fail (log error)
```

**Error Handling**:
- Network errors: Retry with backoff
- 401 Unauthorized: Disable sync, log error (invalid API key)
- 500 Server Error: Retry with backoff
- Connection timeout: Retry with backoff

---

## Data Flow

### Transaction Sync

**Data Sent**:
```json
{
  "transactions": [
    {
      "transactionId": "uuid",
      "shopId": "uuid",
      "totalAmount": 150.00,
      "paymentMethod": "CASH",
      "timestamp": "2026-01-06T10:30:00Z",
      "items": [
        {
          "productId": "uuid",
          "productName": "Product A",
          "quantity": 2,
          "unitPrice": 75.00
        }
      ]
    }
  ],
  "syncTimestamp": "2026-01-06T11:00:00Z"
}
```

**API Endpoint**:
```
POST https://api.retailhq.app/api/sync/transactions
Headers:
  X-API-Key: rhq_abc123def456...
  Content-Type: application/json
```

**Response**:
```json
{
  "processed": 15,
  "failed": 0,
  "message": "Successfully synced 15 transactions"
}
```

---

## API Specification

### Embedded → Cloud Aggregator

#### Register Tenant
```
POST /api/registration/tenants
Content-Type: application/json

Request:
{
  "tenantName": "My Business",
  "tenantEmail": "contact@mybusiness.com",
  "companyRegistration": "12345",
  "taxId": "TAX-123",
  "address": "123 Main St",
  "city": "Springfield",
  "country": "USA",
  "shops": [
    {
      "shopName": "Main Shop",
      "shopEmail": "shop@mybusiness.com",
      "address": "123 Main St",
      "city": "Springfield",
      "country": "USA",
      "phoneNumber": "1-800-000-0000"
    }
  ]
}

Response: 201 Created
{
  "cloudTenantId": "cloud-tenant-uuid",
  "apiKey": "rhq_abc123def456ghi789...",
  "message": "Tenant registered successfully"
}
```

#### Link Additional Shop
```
POST /api/registration/shops
Headers:
  X-API-Key: rhq_abc123def456...
Content-Type: application/json

Request:
{
  "cloudTenantId": "cloud-tenant-uuid",
  "shop": {
    "shopName": "Second Location",
    "shopEmail": "shop2@mybusiness.com",
    ...
  }
}

Response: 201 Created
{
  "cloudShopId": "cloud-shop-uuid",
  "message": "Shop linked successfully"
}
```

#### Unregister Tenant
```
DELETE /api/registration/tenants/{cloudTenantId}
Headers:
  X-API-Key: rhq_abc123def456...

Response: 204 No Content
```

---

## Security

### API Key Format

**Format**: `rhq_<40-character-random-string>`

**Example**: `rhq_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0`

**Generation** (Cloud Side):
```java
String apiKey = "rhq_" + RandomStringUtils.randomAlphanumeric(40);
String hashedKey = BCryptPasswordEncoder.encode(apiKey);
// Store hashedKey in database
// Return plain apiKey to embedded installation (once only)
```

### Storage

**Embedded Installation**:
- Stored in `cloud_sync_config` table
- Should be encrypted at rest (future enhancement)
- Never logged in plain text
- Never returned in API responses (masked)

**Cloud Aggregator**:
- Stored hashed with BCrypt
- Validation: `BCrypt.matches(providedKey, storedHash)`

### Transmission

- HTTPS only (TLS 1.2+)
- Sent in `X-API-Key` header
- Never in URL parameters
- Never in logs

### Rotation

**Process**:
1. User revokes old key in cloud portal
2. Cloud generates new key
3. User manually updates key in embedded installation
4. Next sync validates new key

---

## Offline Mode

### Behavior

**When cloud unreachable**:
- Application functions normally
- Transactions saved locally
- Sync attempts fail gracefully
- Errors logged to `cloud_sync_log`
- Automatic retry on next schedule

**Configuration**:
```properties
CLOUD_ALLOW_OFFLINE=true  # Default: true
```

### Queue Management

**Sync Status**:
- `CONFIGURED`: Ready to sync
- `SYNCING`: Currently syncing
- `FAILED`: Last sync failed
- `NOT_CONFIGURED`: No config

**Pending Transactions**:
- Tracked via `last_sync_at` timestamp
- Sync sends all transactions since last successful sync
- Idempotent: Duplicate sends are safe

---

## Troubleshooting

### Sync Not Working

**Check sync status**:
```bash
curl http://localhost:8081/api/cloud-sync/status \
  -H "Authorization: Bearer <jwt-token>"
```

**Common issues**:
1. **syncEnabled=false**: Enable in settings or .env
2. **Invalid API key**: Check format, check revocation
3. **Network blocked**: Check firewall, proxy
4. **Cloud unavailable**: Check cloud status page

### Registration Failing

**Check logs**:
```bash
# Linux
sudo journalctl -u shop-manager | grep "CloudRegistration"

# Windows
type "C:\Program Files\Shop Manager\data\logs\shop-manager.log" | findstr "CloudRegistration"

# macOS
tail -f ~/.shopmanager/data/logs/shop-manager.log | grep "CloudRegistration"
```

**Common errors**:
- `CLOUD_REGISTRATION_FAILED`: Network issue or invalid data
- `CLOUD_SYNC_UNAVAILABLE`: Cloud service down
- `BUSINESS_RULE_VIOLATION`: Already registered

### Test Connection

**API Test**:
```bash
curl -X POST https://api.retailhq.app/api/registration/health
```

**Manual Sync**:
```bash
curl -X POST http://localhost:8081/api/cloud-sync/sync \
  -H "Authorization: Bearer <jwt-token>"
```

---

## Support

- **Documentation**: [docs/INSTALLER_FEATURES.md](./INSTALLER_FEATURES.md)
- **Cloud Portal**: https://cloud.retailhq.app
- **Issues**: https://github.com/yourorg/shop-manager/issues

---

**Last Updated**: 2026-01-06
