import { renderHook, act } from '@testing-library/react'
import { useSales, Product, CreateSaleRequest } from '../useSales'
import { useAuth } from '@/context/AuthContext'
import { useCurrency } from '../useCurrency'

// Mock dependencies
jest.mock('@/context/AuthContext')
jest.mock('../useCurrency')

const mockUseAuth = useAuth as jest.MockedFunction<typeof useAuth>
const mockUseCurrency = useCurrency as jest.MockedFunction<typeof useCurrency>

// Mock fetch globally
global.fetch = jest.fn()

describe('useSales', () => {
  const mockUser = {
    id: '1',
    username: 'cashier',
    email: 'cashier@example.com',
    firstName: 'John',
    lastName: 'Doe',
    roles: ['ROLE_CASHIER']
  }

  const mockProduct: Product = {
    id: '1',
    name: 'Test Product',
    description: 'Test Description',
    price: 100,
    barcode: '123456789',
    category: 'Electronics',
    stock: 50,
    isActive: true,
    taxRate: 7.5
  }

  beforeEach(() => {
    jest.clearAllMocks()

    mockUseAuth.mockReturnValue({
      user: mockUser,
      login: jest.fn(),
      logout: jest.fn(),
      isLoading: false,
      isAuthenticated: true,
    })

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
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      })

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const products = await result.current.searchProducts('test')
        expect(products).toEqual(mockResponse)
      })

      expect(fetch).toHaveBeenCalledWith(
        '/api/v1/products/search?q=test',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer mock-token',
            'Content-Type': 'application/json'
          })
        })
      )
    })

    it('should handle search errors', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 500,
      })

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const products = await result.current.searchProducts('test')
        expect(products).toEqual([])
      })

      expect(result.current.error).toContain('Failed to search products')
    })

    it('should find product by barcode', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockProduct,
      })

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const product = await result.current.findProductByBarcode('123456789')
        expect(product).toEqual(mockProduct)
      })
    })

    it('should return null for non-existent barcode', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 404,
      })

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
        result.current.addToCart(mockProduct, 2)
      })

      expect(result.current.cart).toHaveLength(1)
      expect(result.current.cart[0]).toEqual({
        product: mockProduct,
        quantity: 2,
        subtotal: 200,
        taxAmount: 15,
        total: 215
      })
    })

    it('should update quantity for existing product', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1)
        result.current.addToCart(mockProduct, 2)
      })

      expect(result.current.cart).toHaveLength(1)
      expect(result.current.cart[0].quantity).toBe(3)
      expect(result.current.cart[0].total).toBe(322.5)
    })

    it('should not add more than stock quantity', () => {
      const lowStockProduct = { ...mockProduct, stock: 5 }
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(lowStockProduct, 10)
      })

      expect(result.current.cart).toHaveLength(0)
      expect(result.current.error).toContain('Only 5 items available')
    })

    it('should remove item from cart', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1)
        result.current.removeFromCart(mockProduct.id)
      })

      expect(result.current.cart).toHaveLength(0)
    })

    it('should update cart item quantity', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2)
        result.current.updateCartItemQuantity(mockProduct.id, 5)
      })

      expect(result.current.cart[0].quantity).toBe(5)
      expect(result.current.cart[0].total).toBe(537.5)
    })

    it('should remove item when quantity is set to 0', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2)
        result.current.updateCartItemQuantity(mockProduct.id, 0)
      })

      expect(result.current.cart).toHaveLength(0)
    })

    it('should clear entire cart', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 1)
        result.current.clearCart()
      })

      expect(result.current.cart).toHaveLength(0)
    })
  })

  describe('Cart Summary', () => {
    it('should calculate cart summary correctly', () => {
      const { result } = renderHook(() => useSales())

      act(() => {
        result.current.addToCart(mockProduct, 2)
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
      }

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockSale,
      })

      const { result } = renderHook(() => useSales())

      // Add item to cart first
      act(() => {
        result.current.addToCart(mockProduct, 2)
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

      // Cart should be cleared after successful sale
      expect(result.current.cart).toHaveLength(0)
    })

    it('should handle sale processing errors', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 400,
      })

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
      const mockSales = [{
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

      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockSales,
      })

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const sales = await result.current.fetchSales()
        expect(sales).toEqual(mockSales)
      })

      expect(result.current.sales).toEqual(mockSales)
    })

    it('should fetch sales with filters', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => [],
      })

      const { result } = renderHook(() => useSales())

      const filter = {
        startDate: '2024-01-01',
        endDate: '2024-01-31',
        status: 'COMPLETED'
      }

      await act(async () => {
        await result.current.fetchSales(filter)
      })

      expect(fetch).toHaveBeenCalledWith(
        '/api/v1/sales?startDate=2024-01-01&endDate=2024-01-31&status=COMPLETED',
        expect.any(Object)
      )
    })
  })

  describe('Receipt Management', () => {
    it('should generate receipt URL', async () => {
      const mockBlob = new Blob(['receipt content'], { type: 'application/pdf' })
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        blob: async () => mockBlob,
      })

      // Mock URL.createObjectURL
      global.URL.createObjectURL = jest.fn(() => 'blob:receipt-url')

      const { result } = renderHook(() => useSales())

      await act(async () => {
        const url = await result.current.generateReceipt('1')
        expect(url).toBe('blob:receipt-url')
      })
    })

    it('should handle receipt generation errors', async () => {
      ;(global.fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 404,
      })

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
        result.current.addToCart({ ...mockProduct, stock: 1 }, 5)
      })

      expect(result.current.error).toBeTruthy()

      act(() => {
        result.current.clearError()
      })

      expect(result.current.error).toBeNull()
    })
  })
})