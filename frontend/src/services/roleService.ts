import apiService from './api'
import {
  Role,
  RoleCreateRequest,
  RoleUpdateRequest,
  Permission,
  AssignPermissionsRequest,
} from '@/types/role'

/**
 * Role Management Service
 * Handles all role and permission related API calls
 */
class RoleService {
  private baseUrl = '/roles'
  private permissionsUrl = '/permissions'

  /**
   * Get all roles
   */
  async getRoles(): Promise<Role[]> {
    return apiService.get<Role[]>(this.baseUrl)
  }

  /**
   * Get a single role by ID
   */
  async getRole(roleId: string): Promise<Role> {
    return apiService.get<Role>(`${this.baseUrl}/${roleId}`)
  }

  /**
   * Create a new role
   */
  async createRole(data: RoleCreateRequest): Promise<Role> {
    return apiService.post<Role>(this.baseUrl, data)
  }

  /**
   * Update an existing role
   * API only accepts description (not name)
   */
  async updateRole(roleId: string, data: RoleUpdateRequest): Promise<Role> {
    // Only send description to API
    const payload = { description: data.description }
    return apiService.put<Role>(`${this.baseUrl}/${roleId}`, payload)
  }

  /**
   * Delete a role
   */
  async deleteRole(roleId: string): Promise<void> {
    return apiService.delete<void>(`${this.baseUrl}/${roleId}`)
  }

  /**
   * Get all available permissions
   */
  async getPermissions(): Promise<Permission[]> {
    return apiService.get<Permission[]>(this.permissionsUrl)
  }

  /**
   * Get permissions for a specific role
   */
  async getRolePermissions(roleId: string): Promise<Permission[]> {
    return apiService.get<Permission[]>(`${this.baseUrl}/${roleId}/permissions`)
  }

  /**
   * Assign permissions to a role
   * API expects permissionIdentifiers (replaces all permissions)
   */
  async assignPermissions(
    roleId: string,
    permissionIds: string[]
  ): Promise<void> {
    return apiService.put<void>(
      `${this.baseUrl}/${roleId}/permissions`,
      { permissionIdentifiers: permissionIds }
    )
  }

  /**
   * Remove permissions from a role
   */
  async removePermissions(
    roleId: string,
    permissionIds: string[]
  ): Promise<void> {
    return apiService.delete<void>(
      `${this.baseUrl}/${roleId}/permissions`,
      { data: { permissionIds } }
    )
  }

  /**
   * Remove a single permission from a role
   */
  async removePermission(roleId: string, permissionId: string): Promise<void> {
    return this.removePermissions(roleId, [permissionId])
  }
}

export const roleService = new RoleService()
export default roleService
