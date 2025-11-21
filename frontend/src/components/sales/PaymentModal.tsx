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
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <CreditCardIcon className="h-5 w-5" />
            <span>Payment Processing</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Order Summary */}
          <div className="bg-gray-50 rounded-lg p-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span>Subtotal:</span>
              <span>{cartSummary.formattedSubtotal}</span>
            </div>

            {cartSummary.taxAmount > 0 && (
              <div className="flex justify-between text-sm">
                <span>Tax:</span>
                <span>{cartSummary.formattedTaxAmount}</span>
              </div>
            )}

            {discountAmount > 0 && (
              <div className="flex justify-between text-sm text-green-600">
                <span>Discount:</span>
                <span>-{formatCurrency(discountAmount)}</span>
              </div>
            )}

            <div className="flex justify-between font-semibold text-lg border-t pt-2">
              <span>Total:</span>
              <span>{formatCurrency(finalTotal)}</span>
            </div>

            {paymentMethod === 'CASH' && changeAmount > 0 && (
              <div className="flex justify-between text-sm text-blue-600 border-t pt-2">
                <span>Change:</span>
                <span>{formatCurrency(changeAmount)}</span>
              </div>
            )}
          </div>

          {/* Discount */}
          <div className="space-y-2">
            <Label htmlFor="discount">Discount Amount</Label>
            <Input
              id="discount"
              type="number"
              step="0.01"
              min="0"
              max={cartSummary.total}
              value={discount}
              onChange={(e) => setDiscount(e.target.value)}
              placeholder="0.00"
            />
            {validationErrors.discount && (
              <p className="text-sm text-red-600">{validationErrors.discount}</p>
            )}
          </div>

          {/* Payment Method */}
          <div className="space-y-3">
            <Label>Payment Method</Label>
            <RadioGroup value={paymentMethod} onValueChange={handlePaymentMethodChange}>
              {paymentMethods.map((method) => {
                const IconComponent = method.icon
                return (
                  <div key={method.id} className="flex items-center space-x-2 border rounded-lg p-3">
                    <RadioGroupItem value={method.id} id={method.id} />
                    <IconComponent className="h-5 w-5 text-gray-600" />
                    <label htmlFor={method.id} className="flex-1 cursor-pointer">
                      <div className="font-medium">{method.name}</div>
                      <div className="text-sm text-gray-600">{method.description}</div>
                    </label>
                  </div>
                )
              })}
            </RadioGroup>
          </div>

          {/* Amount Paid (for CASH payments) */}
          {paymentMethod === 'CASH' && (
            <div className="space-y-2">
              <Label htmlFor="amountPaid">Amount Paid</Label>
              <Input
                id="amountPaid"
                type="number"
                step="0.01"
                min={finalTotal}
                value={amountPaid}
                onChange={(e) => setAmountPaid(e.target.value)}
                placeholder={finalTotal.toString()}
              />
              {validationErrors.amountPaid && (
                <p className="text-sm text-red-600">{validationErrors.amountPaid}</p>
              )}
            </div>
          )}

          {/* Notes */}
          <div className="space-y-2">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Textarea
              id="notes"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Add any additional notes..."
              rows={3}
            />
          </div>

          <DialogFooter className="space-x-2">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isLoading || Object.keys(validationErrors).length > 0}
              className="min-w-32"
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Processing...</span>
                </>
              ) : (
                <>
                  <CheckCircleIcon className="h-4 w-4 mr-2" />
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