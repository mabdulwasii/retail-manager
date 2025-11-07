# Investment Redesign - Remaining Work

## ✅ Completed (Session 1)

### Phase 1: Core Infrastructure
- [x] JwtPrincipal with userId field
- [x] JwtAuthConverter with user lookup
- [x] AuthenticationSuccessListener enrichment
- [x] UserSyncService refactoring (reduced cognitive complexity)

### Phase 2: InvestmentRound Entity & Logic
- [x] TierConfiguration embeddable
- [x] TimeWeightingRules embeddable
- [x] InvestmentRound entity
- [x] InvestmentRound repository
- [x] Investment entity updated (added investmentRound FK, removed duplicate fields)

### Phase 3: DTOs & Validation
- [x] InvestmentRoundCreateRequest DTO (with nested DTOs for InvestorInput, TierConfiguration, TimeWeightingRules)
- [x] InvestmentRoundResponse DTO (with InvestmentSummary)
- [x] InvestmentRoundValidator (comprehensive validation logic)

### Permissions
- [x] Updated permission-matrix.csv (removed MANAGER and INVESTOR from INVESTMENT_CREATE and INVESTMENT_UPDATE)

## ❌ TODO (Session 2)

### CRITICAL: Add TENANT_ADMIN to Permission Matrix
**File**: `src/main/resources/permission-matrix.csv`

Current header:
```
Resource,Permission,Description,SYSTEM_ADMIN,OWNER,MANAGER,EMPLOYEE,INVESTOR,Permission_Constant
```

**MUST UPDATE TO**:
```
Resource,Permission,Description,SYSTEM_ADMIN,TENANT_ADMIN,OWNER,MANAGER,EMPLOYEE,INVESTOR,Permission_Constant
```

Then update ALL rows to include TENANT_ADMIN column (between SYSTEM_ADMIN and OWNER).

For investment permissions, TENANT_ADMIN should have SAME permissions as OWNER:
- INVESTMENT_CREATE: ✓
- INVESTMENT_UPDATE: ✓
- INVESTMENT_DELETE: ✓
- INVESTMENT_READ: ✓
- INVESTMENT_LIST: ✓
- INVESTMENT_CLOSE: ✓
- INVESTMENT_PROFIT_DISTRIBUTE: ✓

### Phase 4: Profit Calculation Redesign
**File**: `InvestmentProfitService.java`

Update `calculateInvestorSharePercentage()` method:

```java
private BigDecimal calculateInvestorSharePercentage(Investment investment) {
    InvestmentRound round = investment.getInvestmentRound();

    return switch (round.getProfitSharingModel()) {
        case PROPORTIONAL_BY_AMOUNT -> {
            // Query total amount in round
            BigDecimal totalRoundAmount = investmentRepository
                .sumAmountByInvestmentRoundId(round.getId());
            // Calculate proportion
            yield investment.getAmount()
                .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        case FIXED_SHARES -> {
            // Query total shares in round
            Integer totalShares = investmentRepository
                .sumFixedSharesByInvestmentRoundId(round.getId());
            // Calculate proportion
            yield BigDecimal.valueOf(investment.getFixedShares())
                .divide(BigDecimal.valueOf(totalShares), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        case TIME_WEIGHTED -> {
            // Calculate base proportion
            BigDecimal totalRoundAmount = investmentRepository
                .sumAmountByInvestmentRoundId(round.getId());
            BigDecimal baseProportion = investment.getAmount()
                .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            // Apply time multiplier
            BigDecimal yearsInvested = calculateYearsInvested(investment);
            BigDecimal timeMultiplier = round.getTimeWeightingRules()
                .getMultiplierForYears(yearsInvested);

            yield baseProportion.multiply(timeMultiplier);
        }

        case TIERED -> {
            // Calculate base proportion
            BigDecimal totalRoundAmount = investmentRepository
                .sumAmountByInvestmentRoundId(round.getId());
            BigDecimal baseProportion = investment.getAmount()
                .divide(totalRoundAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            // Apply tier multiplier
            BigDecimal tierMultiplier = round.getTierConfiguration()
                .getMultiplierForAmount(investment.getAmount());

            yield baseProportion.multiply(tierMultiplier);
        }
    };
}

private BigDecimal calculateYearsInvested(Investment investment) {
    long daysBetween = ChronoUnit.DAYS.between(
        investment.getInvestmentDate(),
        LocalDateTime.now()
    );
    return BigDecimal.valueOf(daysBetween / 365.0);
}
```

**Add to InvestmentRepository**:
```java
@Query("SELECT SUM(i.amount) FROM Investment i WHERE i.investmentRound.id = :roundId")
BigDecimal sumAmountByInvestmentRoundId(@Param("roundId") String roundId);

@Query("SELECT SUM(i.fixedShares) FROM Investment i WHERE i.investmentRound.id = :roundId")
Integer sumFixedSharesByInvestmentRoundId(@Param("roundId") String roundId);
```

### Phase 5: InvestmentRoundService
**File**: Create `InvestmentRoundService.java`

Key methods:
- `createInvestmentRound(request)` - Create round with investors
- `getInvestmentRound(roundId)` - Get round details
- `listInvestmentRounds(shopId, pageable)` - List rounds for shop
- `updateInvestmentRound(roundId, request)` - Update round config
- `deleteInvestmentRound(roundId)` - Delete round
- `closeRound(roundId)` - Close round to new investors
- `addInvestorToRound(roundId, investorInput)` - Add investor to existing round

**Important**: Generate round numbers like: `ROUND-{SHOP_CODE}-{YEAR}-Q{QUARTER}-{SEQUENCE}`

### Phase 6: InvestmentRoundController
**File**: Create `InvestmentRoundController.java`

Endpoints:
```
POST   /api/shops/{shopId}/investment-rounds
GET    /api/shops/{shopId}/investment-rounds
GET    /api/investment-rounds/{roundId}
PUT    /api/investment-rounds/{roundId}
DELETE /api/investment-rounds/{roundId}
POST   /api/investment-rounds/{roundId}/close
POST   /api/investment-rounds/{roundId}/investors
```

All endpoints should use:
```java
@PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CREATE)")
```

### Phase 7: Migration V22
**File**: `V22__investment_rounds_and_permissions.sql`

```sql
-- Part 1: Remove INVESTMENT_CREATE and INVESTMENT_UPDATE from MANAGER and INVESTOR
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('MANAGER', 'INVESTOR'))
  AND permission_id IN (
      SELECT id FROM permissions
      WHERE name IN ('INVESTMENT_CREATE', 'INVESTMENT_UPDATE')
  );

-- Part 2: Create investment_rounds table
CREATE TABLE investment_rounds (
    id VARCHAR(255) PRIMARY KEY,
    round_number VARCHAR(50) NOT NULL,
    shop_id VARCHAR(255) NOT NULL,
    investment_type VARCHAR(50) NOT NULL,
    profit_sharing_model VARCHAR(50) NOT NULL,
    maturity_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',

    -- Tier configuration fields (embedded)
    tier1_threshold NUMERIC(12,2),
    tier1_multiplier NUMERIC(5,2),
    tier2_threshold NUMERIC(12,2),
    tier2_multiplier NUMERIC(5,2),
    tier3_threshold NUMERIC(12,2),
    tier3_multiplier NUMERIC(5,2),

    -- Time weighting fields (embedded)
    base_years NUMERIC(5,2),
    base_multiplier NUMERIC(5,2),
    year2_threshold NUMERIC(5,2),
    year2_multiplier NUMERIC(5,2),
    year3_threshold NUMERIC(5,2),
    year3_multiplier NUMERIC(5,2),
    max_multiplier NUMERIC(5,2),

    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT DEFAULT 0,

    CONSTRAINT fk_investment_round_shop FOREIGN KEY (shop_id) REFERENCES shops(id),
    CONSTRAINT uk_round_number_shop UNIQUE (round_number, shop_id)
);

CREATE INDEX idx_investment_round_shop ON investment_rounds(shop_id);
CREATE INDEX idx_investment_round_number ON investment_rounds(round_number);
CREATE INDEX idx_investment_round_status ON investment_rounds(status);

-- Part 3: Add investment_round_id to investments
ALTER TABLE investments ADD COLUMN investment_round_id VARCHAR(255);
ALTER TABLE investments ADD CONSTRAINT fk_investment_round
    FOREIGN KEY (investment_round_id) REFERENCES investment_rounds(id);

CREATE INDEX idx_investment_round ON investments(investment_round_id);

-- Part 4: Drop columns that moved to investment_rounds
-- NOTE: Only do this AFTER migrating existing data!
-- For now, make these nullable to support migration
ALTER TABLE investments ALTER COLUMN investment_type DROP NOT NULL;
ALTER TABLE investments ALTER COLUMN profit_sharing_model DROP NOT NULL;

-- Part 5: Migrate existing investments to default rounds
-- Create a default round for each shop with existing investments
INSERT INTO investment_rounds (id, round_number, shop_id, investment_type, profit_sharing_model, status, notes, created_at, updated_at, version)
SELECT
    gen_random_uuid()::varchar,
    'MIGRATED-' || s.name || '-' || TO_CHAR(NOW(), 'YYYY-MM'),
    s.id,
    'SHOP_WIDE',
    'PROPORTIONAL_BY_AMOUNT',
    'CLOSED',
    'Migrated from legacy investments',
    NOW(),
    NOW(),
    0
FROM shops s
WHERE EXISTS (SELECT 1 FROM investments i WHERE i.shop_id = s.id AND i.investment_round_id IS NULL);

-- Link existing investments to their migration rounds
UPDATE investments i
SET investment_round_id = (
    SELECT ir.id
    FROM investment_rounds ir
    WHERE ir.shop_id = i.shop_id
    AND ir.round_number LIKE 'MIGRATED-%'
    LIMIT 1
)
WHERE investment_round_id IS NULL;

-- Now make investment_round_id NOT NULL
ALTER TABLE investments ALTER COLUMN investment_round_id SET NOT NULL;
```

### Phase 8: Update test-data.sql
Add sample investment rounds with investors for testing.

### Phase 9: Documentation

#### docs/INVESTMENT_GUIDE.md
Create comprehensive guide explaining:
- Investment rounds concept
- Each profit sharing model with examples
- API usage examples
- Best practices

#### Update src/docs/asciidoc/index.adoc
Add investment section with API documentation.

#### Update docs/PERMISSION_MATRIX.md
Reflect new permissions (TENANT_ADMIN column).

### Phase 10: Tests
Create comprehensive integration tests covering all scenarios.

## Notes for Next Session

1. **CRITICAL**: Fix permission-matrix.csv header - add TENANT_ADMIN column
2. The Investment entity now delegates to InvestmentRound for type/model/maturity
3. All profit calculations must use round-based aggregation
4. Ensure migration handles existing data gracefully
5. Test with multiple scenarios (PROPORTIONAL, FIXED_SHARES, TIME_WEIGHTED, TIERED)
