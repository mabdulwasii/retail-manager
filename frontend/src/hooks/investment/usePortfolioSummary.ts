import { useMemo } from 'react'
import { differenceInDays } from 'date-fns'
import type { Investment, PortfolioSummary } from '@/types/investment'

export function usePortfolioSummary(investments: Investment[] = []): PortfolioSummary {
  return useMemo(() => {
    if (!investments || investments.length === 0) {
      return {
        totalInvested: 0,
        totalReturns: 0,
        totalWithdrawn: 0,
        availableBalance: 0,
        activeCount: 0,
        totalCount: 0,
        maturedCount: 0,
        maturingSoon: 0,
        averageROI: 0,
        monthlyTrend: 0,
      }
    }

    const totalInvested = investments.reduce((sum, inv) => sum + inv.amount, 0)
    const totalReturns = investments.reduce((sum, inv) => sum + inv.totalProfitEarned, 0)
    const totalWithdrawn = investments.reduce((sum, inv) => sum + inv.totalWithdrawn, 0)
    const availableBalance = investments.reduce((sum, inv) => sum + inv.availableBalance, 0)

    const totalCount = investments.length
    const activeCount = investments.filter((inv) => inv.status === 'ACTIVE').length
    const maturedCount = investments.filter((inv) => inv.status === 'MATURED').length

    // Count investments maturing within 30 days
    const maturingSoon = investments.filter((inv) => {
      if (!inv.maturityDate || inv.status !== 'ACTIVE') return false
      const daysUntilMaturity = differenceInDays(new Date(inv.maturityDate), new Date())
      return daysUntilMaturity > 0 && daysUntilMaturity <= 30
    }).length

    // Calculate average ROI
    const averageROI =
      totalInvested > 0 ? ((totalReturns / totalInvested) * 100) : 0

    // Calculate monthly trend (simplified - compare last month vs current)
    // In a real implementation, this would compare actual monthly data
    const monthlyTrend = averageROI > 0 ? 2.5 : 0 // Placeholder

    return {
      totalInvested,
      totalReturns,
      totalWithdrawn,
      availableBalance,
      activeCount,
      totalCount,
      maturedCount,
      maturingSoon,
      averageROI,
      monthlyTrend,
    }
  }, [investments])
}

export function calculateROI(investment: Investment): number {
  if (investment.amount === 0) return 0
  const returns = investment.totalProfitEarned
  const initial = investment.amount
  return ((returns / initial) * 100)
}

export function calculateNetProfit(investment: Investment): number {
  return investment.totalProfitEarned - investment.totalWithdrawn
}
