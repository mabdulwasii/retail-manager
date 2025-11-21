import React, { useState, useCallback, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { ProductSearch } from '@/components/sales/ProductSearch'
import { ShoppingCart } from '@/components/sales/ShoppingCart'
import { PaymentModal } from '@/components/sales/PaymentModal'
import { useSales, Product, CreateSaleRequest } from '@/hooks/useSales'
import { useAuth } from '@/context/ManualAuthContext'
import { toast } from 'sonner'
import {
  ShoppingCartIcon,
  AlertCircle,
  CheckCircle2,
  Printer,
  X,
  ScanBarcode
} from 'lucide-react'

export const POSPage: React.FC = () => {
  const { user } = useAuth()
  const {
    cart,
    cartSummary,
    addToCart,
    updateCartItemQuantity,
    removeFromCart,
    clearCart,
    processSale,
    printReceipt,
    findProductByBarcode,
    isLoading,
    error,
    clearError
  } = useSales()

  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false)
  const [lastSaleId, setLastSaleId] = useState<string | null>(null)
  const [barcodeInput, setBarcodeInput] = useState('')
  const [isBarcodeMode, setIsBarcodeMode] = useState(false)

  // Barcode scanner handler
  useEffect(() => {
    let barcodeBuffer = ''
    let barcodeTimeout: NodeJS.Timeout

    const handleKeyPress = (e: KeyboardEvent) => {
      // If user is typing in an input field, ignore
      if (e.target instanceof HTMLInputElement && !isBarcodeMode) return

      // Enter key completes barcode
      if (e.key === 'Enter' && barcodeBuffer.length > 0) {
        handleBarcodeScanned(barcodeBuffer)
        barcodeBuffer = ''
        return
      }

      // Build barcode buffer (only alphanumeric)
      if (e.key.length === 1 && /[a-zA-Z0-9]/.test(e.key)) {
        barcodeBuffer += e.key
        
        // Clear buffer after 100ms of inactivity
        clearTimeout(barcodeTimeout)
        barcodeTimeout = setTimeout(() => {
          barcodeBuffer = ''
        }, 100)
      }
    }

    window.addEventListener('keypress', handleKeyPress)
    return () => {
      window.removeEventListener('keypress', handleKeyPress)
      clearTimeout(barcodeTimeout)
    }
  }, [isBarcodeMode])

  const handleBarcodeScanned = async (barcode: string) => {
    const product = await findProductByBarcode(barcode)
    if (product) {
      handleProductSelect(product)
      toast.success(`${product.name} added to cart`)
    } else {
      toast.error(`No product found with barcode: ${barcode}`)
    }
    setBarcodeInput('')
  }

  const handleProductSelect = (product: Product) => {
    addToCart(product, 1)
  }

  const handleCheckout = () => {
    if (cart.length === 0) {
      toast.error('Please add items to cart before checkout')
      return
    }
    setIsPaymentModalOpen(true)
  }

  const handlePaymentComplete = async (paymentData: any) => {
    const saleData: CreateSaleRequest = {
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

    

    const sale = await processSale(saleData)

    if (sale) {
      setLastSaleId(sale.id)
      setIsPaymentModalOpen(false)
      
      toast.success(`Sale completed! Receipt #${sale.receiptNumber}`)

      // Ask if they want to print receipt
      const shouldPrint = window.confirm('Print receipt?')
      if (shouldPrint) {
        await printReceipt(sale.id)
      }
    } else {
      toast.error(error || 'Failed to process sale')
    }
  }

  const handleClearCart = () => {
    if (cart.length === 0) return
    
    const confirmed = window.confirm('Clear all items from cart?')
    if (confirmed) {
      clearCart()
      toast.success('Cart cleared - all items removed')
    }
  }

  const cartItemCount = cartSummary.itemCount
  const isCartEmpty = cart.length === 0

  return (
    <div className="h-full flex flex-col">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold">Point of Sale</h1>
          <p className="text-muted-foreground mt-1">
            Cashier: {user?.firstName} {user?.lastName}
          </p>
        </div>
        
        <div className="flex items-center gap-4">
          {/* Barcode Scanner Toggle */}
          <Button
            variant={isBarcodeMode ? 'default' : 'outline'}
            onClick={() => setIsBarcodeMode(!isBarcodeMode)}
            className="gap-2"
          >
            <ScanBarcode className="w-4 h-4" />
            Barcode Mode {isBarcodeMode && '(Active)'}
          </Button>

          {/* Cart Badge */}
          <div className="relative">
            <ShoppingCartIcon className="w-6 h-6 text-muted-foreground" />
            {cartItemCount > 0 && (
              <Badge 
                variant="destructive" 
                className="absolute -top-2 -right-2 h-5 w-5 flex items-center justify-center p-0"
              >
                {cartItemCount}
              </Badge>
            )}
          </div>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive" className="mb-4">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription className="flex items-center justify-between">
            <span>{error}</span>
            <Button variant="ghost" size="sm" onClick={clearError}>
              <X className="h-4 w-4" />
            </Button>
          </AlertDescription>
        </Alert>
      )}

      {/* Success Message */}
      {lastSaleId && !isLoading && (
        <Alert className="mb-4 border-green-500 bg-green-50">
          <CheckCircle2 className="h-4 w-4 text-green-500" />
          <AlertDescription className="flex items-center justify-between text-green-700">
            <span>Sale completed successfully!</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => lastSaleId && printReceipt(lastSaleId)}
            >
              <Printer className="h-4 w-4 mr-2" />
              Print Receipt
            </Button>
          </AlertDescription>
        </Alert>
      )}

      {/* Main Content - Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 overflow-hidden">
        {/* Left Column - Product Search (2/3 width) */}
        <div className="lg:col-span-2 overflow-auto">
          <ProductSearch onProductSelect={handleProductSelect} />
        </div>

        {/* Right Column - Shopping Cart (1/3 width) */}
        <div className="lg:col-span-1 flex flex-col">
          <Card className="flex-1 flex flex-col">
            <CardHeader className="pb-4">
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <ShoppingCartIcon className="w-5 h-5" />
                  Cart ({cartItemCount} items)
                </span>
                {!isCartEmpty && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleClearCart}
                    className="text-destructive hover:text-destructive"
                  >
                    Clear
                  </Button>
                )}
              </CardTitle>
            </CardHeader>

            <CardContent className="flex-1 flex flex-col p-0">
              {/* Cart Items */}
              <div className="flex-1 overflow-auto px-6">
                {isCartEmpty ? (
                  <div className="flex flex-col items-center justify-center h-full text-center py-12">
                    <ShoppingCartIcon className="w-16 h-16 text-muted-foreground/20 mb-4" />
                    <p className="text-muted-foreground font-medium">Cart is empty</p>
                    <p className="text-sm text-muted-foreground mt-1">
                      Search and add products to get started
                    </p>
                  </div>
                ) : (
                  <ShoppingCart
                    items={cart}
                    summary={cartSummary}
                    onUpdateQuantity={updateCartItemQuantity}
                    onRemoveItem={removeFromCart}
                    onClearCart={handleClearCart}
                  />
                )}
              </div>

              {/* Cart Summary & Checkout */}
              {!isCartEmpty && (
                <>
                  <Separator className="my-4" />
                  
                  <div className="px-6 pb-6">
                    {/* Summary */}
                    <div className="space-y-2 mb-4">
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Subtotal</span>
                        <span className="font-medium">{cartSummary.formattedSubtotal}</span>
                      </div>
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Tax</span>
                        <span className="font-medium">{cartSummary.formattedTaxAmount}</span>
                      </div>
                      <Separator className="my-2" />
                      <div className="flex justify-between text-lg font-bold">
                        <span>Total</span>
                        <span className="text-primary">{cartSummary.formattedTotal}</span>
                      </div>
                    </div>

                    {/* Checkout Button */}
                    <Button
                      size="lg"
                      className="w-full"
                      onClick={handleCheckout}
                      disabled={isLoading || isCartEmpty}
                    >
                      {isLoading ? 'Processing...' : 'Proceed to Payment'}
                    </Button>
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {/* Quick Stats */}
          <div className="grid grid-cols-2 gap-2 mt-4">
            <Card className="p-3">
              <p className="text-xs text-muted-foreground">Items</p>
              <p className="text-2xl font-bold">{cartItemCount}</p>
            </Card>
            <Card className="p-3">
              <p className="text-xs text-muted-foreground">Total</p>
              <p className="text-2xl font-bold text-primary">{cartSummary.formattedTotal}</p>
            </Card>
          </div>
        </div>
      </div>

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

export default POSPage
