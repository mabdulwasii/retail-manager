import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { investmentService, PaginatedResponse, InvestorDistribution } from '@/services/investmentService'
import { toast } from 'sonner'

/**
 * Hook to fetch distributions for a specific investment
 * Returns an array of distributions
 */
export function useInvestmentDistributions(investmentId: string) {
  return useQuery({
    queryKey: ['distributions', 'investment', investmentId],
    queryFn: () => investmentService.getInvestmentDistributions(investmentId),
    enabled: !!investmentId,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

/**
 * Hook to fetch paginated distributions for the current investor
 * Returns paginated response
 */
interface UseDistributionsOptions {
  page?: number
  size?: number
}

export function useDistributions(options: UseDistributionsOptions = {}) {
  const { page = 0, size = 20 } = options
  
  return useQuery<PaginatedResponse<InvestorDistribution>>({
    queryKey: ['distributions', 'my', page, size],
    queryFn: () => investmentService.getMyDistributions(page, size),
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}

export function useApproveDistribution() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ distributionId, notes }: { distributionId: string; notes?: string }) =>
      investmentService.approveDistribution(distributionId, notes),
    
    onMutate: async ({ distributionId }) => {
      // Cancel outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['distributions'] })

      // Snapshot previous value
      const previousDistributions = queryClient.getQueryData(['distributions'])

      // Optimistically update
      queryClient.setQueryData(['distributions'], (old: any) =>
        old?.map((d: any) =>
          d.id === distributionId
            ? { ...d, status: 'APPROVED', approvedAt: new Date().toISOString() }
            : d
        )
      )

      return { previousDistributions }
    },

    onError: (err, variables, context) => {
      // Revert on error
      if (context?.previousDistributions) {
        queryClient.setQueryData(['distributions'], context.previousDistributions)
      }
      toast.error('Failed to approve distribution')
    },

    onSuccess: () => {
      toast.success('Distribution approved successfully')
    },

    onSettled: () => {
      // Always refetch after error or success
      queryClient.invalidateQueries({ queryKey: ['distributions'] })
      queryClient.invalidateQueries({ queryKey: ['investment'] })
    },
  })
}

export function useMarkDistributionPaid() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ distributionId, paymentReference }: { 
      distributionId: string
      paymentReference: string 
    }) => investmentService.markDistributionAsPaid(distributionId, paymentReference),

    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['distributions'] })
      queryClient.invalidateQueries({ queryKey: ['investment'] })
      queryClient.invalidateQueries({ queryKey: ['investments'] })
      toast.success('Distribution marked as paid')
    },

    onError: () => {
      toast.error('Failed to mark distribution as paid')
    },
  })
}
