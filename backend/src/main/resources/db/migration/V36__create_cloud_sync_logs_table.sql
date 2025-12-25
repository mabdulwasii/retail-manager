-- Create cloud_sync_logs table for embedded deployment sync tracking
CREATE TABLE cloud_sync_logs (
    id VARCHAR(255) PRIMARY KEY,
    store_id VARCHAR(255) NOT NULL,
    sync_batch_id VARCHAR(255) NOT NULL,
    sync_type VARCHAR(50) NOT NULL CHECK (sync_type IN ('SALES_TRANSACTIONS', 'INVENTORY_UPDATES', 'PRODUCT_CHANGES', 'FULL_SYNC')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'PARTIAL_SUCCESS')),
    sync_start_time TIMESTAMP NOT NULL,
    sync_end_time TIMESTAMP,
    records_processed INTEGER NOT NULL DEFAULT 0,
    records_synced INTEGER,
    records_failed INTEGER,
    error_message VARCHAR(2000),
    error_details VARCHAR(5000),
    retry_attempt INTEGER,
    duration_ms BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_cloud_sync_store ON cloud_sync_logs(store_id);
CREATE INDEX idx_cloud_sync_batch ON cloud_sync_logs(sync_batch_id);
CREATE INDEX idx_cloud_sync_status ON cloud_sync_logs(status);
CREATE INDEX idx_cloud_sync_start_time ON cloud_sync_logs(sync_start_time);
