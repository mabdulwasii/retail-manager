import api from "@/lib/axios";

export interface ShopResponse {
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
}

export interface ShopCreateRequest {
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
  openingDate?: string;
}

export interface ShopUpdateRequest extends Partial<ShopCreateRequest> {}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const shopService = {
  async getShops(params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<ShopResponse>> {
    const { data } = await api.get("/shops", { params });
    return data;
  },

  async getActiveShops(): Promise<ShopResponse[]> {
    const { data } = await api.get("/shops/active");
    return data;
  },

  async getShopById(shopId: string): Promise<ShopResponse> {
    const { data } = await api.get(`/shops/${shopId}`);
    return data;
  },

  async createShop(request: ShopCreateRequest): Promise<ShopResponse> {
    const { data } = await api.post("/shops", request);
    return data;
  },

  async updateShop(
    shopId: string,
    request: ShopUpdateRequest
  ): Promise<ShopResponse> {
    const { data } = await api.put(`/shops/${shopId}`, request);
    return data;
  },

  async updateStatus(shopId: string, status: string): Promise<ShopResponse> {
    const { data } = await api.patch(`/shops/${shopId}/status`, null, {
      params: { status },
    });
    return data;
  },

  async deleteShop(shopId: string): Promise<void> {
    await api.delete(`/shops/${shopId}`);
  },
};
