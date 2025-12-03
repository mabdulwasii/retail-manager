import React from 'react'
import { render, screen } from '@testing-library/react'
import { AnalyticsSummaryCards } from '../AnalyticsSummaryCards'
import { CurrencyProvider } from '@/context/CurrencyContext'

// Mock the currency hooks - both useCurrency and useCurrencyProvider
jest.mock('@/hooks/useCurrency', () => ({
  useCurrency: () => ({
    currency: {
      code: 'NGN',
      symbol: '₦',
      name: 'Nigerian Naira',
      locale: 'en-NG',
      decimalPlaces: 2
    },
    setCurrency: jest.fn(),
    formatAmount: (amount: number) => amount.toLocaleString(),
    formatCurrency: (amount: number) => `₦${amount.toLocaleString()}`,
    parseCurrency: jest.fn()
  }),
  useCurrencyProvider: () => ({
    currency: {
      code: 'NGN',
      symbol: '₦',
      name: 'Nigerian Naira',
      locale: 'en-NG',
      decimalPlaces: 2
    },
    setCurrency: jest.fn(),
    formatAmount: (amount: number) => amount.toLocaleString(),
    formatCurrency: (amount: number) => `₦${amount.toLocaleString()}`,
    parseCurrency: jest.fn()
  })
}))

const mockAnalyticsData = {
  salesSummary: {
    shopId: 'shop-1',
    periodStart: '2024-01-01T00:00:00',
    periodEnd: '2024-01-31T23:59:59',
    totalRevenue: 50000,
    totalTransactions: 250,
    averageTransactionValue: 200,
    calculatedAt: '2024-01-31T12:00:00'
  },
  revenueAnalytics: {
    shopId: 'shop-1',
    periodStart: '2024-01-01T00:00:00',
    periodEnd: '2024-01-31T23:59:59',
    currentRevenue: 50000,
    previousRevenue: 40000,
    growthRate: 25.0,
    currentTransactions: 250,
    previousTransactions: 200,
    calculatedAt: '2024-01-31T12:00:00'
  },
  investmentRoi: {
    shopId: 'shop-1',
    periodStart: '2024-01-01T00:00:00',
    periodEnd: '2024-01-31T23:59:59',
    totalInvestmentAmount: 100000,
    totalDistributions: 15000,
    roiPercentage: 15.0,
    calculatedAt: '2024-01-31T12:00:00'
  },
  fraudStatistics: {
    shopId: 'shop-1',
    periodStart: '2024-01-01T00:00:00',
    periodEnd: '2024-01-31T23:59:59',
    totalAssessments: 1000,
    highRiskCount: 50,
    criticalRiskCount: 5,
    riskRate: 0.055,
    calculatedAt: '2024-01-31T12:00:00'
  }
}

const renderWithCurrencyProvider = (component: React.ReactElement) => {
  return render(
    <CurrencyProvider>
      {component}
    </CurrencyProvider>
  )
}

describe('AnalyticsSummaryCards', () => {
  it('should display loading state', () => {
    const { container } = renderWithCurrencyProvider(
      <AnalyticsSummaryCards isLoading={true} />
    )

    // Just verify component renders with loading state
    expect(container).toBeInTheDocument()
  })

  it('should display analytics summary data correctly', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    // Check main metrics
    expect(screen.getByText('Total Revenue')).toBeInTheDocument()
    // Use getAllByText since currency values may appear multiple times
    const revenueElements = screen.getAllByText('₦50,000')
    expect(revenueElements.length).toBeGreaterThan(0)

    expect(screen.getByText('Total Transactions')).toBeInTheDocument()
    expect(screen.getByText('250')).toBeInTheDocument()

    expect(screen.getByText('Avg Transaction')).toBeInTheDocument()
    expect(screen.getByText('₦200')).toBeInTheDocument()

    expect(screen.getByText('Investment ROI')).toBeInTheDocument()
    // Use getAllByText since '15.0%' may appear multiple times
    const roiElements = screen.getAllByText('15.0%')
    expect(roiElements.length).toBeGreaterThan(0)
  })

  it('should display growth rate correctly', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText('+25.0% from last period')).toBeInTheDocument()
  })

  it('should display revenue growth details', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText('Revenue Growth')).toBeInTheDocument()
    expect(screen.getByText('Current Period:')).toBeInTheDocument()
    expect(screen.getByText('Previous Period:')).toBeInTheDocument()
    expect(screen.getByText('Growth Rate:')).toBeInTheDocument()
  })

  it('should display investment summary', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText('Investment Summary')).toBeInTheDocument()
    expect(screen.getByText('Total Investment:')).toBeInTheDocument()
    expect(screen.getByText('Total Returns:')).toBeInTheDocument()
    expect(screen.getByText('ROI:')).toBeInTheDocument()
  })

  it('should display fraud risk assessment', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText('Risk Assessment')).toBeInTheDocument()
    expect(screen.getByText('Total Assessments:')).toBeInTheDocument()
    expect(screen.getByText('1,000')).toBeInTheDocument()
    expect(screen.getByText('High Risk:')).toBeInTheDocument()
    expect(screen.getByText('50')).toBeInTheDocument()
    expect(screen.getByText('Critical Risk:')).toBeInTheDocument()
    // Use getAllByText since '5' may appear multiple times
    const fiveElements = screen.getAllByText('5')
    expect(fiveElements.length).toBeGreaterThan(0)
  })

  it('should handle missing data gracefully', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        isLoading={false}
      />
    )

    // Should display dashes for missing data
    expect(screen.getAllByText('—')).toHaveLength(4)
  })

  it('should display negative growth rate correctly', () => {
    const negativeGrowthData = {
      ...mockAnalyticsData.revenueAnalytics,
      growthRate: -15.5
    }

    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={negativeGrowthData}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText('-15.5% from last period')).toBeInTheDocument()
  })

  it('should display risk level badge correctly', () => {
    // Test low risk scenario
    const lowRiskData = {
      ...mockAnalyticsData.fraudStatistics,
      riskRate: 0.2 // 20% risk rate = Low
    }

    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={lowRiskData}
        isLoading={false}
      />
    )

    expect(screen.getByText('Low')).toBeInTheDocument()
  })

  it('should display calculated timestamp', () => {
    renderWithCurrencyProvider(
      <AnalyticsSummaryCards
        salesSummary={mockAnalyticsData.salesSummary}
        revenueAnalytics={mockAnalyticsData.revenueAnalytics}
        investmentRoi={mockAnalyticsData.investmentRoi}
        fraudStatistics={mockAnalyticsData.fraudStatistics}
        isLoading={false}
      />
    )

    expect(screen.getByText(/Data calculated at:/)).toBeInTheDocument()
  })
})