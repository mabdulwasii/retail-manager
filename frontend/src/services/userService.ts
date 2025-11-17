import api from "@/lib/axios";
import { Role } from "@/types/api";

export interface User {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  tenantId?: string;
  shopId?: string;
  roles: Role[];
  permissions: string[];
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  createdAt: string;
  updatedAt: string;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  shopId?: string;
  roles: string[];
  status?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
}

export interface PaginatedUsers {
  content: User[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const userService = {
  /**
   * Get users for a specific shop
   * Available to: System Admin, Tenant Admin, Owner, Manager
   */
  async getShopUsers(
    shopId: string,
    status?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED',
    page = 0,
    size = 50
  ): Promise<User[]> {
    const { data } = await api.get(`/shops/${shopId}/users`, {
      params: { status, page, size },
    });
    return data;
  },

  /**
   * Get users for a specific tenant (returns array, not paginated)
   * Available to: System Admin, Tenant Admin
   */
  async getTenantUsers(tenantId: string): Promise<User[]> {
    const { data } = await api.get(`/tenants/${tenantId}/users`);
    return data;
  },

  /**
   * Create user in tenant
   * Available to: System Admin, Tenant Admin
   */
  async createUserInTenant(tenantId: string, request: UserCreateRequest): Promise<User> {
    const { data } = await api.post(`/tenants/${tenantId}/users`, request);
    return data;
  },

  /**
   * Get system-wide users (SYSTEM_ADMIN only)
   */
  async getAllUsers(
    status?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED',
    page = 0,
    size = 50
  ): Promise<PaginatedUsers> {
    const { data } = await api.get('/users', {
      params: { status, page, size },
    });
    return data;
  },

  /**
   * Get user by ID
   */
  async getUserById(userId: string): Promise<User> {
    const { data} = await api.get(`/users/users/${userId}`);
    return data;
  },

  /**
   * Update user
   * Available to: System Admin, Tenant Admin
   */
  async updateUser(userId: string, request: Partial<UserCreateRequest>): Promise<User> {
    const { data } = await api.patch(`/users/${userId}`, request);
    return data;
  },
};
