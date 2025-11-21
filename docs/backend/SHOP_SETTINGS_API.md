# Shop Settings API Documentation

**Version**: 1.0
**Last Updated**: January 2025
**Base URL**: `/api/shops/{shopId}`

---

## Overview

The Shop Settings API provides comprehensive endpoints for managing shop configuration and customization settings. Settings are divided into two categories:

1. **Configuration** - Business settings (currency, tax rates, feature toggles)
2. **Customization** - Branding and visual settings (colors, logos, themes)

---

## Table of Contents

1. [Shop Configuration Endpoints](#shop-configuration-endpoints)
2. [Shop Customization Endpoints](#shop-customization-endpoints)
3. [Request/Response Examples](#requestresponse-examples)
4. [Error Handling](#error-handling)

---

## Shop Configuration Endpoints

### Get Shop Configuration

Retrieves business configuration settings for a shop.

```
GET /api/shops/{shopId}/configuration
```

**Authorization**: SYSTEM_ADMIN, TENANT_ADMIN, OWNER, MANAGER

**Response**: `ShopConfigurationResponse`

```json
{
  "investmentEnabled": true,
  "analyticsEnabled": true,
  "fraudDetectionEnabled": false,
  "autoBackupEnabled": true,
  "currency": "NGN",
  "taxRate": 7.5,
  "maxDiscountPercentage": 20.0,
  "receiptFooter": "Thank you for your patronage!"
}
```

---

### Update Shop Configuration

Updates business configuration settings for a shop. Supports partial updates.

```
PUT /api/shops/{shopId}/configuration
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Request Body**: `ShopConfigurationRequest`

```json
{
  "investmentEnabled": true,
  "analyticsEnabled": true,
  "fraudDetectionEnabled": true,
  "autoBackupEnabled": true,
  "currency": "USD",
  "taxRate": 8.0,
  "maxDiscountPercentage": 15.0,
  "receiptFooter": "Visit us again!"
}
```

**Response**: `ShopResponse` (includes updated configuration)

**Validation Rules**:
- `currency`: Must be 3-letter code (e.g., NGN, USD, EUR)
- `taxRate`: 0-100%
- `maxDiscountPercentage`: 0-100%
- `receiptFooter`: Max 500 characters

---

## Shop Customization Endpoints

### Get Shop Customization

Retrieves branding and visual customization settings for a shop.

```
GET /api/shops/{shopId}/customization
```

**Authorization**: SYSTEM_ADMIN, TENANT_ADMIN, OWNER, MANAGER

**Response**: `ShopCustomizationResponse`

```json
{
  "id": "custom-123e4567",
  "shopId": "shop-123e4567",
  "primaryColor": "#007bff",
  "secondaryColor": "#6c757d",
  "accentColor": "#28a745",
  "backgroundColor": "#ffffff",
  "textColor": "#212529",
  "logoUrl": "https://cdn.example.com/logo.png",
  "faviconUrl": "https://cdn.example.com/favicon.ico",
  "bannerImageUrl": null,
  "backgroundImageUrl": null,
  "websiteUrl": "https://www.myshop.com",
  "socialMediaLinks": "{\"facebook\":\"@myshop\",\"instagram\":\"@myshop\"}",
  "themeVariant": "LIGHT",
  "fontFamily": "Inter, sans-serif",
  "fontSize": "MEDIUM",
  "borderRadius": 8,
  "customStyles": null,
  "dashboardLayout": "GRID",
  "receiptHeader": "Welcome to Our Store!",
  "receiptFooter": "Thank you for shopping with us!",
  "receiptShowLogo": true,
  "showBanner": true,
  "enableAnimations": true,
  "showAdvancedFeatures": false
}
```

---

### Update Shop Customization

Creates or updates customization settings. Supports partial updates.

```
PUT /api/shops/{shopId}/customization
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Request Body**: `ShopCustomizationRequest`

```json
{
  "primaryColor": "#1e40af",
  "secondaryColor": "#64748b",
  "accentColor": "#10b981",
  "themeVariant": "DARK",
  "fontSize": "LARGE",
  "dashboardLayout": "CARD",
  "receiptShowLogo": true,
  "enableAnimations": false
}
```

**Response**: `ShopCustomizationResponse`

**Validation Rules**:
- **Colors**: Must be valid hex format (`#RRGGBB`)
- **themeVariant**: `LIGHT`, `DARK`, `AUTO`
- **fontSize**: `SMALL`, `MEDIUM`, `LARGE`
- **dashboardLayout**: `GRID`, `LIST`, `CARD`
- **URLs**: Max 500 characters
- **receiptHeader/Footer**: Max 1000 characters

---

### Update Color Scheme

Updates color scheme (primary, secondary, accent).

```
PATCH /api/shops/{shopId}/customization/colors
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Query Parameters**:
- `primaryColor` (optional): Hex color (e.g., `#007bff`)
- `secondaryColor` (optional): Hex color
- `accentColor` (optional): Hex color

**Example**:
```
PATCH /api/shops/shop-123/customization/colors?primaryColor=%23007bff&secondaryColor=%236c757d
```

**Response**: `ShopCustomizationResponse`

---

### Update Theme Settings

Updates theme variant and font size.

```
PATCH /api/shops/{shopId}/customization/theme
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Query Parameters**:
- `themeVariant` (optional): `LIGHT`, `DARK`, `AUTO`
- `fontSize` (optional): `SMALL`, `MEDIUM`, `LARGE`

**Example**:
```
PATCH /api/shops/shop-123/customization/theme?themeVariant=DARK&fontSize=LARGE
```

**Response**: `ShopCustomizationResponse`

---

### Upload Logo

Uploads a logo image for the shop.

```
POST /api/shops/{shopId}/customization/logo
Content-Type: multipart/form-data
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Form Data**:
- `file`: Logo image file (PNG, JPG, SVG)

**Example (cURL)**:
```bash
curl -X POST "http://localhost:8081/api/shops/shop-123/customization/logo" \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/logo.png"
```

**Response**: `ShopCustomizationResponse` (with updated `logoUrl`)

**File Requirements**:
- Max size: 5MB (configurable)
- Supported formats: PNG, JPG, JPEG, SVG
- Recommended dimensions: 200x200px to 400x400px

---

### Update Contact Information

Updates website URL and social media links.

```
PATCH /api/shops/{shopId}/customization/contact
```

**Authorization**: SYSTEM_ADMIN, OWNER, MANAGER

**Query Parameters**:
- `websiteUrl` (optional): Shop website URL
- `socialMediaLinks` (optional): JSON string with social media links

**Example**:
```
PATCH /api/shops/shop-123/customization/contact?websiteUrl=https://myshop.com
```

**Response**: `ShopCustomizationResponse`

---

### Reset to Default Customization

Resets all customization settings to default values.

```
DELETE /api/shops/{shopId}/customization
```

**Authorization**: SYSTEM_ADMIN, OWNER

**Response**: `ShopCustomizationResponse` (with default values)

**Default Values**:
- Theme: `LIGHT`
- Font Size: `MEDIUM`
- Dashboard Layout: `GRID`
- Show Banner: `true`
- Enable Animations: `true`
- Show Advanced Features: `false`
- Receipt Show Logo: `true`

---

## Request/Response Examples

### Example 1: Update Shop Configuration

**Request**:
```http
PUT /api/shops/shop-abc123/configuration
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "currency": "USD",
  "taxRate": 8.5,
  "maxDiscountPercentage": 25.0,
  "fraudDetectionEnabled": true
}
```

**Response** (200 OK):
```json
{
  "id": "shop-abc123",
  "name": "Downtown Electronics",
  "tenantId": "tenant-xyz",
  "address": "123 Main St",
  "city": "New York",
  "status": "ACTIVE",
  "configuration": {
    "investmentEnabled": true,
    "analyticsEnabled": true,
    "fraudDetectionEnabled": true,
    "autoBackupEnabled": true,
    "currency": "USD",
    "taxRate": 8.5,
    "maxDiscountPercentage": 25.0,
    "receiptFooter": "Thank you for your patronage!"
  },
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2025-01-21T15:30:00"
}
```

---

### Example 2: Update Shop Customization (Complete)

**Request**:
```http
PUT /api/shops/shop-abc123/customization
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "primaryColor": "#1e40af",
  "secondaryColor": "#64748b",
  "accentColor": "#10b981",
  "backgroundColor": "#ffffff",
  "textColor": "#0f172a",
  "logoUrl": "https://cdn.myshop.com/logo.png",
  "websiteUrl": "https://www.myshop.com",
  "themeVariant": "LIGHT",
  "fontFamily": "Poppins, sans-serif",
  "fontSize": "MEDIUM",
  "borderRadius": 12,
  "dashboardLayout": "GRID",
  "receiptHeader": "Welcome to Downtown Electronics!",
  "receiptFooter": "Your satisfaction is our priority!",
  "receiptShowLogo": true,
  "showBanner": true,
  "enableAnimations": true,
  "showAdvancedFeatures": false
}
```

**Response** (200 OK):
```json
{
  "id": "custom-xyz789",
  "shopId": "shop-abc123",
  "primaryColor": "#1e40af",
  "secondaryColor": "#64748b",
  "accentColor": "#10b981",
  "backgroundColor": "#ffffff",
  "textColor": "#0f172a",
  "logoUrl": "https://cdn.myshop.com/logo.png",
  "faviconUrl": null,
  "bannerImageUrl": null,
  "backgroundImageUrl": null,
  "websiteUrl": "https://www.myshop.com",
  "socialMediaLinks": null,
  "themeVariant": "LIGHT",
  "fontFamily": "Poppins, sans-serif",
  "fontSize": "MEDIUM",
  "borderRadius": 12,
  "customStyles": null,
  "dashboardLayout": "GRID",
  "receiptHeader": "Welcome to Downtown Electronics!",
  "receiptFooter": "Your satisfaction is our priority!",
  "receiptShowLogo": true,
  "showBanner": true,
  "enableAnimations": true,
  "showAdvancedFeatures": false
}
```

---

### Example 3: Partial Update (Colors Only)

**Request**:
```http
PATCH /api/shops/shop-abc123/customization/colors?primaryColor=%231e40af&accentColor=%2310b981
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response** (200 OK):
```json
{
  "id": "custom-xyz789",
  "shopId": "shop-abc123",
  "primaryColor": "#1e40af",
  "secondaryColor": "#6c757d",
  "accentColor": "#10b981",
  ...
}
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Invalid Color Format
```json
{
  "timestamp": "2025-01-21T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Primary color must be a valid hex color (e.g., #007bff)",
  "path": "/api/shops/shop-abc123/customization"
}
```

#### 400 Bad Request - Invalid Tax Rate
```json
{
  "timestamp": "2025-01-21T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Tax rate must be non-negative",
  "path": "/api/shops/shop-abc123/configuration"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2025-01-21T15:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "path": "/api/shops/shop-abc123/configuration"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2025-01-21T15:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions - requires OWNER or MANAGER role",
  "path": "/api/shops/shop-abc123/configuration"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2025-01-21T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shop not found: shop-abc123",
  "path": "/api/shops/shop-abc123/configuration"
}
```

---

## Frontend Integration

### React/TypeScript Example

```typescript
// API Service
const shopSettingsApi = {
  // Get configuration
  getConfiguration: async (shopId: string) => {
    const response = await fetch(`/api/shops/${shopId}/configuration`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.json();
  },

  // Update configuration
  updateConfiguration: async (shopId: string, config: ShopConfigurationRequest) => {
    const response = await fetch(`/api/shops/${shopId}/configuration`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(config)
    });
    return response.json();
  },

  // Get customization
  getCustomization: async (shopId: string) => {
    const response = await fetch(`/api/shops/${shopId}/customization`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.json();
  },

  // Update customization
  updateCustomization: async (shopId: string, customization: ShopCustomizationRequest) => {
    const response = await fetch(`/api/shops/${shopId}/customization`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(customization)
    });
    return response.json();
  },

  // Upload logo
  uploadLogo: async (shopId: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`/api/shops/${shopId}/customization/logo`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: formData
    });
    return response.json();
  },

  // Update theme
  updateTheme: async (shopId: string, themeVariant: string, fontSize: string) => {
    const params = new URLSearchParams({ themeVariant, fontSize });
    const response = await fetch(`/api/shops/${shopId}/customization/theme?${params}`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token}` }
    });
    return response.json();
  }
};
```

---

## Testing

### cURL Examples

```bash
# Get configuration
curl -X GET "http://localhost:8081/api/shops/shop-123/configuration" \
  -H "Authorization: Bearer {token}"

# Update configuration
curl -X PUT "http://localhost:8081/api/shops/shop-123/configuration" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "currency": "USD",
    "taxRate": 8.5,
    "maxDiscountPercentage": 25.0
  }'

# Get customization
curl -X GET "http://localhost:8081/api/shops/shop-123/customization" \
  -H "Authorization: Bearer {token}"

# Update customization
curl -X PUT "http://localhost:8081/api/shops/shop-123/customization" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "primaryColor": "#1e40af",
    "themeVariant": "DARK",
    "fontSize": "LARGE"
  }'

# Update colors
curl -X PATCH "http://localhost:8081/api/shops/shop-123/customization/colors?primaryColor=%231e40af" \
  -H "Authorization: Bearer {token}"

# Upload logo
curl -X POST "http://localhost:8081/api/shops/shop-123/customization/logo" \
  -H "Authorization: Bearer {token}" \
  -F "file=@logo.png"

# Reset to defaults
curl -X DELETE "http://localhost:8081/api/shops/shop-123/customization" \
  -H "Authorization: Bearer {token}"
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
