import React, { useState, useCallback, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import { ProductSearch } from '@/components/sales/ProductSearch'
import { ShoppingCart } from '@/components/sales/ShoppingCart'
import { PaymentModal } from '@/components/sales/PaymentModal'
import { useSales, CreateSaleRequest } from '@/hooks/useSales'
import { Product } from '@/types/api'
import { useAuth } from '@/context/ManualAuthContext'
import { toast } from 'sonner'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { usePDFReceipt } from '@/hooks/usePDFReceipt'
import {
  ShoppingCartIcon,
  AlertCircle,
  CheckCircle2,
  Printer,
  X,
  ScanBarcode
} from 'lucide-react'
import { ShopSelector } from '@/components/ui/shop-selector'

export const POSPage: React.FC = () => {
  const { user } = useAuth()
  
  // Selected shop state - default to user's shop or first available
  const [selectedShopId, setSelectedShopId] = useState<string>(user?.shopId || '')
  
  const {
    cart,
    cartSummary,
    addToCart,
    updateCartItemQuantity,
    removeFromCart,
    clearCart,
    processSale,
    findProductByBarcode,
    isLoading,
    error,
    clearError
  } = useSales()

  // PDF Receipt hook
  const { printReceiptByTransactionId } = usePDFReceipt()

  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false)
  const [lastSaleId, setLastSaleId] = useState<string | null>(null)
  const [barcodeInput, setBarcodeInput] = useState('')
  const [isBarcodeMode, setIsBarcodeMode] = useState(false)
  
  // Update selected shop when user shop changes
  useEffect(() => {
    if (user?.shopId && !selectedShopId) {
      setSelectedShopId(user.shopId)
    }
  }, [user?.shopId])

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
    // Note: Barcode lookup doesn't include inventory pricing from backend
    // Users should use the search box to find and add items with correct pricing
    toast.error(`Barcode scanning temporarily disabled. Please use search box to find "${barcode}"`)
    setBarcodeInput('')
    
    // TODO: Update when backend returns inventory pricing in barcode search
    // const product = await findProductByBarcode(barcode)
    // if (product) {
    //   handleProductSelect(product, inventoryId, sellingPrice)
    //   toast.success(`${product.name} added to cart`)
    // } else {
    //   toast.error(`No product found with barcode: ${barcode}`)
    // }
  }

  const handleProductSelect = (product: Product, inventoryId: string, sellingPrice: number) => {
    if (!sellingPrice) {
      toast.error('Product price not available. Please ensure inventory is set up.')
      return
    }
    
    addToCart(product, 1, inventoryId, sellingPrice)
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
        unitPrice: item.unitPrice,  // Use inventory-based selling price
        discount: 0
      })),
      paymentMethod: paymentData.method,
      discountAmount: paymentData.discount || 0,
      notes: paymentData.notes,
      shopId: selectedShopId || user?.shopId as string,
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
      
      toast.success(`Sale completed! Receipt #${sale.receiptNumber || sale.transactionNumber}`)

      // Ask if they want to print receipt
      const shouldPrint = window.confirm('Print receipt?')
      if (shouldPrint) {
        await printReceiptByTransactionId(sale.id, {
          shopAddress: 'Shop Address Here', // TODO: Get from shop settings
          shopPhone: 'Shop Phone Here', // TODO: Get from shop settings
          shopEmail: 'shop@email.com', // TODO: Get from shop settings
        })
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
    <div className="h-full flex flex-col bg-gradient-to-br from-background to-muted/30">
      {/* Header */}
      <div className="flex items-center justify-between mb-6 bg-card/80 backdrop-blur-sm rounded-xl p-6 shadow-lg border">
        <div>
          <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-purple-600 dark:from-primary dark:to-purple-400 bg-clip-text text-transparent">Point of Sale</h1>
          <p className="text-muted-foreground mt-1 flex items-center gap-2">
            <span className="inline-block w-2 h-2 bg-green-500 dark:bg-green-400 rounded-full animate-pulse"></span>
            <span className="text-sm">Cashier: {user?.firstName} {user?.lastName}</span>
          </p>
        </div>
        
        <div className="flex items-center gap-3">
          {/* Shop Selector */}
          <ShopSelector
            value={selectedShopId}
            onValueChange={setSelectedShopId}
            className="w-[200px]"
            placeholder="Select shop"
          />
          
          {/* Barcode Scanner Toggle */}
          <Button
            variant={isBarcodeMode ? 'default' : 'outline'}
            onClick={() => setIsBarcodeMode(!isBarcodeMode)}
            className="gap-2"
            size="sm"
          >
            <ScanBarcode className="w-4 h-4" />
            <span className="hidden sm:inline">{isBarcodeMode ? 'Scanning...' : 'Scan Mode'}</span>
          </Button>

          {/* Cart Badge */}
          <div className="relative p-2">
            <ShoppingCartIcon className="w-5 h-5 text-muted-foreground" />
            {cartItemCount > 0 && (
              <Badge 
                className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 bg-primary text-primary-foreground"
              >
                {cartItemCount}
              </Badge>
            )}
          </div>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive" className="mb-4 animate-in fade-in slide-in-from-top-2">
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
        <Alert className="mb-4 border-green-500 bg-green-500/10 dark:bg-green-500/20 animate-in fade-in slide-in-from-top-2">
          <CheckCircle2 className="h-4 w-4 text-green-600 dark:text-green-400" />
          <AlertDescription className="flex items-center justify-between text-green-700 dark:text-green-300">
            <span>Sale completed successfully!</span>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => lastSaleId && printReceiptByTransactionId(lastSaleId, {
                shopAddress: 'Shop Address Here',
                shopPhone: 'Shop Phone Here',
                shopEmail: 'shop@email.com',
              })}
              className="hover:bg-green-500/20"
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
          <ProductSearch 
            onProductSelect={handleProductSelect} 
            shopId={selectedShopId}
          />
        </div>

        {/* Right Column - Shopping Cart (1/3 width) */}
        <Card className="flex flex-col h-full overflow-hidden shadow-xl border-2">
          <CardHeader className="pb-4 bg-muted/30">
            <CardTitle className="flex items-center justify-between text-xl">
              <span className="flex items-center gap-2">
                <ShoppingCartIcon className="w-5 h-5 text-primary" />
                <span>Cart</span>
                <Badge variant="secondary" className="text-xs">{cartItemCount}</Badge>
              </span>
              {!isCartEmpty && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleClearCart}
                  className="text-destructive hover:text-destructive hover:bg-destructive/10"
                >
                  <X className="w-4 h-4" />
                </Button>
              )}
            </CardTitle>
          </CardHeader>

          <CardContent className="flex-1 flex flex-col p-0 overflow-hidden">
            <div className="flex-1 overflow-auto px-6">
              {isCartEmpty ? (
                <div className="flex flex-col items-center justify-center h-full text-center py-12">
                  <div className="rounded-full bg-muted p-6 mb-4">
                    <ShoppingCartIcon className="w-12 h-12 text-muted-foreground/40" />
                  </div>
                  <p className="text-muted-foreground font-medium">Cart is empty</p>
                  <p className="text-sm text-muted-foreground/70 mt-1">
                    Add products to get started
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
                <Separator />
                
                <div className="px-6 py-6 bg-muted/20">
                  {/* Summary */}
                  <div className="space-y-3 mb-6">
                    <div className="flex justify-between text-sm text-muted-foreground">
                      <span>Subtotal</span>
                      <span className="font-medium text-foreground">{cartSummary.formattedSubtotal}</span>
                    </div>

                    {cartSummary.taxAmount > 0 && (
                      <div className="flex justify-between text-sm text-muted-foreground">
                        <span>Tax</span>
                        <span className="font-medium text-foreground">{cartSummary.formattedTaxAmount}</span>
                      </div>
                    )}

                    <Separator />
                    
                    <div className="flex justify-between font-bold text-xl pt-2">
                      <span>Total</span>
                      <span className="text-primary">{cartSummary.formattedTotal}</span>
                    </div>
                  </div>

                  {/* Checkout Button */}
                  <Button
                    onClick={handleCheckout}
                    disabled={isCartEmpty || isLoading}
                    className="w-full h-14 text-lg font-semibold shadow-lg hover:shadow-xl transition-all"
                    size="lg"
                  >
                    {isLoading ? (
                      <LoadingSpinner size="sm" />
                    ) : (
                      <>
                        <CheckCircle2 className="w-5 h-5 mr-2" />
                        Checkout
                      </>
                    )}
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>
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
