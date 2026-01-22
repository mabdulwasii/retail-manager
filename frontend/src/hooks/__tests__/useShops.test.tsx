import React from 'react'
import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  useShops,
  useActiveShops,
  useShopById,
  useCreateShop,
  useUpdateShop,
  useUpdateShopStatus
} from '../useShops'
import { getMockShop, getMockShopsList, getMockActiveShops } from '@/testData/shops'
import { shopService } from '@/services/shopService'
import { toast } from 'sonner'

jest.mock('@/services/shopService', () => ({
  shopService: {
    getShops: jest.fn(),
    getActiveShops: jest.fn(),
    getShopById: jest.fn(),
    createShop: jest.fn(),
    updateShop: jest.fn(),
    updateStatus: jest.fn()
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
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_ADMIN'],
      shopId: 'shop1'
    },
    isAuthenticated: true,
    hasAnyPermission: () => true
  })
}))

const mockShopService = shopService as jest.Mocked<typeof shopService>
const mockToast = toast as jest.Mocked<typeof toast>

describe('useShops', () => {
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

  describe('useShops', () => {
    it('should fetch paginated shops successfully', async () => {
      // Using test data from @/testData/shops - centralized mock data
      const mockData = getMockShopsList()
      mockShopService.getShops.mockResolvedValueOnce(mockData)

      const { result } = renderHook(() => useShops(0, 10), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.content).toHaveLength(3)
      expect(result.current.data?.totalElements).toBe(3)
      expect(result.current.data?.content[0].name).toBe('Electronics Store')
    })

    it('should handle empty shop list', async () => {
      // Test data can be customized or use factory default
      mockShopService.getShops.mockResolvedValueOnce({
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 10
      })

      const { result } = renderHook(() => useShops(0, 10), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.content).toHaveLength(0)
    })

    it('should handle pagination', async () => {
      // Using test data factory with overrides
      mockShopService.getShops.mockResolvedValueOnce({
        content: [getMockShop({ id: 'shop4', name: 'Page 2 Shop' })],
        totalElements: 4,
        totalPages: 2,
        number: 1,
        size: 20
      })

      const { result } = renderHook(() => useShops(1, 20), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.number).toBe(1)
    })

    it('should handle fetch error', async () => {
      mockShopService.getShops.mockRejectedValueOnce(new Error('Failed to fetch shops'))

      const { result } = renderHook(() => useShops(0, 10), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })

      expect(result.current.error).toBeTruthy()
    })
  })

  describe('useActiveShops', () => {
    it('should fetch active shops successfully', async () => {
      // Using test data factory from @/testData/shops
      const mockActiveShops = getMockActiveShops()
      mockShopService.getActiveShops.mockResolvedValueOnce(mockActiveShops)

      const { result } = renderHook(() => useActiveShops(), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data).toHaveLength(2)
      expect(result.current.data?.[0].status).toBe('ACTIVE')
    })
  })

  describe('useShopById', () => {
    it('should fetch single shop successfully', async () => {
      // Using test data factory from @/testData/shops
      const mockShop = getMockShop()
      mockShopService.getShopById.mockResolvedValueOnce(mockShop)

      const { result } = renderHook(() => useShopById('shop1'), { wrapper })

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true)
      })

      expect(result.current.data?.id).toBe('shop1')
      expect(result.current.data?.name).toBe('Electronics Store')
    })

    it('should not fetch if shopId is undefined', () => {
      const { result } = renderHook(() => useShopById(undefined), { wrapper })

      expect(result.current.isLoading).toBe(false)
      expect(result.current.fetchStatus).toBe('idle')
    })

    it('should handle 404 error', async () => {
      mockShopService.getShopById.mockRejectedValueOnce(new Error('Shop not found'))

      const { result } = renderHook(() => useShopById('nonexistent'), { wrapper })

      await waitFor(() => {
        expect(result.current.isError).toBe(true)
      }, { timeout: 3000 })

      expect(result.current.error).toBeTruthy()
    })
  })

  describe('useCreateShop', () => {
    it('should create shop successfully', async () => {
      // Using test data factory with custom properties
      const newShop = getMockShop({ id: 'new-shop', name: 'New Shop' })
      mockShopService.createShop.mockResolvedValueOnce(newShop)

      const { result } = renderHook(() => useCreateShop(), { wrapper })

      await result.current.mutateAsync({
        name: 'New Shop',
        address: '123 Test St',
        city: 'Lagos',
        state: 'Lagos',
        country: 'Nigeria',
        phone: '+234-123-456-7890',
        email: 'newshop@example.com'
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          'Shop created successfully',
          { description: 'New Shop has been created.' }
        )
      })
    })

    it('should handle create error', async () => {
      const error = { response: { data: { message: 'Shop already exists' } } }
      mockShopService.createShop.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useCreateShop(), { wrapper })

      try {
        await result.current.mutateAsync({
          name: 'Duplicate Shop',
          address: '123 Test St',
          city: 'Lagos',
          state: 'Lagos',
          country: 'Nigeria',
          phone: '+234-123-456-7890',
          email: 'duplicate@example.com'
        })
      } catch (e) {
        // Expected error
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled()
      })
    })
  })

  describe('useUpdateShop', () => {
    it('should update shop successfully', async () => {
      // Using test data factory with updated properties
      const updatedShop = getMockShop({ name: 'Updated Electronics Store' })
      mockShopService.updateShop.mockResolvedValueOnce(updatedShop)

      const { result } = renderHook(() => useUpdateShop(), { wrapper })

      await result.current.mutateAsync({
        shopId: 'shop1',
        data: { name: 'Updated Electronics Store' }
      })

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          'Shop updated successfully',
          { description: 'Updated Electronics Store has been updated.' }
        )
      })
    })

    it('should handle update error', async () => {
      const error = { response: { data: { message: 'Unauthorized' } } }
      mockShopService.updateShop.mockRejectedValueOnce(error)

      const { result } = renderHook(() => useUpdateShop(), { wrapper })

      try {
        await result.current.mutateAsync({
          shopId: 'shop1',
          data: { name: 'Unauthorized Update' }
        })
      } catch (e) {
        // Expected error
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled()
      })
    })
  })

  describe('useUpdateShopStatus', () => {
    it('should update shop status successfully', async () => {
      // Using test data factory with status override
      const updatedShop = getMockShop({ status: 'INACTIVE' })
      mockShopService.updateStatus.mockResolvedValueOnce(updatedShop)

      const { result } = renderHook(() => useUpdateShopStatus(), { wrapper })

      await result.current.mutateAsync({
        shopId: 'shop1',
        status: 'INACTIVE'
      })
      
      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalled()
      }, { timeout: 1000 })
    })
  })
})
