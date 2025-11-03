import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { categoryService, CategoryCreateRequest, CategoryUpdateRequest } from '@/services/categoryService'
import { toast } from 'sonner'
import { useAuth } from '@/context/ManualAuthContext'

/**
 * Hook to fetch categories for a shop
 * @param tree - Whether to fetch hierarchical tree structure
 */
export const useCategories = (tree: boolean = false) => {
  const { user } = useAuth()
  const shopId = user?.shopId

  return useQuery({
    queryKey: ['categories', shopId, tree],
    queryFn: () => categoryService.getCategories(shopId!, tree),
    enabled: !!shopId,
    staleTime: 10 * 60 * 1000, // 10 minutes
  })
}

/**
 * Hook to fetch a single category by ID
 */
export const useCategory = (categoryId: string | undefined) => {
  return useQuery({
    queryKey: ['category', categoryId],
    queryFn: () => categoryService.getCategory(categoryId!),
    enabled: !!categoryId,
    staleTime: 10 * 60 * 1000,
  })
}

/**
 * Hook to fetch category names only (for dropdowns)
 */
export const useCategoryNames = () => {
  const { user } = useAuth()
  const shopId = user?.shopId

  return useQuery({
    queryKey: ['categoryNames', shopId],
    queryFn: () => categoryService.getCategoryNames(shopId!),
    enabled: !!shopId,
    staleTime: 10 * 60 * 1000,
  })
}

/**
 * Hook to create a new category
 */
export const useCreateCategory = () => {
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const shopId = user?.shopId

  return useMutation({
    mutationFn: (data: CategoryCreateRequest) => {
      if (!shopId) throw new Error('Shop ID not found')
      return categoryService.createCategory(shopId, data)
    },
    onSuccess: (newCategory) => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      queryClient.invalidateQueries({ queryKey: ['categoryNames'] })
      toast.success('Category created successfully', {
        description: `${newCategory.name} has been added`
      })
      return newCategory
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to create category'
      toast.error('Failed to create category', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to update an existing category
 */
export const useUpdateCategory = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ categoryId, data }: { categoryId: string; data: CategoryUpdateRequest }) =>
      categoryService.updateCategory(categoryId, data),
    onSuccess: (updatedCategory, variables) => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      queryClient.invalidateQueries({ queryKey: ['category', variables.categoryId] })
      queryClient.invalidateQueries({ queryKey: ['categoryNames'] })
      toast.success('Category updated successfully', {
        description: `${updatedCategory.name} has been updated`
      })
      return updatedCategory
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to update category'
      toast.error('Failed to update category', {
        description: message
      })
      throw error
    }
  })
}

/**
 * Hook to delete a category
 */
export const useDeleteCategory = () => {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (categoryId: string) => categoryService.deleteCategory(categoryId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories'] })
      queryClient.invalidateQueries({ queryKey: ['categoryNames'] })
      toast.success('Category deleted successfully')
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to delete category'
      toast.error('Failed to delete category', {
        description: message
      })
      throw error
    }
  })
}
