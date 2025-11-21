import { useQuery } from '@tanstack/react-query'
import { investmentService } from '@/services/investmentService'
import type { Investment, InvestmentFilters } from '@/types/investment'

interface UseInvestmentsOptions {
  shopId?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  filters?: InvestmentFilters
  enabled?: boolean
}

export function useInvestments({
  shopId,
  page = 0,
  size = 20,
  sortBy = 'investmentDate',
  sortDir = 'desc',
  enabled = true
}: UseInvestmentsOptions = {}) {
  return useQuery({
    queryKey: ['investments', shopId, page, size, sortBy, sortDir],
    queryFn: () => {
      if (shopId) {
        return investmentService.getShopInvestments(shopId, page, size, sortBy, sortDir)
      }
      return investmentService.getMyInvestments(page, size, sortBy, sortDir)
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchOnWindowFocus: false,
    enabled,
  })
}

export function useInvestment(investmentId: string, enabled = true) {
  return useQuery({
    queryKey: ['investment', investmentId],
    queryFn: () => investmentService.getInvestmentById(investmentId),
    enabled: !!investmentId && enabled,
    staleTime: 2 * 60 * 1000, // 2 minutes
  })
}
