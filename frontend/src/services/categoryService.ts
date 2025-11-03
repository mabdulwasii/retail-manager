import api from '@/lib/axios'

export interface Category {
  id: string
  shopId: string
  name: string
  description?: string
  parentId?: string
  slug?: string
  displayOrder?: number
  isActive: boolean
  children?: Category[]
  createdAt: string
  updatedAt: string
}

export interface CategoryCreateRequest {
  name: string
  description?: string
  parentId?: string
  slug?: string
  displayOrder?: number
  isActive?: boolean
}

export interface CategoryUpdateRequest {
  name?: string
  description?: string
  parentId?: string
  slug?: string
  displayOrder?: number
  isActive?: boolean
}

export const categoryService = {
  /**
   * Get all categories for a shop
   * @param shopId - Shop ID
   * @param tree - Include hierarchical tree structure
   */
  async getCategories(shopId: string, tree: boolean = false): Promise<Category[]> {
    const response = await api.get(`/shops/${shopId}/categories`, {
      params: { tree }
    })
    return response.data
  },

  /**
   * Get a single category by ID
   * @param categoryId - Category ID
   */
  async getCategory(categoryId: string): Promise<Category> {
    const response = await api.get(`/categories/${categoryId}`)
    return response.data
  },

  /**
   * Create a new category for a shop
   * @param shopId - Shop ID
   * @param data - Category creation data
   */
  async createCategory(shopId: string, data: CategoryCreateRequest): Promise<Category> {
    const response = await api.post(`/shops/${shopId}/categories`, data)
    return response.data
  },

  /**
   * Update an existing category
   * @param categoryId - Category ID
   * @param data - Category update data
   */
  async updateCategory(categoryId: string, data: CategoryUpdateRequest): Promise<Category> {
    const response = await api.put(`/categories/${categoryId}`, data)
    return response.data
  },

  /**
   * Delete a category
   * @param categoryId - Category ID
   */
  async deleteCategory(categoryId: string): Promise<void> {
    await api.delete(`/categories/${categoryId}`)
  },

  /**
   * Get category names only (helper method for dropdowns)
   * @param shopId - Shop ID
   */
  async getCategoryNames(shopId: string): Promise<string[]> {
    const categories = await this.getCategories(shopId, false)
    return categories
      .filter(cat => cat.isActive)
      .map(cat => cat.name)
      .sort()
  }
}
