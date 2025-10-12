# API Integration Guide

**Version**: 1.0
**Last Updated**: January 2025

---

## Quick Reference

- **Base URL**: `http://localhost:8081` (dev), `https://api.shopmanager.com` (prod)
- **Authentication**: Bearer JWT tokens from Keycloak
- **HTTP Client**: Axios
- **Server State**: React Query (@tanstack/react-query)

---

## Axios Configuration

See FRONTEND_ARCHITECTURE.md for complete Axios setup with interceptors.

---

## API Service Pattern

All API calls go through service layer files in `src/services/`:

```typescript
// src/services/shopService.ts
import api from '@/lib/axios';
import type { ShopResponse, ShopCreateRequest, PaginatedResponse } from '@/types/api';

export const shopService = {
  async getShops(params?: Record<string, any>): Promise<PaginatedResponse<ShopResponse>> {
    const { data } = await api.get('/api/shops', { params });
    return data;
  },

  async getShopById(shopId: string): Promise<ShopResponse> {
    const { data } = await api.get(`/api/shops/${shopId}`);
    return data;
  },

  async createShop(request: ShopCreateRequest): Promise<ShopResponse> {
    const { data } = await api.post('/api/shops', request);
    return data;
  },

  async updateShop(shopId: string, request: ShopUpdateRequest): Promise<ShopResponse> {
    const { data } = await api.put(`/api/shops/${shopId}`, request);
    return data;
  },

  async updateStatus(shopId: string, status: string): Promise<ShopResponse> {
    const { data } = await api.patch(`/api/shops/${shopId}/status`, null, { params: { status } });
    return data;
  },

  async deleteShop(shopId: string): Promise<void> {
    await api.delete(`/api/shops/${shopId}`);
  },
};
```

---

## React Query Hooks Pattern

Refer to FRONTEND_ARCHITECTURE.md for complete React Query examples with:
- Query keys pattern
- useQuery hooks
- useMutation hooks with optimistic updates
- Cache invalidation strategies

---

## Complete API Reference

### Tenant Management APIs

```
POST   /api/v1/public/registration/tenant
GET    /api/v1/public/registration/check-tenant-name
GET    /api/v1/public/registration/check-email
GET    /api/v1/admin/tenants/pending
GET    /api/v1/admin/tenants/{tenantId}
POST   /api/v1/admin/tenants/{tenantId}/activate
```

### Shop Management APIs

```
GET    /api/shops
POST   /api/shops
GET    /api/shops/{shopId}
PUT    /api/shops/{shopId}
PATCH  /api/shops/{shopId}/status
DELETE /api/shops/{shopId}
GET    /api/shops/active
```

### Inventory APIs

```
POST   /api/v1/shops/{shopId}/inventory
GET    /api/v1/shops/{shopId}/inventory
GET    /api/v1/inventory/{inventoryId}
PUT    /api/v1/inventory/{inventoryId}/adjust-stock
POST   /api/v1/inventory/{inventoryId}/reserve
POST   /api/v1/inventory/{inventoryId}/release
PUT    /api/v1/inventory/{inventoryId}/status
GET    /api/v1/inventory/{inventoryId}/history
GET    /api/v1/shops/{shopId}/inventory/low-stock
GET    /api/v1/shops/{shopId}/inventory/expiring
GET    /api/v1/shops/{shopId}/inventory/total-value
GET    /api/v1/shops/{shopId}/inventory/summary
```

### Investment APIs

```
POST   /api/v1/investments
GET    /api/v1/shops/{shopId}/investments
GET    /api/v1/my-investments
GET    /api/v1/investments/{investmentId}
PUT    /api/v1/investments/{investmentId}/status
POST   /api/v1/investments/{investmentId}/withdraw
GET    /api/v1/investments/{investmentId}/distributions
GET    /api/v1/my-distributions
POST   /api/v1/distributions/{distributionId}/approve
POST   /api/v1/distributions/{distributionId}/mark-paid
```

### Expense APIs

```
POST   /api/v1/shops/{shopId}/expenses
PUT    /api/v1/expenses/{expenseId}
GET    /api/v1/expenses/{expenseId}
GET    /api/v1/shops/{shopId}/expenses
POST   /api/v1/expenses/{expenseId}/approve
POST   /api/v1/expenses/{expenseId}/reject
DELETE /api/v1/expenses/{expenseId}
GET    /api/v1/shops/{shopId}/expenses/summary
```

### Fraud Detection APIs

```
GET    /api/v1/fraud/alerts
GET    /api/v1/fraud/alerts/{alertId}
POST   /api/v1/fraud/alerts/{alertId}/acknowledge
POST   /api/v1/fraud/alerts/{alertId}/resolve
POST   /api/v1/fraud/alerts/{alertId}/false-positive
GET    /api/v1/fraud/risk-assessments
POST   /api/v1/fraud/risk-assessments/{assessmentId}/approve
POST   /api/v1/fraud/risk-assessments/{assessmentId}/reject
GET    /api/v1/fraud/rules
POST   /api/v1/fraud/rules
PUT    /api/v1/fraud/rules/{ruleId}
DELETE /api/v1/fraud/rules/{ruleId}
PUT    /api/v1/fraud/rules/{ruleId}/status
GET    /api/v1/fraud/statistics
```

### Product Returns APIs

```
POST   /api/shops/{shopId}/returns
POST   /api/shops/{shopId}/returns/{returnId}/process
GET    /api/shops/{shopId}/returns
```

### Receipt APIs

```
POST   /api/receipts/generate/{transactionId}
GET    /api/receipts/{receiptId}
GET    /api/receipts/by-number/{receiptNumber}
GET    /api/receipts/transaction/{transactionId}
GET    /api/receipts/{receiptId}/content
GET    /api/receipts/{receiptId}/printable
POST   /api/receipts/{receiptId}/mark-printed
POST   /api/receipts/{receiptId}/mark-emailed
POST   /api/receipts/regenerate/{transactionId}
```

### Analytics APIs

```
GET    /api/analytics/sales-summary
GET    /api/analytics/investment-roi
GET    /api/analytics/fraud-statistics
GET    /api/analytics/revenue-analytics
POST   /api/analytics/clear-cache/{shopId}
```

### User APIs

```
GET    /api/users/profile
```

---

## Error Handling

### Error Response Format

```typescript
interface ErrorResponse {
  success: false;
  message: string;
  errors?: Array<{
    field: string;
    message: string;
  }>;
  timestamp: string;
}
```

### HTTP Status Codes

- `200` - Success
- `201` - Created
- `204` - No Content (successful deletion)
- `400` - Bad Request (validation error)
- `401` - Unauthorized (not authenticated)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (duplicate resource)
- `500` - Internal Server Error

---

## File Upload

```typescript
async function uploadReceipt(file: File, expenseId: string) {
  const formData = new FormData();
  formData.append('file', file);

  const { data } = await api.post(
    `/api/v1/expenses/${expenseId}/receipt`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total!
        );
        console.log(`Upload progress: ${percentCompleted}%`);
      },
    }
  );

  return data;
}
```

---

## Retry & Cancellation

### Request Cancellation

```typescript
import { useEffect } from 'react';

function MyComponent() {
  useEffect(() => {
    const controller = new AbortController();

    api.get('/api/shops', {
      signal: controller.signal,
    });

    return () => {
      controller.abort();
    };
  }, []);
}
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
