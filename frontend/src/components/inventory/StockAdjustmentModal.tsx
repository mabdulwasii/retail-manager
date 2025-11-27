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
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useInventory, AdjustStockRequest } from '@/hooks/useInventory'
import {
  PlusIcon,
  MinusIcon,
  EditIcon,
  PackageIcon,
  AlertTriangleIcon
} from 'lucide-react'

interface StockAdjustmentModalProps {
  isOpen: boolean
  onClose: () => void
  inventoryId: string
  onAdjustmentComplete: () => void
}

export const StockAdjustmentModal: React.FC<StockAdjustmentModalProps> = ({
  isOpen,
  onClose,
  inventoryId,
  onAdjustmentComplete
}) => {
  const { adjustStock, isLoading } = useInventory()
  const [adjustmentType, setAdjustmentType] = useState<'set' | 'add' | 'remove'>('set')
  const [quantity, setQuantity] = useState('')
  const [reason, setReason] = useState('')
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})

  const handleClose = () => {
    if (!isLoading) {
      onClose()
      // Reset form
      setAdjustmentType('set')
      setQuantity('')
      setReason('')
      setValidationErrors({})
    }
  }

  const validateForm = () => {
    const errors: Record<string, string> = {}

    if (!quantity || quantity.trim() === '') {
      errors.quantity = 'Quantity is required'
    } else {
      const qty = parseInt(quantity, 10)
      if (isNaN(qty) || qty < 0) {
        errors.quantity = 'Quantity must be a non-negative number'
      }
    }

    if (!reason || reason.trim() === '') {
      errors.reason = 'Reason is required'
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    const qty = parseInt(quantity, 10)
    let newStock: number

    // For demonstration, we'll assume current stock is 0
    // In a real app, you'd get this from the inventory item
    const currentStock = 0

    switch (adjustmentType) {
      case 'set':
        newStock = qty
        break
      case 'add':
        newStock = currentStock + qty
        break
      case 'remove':
        newStock = Math.max(0, currentStock - qty)
        break
      default:
        newStock = qty
    }

    const request: AdjustStockRequest = {
      newStock,
      reason: reason.trim(),
      changeType: adjustmentType === 'add' ? 'STOCK_IN' :
                 adjustmentType === 'remove' ? 'STOCK_OUT' : 'ADJUSTMENT'
    }

    const success = await adjustStock(inventoryId, request)
    if (success) {
      onAdjustmentComplete()
    }
  }

  const getAdjustmentTypeIcon = (type: string) => {
    switch (type) {
      case 'add':
        return <PlusIcon className="h-4 w-4" />
      case 'remove':
        return <MinusIcon className="h-4 w-4" />
      default:
        return <EditIcon className="h-4 w-4" />
    }
  }

  const getAdjustmentTypeDescription = (type: string) => {
    switch (type) {
      case 'set':
        return 'Set the exact stock quantity'
      case 'add':
        return 'Add to current stock (stock in)'
      case 'remove':
        return 'Remove from current stock (stock out)'
      default:
        return ''
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <PackageIcon className="h-5 w-5" />
            <span>Adjust Stock Level</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Adjustment Type */}
          <div className="space-y-3">
            <Label>Adjustment Type</Label>
            <div className="grid grid-cols-3 gap-2">
              {[
                { value: 'set', label: 'Set To', color: 'blue' },
                { value: 'add', label: 'Add', color: 'green' },
                { value: 'remove', label: 'Remove', color: 'red' }
              ].map((type) => (
                <button
                  key={type.value}
                  type="button"
                  onClick={() => setAdjustmentType(type.value as any)}
                  className={`p-3 rounded-lg border-2 text-center transition-colors ${
                    adjustmentType === type.value
                      ? `border-${type.color}-500 bg-${type.color}-50`
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <div className="flex justify-center mb-1">
                    {getAdjustmentTypeIcon(type.value)}
                  </div>
                  <div className="text-sm font-medium">{type.label}</div>
                </button>
              ))}
            </div>
            <p className="text-xs text-gray-600">
              {getAdjustmentTypeDescription(adjustmentType)}
            </p>
          </div>

          {/* Quantity Input */}
          <div className="space-y-2">
            <Label htmlFor="quantity">
              Quantity {adjustmentType === 'set' ? '(New Total)' : '(Amount)'}
            </Label>
            <NumericInput
              id="quantity"
              value={quantity}
              onValueChange={(values) => {
                setQuantity(values.value || '')
              }}
              placeholder="Enter quantity..."
              className={validationErrors.quantity ? 'border-red-500' : ''}
              isNumberInput={true}
              allowNegative={false}
              decimalScale={0}
            />
            {validationErrors.quantity && (
              <p className="text-sm text-red-600">{validationErrors.quantity}</p>
            )}
          </div>

          {/* Reason */}
          <div className="space-y-2">
            <Label htmlFor="reason">Reason for Adjustment</Label>
            <Textarea
              id="reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Explain why you're adjusting the stock..."
              rows={3}
              className={validationErrors.reason ? 'border-red-500' : ''}
            />
            {validationErrors.reason && (
              <p className="text-sm text-red-600">{validationErrors.reason}</p>
            )}
          </div>

          {/* Warning for stock removal */}
          {adjustmentType === 'remove' && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
              <div className="flex items-start space-x-2">
                <AlertTriangleIcon className="h-5 w-5 text-yellow-600 mt-0.5" />
                <div className="text-sm text-yellow-800">
                  <p className="font-medium">Stock Reduction Warning</p>
                  <p>Removing stock will reduce available inventory. Make sure this adjustment is accurate.</p>
                </div>
              </div>
            </div>
          )}

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
              disabled={isLoading || !quantity || !reason}
              className={`min-w-32 ${
                adjustmentType === 'add' ? 'bg-green-600 hover:bg-green-700' :
                adjustmentType === 'remove' ? 'bg-red-600 hover:bg-red-700' :
                'bg-blue-600 hover:bg-blue-700'
              }`}
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Adjusting...</span>
                </>
              ) : (
                <>
                  {getAdjustmentTypeIcon(adjustmentType)}
                  <span className="ml-2">
                    {adjustmentType === 'set' ? 'Set Stock' :
                     adjustmentType === 'add' ? 'Add Stock' : 'Remove Stock'}
                  </span>
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default StockAdjustmentModal