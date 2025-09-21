import React from 'react'
import { useAuth } from '@/context/KeycloakAuthContext'
import { AdminDashboard } from './AdminDashboard'
import { OwnerManagerDashboard } from './OwnerManagerDashboard'
import { CashierDashboard } from './CashierDashboard'
import { InvestorDashboard } from './InvestorDashboard'
import { EmployeeDashboard } from './EmployeeDashboard'
import { AccountantDashboard } from './AccountantDashboard'
import { AuditorDashboard } from './AuditorDashboard'
import { CustomerDashboard } from './CustomerDashboard'

export const RoleBasedDashboard: React.FC = () => {
  const { user } = useAuth()

  if (!user || !user.roles || user.roles.length === 0) {
    return <div>Loading dashboard...</div>
  }

  // Get the highest priority role for dashboard selection
  const roles = user.roles.map(role => role.replace('ROLE_', ''))

  // Priority order for dashboard selection (highest first)
  const rolePriority = [
    'SYSTEM_ADMIN',
    'SUPER_ADMIN',
    'SHOP_OWNER',
    'SHOP_MANAGER',
    'SALES_MANAGER',
    'INVENTORY_MANAGER',
    'ACCOUNTANT',
    'AUDITOR',
    'INVESTOR',
    'CASHIER',
    'SHOP_EMPLOYEE',
    'CUSTOMER'
  ]

  const primaryRole = rolePriority.find(role => roles.includes(role)) || 'CUSTOMER'

  switch (primaryRole) {
    case 'SYSTEM_ADMIN':
    case 'SUPER_ADMIN':
      return <AdminDashboard />

    case 'SHOP_OWNER':
    case 'SHOP_MANAGER':
    case 'SALES_MANAGER':
      return <OwnerManagerDashboard />

    case 'INVENTORY_MANAGER':
      return <EmployeeDashboard />

    case 'CASHIER':
      return <CashierDashboard />

    case 'INVESTOR':
      return <InvestorDashboard />

    case 'ACCOUNTANT':
      return <AccountantDashboard />

    case 'AUDITOR':
      return <AuditorDashboard />

    case 'SHOP_EMPLOYEE':
      return <EmployeeDashboard />

    case 'CUSTOMER':
    default:
      return <CustomerDashboard />
  }
}