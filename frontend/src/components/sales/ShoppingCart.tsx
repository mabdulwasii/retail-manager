import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { NumericInput } from '@/components/ui/numeric-input'
import { Badge } from '@/components/ui/badge'
import { CartItem } from '@/hooks/useSales'
import { useCurrency } from '@/hooks/useCurrency'
import { ShoppingCartIcon, MinusIcon, PlusIcon, TrashIcon, XIcon } from 'lucide-react'

interface ShoppingCartProps {
  items: CartItem[]
  summary: {
    subtotal: number
    taxAmount: number
    total: number
    itemCount: number
    formattedSubtotal: string
    formattedTaxAmount: string
    formattedTotal: string
  }
  onUpdateQuantity: (productId: string, quantity: number) => void
  onRemoveItem: (productId: string) => void
  onClearCart: () => void
}

export const ShoppingCart: React.FC<ShoppingCartProps> = ({
  items,
  summary,
  onUpdateQuantity,
  onRemoveItem,
  onClearCart
}) => {
  const { formatCurrency } = useCurrency()

  const handleQuantityChange = (productId: string, newQuantity: string) => {
    const quantity = parseInt(newQuantity, 10)
    if (!isNaN(quantity) && quantity >= 0) {
      onUpdateQuantity(productId, quantity)
    }
  }

  const incrementQuantity = (productId: string, currentQuantity: number, maxStock: number) => {
    if (currentQuantity < maxStock) {
      onUpdateQuantity(productId, currentQuantity + 1)
    }
  }

  const decrementQuantity = (productId: string, currentQuantity: number) => {
    if (currentQuantity > 1) {
      onUpdateQuantity(productId, currentQuantity - 1)
    }
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="flex items-center space-x-2">
          <ShoppingCartIcon className="h-5 w-5" />
          <span>Shopping Cart</span>
          {items.length > 0 && (
            <Badge variant="secondary">{summary.itemCount}</Badge>
          )}
        </CardTitle>
        {items.length > 0 && (
          <Button
            variant="outline"
            size="sm"
            onClick={onClearCart}
            className="text-red-600 hover:text-red-700"
          >
            <XIcon className="h-4 w-4 mr-1" />
            Clear
          </Button>
        )}
      </CardHeader>
      <CardContent>
        {items.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <ShoppingCartIcon className="h-12 w-12 mx-auto mb-2 text-muted-foreground/40" />
            <p>Cart is empty</p>
            <p className="text-sm">Add products to get started</p>
          </div>
        ) : (
          <div className="space-y-4">
            {/* Cart Items */}
            <div className="space-y-3 max-h-64 overflow-y-auto">
              {items.map((item) => (
                <div key={item.product.id} className="border rounded-lg p-3 bg-card">
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <h4 className="font-medium text-foreground truncate">
                        {item.product.name}
                      </h4>
                      <p className="text-sm text-muted-foreground truncate">
                        {formatCurrency(item.unitPrice)} each
                      </p>
                      {item.product.taxRate && (
                        <p className="text-xs text-muted-foreground">
                          Tax: {item.product.taxRate}%
                        </p>
                      )}
                    </div>

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onRemoveItem(item.product.id)}
                      className="text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 hover:bg-red-50 dark:hover:bg-red-950/20"
                    >
                      <TrashIcon className="h-4 w-4" />
                    </Button>
                  </div>

                  <div className="flex items-center justify-between mt-3">
                    {/* Quantity Controls */}
                    <div className="flex items-center space-x-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => decrementQuantity(item.product.id, item.quantity)}
                        disabled={item.quantity <= 1}
                        className="h-8 w-8 p-0"
                      >
                        <MinusIcon className="h-3 w-3" />
                      </Button>

                      <NumericInput
                        value={item.quantity}
                        onValueChange={(values) => {
                          handleQuantityChange(item.product.id, values.value || '1')
                        }}
                        className="w-16 h-8 text-center"
                        isNumberInput={true}
                        allowNegative={false}
                        decimalScale={0}
                        isAllowed={(values) => {
                          const { floatValue } = values
                          return floatValue === undefined || (floatValue >= 1 && floatValue <= (item.product.availableStock || 9999))
                        }}
                      />

                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => incrementQuantity(item.product.id, item.quantity, item.product.availableStock || 9999)}
                        disabled={item.quantity >= (item.product.availableStock || 9999)}
                        className="h-8 w-8 p-0"
                      >
                        <PlusIcon className="h-3 w-3" />
                      </Button>
                    </div>

                    {/* Item Total */}
                    <div className="text-right">
                      <div className="font-medium text-foreground">
                        {formatCurrency(item.total)}
                      </div>
                      {item.taxAmount > 0 && (
                        <div className="text-xs text-muted-foreground">
                          incl. {formatCurrency(item.taxAmount)} tax
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Stock Warning */}
                  {item.product.availableStock && item.quantity >= item.product.availableStock && (
                    <div className="mt-2 text-xs text-orange-600 dark:text-orange-400">
                      Maximum stock quantity reached
                    </div>
                  )}
                </div>
              ))}
            </div>

            {/* Cart Summary */}
            <div className="border-t pt-3 space-y-2">
              <div className="flex justify-between text-sm">
                <span>Subtotal:</span>
                <span>{summary.formattedSubtotal}</span>
              </div>

              {summary.taxAmount > 0 && (
                <div className="flex justify-between text-sm">
                  <span>Tax:</span>
                  <span>{summary.formattedTaxAmount}</span>
                </div>
              )}

              <div className="flex justify-between font-semibold text-lg border-t pt-2">
                <span>Total:</span>
                <span className="text-green-600">{summary.formattedTotal}</span>
              </div>

              <div className="text-center text-xs text-gray-500">
                {summary.itemCount} item{summary.itemCount !== 1 ? 's' : ''} in cart
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export default ShoppingCart