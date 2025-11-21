// Investment Module Type Definitions

export enum InvestmentStatus {
  ACTIVE = 'ACTIVE',
  MATURED = 'MATURED',
  WITHDRAWN = 'WITHDRAWN',
  DEFAULTED = 'DEFAULTED'
}

export enum InvestmentType {
  SHOP_WIDE = 'SHOP_WIDE',
  PRODUCT_SPECIFIC = 'PRODUCT_SPECIFIC',
  CATEGORY_SPECIFIC = 'CATEGORY_SPECIFIC'
}

export enum ProfitSharingModel {
  PROPORTIONAL_BY_AMOUNT = 'PROPORTIONAL_BY_AMOUNT',
  FIXED_SHARES = 'FIXED_SHARES',
  TIME_WEIGHTED = 'TIME_WEIGHTED',
  TIERED = 'TIERED'
}

export enum DistributionStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  PAID = 'PAID',
  REJECTED = 'REJECTED'
}

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
  products: InvestmentProduct[]
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface InvestmentProduct {
  id: string
  name: string
  sku?: string
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
  approvedBy?: string
  approvedAt?: string
  createdAt: string
}

export interface InvestmentCreateRequest {
  investorId: string
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

export interface InvestmentFilters {
  status: InvestmentStatus[]
  type: InvestmentType[]
  shopId?: string
  dateRange: {
    start?: Date
    end?: Date
  }
  amountRange: {
    min?: number
    max?: number
  }
  search: string
}

export interface PortfolioSummary {
  totalInvested: number
  totalReturns: number
  totalWithdrawn: number
  availableBalance: number
  activeCount: number
  totalCount: number
  maturedCount: number
  maturingSoon: number
  averageROI: number
  monthlyTrend: number
}

export interface ROIChartData {
  date: string
  actualReturn: number
  expectedReturn?: number
  cumulativeROI?: number
  profit?: number
}

export interface DistributionResult {
  grossProfit: number
  sharePercentage: number
  grossDistribution: number
  tax: number
  netDistribution: number
}

export interface TimelineEvent {
  id: string
  type: 'created' | 'distribution' | 'withdrawal' | 'status_change' | 'maturity'
  date: string
  title: string
  description?: string
  amount?: number
  icon: React.ReactNode
  color: string
}

// Investment Round Types
export enum RoundStatus {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export interface TimeWeightingRules {
  baseYears: number
  baseMultiplier: number
  year2Threshold: number
  year2Multiplier: number
  year3Threshold: number
  year3Multiplier: number
  maxMultiplier: number
}

export interface TierConfiguration {
  tier1Threshold: number
  tier1Multiplier: number
  tier2Threshold: number
  tier2Multiplier: number
  tier3Threshold: number
  tier3Multiplier: number
}

export interface InvestmentRoundInvestor {
  investorId: string
  amount: number
  fixedShares?: number
  notes?: string
}

export interface InvestmentRoundCreateRequest {
  shopId: string
  investmentType: InvestmentType
  profitSharingModel: ProfitSharingModel
  maturityDate?: string
  notes?: string
  productIds?: string[]
  categoryFilter?: string
  timeWeightingRules?: TimeWeightingRules
  tierConfiguration?: TierConfiguration
  investors: InvestmentRoundInvestor[]
}

export interface InvestmentRound {
  id: string
  roundNumber: string
  shopId: string
  shopName: string
  investmentType: InvestmentType
  profitSharingModel: ProfitSharingModel
  maturityDate?: string
  status: RoundStatus
  totalAmount: number
  totalInvestors: number
  notes?: string
  timeWeightingRules?: TimeWeightingRules
  tierConfiguration?: TierConfiguration
  investments: Investment[]
  createdBy: string
  createdAt: string
  updatedAt: string
  closedAt?: string
  completedAt?: string
}
