import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { NumericInput } from '@/components/ui/numeric-input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useCurrency } from '@/hooks/useCurrency'
import {
  CreditCardIcon,
  BanknoteIcon,
  SmartphoneIcon,
  WalletIcon,
  CheckCircleIcon,
} from 'lucide-react'

interface PaymentModalProps {
  isOpen: boolean
  onClose: () => void
  onPaymentComplete: (paymentData: {
    method: string
    amountPaid: number
    discount: number
    notes: string
  }) => void
  cartSummary: {
    subtotal: number
    taxAmount: number
    total: number
    formattedSubtotal: string
    formattedTaxAmount: string
    formattedTotal: string
  }
  isLoading: boolean
}

const paymentMethods = [
  {
    id: 'CASH',
    name: 'Cash',
    icon: BanknoteIcon,
    description: 'Cash payment'
  },
  {
    id: 'CARD',
    name: 'Card',
    icon: CreditCardIcon,
    description: 'Credit/Debit card'
  },
  {
    id: 'MOBILE',
    name: 'Mobile Money',
    icon: SmartphoneIcon,
    description: 'Mobile payment'
  },
  {
    id: 'bank_transfer',
    name: 'Bank Transfer',
    icon: WalletIcon,
    description: 'Electronic transfer'
  }
]

export const PaymentModal: React.FC<PaymentModalProps> = ({
  isOpen,
  onClose,
  onPaymentComplete,
  cartSummary,
  isLoading
}) => {
  const { formatCurrency, parseCurrency } = useCurrency()
  const [paymentMethod, setPaymentMethod] = useState('CASH')
  const [amountPaid, setAmountPaid] = useState(cartSummary.total.toString())
  const [discount, setDiscount] = useState('0')
  const [notes, setNotes] = useState('')
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})

  const discountAmount = parseCurrency(discount) || 0
  const finalTotal = Math.max(0, cartSummary.total - discountAmount)
  const paidAmount = parseCurrency(amountPaid) || 0
  const changeAmount = Math.max(0, paidAmount - finalTotal)

  const validateForm = () => {
    const errors: Record<string, string> = {}

    if (discountAmount < 0) {
      errors.discount = 'Discount cannot be negative'
    }

    if (discountAmount > cartSummary.total) {
      errors.discount = 'Discount cannot exceed total amount'
    }

    if (paymentMethod === 'CASH') {
      if (paidAmount < finalTotal) {
        errors.amountPaid = 'Amount paid must be at least the total amount'
      }
    } else {
      if (paidAmount !== finalTotal) {
        setAmountPaid(finalTotal.toString())
      }
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handlePaymentMethodChange = (method: string) => {
    setPaymentMethod(method)

    // For non-CASH payments, amount paid should equal the final total
    if (method !== 'CASH') {
      setAmountPaid(finalTotal.toString())
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    onPaymentComplete({
      method: paymentMethod,
      amountPaid: paidAmount,
      discount: discountAmount,
      notes: notes.trim()
    })
  }

  const handleClose = () => {
    if (!isLoading) {
      onClose()
      // Reset form
      setPaymentMethod('CASH')
      setAmountPaid(cartSummary.total.toString())
      setDiscount('0')
      setNotes('')
      setValidationErrors({})
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh]">
        <DialogHeader className="space-y-3">
          <DialogTitle className="flex items-center space-x-3 text-2xl">
            <div className="p-2 rounded-lg bg-primary/10">
              <CreditCardIcon className="h-6 w-6 text-primary" />
            </div>
            <span className="bg-gradient-to-r from-primary to-purple-600 dark:from-primary dark:to-purple-400 bg-clip-text text-transparent">Complete Payment</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Order Summary */}
          <div className="bg-muted/40 dark:bg-muted/20 rounded-xl p-5 space-y-3 border">
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">Order Summary</p>
            
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Subtotal</span>
              <span className="font-medium">{cartSummary.formattedSubtotal}</span>
            </div>

            {cartSummary.taxAmount > 0 && (
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Tax</span>
                <span className="font-medium">{cartSummary.formattedTaxAmount}</span>
              </div>
            )}

            {discountAmount > 0 && (
              <div className="flex justify-between text-sm text-green-600 dark:text-green-400">
                <span>Discount</span>
                <span className="font-medium">-{formatCurrency(discountAmount)}</span>
              </div>
            )}

            <div className="flex justify-between font-bold text-xl border-t pt-3 mt-2">
              <span>Total</span>
              <span className="text-primary">{formatCurrency(finalTotal)}</span>
            </div>

            {paymentMethod === 'CASH' && changeAmount > 0 && (
              <div className="flex justify-between text-sm bg-blue-500/10 dark:bg-blue-500/20 rounded-lg p-3 mt-2">
                <span className="text-blue-700 dark:text-blue-300 font-medium">Change Due</span>
                <span className="text-blue-700 dark:text-blue-300 font-bold">{formatCurrency(changeAmount)}</span>
              </div>
            )}
          </div>

          {/* Discount */}
          <div className="space-y-2">
            <Label htmlFor="discount" className="text-sm font-semibold">Discount Amount (Optional)</Label>
            <NumericInput
              id="discount"
              value={discount}
              onValueChange={(values) => {
                setDiscount(values.value || '0')
              }}
              placeholder="0.00"
              className="h-11"
              decimalScale={2}
              fixedDecimalScale={false}
              allowNegative={false}
              isAllowed={(values) => {
                const { floatValue } = values
                return floatValue === undefined || floatValue <= cartSummary.total
              }}
            />
            {validationErrors.discount && (
              <p className="text-sm text-destructive font-medium">{validationErrors.discount}</p>
            )}
          </div>

          {/* Payment Method */}
          <div className="space-y-3">
            <Label className="text-sm font-semibold">Payment Method</Label>
            <RadioGroup value={paymentMethod} onValueChange={handlePaymentMethodChange} className="grid grid-cols-2 gap-3">
              {paymentMethods.map((method) => {
                const IconComponent = method.icon
                const isSelected = paymentMethod === method.id
                return (
                  <div key={method.id} className={`relative flex items-center space-x-3 border-2 rounded-xl p-4 cursor-pointer transition-all ${
                    isSelected 
                      ? 'border-primary bg-primary/5 shadow-md' 
                      : 'border-border hover:border-primary/50 hover:bg-muted/50'
                  }`}>
                    <RadioGroupItem value={method.id} id={method.id} className="sr-only" />
                    <label htmlFor={method.id} className="flex items-center space-x-3 cursor-pointer flex-1">
                      <div className={`p-2 rounded-lg ${
                        isSelected ? 'bg-primary/20' : 'bg-muted'
                      }`}>
                        <IconComponent className={`h-5 w-5 ${
                          isSelected ? 'text-primary' : 'text-muted-foreground'
                        }`} />
                      </div>
                      <div className="flex-1">
                        <div className="font-semibold text-sm">{method.name}</div>
                        <div className="text-xs text-muted-foreground">{method.description}</div>
                      </div>
                    </label>
                    {isSelected && (
                      <CheckCircleIcon className="h-5 w-5 text-primary absolute top-2 right-2" />
                    )}
                  </div>
                )
              })}
            </RadioGroup>
          </div>

          {/* Amount Paid (for CASH payments) */}
          {paymentMethod === 'CASH' && (
            <div className="space-y-2 animate-in fade-in slide-in-from-top-2">
              <Label htmlFor="amountPaid" className="text-sm font-semibold">Amount Paid (Cash)</Label>
              <NumericInput
                id="amountPaid"
                value={amountPaid}
                onValueChange={(values) => {
                  setAmountPaid(values.value || '')
                }}
                placeholder={`Minimum: ${finalTotal.toFixed(2)}`}
                className="h-11 text-lg font-semibold"
                decimalScale={2}
                fixedDecimalScale={false}
                allowNegative={false}
                autoFocus
              />
              {validationErrors.amountPaid && (
                <p className="text-sm text-destructive font-medium">{validationErrors.amountPaid}</p>
              )}
            </div>
          )}

          {/* Notes */}
          <div className="space-y-2">
            <Label htmlFor="notes" className="text-sm font-semibold">Notes (Optional)</Label>
            <Textarea
              id="notes"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Add any additional notes about this transaction..."
              rows={3}
              className="resize-none"
            />
          </div>

          <DialogFooter className="gap-3 sm:gap-2">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
              className="flex-1 sm:flex-none h-12"
              size="lg"
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isLoading || Object.keys(validationErrors).length > 0}
              className="flex-1 sm:flex-none min-w-40 h-12 text-base font-semibold shadow-lg hover:shadow-xl transition-all"
              size="lg"
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Processing...</span>
                </>
              ) : (
                <>
                  <CheckCircleIcon className="h-5 w-5 mr-2" />
                  Complete Sale
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default PaymentModal