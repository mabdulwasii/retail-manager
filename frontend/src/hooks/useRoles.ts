import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import roleService from '@/services/roleService'
import {
  RoleCreateRequest,
  RoleUpdateRequest,
} from '@/types/role'

// Query keys
export const roleKeys = {
  all: ['roles'] as const,
  lists: () => [...roleKeys.all, 'list'] as const,
  list: () => [...roleKeys.lists()] as const,
  details: () => [...roleKeys.all, 'detail'] as const,
  detail: (id: string) => [...roleKeys.details(), id] as const,
  permissions: (id: string) => [...roleKeys.detail(id), 'permissions'] as const,
}

export const permissionKeys = {
  all: ['permissions'] as const,
  lists: () => [...permissionKeys.all, 'list'] as const,
  list: () => [...permissionKeys.lists()] as const,
}

/**
 * Fetch all roles
 */
export const useRoles = () => {
  return useQuery({
    queryKey: roleKeys.list(),
    queryFn: () => roleService.getRoles(),
  })
}

/**
 * Fetch a single role by ID
 */
export const useRole = (roleId: string | undefined) => {
  return useQuery({
    queryKey: roleKeys.detail(roleId || ''),
    queryFn: () => roleService.getRole(roleId!),
    enabled: !!roleId,
  })
}

/**
 * Fetch all available permissions
 */
export const usePermissions = () => {
  return useQuery({
    queryKey: permissionKeys.list(),
    queryFn: () => roleService.getPermissions(),
  })
}

/**
 * Fetch permissions for a specific role
 */
export const useRolePermissions = (roleId: string | undefined) => {
  return useQuery({
    queryKey: roleKeys.permissions(roleId || ''),
    queryFn: () => roleService.getRolePermissions(roleId!),
    enabled: !!roleId,
  })
}

/**
 * Create a new role
 */
export const useCreateRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: RoleCreateRequest) => roleService.createRole(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: roleKeys.lists() })
      toast.success('Role created successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to create role')
    },
  })
}

/**
 * Update an existing role
 */
export const useUpdateRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ roleId, data }: { roleId: string; data: RoleUpdateRequest }) =>
      roleService.updateRole(roleId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleKeys.lists() })
      queryClient.invalidateQueries({ queryKey: roleKeys.detail(variables.roleId) })
      toast.success('Role updated successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to update role')
    },
  })
}

/**
 * Delete a role
 */
export const useDeleteRole = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (roleId: string) => roleService.deleteRole(roleId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: roleKeys.lists() })
      toast.success('Role deleted successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to delete role')
    },
  })
}

/**
 * Assign permissions to a role
 */
export const useAssignPermissions = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: string; permissionIds: string[] }) =>
      roleService.assignPermissions(roleId, permissionIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleKeys.detail(variables.roleId) })
      queryClient.invalidateQueries({ queryKey: roleKeys.permissions(variables.roleId) })
      toast.success('Permissions assigned successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to assign permissions')
    },
  })
}

/**
 * Remove permissions from a role
 */
export const useRemovePermissions = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ roleId, permissionIds }: { roleId: string; permissionIds: string[] }) =>
      roleService.removePermissions(roleId, permissionIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: roleKeys.detail(variables.roleId) })
      queryClient.invalidateQueries({ queryKey: roleKeys.permissions(variables.roleId) })
      toast.success('Permissions removed successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to remove permissions')
    },
  })
}
