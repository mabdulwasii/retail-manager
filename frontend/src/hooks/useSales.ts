import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '@/context/AuthContext'
import { useCurrency } from './useCurrency'

export interface Product {
  id: string
  name: string
  description?: string
  price: number
  barcode?: string
  category: string
  stock: number
  isActive: boolean
  imageUrl?: string
  taxRate?: number
}

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
  customerId?: string
  items: Array<{
    productId: string
    quantity: number
    unitPrice: number
  }>
  paymentMethod: string
  discountAmount?: number
  notes?: string
}

export interface SalesFilter {
  startDate?: string
  endDate?: string
  customerId?: string
  status?: string
  paymentMethod?: string
  minAmount?: number
  maxAmount?: number
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

      const response = await fetch(`/api/products/search?q=${encodeURIComponent(query)}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to search products')
      }

      const data = await response.json()
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

      const response = await fetch(`/api/products/barcode/${encodeURIComponent(barcode)}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        if (response.status === 404) {
          return null
        }
        throw new Error('Failed to find product by barcode')
      }

      const product = await response.json()
      return product
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  // Cart management
  const addToCart = useCallback((product: Product, quantity: number = 1) => {
    if (quantity <= 0) return
    if (quantity > product.stock) {
      setError(`Only ${product.stock} items available in stock`)
      return
    }

    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.product.id === product.id)

      if (existingItem) {
        const newQuantity = existingItem.quantity + quantity
        if (newQuantity > product.stock) {
          setError(`Only ${product.stock} items available in stock`)
          return prevCart
        }

        return prevCart.map(item =>
          item.product.id === product.id
            ? {
                ...item,
                quantity: newQuantity,
                subtotal: product.price * newQuantity,
                taxAmount: (product.price * newQuantity) * (product.taxRate || 0) / 100,
                total: (product.price * newQuantity) * (1 + (product.taxRate || 0) / 100)
              }
            : item
        )
      }

      const subtotal = product.price * quantity
      const taxAmount = subtotal * (product.taxRate || 0) / 100
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
          if (quantity > item.product.stock) {
            setError(`Only ${item.product.stock} items available in stock`)
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

      const response = await fetch('/api/sales', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(saleData)
      })

      if (!response.ok) {
        throw new Error('Failed to process sale')
      }

      const sale = await response.json()

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

  // Sales history
  const fetchSales = useCallback(async (filter?: SalesFilter): Promise<SalesTransaction[]> => {
    try {
      setIsLoading(true)
      setError(null)

      const queryParams = new URLSearchParams()
      if (filter?.startDate) queryParams.append('startDate', filter.startDate)
      if (filter?.endDate) queryParams.append('endDate', filter.endDate)
      if (filter?.customerId) queryParams.append('customerId', filter.customerId)
      if (filter?.status) queryParams.append('status', filter.status)
      if (filter?.paymentMethod) queryParams.append('paymentMethod', filter.paymentMethod)
      if (filter?.minAmount) queryParams.append('minAmount', filter.minAmount.toString())
      if (filter?.maxAmount) queryParams.append('maxAmount', filter.maxAmount.toString())

      const response = await fetch(`/api/sales?${queryParams}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error('Failed to fetch sales')
      }

      const salesData = await response.json()
      setSales(salesData)
      return salesData
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred'
      setError(errorMessage)
      return []
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getSaleById = useCallback(async (saleId: string): Promise<SalesTransaction | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await fetch(`/api/sales/${saleId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        if (response.status === 404) {
          return null
        }
        throw new Error('Failed to fetch sale')
      }

      const sale = await response.json()
      return sale
    } catch (err) {
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

      const response = await fetch(`/api/sales/${saleId}/receipt`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        }
      })

      if (!response.ok) {
        throw new Error('Failed to generate receipt')
      }

      const blob = await response.blob()
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