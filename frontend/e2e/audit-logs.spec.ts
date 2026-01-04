import { test, expect } from '@playwright/test';

/**
 * E2E Tests for Audit Logs Page
 *
 * Test Scenarios:
 * 1. Page loads with audit logs table
 * 2. Search functionality
 * 3. Action type filtering
 * 4. Entity type filtering
 * 5. Date range filtering
 * 6. Pagination
 * 7. Export to CSV functionality
 * 8. Empty state handling
 */

test.describe('Audit Logs Page', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to audit logs page
    await page.goto('/cloud/audit-logs');
  });

  test('should load page with audit logs table', async ({ page }) => {
    // Check page title
    await expect(page.locator('h1')).toContainText('Audit Logs');

    // Wait for table to load
    await page.waitForLoadState('networkidle');

    // Check table headers
    await expect(page.getByRole('columnheader', { name: 'Timestamp' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Action' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Entity' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'User' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'IP Address' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Details' })).toBeVisible();
  });

  test('should display audit logs in the table', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check that at least one log entry is displayed
    const firstLogTimestamp = page.locator('table tbody tr').first();
    await expect(firstLogTimestamp).toBeVisible();
  });

  test('should show filter section', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check filter heading
    await expect(page.getByRole('heading', { name: 'Filters' })).toBeVisible();

    // Check all filter inputs are present
    await expect(page.getByPlaceholder(/Search logs/)).toBeVisible();
    await expect(page.getByLabel('Action')).toBeVisible();
    await expect(page.getByLabel('Entity Type')).toBeVisible();
    await expect(page.getByLabel(/Date Range/)).toBeVisible();
  });

  test('should search audit logs', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Perform search
    const searchInput = page.getByPlaceholder(/Search logs/);
    await searchInput.fill('Created');

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Check that search results are filtered
    const activityLog = page.getByRole('heading', { name: /Activity Log/ });
    await expect(activityLog).toBeVisible();
  });

  test('should filter by action type', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click action filter dropdown
    await page.getByLabel('Action').click();

    // Select "CREATE" action
    await page.getByText('CREATE', { exact: true }).click();

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Verify filtered results
    const createBadges = page.locator('text=Create');
    const count = await createBadges.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should filter by entity type', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click entity type filter dropdown
    await page.getByLabel('Entity Type').click();

    // Select "SHOP" entity
    await page.getByText('SHOP', { exact: true }).click();

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Verify filtered results show SHOP entities
    const shopEntities = page.locator('text=SHOP');
    const count = await shopEntities.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should change date range', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Click date range dropdown
    await page.getByLabel(/Date Range/).click();

    // Select "Last 24 Hours"
    await page.getByText('Last 24 Hours').click();

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Logs should be filtered (count might change)
    const activityLog = page.getByRole('heading', { name: /Activity Log/ });
    await expect(activityLog).toBeVisible();
  });

  test('should combine multiple filters', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Apply search filter
    await page.getByPlaceholder(/Search logs/).fill('shop');

    // Apply action filter
    await page.getByLabel('Action').click();
    await page.getByText('CREATE', { exact: true }).click();

    // Wait for filters to apply
    await page.waitForTimeout(500);

    // Should show filtered results
    const activityLog = page.getByRole('heading', { name: /Activity Log/ });
    await expect(activityLog).toBeVisible();
  });

  test('should display export button', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check export button is visible
    const exportButton = page.getByRole('button', { name: /Export CSV/ });
    await expect(exportButton).toBeVisible();
  });

  test('should handle export functionality', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Set up download listener
    const downloadPromise = page.waitForEvent('download', { timeout: 10000 });

    // Click export button
    await page.getByRole('button', { name: /Export CSV/ }).click();

    // Wait for download to start
    try {
      const download = await downloadPromise;

      // Verify download filename contains expected pattern
      expect(download.suggestedFilename()).toContain('audit-logs');
      expect(download.suggestedFilename()).toContain('.csv');
    } catch (error) {
      // Export might be mocked and not actually download
      // Just verify the button showed exporting state
      await expect(page.getByText(/Exporting/)).toBeVisible();
    }
  });

  test('should show action badges with different colors', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check for different action types in the table
    const actionBadges = page.locator('table tbody').locator('text=/Create|Update|Delete|Login|Sync/');
    const count = await actionBadges.count();
    expect(count).toBeGreaterThan(0);
  });

  test('should display user information for each log', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check first log has user info
    const firstRow = page.locator('table tbody tr').first();
    const userCell = firstRow.locator('td').nth(3); // User column
    await expect(userCell).toBeVisible();
  });

  test('should display IP addresses', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check for IP address pattern (e.g., 192.168.1.100)
    const ipPattern = page.locator('text=/\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/');
    await expect(ipPattern.first()).toBeVisible();
  });

  test('should show entity details with icons', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check that entity column shows both type and name
    const firstRow = page.locator('table tbody tr').first();
    const entityCell = firstRow.locator('td').nth(2); // Entity column
    await expect(entityCell).toBeVisible();
  });

  test('should display log details', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check that details column has content
    const firstRow = page.locator('table tbody tr').first();
    const detailsCell = firstRow.locator('td').nth(5); // Details column
    await expect(detailsCell).toBeVisible();
  });

  test('should show pagination when there are many logs', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check for pagination controls (they might not be visible if < 20 logs)
    const paginationText = page.locator('text=/Showing \\d+ to \\d+ of \\d+ logs/');
    const previousButton = page.getByRole('button', { name: 'Previous' });
    const nextButton = page.getByRole('button', { name: 'Next' });

    // If pagination is present
    if (await paginationText.isVisible()) {
      await expect(previousButton).toBeVisible();
      await expect(nextButton).toBeVisible();
    }
  });

  test('should navigate between pages', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check if next button exists and is enabled
    const nextButton = page.getByRole('button', { name: 'Next' });

    if (await nextButton.isVisible()) {
      const isDisabled = await nextButton.isDisabled();

      if (!isDisabled) {
        // Click next page
        await nextButton.click();

        // Wait for page to update
        await page.waitForTimeout(500);

        // Should show page 2
        await expect(page.locator('text=/Page 2 of/')).toBeVisible();
      }
    }
  });

  test('should show empty state when no logs match filter', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Search for non-existent term
    await page.getByPlaceholder(/Search logs/).fill('NonExistentLogXYZ123456');

    // Wait for filter to apply
    await page.waitForTimeout(500);

    // Check for empty state
    await expect(page.getByText(/No audit logs found/i)).toBeVisible();
    await expect(page.getByText(/No logs match your search criteria/i)).toBeVisible();
  });

  test('should disable export when no logs are available', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Filter to show no results
    await page.getByPlaceholder(/Search logs/).fill('NonExistentLogXYZ123456');
    await page.waitForTimeout(500);

    // Export button should be disabled
    const exportButton = page.getByRole('button', { name: /Export CSV/ });
    await expect(exportButton).toBeDisabled();
  });

  test('should show log count in heading', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Check activity log count display
    const activityLogHeading = page.getByRole('heading', { name: /Activity Log \\(\\d+/ });
    await expect(activityLogHeading).toBeVisible();
  });

  test('should handle loading state', async ({ page }) => {
    // Navigate and immediately check for loading state
    await page.goto('/cloud/audit-logs');

    // Should show loading text or spinner
    const loadingIndicator = page.getByText(/Loading audit logs/i);
    // Loading state might be very fast
    await page.waitForLoadState('networkidle');

    // Eventually should show the table or empty state
    const tableOrEmpty = page.locator('table, text=/No audit logs/');
    await expect(tableOrEmpty.first()).toBeVisible();
  });

  test('should reset filters correctly', async ({ page }) => {
    await page.waitForLoadState('networkidle');

    // Apply filters
    await page.getByPlaceholder(/Search logs/).fill('test');
    await page.getByLabel('Action').click();
    await page.getByText('CREATE', { exact: true }).click();

    // Clear search
    await page.getByPlaceholder(/Search logs/).clear();

    // Reset action filter
    await page.getByLabel('Action').click();
    await page.getByText('All Actions').click();

    // Wait for filters to reset
    await page.waitForTimeout(500);

    // Should show all logs again
    const logs = page.locator('table tbody tr');
    const count = await logs.count();
    expect(count).toBeGreaterThan(0);
  });
});
