export interface Permission {
  id: string
  name: string
  description?: string
  category?: string
  createdAt?: string
  updatedAt?: string
}

export interface Role {
  id: string
  name: string
  description?: string
  permissions: string[] // Array of permission names (not objects)
  tenantId?: string // null for system roles, specific tenantId for custom roles
  isSystem?: boolean // System roles cannot be edited/deleted except by SYSTEM_ADMIN
  createdAt?: string
  updatedAt?: string
}

export interface RoleCreateRequest {
  name: string
  description?: string
  tenantId?: string // Set for custom tenant-specific roles
}

export interface RoleUpdateRequest {
  name: string // Only used for form validation, not sent to API
  description?: string // Only field sent to API
}

export interface AssignPermissionsRequest {
  permissionIds: string[]
}

export interface RoleResponse extends Role {}

export interface PermissionResponse extends Permission {}

// For permission matrix visualization
export interface PermissionCategory {
  category: string
  permissions: Permission[]
}

export interface RolePermissionMatrix {
  roles: Role[]
  permissions: PermissionCategory[]
}
