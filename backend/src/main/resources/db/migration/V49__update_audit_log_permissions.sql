-- V49: Update audit log permissions to match new API endpoints
-- Updates existing AUDIT_LOG_VIEW_SHOP and AUDIT_LOG_VIEW_TENANT to AUDIT_LOG_LIST and AUDIT_LOG_EXPORT

-- Update permission names
UPDATE permissions
SET name = 'AUDIT_LOG_LIST',
    description = 'List and filter audit logs'
WHERE name = 'AUDIT_LOG_VIEW_SHOP';

UPDATE permissions
SET name = 'AUDIT_LOG_EXPORT',
    description = 'Export audit logs to CSV'
WHERE name = 'AUDIT_LOG_VIEW_TENANT';
