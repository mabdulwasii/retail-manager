/**
 * Test Data: Investments
 * Mock investment data for testing
 */

export const getMockInvestment = (overrides = {}) => ({
  id: 'inv1',
  amount: 100000,
  totalProfitEarned: 15000,
  totalWithdrawn: 0,
  availableBalance: 15000,
  status: 'ACTIVE',
  investmentDate: new Date('2024-01-15').toISOString(),
  investmentType: 'EQUITY_INVESTMENT',
  shopName: 'Electronics Store',
  shopId: 'shop1',
  startDate: new Date('2024-01-01').toISOString(),
  maturityDate: new Date('2025-01-01').toISOString(),
  profitSharingModel: 'PERCENTAGE',
  profitPercentage: 20,
  roi: 15.0,
  ...overrides
})

export const getMockInvestmentsList = () => ({
  content: [
    getMockInvestment(),
    getMockInvestment({
      id: 'inv2',
      amount: 50000,
      totalProfitEarned: 7500,
      availableBalance: 7500,
      investmentType: 'LOAN',
      shopName: 'Fashion Boutique',
      shopId: 'shop2',
      roi: 12.5
    })
  ],
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 20
})

export const getMockDistribution = (overrides = {}) => ({
  id: 'dist1',
  amount: 5000,
  distributionDate: new Date('2024-03-01').toISOString(),
  status: 'COMPLETED',
  investmentId: 'inv1',
  type: 'PROFIT',
  description: 'Q1 Profit Distribution',
  paymentMethod: 'BANK_TRANSFER',
  transactionReference: 'TXN123456',
  ...overrides
})

export const getMockDistributionsList = () => ({
  content: [
    getMockDistribution(),
    getMockDistribution({
      id: 'dist2',
      amount: 3000,
      distributionDate: new Date('2024-02-01').toISOString(),
      description: 'February Distribution',
      transactionReference: 'TXN123457'
    })
  ],
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 20
})
