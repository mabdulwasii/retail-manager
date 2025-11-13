import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from './useCurrency'
import { api } from '@/services/api'
import { Product } from '@/types/api'

// export interface Product {
//   id: string
//   name: string
//   description?: string
//   price: number
//   barcode?: string
//   category: string
//   stock: number
//   isActive: boolean
//   imageUrl?: string
//   taxRate?: number
// }

export interface CartItem {
  product: Product
  quantity: number
  subtotal: number
  taxAmount: number
  total: number
}

export interface SalesTransaction {
  id: string
  transactionDate: string
  customerId?: string
  customerName?: string
  items: CartItem[]
  subtotal: number
  taxAmount: number
  discountAmount: number
  totalAmount: number
  paymentMethod: string
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED'
  receiptNumber: string
  cashierId: string
  shopId: string
}

export interface CreateSaleRequest {
  paymentMethod: string;
  discountAmount?: number;
  notes?: string;
  shopId: string;
  customerName: string;
  customerPhone: string;
  customerEmail: string;
  lineItems: Array<{
    productId: string;
    quantity: number;
    unitPrice: number;
    discount: number;
  }>;
  taxAmount: number;
  paymentReference: string;
}

export interface SalesFilter {
  shopId?: string
  startDate?: string
  endDate?: string
  customerId?: string
  status?: string
  paymentMethod?: string
  minAmount?: number
  maxAmount?: number
  page?: number
  size?: number
  sort?: string
}

export interface PagedSalesResponse {
  content: SalesTransaction[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export const useSales = () => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const [cart, setCart] = useState<CartItem[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [sales, setSales] = useState<SalesTransaction[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Product search and management
  const searchProducts = useCallback(async (query: string): Promise<Product[]> => {
    try {
      setIsLoading(true)
      setError(null)

      const data = await api.get<Product[]>(`/products/search`, {
        params: { q: query }
      })
      
      setProducts(data)
      return data
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  const findProductByBarcode = useCallback(async (barcode: string): Promise<Product | null> => {
    try {
      setIsLoading(true)
      setError(null)

      if (!user?.shopId) {
        setError('Shop ID is required for barcode search')
        return null
      }

      // Use the correct API endpoint as per backend spec: /products/search?barcode=...&shopId=...
      const product = await api.get<Product>(`/products/search`, {
        params: {
          barcode: barcode,
          shopId: user.shopId,
          includeInventory: true
        }
      })
      return product
    } catch (err: any) {
      if (err?.response?.status === 404) {
        return null
      }
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [user?.shopId])

  // Cart management
  const addToCart = useCallback((product: Product, quantity: number = 1) => {
    if (quantity <= 0) return
    if (quantity > product.availableStock) {
      setError(`Only ${product.availableStock} items available in stock`)
      return
    }

    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.product.id === product.id)

      if (existingItem) {
        const newQuantity = existingItem.quantity + quantity
        if (newQuantity > product.availableStock) {
          setError(`Only ${product.availableStock} items available in stock`)
          return prevCart
        }

        return prevCart.map(item =>
          item.product.id === product.id
            ? {
                ...item,
                quantity: newQuantity,
                subtotal: product.price * newQuantity,
                taxAmount: (product.price * newQuantity) * (product?.taxRate || 0) / 100,
                total: (product.price * newQuantity) * (1 + (product?.taxRate || 0) / 100)
              }
            : item
        )
      }

      const subtotal = product.price * quantity
      const taxAmount = subtotal * (product?.taxRate || 0) / 100
      const total = subtotal + taxAmount

      return [...prevCart, {
        product,
        quantity,
        subtotal,
        taxAmount,
        total
      }]
    })

    setError(null)
  }, [])

  const removeFromCart = useCallback((productId: string) => {
    setCart(prevCart => prevCart.filter(item => item.product.id !== productId))
  }, [])

  const updateCartItemQuantity = useCallback((productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(productId)
      return
    }

    setCart(prevCart =>
      prevCart.map(item => {
        if (item.product.id === productId) {
          if (quantity > item.product.availableStock) {
            setError(`Only ${item.product.availableStock} items available in stock`)
            return item
          }

          const subtotal = item.product.price * quantity
          const taxAmount = subtotal * (item.product.taxRate || 0) / 100
          const total = subtotal + taxAmount

          return {
            ...item,
            quantity,
            subtotal,
            taxAmount,
            total
          }
        }
        return item
      })
    )

    setError(null)
  }, [removeFromCart])

  const clearCart = useCallback(() => {
    setCart([])
    setError(null)
  }, [])

  // Cart calculations
  const cartSummary = useCallback(() => {
    const subtotal = cart.reduce((sum, item) => sum + item.subtotal, 0)
    const taxAmount = cart.reduce((sum, item) => sum + item.taxAmount, 0)
    const total = cart.reduce((sum, item) => sum + item.total, 0)
    const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0)

    return {
      subtotal,
      taxAmount,
      total,
      itemCount,
      formattedSubtotal: formatCurrency(subtotal),
      formattedTaxAmount: formatCurrency(taxAmount),
      formattedTotal: formatCurrency(total)
    }
  }, [cart, formatCurrency])

  // Sales processing
  const processSale = useCallback(async (saleData: CreateSaleRequest): Promise<SalesTransaction | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const sale = await api.post<SalesTransaction>('/sales', saleData)

      // Clear cart after successful sale
      clearCart()

      // Add to sales history
      setSales(prevSales => [sale, ...prevSales])

      return sale
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [clearCart])

  // Sales history with pagination
  const fetchSales = useCallback(async (filter?: SalesFilter): Promise<PagedSalesResponse | null> => {
    try {
      setIsLoading(true)
      setError(null)

      // Build query params according to API spec
      const params: Record<string, any> = {
        page: filter?.page ?? 0,
        size: filter?.size ?? 20,
        ...(filter?.shopId && { shopId: filter.shopId }),
        ...(filter?.sort && { sort: filter.sort }),
        ...(filter?.status && { status: filter.status }),
        ...(filter?.paymentMethod && { paymentMethod: filter.paymentMethod }),
        ...(filter?.startDate && { startDate: filter.startDate }),
        ...(filter?.endDate && { endDate: filter.endDate }),
        ...(filter?.customerId && { customerId: filter.customerId }),
        ...(filter?.minAmount !== undefined && { minAmount: filter.minAmount }),
        ...(filter?.maxAmount !== undefined && { maxAmount: filter.maxAmount }),
      }

      const response = await api.get<PagedSalesResponse>('/sales', {
        params
      })
      
      // Update local state with content array
      setSales(response.content)
      return response
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getSaleById = useCallback(async (saleId: string): Promise<SalesTransaction | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const sale = await api.get<SalesTransaction>(`/sales/${saleId}`)
      return sale
    } catch (err: any) {
      if (err?.response?.status === 404) {
        return null
      }
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Receipt management
  const generateReceipt = useCallback(async (saleId: string): Promise<string | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const blob = await api.getBlob(`/sales/${saleId}/receipt`)
      const url = window.URL.createObjectURL(blob)
      return url
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const printReceipt = useCallback(async (saleId: string) => {
    const receiptUrl = await generateReceipt(saleId)
    if (receiptUrl) {
      const printWindow = window.open(receiptUrl)
      if (printWindow) {
        printWindow.onload = () => {
          printWindow.print()
          printWindow.close()
          window.URL.revokeObjectURL(receiptUrl)
        }
      }
    }
  }, [generateReceipt])

  // Error handling
  const clearError = useCallback(() => {
    setError(null)
  }, [])

  return {
    // State
    cart,
    products,
    sales,
    isLoading,
    error,
    user,

    // Product operations
    searchProducts,
    findProductByBarcode,

    // Cart operations
    addToCart,
    removeFromCart,
    updateCartItemQuantity,
    clearCart,
    cartSummary: cartSummary(),

    // Sales operations
    processSale,
    fetchSales,
    getSaleById,

    // Receipt operations
    generateReceipt,
    printReceipt,

    // Utility
    clearError
  }
}

export default useSales