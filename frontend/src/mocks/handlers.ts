import { http, HttpResponse } from 'msw'
import {
  getMockInvestment,
  getMockInvestmentsList,
  getMockDistributionsList,
  getMockDashboardStats,
  getMockShopsPerformance,
  getMockActiveShops,
  getMockShopsList,
  getMockSalesSummary,
  getMockInvestmentROI,
  getMockRevenueAnalytics,
  getMockFraudStatistics,
  getMockInventorySummary,
  getMockExpenseSummary,
  getMockAlerts,
  getMockShopOwner
} from '@/testData'

/**
 * MSW Request Handlers
 * Define mock API responses for testing
 * Uses centralized test data factories for consistency
 */

const API_BASE_URL = 'http://localhost:8081/api';

export const handlers = [
  // ============================================================================
  // AUTH ENDPOINTS
  // ============================================================================

  // Login - successful
  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = await request.json() as { username: string; password: string };

    // Simulate invalid credentials
    if (body.username === 'wronguser' || body.password === 'wrongpass') {
      return HttpResponse.json(
        { message: 'Invalid credentials' },
        { status: 401 }
      );
    }

    // Simulate empty credentials
    if (!body.username || !body.password) {
      return HttpResponse.json(
        { message: 'Username and password are required' },
        { status: 400 }
      );
    }

    // Successful login
    return HttpResponse.json({
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTksInVzZXJuYW1lIjoidGVzdHVzZXIiLCJpZCI6IjEyMyIsInBlcm1pc3Npb25zIjpbIlBST0RVQ1RfUkVBRCIsIlBST0RVQ1RfV1JJVEUiXX0.C9pGXvBHfHdJsYdRfPOmfZpFw7xO7l8YxPwCqYqXzTM',
      refreshToken: 'refresh-token-123',
    });
  }),

  // Register - successful
  http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
    const body = await request.json() as { username: string; email: string; password: string };

    // Simulate duplicate username
    if (body.username === 'existinguser') {
      return HttpResponse.json(
        { message: 'Username already exists' },
        { status: 409 }
      );
    }

    // Successful registration - return tokens
    return HttpResponse.json({
      accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI0NTYiLCJuYW1lIjoidGVzdHVzZXIiLCJpYXQiOjE1MTYyMzkwMjIsImV4cCI6OTk5OTk5OTk5OSwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImlkIjoiNDU2IiwicGVybWlzc2lvbnMiOlsiUFJPRFVDVF9SRUFEIiwiUFJPRFVDVF9XUklURSJdfQ.abc123def456',
      refreshToken: 'refresh-token-456',
    }, { status: 201 });
  }),

  // Get Profile (Auth-specific)
  http.get(`${API_BASE_URL}/users/profile`, ({ request }) => {
    const authHeader = request.headers.get('Authorization');

    // Check for valid token
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      );
    }

    return HttpResponse.json({
      id: '123',
      username: 'testuser',
      email: 'test@example.com',
      roles: [
        {
          id: '1',
          name: 'USER',
          description: 'User role',
          isSystem: false,
          permissions: ['USER_READ', 'USER_WRITE']
        },
        {
          id: '2',
          name: 'ADMIN',
          description: 'Admin role',
          isSystem: true,
          permissions: ['ADMIN_READ', 'SYSTEM_ADMIN']
        }
      ]
    });
  }),

  // Refresh Token
  http.post(`${API_BASE_URL}/auth/refresh`, async ({ request }) => {
    const body = await request.json() as { refreshToken: string };

    // Simulate invalid refresh token
    if (body.refreshToken === 'invalid-refresh-token') {
      return HttpResponse.json(
        { message: 'Invalid refresh token' },
        { status: 401 }
      );
    }

    return HttpResponse.json({
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token',
    });
  }),

  // Logout
  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' });
  }),

  // Check Permissions
  http.post(`${API_BASE_URL}/auth/check-permissions`, async ({ request }) => {
    const body = await request.json() as { permissions: string[] };
    const authHeader = request.headers.get('Authorization');

    if (!authHeader) {
      return HttpResponse.json(
        { message: 'Unauthorized' },
        { status: 401 }
      );
    }

    // Mock user has PRODUCT_READ and PRODUCT_WRITE
    const userPermissions = ['PRODUCT_READ', 'PRODUCT_WRITE'];
    const hasPermission = body.permissions.every(p => userPermissions.includes(p));

    return HttpResponse.json({ hasPermission });
  }),

  // ============================================================================
  // ANALYTICS & INVESTMENT ENDPOINTS
  // ============================================================================

  // My Investments (investor's own investments)
  // Match both absolute and relative URLs
  http.get('*/api/my-investments', () => {
    return HttpResponse.json({
      content: [getMockInvestment()],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20
    })
  }),

  // Shop Investments (for shop owners)
  http.get('*/api/shops/:shopId/investments', () => {
    return HttpResponse.json({
      content: [
        getMockInvestment({
          id: 'inv2',
          amount: 50000,
          totalProfitEarned: 7500,
          availableBalance: 7500,
          investmentType: 'LOAN',
          shopName: 'Fashion Boutique',
          shopId: 'shop2',
          roi: 15.0
        })
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 20
    })
  }),

  // My Distributions
  http.get('*/api/my-distributions', () => {
    return HttpResponse.json(getMockDistributionsList())
  }),

  // Dashboard Stats
  http.get('*/api/analytics/dashboard-stats', () => {
    return HttpResponse.json(getMockDashboardStats())
  }),

  // Shop Performance
  http.get('*/api/analytics/shop-performance', () => {
    return HttpResponse.json(getMockShopsPerformance())
  }),

  // Active Shops
  http.get('*/api/shops/active', () => {
    return HttpResponse.json(getMockActiveShops())
  }),

  // Shops List (Paginated)
  http.get('*/api/shops', () => {
    return HttpResponse.json(getMockShopsList())
  }),

  // Sales Summary
  http.get('*/api/analytics/sales-summary', () => {
    return HttpResponse.json(getMockSalesSummary())
  }),

  // Investment ROI
  http.get('*/api/analytics/investment-roi', () => {
    return HttpResponse.json(getMockInvestmentROI())
  }),

  // Revenue Analytics
  http.get('*/api/analytics/revenue-analytics', () => {
    return HttpResponse.json(getMockRevenueAnalytics())
  }),

  // Fraud Statistics
  http.get('*/api/analytics/fraud-statistics', () => {
    return HttpResponse.json(getMockFraudStatistics())
  }),

  // Inventory Summary
  http.get('*/api/inventory/summary/:shopId', ({ params }) => {
    return HttpResponse.json(getMockInventorySummary(params.shopId as string))
  }),

  // Expense Summary
  http.get('*/api/expenses/summary', () => {
    return HttpResponse.json(getMockExpenseSummary())
  }),

  // Alerts
  http.get('*/api/alerts/active', () => {
    return HttpResponse.json(getMockAlerts())
  })

  // Note: User Profile endpoint is defined in AUTH ENDPOINTS section above
]

// Error handlers for testing error scenarios
export const errorHandlers = {
  networkError: http.get('*/api/*', () => {
    return HttpResponse.error()
  }),

  serverError: http.get('*/api/*', () => {
    return HttpResponse.json(
      { message: 'Internal server error' },
      { status: 500 }
    )
  }),

  notFound: http.get('*/api/*', () => {
    return HttpResponse.json(
      { message: 'Resource not found' },
      { status: 404 }
    )
  }),

  unauthorized: http.get('*/api/*', () => {
    return HttpResponse.json(
      { message: 'Unauthorized' },
      { status: 401 }
    )
  })
}
