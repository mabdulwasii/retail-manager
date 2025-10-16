import { useState, useCallback } from 'react'
import { api } from '@/services/api'

export interface Investment {
  id: string
  investmentNumber: string
  investorId: string
  investorName: string
  investorEmail: string
  shopId: string
  shopName: string
  investmentType: InvestmentType
  amount: number
  profitSharingModel: ProfitSharingModel
  profitPercentage?: number
  fixedShares?: number
  investmentDate: string
  maturityDate?: string
  status: InvestmentStatus
  totalProfitEarned: number
  totalWithdrawn: number
  availableBalance: number
  lastProfitCalculation?: string
  products: ProductInfo[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface ProductInfo {
  id: string
  name: string
  category: string
  price: number
}

export interface InvestorDistribution {
  id: string
  investmentId: string
  investmentNumber: string
  investorName: string
  periodStart: string
  periodEnd: string
  totalSalesRevenue: number
  totalProfit: number
  investorSharePercentage: number
  investorProfitAmount: number
  distributionAmount: number
  status: DistributionStatus
  distributionDate?: string
  paymentReference?: string
  notes?: string
  calculationDetails?: string
  createdAt: string
}

export interface CreateInvestmentRequest {
  shopId: string
  investmentType: InvestmentType
  amount: number
  profitSharingModel: ProfitSharingModel
  profitPercentage?: number
  fixedShares?: number
  maturityDate?: string
  productIds?: string[]
  categoryFilter?: string
  notes?: string
}

export interface WithdrawalRequest {
  amount: number
  reason: string
  paymentMethod?: string
  bankAccount?: string
  notes?: string
}

export type InvestmentType = 'SHOP_WIDE' | 'PRODUCT_SPECIFIC' | 'CATEGORY_SPECIFIC'
export type ProfitSharingModel = 'PROPORTIONAL_BY_AMOUNT' | 'FIXED_SHARES' | 'TIME_WEIGHTED' | 'TIERED'
export type InvestmentStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'MATURED' | 'WITHDRAWN' | 'CANCELLED'
export type DistributionStatus = 'CALCULATED' | 'APPROVED' | 'PAID' | 'FAILED' | 'CANCELLED'

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const useInvestment = () => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const createInvestment = useCallback(async (request: CreateInvestmentRequest): Promise<Investment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.post('/api/v1/investments', request)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create investment')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getShopInvestments = useCallback(async (
    shopId: string,
    page = 0,
    size = 20,
    sortBy = 'investmentDate',
    sortDir = 'desc'
  ): Promise<PaginatedResponse<Investment> | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.getShopInvestments(shopId, page, size)
      return response as unknown as PaginatedResponse<Investment>
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch shop investments')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getMyInvestments = useCallback(async (
    page = 0,
    size = 20,
    sortBy = 'investmentDate',
    sortDir = 'desc'
  ): Promise<PaginatedResponse<Investment> | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.getMyInvestments(page, size)
      return response as unknown as PaginatedResponse<Investment>
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch my investments')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getInvestmentById = useCallback(async (investmentId: string): Promise<Investment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.get(`/api/v1/investments/${investmentId}`)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch investment')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const updateInvestmentStatus = useCallback(async (
    investmentId: string,
    status: InvestmentStatus
  ): Promise<Investment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.put(`/api/v1/investments/${investmentId}/status?status=${status}`)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update investment status')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const processWithdrawal = useCallback(async (
    investmentId: string,
    request: WithdrawalRequest
  ): Promise<Investment | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.post(`/api/v1/investments/${investmentId}/withdraw`, request)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to process withdrawal')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getInvestmentDistributions = useCallback(async (investmentId: string): Promise<InvestorDistribution[] | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.get(`/api/v1/investments/${investmentId}/distributions`)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch distributions')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getMyDistributions = useCallback(async (): Promise<InvestorDistribution[] | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.get('/api/v1/my-distributions')
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch my distributions')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const approveDistribution = useCallback(async (
    distributionId: string,
    notes?: string
  ): Promise<InvestorDistribution | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const params = notes ? `?notes=${encodeURIComponent(notes)}` : ''
      const response = await api.post(`/api/v1/distributions/${distributionId}/approve${params}`)
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve distribution')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const markDistributionAsPaid = useCallback(async (
    distributionId: string,
    paymentReference: string
  ): Promise<InvestorDistribution | null> => {
    try {
      setIsLoading(true)
      setError(null)

      const response = await api.post(
        `/api/v1/distributions/${distributionId}/mark-paid?paymentReference=${encodeURIComponent(paymentReference)}`
      )
      return response
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to mark distribution as paid')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [])

  const getInvestmentSummary = useCallback(async () => {
    try {
      setIsLoading(true)
      setError(null)

      // Fetch multiple data points in parallel
      const [myInvestments, myDistributions] = await Promise.allSettled([
        getMyInvestments(0, 100),
        getMyDistributions()
      ])

      const investments = myInvestments.status === 'fulfilled' ? myInvestments.value?.content || [] : []
      const distributions = myDistributions.status === 'fulfilled' ? myDistributions.value || [] : []

      // Calculate summary statistics
      const totalInvested = investments.reduce((sum, inv) => sum + inv.amount, 0)
      const totalProfitEarned = investments.reduce((sum, inv) => sum + inv.totalProfitEarned, 0)
      const totalWithdrawn = investments.reduce((sum, inv) => sum + inv.totalWithdrawn, 0)
      const availableBalance = investments.reduce((sum, inv) => sum + inv.availableBalance, 0)
      const activeInvestments = investments.filter(inv => inv.status === 'ACTIVE').length

      const pendingDistributions = distributions.filter(dist =>
        dist.status === 'CALCULATED' || dist.status === 'APPROVED'
      ).length

      const totalROI = totalInvested > 0 ? (totalProfitEarned / totalInvested) * 100 : 0

      return {
        totalInvested,
        totalProfitEarned,
        totalWithdrawn,
        availableBalance,
        activeInvestments,
        totalInvestments: investments.length,
        pendingDistributions,
        totalROI,
        investments,
        distributions
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch investment summary')
      return null
    } finally {
      setIsLoading(false)
    }
  }, [getMyInvestments, getMyDistributions])

  return {
    isLoading,
    error,
    createInvestment,
    getShopInvestments,
    getMyInvestments,
    getInvestmentById,
    updateInvestmentStatus,
    processWithdrawal,
    getInvestmentDistributions,
    getMyDistributions,
    approveDistribution,
    markDistributionAsPaid,
    getInvestmentSummary
  }
}