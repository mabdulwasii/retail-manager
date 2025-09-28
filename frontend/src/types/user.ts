// User-related TypeScript interfaces

export interface UserProfile {
  id: string
  username: string
  email: string
  firstName?: string
  lastName?: string
  fullName?: string
  phoneNumber?: string
  status: UserStatus
  isInvestor: boolean
  roles: string[]
  tenantId?: string
  shopId?: string
  createdAt?: string
  updatedAt?: string
}

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED'

export interface UpdateUserProfileRequest {
  firstName?: string
  lastName?: string
  phoneNumber?: string
}

export interface UserProfileResponse {
  id: string
  username: string
  email: string
  firstName?: string
  lastName?: string
  fullName?: string
  phoneNumber?: string
  status: string
  isInvestor: boolean
  roles: string[]
  tenantId?: string
  shopId?: string
  createdAt?: string
  updatedAt?: string
}