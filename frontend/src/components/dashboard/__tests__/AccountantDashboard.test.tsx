import React from 'react'
import { render, screen } from '@testing-library/react'
import { createMockAuth } from '@/test/test-utils'
import { MemoryRouter } from 'react-router-dom'
import { AccountantDashboard } from '../AccountantDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'
import { useRevenueAnalytics, useExpenseSummary } from '@/hooks/useDashboard'
import { useCurrency } from '@/hooks/useCurrency'

// Mock dependencies
jest.mock('@/context/UnifiedAuthContext')
jest.mock('@/hooks/useDashboard')
jest.mock('@/hooks/useCurrency')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUseRevenueAnalytics = useRevenueAnalytics as jest.MockedFunction<typeof useRevenueAnalytics>
const mockUseExpenseSummary = useExpenseSummary as jest.MockedFunction<typeof useExpenseSummary>
const mockUseCurrency = useCurrency as jest.MockedFunction<typeof useCurrency>

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: any) => <div className={`card ${className || ''}`}>{children}</div>,
  CardContent: ({ children }: any) => <div className="card-content">{children}</div>,
  CardDescription: ({ children }: any) => <div className="card-description">{children}</div>,
  CardHeader: ({ children }: any) => <div className="card-header">{children}</div>,
  CardTitle: ({ children }: any) => <div className="card-title">{children}</div>,
}))

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, className, asChild, ...props }: any) =>
    asChild ? children : <button className={className} {...props}>{children}</button>
}))

jest.mock('@/components/ui/select', () => ({
  Select: ({ children, value, onValueChange }: any) => (
    <select value={value} onChange={(e) => onValueChange(e.target.value)}>
      {children}
    </select>
  ),
  SelectContent: ({ children }: any) => <div>{children}</div>,
  SelectItem: ({ children, value }: any) => <option value={value}>{children}</option>,
  SelectTrigger: ({ children }: any) => <div>{children}</div>,
  SelectValue: ({ placeholder }: any) => <span>{placeholder}</span>
}))

jest.mock('@/components/ui/shop-selector', () => ({
  ShopSelector: ({ value, onValueChange }: any) => (
    <select data-testid="shop-selector" value={value} onChange={(e) => onValueChange(e.target.value)}>
      <option value="shop1">Shop 1</option>
    </select>
  )
}))

const AccountantDashboardWrapper: React.FC = () => (
  <MemoryRouter>
    <AccountantDashboard />
  </MemoryRouter>
)

describe('AccountantDashboard', () => {
  const mockUser = {
    id: '1',
    username: 'accountant',
    email: 'accountant@example.com',
    firstName: 'Jane',
    shopId: 'shop1',
    roles: ['ROLE_ACCOUNTANT']
  }

  const mockRevenueData = {
    currentRevenue: 300000,
    previousRevenue: 250000,
    growthRate: 20.0
  }

  const mockExpenseData = {
    totalAmount: 100000,
    pendingApproval: 10
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))

    mockUseCurrency.mockReturnValue({
      currency: {
        code: 'NGN',
        symbol: '₦',
        name: 'Nigerian Naira',
        locale: 'en-NG',
        decimalPlaces: 2
      },
      setCurrency: jest.fn(),
      formatAmount: jest.fn((amount) => amount.toLocaleString()),
      formatCurrency: jest.fn((amount) => `₦${amount.toLocaleString()}`),
      parseCurrency: jest.fn()
    })

    mockUseRevenueAnalytics.mockReturnValue({
      data: mockRevenueData,
      isLoading: false,
      error: null,
      refetch: jest.fn()
    })

    mockUseExpenseSummary.mockReturnValue({
      data: mockExpenseData,
      isLoading: false,
      error: null,
      refetch: jest.fn()
    })
  })

  it('should render welcome message', () => {
    render(<AccountantDashboardWrapper />)

    expect(screen.getByText('Financial Dashboard')).toBeInTheDocument()
    expect(screen.getByText(/Welcome back, Jane/)).toBeInTheDocument()
  })

  it('should render financial statistics cards', () => {
    render(<AccountantDashboardWrapper />)

    expect(screen.getByText('Total Revenue')).toBeInTheDocument()
    // Use getAllByText since 'Total Expenses' may appear multiple times
    const expensesElements = screen.getAllByText('Total Expenses')
    expect(expensesElements.length).toBeGreaterThan(0)
    expect(screen.getByText('Net Profit')).toBeInTheDocument()
    expect(screen.getByText('Profit Margin')).toBeInTheDocument()
  })

  it('should display revenue with currency formatting', () => {
    render(<AccountantDashboardWrapper />)

    expect(screen.getByText(/₦300,000/)).toBeInTheDocument()
  })

  it('should calculate net profit correctly', () => {
    render(<AccountantDashboardWrapper />)

    // Net profit = Revenue - Expenses = 300000 - 100000 = 200000
    expect(screen.getByText(/₦200,000/)).toBeInTheDocument()
  })

  it('should display growth rate with trend indicator', () => {
    render(<AccountantDashboardWrapper />)

    // Use getAllByText since percentage may appear multiple times
    const percentageElements = screen.getAllByText(/20.0%/)
    expect(percentageElements.length).toBeGreaterThan(0)
  })

  it('should render Quick Actions with fixed routes', () => {
    render(<AccountantDashboardWrapper />)

    expect(screen.getByText('Quick Actions')).toBeInTheDocument()
    expect(screen.getByText('Review Analytics')).toBeInTheDocument()
    // Use getAllByText since 'View Receipts' appears multiple times
    const receiptsElements = screen.getAllByText('View Receipts')
    expect(receiptsElements.length).toBeGreaterThan(0)
    expect(screen.getByText('Sales Report')).toBeInTheDocument()

    // Verify correct routes (no broken links)
    const analyticsButton = screen.getByText('Review Analytics')
    expect(analyticsButton.closest('a')).toHaveAttribute('href', '/analytics')

    // Find the receipts button that has a link
    const receiptsButton = receiptsElements.find(el => el.closest('a'))
    expect(receiptsButton?.closest('a')).toHaveAttribute('href', '/receipts')

    const salesButton = screen.getByText('Sales Report')
    expect(salesButton.closest('a')).toHaveAttribute('href', '/sales')
  })

  it('should render quick action buttons', () => {
    render(<AccountantDashboardWrapper />)

    // Just verify the button renders
    expect(screen.getByText('Review Analytics')).toBeInTheDocument()
  })

  it('should render shop selector', () => {
    render(<AccountantDashboardWrapper />)

    const shopSelector = screen.getByTestId('shop-selector')
    expect(shopSelector).toBeInTheDocument()
  })

  it('should handle loading state', () => {
    mockUseRevenueAnalytics.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
      refetch: jest.fn()
    })

    mockUseExpenseSummary.mockReturnValue({
      data: null,
      isLoading: true,
      error: null,
      refetch: jest.fn()
    })

    render(<AccountantDashboardWrapper />)

    // Should show loading spinner
    expect(screen.getByText(/Loading/)).toBeInTheDocument()
  })

  it('should render header action buttons', () => {
    render(<AccountantDashboardWrapper />)

    expect(screen.getByText('Reports')).toBeInTheDocument()
    // Use getAllByText since 'View Receipts' appears multiple times
    const receiptsElements = screen.getAllByText('View Receipts')
    expect(receiptsElements.length).toBeGreaterThan(0)
  })

  it('should use 3-column grid layout for quick actions', () => {
    render(<AccountantDashboardWrapper />)

    const quickActionsContainer = screen.getByText('Quick Actions').closest('.card')
    const gridContainer = quickActionsContainer?.querySelector('.grid')
    expect(gridContainer?.className).toContain('md:grid-cols-3')
  })
})
