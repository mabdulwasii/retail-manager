-- Migration V22: Investment Rounds and Permission Updates
-- This migration implements the investment round architecture redesign

-- Part 1: Remove INVESTMENT_CREATE and INVESTMENT_UPDATE from MANAGER and INVESTOR roles
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('MANAGER', 'INVESTOR'))
  AND permission_id IN (
      SELECT id FROM permissions
      WHERE name IN ('INVESTMENT_CREATE', 'INVESTMENT_UPDATE')
  );

-- Part 2: Create investment_rounds table
CREATE TABLE IF NOT EXISTS investment_rounds (
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

    -- Audit fields
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

-- Part 3: Add investment_round_id to investments table
ALTER TABLE investments ADD COLUMN investment_round_id VARCHAR(255);
ALTER TABLE investments ADD CONSTRAINT fk_investment_round
    FOREIGN KEY (investment_round_id) REFERENCES investment_rounds(id);

CREATE INDEX idx_investment_round ON investments(investment_round_id);

-- Part 4: Make old fields nullable to support migration
-- We keep these fields for backward compatibility during migration period
ALTER TABLE investments ALTER COLUMN investment_type DROP NOT NULL;
ALTER TABLE investments ALTER COLUMN profit_sharing_model DROP NOT NULL;

-- Part 5: Migrate existing investments to default rounds
-- Create a default "MIGRATED" round for each shop with existing investments
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

-- Part 6: Make investment_round_id NOT NULL after migration
ALTER TABLE investments ALTER COLUMN investment_round_id SET NOT NULL;

-- Part 7: Add comments for documentation
COMMENT ON TABLE investment_rounds IS 'Investment rounds group multiple investors with shared configuration (profit model, type, maturity date)';
COMMENT ON COLUMN investment_rounds.round_number IS 'Round number format: ROUND-{SHOP_CODE}-{YEAR}-Q{QUARTER}-{SEQUENCE}';
COMMENT ON COLUMN investment_rounds.status IS 'Round status: OPEN (accepting investors), CLOSED (no new investors), COMPLETED (all distributions paid), CANCELLED';
COMMENT ON COLUMN investment_rounds.tier1_threshold IS 'Tier 1 minimum amount threshold for TIERED profit sharing';
COMMENT ON COLUMN investment_rounds.tier1_multiplier IS 'Tier 1 profit multiplier (e.g., 1.0 = 100%, 1.2 = 120%)';
COMMENT ON COLUMN investment_rounds.base_years IS 'Base years for 1.0x multiplier in TIME_WEIGHTED model';
COMMENT ON COLUMN investment_rounds.max_multiplier IS 'Maximum multiplier cap for TIME_WEIGHTED model';

COMMENT ON COLUMN investments.investment_round_id IS 'FK to investment_rounds - all investments must belong to a round';
