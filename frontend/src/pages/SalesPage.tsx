import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { ProductSearch } from '@/components/sales/ProductSearch'
import { ShoppingCart } from '@/components/sales/ShoppingCart'
import { PaymentModal } from '@/components/sales/PaymentModal'
import { SalesHistory } from '@/components/sales/SalesHistory'
import { useSales } from '@/hooks/useSales'
import { ShoppingCartIcon, ScanIcon, HistoryIcon, CreditCardIcon, ExternalLink } from 'lucide-react'
import { useAuth } from '@/context/ManualAuthContext'
import { useNavigate } from 'react-router-dom'

export const SalesPage: React.FC = () => {
  const navigate = useNavigate()
  const {
    cart,
    cartSummary,
    sales,
    fetchSales,
    isLoading,
    error,
    addToCart,
    removeFromCart,
    updateCartItemQuantity,
    clearCart,
    findProductByBarcode,
    processSale,
    clearError
  } = useSales()

  const [activeTab, setActiveTab] = useState<'pos' | 'history'>('pos')
  const [barcodeInput, setBarcodeInput] = useState('')
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false)
  const { user } = useAuth()

  useEffect(() => {
    if (activeTab === 'history') {
      fetchSales()
    }
  }, [activeTab, fetchSales])

  useEffect(() => {
    if (!error) return

    const timer = setTimeout(() => {
      clearError()
    }, 5000)
    
    return () => clearTimeout(timer)
  }, [error, clearError])

  const handleBarcodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!barcodeInput.trim()) return

    const product = await findProductByBarcode(barcodeInput.trim())
    if (product) {
      addToCart(product, 1)
      setBarcodeInput('')
    }
  }

  const handleCheckout = () => {
    if (cart.length === 0) return
    setIsPaymentModalOpen(true)
  }

  const handlePaymentComplete = async (paymentData: any) => {
    const saleData = {
      lineItems: cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity,
        unitPrice: item.product.price,
        discount: 0
      })),
      paymentMethod: paymentData.method,
      discountAmount: paymentData.discount || 0,
      notes: paymentData.notes,
      shopId: user?.shopId as string,
      customerName: paymentData.customerName,
      customerPhone: paymentData.customerPhone,
      customerEmail: paymentData.customerEmail,
      taxAmount: cartSummary.taxAmount,
      paymentReference: paymentData.paymentReference
    }

    console.log('saleData', saleData);
    

    const sale = await processSale(saleData)
    if (sale) {
      setIsPaymentModalOpen(false)
      // Show success message or redirect
    }
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Sales Point</h1>
          <p className="text-gray-600">Process sales and manage transactions</p>
        </div>
        <div className="flex space-x-2">
          <Button
            onClick={() => navigate('/pos')}
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all duration-200"
            size="lg"
          >
            <ExternalLink className="h-5 w-5 mr-2" />
            <span className="font-semibold">Open POS Terminal</span>
          </Button>
          <Button
            variant={activeTab === 'pos' ? 'default' : 'outline'}
            onClick={() => setActiveTab('pos')}
            className="flex items-center space-x-2"
          >
            <ShoppingCartIcon className="h-4 w-4" />
            <span>Point of Sale</span>
          </Button>
          <Button
            variant={activeTab === 'history' ? 'default' : 'outline'}
            onClick={() => setActiveTab('history')}
            className="flex items-center space-x-2"
          >
            <HistoryIcon className="h-4 w-4" />
            <span>Sales History</span>
          </Button>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error}</p>
            </div>
            <div className="ml-auto pl-3">
              <button
                onClick={clearError}
                className="text-red-400 hover:text-red-600"
              >
                <span className="sr-only">Dismiss</span>
                <svg className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'pos' ? (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column - Product Search and Barcode Scanner */}
          <div className="lg:col-span-2 space-y-6">
            {/* Barcode Scanner */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <ScanIcon className="h-5 w-5" />
                  <span>Barcode Scanner</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleBarcodeSubmit} className="flex space-x-2">
                  <Input
                    type="text"
                    placeholder="Scan or enter barcode..."
                    value={barcodeInput}
                    onChange={(e) => setBarcodeInput(e.target.value)}
                    className="flex-1"
                    autoFocus
                  />
                  <Button type="submit" disabled={!barcodeInput.trim() || isLoading}>
                    {isLoading ? <LoadingSpinner size="sm" /> : 'Add'}
                  </Button>
                </form>
              </CardContent>
            </Card>

            {/* Product Search */}
            <ProductSearch onProductSelect={(product) => addToCart(product, 1)} />
          </div>

          {/* Right Column - Shopping Cart */}
          <div className="space-y-6">
            <ShoppingCart
              items={cart}
              summary={cartSummary}
              onUpdateQuantity={updateCartItemQuantity}
              onRemoveItem={removeFromCart}
              onClearCart={clearCart}
            />

            {/* Checkout Actions */}
            <Card>
              <CardContent className="p-4">
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span className="text-lg font-semibold">Total:</span>
                    <span className="text-2xl font-bold text-green-600">
                      {cartSummary.formattedTotal}
                    </span>
                  </div>

                  <Button
                    onClick={handleCheckout}
                    disabled={cart.length === 0 || isLoading}
                    className="w-full"
                    size="lg"
                  >
                    <CreditCardIcon className="h-5 w-5 mr-2" />
                    {cart.length === 0 ? 'Cart is Empty' : 'Proceed to Payment'}
                  </Button>

                  {cart.length > 0 && (
                    <div className="text-center text-sm text-gray-500">
                      <Badge variant="secondary">
                        {cartSummary.itemCount} item{cartSummary.itemCount !== 1 ? 's' : ''}
                      </Badge>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      ) : (
        <SalesHistory transactions={sales} />
      )}

      {/* Payment Modal */}
      <PaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        onPaymentComplete={handlePaymentComplete}
        cartSummary={cartSummary}
        isLoading={isLoading}
      />
    </div>
  )
}

export default SalesPage