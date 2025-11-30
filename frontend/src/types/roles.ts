/**
 * User Role Enums
 * Centralized role definitions for consistent usage across the application
 * 
 * Usage:
 * import { UserRole } from '@/types/roles'
 * hasAnyRole([UserRole.SHOP_OWNER, UserRole.MANAGER])
 */

/**
 * System-wide user roles
 * These match the roles defined in Keycloak
 */
export enum UserRole {
  // System Roles
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  
  // Shop Management Roles
  SHOP_OWNER = 'OWNER',
  MANAGER = 'MANAGER',
  
  // Sales & Operations Roles
  SALES_MANAGER = 'SALES_MANAGER',
  CASHIER = 'CASHIER',
  
  // Inventory Roles
  INVENTORY_MANAGER = 'INVENTORY_MANAGER',
  EMPLOYEE = 'EMPLOYEE',
  
  // Financial Roles
  ACCOUNTANT = 'ACCOUNTANT',
  INVESTOR = 'INVESTOR',
  AUDITOR = 'AUDITOR',
  
  // Customer Role
  CUSTOMER = 'CUSTOMER',
}

export const RoleGroups = {
  SHOP_MANAGERS: [
    UserRole.TENANT_ADMIN,
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
  ],
  
  SHOP_CREATORS: [
    UserRole.TENANT_ADMIN,
    UserRole.SHOP_OWNER,
  ],
  
  PRODUCT_MANAGERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
  ],
  
  INVENTORY_MANAGERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.INVENTORY_MANAGER,
  ],
  
  INVENTORY_VIEWERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.INVENTORY_MANAGER,
    UserRole.EMPLOYEE,
    UserRole.CASHIER,
  ],
  
  SALES_OPERATORS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.CASHIER,
  ],
  
  SALES_VIEWERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.SALES_MANAGER,
    UserRole.CASHIER,
    UserRole.ACCOUNTANT,
  ],
  
  EXPENSE_MANAGERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.ACCOUNTANT,
  ],
  
  EXPENSE_CREATORS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
  ],
  
  EXPENSE_APPROVERS: [
    UserRole.SHOP_OWNER,
    UserRole.ACCOUNTANT,
  ],
  
  EXPENSE_VIEWERS: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.ACCOUNTANT,
  ],
  
  INVESTMENT_MANAGERS: [
    UserRole.SHOP_OWNER,
    UserRole.INVESTOR,
  ],
  
  INVESTMENT_VIEWERS: [
    UserRole.SHOP_OWNER,
    UserRole.INVESTOR,
    UserRole.ACCOUNTANT,
  ],
  
  SETTINGS_MANAGERS: [
    UserRole.SYSTEM_ADMIN,
    UserRole.TENANT_ADMIN,
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
  ],
  
  ADMINS: [
    UserRole.SYSTEM_ADMIN,
    UserRole.SUPER_ADMIN,
    UserRole.TENANT_ADMIN,
  ],
  
  STAFF: [
    UserRole.SHOP_OWNER,
    UserRole.MANAGER,
    UserRole.SALES_MANAGER,
    UserRole.CASHIER,
    UserRole.INVENTORY_MANAGER,
    UserRole.EMPLOYEE,
    UserRole.ACCOUNTANT,
  ],
}


export const isAdminRole = (role: string): boolean => {
  return RoleGroups.ADMINS.some(adminRole => adminRole === role)
}


export const isStaffRole = (role: string): boolean => {
  return RoleGroups.STAFF.some(staffRole => staffRole === role)
}


export const getRoleDisplayName = (role: string): string => {
  const displayNames: Record<string, string> = {
    [UserRole.SYSTEM_ADMIN]: 'System Admin',
    [UserRole.SUPER_ADMIN]: 'Super Admin',
    [UserRole.TENANT_ADMIN]: 'Tenant Admin',
    [UserRole.SHOP_OWNER]: 'Shop Owner',
    [UserRole.MANAGER]: 'Manager',
    [UserRole.SALES_MANAGER]: 'Sales Manager',
    [UserRole.CASHIER]: 'Cashier',
    [UserRole.INVENTORY_MANAGER]: 'Inventory Manager',
    [UserRole.EMPLOYEE]: 'Employee',
    [UserRole.ACCOUNTANT]: 'Accountant',
    [UserRole.INVESTOR]: 'Investor',
    [UserRole.AUDITOR]: 'Auditor',
    [UserRole.CUSTOMER]: 'Customer',
  }
  
  return displayNames[role] || role
}

export const isValidRole = (role: string): role is UserRole => {
  return Object.values(UserRole).includes(role as UserRole)
}
