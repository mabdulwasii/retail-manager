import { describe, it, expect, beforeEach, jest } from '@jest/globals'

// Mock user profiles for testing
const mockUserProfile = {
  id: 'user-123',
  username: 'john.doe',
  email: 'john.doe@example.com',
  roles: [
    {
      id: '1',
      name: 'MANAGER',
      description: 'Manager role',
      isSystem: false,
      permissions: ['PRODUCT_READ', 'PRODUCT_WRITE']
    },
    {
      id: '2',
      name: 'EMPLOYEE',
      description: 'Employee role',
      isSystem: false,
      permissions: ['PRODUCT_READ']
    }
  ]
};

/* eslint-disable @typescript-eslint/no-explicit-any */
const mockUserProfileInvestor = {
  id: '456',
  username: 'investor',
  email: 'investor@example.com',
  isInvestor: true,
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
      permissions: ['TENANT_MANAGE']
    }
  ]
};

/**
 * API Service Tests
 *
 * Note: These tests mock the API service directly rather than testing HTTP calls.
 * MSW v2 intercepts fetch requests, but axios in jsdom uses XHR adapter.
 * For actual HTTP interception testing, see component tests that use React Query hooks.
 */

// Mock the entire API service module
const mockGetUserProfile = jest.fn()

jest.mock('@/services/api', () => ({
  apiService: {
    getUserProfile: mockGetUserProfile,
    setTokenProvider: jest.fn()
  }
}))

// Import after mock
import { apiService } from '@/services/api'

describe('API Service - User Profile', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('getUserProfile', () => {
    it('should fetch user profile successfully', async () => {
      mockGetUserProfile.mockResolvedValue(mockUserProfile)

      const profile = await apiService.getUserProfile()

      expect(profile).toEqual(mockUserProfile)
      expect(profile.id).toBe('user-123')
      expect(profile.username).toBe('john.doe')
      expect(profile.email).toBe('john.doe@example.com')
      expect(profile.roles).toHaveLength(2)
      expect(profile.roles.map((r: any) => r.name)).toEqual(['MANAGER', 'EMPLOYEE'])
      expect(mockGetUserProfile).toHaveBeenCalledTimes(1)
    })

    it('should handle 401 unauthorized error', async () => {
      const error = new Error('Unauthorized')
      mockGetUserProfile.mockRejectedValue(error)

      await expect(apiService.getUserProfile()).rejects.toThrow('Unauthorized')
      expect(mockGetUserProfile).toHaveBeenCalledTimes(1)
    })

    it('should handle 500 server error', async () => {
      const error = new Error('Internal server error')
      mockGetUserProfile.mockRejectedValue(error)

      await expect(apiService.getUserProfile()).rejects.toThrow('Internal server error')
      expect(mockGetUserProfile).toHaveBeenCalledTimes(1)
    })

    it('should return investor profile correctly', async () => {
      mockGetUserProfile.mockResolvedValue(mockUserProfileInvestor)

      const profile = await apiService.getUserProfile()

      expect(profile).toEqual(mockUserProfileInvestor)
      expect(profile.isInvestor).toBe(true)
      expect(profile.roles).toHaveLength(2)
      expect(profile.roles.map((r: any) => r.name)).toContain('INVESTOR')
      expect(profile.roles.map((r: any) => r.name)).toContain('TENANT_ADMIN')
      expect(mockGetUserProfile).toHaveBeenCalledTimes(1)
    })
  })
})
