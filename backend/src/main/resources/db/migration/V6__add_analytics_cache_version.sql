-- Add cache_version column to analytics_cache table

ALTER TABLE analytics_cache ADD COLUMN IF NOT EXISTS cache_version VARCHAR(50);

-- Add comment for documentation
COMMENT ON COLUMN analytics_cache.cache_version IS 'Version identifier for cache invalidation strategy';
