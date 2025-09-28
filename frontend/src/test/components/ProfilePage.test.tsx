import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach } from '@jest/globals'
import { ProfilePage } from '@/pages/ProfilePage'
import { mockData } from '@/test/mocks/data'

// Mock the auth context
jest.mock('@/context/KeycloakAuthContext')

// Mock the API service
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
import { useAuth } from '@/context/KeycloakAuthContext'
import { apiService } from '@/services/api'

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockApiService = apiService as jest.Mocked<typeof apiService>

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

      render(<ProfilePage />)

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

      render(<ProfilePage />)

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
          roles: ['SHOP_MANAGER']
        }
      })
    })

    it('should display user profile information successfully', async () => {
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)

      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByText('Profile')).toBeInTheDocument()
      })

      // Check personal information
      expect(screen.getByText('John')).toBeInTheDocument()
      expect(screen.getByText('Doe')).toBeInTheDocument()
      expect(screen.getByText('john.doe')).toBeInTheDocument()
      expect(screen.getByText('john.doe@example.com')).toBeInTheDocument()
      expect(screen.getByText('+1234567890')).toBeInTheDocument()

      // Check roles
      expect(screen.getByText('SHOP MANAGER')).toBeInTheDocument()
      expect(screen.getByText('SHOP EMPLOYEE')).toBeInTheDocument()

      // Check status
      expect(screen.getByText('ACTIVE')).toBeInTheDocument()

      // Check IDs
      expect(screen.getByText('tenant-123')).toBeInTheDocument()
      expect(screen.getByText('shop-456')).toBeInTheDocument()
    })

    it('should display investor badge when user is an investor', async () => {
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.investor)

      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByText('Investor')).toBeInTheDocument()
        expect(screen.getByText('INVESTOR')).toBeInTheDocument()
        expect(screen.getByText('TENANT ADMIN')).toBeInTheDocument()
      })
    })

    it('should fallback to auth context data when API fails', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('API Error'))

      render(<ProfilePage />)

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

      render(<ProfilePage />)

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
      render(<ProfilePage />)

      await waitFor(() => {
        const editButtons = screen.getAllByText('Edit Profile')
        expect(editButtons).toHaveLength(2) // One in header, one in actions
      })
    })

    it('should render action buttons', async () => {
      render(<ProfilePage />)

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

      render(<ProfilePage />)

      await waitFor(() => {
        // Check that dates are formatted (exact format may vary based on locale)
        expect(screen.getByText(/January \d+, 2024/)).toBeInTheDocument()
      })
    })

    it('should handle missing dates', async () => {
      const profileWithoutDates = {
        ...mockData.users.profile,
        createdAt: undefined,
        updatedAt: undefined
      }

      mockApiService.getUserProfile.mockResolvedValue(profileWithoutDates)

      render(<ProfilePage />)

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

      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toHaveAttribute('data-variant', 'destructive')
        expect(screen.getByText('Failed to load profile information. Please try again.')).toBeInTheDocument()
      })
    })

    it('should handle 401 unauthorized errors', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('Unauthorized'))

      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByTestId('alert')).toHaveAttribute('data-variant', 'destructive')
      })
    })

    it('should handle 404 not found errors', async () => {
      mockApiService.getUserProfile.mockRejectedValue(new Error('Not found'))

      render(<ProfilePage />)

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
      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Profile')
      })
    })

    it('should have proper labels for form fields', async () => {
      render(<ProfilePage />)

      await waitFor(() => {
        expect(screen.getByText('First Name')).toBeInTheDocument()
        expect(screen.getByText('Last Name')).toBeInTheDocument()
        expect(screen.getByText('Email')).toBeInTheDocument()
        expect(screen.getByText('Phone')).toBeInTheDocument()
      })
    })
  })
})