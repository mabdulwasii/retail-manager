-- Fix investment-related date columns from DATE to TIMESTAMP
-- These entities use LocalDateTime which maps to TIMESTAMP

-- investments table
ALTER TABLE investments
ALTER COLUMN investment_date TYPE TIMESTAMP USING investment_date::TIMESTAMP;

-- investor_distributions table
ALTER TABLE investor_distributions
ALTER COLUMN period_start TYPE TIMESTAMP USING period_start::TIMESTAMP,
ALTER COLUMN period_end TYPE TIMESTAMP USING period_end::TIMESTAMP;
