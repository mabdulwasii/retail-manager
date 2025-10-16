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
POST   /api/public/registration/tenant
GET    /api/public/registration/check-tenant-name
GET    /api/public/registration/check-email
GET    /api/admin/tenants/pending
GET    /api/admin/tenants/{tenantId}
POST   /api/admin/tenants/{tenantId}/activate
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
POST   /api/shops/{shopId}/inventory
GET    /api/shops/{shopId}/inventory
GET    /api/inventory/{inventoryId}
PUT    /api/inventory/{inventoryId}/adjust-stock
POST   /api/inventory/{inventoryId}/reserve
POST   /api/inventory/{inventoryId}/release
PUT    /api/inventory/{inventoryId}/status
GET    /api/inventory/{inventoryId}/history
GET    /api/shops/{shopId}/inventory/low-stock
GET    /api/shops/{shopId}/inventory/expiring
GET    /api/shops/{shopId}/inventory/total-value
GET    /api/shops/{shopId}/inventory/summary
```

### Investment APIs

```
POST   /api/investments
GET    /api/shops/{shopId}/investments
GET    /api/my-investments
GET    /api/investments/{investmentId}
PUT    /api/investments/{investmentId}/status
POST   /api/investments/{investmentId}/withdraw
GET    /api/investments/{investmentId}/distributions
GET    /api/my-distributions
POST   /api/distributions/{distributionId}/approve
POST   /api/distributions/{distributionId}/mark-paid
```

### Expense APIs

```
POST   /api/shops/{shopId}/expenses
PUT    /api/expenses/{expenseId}
GET    /api/expenses/{expenseId}
GET    /api/shops/{shopId}/expenses
POST   /api/expenses/{expenseId}/approve
POST   /api/expenses/{expenseId}/reject
DELETE /api/expenses/{expenseId}
GET    /api/shops/{shopId}/expenses/summary
```

### Fraud Detection APIs

```
GET    /api/fraud/alerts
GET    /api/fraud/alerts/{alertId}
POST   /api/fraud/alerts/{alertId}/acknowledge
POST   /api/fraud/alerts/{alertId}/resolve
POST   /api/fraud/alerts/{alertId}/false-positive
GET    /api/fraud/risk-assessments
POST   /api/fraud/risk-assessments/{assessmentId}/approve
POST   /api/fraud/risk-assessments/{assessmentId}/reject
GET    /api/fraud/rules
POST   /api/fraud/rules
PUT    /api/fraud/rules/{ruleId}
DELETE /api/fraud/rules/{ruleId}
PUT    /api/fraud/rules/{ruleId}/status
GET    /api/fraud/statistics
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
    `/api/expenses/${expenseId}/receipt`,
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
