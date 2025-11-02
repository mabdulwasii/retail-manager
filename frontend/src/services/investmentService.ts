import api from "@/lib/axios";
import type { 
  Investment, 
  InvestorDistribution 
} from "@/types/investment";

// Re-export types for backward compatibility
export type { Investment, InvestorDistribution };

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
    const { data } = await api.post("/investments", request);
    return data;
  },

  async getShopInvestments(
    shopId: string,
    page = 0,
    size = 20,
    sortBy = "investmentDate",
    sortDir = "desc"
  ): Promise<PaginatedResponse<Investment>> {
    const { data } = await api.get(`/shops/${shopId}/investments`, {
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
    const { data } = await api.get("/my-investments", {
      params: { page, size, sortBy, sortDir },
    });
    return data;
  },

  async getInvestmentById(investmentId: string): Promise<Investment> {
    const { data } = await api.get(`/investments/${investmentId}`);
    return data;
  },

  async updateInvestmentStatus(
    investmentId: string,
    status: string
  ): Promise<Investment> {
    const { data } = await api.put(
      `/investments/${investmentId}/status`,
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
      `/investments/${investmentId}/withdraw`,
      request
    );
    return data;
  },

  async getInvestmentDistributions(
    investmentId: string
  ): Promise<InvestorDistribution[]> {
    const { data } = await api.get(
      `/investments/${investmentId}/distributions`
    );
    return data;
  },

  async getMyDistributions(
    page = 0,
    size = 20
  ): Promise<PaginatedResponse<InvestorDistribution>> {
    const { data } = await api.get("/my-distributions", {
      params: { page, size },
    });
    return data;
  },

  async approveDistribution(
    distributionId: string,
    notes?: string
  ): Promise<InvestorDistribution> {
    const { data } = await api.post(
      `/distributions/${distributionId}/approve`,
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
      `/distributions/${distributionId}/mark-paid`,
      null,
      {
        params: { paymentReference },
      }
    );
    return data;
  },
};
