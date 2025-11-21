import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useMarkDistributionPaid } from '@/hooks/investment/useDistributions'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import type { InvestorDistribution } from '@/types/investment'

const markPaidSchema = yup.object().shape({
  paymentMethod: yup.string().required('Payment method is required'),
  paymentReference: yup.string().required('Payment reference is required'),
  paymentDate: yup.string().optional(),
  notes: yup.string().optional(),
})

type MarkPaidFormValues = yup.InferType<typeof markPaidSchema>

interface MarkPaidModalProps {
  open: boolean
  onClose: () => void
  distribution: InvestorDistribution
}

export function MarkPaidModal({ open, onClose, distribution }: MarkPaidModalProps) {
  const { formatCurrency } = useCurrency()
  const markPaidMutation = useMarkDistributionPaid()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors },
  } = useForm<MarkPaidFormValues>({
    resolver: yupResolver(markPaidSchema),
    defaultValues: {
      paymentMethod: '',
      paymentReference: '',
      paymentDate: format(new Date(), 'yyyy-MM-dd'),
      notes: '',
    },
  })

  const onSubmit = async (data: MarkPaidFormValues) => {
    try {
      await markPaidMutation.mutateAsync({
        distributionId: distribution.id,
        paymentReference: data.paymentReference,
      })
      reset()
      onClose()
    } catch (error) {
      console.error('Failed to mark distribution as paid:', error)
    }
  }

  const handleClose = () => {
    reset()
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Mark Distribution as Paid</DialogTitle>
          <DialogDescription>
            Confirm payment details for this distribution
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4 py-4">
            {/* Distribution Summary */}
            <div className="rounded-lg border p-4 space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Investor:</span>
                <span className="font-medium">{distribution.investorName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Period:</span>
                <span className="font-medium">
                  {format(new Date(distribution.periodStart), 'MMM yyyy')}
                </span>
              </div>
              <div className="flex justify-between text-lg font-semibold pt-2 border-t">
                <span>Amount to Pay:</span>
                <span className="text-green-600">
                  {formatCurrency(distribution.distributionAmount)}
                </span>
              </div>
            </div>

            {/* Payment Method */}
            <div className="space-y-2">
              <Label htmlFor="paymentMethod">Payment Method *</Label>
              <Select
                value={watch('paymentMethod')}
                onValueChange={(value) => setValue('paymentMethod', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select payment method" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="BANK_TRANSFER">Bank Transfer</SelectItem>
                  <SelectItem value="CHECK">Check</SelectItem>
                  <SelectItem value="CASH">Cash</SelectItem>
                  <SelectItem value="MOBILE_MONEY">Mobile Money</SelectItem>
                  <SelectItem value="OTHER">Other</SelectItem>
                </SelectContent>
              </Select>
              {errors.paymentMethod && (
                <p className="text-sm text-red-600">{errors.paymentMethod.message}</p>
              )}
            </div>

            {/* Payment Reference */}
            <div className="space-y-2">
              <Label htmlFor="paymentReference">Payment Reference *</Label>
              <Input
                id="paymentReference"
                placeholder="e.g., TXN-20241102-001"
                {...register('paymentReference')}
              />
              <p className="text-xs text-muted-foreground">
                Enter transaction ID or reference number
              </p>
              {errors.paymentReference && (
                <p className="text-sm text-red-600">{errors.paymentReference.message}</p>
              )}
            </div>

            {/* Payment Date */}
            <div className="space-y-2">
              <Label htmlFor="paymentDate">Payment Date</Label>
              <Input
                id="paymentDate"
                type="date"
                {...register('paymentDate')}
              />
            </div>

            {/* Notes */}
            <div className="space-y-2">
              <Label htmlFor="notes">Notes (Optional)</Label>
              <Textarea
                id="notes"
                placeholder="Add any additional payment details..."
                rows={3}
                {...register('notes')}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={markPaidMutation.isPending}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={markPaidMutation.isPending}>
              {markPaidMutation.isPending ? 'Processing...' : 'Mark as Paid'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
