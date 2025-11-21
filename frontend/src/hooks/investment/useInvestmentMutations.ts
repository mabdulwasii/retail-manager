import { useMutation, useQueryClient } from '@tanstack/react-query'
import { investmentService } from '@/services/investmentService'
import type { InvestmentCreateRequest, WithdrawalRequest } from '@/types/investment'
import { toast } from 'sonner'

export function useCreateInvestment() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: InvestmentCreateRequest) =>
      investmentService.createInvestment(request),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investments'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      toast.success('Investment created successfully')
    },

    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to create investment'
      toast.error(message)
    },
  })
}

export function useUpdateInvestmentStatus() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ investmentId, status }: { investmentId: string; status: string }) =>
      investmentService.updateInvestmentStatus(investmentId, status),

    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['investments'] })
      queryClient.invalidateQueries({ queryKey: ['investment', data.id] })
      toast.success('Investment status updated')
    },

    onError: () => {
      toast.error('Failed to update investment status')
    },
  })
}

export function useProcessWithdrawal() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ investmentId, request }: { 
      investmentId: string
      request: WithdrawalRequest 
    }) => investmentService.processWithdrawal(investmentId, request),

    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['investments'] })
      queryClient.invalidateQueries({ queryKey: ['investment', data.id] })
      queryClient.invalidateQueries({ queryKey: ['distributions'] })
      toast.success('Withdrawal request submitted successfully')
    },

    onError: (error: any) => {
      const message = error.response?.data?.message || 'Failed to process withdrawal'
      toast.error(message)
    },
  })
}
