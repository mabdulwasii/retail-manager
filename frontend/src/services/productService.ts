import api from '@/lib/axios'
import { Product, ProductCreateRequest, ProductUpdateRequest } from '@/types/api'

export interface ProductListParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  search?: string | undefined
  categoryId?: string | undefined
  status?: string | undefined
  shopId?: string | undefined
  minPrice?: number | undefined
  maxPrice?: number | undefined
  includeInventory?: boolean
}

export interface ProductListResponse {
  content: Product[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export const productService = {
  async getProducts(shopId: string, params: ProductListParams = {}): Promise<ProductListResponse> {
    const response = await api.get(`/shops/${shopId}/products`, { params })
    return response.data
  },

  async getProduct(productId: string): Promise<Product> {
    const response = await api.get(`/products/${productId}`)
    return response.data
  },

  async createProduct(shopId: string, data: ProductCreateRequest): Promise<Product> {
    const response = await api.post(`/shops/${shopId}/products`, data)
    return response.data
  },

  async updateProduct(productId: string, data: ProductUpdateRequest): Promise<Product> {
    const response = await api.patch(`/products/${productId}`, data)
    return response.data
  },

  async deleteProduct(productId: string): Promise<void> {
    await api.delete(`/products/${productId}`)
  },

  async updateProductStatus(productId: string, status: string): Promise<Product> {
    const response = await api.patch(`/products/${productId}/status`, null, {
      params: { status }
    })
    return response.data
  },


  generateSKU(): string {
    const timestamp = Date.now().toString(36).toUpperCase()
    const random = Math.random().toString(36).substring(2, 7).toUpperCase()
    return `PRD-${timestamp}-${random}`
  },

  async searchProducts(shopId: string, query: string): Promise<Product[]> {
    const response = await api.get(`/shops/${shopId}/products`, {
      params: { search: query, size: 50 }
    })
    return response.data.content
  }
}
