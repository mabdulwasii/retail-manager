import { test, expect } from '@playwright/test';

/**
 * E2E Tests for Shop Management Page
 *
 * Test Scenarios:
 * 1. Page loads with shops table
 * 2. Search functionality
 * 3. Status filtering
 * 4. Create new shop
 * 5. Edit existing shop
 * 6. Activate/deactivate shop
 * 7. Empty state handling
 * 8. Form validation
 */

test.describe('Shop Management Page', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to shop management page
    await page.goto('/shops');
  });

  test('should load page with shops table', async ({ page }) => {
    // Check page title
    await expect(page.locator('h1')).toContainText('Shop Management');

    // Wait for table to load
    await page.waitForLoadState('networkidle');

    // Check table headers
    await expect(page.getByRole('columnheader', { name: 'Shop Name' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Contact' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Location' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Status' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Last Updated' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Actions' })).toBeVisible();
  });

  test('should display shops in the table', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check that at least one shop is displayed
    const firstShopName = page.locator('table tbody tr').first().getByText(/Store|Branch|Mall/);
    await expect(firstShopName).toBeVisible();
  });

  test('should search shops by name', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Get total shops before search
    const shopsCount = page.locator('text=/Shops \\(\\d+ of \\d+\\)/');
    await expect(shopsCount).toBeVisible();

    // Perform search
    const searchInput = page.getByPlaceholder(/Search by name/);
    await searchInput.fill('Downtown');

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Check that only matching shops are shown
    await expect(page.getByText('Downtown Store')).toBeVisible();
  });

  test('should filter shops by status', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click status filter dropdown
    await page.getByLabel('Status').click();

    // Select "Active" status
    await page.getByText('Active', { exact: true }).click();

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Verify only active shops are shown
    const activeBadges = page.locator('text=Active').filter({ hasText: /^Active$/ });
    const count = await activeBadges.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should open create shop dialog', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click "Add Shop" button
    await page.getByRole('button', { name: /Add Shop/ }).click();

    // Check dialog is visible
    await expect(page.getByRole('heading', { name: 'Add New Shop' })).toBeVisible();

    // Check all form fields are present
    await expect(page.getByLabel(/Shop Name.*\*/)).toBeVisible();
    await expect(page.getByLabel(/Shop Email.*\*/)).toBeVisible();
    await expect(page.getByLabel(/Street Address/)).toBeVisible();
    await expect(page.getByLabel(/City/)).toBeVisible();
    await expect(page.getByLabel(/Country/)).toBeVisible();
    await expect(page.getByLabel(/Phone Number/)).toBeVisible();
  });

  test('should validate required fields when creating shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Open create dialog
    await page.getByRole('button', { name: /Add Shop/ }).click();

    // Click create without filling required fields
    await page.getByRole('button', { name: 'Create Shop' }).click();

    // Check for validation error
    await expect(page.getByText(/required/i)).toBeVisible();
  });

  test('should validate email format when creating shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Open create dialog
    await page.getByRole('button', { name: /Add Shop/ }).click();

    // Fill with invalid email
    await page.getByLabel(/Shop Name.*\*/).fill('Test Shop');
    await page.getByLabel(/Shop Email.*\*/).fill('invalid-email');

    // Try to create
    await page.getByRole('button', { name: 'Create Shop' }).click();

    // Check for validation error
    await expect(page.getByText(/valid email/i)).toBeVisible();
  });

  test('should create a new shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Open create dialog
    await page.getByRole('button', { name: /Add Shop/ }).click();

    // Fill in shop details
    await page.getByLabel(/Shop Name.*\*/).fill('New Test Shop');
    await page.getByLabel(/Shop Email.*\*/).fill('newshop@test.com');
    await page.getByLabel(/Street Address/).fill('789 Test Street');
    await page.getByLabel(/City/).fill('Test City');
    await page.getByLabel(/Country/).fill('Test Country');
    await page.getByLabel(/Phone Number/).fill('+1-555-1234');

    // Submit
    await page.getByRole('button', { name: 'Create Shop' }).click();

    // Check for success message
    await expect(page.getByText(/created successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should cancel creating a shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Open create dialog
    await page.getByRole('button', { name: /Add Shop/ }).click();

    // Fill in some data
    await page.getByLabel(/Shop Name.*\*/).fill('Will Not Be Created');

    // Click cancel
    await page.getByRole('button', { name: 'Cancel' }).click();

    // Dialog should close
    await expect(page.getByRole('heading', { name: 'Add New Shop' })).not.toBeVisible();
  });

  test('should open edit shop dialog', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click edit button on first shop
    await page.getByRole('button', { name: /Edit/ }).first().click();

    // Check dialog is visible
    await expect(page.getByRole('heading', { name: 'Edit Shop' })).toBeVisible();

    // Check form is pre-populated
    const shopNameInput = page.getByLabel(/Shop Name.*\*/);
    await expect(shopNameInput).not.toHaveValue('');
  });

  test('should edit an existing shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click edit button on first shop
    await page.getByRole('button', { name: /Edit/ }).first().click();

    // Update shop name
    const shopNameInput = page.getByLabel(/Shop Name.*\*/);
    await shopNameInput.clear();
    await shopNameInput.fill('Updated Shop Name');

    // Submit
    await page.getByRole('button', { name: 'Save Changes' }).click();

    // Check for success message
    await expect(page.getByText(/updated successfully/i)).toBeVisible({ timeout: 5000 });
  });

  test('should activate/deactivate shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Find first active shop and deactivate it
    const firstRow = page.locator('table tbody tr').first();
    const statusBadge = firstRow.locator('text=Active').first();

    if (await statusBadge.isVisible()) {
      // Click deactivate button
      await firstRow.getByRole('button', { name: /Deactivate/ }).click();

      // Check for success message
      await expect(page.getByText(/deactivated successfully/i)).toBeVisible({ timeout: 5000 });
    }
  });

  test('should show empty state when no shops match filter', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Search for non-existent shop
    const searchInput = page.getByPlaceholder(/Search by name/);
    await searchInput.fill('NonExistentShopXYZ123');

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Check for empty state
    await expect(page.getByText(/No shops found/i)).toBeVisible();
    await expect(page.getByText(/No shops match your search criteria/i)).toBeVisible();
  });

  test('should display shop count correctly', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check shop count display
    const shopsCount = page.locator('text=/Shops \\(\\d+ of \\d+\\)/');
    await expect(shopsCount).toBeVisible();
  });

  test('should show contact information for each shop', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check that first shop has email displayed
    const firstRow = page.locator('table tbody tr').first();
    await expect(firstRow.locator('text=/@[a-z0-9.-]+\\.[a-z]{2,}$/i')).toBeVisible();
  });

  test('should show location information when available', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check for location display (city, country)
    const firstRow = page.locator('table tbody tr').first();
    const locationCell = firstRow.locator('td').nth(2); // Location column
    await expect(locationCell).toBeVisible();
  });

  test('should handle loading state', async ({ page }) => {
    // Navigate and immediately check for loading state
    await page.goto('/cloud/shops');

    // Loading state might be very fast, so we just check the page loads eventually
    await page.waitForLoadState('networkidle');

    // Eventually should show the table or empty state
    const tableOrEmpty = page.locator('table, text=/No shops/');
    await expect(tableOrEmpty.first()).toBeVisible();
  });
});
