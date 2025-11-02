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
  CATEGORY_BASED = 'CATEGORY_BASED'
}

export enum ProfitSharingModel {
  PROPORTIONAL_BY_AMOUNT = 'PROPORTIONAL_BY_AMOUNT',
  FIXED_PERCENTAGE = 'FIXED_PERCENTAGE',
  FIXED_AMOUNT = 'FIXED_AMOUNT'
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
