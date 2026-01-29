# Inventory Cost & Profit Calculation Redesign

## Overview
This document outlines the redesign of inventory cost tracking and profit calculations to support total purchase cost input and accurate cost breakdown across multiple unit types.

## Current Issues

### 1. Inventory Cost Calculation
**Problem:** Cost is entered per unit, not as total purchase cost
**Impact:** Difficult to track actual purchase expenses
**Example:** Buying 20 packs @ ₦106,000 total requires calculating ₦5,300 per pack manually

### 2. Redundant Current Stock Field
**Problem:** Both `currentStock` and `purchaseQuantity` fields exist
**Impact:** Data duplication and confusion
**Solution:** Remove `currentStock`, compute from `purchaseQuantity`

### 3. Incorrect Profit Calculations
**Problem:** Profit shows ₦10,733.34 instead of expected ₦1,000
**Example:**
- Sold: 2 packs @ ₦5,800 each = ₦11,600 revenue
- Cost: 2 packs @ ₦5,300 each = ₦10,600
- **Expected profit:** ₦1,000
- **Actual shown:** ₦10,733.34 ❌

### 4. POS Not Refreshing Stock
**Problem:** Product stock doesn't update in real-time after sales
**Impact:** Stale data shown on POS screen

### 5. Wrong Inventory Summary Values
**Problem:** Inventory cost, projected sales, and projected profit are incorrect
**Impact:** Unreliable inventory reports

## Requirements

### R1: Total Purchase Cost Input
**User Story:** As a user, I want to enter the total cost I paid for all inventory, not calculate cost per unit manually.

**Input:**
- Purchase Unit: "pack"
- Purchase Quantity: 20
- **Total Purchase Cost:** ₦106,000 (NEW - was cost per unit before)

**System calculates:**
- Cost per pack: ₦106,000 ÷ 20 = ₦5,300.00

### R2: Cost Breakdown by Unit Ratios
**User Story:** As a user, I want the system to automatically calculate cost for each unit type based on the product's unit definitions.

**Given Product Units:**
- Piece (base unit): conversion = 1.0
- Pack: conversion = 12.0 (1 pack = 12 pieces)
- Half Pack: conversion = 6.0 (1 half pack = 6 pieces)
- Quarter Pack: conversion = 3.0 (1 quarter pack = 3 pieces)

**System calculates cost for each unit:**
```
Purchase unit cost = ₦106,000 / 20 = ₦5,300.00

For each unit type:
  unitCost = purchaseUnitCost × (unitConversion / purchaseUnitConversion)

Results:
- Pack: ₦5,300 × (12/12) = ₦5,300.00
- Half Pack: ₦5,300 × (6/12) = ₦2,650.00
- Quarter Pack: ₦5,300 × (3/12) = ₦1,325.00
- Piece: ₦5,300 × (1/12) = ₦441.67 (rounded HALF_UP to 2 decimals)
```

### R3: Auto-Create Missing Purchase Units
**User Story:** As a user, if I purchase in a unit that doesn't exist in the product's unit definitions, the system should create it automatically.

**Scenario:** User purchases in "carton" but product only has piece/pack/half_pack

**System behavior:**
1. Check if "carton" exists in ProductUnitDefinitions
2. If NOT exists:
   - Prompt user for conversion factor (e.g., 1 carton = 24 pieces)
   - Create ProductUnitDefinition automatically
   - Continue with inventory creation

### R4: Dual Stock Tracking
**User Story:** As a user, I want to see stock in both purchase units and base units for clarity.

**Display:**
- **Stock in Purchase Units:** 18 packs
- **Stock in Base Units:** 216 pieces
- **Both values** shown for user convenience

### R5: Accurate Profit Calculation
**User Story:** As a user, I want to see correct profit based on actual batch costs using FEFO.

**Scenario:** Product has 2 inventory batches
- Batch A (expires 2025-06-30): 20 packs @ ₦5,300/pack
- Batch B (expires 2025-12-31): 15 packs @ ₦5,500/pack

**Sale:** 2 packs @ ₦5,800 each
- Uses FEFO: 2 packs from Batch A (expires first)
- **Revenue:** 2 × ₦5,800 = ₦11,600
- **Cost:** 2 × ₦5,300 = ₦10,600 (from Batch A)
- **Profit:** ₦11,600 - ₦10,600 = **₦1,000** ✅

### R6: Real-time POS Stock Refresh
**User Story:** As a cashier, I want to see updated stock immediately after completing a sale.

**Behavior:**
- After sale completion
- Inventory stock updates
- POS product list refreshes automatically
- New stock values displayed

## Phase 1: Completed ✅

### Files Changed:
1. **Inventory.java**
   - Removed `currentStock` field
   - Renamed `purchaseUnitCost` → `totalPurchaseCost`
   - Updated stock methods to use `purchaseQuantity`
   - Added `getCurrentStock()` computed method

2. **V60__update_inventory_cost_structure.sql**
   - Add `total_purchase_cost` column
   - Migrate existing data
   - Drop `purchase_unit_cost` column
   - Drop `current_stock` column

3. **InventoryCreateRequest.java**
   - Removed `currentStock` field
   - Renamed `purchaseUnitCost` → `totalPurchaseCost`

4. **InventoryUpdateRequest.java**
   - Renamed `purchaseUnitCost` → `totalPurchaseCost`

5. **InventoryResponse.java**
   - Added `currentStockInPurchaseUnit` field
   - Added `totalPurchaseCost` field
   - Kept `purchaseUnitCost` as calculated value

6. **InventoryCostCalculator.java** (NEW)
   - `calculateCostsForAllUnits()` - Main cost breakdown algorithm
   - `calculateCostForUnit()` - Single unit cost calculation
   - `convertQuantity()` - Unit conversion logic
   - `convertToBaseUnits()` - Convert to base units (pieces)

## Phase 2: Implementation Progress

### 2.1 InventoryService.java ✅ COMPLETED

#### Methods to Update:

**createInventory() - Line 96**
```java
// BEFORE
.currentStock(request.getCurrentStock())
.purchaseUnitCost(request.getPurchaseUnitCost())

// AFTER
// currentStock removed - computed from purchaseQuantity
.totalPurchaseCost(request.getTotalPurchaseCost())

// Auto-create purchase unit if missing
if (request.getPurchaseUnit() != null) {
    ensurePurchaseUnitExists(product, request.getPurchaseUnit());
}

// Calculate and set unit costs
if (request.getTotalPurchaseCost() != null && request.getPurchaseQuantity() != null) {
    List<ProductUnitDefinition> unitDefs = productUnitDefRepository
        .findByProductId(product.getId());

    Map<String, BigDecimal> unitCosts = costCalculator.calculateCostsForAllUnits(
        request.getTotalPurchaseCost(),
        request.getPurchaseQuantity(),
        request.getPurchaseUnit(),
        unitDefs
    );

    // Update or create InventoryUnitPrice records
    updateUnitPrices(inventory, unitCosts);
}
```

**adjustStock() - Line 159**
```java
// BEFORE
int previousStock = inventory.getCurrentStock();

// AFTER
int previousStock = inventory.getCurrentStock(); // Uses computed method
```

**mapToResponse() - Needs update**
```java
// Add calculations
Integer currentStockInPurchaseUnit = null;
BigDecimal purchaseUnitCost = null;

if (inventory.getTotalPurchaseCost() != null &&
    inventory.getPurchaseQuantity() != null &&
    inventory.getPurchaseQuantity().compareTo(BigDecimal.ZERO) > 0) {

    purchaseUnitCost = inventory.getTotalPurchaseCost()
        .divide(inventory.getPurchaseQuantity(), 2, RoundingMode.HALF_UP);

    currentStockInPurchaseUnit = inventory.getPurchaseQuantity().intValue();
}

return InventoryResponse.builder()
    // ... existing fields ...
    .currentStock(inventory.getCurrentStock()) // Computed
    .currentStockInPurchaseUnit(currentStockInPurchaseUnit)
    .totalPurchaseCost(inventory.getTotalPurchaseCost())
    .purchaseUnitCost(purchaseUnitCost)
    // ... rest ...
    .build();
```

#### New Methods to Add:

**ensurePurchaseUnitExists()**
```java
private void ensurePurchaseUnitExists(Product product, String purchaseUnit) {
    List<ProductUnitDefinition> existing = productUnitDefRepository
        .findByProductId(product.getId());

    boolean exists = existing.stream()
        .anyMatch(ud -> ud.getUnitType().equalsIgnoreCase(purchaseUnit));

    if (!exists) {
        // Purchase unit doesn't exist - need to create it
        // This requires user input for conversion factor
        // For now, throw exception requiring manual creation
        throw new IllegalArgumentException(
            "Purchase unit '" + purchaseUnit + "' not found. " +
            "Please create unit definition first with conversion factor."
        );

        // TODO: In future, prompt user via UI for conversion factor
    }
}
```

**updateUnitPrices()**
```java
private void updateUnitPrices(Inventory inventory, Map<String, BigDecimal> unitCosts) {
    // Clear existing unit prices
    inventory.getUnitPrices().clear();

    // Create new unit prices from calculated costs
    for (Map.Entry<String, BigDecimal> entry : unitCosts.entrySet()) {
        InventoryUnitPrice unitPrice = InventoryUnitPrice.builder()
            .inventory(inventory)
            .unitType(entry.getKey())
            .sellingPrice(calculateSellingPrice(entry.getValue())) // Apply margin
            .costPrice(entry.getValue())
            .build();

        inventory.getUnitPrices().add(unitPrice);
    }
}
```

### 2.2 Inventory Summary Calculations ✅ COMPLETED

**Previous Calculations (WRONG):**
```java
// Wrong - uses old cost structure
itemTotalCost = currentStock × costPrice
```

**Fixed Calculations:**
```java
// Correct - use actual total purchase cost
itemTotalCost = inventory.getTotalPurchaseCost()

// Projected sales based on current stock in base units
Integer currentStockBase = inventory.getCurrentStock();
BigDecimal avgSellingPrice = calculateAverageSellingPrice(inventory);
itemProjectedSales = currentStockBase × avgSellingPrice

// Projected profit
itemProjectedProfit = itemProjectedSales - itemTotalCost
```

### 2.3 Sales Profit Calculation with FEFO ✅ COMPLETED

**File:** `SalesLineItemBuilder.java`

**Previous Logic (WRONG):**
```java
// Simplified - actual implementation varies
profit = (sellingPrice - costPrice) × quantity
```

**Fixed Logic (CORRECT):**
```java
// Use FEFO to get actual batch costs
List<InventoryAllocation> allocations = inventoryService
    .allocateStockFEFO(productId, quantity, unitType);

BigDecimal totalCost = BigDecimal.ZERO;
for (InventoryAllocation allocation : allocations) {
    // Get unit cost from the specific batch
    BigDecimal unitCost = allocation.getInventory()
        .getUnitPriceForType(unitType)
        .getCostPrice();

    totalCost = totalCost.add(
        unitCost.multiply(BigDecimal.valueOf(allocation.getQuantity()))
    );
}

BigDecimal revenue = sellingPrice.multiply(BigDecimal.valueOf(quantity));
BigDecimal profit = revenue.subtract(totalCost);
```

### 2.4 POS Stock Refresh

**Frontend Changes Needed:**
After successful sale POST request:
```typescript
// After sale completion
await salesApi.createSale(saleData);

// Refresh inventory immediately
await inventoryApi.getInventory(shopId, {
  refresh: true
});

// Update UI state
dispatch(updateInventoryList(freshInventory));
```

**Backend:** Ensure inventory updates are committed before response

## Implementation Strategy

### Step 1: Complete InventoryService Updates
- Update all methods using old field names
- Add new helper methods
- Test create/update/adjust operations

### Step 2: Fix Repository & Specifications
- Update queries removing currentStock references
- Add computed fields where needed

### Step 3: Fix Sales/Profit Calculations
- Implement FEFO cost allocation
- Update profit calculation logic
- Test with multiple batches

### Step 4: Update Summary Calculations
- Fix InventorySummaryDto
- Update repository aggregations
- Test summary endpoints

### Step 5: Frontend Changes
- Update inventory forms (total cost input)
- Add POS stock refresh
- Update display fields

### Step 6: Testing
- Unit tests for InventoryCostCalculator
- Integration tests for inventory CRUD
- E2E tests for sales with profit calculation
- Test POS stock refresh

## Testing Scenarios

### Scenario 1: Create Inventory with Total Cost
```
Input:
- Product: Coca-Cola 500ml
- Purchase Unit: pack
- Purchase Quantity: 20
- Total Purchase Cost: ₦106,000

Expected:
- Inventory created
- Unit costs calculated:
  - Pack: ₦5,300.00
  - Half Pack: ₦2,650.00
  - Piece: ₦441.67
- InventoryUnitPrice records created for each unit
```

### Scenario 2: Multi-Batch Profit Calculation
```
Setup:
- Batch A: 20 packs @ ₦5,300/pack (expires 2025-06-30)
- Batch B: 15 packs @ ₦5,500/pack (expires 2025-12-31)

Sale:
- 2 packs @ ₦5,800 each

Expected:
- Allocates from Batch A (FEFO)
- Revenue: ₦11,600
- Cost: ₦10,600
- Profit: ₦1,000 ✅
```

### Scenario 3: POS Stock Refresh
```
Before Sale:
- Product stock: 20 packs

Sale:
- Sell 2 packs

After Sale:
- POS refreshes immediately
- Product stock shows: 18 packs
- No page reload needed
```

## Migration Considerations

### Data Migration
- V60 migration handles existing data
- Calculates totalPurchaseCost from old purchaseUnitCost × purchaseQuantity
- Fallback to costPrice if purchase data missing

### Backward Compatibility
- Frontend must handle both old and new fields during rollout
- API responses include both computed currentStock and purchaseQuantity
- Gradual migration possible

### Rollback Plan
If issues found:
1. Keep V60 migration (don't rollback DB)
2. Revert code changes
3. Manually fix data if needed
4. Create V61 migration to restore old structure if critical

## Success Criteria

✅ Users can input total purchase cost
✅ System calculates unit costs automatically
✅ Profit calculations are accurate
✅ POS stock refreshes in real-time
✅ Inventory summaries show correct values
✅ No rounding errors in money calculations
✅ FEFO allocation works with batch-specific costs
✅ Stock displays in both purchase units and base units

## Timeline Estimate

- Phase 2.1 (InventoryService): 2-3 hours
- Phase 2.2 (Summary DTO): 1 hour
- Phase 2.3 (Sales/Profit): 2-3 hours
- Phase 2.4 (POS Refresh): 1-2 hours
- Testing & Debug: 2-3 hours

**Total: 8-12 hours**

## Design Decisions ✅

### Q1: Missing Purchase Unit Handling
**Decision:** Prompt user for conversion factor and auto-create ProductUnitDefinition
- When user enters purchase unit not in ProductUnitDefinitions
- System prompts: "Enter how many [base_unit] in 1 [purchase_unit]"
- Creates ProductUnitDefinition automatically
- Continues with inventory creation

### Q2: Selling Price Auto-calculation
**Decision:** NO auto-calculation - User must set selling price for each unit during inventory
- Do NOT apply automatic margins
- User explicitly sets selling price for each ProductUnit during inventory creation
- System only calculates COST breakdown, not selling prices

### Q3: Partial FEFO Allocations
**Decision:** Chain multiple batches using FEFO priority
- Example: Need 25 packs, Batch A has 20, Batch B has 15
  1. Allocate 20 packs from Batch A (expires first)
  2. Allocate 5 packs from Batch B (expires next)
  3. Create sale line items with separate costs:
     - Line 1: 20 packs @ Batch A cost
     - Line 2: 5 packs @ Batch B cost
- Each allocation tracks actual batch cost for accurate profit calculation

### Q4: Stock Adjustments and Total Cost
**Decision:** YES - Adjustments require re-entering total cost
- When adjusting stock quantity, user must provide new total purchase cost
- User can re-enter existing total cost if unchanged
- System recalculates unit costs based on new quantity + new total cost
- Ensures cost accuracy after manual adjustments

### Q5: Average Selling Price in Summaries
**Decision:** Remove average selling price calculation
- No benefit to averaging across multiple units
- If really necessary, use base unit price only
- Simplifies summary calculations
- Removes ambiguity in multi-unit pricing

## Next Steps

1. Review this design document
2. Answer outstanding questions
3. Proceed with Phase 2 implementation
4. Create separate PRs for each phase
5. Test thoroughly before merging to main
