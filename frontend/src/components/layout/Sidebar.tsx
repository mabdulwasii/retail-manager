import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/ManualAuthContext'
import { cn } from '@/lib/utils'
import {
  LayoutDashboard,
  Store,
  Package,
  ShoppingCart,
  Receipt,
  TrendingUp,
  BarChart3,
  FileText,
  Warehouse,
} from 'lucide-react'

interface NavItem {
  title: string
  href: string
  icon: React.ElementType
  roles: string[]
}

const navItems: NavItem[] = [
  {
    title: 'Dashboard',
    href: '/',
    icon: LayoutDashboard,
    roles: [],
  },
  {
    title: 'Shops',
    href: '/shops',
    icon: Store,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER'],
  },
  {
    title: 'Products',
    href: '/products',
    icon: Package,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER'],
  },
  {
    title: 'Inventory',
    href: '/inventory',
    icon: Warehouse,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER'],
  },
  {
    title: 'Sales',
    href: '/sales',
    icon: ShoppingCart,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER'],
  },
  {
    title: 'Receipts',
    href: '/receipts',
    icon: Receipt,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER', 'CASHIER'],
  },
  {
    title: 'Investments',
    href: '/investments',
    icon: TrendingUp,
    roles: ['SHOP_OWNER', 'INVESTOR'],
  },
  {
    title: 'Analytics',
    href: '/analytics',
    icon: BarChart3,
    roles: ['SHOP_OWNER', 'SHOP_MANAGER'],
  },
  {
    title: 'Audit Logs',
    href: '/audit',
    icon: FileText,
    roles: ['SHOP_OWNER', 'SYSTEM_ADMIN'],
  },
]

export const Sidebar: React.FC = () => {
  const location = useLocation()
  const { hasAnyRole } = useAuth()

  const filteredNavItems = navItems.filter(item => {
    if (item.roles.length === 0) return true
    return hasAnyRole(item.roles)
  })

  return (
    <div className="pb-12 w-64">
      <div className="space-y-4 py-4">
        <div className="px-3 py-2">
          <div className="space-y-1">
            {filteredNavItems.map((item) => (
              <Link
                key={item.href}
                to={item.href}
                className={cn(
                  'text-sm group flex p-3 w-full justify-start font-medium cursor-pointer hover:text-primary hover:bg-primary/10 rounded-lg transition',
                  location.pathname === item.href
                    ? 'text-primary bg-primary/10'
                    : 'text-muted-foreground'
                )}
              >
                <div className="flex items-center flex-1">
                  <item.icon className="h-5 w-5 mr-3" />
                  {item.title}
                </div>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}