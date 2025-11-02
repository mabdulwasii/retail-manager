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
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { Badge } from '@/components/ui/badge'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { AlertCircle } from 'lucide-react'
import { useUpdateInvestmentStatus } from '@/hooks/investment/useInvestmentMutations'
import { InvestmentStatus } from '@/types/investment'
import type { Investment } from '@/types/investment'

const statusUpdateSchema = yup.object().shape({
  newStatus: yup.string().oneOf(Object.values(InvestmentStatus)).required(),
  reason: yup.string().required('Reason is required'),
  notifyInvestor: yup.boolean().default(true),
})

type StatusUpdateFormValues = yup.InferType<typeof statusUpdateSchema>

interface InvestmentStatusModalProps {
  open: boolean
  onClose: () => void
  investment: Investment
}

export function InvestmentStatusModal({
  open,
  onClose,
  investment,
}: InvestmentStatusModalProps) {
  const updateStatusMutation = useUpdateInvestmentStatus()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors },
  } = useForm<StatusUpdateFormValues>({
    resolver: yupResolver(statusUpdateSchema),
    defaultValues: {
      newStatus: investment.status as InvestmentStatus,
      reason: '',
      notifyInvestor: true,
    },
  })

  const newStatus = watch('newStatus')
  const notifyInvestor = watch('notifyInvestor')

  const onSubmit = async (data: StatusUpdateFormValues) => {
    try {
      await updateStatusMutation.mutateAsync({
        investmentId: investment.id,
        status: data.newStatus,
      })

      reset()
      onClose()
    } catch (error) {
      console.error('Failed to update investment status:', error)
    }
  }

  const handleClose = () => {
    reset()
    onClose()
  }

  const getStatusColor = (status: InvestmentStatus) => {
    const colors = {
      ACTIVE: 'bg-green-100 text-green-800',
      MATURED: 'bg-blue-100 text-blue-800',
      WITHDRAWN: 'bg-gray-100 text-gray-800',
      DEFAULTED: 'bg-red-100 text-red-800',
    }
    return colors[status]
  }

  const getStatusDescription = (status: InvestmentStatus) => {
    const descriptions = {
      ACTIVE: 'Investment is currently active and earning returns',
      MATURED: 'Investment has reached its maturity date',
      WITHDRAWN: 'Investment has been fully withdrawn',
      DEFAULTED: 'Investment is in default due to payment issues',
    }
    return descriptions[status]
  }

  const getStatusWarning = (currentStatus: InvestmentStatus, newStatus: InvestmentStatus) => {
    // Warn about irreversible status changes
    if (newStatus === InvestmentStatus.WITHDRAWN || newStatus === InvestmentStatus.DEFAULTED) {
      return 'This status change is irreversible. Please confirm you want to proceed.'
    }
    if (currentStatus === InvestmentStatus.DEFAULTED && newStatus === InvestmentStatus.ACTIVE) {
      return 'Reactivating a defaulted investment requires approval from management.'
    }
    return null
  }

  const statusWarning = getStatusWarning(investment.status as InvestmentStatus, newStatus)

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Update Investment Status</DialogTitle>
          <DialogDescription>
            Change the status of this investment
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-6 py-4">
            {/* Investment Info */}
            <div className="rounded-lg border p-4 space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Investment:</span>
                <span className="font-medium">{investment.investmentNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Investor:</span>
                <span className="font-medium">{investment.investorName}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Current Status:</span>
                <Badge className={getStatusColor(investment.status as InvestmentStatus)}>
                  {investment.status}
                </Badge>
              </div>
            </div>

            {/* New Status */}
            <div className="space-y-2">
              <Label htmlFor="newStatus">New Status *</Label>
              <Select
                value={newStatus}
                onValueChange={(value) => setValue('newStatus', value as InvestmentStatus)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={InvestmentStatus.ACTIVE}>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 rounded-full bg-green-500" />
                      <span>Active</span>
                    </div>
                  </SelectItem>
                  <SelectItem value={InvestmentStatus.MATURED}>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 rounded-full bg-blue-500" />
                      <span>Matured</span>
                    </div>
                  </SelectItem>
                  <SelectItem value={InvestmentStatus.WITHDRAWN}>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 rounded-full bg-gray-500" />
                      <span>Withdrawn</span>
                    </div>
                  </SelectItem>
                  <SelectItem value={InvestmentStatus.DEFAULTED}>
                    <div className="flex items-center gap-2">
                      <div className="w-2 h-2 rounded-full bg-red-500" />
                      <span>Defaulted</span>
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
              <p className="text-sm text-muted-foreground">
                {getStatusDescription(newStatus)}
              </p>
            </div>

            {/* Status Warning */}
            {statusWarning && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>{statusWarning}</AlertDescription>
              </Alert>
            )}

            {/* Reason */}
            <div className="space-y-2">
              <Label htmlFor="reason">Reason for Status Change *</Label>
              <Textarea
                id="reason"
                placeholder="Explain why you are changing the status..."
                rows={4}
                {...register('reason')}
              />
              {errors.reason && (
                <p className="text-sm text-red-600">{errors.reason.message}</p>
              )}
            </div>

            {/* Notify Investor */}
            <div className="flex items-start space-x-2">
              <Checkbox
                id="notifyInvestor"
                checked={notifyInvestor}
                onCheckedChange={(checked) => setValue('notifyInvestor', checked as boolean)}
              />
              <div className="space-y-1">
                <Label htmlFor="notifyInvestor" className="cursor-pointer font-normal">
                  Notify investor via email
                </Label>
                <p className="text-sm text-muted-foreground">
                  Send an email notification to {investment.investorEmail}
                </p>
              </div>
            </div>

            {/* Status Transition Info */}
            <div className="rounded-lg bg-muted p-4 space-y-2">
              <h4 className="font-semibold text-sm">Status Transition</h4>
              <div className="flex items-center gap-3">
                <Badge className={getStatusColor(investment.status as InvestmentStatus)}>
                  {investment.status}
                </Badge>
                <span className="text-muted-foreground">→</span>
                <Badge className={getStatusColor(newStatus)}>
                  {newStatus}
                </Badge>
              </div>
              {newStatus === InvestmentStatus.MATURED && (
                <p className="text-sm text-muted-foreground mt-2">
                  💡 Investor can still withdraw available balance after maturity
                </p>
              )}
              {newStatus === InvestmentStatus.WITHDRAWN && (
                <p className="text-sm text-muted-foreground mt-2">
                  ⚠️ This will close the investment. Available balance should be zero.
                </p>
              )}
              {newStatus === InvestmentStatus.DEFAULTED && (
                <p className="text-sm text-red-600 mt-2">
                  ⛔ This will mark the investment as defaulted. Use with caution.
                </p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={updateStatusMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={updateStatusMutation.isPending || investment.status === newStatus}
            >
              {updateStatusMutation.isPending ? 'Updating...' : 'Update Status'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
