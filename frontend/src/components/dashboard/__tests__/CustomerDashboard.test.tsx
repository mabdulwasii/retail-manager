import React from 'react'
import { render, screen } from '@testing-library/react'
import type {
  MockCardProps,
  MockButtonProps
} from '@/test-utils/mock-types'

import { BrowserRouter } from 'react-router-dom'
import { CustomerDashboard } from '../CustomerDashboard'
import { useAuth } from '@/context/UnifiedAuthContext'

// Mock dependencies
jest.mock('@/context/UnifiedAuthContext')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>

// Mock UI components
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, className }: MockCardProps) => <div className={`card ${className || ''}`}>{children}</div>,
  CardContent: ({ children }: MockCardProps) => <div className="card-content">{children}</div>,
  CardDescription: ({ children }: MockCardProps) => <div className="card-description">{children}</div>,
  CardHeader: ({ children }: MockCardProps) => <div className="card-header">{children}</div>,
  CardTitle: ({ children }: MockCardProps) => <div className="card-title">{children}</div>,
}))

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, className, asChild, ...props }: MockButtonProps) =>
    asChild ? children : <button className={className} {...props}>{children}</button>
}))

const CustomerDashboardWrapper: React.FC = () => (
  <BrowserRouter>
    <CustomerDashboard />
  </BrowserRouter>
)

describe('CustomerDashboard', () => {
  const mockUser = {
    id: '1',
    username: 'customer',
    email: 'customer@example.com',
    firstName: 'John',
    roles: ['ROLE_CUSTOMER']
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })
  })

  it('should render welcome message with customer name', () => {
    render(<CustomerDashboardWrapper />)

    expect(screen.getByText('Customer Portal')).toBeInTheDocument()
    expect(screen.getByText(/Welcome, John/)).toBeInTheDocument()
  })

  it('should render welcome message with username fallback', () => {
    mockUseAuth.mockReturnValue({
      user: { ...mockUser, firstName: undefined },
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

    render(<CustomerDashboardWrapper />)

    expect(screen.getByText(/Welcome, customer/)).toBeInTheDocument()
  })

  it('should render coming soon message', () => {
    render(<CustomerDashboardWrapper />)

    expect(screen.getByText('Customer Features Coming Soon')).toBeInTheDocument()
    expect(screen.getByText(/We're building exciting features/)).toBeInTheDocument()
  })

  it('should render available action buttons', () => {
    render(<CustomerDashboardWrapper />)

    expect(screen.getByText('Available Actions')).toBeInTheDocument()
    expect(screen.getByText('Browse Shops')).toBeInTheDocument()
    expect(screen.getByText('View Products')).toBeInTheDocument()
  })

  it('should have correct navigation links', () => {
    render(<CustomerDashboardWrapper />)

    const browseShopsButton = screen.getByText('Browse Shops')
    expect(browseShopsButton.closest('a')).toHaveAttribute('href', '/shops')

    const viewProductsButton = screen.getByText('View Products')
    expect(viewProductsButton.closest('a')).toHaveAttribute('href', '/products')
  })

  it('should render help text', () => {
    render(<CustomerDashboardWrapper />)

    expect(screen.getByText(/For assistance, please contact your shop administrator/)).toBeInTheDocument()
  })

  it('should display customer portal icon', () => {
    render(<CustomerDashboardWrapper />)

    // Icon should be present (ShoppingBag icon)
    const portalCard = screen.getByText('Customer Portal').closest('.card')
    expect(portalCard).toBeInTheDocument()
  })

  it('should render action descriptions', () => {
    render(<CustomerDashboardWrapper />)

    expect(screen.getByText('Explore available retail locations')).toBeInTheDocument()
    expect(screen.getByText('Browse available products')).toBeInTheDocument()
  })

  it('should have centered layout', () => {
    render(<CustomerDashboardWrapper />)

    const mainContainer = screen.getByText('Customer Portal').closest('.card')
    expect(mainContainer).toBeInTheDocument()
  })
})
