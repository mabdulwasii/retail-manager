# Product & Inventory Management Guide

**Version:** 2.0
**Last Updated:** January 2025
**Architecture:** Two-Tier Model (Product/Inventory Separation)

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [User Flows](#user-flows)
4. [API Reference](#api-reference)
5. [Business Logic](#business-logic)
6. [UX Guidelines](#ux-guidelines)
7. [Examples](#examples)

---

## Overview

Shop Manager uses a **two-tier model** for product and inventory management:

- **Product**: Master catalog representing *what you sell* (SKU, price, description)
- **Inventory**: Stock tracking representing *what you have* (batches, locations, expiry dates)

### Why Two-Tier?

| Feature | Benefit |
|---------|---------|
| **Batch Tracking** | Track different batches with unique expiry dates, costs, and properties |
| **Multi-Location** | Manage same product across warehouses, stores, and locations |
| **Expiry Management** | FEFO (First Expiry, First Out) selling to minimize waste |
| **Flexible Costing** | Different unit costs per batch due to price fluctuations |
| **Traceability** | Full audit trail per batch for product recalls |
| **Scalability** | Supports complex retail and pharmaceutical operations |

---

## Architecture

### Data Model

```
┌─────────────────────────────────────────────────────────────┐
│                         PRODUCT                              │
│  (Master Catalog - What You Sell)                          │
├─────────────────────────────────────────────────────────────┤
│ • id, sku, barcode                                          │
│ • name, description, category                               │
│ • price, costPrice                                          │
│ • supplier info, image                                      │
│ • status (ACTIVE, INACTIVE, DISCONTINUED)                   │
│                                                             │
│ NO STOCK FIELDS - Stock is in Inventory table              │
└─────────────────────────────────────────────────────────────┘
                           │
                           │ 1 Product → N Inventory Records
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                       INVENTORY                             │
│  (Stock Tracking - What You Have)                          │
├─────────────────────────────────────────────────────────────┤
│ • product_id (FK to Product)                                │
│ • shop_id, location                                         │
│ • currentStock, reservedStock                               │
│ • batchNumber, expiryDate                                   │
│ • unitCost, minimumStock, reorderPoint                      │
│ • status (ACTIVE, INACTIVE, QUARANTINED, EXPIRED)           │
└─────────────────────────────────────────────────────────────┘

Example:
  Product: "Coca-Cola 500ml" (SKU: COCA-500ML)
    ├─ Inventory Batch 1: 100 units, expires 2025-06-30, Main Store
    ├─ Inventory Batch 2: 150 units, expires 2025-12-31, Main Store
    └─ Inventory Batch 3: 80 units, expires 2025-09-15, Warehouse
  Total Available: 330 units across 3 batches
```

### Stock Aggregation

```java
// Product-level stock is aggregated from all inventory records
Product.totalStock = SUM(Inventory.currentStock) WHERE productId
Product.availableStock = SUM(Inventory.currentStock - reservedStock) WHERE productId AND status=ACTIVE
Product.reservedStock = SUM(Inventory.reservedStock) WHERE productId
```

---

## User Flows

### Flow 1: Create Product & Add Stock

#### Step 1: Create Product (Catalog Entry)

**Who:** Manager, Owner, Tenant Admin
**Endpoint:** `POST /api/shops/{shopId}/products`

**Request:**
```json
{
  "name": "Coca-Cola 500ml",
  "sku": "COCA-500ML",
  "barcode": "5449000000996",
  "categoryId": "beverage-category-id",
  "price": 500.00,
  "costPrice": 350.00,
  "unit": "bottle",
  "supplierName": "Coca-Cola Bottling",
  "isTaxable": true,
  "isDiscountable": true
}
```

**Response:**
```json
{
  "id": "product-uuid",
  "name": "Coca-Cola 500ml",
  "sku": "COCA-500ML",
  "price": 500.00,
  "status": "ACTIVE",
  "totalStock": 0,          // No stock yet!
  "availableStock": 0,
  "inventoryCount": 0
}
```

**Result:** Product created in catalog with **ZERO stock**.

---

#### Step 2: Add Inventory (Stock Receipt)

**Who:** Manager, Owner, Tenant Admin
**Endpoint:** `POST /api/shops/{shopId}/inventory`

**Request:**
```json
{
  "productId": "product-uuid",
  "currentStock": 240,
  "unitCost": 350.00,
  "batchNumber": "BATCH-2025-001",
  "expiryDate": "2025-12-31",
  "location": "Main Store",
  "minimumStock": 20,
  "reorderPoint": 50
}
```

**Response:**
```json
{
  "id": "inventory-uuid",
  "productName": "Coca-Cola 500ml",
  "currentStock": 240,
  "availableStock": 240,
  "reservedStock": 0,
  "batchNumber": "BATCH-2025-001",
  "expiryDate": "2025-12-31",
  "location": "Main Store",
  "status": "ACTIVE"
}
```

**Result:** Inventory batch created with 240 units available.

---

#### Step 3: View Product with Stock

**Endpoint:** `GET /api/products/{productId}?includeInventory=true`

**Response:**
```json
{
  "id": "product-uuid",
  "name": "Coca-Cola 500ml",
  "sku": "COCA-500ML",
  "price": 500.00,
  "totalStock": 240,
  "availableStock": 240,
  "reservedStock": 0,
  "inventoryCount": 1,
  "hasLowStock": false,
  "hasExpiredBatches": false
}
```

---

### Flow 2: Multi-Batch Scenario

#### Adding Multiple Batches

```bash
# Batch 1 - Expires June 2025
POST /api/shops/{shopId}/inventory
{
  "productId": "product-uuid",
  "currentStock": 100,
  "batchNumber": "BATCH-JUN-2025",
  "expiryDate": "2025-06-30",
  "location": "Main Store"
}

# Batch 2 - Expires December 2025
POST /api/shops/{shopId}/inventory
{
  "productId": "product-uuid",
  "currentStock": 150,
  "batchNumber": "BATCH-DEC-2025",
  "expiryDate": "2025-12-31",
  "location": "Main Store"
}

# Batch 3 - Expires September 2025 (different location)
POST /api/shops/{shopId}/inventory
{
  "productId": "product-uuid",
  "currentStock": 80,
  "batchNumber": "BATCH-SEP-2025",
  "expiryDate": "2025-09-15",
  "location": "Warehouse A"
}
```

**Product Summary:**
- Total Stock: 330 units
- Inventory Batches: 3
- Locations: Main Store (250 units), Warehouse A (80 units)

---

### Flow 3: Sales with FEFO

#### Sale Transaction

**Endpoint:** `POST /api/shops/{shopId}/sales`

**Request:**
```json
{
  "lineItems": [
    {
      "productId": "product-uuid",
      "quantity": 50,
      "unitPrice": 500.00
    }
  ],
  "paymentMethod": "CASH"
}
```

#### What Happens Internally:

1. **Availability Check:**
   - System aggregates stock: 330 units available ✅
   - Required: 50 units ✅

2. **FEFO Allocation:**
   - Sorts batches by expiry date (oldest first)
   - Order: BATCH-JUN-2025 (Jun 30) → BATCH-SEP-2025 (Sep 15) → BATCH-DEC-2025 (Dec 31)
   - Allocates 50 units from BATCH-JUN-2025 (100 → 50 remaining)

3. **Stock Deduction:**
   - Deducts 50 units from BATCH-JUN-2025
   - Creates inventory history record
   - Audit log created

**Result After Sale:**
- Batch 1 (Jun 2025): 50 units remaining
- Batch 2 (Dec 2025): 150 units (unchanged)
- Batch 3 (Sep 2025): 80 units (unchanged)
- Total Available: 280 units

---

## API Reference

### Product Management

#### Create Product
```http
POST /api/shops/{shopId}/products
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Product Name",
  "sku": "PRODUCT-SKU",
  "price": 100.00,
  "costPrice": 70.00
}
```

#### List Products
```http
GET /api/shops/{shopId}/products?search=cola&categoryId=xxx&includeInventory=true&page=0&size=20
```

**Query Parameters:**
- `search` - Search by name, SKU, or barcode
- `categoryId` - Filter by category
- `status` - Filter by status (ACTIVE, INACTIVE, DISCONTINUED)
- `minPrice` / `maxPrice` - Price range filter
- `includeInventory` - Include stock aggregation (default: true)
- `page` / `size` - Pagination
- `sortBy` / `sortDir` - Sorting

#### Get Product Details
```http
GET /api/products/{productId}?includeInventory=true
```

#### Update Product
```http
PUT /api/products/{productId}
{
  "name": "Updated Name",
  "price": 120.00
}
```

#### Delete Product (Soft Delete)
```http
DELETE /api/products/{productId}
```
Sets status to `DISCONTINUED`. Inventory records are NOT deleted.

#### Get Inventory Summary
```http
GET /api/products/{productId}/inventory-summary
```

**Response:**
```json
{
  "productId": "xxx",
  "totalStock": 330,
  "availableStock": 315,
  "reservedStock": 15,
  "inventoryCount": 3,
  "hasLowStock": false,
  "hasExpiredBatches": false
}
```

#### Get Low Stock Products
```http
GET /api/shops/{shopId}/products/low-stock
```

#### Get Out of Stock Products
```http
GET /api/shops/{shopId}/products/out-of-stock
```

---

### Inventory Management

See existing Inventory API documentation. Key endpoints:

```http
POST   /api/shops/{shopId}/inventory              # Add stock
GET    /api/shops/{shopId}/inventory              # List inventory
PUT    /api/inventory/{id}/adjust-stock           # Adjust stock
POST   /api/inventory/{id}/reserve                # Reserve for sale
POST   /api/inventory/{id}/release                # Release reservation
GET    /api/inventory/{id}/history                # View history
```

---

## Business Logic

### FEFO (First Expiry, First Out) Strategy

**Priority Order:**
1. **Expiry Date** - Products expiring soonest sell first
2. **Creation Date** - For same expiry, older batches sell first (FIFO)

**Algorithm:**
```java
List<Inventory> sorted = inventories.stream()
  .filter(inv -> inv.isActive() && !inv.isExpired())
  .sorted(Comparator
    .comparing(inv -> inv.getExpiryDate() != null ? inv.getExpiryDate() : LocalDate.MAX)
    .thenComparing(Inventory::getCreatedAt))
  .collect(Collectors.toList());
```

### Stock Availability Rules

**Available Stock** = Current Stock - Reserved Stock

Product can be sold if:
- ✅ Status is ACTIVE
- ✅ Not expired
- ✅ Available stock ≥ quantity requested
- ✅ Aggregated across all inventory batches

### Low Stock Detection

Product has low stock if **ANY** inventory batch has:
```
(currentStock - reservedStock) <= minimumStock
```

### Expired Batch Handling

- Expired batches are NOT used in sales
- Filter: `expiryDate > CURRENT_DATE`
- Status automatically set to EXPIRED by scheduled job

---

## UX Guidelines

### Product List Page

**Display:**
```
┌─────────────────────────────────────────────────────────────┐
│ Products                                    [+ Add Product]  │
├─────────────────────────────────────────────────────────────┤
│ Search: [________] Category: [All ▾] Status: [Active ▾]     │
├─────────────────────────────────────────────────────────────┤
│ Product Name       SKU          Price    Stock   Status      │
├─────────────────────────────────────────────────────────────┤
│ 🥤 Coca-Cola 500ml  COCA-500ML   ₦500    330    🟢 Active   │
│                                           ↳ 3 batches        │
├─────────────────────────────────────────────────────────────┤
│ 🍞 Bread Loaf      BREAD-001    ₦800     0      🔴 No Stock │
│                                           ↳ Add Stock        │
└─────────────────────────────────────────────────────────────┘
```

**Stock Indicators:**
- 🟢 **In Stock** - Available stock > 0
- 🔴 **No Stock** - Total stock = 0
- ⚠️ **Low Stock** - Any batch below minimum

### Product Detail Page

**Layout:**
```
┌─────────────────────────────────────────────────────────────┐
│ ← Back to Products                                           │
│                                                              │
│ 🥤 Coca-Cola 500ml                           [Edit Product] │
│ SKU: COCA-500ML  |  Barcode: 5449000000996                  │
│                                                              │
│ Price: ₦500  |  Cost: ₦350  |  Margin: 42.86%               │
│ Category: Beverages  |  Status: 🟢 Active                   │
│                                                              │
│ ┌─ Inventory Summary ───────────────────────────────────┐   │
│ │ Total Stock: 330 units                                │   │
│ │ Available: 315 units  |  Reserved: 15 units           │   │
│ │ Batches: 3 locations                 [+ Add Stock]    │   │
│ └───────────────────────────────────────────────────────┘   │
│                                                              │
│ ┌─ Inventory Batches ───────────────────────────────────┐   │
│ │ Location      Batch         Stock  Expires    Status  │   │
│ │ Main Store    BATCH-JUN     50     2025-06-30 ⚠️      │   │
│ │ Main Store    BATCH-DEC     150    2025-12-31 🟢      │   │
│ │ Warehouse     BATCH-SEP     80     2025-09-15 🟢      │   │
│ └───────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### POS Integration

**Product Selection:**
1. Scan barcode or search by name/SKU
2. System displays product with **aggregated** available stock
3. Add to cart with quantity
4. System validates availability across all batches
5. Complete sale → FEFO deduction happens automatically

**Stock Display in POS:**
```
Product: Coca-Cola 500ml
Price: ₦500
Available: 315 units  ← Aggregated from all batches
```

---

## Examples

### Example 1: Basic Product Setup

```bash
# 1. Create product
curl -X POST http://localhost:8081/api/shops/shop-123/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Paracetamol 500mg",
    "sku": "PARA-500MG",
    "barcode": "1234567890123",
    "price": 50.00,
    "costPrice": 30.00,
    "unit": "tablet"
  }'

# 2. Add initial inventory
curl -X POST http://localhost:8081/api/shops/shop-123/inventory \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "para-product-id",
    "currentStock": 1000,
    "unitCost": 30.00,
    "batchNumber": "PARA-BATCH-001",
    "expiryDate": "2026-12-31",
    "location": "Pharmacy Counter",
    "minimumStock": 100
  }'

# 3. View product with stock
curl http://localhost:8081/api/products/para-product-id?includeInventory=true \
  -H "Authorization: Bearer $TOKEN"
```

### Example 2: Multi-Location Inventory

```bash
# Main Store inventory
POST /api/shops/shop-123/inventory
{
  "productId": "product-id",
  "currentStock": 200,
  "location": "Main Store - Front Counter",
  "batchNumber": "MAIN-001"
}

# Warehouse inventory
POST /api/shops/shop-123/inventory
{
  "productId": "product-id",
  "currentStock": 500,
  "location": "Warehouse - Section A",
  "batchNumber": "WH-001"
}

# Result: Product shows 700 total units across 2 locations
```

### Example 3: Handling Expiring Products

```bash
# Get products with expiring inventory
GET /api/shops/shop-123/inventory/expiring?daysThreshold=30

# System returns batches expiring in next 30 days
# Sales automatically use these batches first (FEFO)
```

---

## Best Practices

### For Developers

1. **Always check inventory availability** before creating sales
2. **Use includeInventory=true** when displaying products in POS
3. **Handle multi-batch scenarios** - one product can have multiple inventory records
4. **Respect FEFO** - don't override allocation logic without good reason
5. **Audit everything** - use AuditService for all stock changes

### For Business Users

1. **Create products first**, then add inventory
2. **Use batch numbers** for traceability
3. **Set expiry dates** for perishable items
4. **Monitor low stock alerts** regularly
5. **Track by location** for multi-store operations
6. **Regular stock audits** - adjust inventory when physical count differs

### For System Administrators

1. **Run migration V10** before deploying
2. **Backup database** before migration
3. **Monitor FEFO performance** - ensure proper indexes exist
4. **Schedule expired batch cleanup** jobs
5. **Configure low stock thresholds** per product category

---

## Troubleshooting

### "Insufficient stock" error during sales

**Cause:** Aggregated available stock < requested quantity

**Solution:**
1. Check total available stock: `GET /api/products/{id}/inventory-summary`
2. Check individual batches: `GET /api/shops/{shopId}/inventory?productId={id}`
3. Verify no batches are expired or INACTIVE
4. Check for reserved stock blocking availability

### Product shows zero stock but inventory exists

**Cause:** Inventory status is INACTIVE or EXPIRED

**Solution:**
1. Check inventory status: `GET /api/shops/{shopId}/inventory?productId={id}`
2. Activate inactive batches: `PUT /api/inventory/{id}/status?status=ACTIVE`
3. Remove or adjust expired batches

### FEFO not working as expected

**Cause:** Missing or incorrect expiry dates

**Solution:**
1. Ensure all batches have `expiryDate` set
2. Batches without expiry dates are treated as "never expires" (sold last)
3. Check inventory history: `GET /api/inventory/{id}/history`

---

## Migration Notes

### Upgrading from Single-Tier Model

If upgrading from a system where Product had stock fields:

1. **Migration V10 runs automatically** on deployment
2. **Existing stock → Inventory records** with batch number `MIGRATED-{productId}`
3. **OUT_OF_STOCK status → INACTIVE**
4. **Stock columns removed** from products table
5. **No data loss** - all stock values preserved

### Post-Migration Verification

```sql
-- Check migrated inventory
SELECT p.name, i.current_stock, i.batch_number
FROM products p
JOIN inventory i ON i.product_id = p.id
WHERE i.batch_number LIKE 'MIGRATED-%';

-- Verify no products have stock columns (should error)
SELECT quantity_in_stock FROM products LIMIT 1;
```

---

## Support

For issues or questions:
- **Developer Guide**: [DEVELOPER_GUIDE.md](../DEVELOPER_GUIDE.md)
- **API Docs**: http://localhost:8081/swagger-ui.html
- **GitHub Issues**: https://github.com/your-org/shop-manager/issues

---

**Document Version:** 2.0
**Last Updated:** January 2025
**Implementation:** Two-Tier Product/Inventory Model with FEFO
