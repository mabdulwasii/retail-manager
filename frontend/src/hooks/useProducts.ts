import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { productService, ProductListParams } from '@/services/productService'
import { ProductCreateRequest, ProductUpdateRequest } from '@/types/api'
import { toast } from 'sonner'

/**
 * Hook to fetch paginated product list with filters
 */
export const useProducts = (params: ProductListParams = {}) => {
  const {
    data: productsData,
    isLoading,
    error,
    refetch
  } = useQuery({
    queryKey: ['products', params],
    queryFn: () => productService.getProducts(params),
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
 */
export const useProductCategories = () => {
  return useQuery({
    queryKey: ['productCategories'],
    queryFn: () => productService.getCategories(),
    staleTime: 30 * 60 * 1000, // 30 minutes
  })
}

/**
 * Hook to create a new product
 */
export const useCreateProduct = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: ProductCreateRequest) => productService.createProduct(data),
    onSuccess: (newProduct) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      queryClient.invalidateQueries({ queryKey: ['productCategories'] })
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
 */
export const useProductSearch = (query: string) => {
  return useQuery({
    queryKey: ['productSearch', query],
    queryFn: () => productService.searchProducts(query),
    enabled: query.length > 2,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}
