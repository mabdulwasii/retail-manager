import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useRoles,
  useRole,
  usePermissions,
  useRolePermissions,
  useCreateRole,
  useUpdateRole,
  useDeleteRole,
  useAssignPermissions,
  useRemovePermissions
} from '../useRoles'
import roleService from '@/services/roleService'
import { toast } from 'sonner'

// Mock dependencies  
jest.mock('@/services/roleService', () => ({
  __esModule: true,
  default: {
    getRoles: jest.fn(),
    getRole: jest.fn(),
    getPermissions: jest.fn(),
    getRolePermissions: jest.fn(),
    createRole: jest.fn(),
    updateRole: jest.fn(),
    deleteRole: jest.fn(),
    assignPermissions: jest.fn(),
    removePermissions: jest.fn()
  }
}))

const mockGetRoles = roleService.getRoles as jest.MockedFunction<typeof roleService.getRoles>
const mockGetRole = roleService.getRole as jest.MockedFunction<typeof roleService.getRole>
const mockGetPermissions = roleService.getPermissions as jest.MockedFunction<typeof roleService.getPermissions>
const mockGetRolePermissions = roleService.getRolePermissions as jest.MockedFunction<typeof roleService.getRolePermissions>
const mockCreateRole = roleService.createRole as jest.MockedFunction<typeof roleService.createRole>
const mockUpdateRole = roleService.updateRole as jest.MockedFunction<typeof roleService.updateRole>
const mockDeleteRole = roleService.deleteRole as jest.MockedFunction<typeof roleService.deleteRole>
const mockAssignPermissions = roleService.assignPermissions as jest.MockedFunction<typeof roleService.assignPermissions>
const mockRemovePermissions = roleService.removePermissions as jest.MockedFunction<typeof roleService.removePermissions>

jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}))

const mockToast = toast as jest.Mocked<typeof toast>

describe('useRoles', () => {
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

  describe('useRoles', () => {
    const mockSystemRoles = [
      { id: '1', name: 'ROLE_ADMIN', isSystem: true, tenantId: null },
      { id: '2', name: 'ROLE_MANAGER', isSystem: true, tenantId: null }
    ]

    const mockTenantRoles = [
      { id: '3', name: 'CUSTOM_ROLE', isSystem: false, tenantId: 'tenant1' }
    ]

    it('should fetch all roles for system admin', async () => {
      const allRoles = [...mockSystemRoles, ...mockTenantRoles]
      mockGetRoles.mockResolvedValueOnce(allRoles)

      const { result } = renderHook(() => useRoles(), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(3)
      expect(mockGetRoles).toHaveBeenCalled()
    })

    it('should filter roles for tenant admin', async () => {
      const allRoles = [
        ...mockSystemRoles,
        ...mockTenantRoles,
        { id: '4', name: 'OTHER_CUSTOM', isSystem: false, tenantId: 'tenant2' }
      ]
      mockGetRoles.mockResolvedValueOnce(allRoles)

      const { result } = renderHook(() => useRoles('tenant1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      // Should include system roles and tenant1 custom roles only
      expect(result.current.data).toHaveLength(3)
      expect(result.current.data?.some(r => r.id === '4')).toBe(false)
    })

    it('should handle fetch error', async () => {
      mockGetRoles.mockRejectedValueOnce(new Error('Failed to fetch'))

      const { result } = renderHook(() => useRoles(), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('useRole', () => {
    it('should fetch single role successfully', async () => {
      const mockRole = {
        id: '1',
        name: 'ROLE_MANAGER',
        description: 'Manager role',
        isSystem: true
      }
      mockGetRole.mockResolvedValueOnce(mockRole)

      const { result } = renderHook(() => useRole('1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.name).toBe('ROLE_MANAGER')
      expect(mockGetRole).toHaveBeenCalledWith('1')
    })

    it('should not fetch if roleId is undefined', () => {
      const { result } = renderHook(() => useRole(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockGetRole).not.toHaveBeenCalled()
    })

    it('should handle fetch error', async () => {
      mockGetRole.mockRejectedValueOnce(new Error('Role not found'))

      const { result } = renderHook(() => useRole('999'), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('usePermissions', () => {
    it('should fetch all permissions successfully', async () => {
      const mockPermissions = [
        { id: '1', name: 'USER_CREATE', category: 'USER' },
        { id: '2', name: 'SHOP_MANAGE', category: 'SHOP' }
      ]
      mockGetPermissions.mockResolvedValueOnce(mockPermissions)

      const { result } = renderHook(() => usePermissions(), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(2)
      expect(mockGetPermissions).toHaveBeenCalled()
    })
  })

  describe('useRolePermissions', () => {
    it('should fetch permissions for a role', async () => {
      const mockPermissions = [
        { id: '1', name: 'USER_CREATE' },
        { id: '2', name: 'USER_READ' }
      ]
      mockGetRolePermissions.mockResolvedValueOnce(mockPermissions)

      const { result } = renderHook(() => useRolePermissions('role1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(2)
      expect(mockGetRolePermissions).toHaveBeenCalledWith('role1')
    })

    it('should not fetch if roleId is undefined', () => {
      const { result } = renderHook(() => useRolePermissions(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(mockGetRolePermissions).not.toHaveBeenCalled()
    })
  })

  describe('useCreateRole', () => {
    it('should create role successfully', async () => {
      const newRole = {
        id: '10',
        name: 'CUSTOM_ROLE',
        description: 'Custom role'
      }
      mockCreateRole.mockResolvedValueOnce(newRole)

      const { result } = renderHook(() => useCreateRole(), { wrapper })

      await result.current.mutateAsync({
        name: 'CUSTOM_ROLE',
        description: 'Custom role',
        tenantId: 'tenant1'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Role created successfully')
      })

      expect(mockCreateRole).toHaveBeenCalled()
    })

    it('should handle create error', async () => {
      const error = { response: { data: { message: 'Role already exists' } } }
      mockCreateRole.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useCreateRole(), { wrapper })

      try {
        await result.current.mutateAsync({
          name: 'DUPLICATE',
          description: 'Test'
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Role already exists')
      })
    })
  })

  describe('useUpdateRole', () => {
    it('should update role successfully', async () => {
      const updatedRole = {
        id: '1',
        name: 'UPDATED_ROLE',
        description: 'Updated'
      }
      mockUpdateRole.mockResolvedValueOnce(updatedRole)

      const { result } = renderHook(() => useUpdateRole(), { wrapper })

      await result.current.mutateAsync({
        roleId: '1',
        data: { name: 'UPDATED_ROLE', description: 'Updated' }
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Role updated successfully')
      })

      expect(mockUpdateRole).toHaveBeenCalledWith('1', {
        name: 'UPDATED_ROLE',
        description: 'Updated'
      })
    })

    it('should handle update error', async () => {
      const error = { response: { data: { message: 'Cannot update system role' } } }
      mockUpdateRole.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useUpdateRole(), { wrapper })

      try {
        await result.current.mutateAsync({
          roleId: '1',
          data: { name: 'ADMIN' }
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Cannot update system role')
      })
    })
  })

  describe('useDeleteRole', () => {
    it('should delete role successfully', async () => {
      mockDeleteRole.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useDeleteRole(), { wrapper })

      await result.current.mutateAsync('role1')

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Role deleted successfully')
      })

      expect(mockDeleteRole).toHaveBeenCalledWith('role1')
    })

    it('should handle delete error', async () => {
      const error = { response: { data: { message: 'Role is in use' } } }
      mockDeleteRole.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useDeleteRole(), { wrapper })

      try {
        await result.current.mutateAsync('role1')
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Role is in use')
      })
    })
  })

  describe('useAssignPermissions', () => {
    it('should assign permissions successfully', async () => {
      mockAssignPermissions.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useAssignPermissions(), { wrapper })

      await result.current.mutateAsync({
        roleId: 'role1',
        permissionIds: ['perm1', 'perm2']
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Permissions assigned successfully')
      })

      expect(mockAssignPermissions).toHaveBeenCalledWith('role1', ['perm1', 'perm2'])
    })

    it('should handle assign error', async () => {
      const error = { response: { data: { message: 'Invalid permissions' } } }
      mockAssignPermissions.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useAssignPermissions(), { wrapper })

      try {
        await result.current.mutateAsync({
          roleId: 'role1',
          permissionIds: ['invalid']
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Invalid permissions')
      })
    })
  })

  describe('useRemovePermissions', () => {
    it('should remove permissions successfully', async () => {
      mockRemovePermissions.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useRemovePermissions(), { wrapper })

      await result.current.mutateAsync({
        roleId: 'role1',
        permissionIds: ['perm1']
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Permissions removed successfully')
      })

      expect(mockRemovePermissions).toHaveBeenCalledWith('role1', ['perm1'])
    })

    it('should handle remove error', async () => {
      const error = { response: { data: { message: 'Permission not found' } } }
      mockRemovePermissions.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useRemovePermissions(), { wrapper })

      try {
        await result.current.mutateAsync({
          roleId: 'role1',
          permissionIds: ['perm1']
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith('Permission not found')
      })
    })
  })
})
