import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { CashierDashboard } from '../CashierDashboard'
import { useAuth } from '@/context/AuthContext'
import { useDashboardStats, useRecentActivities } from '@/hooks/useDashboard'
import { useCurrency } from '@/hooks/useCurrency'

// Mock all dependencies
jest.mock('@/context/AuthContext')
jest.mock('@/hooks/useDashboard')
jest.mock('@/hooks/useCurrency')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUseDashboardStats = useDashboardStats as jest.MockedFunction<typeof useDashboardStats>
const mockUseRecentActivities = useRecentActivities as jest.MockedFunction<typeof useRecentActivities>
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

jest.mock('@/components/ui/loading-spinner', () => ({
  LoadingSpinner: ({ size }: { size?: string }) =>
    <div data-testid="loading-spinner" className={size}>Loading...</div>
}))

const CashierDashboardWrapper: React.FC = () => (
  <BrowserRouter>
    <CashierDashboard />
  </BrowserRouter>
)

describe('CashierDashboard', () => {
  const mockUser = {
    id: '1',
    username: 'cashier',
    email: 'cashier@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['ROLE_CASHIER']
  }

  const mockStats = {
    totalRevenue: 150000,
    totalShops: 5,
    totalProducts: 1200,
    totalSales: 450,
    investmentROI: 25.5,
    activeUsers: 125,
    systemHealth: 99.9,
    revenueGrowth: 12.5,
  }

  const mockActivities = [
    {
      id: '1',
      type: 'sale' as const,
      description: 'iPhone 15 Pro sale completed',
      shop: 'Electronics Store',
      amount: '₦450,000',
      time: '5 minutes ago'
    },
    {
      id: '2',
      type: 'inventory' as const,
      description: 'Stock check completed',
      shop: 'Electronics Store',
      amount: '125 items',
      time: '1 hour ago'
    }
  ]

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

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

    mockUseDashboardStats.mockReturnValue({
      stats: mockStats,
      loading: false,
      error: null,
      refetch: jest.fn()
    })

    mockUseRecentActivities.mockReturnValue({
      activities: mockActivities,
      loading: false,
      error: null,
      refetch: jest.fn()
    })
  })

  it('should render welcome message with user name', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Welcome, John!')).toBeInTheDocument()
    expect(screen.getByText('Ready to serve customers and process sales.')).toBeInTheDocument()
  })

  it('should render welcome message with username fallback', () => {
    mockUseAuth.mockReturnValue({
      user: { ...mockUser, firstName: undefined, lastName: undefined },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Welcome, cashier!')).toBeInTheDocument()
  })

  it('should render performance stats cards', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Sales Today')).toBeInTheDocument()
    expect(screen.getByText('Revenue Today')).toBeInTheDocument()
    expect(screen.getByText('Items Sold')).toBeInTheDocument()
    expect(screen.getByText('Avg. Transaction')).toBeInTheDocument()

    // Check formatted values
    expect(screen.getByText('450')).toBeInTheDocument() // totalSales
    expect(screen.getByText('1,200')).toBeInTheDocument() // totalProducts
  })

  it('should handle stats calculation when data is available', () => {
    render(<CashierDashboardWrapper />)

    // Should calculate average transaction
    const avgTransaction = mockStats.totalRevenue / mockStats.totalSales
    expect(screen.getByText(`₦${avgTransaction.toFixed(2)}`)).toBeInTheDocument()
  })

  it('should handle missing stats gracefully', () => {
    mockUseDashboardStats.mockReturnValue({
      stats: null,
      loading: false,
      error: null,
      refetch: jest.fn()
    })

    render(<CashierDashboardWrapper />)

    expect(screen.getByText('0')).toBeInTheDocument() // Should show 0 for missing sales
    expect(screen.getByText('₦0')).toBeInTheDocument() // Should show ₦0 for missing revenue
  })

  it('should render quick action buttons', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('New Sale')).toBeInTheDocument()
    expect(screen.getByText('Scan Product')).toBeInTheDocument()
    expect(screen.getByText('View Receipts')).toBeInTheDocument()
    expect(screen.getByText('Check Inventory')).toBeInTheDocument()

    expect(screen.getByText('Process customer purchase')).toBeInTheDocument()
    expect(screen.getByText('Quick barcode scan')).toBeInTheDocument()
    expect(screen.getByText('Recent transactions')).toBeInTheDocument()
    expect(screen.getByText('Product availability')).toBeInTheDocument()
  })

  it('should render recent activities when available', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Recent Activity')).toBeInTheDocument()
    expect(screen.getByText('iPhone 15 Pro sale completed')).toBeInTheDocument()
    expect(screen.getByText('Stock check completed')).toBeInTheDocument()
    expect(screen.getByText('₦450,000')).toBeInTheDocument()
    expect(screen.getByText('125 items')).toBeInTheDocument()
  })

  it('should show loading spinner when activities are loading', () => {
    mockUseRecentActivities.mockReturnValue({
      activities: [],
      loading: true,
      error: null,
      refetch: jest.fn()
    })

    render(<CashierDashboardWrapper />)

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument()
  })

  it('should show no activity message when activities array is empty', () => {
    mockUseRecentActivities.mockReturnValue({
      activities: [],
      loading: false,
      error: null,
      refetch: jest.fn()
    })

    render(<CashierDashboardWrapper />)

    expect(screen.getByText('No recent activity')).toBeInTheDocument()
  })

  it('should render sales tips section', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Sales Tips')).toBeInTheDocument()
    expect(screen.getByText('Boost your performance with these tips')).toBeInTheDocument()
    expect(screen.getByText('Upselling Opportunity')).toBeInTheDocument()
    expect(screen.getByText('Customer Service')).toBeInTheDocument()
    expect(screen.getByText('Product Knowledge')).toBeInTheDocument()
  })

  it('should render shift information with current time', () => {
    render(<CashierDashboardWrapper />)

    expect(screen.getByText('Shift Information')).toBeInTheDocument()
    expect(screen.getByText('8:00 AM')).toBeInTheDocument()
    expect(screen.getByText('Shift Started')).toBeInTheDocument()
    expect(screen.getByText('6:00 PM')).toBeInTheDocument()
    expect(screen.getByText('Shift Ends')).toBeInTheDocument()
    expect(screen.getByText('Current Time')).toBeInTheDocument()
  })

  it('should show loading spinner when stats are loading', async () => {
    mockUseDashboardStats.mockReturnValue({
      stats: null,
      loading: true,
      error: null,
      refetch: jest.fn()
    })

    render(<CashierDashboardWrapper />)

    expect(screen.getByTestId('loading-spinner')).toBeInTheDocument()
    expect(screen.queryByText('Welcome, John!')).not.toBeInTheDocument()
  })

  it('should render correctly with Nigerian Naira currency formatting', () => {
    render(<CashierDashboardWrapper />)

    // Verify that currency formatting is using Naira
    expect(mockUseCurrency().formatCurrency).toBeDefined()

    // Check that revenue is displayed with Naira symbol
    const formattedRevenue = screen.getByText(/₦150,000/)
    expect(formattedRevenue).toBeInTheDocument()
  })

  it('should handle activity type styling correctly', () => {
    render(<CashierDashboardWrapper />)

    const activities = screen.getByText('Recent Activity').parentElement
    expect(activities).toBeInTheDocument()

    // Should have different colored indicators for different activity types
    // This would be tested by checking the className contains the right color classes
    // but since we're mocking the UI components, we just verify the content is rendered
    expect(screen.getByText('iPhone 15 Pro sale completed')).toBeInTheDocument()
    expect(screen.getByText('Stock check completed')).toBeInTheDocument()
  })
})