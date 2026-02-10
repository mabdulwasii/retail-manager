import api from "@/lib/axios";

export interface InventorySummary {
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
}

export interface InventoryItem {
  id: string;
  shopId: string;
  shopName: string;
  productId: string;
  product: {
    id: string;
    name: string;
    description?: string;
    price: number;
    category: string;
    sku?: string;
    barcode?: string;
  };
  currentStock: number;
  reservedStock: number;
  availableStock: number;
  minimumStock: number;
  unitCost?: number;
  location?: string;
  batchNumber?: string;
  expiryDate?: string;
  status: string;
  lastStockUpdate: string;
  isLowStock: boolean;
  isExpired: boolean;
  isExpiringSoon: boolean;
}

export interface CreateInventoryRequest {
  productId: string;
  currentStock: number;
  minimumStock: number;
  unitCost?: number;
  location?: string;
  batchNumber?: string;
  expiryDate?: string;
}

export interface AdjustStockRequest {
  newStock: number;
  reason: string;
  changeType: "STOCK_IN" | "STOCK_OUT" | "ADJUSTMENT";
}

export interface ReserveStockRequest {
  quantity: number;
  referenceId?: string;
  referenceType?: string;
  reason?: string;
}

export const inventoryService = {
  async getInventorySummary(shopId: string): Promise<InventorySummary> {
    const { data } = await api.get(`/shops/${shopId}/inventory/summary`);
    return data;
  },

  async getInventory(
    shopId: string,
    params?: Record<string, any>
  ): Promise<InventoryItem[]> {
    const { data } = await api.get(`/shops/${shopId}/inventory`, { params });
    return data;
  },

  async createInventoryItem(
    shopId: string,
    request: CreateInventoryRequest
  ): Promise<InventoryItem> {
    const { data } = await api.post(`/shops/${shopId}/inventory`, request);
    return data;
  },

  async adjustStock(
    inventoryId: string,
    request: AdjustStockRequest
  ): Promise<InventoryItem> {
    const { data } = await api.post(
      `/inventory/${inventoryId}/adjust`,
      request
    );
    return data;
  },

  async reserveStock(
    inventoryId: string,
    request: ReserveStockRequest
  ): Promise<InventoryItem> {
    const { data } = await api.post(
      `/inventory/${inventoryId}/reserve`,
      request
    );
    return data;
  },

  async releaseStock(
    inventoryId: string,
    quantity: number
  ): Promise<InventoryItem> {
    const { data } = await api.post(`/inventory/${inventoryId}/release`, {
      quantity,
    });
    return data;
  },

  async updateInventory(
    inventoryId: string,
    updates: Partial<CreateInventoryRequest>
  ): Promise<InventoryItem> {
    const { data } = await api.patch(`/inventory/${inventoryId}`, updates);
    return data;
  },

  async updateStatus(
    inventoryId: string,
    status: string
  ): Promise<InventoryItem> {
    const { data } = await api.patch(`/inventory/${inventoryId}/status`, {
      status,
    });
    return data;
  },

  async getInventoryHistory(inventoryId: string): Promise<any[]> {
    const { data } = await api.get(`/inventory/${inventoryId}/history`);
    return data;
  },

  async getLowStockAlerts(shopId: string): Promise<InventoryItem[]> {
    const { data } = await api.get(`/shops/${shopId}/inventory/low-stock`);
    return data;
  },

  async getExpiringItems(
    shopId: string,
    daysAhead: number = 30
  ): Promise<InventoryItem[]> {
    const { data } = await api.get(`/shops/${shopId}/inventory/expiring`, {
      params: { daysAhead },
    });
    return data;
  },
};
