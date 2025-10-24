import api from '@/lib/axios'
import { Product, ProductCreateRequest, ProductUpdateRequest } from '@/types/api'

export interface ProductListParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  search?: string | undefined
  category?: string | undefined
  status?: string | undefined
  shopId?: string | undefined
  minPrice?: number | undefined
  maxPrice?: number | undefined
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
  async getProducts(params: ProductListParams = {}): Promise<ProductListResponse> {
    const response = await api.get('/products', { params })
    return response.data
  },

  async getProduct(productId: string): Promise<Product> {
    const response = await api.get(`/products/${productId}`)
    return response.data
  },

  async createProduct(data: ProductCreateRequest): Promise<Product> {
    const response = await api.post('/products', data)
    return response.data
  },

  async updateProduct(productId: string, data: ProductUpdateRequest): Promise<Product> {
    const response = await api.put(`/products/${productId}`, data)
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

  async getCategories(): Promise<string[]> {
    try {
      // Try to fetch from a dedicated endpoint if it exists
      const response = await api.get<string[]>('/products/categories')
      return response.data
    } catch (error) {
      // Fallback: Extract categories from products list
      const response = await api.get<ProductListResponse>('/products', { 
        params: { size: 1000 } 
      })
      const categories = [...new Set(
        response.data.content
          .map((p: Product) => p.category)
          .filter((cat): cat is string => Boolean(cat))
      )] as string[]
      return categories.sort()
    }
  },

  generateSKU(): string {
    const timestamp = Date.now().toString(36).toUpperCase()
    const random = Math.random().toString(36).substring(2, 7).toUpperCase()
    return `PRD-${timestamp}-${random}`
  },

  async searchProducts(query: string): Promise<Product[]> {
    const response = await api.get('/products', {
      params: { search: query, size: 50 }
    })
    return response.data.content
  }
}
