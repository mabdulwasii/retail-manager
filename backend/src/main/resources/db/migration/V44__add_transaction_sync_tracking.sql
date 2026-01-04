-- V44: Add sync tracking to sales_transactions for offline resilience
-- This ensures transactions are properly synced even after weeks offline

-- Add sync tracking columns to sales_transactions
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS synced_to_cloud BOOLEAN DEFAULT false;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS sync_attempts INTEGER DEFAULT 0;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS last_sync_attempt TIMESTAMP;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS last_sync_error TEXT;
ALTER TABLE sales_transactions ADD COLUMN IF NOT EXISTS synced_at TIMESTAMP;

-- Create index for efficient querying of unsynced transactions
CREATE INDEX IF NOT EXISTS idx_sales_transactions_sync_status
    ON sales_transactions(synced_to_cloud, created_at)
    WHERE synced_to_cloud = false;

-- Create index for sync retry queries
CREATE INDEX IF NOT EXISTS idx_sales_transactions_sync_attempts
    ON sales_transactions(sync_attempts, last_sync_attempt)
    WHERE synced_to_cloud = false;

-- Add comments
COMMENT ON COLUMN sales_transactions.synced_to_cloud IS 'Whether transaction has been successfully synced to cloud aggregator';
COMMENT ON COLUMN sales_transactions.sync_attempts IS 'Number of sync attempts made for this transaction';
COMMENT ON COLUMN sales_transactions.last_sync_attempt IS 'Timestamp of last sync attempt';
COMMENT ON COLUMN sales_transactions.last_sync_error IS 'Error message from last failed sync attempt';
COMMENT ON COLUMN sales_transactions.synced_at IS 'Timestamp when transaction was successfully synced to cloud';
