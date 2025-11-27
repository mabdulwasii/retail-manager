import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { productService, ProductListParams } from '@/services/productService'
import { ProductCreateRequest, ProductUpdateRequest } from '@/types/api'
import { toast } from 'sonner'
import { useAuth } from '@/context/ManualAuthContext'

/**
 * Hook to fetch paginated product list with filters
 */
export const useProducts = (params: ProductListParams = {}) => {
  const { user } = useAuth()
  const shopId = user?.shopId || params.shopId

  const {
    data: productsData,
    isLoading,
    error,
    refetch
  } = useQuery({
    queryKey: ['products', shopId, params],
    queryFn: () => productService.getProducts(shopId!, params),
    enabled: !!shopId,
    staleTime: 5 * 60 * 1000, // 5 minutes
  })

  return {
    products: productsData?.content || [],
    totalPages: productsData?.totalPages || 0,
    totalElements: productsData?.totalElements || 0,
    currentPage: productsData?.number || 0,
    pageSize: productsData?.size || 20,
    isFirst: productsData?.first ?? true,
    isLast: productsData?.last ?? true,
    isLoading,
    error,
    refetch
  }
}

/**
 * Hook to fetch a single product by ID
 */
export const useProduct = (productId: string | undefined) => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: () => productService.getProduct(productId!),
    enabled: !!productId,
    staleTime: 5 * 60 * 1000,
  })
}

/**
 * Hook to fetch product categories
 * @deprecated Use useCategories or useCategoryNames from @/hooks/useCategories instead
 * This hook is kept for backward compatibility with existing components
 */
export const useProductCategories = () => {
  const { user } = useAuth()
  const shopId = user?.shopId

  return useQuery({
    queryKey: ['productCategories', shopId],
    queryFn: async () => {
      // Fetch from the proper categories endpoint
      const { categoryService } = await import('@/services/categoryService')
      return categoryService.getCategoryNames(shopId!)
    },
    enabled: !!shopId,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

/**
 * Hook to create a new product
 */
export const useCreateProduct = () => {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const shopId = user?.shopId

  return useMutation({
    mutationFn: (data: ProductCreateRequest) => {
      if (!shopId) throw new Error('Shop ID not found')
      return productService.createProduct(shopId, data)
    },
    onSuccess: (newProduct) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      queryClient.invalidateQueries({ queryKey: ['productCategories'] })
      queryClient.invalidateQueries({ queryKey: ['categoryNames'] })
      toast.success('Product created successfully', {
        description: `${newProduct.name} has been added to your catalog`
      })
      return newProduct
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to create product'
      toast.error('Failed to create product', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to update an existing product
 */
export const useUpdateProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ productId, data }: { productId: string; data: ProductUpdateRequest }) =>
      productService.updateProduct(productId, data),
    onSuccess: (updatedProduct, variables) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      queryClient.invalidateQueries({ queryKey: ['product', variables.productId] })
      queryClient.invalidateQueries({ queryKey: ['productCategories'] })
      queryClient.invalidateQueries({ queryKey: ['categoryNames'] })
      toast.success('Product updated successfully', {
        description: `${updatedProduct.name} has been updated`
      })
      return updatedProduct
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to update product'
      toast.error('Failed to update product', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to delete a product
 */
export const useDeleteProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (productId: string) => productService.deleteProduct(productId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      toast.success('Product deleted successfully')
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to delete product'
      toast.error('Failed to delete product', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to update product status
 */
export const useUpdateProductStatus = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ productId, status }: { productId: string; status: string }) =>
      productService.updateProductStatus(productId, status),
    onSuccess: (updatedProduct, variables) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      queryClient.invalidateQueries({ queryKey: ['product', variables.productId] })
      toast.success('Product status updated', {
        description: `Status changed to ${updatedProduct.status}`
      })
      return updatedProduct
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to update product status'
      toast.error('Failed to update product status', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to search products
 * @param query - Search query string
 * @param shopId - Optional shop ID to search in (defaults to user's shop)
 */
export const useProductSearch = (query: string, shopId?: string) => {
  const { user } = useAuth()
  const effectiveShopId = shopId || user?.shopId

  return useQuery({
    queryKey: ['productSearch', effectiveShopId, query],
    queryFn: () => productService.searchProducts(effectiveShopId!, query),
    enabled: !!effectiveShopId && query.length >= 2,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}
