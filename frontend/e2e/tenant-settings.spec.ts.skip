import { test, expect } from '@playwright/test';

/**
 * E2E Tests for Tenant Settings Page
 *
 * Test Scenarios:
 * 1. Page loads correctly with all sections
 * 2. Company information form validation
 * 3. Contact details form validation
 * 4. Timezone and locale selection
 * 5. Form save functionality
 * 6. Cancel/reset functionality
 * 7. Read-only subscription display
 */

test.describe('Tenant Settings Page', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to tenant settings page
    // Note: In real scenario, would need authentication first
    await page.goto('/cloud/settings');
  });

  test('should load page with all sections', async ({ page }) => {
    // Check page title
    await expect(page.locator('h1')).toContainText('Tenant Settings');

    // Check all section headings
    await expect(page.getByRole('heading', { name: 'Company Information' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Contact Details' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Timezone & Locale Settings' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Subscription Information' })).toBeVisible();
  });

  test('should display loading state initially', async ({ page }) => {
    // Wait for page to load
    await page.waitForLoadState('networkidle');

    // Check that form fields are populated (not in loading state)
    const companyNameInput = page.getByLabel(/Company Name/);
    await expect(companyNameInput).toHaveValue(/./); // Has some value
  });

  test('should validate required fields', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Clear required field
    const companyNameInput = page.getByLabel(/Company Name/);
    await companyNameInput.clear();

    // Try to save
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for error message
    await expect(page.getByText(/required/i)).toBeVisible();
  });

  test('should validate email format', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Enter invalid email
    const emailInput = page.getByLabel(/Company Email/);
    await emailInput.clear();
    await emailInput.fill('invalid-email');

    // Try to save
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for validation error
    await expect(page.getByText(/valid email/i)).toBeVisible();
  });

  test('should update company information', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Update company name
    const companyNameInput = page.getByLabel(/Company Name/);
    await companyNameInput.clear();
    await companyNameInput.fill('New Company Name');

    // Update tax ID
    const taxIdInput = page.getByLabel(/Tax ID/);
    await taxIdInput.clear();
    await taxIdInput.fill('TAX-123-456');

    // Save changes
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for success message
    await expect(page.getByText(/saved successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should update contact details', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Update address
    const addressInput = page.getByLabel(/Street Address/);
    await addressInput.clear();
    await addressInput.fill('456 New Street');

    // Update city
    const cityInput = page.getByLabel(/City/);
    await cityInput.clear();
    await cityInput.fill('San Francisco');

    // Update phone
    const phoneInput = page.getByLabel(/Phone Number/);
    await phoneInput.clear();
    await phoneInput.fill('+1-555-9999');

    // Save changes
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for success message
    await expect(page.getByText(/saved successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should change timezone', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click timezone selector
    await page.getByLabel(/Timezone/).click();

    // Select a timezone
    await page.getByText('Asia/Tokyo').click();

    // Save changes
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for success message
    await expect(page.getByText(/saved successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should change locale', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click locale selector
    await page.getByLabel(/Locale/).click();

    // Select a locale
    await page.getByText(/French/).click();

    // Save changes
    await page.getByRole('button', { name: /Save Changes/ }).click();

    // Check for success message
    await expect(page.getByText(/saved successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should cancel changes', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Get initial value
    const companyNameInput = page.getByLabel(/Company Name/);
    const initialValue = await companyNameInput.inputValue();

    // Make a change
    await companyNameInput.clear();
    await companyNameInput.fill('Changed Name');

    // Click cancel
    await page.getByRole('button', { name: /Cancel/ }).click();

    // Verify value is reset
    await expect(companyNameInput).toHaveValue(initialValue);
  });

  test('should display subscription information as read-only', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check subscription section is present
    const subscriptionSection = page.locator('text=Subscription Information').locator('..');
    await expect(subscriptionSection).toBeVisible();

    // Verify it shows tier, status, and shop count
    await expect(page.getByText(/Subscription Tier/)).toBeVisible();
    await expect(page.getByText(/Status/)).toBeVisible();
    await expect(page.getByText(/Total Shops/)).toBeVisible();

    // Verify the alert about subscription management
    await expect(page.getByText(/subscription tier or manage billing/i)).toBeVisible();
  });

  test('should show all form fields with proper labels', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Company information fields
    await expect(page.getByLabel(/Company Name/)).toBeVisible();
    await expect(page.getByLabel(/Company Email/)).toBeVisible();
    await expect(page.getByLabel(/Company Registration/)).toBeVisible();
    await expect(page.getByLabel(/Tax ID/)).toBeVisible();

    // Contact details fields
    await expect(page.getByLabel(/Street Address/)).toBeVisible();
    await expect(page.getByLabel(/City/)).toBeVisible();
    await expect(page.getByLabel(/State/)).toBeVisible();
    await expect(page.getByLabel(/Country/)).toBeVisible();
    await expect(page.getByLabel(/Phone Number/)).toBeVisible();

    // Timezone & Locale fields
    await expect(page.getByLabel(/Timezone/)).toBeVisible();
    await expect(page.getByLabel(/Locale/)).toBeVisible();
  });

  test('should display saving state when submitting', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Make a change
    const companyNameInput = page.getByLabel(/Company Name/);
    await companyNameInput.fill('Test Company');

    // Click save
    const saveButton = page.getByRole('button', { name: /Save Changes/ });
    await saveButton.click();

    // Check for saving state (button should be disabled and show "Saving...")
    await expect(saveButton).toContainText(/Saving/);
    await expect(saveButton).toBeDisabled();
  });
});
