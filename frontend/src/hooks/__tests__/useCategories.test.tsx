/* eslint-disable @typescript-eslint/no-var-requires */
import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useCategories,
  useCategory,
  useCategoryNames,
  useCreateCategory,
  useUpdateCategory,
  useDeleteCategory
} from '../useCategories'
import { getMockCategory, getMockCategories, getMockCategoryTree, getMockCategoryNames } from '@/testData/categories'
import { Category, categoryService } from '@/services/categoryService'
import { toast } from 'sonner'


jest.mock('@/services/categoryService', () => ({
  categoryService: {
    getCategories: jest.fn(),
    getCategory: jest.fn(),
    getCategoryNames: jest.fn(),
    createCategory: jest.fn(),
    updateCategory: jest.fn(),
    deleteCategory: jest.fn()
  }
}))

jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}))

jest.mock('@/context/ManualAuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'user1',
      username: 'manager',
      email: 'manager@example.com',
      roles: ['ROLE_MANAGER'],
      shopId: 'shop1'
    },
    isAuthenticated: true
  })
}))

const mockCategoryService = categoryService as jest.Mocked<typeof categoryService>
const mockToast = toast as jest.Mocked<typeof toast>

describe('useCategories', () => {
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

  describe('useCategories', () => {
    it('should fetch flat categories list successfully', async () => {
      // Using test data from @/testData/categories
      const mockData = getMockCategories() as unknown as Category[]
      mockCategoryService.getCategories.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useCategories(false), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(4)
      expect(result.current.data?.[0].name).toBe('Electronics')
      expect(mockCategoryService.getCategories).toHaveBeenCalledWith('shop1', false)
    })

    it('should fetch hierarchical category tree', async () => {
      // Using test data factory for tree structure
      const mockTree = getMockCategoryTree() as unknown as Category[]
      mockCategoryService.getCategories.mockResolvedValueOnce(mockTree)

      const { result } = renderHook(() => useCategories(true), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(2)
      expect(result.current.data?.[0].children).toBeDefined()
      expect(mockCategoryService.getCategories).toHaveBeenCalledWith('shop1', true)
    })

    it('should use shopId parameter over user.shopId', async () => {
      const mockData = getMockCategories() as unknown as Category[]
      mockCategoryService.getCategories.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useCategories(false, 'shop2'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(mockCategoryService.getCategories).toHaveBeenCalledWith('shop2', false)
    })

    it('should not fetch if shopId is not available', () => {
      // Override useAuth mock temporarily
      jest.spyOn(require('@/context/ManualAuthContext'), 'useAuth').mockReturnValueOnce({
        user: null,
        isAuthenticated: false
      })

      const { result } = renderHook(() => useCategories(false), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockCategoryService.getCategories).not.toHaveBeenCalled()
    })

    it('should handle fetch error', async () => {
      mockCategoryService.getCategories.mockRejectedValueOnce(new Error('Failed to fetch'))

      const { result } = renderHook(() => useCategories(false), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })

      expect(result.current.error).toBeTruthy()
    })
  })

  describe('useCategory', () => {
    it('should fetch single category successfully', async () => {
      // Using test data factory
      const mockCategory = getMockCategory() as unknown as Category
      mockCategoryService.getCategory.mockResolvedValueOnce(mockCategory)

      const { result } = renderHook(() => useCategory('cat1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBe('cat1')
      expect(result.current.data?.name).toBe('Electronics')
      expect(mockCategoryService.getCategory).toHaveBeenCalledWith('cat1')
    })

    it('should not fetch if categoryId is undefined', () => {
      const { result } = renderHook(() => useCategory(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
      expect(mockCategoryService.getCategory).not.toHaveBeenCalled()
    })

    it('should handle 404 error', async () => {
      mockCategoryService.getCategory.mockRejectedValueOnce(new Error('Category not found'))

      const { result } = renderHook(() => useCategory('invalid'), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })
    })
  })

  describe('useCategoryNames', () => {
    it('should fetch category names for dropdown', async () => {
      // Using test data factory for names
      const mockNames = getMockCategoryNames() as unknown as string[]
      mockCategoryService.getCategoryNames.mockResolvedValueOnce(mockNames)

      const { result } = renderHook(() => useCategoryNames(), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(4)
      expect(result.current.data?.[0]).toEqual({ id: 'cat1', name: 'Electronics' })
      expect(mockCategoryService.getCategoryNames).toHaveBeenCalledWith('shop1')
    })

    it('should use shopId parameter', async () => {
      const mockNames = getMockCategoryNames() as unknown as string[]
      mockCategoryService.getCategoryNames.mockResolvedValueOnce(mockNames)

      const { result } = renderHook(() => useCategoryNames('shop2'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(mockCategoryService.getCategoryNames).toHaveBeenCalledWith('shop2')
    })
  })

  describe('useCreateCategory', () => {
    it('should create category successfully', async () => {
      // Using test data factory with custom properties
      const newCategory = getMockCategory({ id: 'cat5', name: 'New Category' })
      mockCategoryService.createCategory.mockResolvedValueOnce(newCategory as unknown as Category)

      const { result } = renderHook(() => useCreateCategory(), { wrapper })

      await result.current.mutateAsync({
        name: 'New Category',
        description: 'Test description',
        shopId: 'shop1'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          'Category created successfully',
          { description: 'New Category has been added' }
        )
      })

      expect(mockCategoryService.createCategory).toHaveBeenCalledWith('shop1', {
        name: 'New Category',
        description: 'Test description',
        shopId: 'shop1'
      })
    })

    it('should throw error if shopId is missing', async () => {
      const { result } = renderHook(() => useCreateCategory(), { wrapper })

      await expect(
        result.current.mutateAsync({
          name: 'Test',
          description: 'Test'
        } as unknown as Category)
      ).rejects.toThrow('Shop ID is required')
    })

    it('should handle create error', async () => {
      const error = { response: { data: { message: 'Category already exists' } } }
      mockCategoryService.createCategory.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useCreateCategory(), { wrapper })

      try {
        await result.current.mutateAsync({
          name: 'Duplicate',
          shopId: 'shop1'
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to create category',
          { description: 'Category already exists' }
        )
      })
    })
  })

  describe('useUpdateCategory', () => {
    it('should update category successfully', async () => {
      // Using test data factory with updated properties
      const updatedCategory = getMockCategory({ name: 'Updated Electronics' })
      mockCategoryService.updateCategory.mockResolvedValueOnce(updatedCategory as unknown as Category)

      const { result } = renderHook(() => useUpdateCategory(), { wrapper })

      await result.current.mutateAsync({
        categoryId: 'cat1',
        data: { name: 'Updated Electronics' }
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          'Category updated successfully',
          { description: 'Updated Electronics has been updated' }
        )
      })

      expect(mockCategoryService.updateCategory).toHaveBeenCalledWith('cat1', {
        name: 'Updated Electronics'
      })
    })

    it('should handle update error', async () => {
      const error = { response: { data: { message: 'Cannot update system category' } } }
      mockCategoryService.updateCategory.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useUpdateCategory(), { wrapper })

      try {
        await result.current.mutateAsync({
          categoryId: 'cat1',
          data: { name: 'Test' }
        })
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to update category',
          { description: 'Cannot update system category' }
        )
      })
    })
  })

  describe('useDeleteCategory', () => {
    it('should delete category successfully', async () => {
      mockCategoryService.deleteCategory.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useDeleteCategory(), { wrapper })

      await result.current.mutateAsync('cat1')

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith('Category deleted successfully')
      })

      expect(mockCategoryService.deleteCategory).toHaveBeenCalledWith('cat1')
    })

    it('should handle delete error', async () => {
      const error = { response: { data: { message: 'Category has products' } } }
      mockCategoryService.deleteCategory.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useDeleteCategory(), { wrapper })

      try {
        await result.current.mutateAsync('cat1')
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          'Failed to delete category',
          { description: 'Category has products' }
        )
      })
    })
  })
})
