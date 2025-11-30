import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/ManualAuthContext'
import { useSidebar } from '@/context/SidebarContext'
import { useTheme } from '@/context/ThemeContext'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { CurrencySelector } from '@/components/ui/currency-selector'
import { Permission } from '@/types/permissions'
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
  Tag,
  X,
  Settings,
  Moon,
  Sun,
  Shield,
  Users,
} from 'lucide-react'

interface NavItem {
  title: string
  href: string
  icon: React.ElementType
  /** List of permissions required to access this nav item. User needs at least one of these permissions. Empty array means available to all authenticated users. */
  permissions: Permission[]
}

const navItems: NavItem[] = [
  {
    title: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
    permissions: [], // Available to all authenticated users
  },
  {
    title: 'Shops',
    href: '/shops',
    icon: Store,
    permissions: [Permission.SHOP_LIST, Permission.SHOP_MANAGE],
  },
  {
    title: 'Products',
    href: '/products',
    icon: Package,
    permissions: [Permission.PRODUCT_LIST],
  },
  // {
  //   title: 'Categories',
  //   href: '/categories',
  //   icon: Tag,
  //   permissions: [Permission.CATEGORY_LIST],
  // },
  {
    title: 'Inventory',
    href: '/inventory',
    icon: Warehouse,
    permissions: [Permission.INVENTORY_LIST],
  },
  {
    title: 'Sales',
    href: '/sales',
    icon: ShoppingCart,
    permissions: [Permission.SALES_CREATE, Permission.SALES_READ, Permission.SALES_LIST],
  },
  {
    title: 'Receipts',
    href: '/receipts',
    icon: Receipt,
    permissions: [Permission.RECEIPT_LIST],
  },
  {
    title: 'Investments',
    href: '/investments',
    icon: TrendingUp,
    permissions: [Permission.INVESTMENT_LIST, Permission.INVESTMENT_VIEW],
  },
  {
    title: 'Analytics',
    href: '/analytics',
    icon: BarChart3,
    permissions: [Permission.ANALYTICS_VIEW, Permission.ANALYTICS_SALES_VIEW, Permission.ANALYTICS_INVESTMENT_VIEW],
  },
  {
    title: 'Audit Logs',
    href: '/audit',
    icon: FileText,
    permissions: [Permission.AUDIT_LOG_VIEW, Permission.AUDIT_LOG_LIST, Permission.AUDIT_LOG_VIEW_SHOP, Permission.AUDIT_LOG_VIEW_TENANT],
  },
  {
    title: 'Users',
    href: '/users',
    icon: Users,
    permissions: [Permission.USER_LIST, Permission.USER_MANAGE],
  },
  {
    title: 'Role Management',
    href: '/admin/roles',
    icon: Shield,
    permissions: [Permission.ROLE_LIST, Permission.ROLE_READ],
  },
]

export const Sidebar: React.FC = () => {
  const location = useLocation()
  const { hasAnyPermission } = useAuth()
  const { close } = useSidebar()
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const filteredNavItems = navItems.filter(item => {
    // If no permissions required, show to all authenticated users
    if (item.permissions.length === 0) return true
    // Check if user has at least one of the required permissions
    return hasAnyPermission(item.permissions)
  })

  // Handle navigation on mobile - close sidebar after clicking a link
  const handleNavigation = () => {
    if (window.innerWidth < 1024) {
      close()
    }
  }

  return (
    <div className="w-64 flex flex-col h-screen lg:h-full">
      <div className="flex items-center justify-between p-4 lg:hidden">
        <h2 className="font-semibold">Navigation</h2>
        <Button
          variant="ghost"
          size="icon"
          onClick={close}
          aria-label="Close sidebar"
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto">
        <div className="px-3 py-4">
          <div className="space-y-1">
            {filteredNavItems.map((item) => {
              const isActive =
                location.pathname === item.href ||
                (item.href !== "/" &&
                  location.pathname.startsWith(item.href + "/"));

              return (
                <Link
                  key={item.href}
                  to={item.href}
                  onClick={handleNavigation}
                  className={cn(
                    "text-sm group flex p-3 w-full justify-start font-medium cursor-pointer hover:text-primary hover:bg-primary/10 rounded-lg transition",
                    isActive
                      ? "text-primary bg-primary/10"
                      : "text-muted-foreground"
                  )}
                >
                  <div className="flex items-center flex-1">
                    <item.icon className="h-5 w-5 mr-3" />
                    {item.title}
                  </div>
                </Link>
              );
            })}
          </div>
        </div>
      </div>

      {/* Settings Section - Always visible at bottom */}
      <div className="shrink-0 px-3 py-4 border-t">
        <div className="space-y-4">
          <div>
            <h3 className="text-xs uppercase tracking-wider text-muted-foreground font-semibold mb-2 px-2">
              Settings
            </h3>
            <div className="space-y-1">
              <div className="flex items-center justify-between p-2">
                <div className="flex items-center">
                  <Settings className="h-4 w-4 mr-2 text-muted-foreground" />
                  <span className="text-sm">Currency</span>
                </div>
                <CurrencySelector variant="ghost" size="sm" />
              </div>
              <button
                type="button"
                className="flex items-center justify-between p-2 w-full cursor-pointer hover:bg-primary/10 rounded-lg transition"
                onClick={toggleTheme}
                aria-label={`Switch to ${theme === "dark" ? "light" : "dark"} mode`}
              >
                <div className="flex items-center">
                  {theme === "dark" ? (
                    <Moon className="h-4 w-4 mr-2 text-muted-foreground" />
                  ) : (
                    <Sun className="h-4 w-4 mr-2 text-muted-foreground" />
                  )}
                  <span className="text-sm">
                    {theme === "dark" ? "Dark" : "Light"} Mode
                  </span>
                </div>
                <div className="w-8 h-4 bg-muted relative rounded-full">
                  <div
                    className={`absolute top-0.5 left-0.5 w-3 h-3 rounded-full transform transition-transform ${
                      theme === "dark"
                        ? "bg-primary translate-x-4"
                        : "bg-foreground"
                    }`}
                  ></div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}