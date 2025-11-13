import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { investmentService } from '@/services/investmentService'
import type { InvestmentRound, InvestmentRoundCreateRequest } from '@/types/investment'
import { toast } from 'sonner'

interface UseInvestmentRoundsOptions {
  shopId?: string
  page?: number
  size?: number
  status?: string | undefined
  enabled?: boolean
}

export function useInvestmentRounds({
  shopId,
  page = 0,
  size = 20,
  status,
  enabled = true
}: UseInvestmentRoundsOptions = {}) {
  return useQuery({
    queryKey: ['investment-rounds', shopId, page, size, status],
    queryFn: () => {
      if (!shopId) {
        throw new Error('shopId is required')
      }
      return investmentService.getShopInvestmentRounds(shopId, page, size, status)
    },
    staleTime: 1 * 60 * 1000, // 1 minute
    refetchOnMount: true, // Always fetch fresh data on mount
    refetchOnWindowFocus: false,
    enabled: enabled && !!shopId,
  })
}

export function useInvestmentRound(roundId: string, enabled = true) {
  return useQuery({
    queryKey: ['investment-round', roundId],
    queryFn: () => investmentService.getInvestmentRoundById(roundId),
    enabled: !!roundId && enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useCreateInvestmentRound() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({
      shopId,
      request,
    }: {
      shopId: string
      request: InvestmentRoundCreateRequest
    }) => investmentService.createInvestmentRound(shopId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investment-rounds'] })
      toast.success('Investment round created successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to create investment round')
    },
  })
}

export function useCloseInvestmentRound() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (roundId: string) => investmentService.closeInvestmentRound(roundId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investment-rounds'] })
      queryClient.invalidateQueries({ queryKey: ['investment-round'] })
      toast.success('Investment round closed successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to close investment round')
    },
  })
}

export function useCompleteInvestmentRound() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (roundId: string) => investmentService.completeInvestmentRound(roundId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investment-rounds'] })
      queryClient.invalidateQueries({ queryKey: ['investment-round'] })
      toast.success('Investment round completed successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to complete investment round')
    },
  })
}

export function useCancelInvestmentRound() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ roundId, reason }: { roundId: string; reason?: string }) =>
      investmentService.cancelInvestmentRound(roundId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investment-rounds'] })
      queryClient.invalidateQueries({ queryKey: ['investment-round'] })
      toast.success('Investment round cancelled successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to cancel investment round')
    },
  })
}

export function useDeleteInvestmentRound() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (roundId: string) => investmentService.deleteInvestmentRound(roundId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['investment-rounds'] })
      toast.success('Investment round deleted successfully')
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to delete investment round')
    },
  })
}
