-- V42: Add password hash column for embedded mode authentication
-- This allows the application to support both Keycloak (cloud) and local JWT (embedded) authentication

-- Add password_hash column (nullable for backward compatibility with Keycloak-only users)
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Make keycloak_id nullable to support embedded mode users
ALTER TABLE users ALTER COLUMN keycloak_id DROP NOT NULL;

-- Add comment
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password for embedded mode authentication (null for Keycloak-only users)';
COMMENT ON COLUMN users.keycloak_id IS 'Keycloak user ID for cloud mode (null for embedded-only users)';

-- Note: Existing users with keycloak_id will continue to use Keycloak authentication
-- New embedded mode users will have password_hash set and keycloak_id as null
