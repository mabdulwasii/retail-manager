import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { getKeycloak } from '@/lib/keycloak'
import { UserProfileResponse } from '@/types/user'

class ApiService {
  private api: AxiosInstance

  constructor() {
    this.api = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor to add auth token
    this.api.interceptors.request.use(
      async (config) => {
        const keycloak = getKeycloak()
        if (keycloak?.token) {
          config.headers.Authorization = `Bearer ${keycloak.token}`
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response: AxiosResponse) => response,
      async (error) => {
        if (error.response?.status === 401) {
          const keycloak = getKeycloak()
          if (keycloak) {
            try {
              await keycloak.updateToken(5)
              // Retry the original request
              return this.api.request(error.config)
            } catch (refreshError) {
              keycloak.logout()
              return Promise.reject(refreshError)
            }
          }
        }
        return Promise.reject(error)
      }
    )
  }

  // Generic HTTP methods
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.get<T>(url, config)
    return response.data
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.post<T>(url, data, config)
    return response.data
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.put<T>(url, data, config)
    return response.data
  }

  async patch<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.patch<T>(url, data, config)
    return response.data
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.api.delete<T>(url, config)
    return response.data
  }

  // File upload
  async uploadFile<T>(url: string, file: File, onProgress?: (progress: number) => void): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await this.api.post<T>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      },
    })

    return response.data
  }

  // Download file
  async downloadFile(url: string, filename: string): Promise<void> {
    const response = await this.api.get(url, {
      responseType: 'blob',
    })

    const blob = new Blob([response.data])
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  }

  // Analytics API endpoints
  async getSalesSummary(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      totalSales: number
      totalRevenue: number
      averageOrderValue: number
      topProducts: Array<{ name: string; sales: number }>
    }>(`/analytics/sales-summary?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`)
  }

  async getInvestmentROI(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      totalInvestment: number
      totalReturn: number
      roi: number
      profitMargin: number
    }>(`/analytics/investment-roi?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`)
  }

  async getRevenueAnalytics(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      totalRevenue: number
      monthlyRevenue: Array<{ month: string; revenue: number }>
      revenueGrowth: number
    }>(`/analytics/revenue-analytics?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`)
  }

  async getFraudStatistics(shopId: string, startDate: string, endDate: string) {
    return this.get<{
      totalTransactions: number
      flaggedTransactions: number
      fraudRate: number
      suspiciousActivities: Array<{ type: string; count: number }>
    }>(`/analytics/fraud-statistics?shopId=${shopId}&startDate=${startDate}&endDate=${endDate}`)
  }

  // Shop API endpoints
  async getShops(page = 0, size = 20) {
    return this.get<{
      content: Array<{
        id: string
        name: string
        email: string
        phone?: string
        status: string
        createdAt: string
      }>
      totalElements: number
      totalPages: number
      number: number
      size: number
    }>(`/shops?page=${page}&size=${size}`)
  }

  async getActiveShops() {
    return this.get<Array<{
      id: string
      name: string
      email: string
      status: string
    }>>('/shops/active')
  }

  async getShop(shopId: string) {
    return this.get<{
      id: string
      name: string
      email: string
      phone?: string
      address?: any
      status: string
      createdAt: string
      updatedAt: string
    }>(`/shops/${shopId}`)
  }

  // Health check endpoint
  async getHealth() {
    return this.get<{ status: string }>('/actuator/health')
  }

  // Clear analytics cache
  async clearAnalyticsCache(shopId: string) {
    return this.post<void>(`/analytics/clear-cache/${shopId}`)
  }

  // User Profile API endpoints
  async getUserProfile(): Promise<UserProfileResponse> {
    return this.get<UserProfileResponse>('/users/profile')
  }
}

export const apiService = new ApiService()
export const api = apiService // Alias for backward compatibility
export default apiService