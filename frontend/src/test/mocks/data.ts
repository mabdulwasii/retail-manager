import { UserProfile } from '@/types/user'

// Mock user profile data for testing
export const mockUserProfile: UserProfile = {
  id: 'user-123',
  username: 'john.doe',
  email: 'john.doe@example.com',
  firstName: 'John',
  lastName: 'Doe',
  fullName: 'John Doe',
  phoneNumber: '+1234567890',
  status: 'ACTIVE',
  isInvestor: false,
  roles: ['MANAGER', 'EMPLOYEE'],
  tenantId: 'tenant-123',
  shopId: 'shop-456',
  createdAt: '2024-01-15T10:30:00Z',
  updatedAt: '2024-01-20T15:45:00Z'
}

// Mock investor user profile
export const mockUserProfileInvestor: UserProfile = {
  id: 'investor-123',
  username: 'jane.investor',
  email: 'jane.investor@example.com',
  firstName: 'Jane',
  lastName: 'Investor',
  fullName: 'Jane Investor',
  phoneNumber: '+1987654321',
  status: 'ACTIVE',
  isInvestor: true,
  roles: ['INVESTOR', 'TENANT_ADMIN'],
  tenantId: 'tenant-789',
  shopId: 'shop-999',
  createdAt: '2024-01-10T08:15:00Z',
  updatedAt: '2024-01-25T12:30:00Z'
}

// Mock minimal user profile (missing optional fields)
export const mockUserProfileMinimal: UserProfile = {
  id: 'user-minimal',
  username: 'minimal.user',
  email: 'minimal@example.com',
  status: 'ACTIVE',
  isInvestor: false,
  roles: ['EMPLOYEE']
}

// Mock user profile for error scenarios
export const mockUserProfileError = {
  error: 'User not found',
  message: 'The requested user profile could not be found'
}

// Mock authentication context data
export const mockAuthContext = {
  authenticated: {
    isAuthenticated: true,
    user: {
      username: 'john.doe',
      email: 'john.doe@example.com',
      firstName: 'John',
      lastName: 'Doe',
      roles: ['MANAGER']
    }
  },
  unauthenticated: {
    isAuthenticated: false,
    user: null
  }
}

// Mock API responses
export const mockApiResponses = {
  profileSuccess: {
    data: mockUserProfile,
    success: true,
    message: 'Profile retrieved successfully'
  },
  profileError: {
    success: false,
    message: 'Failed to retrieve profile',
    error: 'PROFILE_NOT_FOUND'
  },
  unauthorized: {
    success: false,
    message: 'Unauthorized access',
    error: 'UNAUTHORIZED'
  },
  serverError: {
    success: false,
    message: 'Internal server error',
    error: 'SERVER_ERROR'
  }
}

// Mock Keycloak instances
export const mockKeycloak = {
  valid: {
    token: 'mock-jwt-token',
    updateToken: jest.fn().mockResolvedValue(true),
    logout: jest.fn()
  },
  expired: {
    token: 'expired-token',
    updateToken: jest.fn().mockResolvedValue(false),
    logout: jest.fn()
  },
  missing: {
    token: null,
    updateToken: jest.fn(),
    logout: jest.fn()
  }
}

// Mock shop data
export const mockShop = {
  id: 'shop-456',
  name: 'Test Shop',
  email: 'shop@example.com',
  phone: '+1234567890',
  address: {
    street: '123 Main St',
    city: 'Test City',
    state: 'TS',
    zipCode: '12345',
    country: 'Test Country'
  },
  status: 'ACTIVE',
  tenantId: 'tenant-123'
}

// Mock tenant data
export const mockTenant = {
  id: 'tenant-123',
  name: 'Test Tenant',
  email: 'tenant@example.com',
  status: 'ACTIVE'
}

// Export all mocks in a convenient object
export const mockData = {
  users: {
    profile: mockUserProfile,
    investor: mockUserProfileInvestor,
    minimal: mockUserProfileMinimal
  },
  auth: mockAuthContext,
  api: mockApiResponses,
  keycloak: mockKeycloak,
  shop: mockShop,
  tenant: mockTenant
}