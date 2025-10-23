-- Rename contact_user column to contact_user_id to match JPA naming conventions
-- This fixes the schema mismatch between database and Hibernate entity mapping

-- Drop the existing foreign key constraint
ALTER TABLE tenants DROP CONSTRAINT IF EXISTS fk_tenant_contact_user;

-- Rename the column
ALTER TABLE tenants RENAME COLUMN contact_user TO contact_user_id;

-- Recreate the foreign key constraint with the new column name
ALTER TABLE tenants ADD CONSTRAINT fk_tenant_contact_user
    FOREIGN KEY (contact_user_id) REFERENCES users(id);

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_tenant_contact_user ON tenants(contact_user_id);