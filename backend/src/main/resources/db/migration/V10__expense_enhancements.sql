-- V10: Expense Management Enhancements
-- This migration enhances the expense tracking with categories and additional fields

-- Create expense_categories table
CREATE TABLE expense_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shop_id UUID NOT NULL REFERENCES shops(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    requires_approval BOOLEAN NOT NULL DEFAULT TRUE,
    approval_limit DECIMAL(10,2),
    default_payment_method VARCHAR(50),
    gl_account_code VARCHAR(50),
    tax_deductible BOOLEAN NOT NULL DEFAULT TRUE,
    auto_approval_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    UNIQUE(shop_id, name)
);

-- Create expense_tags table for many-to-many relationship
CREATE TABLE expense_tags (
    expense_id UUID NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (expense_id, tag)
);

-- Enhance the existing expenses table
ALTER TABLE expenses DROP COLUMN IF EXISTS category;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS category_id UUID;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS created_by_name VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS approved_by_name VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS approval_date DATE;
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS approval_notes TEXT;

-- Update expenses table constraints
ALTER TABLE expenses ALTER COLUMN description DROP NOT NULL;
ALTER TABLE expenses ALTER COLUMN status TYPE VARCHAR(20);
UPDATE expenses SET status = 'APPROVED' WHERE status = 'approved';
UPDATE expenses SET status = 'PENDING_APPROVAL' WHERE status = 'pending';
UPDATE expenses SET status = 'REJECTED' WHERE status = 'rejected';

-- Add foreign key constraint for category
ALTER TABLE expenses ADD CONSTRAINT fk_expenses_category_id
    FOREIGN KEY (category_id) REFERENCES expense_categories(id);

-- Add foreign key constraint for expense tags
ALTER TABLE expense_tags ADD CONSTRAINT fk_expense_tags_expense_id
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE;

-- Insert default expense categories for existing shops
INSERT INTO expense_categories (id, shop_id, name, description, requires_approval, approval_limit)
SELECT
    gen_random_uuid(),
    s.id,
    category_name,
    category_description,
    requires_approval,
    approval_limit
FROM shops s
CROSS JOIN (
    VALUES
        ('Shop Maintenance & Repairs', 'Shop repairs, maintenance, and improvements', TRUE, 50000),
        ('Transportation', 'Transport of goods, delivery costs, fuel', FALSE, 10000),
        ('Meals & Refreshments', 'Staff meals, refreshments, catering', FALSE, 5000),
        ('Utilities', 'Electricity, water, internet, phone bills', TRUE, 25000),
        ('Office & Shop Supplies', 'Stationery, cleaning supplies, packaging materials', FALSE, 15000),
        ('Marketing & Advertising', 'Promotional materials, advertising, marketing campaigns', TRUE, 30000),
        ('Professional Services', 'Legal, accounting, consulting services', TRUE, 100000),
        ('Equipment & Technology', 'POS systems, computers, tools, equipment', TRUE, 200000),
        ('Insurance', 'Shop insurance, liability coverage', TRUE, 50000),
        ('Other Expenses', 'Miscellaneous expenses not fitting other categories', TRUE, 20000)
) AS default_categories(category_name, category_description, requires_approval, approval_limit);

-- Update existing expenses to use the first category (Other Expenses)
UPDATE expenses
SET category_id = (
    SELECT id
    FROM expense_categories
    WHERE shop_id = expenses.shop_id
    AND name = 'Other Expenses'
    LIMIT 1
),
title = COALESCE(description, 'Legacy Expense')
WHERE category_id IS NULL;

-- Update expenses table to make category_id NOT NULL
ALTER TABLE expenses ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE expenses ALTER COLUMN title SET NOT NULL;

-- Create indexes for performance
CREATE INDEX idx_expense_categories_shop_active ON expense_categories(shop_id, is_active);
CREATE INDEX idx_expense_categories_name ON expense_categories(name);
CREATE INDEX idx_expense_categories_approval ON expense_categories(requires_approval, approval_limit);

CREATE INDEX idx_expense_tags_expense ON expense_tags(expense_id);
CREATE INDEX idx_expense_tags_tag ON expense_tags(tag);

CREATE INDEX idx_expenses_category_id ON expenses(category_id);
CREATE INDEX idx_expenses_title ON expenses(title);
CREATE INDEX idx_expenses_created_by_name ON expenses(created_by_name);
CREATE INDEX idx_expenses_approval_date ON expenses(approval_date);

-- Create trigger for automatic timestamp updates on expense_categories
CREATE TRIGGER update_expense_categories_updated_at
    BEFORE UPDATE ON expense_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints
ALTER TABLE expense_categories ADD CONSTRAINT chk_expense_categories_approval_limit_positive
    CHECK (approval_limit IS NULL OR approval_limit >= 0);

ALTER TABLE expenses ADD CONSTRAINT chk_expenses_amount_positive
    CHECK (amount > 0);

-- Update audit_logs to include shopId column if it doesn't exist
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS shop_id UUID REFERENCES shops(id);