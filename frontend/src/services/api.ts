import configService from "@/config/runtime-config";
import { UserProfileResponse } from "@/types/user";
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";

class ApiService {
  private api: AxiosInstance;
  private getTokenCallback: (() => string | undefined) | null = null;

  constructor() {
    this.api = axios.create({
      baseURL: configService.apiBaseUrl,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    this.setupInterceptors();
  }

  // Set token callback function
  setTokenProvider(getToken: () => string | undefined) {
    this.getTokenCallback = getToken;
  }

  private setupInterceptors() {
    // Request interceptor to add auth token and Keycloak headers
    this.api.interceptors.request.use(
      async (config) => {
        // Try to get token from callback first, then localStorage as fallback
        let token: string | undefined;
        if (this.getTokenCallback) {
          token = this.getTokenCallback();
        }

        // Fallback to localStorage if no token from callback
        if (!token) {
          token =
            localStorage.getItem("keycloak_token") ||
            localStorage.getItem("keycloak_access_token") ||
            undefined;
        }

        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add Keycloak realm and client headers for proper context
        config.headers["X-Keycloak-Realm"] = configService.keycloakRealm;
        config.headers["X-Keycloak-Client"] = configService.keycloakClientId;

        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response: AxiosResponse) => response,
      async (error) => {
        if (error.response?.status === 401) {
          console.warn(
            "API request received 401 Unauthorized - token may be invalid or expired"
          );
        }
        return Promise.reject(error);
      }
    );
  }

  // Generic HTTP methods
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.get<T>(url, config);
    return response.data;
  }

  async post<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.api.post<T>(url, data, config);
    return response.data;
  }

  async put<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.api.put<T>(url, data, config);
    return response.data;
  }

  async patch<T>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<T> {
    const response = await this.api.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.delete<T>(url, config);
    return response.data;
  }

  // File upload
  async uploadFile<T>(
    url: string,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<T> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await this.api.post<T>(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          );
          onProgress(progress);
        }
      },
    });

    return response.data;
  }

  async getBlob(url: string, config?: AxiosRequestConfig): Promise<Blob> {
    const response = await this.api.get(url, {
      ...config,
      responseType: "blob",
    });
    return new Blob([response.data]);
  }

  // Download file
  async downloadFile(url: string, filename: string): Promise<void> {
    const response = await this.api.get(url, {
      responseType: "blob",
    });

    const blob = new Blob([response.data]);
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }

  // Analytics API endpoints (matching Swagger spec)
  async getSalesSummary(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      shopId: string;
      periodStart: string;
      periodEnd: string;
      totalRevenue: number;
      totalTransactions: number;
      averageTransactionValue: number;
      calculatedAt: string;
    }>(
      `/analytics/sales-summary?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`
    );
  }

  async getInvestmentROI(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      shopId: string;
      periodStart: string;
      periodEnd: string;
      totalInvestmentAmount: number;
      totalDistributions: number;
      roiPercentage: number;
      calculatedAt: string;
    }>(
      `/analytics/investment-roi?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`
    );
  }

  async getRevenueAnalytics(
    shopId: string,
    startDate: string,
    endDate: string
  ) {
    return this.get<{
      shopId: string;
      periodStart: string;
      periodEnd: string;
      currentRevenue: number;
      previousRevenue: number;
      growthRate: number;
      currentTransactions: number;
      previousTransactions: number;
      calculatedAt: string;
    }>(
      `/analytics/revenue-analytics?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`
    );
  }

  async getFraudStatistics(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      shopId: string;
      periodStart: string;
      periodEnd: string;
      totalAssessments: number;
      highRiskCount: number;
      criticalRiskCount: number;
      riskRate: number;
      calculatedAt: string;
    }>(
      `/analytics/fraud-statistics?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`
    );
  }

  // Shop API endpoints (matching Swagger spec)
  async getShops(page = 0, size = 20) {
    return this.get<{
      content: Array<{
        id: string;
        name: string;
        email: string;
        phoneNumber?: string;
        status: string;
        address?: string;
        city?: string;
        state?: string;
        country?: string;
        createdAt: string;
        updatedAt: string;
      }>;
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
    }>(`/shops?page=${page}&size=${size}`);
  }

  async getActiveShops() {
    return this.get<
      Array<{
        id: string;
        name: string;
        email: string;
        phoneNumber?: string;
        status: string;
      }>
    >("/shops/active");
  }

  async getShop(shopId: string) {
    return this.get<{
      id: string;
      name: string;
      description?: string;
      email: string;
      phoneNumber?: string;
      address?: string;
      city?: string;
      state?: string;
      country?: string;
      postalCode?: string;
      taxId?: string;
      status: string;
      openingDate?: string;
      createdAt: string;
      updatedAt: string;
    }>(`/shops/${shopId}`);
  }

  // Inventory API endpoints
  async getInventorySummary(shopId: string) {
    return this.get<{
      totalItems: number;
      totalValue: number;
      lowStockItems: number;
      expiredItems: number;
      expiringSoonItems: number;
      categoryBreakdown: Array<{
        category: string;
        itemCount: number;
        totalValue: number;
        lowStockCount: number;
      }>;
    }>(`/api/shops/${shopId}/inventory/summary`);
  }

  // Expense API endpoints
  async getExpenseSummary(
    shopId: string,
    startDate?: string,
    endDate?: string
  ) {
    const params = new URLSearchParams();
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);
    const queryString = params.toString() ? `?${params.toString()}` : "";

    return this.get<{
      totalExpenses: number;
      pendingApproval: number;
      approvedExpenses: number;
      totalAmount: number;
      monthlyTotal: number;
      categoryBreakdown: Array<{
        category: string;
        itemCount: number;
        totalValue: number;
      }>;
    }>(`/api/shops/${shopId}/expenses/summary${queryString}`);
  }

  // Health check endpoint
  async getHealth() {
    return this.get<{ status: string }>("/actuator/health");
  }

  // Clear analytics cache
  async clearAnalyticsCache(shopId: string) {
    return this.post<void>(`/analytics/clear-cache/${shopId}`);
  }

  // User Profile API endpoints
  async getUserProfile(): Promise<UserProfileResponse> {
    return this.get<UserProfileResponse>("/users/profile");
  }

  // Investment API endpoints
  async getShopInvestments(shopId: string, page = 0, size = 20) {
    return this.get<{
      content: Array<any>;
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
    }>(`/shops/${shopId}/investments?page=${page}&size=${size}`);
  }

  async getMyInvestments(page = 0, size = 20) {
    return this.get<{
      content: Array<any>;
      totalElements: number;
      totalPages: number;
      number: number;
      size: number;
    }>(`/my-investments?page=${page}&size=${size}`);
  }
}

export const apiService = new ApiService();
export const api = apiService; // Alias for backward compatibility
export default apiService;
