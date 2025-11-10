import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userService, type UserCreateRequest } from '@/services/userService'
import type { User } from '@/services/userService'
import { toast } from 'sonner'

interface UseShopUsersOptions {
  shopId?: string
  status?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  page?: number
  size?: number
  enabled?: boolean
}

export function useShopUsers({
  shopId,
  status = 'ACTIVE',
  page = 0,
  size = 50,
  enabled = true,
}: UseShopUsersOptions = {}) {
  return useQuery({
    queryKey: ['shop-users', shopId, status, page, size],
    queryFn: async () => {
      if (!shopId) {
        throw new Error('shopId is required')
      }
      const response = await userService.getShopUsers(shopId, status, page, size)
      return response
    },
    enabled: enabled && !!shopId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}

interface UseTenantUsersOptions {
  tenantId?: string
  enabled?: boolean
}

export function useTenantUsers({
  tenantId,
  enabled = true,
}: UseTenantUsersOptions = {}) {
  return useQuery({
    queryKey: ['tenant-users', tenantId],
    queryFn: () => {
      if (!tenantId) {
        throw new Error('tenantId is required')
      }
      return userService.getTenantUsers(tenantId)
    },
    enabled: enabled && !!tenantId,
    staleTime: 5 * 60 * 1000,
  })
}

export function useUser(userId: string, enabled = true) {
  return useQuery({
    queryKey: ['user', userId],
    queryFn: () => userService.getUserById(userId),
    enabled: !!userId && enabled,
    staleTime: 5 * 60 * 1000,
  })
}

export function useCreateUser() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ tenantId, request }: { tenantId: string; request: UserCreateRequest }) =>
      userService.createUserInTenant(tenantId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tenant-users'] })
      queryClient.invalidateQueries({ queryKey: ['shop-users'] })
      toast.success('User created successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to create user')
    },
  })
}

export type { User, UserCreateRequest }
