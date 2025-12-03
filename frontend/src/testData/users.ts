/**
 * Test Data: Users
 * Mock user data for testing
 */

export const getMockInvestor = () => ({
  id: '1',
  username: 'investor',
  email: 'investor@example.com',
  firstName: 'Investor',
  lastName: 'User',
  roles: ['ROLE_INVESTOR'],
  createdAt: new Date('2024-01-15').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockShopOwner = () => ({
  id: '2',
  username: 'owner',
  email: 'owner@example.com',
  firstName: 'Shop',
  lastName: 'Owner',
  shopId: 'shop1',
  roles: ['ROLE_SHOP_OWNER'],
  createdAt: new Date('2024-01-10').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockManager = () => ({
  id: '3',
  username: 'manager',
  email: 'manager@example.com',
  firstName: 'Store',
  lastName: 'Manager',
  shopId: 'shop1',
  roles: ['ROLE_MANAGER'],
  createdAt: new Date('2024-01-12').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockAccountant = () => ({
  id: '4',
  username: 'accountant',
  email: 'accountant@example.com',
  firstName: 'Account',
  lastName: 'Manager',
  roles: ['ROLE_ACCOUNTANT'],
  createdAt: new Date('2024-01-14').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockAdmin = () => ({
  id: '5',
  username: 'admin',
  email: 'admin@example.com',
  firstName: 'System',
  lastName: 'Admin',
  roles: ['ROLE_SYSTEM_ADMIN'],
  createdAt: new Date('2024-01-01').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockCashier = () => ({
  id: '6',
  username: 'cashier',
  email: 'cashier@example.com',
  firstName: 'John',
  lastName: 'Doe',
  shopId: 'shop1',
  roles: ['ROLE_CASHIER'],
  createdAt: new Date('2024-01-16').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})

export const getMockAuditor = () => ({
  id: '7',
  username: 'auditor',
  email: 'auditor@example.com',
  firstName: 'Auditor',
  lastName: 'User',
  roles: ['ROLE_AUDITOR'],
  createdAt: new Date('2024-01-05').toISOString(),
  lastLogin: new Date('2024-01-20').toISOString()
})
