-- Shop Customization and Multi-Tenant Features Migration
-- Adds shop customization table and additional feature flags for multi-tenant support

-- Shop Customizations table for branding and UI customization
CREATE TABLE shop_customizations (
    id VARCHAR(36) PRIMARY KEY,
    shop_id VARCHAR(36) NOT NULL UNIQUE,

    -- Brand Identity Colors
    primary_color VARCHAR(7),         -- Hexadecimal color format (#FF5733)
    secondary_color VARCHAR(7),
    accent_color VARCHAR(7),
    background_color VARCHAR(7),
    text_color VARCHAR(7),

    -- Logo and Images
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    banner_image_url VARCHAR(500),
    background_image_url VARCHAR(500),

    -- Contact and Website Information
    website_url VARCHAR(500),
    social_media_links TEXT,         -- JSON format for social media links

    -- UI Theme Settings
    theme_variant VARCHAR(20) NOT NULL DEFAULT 'LIGHT',  -- LIGHT, DARK, AUTO
    font_family VARCHAR(100),
    font_size VARCHAR(20) DEFAULT 'MEDIUM',              -- SMALL, MEDIUM, LARGE
    border_radius INTEGER,
    custom_styles TEXT,                                  -- JSON format for custom CSS

    -- Layout Preferences
    dashboard_layout VARCHAR(20) DEFAULT 'GRID',        -- GRID, LIST, CARD

    -- Receipt Customization
    receipt_header VARCHAR(1000),
    receipt_footer VARCHAR(1000),
    receipt_show_logo BOOLEAN DEFAULT TRUE,

    -- Feature Toggles for UI Elements
    show_banner BOOLEAN DEFAULT TRUE,
    enable_animations BOOLEAN DEFAULT TRUE,
    show_advanced_features BOOLEAN DEFAULT FALSE,

    -- Standard audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    -- Foreign key constraint
    FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
);

-- Create indexes for shop customizations
CREATE INDEX idx_customization_shop ON shop_customizations(shop_id);
CREATE INDEX idx_customization_theme ON shop_customizations(theme_variant);
CREATE INDEX idx_customization_layout ON shop_customizations(dashboard_layout);

-- Add additional feature flags for multi-tenant and customization features
INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by) VALUES
-- Global multi-tenant feature flags
('ff-global-multi-tenant', NULL, 'multi-tenant.enabled', TRUE, 'Enable multi-tenant isolation and context switching', 'SYSTEM'),
('ff-global-customization', NULL, 'customization.enabled', TRUE, 'Enable shop customization and branding features', 'SYSTEM'),
('ff-global-theme-switching', NULL, 'themes.enabled', TRUE, 'Enable theme switching and UI customization', 'SYSTEM'),
('ff-global-logo-upload', NULL, 'logo-upload.enabled', TRUE, 'Enable logo and image upload functionality', 'SYSTEM'),
('ff-global-receipt-custom', NULL, 'receipt-customization.enabled', TRUE, 'Enable receipt header/footer customization', 'SYSTEM'),

-- Advanced feature flags
('ff-global-advanced-analytics', NULL, 'analytics.advanced', FALSE, 'Enable advanced analytics and BI features', 'SYSTEM'),
('ff-global-api-access', NULL, 'api-access.enabled', TRUE, 'Enable API access for external integrations', 'SYSTEM'),
('ff-global-mobile-app', NULL, 'mobile-app.enabled', FALSE, 'Enable mobile application features', 'SYSTEM'),
('ff-global-multi-currency', NULL, 'multi-currency.enabled', FALSE, 'Enable multiple currency support', 'SYSTEM'),
('ff-global-backup-cloud', NULL, 'backup.cloud', FALSE, 'Enable cloud backup functionality', 'SYSTEM');

-- Insert default customization for existing shops
INSERT INTO shop_customizations (
    id, shop_id, theme_variant, font_size, dashboard_layout,
    show_banner, enable_animations, show_advanced_features, receipt_show_logo
)
SELECT
    'cust-' || s.id,
    s.id,
    'LIGHT',
    'MEDIUM',
    'GRID',
    TRUE,
    TRUE,
    FALSE,
    TRUE
FROM shops s
WHERE NOT EXISTS (
    SELECT 1 FROM shop_customizations sc WHERE sc.shop_id = s.id
);

-- Add shop-specific feature flags for multi-tenancy control
INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by)
SELECT
    'ff-' || s.id || '-multi-tenant',
    s.id,
    'multi-tenant.enabled',
    TRUE,
    'Multi-tenant features enabled for shop: ' || s.name,
    'SYSTEM'
FROM shops s;

-- Add shop-specific customization feature flags
INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by)
SELECT
    'ff-' || s.id || '-customization',
    s.id,
    'customization.enabled',
    TRUE,
    'Customization features enabled for shop: ' || s.name,
    'SYSTEM'
FROM shops s;

-- Create view for shop branding information
CREATE VIEW shop_branding_view AS
SELECT
    s.id as shop_id,
    s.name as shop_name,
    s.email as shop_email,
    s.phone as shop_phone,
    s.website_url as shop_website,
    sc.primary_color,
    sc.secondary_color,
    sc.accent_color,
    sc.logo_url,
    sc.theme_variant,
    sc.font_family,
    sc.font_size,
    sc.dashboard_layout,
    sc.show_banner,
    sc.enable_animations,
    sc.receipt_show_logo
FROM shops s
LEFT JOIN shop_customizations sc ON s.id = sc.shop_id;

-- Create view for active feature flags by shop
CREATE VIEW shop_feature_flags_view AS
SELECT
    s.id as shop_id,
    s.name as shop_name,
    ff.feature_name,
    ff.enabled,
    ff.description,
    CASE
        WHEN ff.effective_from IS NULL OR ff.effective_from <= CURRENT_TIMESTAMP THEN TRUE
        ELSE FALSE
    END as is_effective_from,
    CASE
        WHEN ff.effective_until IS NULL OR ff.effective_until > CURRENT_TIMESTAMP THEN TRUE
        ELSE FALSE
    END as is_effective_until,
    (ff.enabled = TRUE
     AND (ff.effective_from IS NULL OR ff.effective_from <= CURRENT_TIMESTAMP)
     AND (ff.effective_until IS NULL OR ff.effective_until > CURRENT_TIMESTAMP)
    ) as is_active
FROM shops s
LEFT JOIN feature_flags ff ON (ff.shop_id = s.id OR ff.shop_id IS NULL)
ORDER BY s.name, ff.feature_name;

-- Create function to get effective feature flag value for a shop
CREATE OR REPLACE FUNCTION get_feature_flag_value(
    p_shop_id VARCHAR(36),
    p_feature_name VARCHAR(255)
) RETURNS BOOLEAN AS $$
DECLARE
    shop_specific_flag BOOLEAN;
    global_flag BOOLEAN;
BEGIN
    -- First check for shop-specific flag
    SELECT
        (enabled = TRUE
         AND (effective_from IS NULL OR effective_from <= CURRENT_TIMESTAMP)
         AND (effective_until IS NULL OR effective_until > CURRENT_TIMESTAMP)
        )
    INTO shop_specific_flag
    FROM feature_flags
    WHERE shop_id = p_shop_id AND feature_name = p_feature_name;

    -- If shop-specific flag exists, return it
    IF shop_specific_flag IS NOT NULL THEN
        RETURN shop_specific_flag;
    END IF;

    -- Otherwise, check for global flag
    SELECT
        (enabled = TRUE
         AND (effective_from IS NULL OR effective_from <= CURRENT_TIMESTAMP)
         AND (effective_until IS NULL OR effective_until > CURRENT_TIMESTAMP)
        )
    INTO global_flag
    FROM feature_flags
    WHERE shop_id IS NULL AND feature_name = p_feature_name;

    -- Return global flag value or FALSE if not found
    RETURN COALESCE(global_flag, FALSE);
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically create default customization for new shops
CREATE OR REPLACE FUNCTION create_default_shop_customization()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO shop_customizations (
        id, shop_id, theme_variant, font_size, dashboard_layout,
        show_banner, enable_animations, show_advanced_features, receipt_show_logo
    ) VALUES (
        'cust-' || NEW.id,
        NEW.id,
        'LIGHT',
        'MEDIUM',
        'GRID',
        TRUE,
        TRUE,
        FALSE,
        TRUE
    );

    -- Also create default feature flags for the new shop
    INSERT INTO feature_flags (id, shop_id, feature_name, enabled, description, created_by) VALUES
    ('ff-' || NEW.id || '-multi-tenant', NEW.id, 'multi-tenant.enabled', TRUE, 'Multi-tenant features for ' || NEW.name, 'SYSTEM'),
    ('ff-' || NEW.id || '-customization', NEW.id, 'customization.enabled', TRUE, 'Customization features for ' || NEW.name, 'SYSTEM');

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for new shops
CREATE TRIGGER trigger_create_shop_customization
    AFTER INSERT ON shops
    FOR EACH ROW
    EXECUTE FUNCTION create_default_shop_customization();

-- Add constraint to ensure valid color formats (hexadecimal)
ALTER TABLE shop_customizations
ADD CONSTRAINT check_primary_color_format
CHECK (primary_color IS NULL OR primary_color ~ '^#[0-9A-Fa-f]{6}$');

ALTER TABLE shop_customizations
ADD CONSTRAINT check_secondary_color_format
CHECK (secondary_color IS NULL OR secondary_color ~ '^#[0-9A-Fa-f]{6}$');

ALTER TABLE shop_customizations
ADD CONSTRAINT check_accent_color_format
CHECK (accent_color IS NULL OR accent_color ~ '^#[0-9A-Fa-f]{6}$');

-- Add constraint to ensure valid theme variants
ALTER TABLE shop_customizations
ADD CONSTRAINT check_theme_variant
CHECK (theme_variant IN ('LIGHT', 'DARK', 'AUTO'));

-- Add constraint to ensure valid font sizes
ALTER TABLE shop_customizations
ADD CONSTRAINT check_font_size
CHECK (font_size IN ('SMALL', 'MEDIUM', 'LARGE'));

-- Add constraint to ensure valid dashboard layouts
ALTER TABLE shop_customizations
ADD CONSTRAINT check_dashboard_layout
CHECK (dashboard_layout IN ('GRID', 'LIST', 'CARD'));

-- Update statistics for query optimization
ANALYZE shops;
ANALYZE shop_customizations;
ANALYZE feature_flags;