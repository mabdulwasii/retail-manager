import api from "@/lib/axios";

export interface SalesSummary {
  shopId: string;
  periodStart: string;
  periodEnd: string;
  totalRevenue: number;
  totalTransactions: number;
  averageTransactionValue: number;
  calculatedAt: string;
}

export interface RevenueAnalytics {
  shopId: string;
  periodStart: string;
  periodEnd: string;
  currentRevenue: number;
  previousRevenue: number;
  growthRate: number;
  currentTransactions: number;
  previousTransactions: number;
  calculatedAt: string;
}

export interface InvestmentRoi {
  shopId: string;
  periodStart: string;
  periodEnd: string;
  totalInvestmentAmount: number;
  totalDistributions: number;
  roiPercentage: number;
  calculatedAt: string;
}

export interface FraudStatistics {
  shopId: string;
  periodStart: string;
  periodEnd: string;
  totalAssessments: number;
  highRiskCount: number;
  criticalRiskCount: number;
  riskRate: number;
  calculatedAt: string;
}

export const analyticsService = {
  async getSalesSummary(
    shopId: string,
    startDate: string,
    endDate: string
  ): Promise<SalesSummary> {
    const { data } = await api.get(`/analytics/sales-summary`, {
      params: { shopId, startDate, endDate },
    });
    return data;
  },

  async getRevenueAnalytics(
    shopId: string,
    startDate: string,
    endDate: string
  ): Promise<RevenueAnalytics> {
    const { data } = await api.get(`/analytics/revenue-analytics`, {
      params: { shopId, startDate, endDate },
    });
    return data;
  },

  async getInvestmentROI(
    shopId: string,
    startDate: string,
    endDate: string
  ): Promise<InvestmentRoi> {
    const { data } = await api.get(`/analytics/investment-roi`, {
      params: { shopId, startDate, endDate },
    });
    return data;
  },

  async getFraudStatistics(
    shopId: string,
    startDate: string,
    endDate: string
  ): Promise<FraudStatistics> {
    const { data } = await api.get(`/analytics/fraud-statistics`, {
      params: { shopId, startDate, endDate },
    });
    return data;
  },

  async clearAnalyticsCache(shopId: string): Promise<void> {
    await api.post(`/v1/analytics/clear-cache/${shopId}`);
  },
};
