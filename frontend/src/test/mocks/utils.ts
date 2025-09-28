import { mockData } from './data'

// Mock function factories for common testing scenarios
export const createMockAuth = (isAuthenticated = true, userOverrides = {}) => {
  const baseUser = isAuthenticated ? mockData.auth.authenticated.user : null
  return {
    isAuthenticated,
    user: baseUser ? { ...baseUser, ...userOverrides } : null
  }
}

export const createMockKeycloak = (hasValidToken = true, tokenOverrides = {}) => {
  const baseKeycloak = hasValidToken ? mockData.keycloak.valid : mockData.keycloak.missing
  return {
    ...baseKeycloak,
    ...tokenOverrides
  }
}

export const createMockApiService = (overrides = {}) => {
  return {
    getUserProfile: jest.fn().mockResolvedValue(mockData.users.profile),
    ...overrides
  }
}

// Common test scenarios
export const testScenarios = {
  // Authentication scenarios
  auth: {
    authenticated: () => createMockAuth(true),
    unauthenticated: () => createMockAuth(false),
    manager: () => createMockAuth(true, { roles: ['SHOP_MANAGER'] }),
    employee: () => createMockAuth(true, { roles: ['SHOP_EMPLOYEE'] }),
    investor: () => createMockAuth(true, { roles: ['INVESTOR'] }),
    admin: () => createMockAuth(true, { roles: ['TENANT_ADMIN'] })
  },

  // API response scenarios
  api: {
    success: () => mockData.api.profileSuccess,
    error: () => mockData.api.profileError,
    unauthorized: () => mockData.api.unauthorized,
    serverError: () => mockData.api.serverError
  },

  // User profile scenarios
  users: {
    standard: () => mockData.users.profile,
    investor: () => mockData.users.investor,
    minimal: () => mockData.users.minimal,
    withMissingFields: () => ({
      ...mockData.users.profile,
      firstName: undefined,
      lastName: undefined,
      phoneNumber: undefined
    })
  },

  // Keycloak scenarios
  keycloak: {
    valid: () => createMockKeycloak(true),
    expired: () => createMockKeycloak(false, { token: 'expired-token' }),
    missing: () => createMockKeycloak(false),
    withRefresh: () => createMockKeycloak(true, {
      updateToken: jest.fn().mockResolvedValue(true)
    })
  }
}

// Helper functions for setting up common test mocks
export const setupAuthMock = (scenario = 'authenticated') => {
  const mockUseAuth = jest.fn()
  const authData = testScenarios.auth[scenario as keyof typeof testScenarios.auth]()
  mockUseAuth.mockReturnValue(authData)
  return mockUseAuth
}

export const setupApiMock = (scenario = 'success') => {
  const mockApiService = createMockApiService()

  switch (scenario) {
    case 'success':
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)
      break
    case 'investor':
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.investor)
      break
    case 'error':
      mockApiService.getUserProfile.mockRejectedValue(new Error('API Error'))
      break
    case 'unauthorized':
      mockApiService.getUserProfile.mockRejectedValue(new Error('Unauthorized'))
      break
    case 'notFound':
      mockApiService.getUserProfile.mockRejectedValue(new Error('Not found'))
      break
    default:
      mockApiService.getUserProfile.mockResolvedValue(mockData.users.profile)
  }

  return mockApiService
}

export const setupKeycloakMock = (scenario = 'valid') => {
  const mockGetKeycloak = jest.fn()
  const keycloakData = testScenarios.keycloak[scenario as keyof typeof testScenarios.keycloak]()
  mockGetKeycloak.mockReturnValue(keycloakData)
  return mockGetKeycloak
}

// Test data generators for dynamic scenarios
export const generateUserProfile = (overrides = {}) => ({
  ...mockData.users.profile,
  ...overrides
})

export const generateApiResponse = (data: any, success = true, message = 'Success') => ({
  data,
  success,
  message
})

// Common assertions for testing
export const commonAssertions = {
  userProfileDisplayed: (screen: any, user = mockData.users.profile) => {
    expect(screen.getByText(user.firstName || 'Not provided')).toBeInTheDocument()
    expect(screen.getByText(user.lastName || 'Not provided')).toBeInTheDocument()
    expect(screen.getByText(user.email)).toBeInTheDocument()
    expect(screen.getByText(user.username)).toBeInTheDocument()
  },

  rolesDisplayed: (screen: any, roles: string[]) => {
    roles.forEach(role => {
      const displayRole = role.replace('_', ' ').toUpperCase()
      expect(screen.getByText(displayRole)).toBeInTheDocument()
    })
  },

  errorMessageDisplayed: (screen: any, message = 'Failed to load profile information. Please try again.') => {
    expect(screen.getByText(message)).toBeInTheDocument()
  },

  loadingStateDisplayed: (screen: any) => {
    expect(screen.getByText('Loading profile...')).toBeInTheDocument()
  }
}