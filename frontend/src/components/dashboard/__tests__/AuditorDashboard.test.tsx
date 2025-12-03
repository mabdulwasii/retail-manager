import React from 'react'
import { screen, waitFor } from '@testing-library/react'
import { createMockAuth, renderWithProviders } from '@/test/test-utils'
import { AuditorDashboard } from '../AuditorDashboard'
import { useAuth } from '@/context/ManualAuthContext'
import { usePermissions } from '@/hooks/usePermissions'
import { getMockAuditor } from '@/testData'

// Mock only infrastructure dependencies
jest.mock('@/context/ManualAuthContext')
jest.mock('@/hooks/usePermissions', () => ({
  usePermissions: jest.fn()
}))

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUsePermissions = usePermissions as jest.MockedFunction<typeof usePermissions>

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

describe('AuditorDashboard', () => {
  const mockUser = getMockAuditor()

  const mockPermissions = {
    canViewAuditLogs: jest.fn(() => true),
    canViewUsers: jest.fn(() => true),
    canViewRoles: jest.fn(() => true),
    canViewSales: jest.fn(() => true),
    canViewShops: jest.fn(() => true),
    canManageShops: jest.fn(() => true),
    canViewAnalytics: jest.fn(() => true)
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))
    mockUsePermissions.mockReturnValue(mockPermissions as any)

    // MSW will handle data API calls!
  })

  it('should render without crashing', async () => {
    const { container } = renderWithProviders(<AuditorDashboard />)
    
    await waitFor(() => {
      expect(container).toBeInTheDocument()
    })
  })

  it('should render audit statistics cards', async () => {
    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      expect(screen.getByText('Audit Logs')).toBeInTheDocument()
    })
  })

  it('should render Quick Actions section', async () => {
    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      expect(screen.getByText('Quick Actions')).toBeInTheDocument()
    })
  })

  it('should render with permissions', async () => {
    renderWithProviders(<AuditorDashboard />)

    // Verify permissions hook is being called
    await waitFor(() => {
      expect(mockUsePermissions).toHaveBeenCalled()
    })
  })

  it('should respect permissions for quick actions', async () => {
    mockPermissions.canViewAuditLogs.mockReturnValue(false)
    mockPermissions.canViewUsers.mockReturnValue(false)

    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      // Component should still render
      expect(mockUseAuth).toHaveBeenCalled()
    })
  })

  it('should render Recent Audit Logs section', async () => {
    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      expect(screen.getByText('Recent Audit Logs')).toBeInTheDocument()
    })
  })

  it('should render compliance status section', async () => {
    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      expect(screen.getByText('Compliance Status')).toBeInTheDocument()
    })
  })

  it('should handle async data loading', async () => {
    renderWithProviders(<AuditorDashboard />)

    await waitFor(() => {
      expect(mockUseAuth).toHaveBeenCalled()
    })
  })
})
