import React from 'react'
import { renderHook, act, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useProducts,
  useProduct,
  useCreateProduct,
  useUpdateProduct,
  useDeleteProduct,
  useUpdateProductStatus,
  useProductSearch
} from '../useProducts'
import { productService } from '@/services/productService'
import { toast } from 'sonner'

// Mock dependencies
jest.mock('@/services/productService', () => ({
  productService: {
    getProducts: jest.fn(),
    getProduct: jest.fn(),
    createProduct: jest.fn(),
    updateProduct: jest.fn(),
    deleteProduct: jest.fn(),
    updateProductStatus: jest.fn(),
    searchProducts: jest.fn()
  }
}))

jest.mock('sonner', () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn()
  }
}))

jest.mock('@/context/UnifiedAuthContext', () => ({
  useAuth: () => ({
    user: {
      id: '1',
      shopId: 'shop-1',
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_MANAGER']
    }
  })
}))

const mockProductService = productService as jest.Mocked<typeof productService>

describe('useProducts', () => {
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

  describe('useProducts', () => {
    it('should fetch products successfully', async () => {
      const mockData = {
        content: [
          { id: '1', name: 'Product 1', price: 100, barcode: '123', category: 'Electronics', stock: 10 },
          { id: '2', name: 'Product 2', price: 200, barcode: '456', category: 'Books', stock: 5 }
        ],
        totalPages: 1,
        totalElements: 2,
        number: 0,
        size: 20,
        first: true,
        last: true
      }

      mockProductService.getProducts.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useProducts(), { wrapper })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.products).toHaveLength(2)
      expect(result.current.totalPages).toBe(1)
      expect(result.current.totalElements).toBe(2)
      expect(mockProductService.getProducts).toHaveBeenCalledWith('shop-1', {})
    })

    it('should handle empty product list', async () => {
      mockProductService.getProducts.mockResolvedValueOnce({
        content: [],
        totalPages: 0,
        totalElements: 0,
        number: 0,
        size: 20,
        first: true,
        last: true
      })

      const { result } = renderHook(() => useProducts(), { wrapper })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.products).toHaveLength(0)
    })
  })

  describe('useProduct', () => {
    it('should fetch single product successfully', async () => {
      const mockProduct = {
        id: '1',
        name: 'Test Product',
        price: 100,
        barcode: '123',
        category: 'Electronics',
        stock: 10
      }

      mockProductService.getProduct.mockResolvedValueOnce(mockProduct)

      const { result } = renderHook(() => useProduct('1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toEqual(mockProduct)
      expect(mockProductService.getProduct).toHaveBeenCalledWith('1')
    })

    it('should not fetch if productId is undefined', () => {
      const { result } = renderHook(() => useProduct(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(mockProductService.getProduct).not.toHaveBeenCalled()
    })
  })

  describe('useCreateProduct', () => {
    it('should create product successfully', async () => {
      const mockNewProduct = {
        id: '3',
        name: 'New Product',
        price: 150,
        barcode: '789',
        category: 'Clothing',
        stock: 20
      }

      mockProductService.createProduct.mockResolvedValueOnce(mockNewProduct)

      const { result } = renderHook(() => useCreateProduct(), { wrapper })

      await act(async () => {
        await result.current.mutateAsync({
          name: 'New Product',
          price: 150,
          barcode: '789',
          category: 'Clothing',
          stock: 20
        })
      })

      expect(mockProductService.createProduct).toHaveBeenCalledWith('shop-1', {
        name: 'New Product',
        price: 150,
        barcode: '789',
        category: 'Clothing',
        stock: 20
      })
      expect(toast.success).toHaveBeenCalledWith('Product created successfully', {
        description: 'New Product has been added to your catalog'
      })
    })

    it('should handle create error', async () => {
      const error = {
        response: { data: { message: 'Product already exists' } }
      }
      mockProductService.createProduct.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useCreateProduct(), { wrapper })

      try {
        await act(async () => {
          await result.current.mutateAsync({
            name: 'Duplicate Product',
            price: 100,
            barcode: '123',
            category: 'Test',
            stock: 10
          })
        })
      } catch (e) {
        // Expected error
      }

      expect(toast.error).toHaveBeenCalledWith('Failed to create product', {
        description: 'Product already exists'
      })
    })
  })

  describe('useUpdateProduct', () => {
    it('should update product successfully', async () => {
      const mockUpdatedProduct = {
        id: '1',
        name: 'Updated Product',
        price: 200,
        barcode: '123',
        category: 'Electronics',
        stock: 15
      }

      mockProductService.updateProduct.mockResolvedValueOnce(mockUpdatedProduct)

      const { result } = renderHook(() => useUpdateProduct(), { wrapper })

      await act(async () => {
        await result.current.mutateAsync({
          productId: '1',
          data: { name: 'Updated Product', price: 200 }
        })
      })

      expect(mockProductService.updateProduct).toHaveBeenCalledWith('1', {
        name: 'Updated Product',
        price: 200
      })
      expect(toast.success).toHaveBeenCalledWith('Product updated successfully', {
        description: 'Updated Product has been updated'
      })
    })
  })

  describe('useDeleteProduct', () => {
    it('should delete product successfully', async () => {
      mockProductService.deleteProduct.mockResolvedValueOnce(undefined)

      const { result } = renderHook(() => useDeleteProduct(), { wrapper })

      await act(async () => {
        await result.current.mutateAsync('1')
      })

      expect(mockProductService.deleteProduct).toHaveBeenCalledWith('1')
      expect(toast.success).toHaveBeenCalledWith('Product deleted successfully')
    })

    it('should handle delete error', async () => {
      const error = {
        response: { data: { message: 'Cannot delete product with active inventory' } }
      }
      mockProductService.deleteProduct.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useDeleteProduct(), { wrapper })

      try {
        await act(async () => {
          await result.current.mutateAsync('1')
        })
      } catch (e) {
        // Expected error
      }

      expect(toast.error).toHaveBeenCalledWith('Failed to delete product', {
        description: 'Cannot delete product with active inventory'
      })
    })
  })

  describe('useUpdateProductStatus', () => {
    it('should update product status successfully', async () => {
      const mockUpdatedProduct = {
        id: '1',
        name: 'Test Product',
        status: 'INACTIVE',
        price: 100,
        barcode: '123',
        category: 'Electronics',
        stock: 10
      }

      mockProductService.updateProductStatus.mockResolvedValueOnce(mockUpdatedProduct)

      const { result } = renderHook(() => useUpdateProductStatus(), { wrapper })

      await act(async () => {
        await result.current.mutateAsync({ productId: '1', status: 'INACTIVE' })
      })

      expect(mockProductService.updateProductStatus).toHaveBeenCalledWith('1', 'INACTIVE')
      expect(toast.success).toHaveBeenCalledWith('Product status updated', {
        description: 'Status changed to INACTIVE'
      })
    })
  })

  describe('useProductSearch', () => {
    it('should search products successfully', async () => {
      const mockResults = [
        { id: '1', name: 'Laptop', price: 1000 },
        { id: '2', name: 'Laptop Charger', price: 50 }
      ]

      mockProductService.searchProducts.mockResolvedValueOnce(mockResults)

      const { result } = renderHook(() => useProductSearch('Laptop'), { wrapper })

      await waitFor(() => {
        expect(result.current.isLoading).toBe(false)
      })

      expect(result.current.data).toEqual(mockResults)
      expect(mockProductService.searchProducts).toHaveBeenCalledWith('shop-1', 'Laptop')
    })

    it('should not search with query less than 2 characters', () => {
      const { result } = renderHook(() => useProductSearch('L'), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(mockProductService.searchProducts).not.toHaveBeenCalled()
    })
  })
})
