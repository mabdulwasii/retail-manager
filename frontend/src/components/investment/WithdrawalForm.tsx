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
import { Alert, AlertDescription } from '@/components/ui/alert'
import { useCurrency } from '@/hooks/useCurrency'
import {
  useInvestment,
  Investment,
  WithdrawalRequest
} from '@/hooks/useInvestment'
import {
  CreditCardIcon,
  DollarSignIcon,
  AlertTriangleIcon,
  InfoIcon,
  BanknoteIcon
} from 'lucide-react'

interface WithdrawalFormProps {
  investment: Investment | null
  isOpen: boolean
  onClose: () => void
  onWithdrawalProcessed: (updatedInvestment: Investment) => void
}

export const WithdrawalForm: React.FC<WithdrawalFormProps> = ({
  investment,
  isOpen,
  onClose,
  onWithdrawalProcessed
}) => {
  const { formatCurrency } = useCurrency()
  const { processWithdrawal, isLoading } = useInvestment()

  const [formData, setFormData] = useState<WithdrawalRequest>({
    amount: 0,
    reason: '',
    paymentMethod: 'BANK_TRANSFER',
    bankAccount: '',
    notes: ''
  })

  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({})

  const handleInputChange = (field: keyof WithdrawalRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear validation error when user starts typing
    if (validationErrors[field]) {
      setValidationErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  const validateForm = () => {
    const errors: Record<string, string> = {}

    if (!formData.amount || formData.amount <= 0) {
      errors.amount = 'Withdrawal amount must be greater than 0'
    }

    if (investment && formData.amount > investment.availableBalance) {
      errors.amount = `Amount cannot exceed available balance of ${formatCurrency(investment.availableBalance)}`
    }

    if (!formData.reason.trim()) {
      errors.reason = 'Withdrawal reason is required'
    }

    if (formData.paymentMethod === 'BANK_TRANSFER' && !formData.bankAccount?.trim()) {
      errors.bankAccount = 'Bank account information is required for bank transfers'
    }

    setValidationErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!investment || !validateForm()) {
      return
    }

    const result = await processWithdrawal(investment.id, formData)
    if (result) {
      onWithdrawalProcessed(result)
      handleClose()
    }
  }

  const handleClose = () => {
    if (!isLoading) {
      setFormData({
        amount: 0,
        reason: '',
        paymentMethod: 'BANK_TRANSFER',
        bankAccount: '',
        notes: ''
      })
      setValidationErrors({})
      onClose()
    }
  }

  const calculateWithdrawalFee = (amount: number) => {
    // Example fee calculation: 2% fee with minimum of ₦100
    const feePercentage = 0.02
    const minFee = 100
    const calculatedFee = amount * feePercentage
    return Math.max(calculatedFee, minFee)
  }

  const getNetAmount = () => {
    if (!formData.amount) return 0
    const fee = calculateWithdrawalFee(formData.amount)
    return formData.amount - fee
  }

  const paymentMethodOptions = [
    { value: 'BANK_TRANSFER', label: 'Bank Transfer', icon: BanknoteIcon },
    { value: 'MOBILE_MONEY', label: 'Mobile Money', icon: CreditCardIcon },
    { value: 'CASH', label: 'Cash Pickup', icon: DollarSignIcon }
  ]

  if (!investment) return null

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <CreditCardIcon className="h-5 w-5" />
            <span>Withdraw Funds</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Investment Info */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <h4 className="font-medium text-blue-900 mb-2">Investment Details</h4>
            <div className="text-sm text-blue-800 space-y-1">
              <p><strong>Investment:</strong> {investment.investmentNumber}</p>
              <p><strong>Available Balance:</strong> {formatCurrency(investment.availableBalance)}</p>
              <p><strong>Total Profit Earned:</strong> {formatCurrency(investment.totalProfitEarned)}</p>
            </div>
          </div>

          {/* Withdrawal Amount */}
          <div className="space-y-2">
            <Label htmlFor="amount">Withdrawal Amount *</Label>
            <div className="relative">
              <DollarSignIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <NumericInput
                id="amount"
                value={formData.amount || ''}
                onValueChange={(values) => {
                  handleInputChange('amount', values.floatValue || 0)
                }}
                placeholder="Enter withdrawal amount"
                className={`pl-10 ${validationErrors.amount ? 'border-red-500' : ''}`}
                decimalScale={2}
                fixedDecimalScale={true}
                isAllowed={(values) => {
                  const { floatValue } = values
                  return floatValue === undefined || (floatValue >= 1 && floatValue <= investment.availableBalance)
                }}
              />
            </div>
            {validationErrors.amount && (
              <p className="text-sm text-red-600">{validationErrors.amount}</p>
            )}
            <p className="text-xs text-gray-500">
              Maximum available: {formatCurrency(investment.availableBalance)}
            </p>
          </div>

          {/* Fee Calculation */}
          {formData.amount > 0 && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
              <div className="flex items-start space-x-2">
                <InfoIcon className="h-4 w-4 text-yellow-600 mt-0.5" />
                <div className="text-sm text-yellow-800">
                  <p className="font-medium mb-1">Withdrawal Summary</p>
                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span>Withdrawal Amount:</span>
                      <span>{formatCurrency(formData.amount)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Processing Fee (2%, min ₦100):</span>
                      <span>-{formatCurrency(calculateWithdrawalFee(formData.amount))}</span>
                    </div>
                    <div className="flex justify-between font-medium border-t pt-1">
                      <span>Net Amount:</span>
                      <span>{formatCurrency(getNetAmount())}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Payment Method */}
          <div className="space-y-3">
            <Label>Payment Method</Label>
            <div className="space-y-2">
              {paymentMethodOptions.map((option) => {
                const IconComponent = option.icon
                return (
                  <label
                    key={option.value}
                    className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                      formData.paymentMethod === option.value ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value={option.value}
                      checked={formData.paymentMethod === option.value}
                      onChange={(e) => handleInputChange('paymentMethod', e.target.value)}
                      className="text-blue-600"
                    />
                    <IconComponent className="h-4 w-4 text-gray-600" />
                    <span className="font-medium">{option.label}</span>
                  </label>
                )
              })}
            </div>
          </div>

          {/* Bank Account (if bank transfer selected) */}
          {formData.paymentMethod === 'BANK_TRANSFER' && (
            <div className="space-y-2">
              <Label htmlFor="bankAccount">Bank Account Information *</Label>
              <Textarea
                id="bankAccount"
                value={formData.bankAccount || ''}
                onChange={(e) => handleInputChange('bankAccount', e.target.value)}
                placeholder="Bank name, account number, account name..."
                rows={3}
                className={validationErrors.bankAccount ? 'border-red-500' : ''}
              />
              {validationErrors.bankAccount && (
                <p className="text-sm text-red-600">{validationErrors.bankAccount}</p>
              )}
            </div>
          )}

          {/* Withdrawal Reason */}
          <div className="space-y-2">
            <Label htmlFor="reason">Withdrawal Reason *</Label>
            <select
              id="reason"
              value={formData.reason}
              onChange={(e) => handleInputChange('reason', e.target.value)}
              className={`w-full px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                validationErrors.reason ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Select withdrawal reason</option>
              <option value="PROFIT_WITHDRAWAL">Profit Withdrawal</option>
              <option value="PARTIAL_DIVESTMENT">Partial Divestment</option>
              <option value="EMERGENCY_FUNDS">Emergency Funds</option>
              <option value="INVESTMENT_REBALANCING">Investment Rebalancing</option>
              <option value="PERSONAL_EXPENSES">Personal Expenses</option>
              <option value="OTHER">Other</option>
            </select>
            {validationErrors.reason && (
              <p className="text-sm text-red-600">{validationErrors.reason}</p>
            )}
          </div>

          {/* Additional Notes */}
          <div className="space-y-2">
            <Label htmlFor="notes">Additional Notes (Optional)</Label>
            <Textarea
              id="notes"
              value={formData.notes || ''}
              onChange={(e) => handleInputChange('notes', e.target.value)}
              placeholder="Any additional information about this withdrawal..."
              rows={3}
              maxLength={500}
            />
            <p className="text-xs text-gray-500">
              {(formData.notes?.length || 0)}/500 characters
            </p>
          </div>

          {/* Warning */}
          {formData.amount > investment.availableBalance * 0.5 && (
            <Alert>
              <AlertTriangleIcon className="h-4 w-4" />
              <AlertDescription>
                You are withdrawing more than 50% of your available balance. This may impact your future profit distributions.
              </AlertDescription>
            </Alert>
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
              disabled={isLoading || !formData.amount || !formData.reason}
              className="min-w-32"
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Processing...</span>
                </>
              ) : (
                <>
                  <CreditCardIcon className="h-4 w-4 mr-2" />
                  Process Withdrawal
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}