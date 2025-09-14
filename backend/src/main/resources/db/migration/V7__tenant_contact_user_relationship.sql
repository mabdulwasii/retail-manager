-- Migration to change tenant contact from email string to User entity relationship
-- This addresses the requirement to have tenant contact as a User entity

-- Add the new contact_user_id column
ALTER TABLE tenants ADD COLUMN contact_user_id VARCHAR(255);

-- Add foreign key constraint
ALTER TABLE tenants ADD CONSTRAINT fk_tenant_contact_user
    FOREIGN KEY (contact_user_id) REFERENCES users(id);

-- For existing tenants, create contact users based on their contact_email
-- This ensures data integrity during migration
INSERT INTO users (id, tenant_id, keycloak_id, username, email, first_name, last_name, status, created_at, updated_at)
SELECT
    gen_random_uuid() as id,
    t.id as tenant_id,
    'contact-' || replace(t.id, '-', '') as keycloak_id,
    'contact-' || t.name as username,
    t.contact_email as email,
    'Contact' as first_name,
    'Admin' as last_name,
    'ACTIVE' as status,
    NOW() as created_at,
    NOW() as updated_at
FROM tenants t
WHERE t.contact_email IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM users u WHERE u.email = t.contact_email AND u.tenant_id = t.id
);

-- Update tenants to reference the newly created contact users
UPDATE tenants t SET contact_user_id = (
    SELECT u.id FROM users u
    WHERE u.email = t.contact_email AND u.tenant_id = t.id
    LIMIT 1
)
WHERE t.contact_email IS NOT NULL;

-- Keep the contact_email column for now for backward compatibility
-- It will be removed in a future migration after full testing
-- ALTER TABLE tenants DROP COLUMN contact_email;

-- Add index for performance
CREATE INDEX idx_tenants_contact_user_id ON tenants(contact_user_id);

-- Update table comments
COMMENT ON COLUMN tenants.contact_user_id IS 'Reference to the primary contact user for this tenant';
COMMENT ON COLUMN tenants.contact_email IS 'Legacy contact email field - to be removed after migration verification';