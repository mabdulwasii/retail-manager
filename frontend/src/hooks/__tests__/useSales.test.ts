import { renderHook, act } from '@testing-library/react'
import { useSales, Product, CreateSaleRequest } from '../useSales'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from '../useCurrency'
import { createMockAuth } from '@/test/test-utils'
import { api } from '@/services/api'

// Mock dependencies
jest.mock('../useCurrency')
jest.mock('@/services/api', () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
    getBlob: jest.fn()
  }
}))

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUseCurrency = useCurrency as jest.MockedFunction<typeof useCurrency>
const mockApi = api as jest.Mocked<typeof api>

describe('useSales', () => {
  const mockUser = {
    id: '1',
    username: 'cashier',
    email: 'cashier@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['ROLE_CASHIER'],
    shopId: 'shop1'
  }

  const mockProduct: Product = {
    id: '1',
    name: 'Test Product',
    description: 'Test Description',
    price: 100,
    barcode: '123456789',
    category: 'Electronics',
    stock: 50,
    availableStock: 50,
    isActive: true,
    taxRate: 7.5
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue(createMockAuth(mockUser))

    mockUseCurrency.mockReturnValue({
      currency: {
        code: 'NGN',
        symbol: '₦',
        name: 'Nigerian Naira',
        locale: 'en-NG',
        decimalPlaces: 2
      },
      setCurrency: jest.fn(),
      formatAmount: jest.fn((amount) => amount.toLocaleString()),
      formatCurrency: jest.fn((amount) => `₦${amount.toLocaleString()}`),
      parseCurrency: jest.fn((value) => parseFloat(value) || 0)
    })

    // Mock localStorage
    Object.defineProperty(window, 'localStorage', {
      value: {
        getItem: jest.fn(() => 'mock-token'),
        setItem: jest.fn(),
        removeItem: jest.fn(),
        clear: jest.fn(),
      },
      writable: true,
    })
  })

  describe('Product Search', () => {
    it('should search products successfully', async () => {
      const mockResponse = [mockProduct]
      mockApi.get.mockResolvedValueOnce(mockResponse)

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const products = await result.current.searchProducts('test')
        expect(products).toEqual(mockResponse)
      })
    })

    it('should handle search errors', async () => {
      mockApi.get.mockRejectedValueOnce(new Error('Failed to search products'))

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const products = await result.current.searchProducts('test')
        expect(products).toEqual([])
      })

      expect(result.current.error).toContain('Failed to search products')
    })

    it('should find product by barcode', async () => {
      mockApi.get.mockResolvedValueOnce(mockProduct)

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const product = await result.current.findProductByBarcode('123456789')
        expect(product).toEqual(mockProduct)
      })
    })

    it('should return null for non-existent barcode', async () => {
      mockApi.get.mockRejectedValueOnce({ response: { status: 404 } })

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const product = await result.current.findProductByBarcode('nonexistent')
        expect(product).toBeNull()
      })
    })
  })

  describe('Cart Management', () => {
    it('should add product to cart', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2, undefined, 100)
      })

      expect(result.current.cart).toHaveLength(1)
      expect(result.current.cart[0]).toMatchObject({
        product: mockProduct,
        quantity: 2,
        subtotal: 200,
        taxAmount: 15,
        total: 215,
        unitPrice: 100
      })
    })

    it('should update quantity for existing product', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1, undefined, 100)
        result.current.addToCart(mockProduct, 2, undefined, 100)
      })

      expect(result.current.cart).toHaveLength(1)
      expect(result.current.cart[0].quantity).toBe(3)
      expect(result.current.cart[0].total).toBe(322.5)
    })

    it('should not add more than stock quantity', () => {
      const lowStockProduct = { ...mockProduct, stock: 5, availableStock: 5 }
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(lowStockProduct, 10, undefined, 100)
      })

      expect(result.current.cart).toHaveLength(0)
      expect(result.current.error).toContain('Only 5 items available')
    })

    it('should remove item from cart', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1, undefined, 100)
        result.current.removeFromCart(mockProduct.id)
      })

      expect(result.current.cart).toHaveLength(0)
    })

    it('should update cart item quantity', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2, undefined, 100)
        result.current.updateCartItemQuantity(mockProduct.id, 5)
      })

      expect(result.current.cart[0].quantity).toBe(5)
      expect(result.current.cart[0].total).toBe(537.5)
    })

    it('should remove item when quantity is set to 0', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2, undefined, 100)
        result.current.updateCartItemQuantity(mockProduct.id, 0)
      })

      expect(result.current.cart).toHaveLength(0)
    })

    it('should clear entire cart', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1, undefined, 100)
        result.current.clearCart()
      })

      expect(result.current.cart).toHaveLength(0)
    })
  })

  describe('Cart Summary', () => {
    it('should calculate cart summary correctly', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2, undefined, 100)
      })

      const summary = result.current.cartSummary
      expect(summary.subtotal).toBe(200)
      expect(summary.taxAmount).toBe(15)
      expect(summary.total).toBe(215)
      expect(summary.itemCount).toBe(2)
    })

    it('should handle empty cart summary', () => {
      const { result } = renderHook(() => useSales())

      const summary = result.current.cartSummary
      expect(summary.subtotal).toBe(0)
      expect(summary.taxAmount).toBe(0)
      expect(summary.total).toBe(0)
      expect(summary.itemCount).toBe(0)
    })
  })

  describe('Sales Processing', () => {
    it('should process sale successfully', async () => {
      const mockSale = {
        id: '1',
        transactionNumber: 'TXN001',
        transactionDate: new Date().toISOString(),
        subtotalAmount: 200,
        taxAmount: 15,
        discountAmount: 0,
        totalAmount: 215,
        paymentMethod: 'cash',
        status: 'COMPLETED' as const,
        receiptNumber: 'R001',
        cashierId: '1',
        shopId: '1'
      }

      mockApi.post.mockResolvedValueOnce(mockSale)

      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2, undefined, 100)
      })

      const saleData: CreateSaleRequest = {
        items: [{
          productId: mockProduct.id,
          quantity: 2,
          unitPrice: mockProduct.price
        }],
        paymentMethod: 'cash'
      }

      await act(async () => {
        const sale = await result.current.processSale(saleData)
        expect(sale).toEqual(mockSale)
      })

      expect(result.current.cart).toHaveLength(0)
    })

    it('should handle sale processing errors', async () => {
      mockApi.post.mockRejectedValueOnce(new Error('Failed to process sale'))

      const { result } = renderHook(() => useSales())

      const saleData: CreateSaleRequest = {
        items: [],
        paymentMethod: 'cash'
      }

      await act(async () => {
        const sale = await result.current.processSale(saleData)
        expect(sale).toBeNull()
      })

      expect(result.current.error).toContain('Failed to process sale')
    })
  })

  describe('Sales History', () => {
    it('should fetch sales history', async () => {
      const mockSalesData = [{
        id: '1',
        transactionDate: '2024-01-01T12:00:00Z',
        items: [],
        subtotal: 200,
        taxAmount: 15,
        discountAmount: 0,
        totalAmount: 215,
        paymentMethod: 'cash',
        status: 'COMPLETED' as const,
        receiptNumber: 'R001',
        cashierId: '1',
        shopId: '1'
      }]

      const mockPagedResponse = {
        content: mockSalesData,
        totalElements: 1,
        totalPages: 1,
        size: 20,
        number: 0,
        first: true,
        last: true,
        empty: false
      }

      mockApi.get.mockResolvedValue(mockPagedResponse)

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const response = await result.current.fetchSales()
        expect(response).toEqual(mockPagedResponse)
      })

      expect(result.current.sales).toEqual(mockSalesData)
    })

    it('should fetch sales with filters', async () => {
      mockApi.get.mockResolvedValueOnce([])

      const { result } = renderHook(() => useSales())

      const filter = {
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        status: 'COMPLETED'
      }

      await act(async () => {
        await result.current.fetchSales(filter)
      })

      expect(mockApi.get).toHaveBeenCalledWith(
        '/sales',
        expect.objectContaining({
          params: expect.objectContaining({
            startDate: '2024-01-01',
            endDate: '2024-01-31',
            status: 'COMPLETED'
          })
        })
      )
    })
  })

  describe('Receipt Management', () => {
    it('should generate receipt URL', async () => {
      const mockBlob = new Blob(['receipt content'], { type: 'application/pdf' })
      mockApi.getBlob.mockResolvedValueOnce(mockBlob)

      // Mock URL.createObjectURL
      global.URL.createObjectURL = jest.fn(() => 'blob:receipt-url')

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const url = await result.current.generateReceipt('1')
        expect(url).toBe('blob:receipt-url')
      })
    })

    it('should handle receipt generation errors', async () => {
      mockApi.getBlob.mockRejectedValueOnce(new Error('Failed to generate receipt'))

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const url = await result.current.generateReceipt('1')
        expect(url).toBeNull()
      })

      expect(result.current.error).toContain('Failed to generate receipt')
    })
  })

  describe('Error Handling', () => {
    it('should clear errors', () => {
      const { result } = renderHook(() => useSales())

      // Trigger an error by adding too many items
      act(() => {
        result.current.addToCart({ ...mockProduct, stock: 1, availableStock: 1 }, 5, undefined, 100)
      })

      expect(result.current.error).toBeTruthy()

      act(() => {
        result.current.clearError()
      })

      expect(result.current.error).toBeNull()
    })
  })
})