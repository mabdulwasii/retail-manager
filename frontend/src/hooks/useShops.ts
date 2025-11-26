import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/context/ManualAuthContext'
import { shopService, ShopCreateRequest, ShopUpdateRequest } from '@/services/shopService'
import { toast } from 'sonner'


export const useShops = (page = 0, size = 20) => {
  const { isAuthenticated, user } = useAuth()

  return useQuery({
    queryKey: ['shops', 'paginated', page, size],
    queryFn: () => shopService.getShops({ page, size }),
    enabled: !!(isAuthenticated && user?.roles && 
      user.roles.some(r => ['MANAGER', 'OWNER', 'TENANT_ADMIN'].includes(r.name))),
    staleTime: 2 * 60 * 1000, // 2 minutes
    retry: 2
  })
}


export const useActiveShops = () => {
  const { isAuthenticated, user } = useAuth()

  return useQuery({
    queryKey: ['shops', 'active'],
    queryFn: () => shopService.getActiveShops(),
    enabled: !!(isAuthenticated && user?.roles && 
      user.roles.some(r => ['MANAGER', 'OWNER', 'TENANT_ADMIN', 'CASHIER'].includes(r.name))),
    staleTime: 5 * 60 * 1000, // 5 minutes
    retry: 2
  })
}


export const useShopById = (shopId: string | undefined) => {
  const { isAuthenticated } = useAuth()

  return useQuery({
    queryKey: ['shops', shopId],
    queryFn: () => shopService.getShopById(shopId!),
    enabled: !!(isAuthenticated && shopId),
    staleTime: 3 * 60 * 1000, // 3 minutes
    retry: 2
  })
}


export const useCreateShop = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: ShopCreateRequest) => shopService.createShop(data),
    onSuccess: (newShop) => {
      queryClient.invalidateQueries({ queryKey: ['shops'] })
      toast.success('Shop created successfully', {
        description: `${newShop.name} has been created.`
      })
    },
    onError: (error: any) => {
      toast.error('Failed to create shop', {
        description: error.response?.data?.message || error.message || 'An error occurred'
      })
    }
  })
}

export const useUpdateShop = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ shopId, data }: { shopId: string; data: ShopUpdateRequest }) => {
      console.log('Updating shop:', shopId, 'with data:', data)
      return shopService.updateShop(shopId, data)
    },
    onSuccess: (updatedShop) => {
      console.log('Shop updated successfully:', updatedShop)
      queryClient.invalidateQueries({ queryKey: ['shops', updatedShop.id] })
      queryClient.invalidateQueries({ queryKey: ['shops', 'paginated'] })
      queryClient.invalidateQueries({ queryKey: ['shops', 'active'] })
      toast.success('Shop updated successfully', {
        description: `${updatedShop.name} has been updated.`
      })
    },
    onError: (error: any) => {
      console.error('Failed to update shop:', error)
      console.error('Error response:', error.response?.data)
      toast.error('Failed to update shop', {
        description: error.response?.data?.message || error.message || 'An error occurred'
      })
    }
  })
}

export const useUpdateShopStatus = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ shopId, status }: { shopId: string; status: string }) => 
      shopService.updateStatus(shopId, status),
    onSuccess: (updatedShop) => {
      // Invalidate all shop-related queries
      queryClient.invalidateQueries({ queryKey: ['shops'] })
      toast.success('Shop status updated', {
        description: `Status changed to ${updatedShop.status}`
      })
    },
    onError: (error: any) => {
      toast.error('Failed to update shop status', {
        description: error.response?.data?.message || error.message || 'An error occurred'
      })
    }
  })
}

export const useDeleteShop = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (shopId: string) => shopService.deleteShop(shopId),
    onSuccess: () => {
      // Invalidate all shop queries
      queryClient.invalidateQueries({ queryKey: ['shops'] })
      toast.success('Shop deleted successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to delete shop', {
        description: error.response?.data?.message || error.message || 'An error occurred'
      })
    }
  })
}
