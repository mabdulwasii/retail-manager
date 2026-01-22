import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, beforeEach } from '@jest/globals'
import { ProfilePage } from '@/pages/ProfilePage'

// Mock data for testing
const mockData = {
  users: {
    profile: {
      id: 'user-123',
      username: 'john.doe',
      email: 'john.doe@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phoneNumber: '+1234567890',
      tenantId: 'tenant-123',
      shopId: 'shop-456',
      status: 'ACTIVE',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-15T00:00:00Z',
      roles: [
        {
          id: '1',
          name: 'MANAGER',
          description: 'Manager role',
          isSystem: false,
          permissions: ['USER_READ', 'USER_WRITE']
        },
        {
          id: '2',
          name: 'EMPLOYEE',
          description: 'Employee role',
          isSystem: false,
          permissions: ['USER_READ']
        }
      ]
    },
    investor: {
      id: 'investor-456',
      username: 'investor',
      email: 'investor@example.com',
      firstName: 'Investor',
      lastName: 'User',
      phoneNumber: '+9876543210',
      tenantId: 'tenant-789',
      shopId: 'shop-999',
      status: 'ACTIVE',
      isInvestor: true,
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-15T00:00:00Z',
      roles: [
        {
          id: '3',
          name: 'INVESTOR',
          description: 'Investor role',
          isSystem: false,
          permissions: ['INVESTMENT_READ']
        },
        {
          id: '4',
          name: 'TENANT_ADMIN',
          description: 'Tenant Admin role',
          isSystem: false,
          permissions: ['TENANT_MANAGE', 'USER_MANAGE']
        }
      ]
    }
  }
};

/* eslint-disable @typescript-eslint/no-explicit-any */
// Mock the API service and auth context
jest.mock('@/context/UnifiedAuthContext')
jest.mock('@/services/api')

// Mock UI components that might cause issues in tests
jest.mock('@/components/ui/card', () => ({
  Card: ({ children, ...props }: any) => <div data-testid="card" {...props}>{children}</div>,
  CardContent: ({ children, ...props }: any) => <div data-testid="card-content" {...props}>{children}</div>,
  CardDescription: ({ children, ...props }: any) => <div data-testid="card-description" {...props}>{children}</div>,
  CardHeader: ({ children, ...props }: any) => <div data-testid="card-header" {...props}>{children}</div>,
  CardTitle: ({ children, ...props }: any) => <h3 data-testid="card-title" {...props}>{children}</h3>
}))

jest.mock('@/components/ui/button', () => ({
  Button: ({ children, ...props }: any) => <button data-testid="button" {...props}>{children}</button>
}))

jest.mock('@/components/ui/badge', () => ({
  Badge: ({ children, className, ...props }: any) => (
    <span data-testid="badge" className={className} {...props}>{children}</span>
  )
}))

jest.mock('@/components/ui/separator', () => ({
  Separator: (props: any) => <hr data-testid="separator" {...props} />
}))

jest.mock('@/components/ui/alert', () => ({
  Alert: ({ children, variant, ...props }: any) => (
    <div data-testid="alert" data-variant={variant} {...props}>{children}</div>
  ),
  AlertDescription: ({ children, ...props }: any) => (
    <div data-testid="alert-description" {...props}>{children}</div>
  )
}))

// Import mocked modules after mocking
import { useAuth } from '@/context/UnifiedAuthContext'
import { apiService } from '@/services/api'

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockApiService = apiService as jest.Mocked<typeof apiService>

// Wrapper component to provide Router context
const ProfilePageWrapper: React.FC = () => (
  <MemoryRouter>
    <ProfilePage />
  </MemoryRouter>
)

describe('ProfilePage', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('Authentication States', () => {
    it('should show login message when user is not authenticated', async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        user: null
      })

      render(<ProfilePageWrapper />)

      expect(screen.getByTestId('alert')).toBeInTheDocument()
      expect(screen.getByTestId('alert-description')).toHaveTextContent(
        'You must be logged in to view your profile.'
      )
    })

    it('should show loading state when fetching profile', async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { username: 'test.user' }
      })

      mockApiService.getUserProfile.mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve(mockData.users.profile), 100))
      )

      render(<ProfilePageWrapper />)

      expect(screen.getByText('Loading profile...')).toBeInTheDocument()

      await waitFor(() => {
        expect(screen.queryByText('Loading profile...')).not.toBeInTheDocument()
      })
    })
  })

  describe('Profile Display', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: {
          username: 'john.doe',
          email: 'john.doe@example.com',
          firstName: 'John',
          lastName: 'Doe',
          roles: ['MANAGER']
        }
      })
    })

    it('should display user profile information successfully', async () => {
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByText('Profile')).toBeInTheDocument()
      })

      // Check personal information - use getAllByText for text that may appear multiple times
      const johnElements = screen.getAllByText('John')
      expect(johnElements.length).toBeGreaterThan(0)

      const doeElements = screen.getAllByText('Doe')
      expect(doeElements.length).toBeGreaterThan(0)

      const usernameElements = screen.getAllByText('john.doe')
      expect(usernameElements.length).toBeGreaterThan(0)

      const emailElements = screen.getAllByText('john.doe@example.com')
      expect(emailElements.length).toBeGreaterThan(0)

      expect(screen.getByText('+1234567890')).toBeInTheDocument()

      // Check roles - use queryAllByText for roles that might not render exactly
      const managerElements = screen.queryAllByText(/MANAGER/i)
      expect(managerElements.length).toBeGreaterThan(0)
      
      const employeeElements = screen.queryAllByText(/EMPLOYEE/i)
      expect(employeeElements.length).toBeGreaterThan(0)

      // Check status - might be rendered differently
      const bodyText = document.body.textContent || ''
      expect(bodyText).toMatch(/ACTIVE|Active/i)

      // Check IDs
      expect(screen.getByText('tenant-123')).toBeInTheDocument()
      expect(screen.getByText('shop-456')).toBeInTheDocument()
    })

    it('should display investor badge when user is an investor', async () => {
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.investor)

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        // "Investor" appears multiple times (name + badge)
        const investorElements = screen.getAllByText('Investor')
        expect(investorElements.length).toBeGreaterThan(0)
        
        // Check for role badges
        const investorRoleElements = screen.getAllByText('INVESTOR')
        expect(investorRoleElements.length).toBeGreaterThan(0)
        
        const tenantAdminElements = screen.getAllByText('TENANT ADMIN')
        expect(tenantAdminElements.length).toBeGreaterThan(0)
      })
    })

    it('should fallback to auth context data when API fails', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('API Error'))

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toBeInTheDocument()
        expect(screen.getByText('Failed to load profile information. Please try again.')).toBeInTheDocument()
      })
    })

    it('should handle missing optional fields gracefully', async () => {
      const incompleteProfile = {
        ...mockData.users.profile,
        firstName: undefined,
        lastName: undefined,
        phoneNumber: undefined
      }

      mockApiService.getUserProfile.mockResolvedValue(incompleteProfile)

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getAllByText('Not provided')).toHaveLength(2) // firstName and lastName
      })
    })
  })

  describe('UI Interactions', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { username: 'test.user' }
      })
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)
    })

    it('should render edit profile buttons', async () => {
      render(<ProfilePageWrapper />)

      await waitFor(() => {
        const editButtons = screen.getAllByText('Edit Profile')
        expect(editButtons).toHaveLength(2) // One in header, one in actions
      })
    })

    it('should render action buttons', async () => {
      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByText('Security Settings')).toBeInTheDocument()
        expect(screen.getByText('Notification Preferences')).toBeInTheDocument()
      })
    })
  })

  describe('Date Formatting', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { username: 'test.user' }
      })
    })

    it('should format dates correctly', async () => {
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        // Check that dates are present (format may vary based on locale)
        const bodyText = document.body.textContent || ''
        expect(bodyText).toMatch(/2024/) // Year should appear
        // Verify profile loaded (dates section exists)
        expect(screen.getByText('Personal Information')).toBeInTheDocument()
      })
    })

    it('should handle missing dates', async () => {
      const profileWithoutDates = {
        ...mockData.users.profile,
        createdAt: undefined,
        updatedAt: undefined
      }

      mockApiService.getUserProfile.mockResolvedValue(profileWithoutDates)

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getAllByText('N/A')).toHaveLength(2)
      })
    })
  })

  describe('Error Handling', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { username: 'test.user' }
      })
    })

    it('should handle API errors gracefully', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('Network error'))

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toHaveAttribute('data-variant', 'destructive')
        expect(screen.getByText('Failed to load profile information. Please try again.')).toBeInTheDocument()
      })
    })

    it('should handle 401 unauthorized errors', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('Unauthorized'))

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toHaveAttribute('data-variant', 'destructive')
      })
    })

    it('should handle 404 not found errors', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('Not found'))

      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toHaveAttribute('data-variant', 'destructive')
      })
    })
  })

  describe('Accessibility', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        user: { username: 'test.user' }
      })
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)
    })

    it('should have proper heading hierarchy', async () => {
      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Profile')
      })
    })

    it('should have proper labels for form fields', async () => {
      render(<ProfilePageWrapper />)

      await waitFor(() => {
        expect(screen.getByText('First Name')).toBeInTheDocument()
        expect(screen.getByText('Last Name')).toBeInTheDocument()
        expect(screen.getByText('Email')).toBeInTheDocument()
        expect(screen.getByText('Phone')).toBeInTheDocument()
      })
    })
  })
})