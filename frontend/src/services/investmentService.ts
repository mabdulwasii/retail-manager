import api from "@/lib/axios";

export interface Investment {
  id: string;
  investmentNumber: string;
  investorId: string;
  investorName: string;
  investorEmail: string;
  shopId: string;
  shopName: string;
  investmentType: string;
  amount: number;
  profitSharingModel: string;
  profitPercentage?: number;
  fixedShares?: number;
  investmentDate: string;
  maturityDate?: string;
  status: string;
  totalProfitEarned: number;
  totalWithdrawn: number;
  availableBalance: number;
  lastProfitCalculation?: string;
  products: any[];
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface InvestorDistribution {
  id: string;
  investmentId: string;
  investmentNumber: string;
  investorName: string;
  periodStart: string;
  periodEnd: string;
  totalSalesRevenue: number;
  totalProfit: number;
  investorSharePercentage: number;
  investorProfitAmount: number;
  distributionAmount: number;
  status: string;
  distributionDate?: string;
  paymentReference?: string;
  notes?: string;
  calculationDetails?: string;
  createdAt: string;
}

export interface CreateInvestmentRequest {
  shopId: string;
  investmentType: string;
  amount: number;
  profitSharingModel: string;
  profitPercentage?: number;
  fixedShares?: number;
  maturityDate?: string;
  productIds?: string[];
  categoryFilter?: string;
  notes?: string;
}

export interface WithdrawalRequest {
  amount: number;
  reason: string;
  paymentMethod?: string;
  bankAccount?: string;
  notes?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const investmentService = {
  async createInvestment(
    request: CreateInvestmentRequest
  ): Promise<Investment> {
    const { data } = await api.post("/v1/investments", request);
    return data;
  },

  async getShopInvestments(
    shopId: string,
    page = 0,
    size = 20,
    sortBy = "investmentDate",
    sortDir = "desc"
  ): Promise<PaginatedResponse<Investment>> {
    const { data } = await api.get(`/v1/shops/${shopId}/investments`, {
      params: { page, size, sortBy, sortDir },
    });
    return data;
  },

  async getMyInvestments(
    page = 0,
    size = 20,
    sortBy = "investmentDate",
    sortDir = "desc"
  ): Promise<PaginatedResponse<Investment>> {
    const { data } = await api.get("/v1/my-investments", {
      params: { page, size, sortBy, sortDir },
    });
    return data;
  },

  async getInvestmentById(investmentId: string): Promise<Investment> {
    const { data } = await api.get(`/v1/investments/${investmentId}`);
    return data;
  },

  async updateInvestmentStatus(
    investmentId: string,
    status: string
  ): Promise<Investment> {
    const { data } = await api.put(
      `/v1/investments/${investmentId}/status`,
      null,
      {
        params: { status },
      }
    );
    return data;
  },

  async processWithdrawal(
    investmentId: string,
    request: WithdrawalRequest
  ): Promise<Investment> {
    const { data } = await api.post(
      `/v1/investments/${investmentId}/withdraw`,
      request
    );
    return data;
  },

  async getInvestmentDistributions(
    investmentId: string
  ): Promise<InvestorDistribution[]> {
    const { data } = await api.get(
      `/v1/investments/${investmentId}/distributions`
    );
    return data;
  },

  async getMyDistributions(): Promise<InvestorDistribution[]> {
    const { data } = await api.get("/v1/my-distributions");
    return data;
  },

  async approveDistribution(
    distributionId: string,
    notes?: string
  ): Promise<InvestorDistribution> {
    const { data } = await api.post(
      `/v1/distributions/${distributionId}/approve`,
      null,
      {
        params: notes ? { notes } : undefined,
      }
    );
    return data;
  },

  async markDistributionAsPaid(
    distributionId: string,
    paymentReference: string
  ): Promise<InvestorDistribution> {
    const { data } = await api.post(
      `/v1/distributions/${distributionId}/mark-paid`,
      null,
      {
        params: { paymentReference },
      }
    );
    return data;
  },
};
