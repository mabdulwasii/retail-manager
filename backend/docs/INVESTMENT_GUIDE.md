# Investment Guide

This guide explains the investment system in Shop Manager, including investment rounds, profit sharing models, and API usage.

## Table of Contents
- [Overview](#overview)
- [Investment Rounds](#investment-rounds)
- [Profit Sharing Models](#profit-sharing-models)
- [API Usage](#api-usage)
- [Best Practices](#best-practices)
- [Examples](#examples)

---

## Overview

The Shop Manager investment system allows shop owners to:
- Raise capital from multiple investors
- Track investments at shop or product level
- Automatically calculate profit distributions
- Support multiple profit sharing models
- Manage investment lifecycles

### Key Concepts

**Investment Round**: A group of investors who invest together with shared configuration (profit model, investment type, maturity date).

**Profit Sharing Model**: The formula used to calculate each investor's share of profits (proportional, fixed shares, time-weighted, or tiered).

**Investment Type**: The scope of the investment (shop-wide or product-specific).

---

## Investment Rounds

### Why Investment Rounds?

Investment rounds provide:
- **Consistency**: All investors in a round follow the same profit rules
- **Simplicity**: Create multiple investments at once
- **Accuracy**: Prevents over-allocation of profits
- **Transparency**: Clear tracking of investor groups

### Round Lifecycle

```
OPEN → CLOSED → COMPLETED
  ↓
CANCELLED
```

- **OPEN**: Accepting new investors
- **CLOSED**: No new investors, profit distributions ongoing
- **COMPLETED**: All distributions paid, round finished
- **CANCELLED**: Round cancelled before completion

### Round Number Format

```
ROUND-{SHOP_CODE}-{YEAR}-Q{QUARTER}-{SEQUENCE}
```

Example: `ROUND-ABC-2025-Q4-001`

---

## Profit Sharing Models

### 1. PROPORTIONAL_BY_AMOUNT

**How it works**: Each investor's share is calculated based on their investment amount as a percentage of total round investment.

**Formula**:
```
Investor Share % = (Investor Amount / Total Round Amount) × 100
```

**Example**:
```
Round Total: $100,000
- Alice invests $50,000 → 50% of profits
- Bob invests $30,000 → 30% of profits
- Charlie invests $20,000 → 20% of profits
```

**Use Case**: Fair distribution based on capital contribution. Most common model.

**Request Example**:
```json
{
  "shopId": "shop-123",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "PROPORTIONAL_BY_AMOUNT",
  "maturityDate": "2026-12-31",
  "investors": [
    {"investorId": "user-1", "amount": 50000},
    {"investorId": "user-2", "amount": 30000},
    {"investorId": "user-3", "amount": 20000}
  ]
}
```

---

### 2. FIXED_SHARES

**How it works**: Each investor is allocated a fixed number of shares, and profits are divided based on share ownership.

**Formula**:
```
Investor Share % = (Investor Shares / Total Shares) × 100
```

**Example**:
```
Total Shares: 1000
- Alice has 500 shares → 50% of profits
- Bob has 300 shares → 30% of profits
- Charlie has 200 shares → 20% of profits
```

**Use Case**: When you want to decouple profit share from investment amount (e.g., sweat equity, strategic investors).

**Request Example**:
```json
{
  "shopId": "shop-123",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "FIXED_SHARES",
  "maturityDate": "2026-12-31",
  "investors": [
    {"investorId": "user-1", "amount": 50000, "fixedShares": 500},
    {"investorId": "user-2", "amount": 30000, "fixedShares": 300},
    {"investorId": "user-3", "amount": 20000, "fixedShares": 200}
  ]
}
```

---

### 3. TIME_WEIGHTED

**How it works**: Base profit share is calculated proportionally by amount, then multiplied by a time-based bonus to reward long-term investors.

**Formula**:
```
Base Share % = (Investor Amount / Total Round Amount) × 100
Time Multiplier = Configured multiplier based on years invested
Final Share % = Base Share % × Time Multiplier
```

**Example**:
```
Time Weighting Rules:
- 0-1 year: 1.0x multiplier (base)
- 1-2 years: 1.2x multiplier (+20% bonus)
- 2-3 years: 1.5x multiplier (+50% bonus)
- 3+ years: 2.0x multiplier (max cap)

Alice invested $50,000 two years ago:
- Base Share: 50%
- Time Multiplier: 1.5x (2 years)
- Final Share: 50% × 1.5 = 75% of profits

Bob invested $50,000 six months ago:
- Base Share: 50%
- Time Multiplier: 1.0x (0.5 years)
- Final Share: 50% × 1.0 = 50% of profits
```

**Use Case**: Incentivize long-term capital commitment.

**Request Example**:
```json
{
  "shopId": "shop-123",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "TIME_WEIGHTED",
  "maturityDate": "2026-12-31",
  "timeWeightingRules": {
    "baseYears": 1.0,
    "baseMultiplier": 1.0,
    "year2Threshold": 2.0,
    "year2Multiplier": 1.2,
    "year3Threshold": 3.0,
    "year3Multiplier": 1.5,
    "maxMultiplier": 2.0
  },
  "investors": [
    {"investorId": "user-1", "amount": 50000},
    {"investorId": "user-2", "amount": 50000}
  ]
}
```

---

### 4. TIERED

**How it works**: Base profit share is calculated proportionally, then multiplied by a tier bonus based on investment amount.

**Formula**:
```
Base Share % = (Investor Amount / Total Round Amount) × 100
Tier Multiplier = Configured multiplier based on investment amount
Final Share % = Base Share % × Tier Multiplier
```

**Example**:
```
Tier Configuration:
- Tier 1: $0-$49,999 → 1.0x multiplier (base)
- Tier 2: $50,000-$99,999 → 1.1x multiplier (+10% bonus)
- Tier 3: $100,000+ → 1.2x multiplier (+20% bonus)

Round Total: $200,000
- Alice invests $100,000 (Tier 3):
  Base: 50%, Tier Multiplier: 1.2x
  Final: 50% × 1.2 = 60% of profits

- Bob invests $60,000 (Tier 2):
  Base: 30%, Tier Multiplier: 1.1x
  Final: 30% × 1.1 = 33% of profits

- Charlie invests $40,000 (Tier 1):
  Base: 20%, Tier Multiplier: 1.0x
  Final: 20% × 1.0 = 20% of profits
```

**Use Case**: Reward larger investments to attract anchor investors.

**Request Example**:
```json
{
  "shopId": "shop-123",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "TIERED",
  "maturityDate": "2026-12-31",
  "tierConfiguration": {
    "tier1Threshold": 0,
    "tier1Multiplier": 1.0,
    "tier2Threshold": 50000,
    "tier2Multiplier": 1.1,
    "tier3Threshold": 100000,
    "tier3Multiplier": 1.2
  },
  "investors": [
    {"investorId": "user-1", "amount": 100000},
    {"investorId": "user-2", "amount": 60000},
    {"investorId": "user-3", "amount": 40000}
  ]
}
```

---

## API Usage

### Permissions

Only **SYSTEM_ADMIN**, **TENANT_ADMIN**, and **OWNER** roles can:
- Create investment rounds
- Update investment configuration
- Close rounds
- Delete investments

**INVESTOR** and **MANAGER** roles can:
- View investments (READ, LIST)
- See their own investment performance

See [PERMISSION_MATRIX.md](PERMISSION_MATRIX.md) for complete details.

---

### Create Investment Round

**Endpoint**: `POST /api/shops/{shopId}/investment-rounds`

**Request Body**:
```json
{
  "shopId": "shop-123",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "PROPORTIONAL_BY_AMOUNT",
  "maturityDate": "2026-12-31",
  "notes": "Q4 2025 Investment Round",
  "investors": [
    {
      "investorId": "user-1",
      "amount": 100000.00,
      "notes": "Lead investor"
    },
    {
      "investorId": "user-2",
      "amount": 50000.00
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "round-123",
  "roundNumber": "ROUND-ABC-2025-Q4-001",
  "shopId": "shop-123",
  "shopName": "ABC Shop",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "PROPORTIONAL_BY_AMOUNT",
  "maturityDate": "2026-12-31T00:00:00",
  "status": "OPEN",
  "totalAmount": 150000.00,
  "totalInvestors": 2,
  "investments": [
    {
      "id": "inv-1",
      "investmentNumber": "INV-ROUND-ABC-2025-Q4-001-001",
      "investorId": "user-1",
      "investorName": "Alice Johnson",
      "amount": 100000.00,
      "totalProfitEarned": 0,
      "availableBalance": 0
    },
    {
      "id": "inv-2",
      "investmentNumber": "INV-ROUND-ABC-2025-Q4-001-002",
      "investorId": "user-2",
      "investorName": "Bob Smith",
      "amount": 50000.00,
      "totalProfitEarned": 0,
      "availableBalance": 0
    }
  ]
}
```

---

### List Investment Rounds

**Endpoint**: `GET /api/shops/{shopId}/investment-rounds`

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `sortBy` (default: createdAt)
- `sortDir` (default: desc)

**Response**: `200 OK`
```json
{
  "content": [...],
  "totalElements": 5,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

### Get Investment Round

**Endpoint**: `GET /api/investment-rounds/{roundId}`

**Response**: `200 OK` (same structure as create response)

---

### Close Investment Round

**Endpoint**: `POST /api/investment-rounds/{roundId}/close`

**Response**: `200 OK`

Closes the round to new investors. Status changes from `OPEN` to `CLOSED`.

---

### Add Investor to Round

**Endpoint**: `POST /api/investment-rounds/{roundId}/investors`

**Request Body**:
```json
{
  "investorId": "user-3",
  "amount": 25000.00,
  "notes": "Additional investor"
}
```

**Response**: `200 OK`

Only works if round status is `OPEN`.

---

### Delete Investment Round

**Endpoint**: `DELETE /api/investment-rounds/{roundId}`

**Response**: `204 No Content`

Only allowed if no profit distributions have been made.

---

## Best Practices

### 1. Choose the Right Profit Model

- **PROPORTIONAL_BY_AMOUNT**: Default choice for most scenarios
- **FIXED_SHARES**: When you want flexibility (sweat equity, strategic partners)
- **TIME_WEIGHTED**: To reward loyalty and discourage early withdrawals
- **TIERED**: To attract large anchor investors

### 2. Plan Your Tiers Carefully (TIERED model)

✅ Good tier structure:
```
Tier 1: $0-$49,999     → 1.0x
Tier 2: $50,000-$99,999  → 1.1x
Tier 3: $100,000+      → 1.2x
```

❌ Avoid extreme differences:
```
Tier 1: $0-$49,999     → 1.0x
Tier 2: $50,000-$99,999  → 2.0x  // Too big a jump
Tier 3: $100,000+      → 5.0x  // Way too high
```

### 3. Set Realistic Time Weights

✅ Good time weighting:
```
1 year:  1.0x (base)
2 years: 1.2x (+20%)
3 years: 1.5x (+50%)
Max:     2.0x (cap at double)
```

❌ Avoid unsustainable bonuses:
```
1 year:  1.0x
2 years: 5.0x  // Too aggressive
3 years: 10.0x // Unsustainable
```

### 4. Close Rounds Promptly

Close rounds once you've reached your fundraising target to:
- Lock in profit calculations
- Prevent late investors from diluting shares
- Signal commitment to existing investors

### 5. Document Everything

Always use the `notes` field to document:
- Purpose of the round
- Special agreements
- Strategic investors
- Payment schedules

---

## Examples

### Example 1: Simple Shop Investment

**Scenario**: You need $100,000 to expand your shop. Three investors contribute.

**Request**:
```json
{
  "shopId": "shop-abc",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "PROPORTIONAL_BY_AMOUNT",
  "maturityDate": "2027-12-31",
  "notes": "Shop expansion - new inventory and equipment",
  "investors": [
    {"investorId": "alice", "amount": 50000},
    {"investorId": "bob", "amount": 30000},
    {"investorId": "charlie", "amount": 20000}
  ]
}
```

**Result**:
- Alice: 50% of shop profits
- Bob: 30% of shop profits
- Charlie: 20% of shop profits

---

### Example 2: Strategic Investor with Higher Share

**Scenario**: You want to give a strategic advisor 30% share despite lower investment.

**Request**:
```json
{
  "shopId": "shop-abc",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "FIXED_SHARES",
  "maturityDate": "2027-12-31",
  "notes": "Strategic partnership with industry expert",
  "investors": [
    {"investorId": "owner", "amount": 70000, "fixedShares": 700},
    {"investorId": "advisor", "amount": 30000, "fixedShares": 300}
  ]
}
```

**Result**:
- Owner: 70% of profits ($70,000 investment)
- Advisor: 30% of profits ($30,000 investment + expertise)

---

### Example 3: Loyalty Bonus for Long-Term Investors

**Scenario**: Reward investors who commit for 3+ years.

**Request**:
```json
{
  "shopId": "shop-abc",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "TIME_WEIGHTED",
  "maturityDate": "2028-12-31",
  "timeWeightingRules": {
    "baseYears": 1.0,
    "baseMultiplier": 1.0,
    "year2Threshold": 2.0,
    "year2Multiplier": 1.25,
    "year3Threshold": 3.0,
    "year3Multiplier": 1.5,
    "maxMultiplier": 2.0
  },
  "investors": [
    {"investorId": "alice", "amount": 100000}
  ]
}
```

**Result Timeline**:
- Year 1: Alice gets 1.0x profits (base)
- Year 2: Alice gets 1.25x profits (+25% bonus)
- Year 3+: Alice gets 1.5x profits (+50% bonus)

---

### Example 4: Attract Large Investors with Tiers

**Scenario**: Encourage larger investments with tier bonuses.

**Request**:
```json
{
  "shopId": "shop-abc",
  "investmentType": "SHOP_WIDE",
  "profitSharingModel": "TIERED",
  "maturityDate": "2027-12-31",
  "tierConfiguration": {
    "tier1Threshold": 0,
    "tier1Multiplier": 1.0,
    "tier2Threshold": 50000,
    "tier2Multiplier": 1.15,
    "tier3Threshold": 100000,
    "tier3Multiplier": 1.3
  },
  "investors": [
    {"investorId": "whale", "amount": 150000},
    {"investorId": "medium", "amount": 75000},
    {"investorId": "small", "amount": 25000}
  ]
}
```

**Result**:
- Whale: 60% base × 1.3 = 78% of profits (Tier 3 bonus)
- Medium: 30% base × 1.15 = 34.5% of profits (Tier 2 bonus)
- Small: 10% base × 1.0 = 10% of profits (no bonus)

Note: Total > 100% because multipliers are applied to base proportions.

---

## Migration from Old System

If you have existing investments created before the investment round system:

1. **Automatic Migration**: The V22 migration script automatically creates a "MIGRATED" round for each shop with existing investments.

2. **Round Status**: Migrated rounds are automatically set to `CLOSED` status.

3. **Profit Model**: Migrated investments default to `PROPORTIONAL_BY_AMOUNT`.

4. **Backward Compatibility**: Old investment records continue to work with the new profit calculation system.

---

## Troubleshooting

### Error: "Validation failed: All investors must provide fixed shares"

**Cause**: Using `FIXED_SHARES` model without specifying `fixedShares` for each investor.

**Solution**: Add `fixedShares` to each investor:
```json
{"investorId": "user-1", "amount": 50000, "fixedShares": 500}
```

---

### Error: "Tier configuration is required for TIERED model"

**Cause**: Using `TIERED` model without `tierConfiguration`.

**Solution**: Add tier configuration:
```json
{
  "profitSharingModel": "TIERED",
  "tierConfiguration": {
    "tier1Threshold": 0,
    "tier1Multiplier": 1.0,
    ...
  }
}
```

---

### Error: "Cannot add investors to CLOSED round"

**Cause**: Trying to add investor to a closed round.

**Solution**: Create a new round for additional investors.

---

### Error: "Cannot delete round with profit distributions"

**Cause**: Trying to delete a round that has already distributed profits.

**Solution**: Rounds with distributions cannot be deleted. You can only mark them as `COMPLETED`.

---

## See Also

- [PERMISSION_MATRIX.md](PERMISSION_MATRIX.md) - Complete permission reference
- [API Documentation](../src/docs/asciidoc/index.adoc) - Full API reference
- [DEVELOPER_GUIDE.md](../DEVELOPER_GUIDE.md) - Development setup

---

**For questions or issues, please refer to the [GitHub Issues](https://github.com/your-repo/shop-manager/issues).**
