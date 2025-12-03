/**
 * Test Data: Dashboard Analytics
 * Mock dashboard and analytics data for testing
 */

export const getMockDashboardStats = () => ({
  totalRevenue: 150000,
  totalShops: 5,
  totalProducts: 1200,
  totalSales: 450,
  investmentROI: 25.5,
  activeUsers: 125,
  systemHealth: 99.9,
  revenueGrowth: 12.5
})

export const getMockSalesSummary = () => ({
  totalRevenue: 150000,
  totalTransactions: 200,
  averageTransactionValue: 750,
  topProducts: [
    { id: '1', name: 'Product A', sales: 50 },
    { id: '2', name: 'Product B', sales: 40 }
  ]
})

export const getMockInventorySummary = (shopId = 'shop1') => ({
  totalItems: 300,
  lowStockItems: 10,
  expiredItems: 2,
  expiringSoonItems: 5,
  totalValue: 2000000,
  shopId
})

export const getMockInvestmentROI = () => ({
  totalInvestment: 500000,
  currentValue: 625000,
  roi: 25,
  profitLoss: 125000
})

export const getMockRevenueAnalytics = () => ({
  currentRevenue: 150000,
  previousRevenue: 120000,
  growthRate: 25,
  trend: 'up' as const
})

export const getMockExpenseSummary = () => ({
  totalExpenses: 50,
  totalAmount: 50000,
  pendingApproval: 5,
  approvedExpenses: 45,
  monthlyTotal: 50000
})

export const getMockFraudStatistics = () => ({
  totalCases: 5,
  resolvedCases: 3,
  pendingCases: 2,
  flaggedTransactions: 10
})

export const getMockAlerts = () => [
  {
    id: '1',
    type: 'warning',
    message: 'Unusual transaction pattern detected in Electronics department',
    timestamp: new Date().toISOString(),
    severity: 'medium'
  },
  {
    id: '2',
    type: 'error',
    message: 'Failed payment processing attempt',
    timestamp: new Date().toISOString(),
    severity: 'high'
  }
]

// Complete dashboard data for owner/manager
export const getMockOwnerDashboardData = () => ({
  salesSummary: getMockSalesSummary(),
  inventorySummary: getMockInventorySummary(),
  investmentROI: getMockInvestmentROI(),
  revenueAnalytics: getMockRevenueAnalytics(),
  expenseSummary: getMockExpenseSummary(),
  fraudStatistics: getMockFraudStatistics()
})
