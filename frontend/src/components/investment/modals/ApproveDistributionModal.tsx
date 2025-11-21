import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { useApproveDistribution } from '@/hooks/investment/useDistributions'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import type { InvestorDistribution } from '@/types/investment'

interface ApproveDistributionModalProps {
  open: boolean
  onClose: () => void
  distribution: InvestorDistribution
}

export function ApproveDistributionModal({
  open,
  onClose,
  distribution,
}: ApproveDistributionModalProps) {
  const { formatCurrency } = useCurrency()
  const [notes, setNotes] = useState('')
  const approveMutation = useApproveDistribution()

  const handleApprove = async () => {
    try {
      await approveMutation.mutateAsync({
        distributionId: distribution.id,
        notes: notes || undefined,
      })
      onClose()
      setNotes('')
    } catch (error) {
      console.error('Failed to approve distribution:', error)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Approve Distribution</DialogTitle>
          <DialogDescription>
            Review and approve this profit distribution for payment
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          {/* Distribution Details */}
          <div className="rounded-lg border p-4 space-y-3">
            <div className="flex justify-between">
              <span className="text-sm text-muted-foreground">Investment:</span>
              <span className="font-medium">{distribution.investmentNumber}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-sm text-muted-foreground">Investor:</span>
              <span className="font-medium">{distribution.investorName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-sm text-muted-foreground">Period:</span>
              <span className="font-medium">
                {format(new Date(distribution.periodStart), 'MMM dd')} -{' '}
                {format(new Date(distribution.periodEnd), 'MMM dd, yyyy')}
              </span>
            </div>
          </div>

          {/* Calculation Breakdown */}
          <div className="rounded-lg bg-muted p-4 space-y-2">
            <h4 className="font-semibold text-sm mb-3">Calculation Breakdown</h4>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Sales Revenue:</span>
              <span className="font-medium">{formatCurrency(distribution.totalSalesRevenue)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Total Profit:</span>
              <span className="font-medium text-green-600">
                {formatCurrency(distribution.totalProfit)}
              </span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">
                Share ({distribution.investorSharePercentage}%):
              </span>
              <span className="font-medium">
                {formatCurrency(distribution.investorProfitAmount)}
              </span>
            </div>
            <div className="flex justify-between text-base font-semibold pt-2 border-t">
              <span>Distribution Amount:</span>
              <span className="text-green-600">
                {formatCurrency(distribution.distributionAmount)}
              </span>
            </div>
          </div>

          {/* Approval Notes */}
          <div className="space-y-2">
            <Label htmlFor="notes">Approval Notes (Optional)</Label>
            <Textarea
              id="notes"
              placeholder="Add any notes regarding this approval..."
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={approveMutation.isPending}>
            Cancel
          </Button>
          <Button onClick={handleApprove} disabled={approveMutation.isPending}>
            {approveMutation.isPending ? 'Approving...' : 'Approve Distribution'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
