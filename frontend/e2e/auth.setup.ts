import { test as setup, expect, chromium } from '@playwright/test';

const authFile = 'playwright/.auth/user.json';

/**
 * Global setup for E2E tests - handles authentication
 * This setup runs once before all tests and stores authentication state
 */
setup('authenticate', async ({ request, baseURL }) => {
  // Login via API
  const loginResponse = await request.post(`http://localhost:8081/api/auth/login`, {
    data: {
      username: 'superadmin',
      password: 'changeme',
    },
    headers: {
      'Content-Type': 'application/json'
    }
  });

  expect(loginResponse.ok()).toBeTruthy();

  const responseBody = await loginResponse.json();
  const accessToken = responseBody.accessToken;

  // Store the authentication token in localStorage via a page context
  // This approach works better than cookies for JWT tokens
  const browser = await chromium.launch();
  const context = await browser.newContext({ baseURL });
  const page = await context.newPage();

  // Navigate to the app and inject the token into localStorage
  await page.goto('/');

  // Set the token in localStorage using the same key as EmbeddedAuthService
  await page.evaluate((token) => {
    localStorage.setItem('embedded_access_token', token);
    localStorage.setItem('embedded_refresh_token', token); // Using access token as refresh token for testing
  }, accessToken);

  // Save the storage state
  await context.storageState({ path: authFile });
  await browser.close();
});
