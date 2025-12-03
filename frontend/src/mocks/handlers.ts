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

export const handlers = [
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
  }),

  // User Profile
  http.get('*/api/users/profile', () => {
    return HttpResponse.json(getMockShopOwner())
  })
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
