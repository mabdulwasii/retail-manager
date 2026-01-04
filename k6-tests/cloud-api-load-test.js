import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

/**
 * k6 Load Test for Cloud APIs
 *
 * Test Scenarios:
 * - API Key Management endpoints
 * - Subscription Management endpoints
 * - Analytics endpoints
 * - Tenant and Shop operations
 *
 * Usage:
 *   # Smoke test (quick check)
 *   k6 run --vus 1 --duration 30s cloud-api-load-test.js
 *
 *   # Load test (moderate load)
 *   k6 run --vus 10 --duration 2m cloud-api-load-test.js
 *
 *   # Stress test (high load)
 *   k6 run --vus 50 --duration 5m cloud-api-load-test.js
 *
 *   # Spike test (sudden traffic surge)
 *   k6 run --stage 30s:10 --stage 1m:100 --stage 30s:10 cloud-api-load-test.js
 */

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users
    { duration: '1m', target: 10 },   // Stay at 10 users for 1 minute
    { duration: '30s', target: 20 },  // Ramp up to 20 users
    { duration: '1m', target: 20 },   // Stay at 20 users
    { duration: '30s', target: 0 },   // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.01'],   // Error rate should be less than 1%
    errors: ['rate<0.05'],            // Custom error rate should be less than 5%
  },
};

// Base URL - adjust as needed
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

// Test data
const TENANT_ID = 'test-tenant-id';
const API_KEY = 'test-api-key-placeholder'; // In real scenario, would authenticate first

// Headers
const headers = {
  'Content-Type': 'application/json',
  'X-API-Key': API_KEY,
};

/**
 * Main test function - executed by each virtual user
 */
export default function () {
  // Test 1: List API Keys
  testListApiKeys();

  // Test 2: Get Tenant Analytics
  testGetTenantAnalytics();

  // Test 3: Get Shop Sync Status
  testGetShopSyncStatus();

  // Test 4: Get Current Subscription
  testGetCurrentSubscription();

  // Test 5: Get Billing Invoices
  testGetBillingInvoices();

  // Sleep between iterations (1-3 seconds)
  sleep(Math.random() * 2 + 1);
}

/**
 * Test: List API Keys
 */
function testListApiKeys() {
  const url = `${BASE_URL}/api/cloud/tenants/${TENANT_ID}/api-keys`;

  const res = http.get(url, { headers });

  const success = check(res, {
    'List API Keys: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'List API Keys: response time < 300ms': (r) => r.timings.duration < 300,
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * Test: Get Tenant Analytics
 */
function testGetTenantAnalytics() {
  const periodStart = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString();
  const periodEnd = new Date().toISOString();

  const url = `${BASE_URL}/api/cloud/tenants/${TENANT_ID}/analytics?periodStart=${periodStart}&periodEnd=${periodEnd}`;

  const res = http.get(url, { headers });

  const success = check(res, {
    'Get Analytics: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'Get Analytics: response time < 500ms': (r) => r.timings.duration < 500,
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * Test: Get Shop Sync Status
 */
function testGetShopSyncStatus() {
  const url = `${BASE_URL}/api/cloud/tenants/${TENANT_ID}/analytics/sync-status`;

  const res = http.get(url, { headers });

  const success = check(res, {
    'Get Sync Status: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'Get Sync Status: response time < 200ms': (r) => r.timings.duration < 200,
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * Test: Get Current Subscription
 */
function testGetCurrentSubscription() {
  const url = `${BASE_URL}/api/cloud/subscriptions/${TENANT_ID}`;

  const res = http.get(url, { headers });

  const success = check(res, {
    'Get Subscription: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'Get Subscription: response time < 200ms': (r) => r.timings.duration < 200,
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * Test: Get Billing Invoices
 */
function testGetBillingInvoices() {
  const url = `${BASE_URL}/api/cloud/billing/${TENANT_ID}/invoices`;

  const res = http.get(url, { headers });

  const success = check(res, {
    'Get Invoices: status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    'Get Invoices: response time < 300ms': (r) => r.timings.duration < 300,
  });

  if (!success) {
    errorRate.add(1);
  }
}

/**
 * Setup function - runs once at the beginning
 */
export function setup() {
  console.log('Starting k6 load test for Cloud APIs...');
  console.log(`Base URL: ${BASE_URL}`);
  console.log(`Target tenant: ${TENANT_ID}`);
}

/**
 * Teardown function - runs once at the end
 */
export function teardown(data) {
  console.log('Load test completed!');
}
