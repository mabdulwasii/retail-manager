# E2E Testing Guide

This guide covers the end-to-end (E2E) and load testing setup for the Shop Manager Cloud Portal.

## Table of Contents
- [Overview](#overview)
- [E2E Testing with Playwright](#e2e-testing-with-playwright)
- [Load Testing with k6](#load-testing-with-k6)
- [Test Coverage](#test-coverage)
- [CI/CD Integration](#cicd-integration)

## Overview

The Shop Manager project includes comprehensive testing at multiple levels:

1. **E2E Tests (Playwright)**: User flow testing from UI perspective
2. **Load Tests (k6)**: API performance testing under concurrent load
3. **Unit/Integration Tests**: Backend service and repository testing (see main TESTING-GUIDE.md)

## E2E Testing with Playwright

### Prerequisites

- Node.js 18+ installed
- Frontend dependencies installed (`npm install`)
- Browsers installed (`npx playwright install`)

### Running E2E Tests

```bash
# Run all E2E tests (headless mode)
npm run test:e2e

# Run with Playwright UI (interactive mode)
npm run test:e2e:ui

# Run in headed mode (see browser)
npm run test:e2e:headed

# Show test report
npm run test:e2e:report
```

### Test Structure

E2E tests are located in the `e2e/` directory:

```
frontend/
├── e2e/
│   ├── tenant-settings.spec.ts    # Tenant Settings page tests
│   ├── shop-management.spec.ts    # Shop Management page tests
│   └── audit-logs.spec.ts         # Audit Logs page tests
└── playwright.config.ts            # Playwright configuration
```

### Test Coverage by Page

#### Tenant Settings (12 tests)
- ✅ Page load with all sections
- ✅ Loading state handling
- ✅ Required field validation
- ✅ Email format validation
- ✅ Company information updates
- ✅ Contact details updates
- ✅ Timezone and locale changes
- ✅ Cancel/reset functionality
- ✅ Read-only subscription display
- ✅ Form field visibility
- ✅ Saving state display

**Example Test:**
```typescript
test('should validate required fields', async ({ page }) => {
  await page.waitForLoadState('networkidle');
  const companyNameInput = page.getByLabel(/Company Name/);
  await companyNameInput.clear();
  await page.getByRole('button', { name: /Save Changes/ }).click();
  await expect(page.getByText(/required/i)).toBeVisible();
});
```

#### Shop Management (18 tests)
- ✅ Table display with all columns
- ✅ Search functionality
- ✅ Status filtering
- ✅ Create shop dialog
- ✅ Form validation (required fields, email)
- ✅ Shop creation
- ✅ Shop editing
- ✅ Activate/deactivate shops
- ✅ Cancel operations
- ✅ Empty state handling
- ✅ Shop count display
- ✅ Contact/location information
- ✅ Loading state handling

**Example Test:**
```typescript
test('should create a new shop', async ({ page }) => {
  await page.waitForLoadState('networkidle');
  await page.getByRole('button', { name: /Add Shop/ }).click();
  await page.getByLabel(/Shop Name.*\*/).fill('New Test Shop');
  await page.getByLabel(/Shop Email.*\*/).fill('newshop@test.com');
  await page.getByRole('button', { name: 'Create Shop' }).click();
  await expect(page.getByText(/created successfully/i)).toBeVisible({ timeout: 5000 });
});
```

#### Audit Logs (20 tests)
- ✅ Table display with all columns
- ✅ Filter section visibility
- ✅ Search functionality
- ✅ Action type filtering
- ✅ Entity type filtering
- ✅ Date range filtering
- ✅ Combined multi-filter support
- ✅ Export to CSV functionality
- ✅ Action badges with colors
- ✅ User information display
- ✅ IP address display
- ✅ Entity details with icons
- ✅ Log details display
- ✅ Pagination controls
- ✅ Page navigation
- ✅ Empty state handling
- ✅ Export button disable state
- ✅ Log count display
- ✅ Loading state handling
- ✅ Filter reset functionality

**Example Test:**
```typescript
test('should filter by action type', async ({ page }) => {
  await page.waitForLoadState('networkidle');
  await page.getByLabel('Action').click();
  await page.getByText('CREATE', { exact: true }).click();
  await page.waitForTimeout(500);
  const createBadges = page.locator('text=Create');
  const count = await createBadges.count();
  expect(count).toBeGreaterThan(0);
});
```

### Playwright Configuration

**Key Settings:**
- **Base URL**: `http://localhost:3001`
- **Test Directory**: `./e2e`
- **Parallel Execution**: Enabled
- **Retries**: 0 (local), 2 (CI)
- **Screenshots**: On failure only
- **Videos**: On failure only
- **Trace**: On first retry

**Browser Support:**
- Chromium (primary)
- Firefox (optional, commented out)
- WebKit (optional, commented out)

**Dev Server Auto-Start:**
The config automatically starts the Vite dev server before running tests:
```typescript
webServer: {
  command: 'npm run dev',
  url: 'http://localhost:3001',
  reuseExistingServer: !process.env.CI,
  timeout: 120000,
}
```

## Load Testing with k6

### Prerequisites

- k6 installed: https://k6.io/docs/getting-started/installation/
- Backend API running on `http://localhost:8081`

### Running Load Tests

```bash
cd k6-tests

# Smoke test (quick check with 1 user for 30s)
k6 run --vus 1 --duration 30s cloud-api-load-test.js

# Load test (moderate load with 10 users for 2m)
k6 run --vus 10 --duration 2m cloud-api-load-test.js

# Stress test (high load with 50 users for 5m)
k6 run --vus 50 --duration 5m cloud-api-load-test.js

# Spike test (sudden traffic surge)
k6 run --stage 30s:10 --stage 1m:100 --stage 30s:10 cloud-api-load-test.js

# Use custom base URL
k6 run -e BASE_URL=https://api.shopmanager.com cloud-api-load-test.js
```

### Load Test Configuration

**Staged Load Profile (Default):**
1. Ramp up to 10 users over 30s
2. Stay at 10 users for 1 minute
3. Ramp up to 20 users over 30s
4. Stay at 20 users for 1 minute
5. Ramp down to 0 users over 30s

**Performance Thresholds (SLA):**
- ✅ 95th percentile response time < 500ms
- ✅ Error rate < 1%
- ✅ Custom error rate < 5%

### API Endpoints Tested

| Endpoint | Expected Response | Target Time |
|----------|------------------|-------------|
| `/api/cloud/tenants/{id}/api-keys` | 200 or 404 | < 300ms |
| `/api/cloud/tenants/{id}/analytics` | 200 or 404 | < 500ms |
| `/api/cloud/tenants/{id}/analytics/sync-status` | 200 or 404 | < 200ms |
| `/api/cloud/subscriptions/{id}` | 200 or 404 | < 200ms |
| `/api/cloud/billing/{id}/invoices` | 200 or 404 | < 300ms |

### Interpreting k6 Results

**Example Output:**
```
✓ List API Keys: status is 200 or 404
✓ List API Keys: response time < 300ms

checks.........................: 100.00% ✓ 1200      ✗ 0
data_received..................: 2.4 MB  8.0 kB/s
data_sent......................: 450 kB  1.5 kB/s
http_req_blocked...............: avg=1.2ms    p(95)=3.4ms
http_req_duration..............: avg=245ms    p(95)=450ms
http_req_failed................: 0.00%   ✓ 0        ✗ 1200
http_reqs......................: 1200    4/s
vus............................: 20      min=0       max=20
```

**Key Metrics:**
- **checks**: Percentage of successful assertions
- **http_req_duration**: Response time distribution
- **http_req_failed**: Error rate
- **http_reqs**: Total requests made
- **vus**: Virtual users (concurrent load)

## Test Coverage

### Overall Statistics

**E2E Tests:**
- 3 test suites
- 50 total test scenarios
- 691 lines of test code

**Load Tests:**
- 5 API endpoints
- 189 lines of test code
- Multiple load profiles supported

### Test Patterns Used

**Accessibility-First Testing:**
- Uses semantic selectors (`getByRole`, `getByLabel`, `getByText`)
- Ensures UI is accessible to screen readers and assistive technologies

**Realistic User Flows:**
- Simulates actual user interactions with delays
- Verifies success messages and state changes
- Tests error handling and validation

**Performance Monitoring:**
- Response time thresholds for SLA compliance
- Error rate tracking
- Custom metrics for business logic validation

## CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'

      - name: Install dependencies
        working-directory: frontend
        run: npm ci

      - name: Install Playwright browsers
        working-directory: frontend
        run: npx playwright install --with-deps chromium

      - name: Run E2E tests
        working-directory: frontend
        run: npm run test:e2e

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: frontend/playwright-report/
```

### k6 Cloud Integration

For production load testing, consider using k6 Cloud:

```bash
# Login to k6 Cloud
k6 login cloud

# Run test and stream results to k6 Cloud
k6 cloud k6-tests/cloud-api-load-test.js
```

## Best Practices

### E2E Testing

1. **Use Semantic Selectors**: Prefer `getByRole`, `getByLabel` over CSS selectors
2. **Wait for Network Idle**: Use `waitForLoadState('networkidle')` before assertions
3. **Test User Flows**: Focus on real user scenarios, not implementation details
4. **Mock External Dependencies**: Use MSW or similar for API mocking
5. **Keep Tests Independent**: Each test should set up its own state
6. **Use Descriptive Test Names**: Clearly state what is being tested

### Load Testing

1. **Start Small**: Begin with smoke tests before running full load tests
2. **Monitor Server Resources**: Watch CPU, memory, database connections
3. **Test Realistic Scenarios**: Use production-like data and user patterns
4. **Set Meaningful Thresholds**: Base on actual SLA requirements
5. **Ramp Up Gradually**: Avoid sudden spikes unless testing spike scenarios
6. **Clean Up Test Data**: Ensure tests don't pollute production databases

## Troubleshooting

### E2E Tests Failing

**Problem**: Tests timeout waiting for elements
**Solution**: Check that dev server is running, increase timeout in `playwright.config.ts`

**Problem**: Tests fail intermittently
**Solution**: Add proper wait conditions, avoid hardcoded timeouts, use `waitForLoadState`

**Problem**: Screenshots not captured
**Solution**: Ensure `screenshot: 'only-on-failure'` is set in config

### Load Tests Failing

**Problem**: Connection refused errors
**Solution**: Verify backend is running on correct port, check BASE_URL

**Problem**: Thresholds failing
**Solution**: Check server resources, optimize slow endpoints, adjust thresholds

**Problem**: Authentication errors
**Solution**: Update API_KEY in test file, implement proper auth flow

## Additional Resources

- [Playwright Documentation](https://playwright.dev/)
- [k6 Documentation](https://k6.io/docs/)
- [Main Testing Guide](../TESTING-GUIDE.md) - Unit and integration tests
- [Frontend README](./README.md) - Frontend setup and development

## Support

For issues or questions:
1. Check existing test examples in `e2e/` directory
2. Review Playwright/k6 documentation
3. Create an issue in the project repository
