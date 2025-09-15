-- V9: Spring Modulith Event Store
-- Creates the event_publication table required by Spring Modulith for event sourcing

-- Spring Modulith event_publication table
-- This table stores domain events that are published across module boundaries
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP WITH TIME ZONE NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_event_publication_listener ON event_publication(listener_id);
CREATE INDEX IF NOT EXISTS idx_event_publication_completion ON event_publication(completion_date);
CREATE INDEX IF NOT EXISTS idx_event_publication_date ON event_publication(publication_date);

-- Partial index for incomplete events (most common query)
CREATE INDEX IF NOT EXISTS idx_event_publication_incomplete
    ON event_publication(listener_id, publication_date)
    WHERE completion_date IS NULL;

-- Comments for maintenance
COMMENT ON TABLE event_publication IS 'Spring Modulith event store for cross-module domain events';
COMMENT ON COLUMN event_publication.listener_id IS 'Unique identifier for the event listener';
COMMENT ON COLUMN event_publication.event_type IS 'Fully qualified class name of the event';
COMMENT ON COLUMN event_publication.serialized_event IS 'JSON serialized event data';
COMMENT ON COLUMN event_publication.publication_date IS 'When the event was published';
COMMENT ON COLUMN event_publication.completion_date IS 'When the event was successfully processed (NULL = pending)';