import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useShopUsers,
  useTenantUsers,
  useUser,
  useCreateUser
} from '../useUsers'
import { userService } from '@/services/userService'
import { getMockManager, getMockInvestor, getMockAdmin, getMockShopOwner } from '@/testData/users'
import { toast } from 'sonner'

// Mock dependencies
jest.mock('@/services/userService', () => ({
  userService: {
    getShopUsers: jest.fn(),
    getTenantUsers: jest.fn(),
    getUserById: jest.fn(),
    createUserInTenant: jest.fn()
  }
}))

jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}))

const mockUserService = userService as jest.Mocked<typeof userService>
const mockToast = toast as jest.Mocked<typeof toast>

describe('useUsers', () => {
  let queryClient: QueryClient
  let wrapper: React.FC<{ children: React.ReactNode }>

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false }
      }
    })
    wrapper = ({ children }) => React.createElement(
      QueryClientProvider,
      { client: queryClient },
      children
    )
    jest.clearAllMocks()
  })

  afterEach(() => {
    queryClient.clear()
  })

  describe('useShopUsers', () => {
    it('should fetch shop users successfully', async () => {
      const mockUsers = {
        content: [getMockManager(), getMockShopOwner()],
        totalElements: 2,
        totalPages: 1,
        number: 0,
        size: 50
      }

      mockUserService.getShopUsers.mockResolvedValueOnce(mockUsers)

      const { result } = renderHook(
        () => useShopUsers({ shopId: 'shop1', status: 'ACTIVE' }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.content).toHaveLength(2)
      expect(mockUserService.getShopUsers).toHaveBeenCalledWith('shop1', 'ACTIVE', 0, 50)
    })

    it('should not fetch if shopId is not provided', () => {
      const { result } = renderHook(
        () => useShopUsers({ shopId: undefined }),
        { wrapper }
      )

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockUserService.getShopUsers).not.toHaveBeenCalled()
    })

    it('should respect enabled option', () => {
      const { result } = renderHook(
        () => useShopUsers({ shopId: 'shop1', enabled: false }),
        { wrapper }
      )

      expect(result.current.isLoading).toBe(false)
      expect(mockUserService.getShopUsers).not.toHaveBeenCalled()
    })

    it('should handle pagination', async () => {
      const mockUsers = {
        content: [getMockManager()],
        totalElements: 5,
        totalPages: 3,
        number: 1,
        size: 2
      }

      mockUserService.getShopUsers.mockResolvedValueOnce(mockUsers)

      const { result } = renderHook(
        () => useShopUsers({ shopId: 'shop1', page: 1, size: 2 }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.number).toBe(1)
      expect(mockUserService.getShopUsers).toHaveBeenCalledWith('shop1', 'ACTIVE', 1, 2)
    })

    it('should filter by status', async () => {
      const mockUsers = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 50
      }

      mockUserService.getShopUsers.mockResolvedValueOnce(mockUsers)

      const { result } = renderHook(
        () => useShopUsers({ shopId: 'shop1', status: 'INACTIVE' }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(mockUserService.getShopUsers).toHaveBeenCalledWith('shop1', 'INACTIVE', 0, 50)
    })

    it('should handle fetch error', async () => {
      mockUserService.getShopUsers.mockRejectedValueOnce(new Error('Failed to fetch users'))

      const { result } = renderHook(
        () => useShopUsers({ shopId: 'shop1' }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })

      expect(result.current.error).toBeTruthy()
    })
  })

  describe('useTenantUsers', () => {
    it('should fetch tenant users successfully', async () => {
      const mockUsers = [
        getMockAdmin(),
        getMockInvestor(),
        getMockShopOwner()
      ]

      mockUserService.getTenantUsers.mockResolvedValueOnce(mockUsers)

      const { result } = renderHook(
        () => useTenantUsers({ tenantId: 'tenant1' }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(3)
      expect(mockUserService.getTenantUsers).toHaveBeenCalledWith('tenant1')
    })

    it('should not fetch if tenantId is not provided', () => {
      const { result } = renderHook(
        () => useTenantUsers({ tenantId: undefined }),
        { wrapper }
      )

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockUserService.getTenantUsers).not.toHaveBeenCalled()
    })

    it('should respect enabled option', () => {
      const { result } = renderHook(
        () => useTenantUsers({ tenantId: 'tenant1', enabled: false }),
        { wrapper }
      )

      expect(result.current.isLoading).toBe(false)
      expect(mockUserService.getTenantUsers).not.toHaveBeenCalled()
    })

    it('should handle fetch error', async () => {
      mockUserService.getTenantUsers.mockRejectedValueOnce(new Error('Unauthorized'))

      const { result } = renderHook(
        () => useTenantUsers({ tenantId: 'tenant1' }),
        { wrapper }
      )

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('useUser', () => {
    it('should fetch single user successfully', async () => {
      const mockUser = getMockManager()
      mockUserService.getUserById.mockResolvedValueOnce(mockUser)

      const { result } = renderHook(() => useUser('3'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBe('3')
      expect(result.current.data?.username).toBe('manager')
      expect(mockUserService.getUserById).toHaveBeenCalledWith('3')
    })

    it('should not fetch if userId is empty', () => {
      const { result } = renderHook(() => useUser(''), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockUserService.getUserById).not.toHaveBeenCalled()
    })

    it('should respect enabled parameter', () => {
      const { result } = renderHook(() => useUser('3', false), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(mockUserService.getUserById).not.toHaveBeenCalled()
    })

    it('should handle 404 error', async () => {
      mockUserService.getUserById.mockRejectedValueOnce(new Error('User not found'))

      const { result } = renderHook(() => useUser('999'), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('useCreateUser', () => {
    it('should create user successfully', async () => {
      const newUser = getMockManager()
      mockUserService.createUserInTenant.mockResolvedValueOnce(newUser)

      const { result } = renderHook(() => useCreateUser(), { wrapper })

      await result.current.mutateAsync({
        tenantId: 'tenant1',
        request: {
          username: 'manager',
          email: 'manager@example.com',
          password: 'password123',
          firstName: 'Store',
          lastName: 'Manager',
          roles: ['ROLE_MANAGER']
        }
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('User created successfully')
      })

      expect(mockUserService.createUserInTenant).toHaveBeenCalledWith('tenant1', {
        username: 'manager',
        email: 'manager@example.com',
        password: 'password123',
        firstName: 'Store',
        lastName: 'Manager',
        roles: ['ROLE_MANAGER']
      })
    })

    it('should handle create error', async () => {
      const error = { response: { data: { message: 'Username already exists' } } }
      mockUserService.createUserInTenant.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useCreateUser(), { wrapper })

      try {
        await result.current.mutateAsync({
          tenantId: 'tenant1',
          request: {
            username: 'duplicate',
            email: 'duplicate@example.com',
            password: 'password123',
            firstName: 'Test',
            lastName: 'User',
            roles: ['ROLE_USER']
          }
        })
      } catch (e) {
        // Expected error
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Username already exists')
      })
    })

    it('should invalidate queries on success', async () => {
      const newUser = getMockManager()
      mockUserService.createUserInTenant.mockResolvedValueOnce(newUser)

      const invalidateQueriesSpy = jest.spyOn(queryClient, 'invalidateQueries')

      const { result } = renderHook(() => useCreateUser(), { wrapper })

      await result.current.mutateAsync({
        tenantId: 'tenant1',
        request: {
          username: 'newuser',
          email: 'newuser@example.com',
          password: 'password123',
          firstName: 'New',
          lastName: 'User',
          roles: ['ROLE_USER']
        }
      })

      await waitFor(() => {
        expect(invalidateQueriesSpy).toHaveBeenCalledWith({ queryKey: ['tenant-users'] })
        expect(invalidateQueriesSpy).toHaveBeenCalledWith({ queryKey: ['shop-users'] })
      })
    })
  })
})
