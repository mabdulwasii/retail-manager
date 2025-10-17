import React from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/ManualAuthContext'
import { useSidebar } from '@/context/SidebarContext'
import { useTheme } from '@/context/ThemeContext'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { CurrencySelector } from '@/components/ui/currency-selector'
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
  X,
  Settings,
  Moon,
  Sun,
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
    href: '/dashboard',
    icon: LayoutDashboard,
    roles: [],
  },
  {
    title: 'Shops',
    href: '/shops',
    icon: Store,
    roles: ['SHOP_OWNER', 'MANAGER'],
  },
  {
    title: 'Products',
    href: '/products',
    icon: Package,
    roles: ['SHOP_OWNER', 'MANAGER'],
  },
  {
    title: 'Inventory',
    href: '/inventory',
    icon: Warehouse,
    roles: ['SHOP_OWNER', 'MANAGER'],
  },
  {
    title: 'Sales',
    href: '/sales',
    icon: ShoppingCart,
    roles: ['SHOP_OWNER', 'MANAGER', 'CASHIER'],
  },
  {
    title: 'Receipts',
    href: '/receipts',
    icon: Receipt,
    roles: ['SHOP_OWNER', 'MANAGER', 'CASHIER'],
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
    roles: ['SHOP_OWNER', 'MANAGER'],
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
  const { close } = useSidebar()
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const filteredNavItems = navItems.filter(item => {
    if (item.roles.length === 0) return true
    return hasAnyRole(item.roles)
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
            {filteredNavItems.map((item) => (
              <Link
                key={item.href}
                to={item.href}
                onClick={handleNavigation}
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

      {/* Settings Section - Always visible at bottom */}
      <div className="shrink-0 px-3 py-4 border-t">
        <div className="space-y-4">
          <div>
            <h3 className="text-xs uppercase tracking-wider text-muted-foreground font-semibold mb-2 px-2">Settings</h3>
            <div className="space-y-1">
              <div className="flex items-center justify-between p-2">
                <div className="flex items-center">
                  <Settings className="h-4 w-4 mr-2 text-muted-foreground" />
                  <span className="text-sm">Currency</span>
                </div>
                <CurrencySelector variant="ghost" size="sm" />
              </div>
              <div 
                className="flex items-center justify-between p-2 cursor-pointer hover:bg-primary/10 rounded-lg transition"
                onClick={toggleTheme}
              >
                <div className="flex items-center">
                  {theme === 'dark' ? (
                    <Moon className="h-4 w-4 mr-2 text-muted-foreground" />
                  ) : (
                    <Sun className="h-4 w-4 mr-2 text-muted-foreground" />
                  )}
                  <span className="text-sm">{theme === 'dark' ? 'Dark' : 'Light'} Mode</span>
                </div>
                <div className="w-8 h-4 bg-muted relative rounded-full">
                  <div className={`absolute top-0.5 left-0.5 w-3 h-3 rounded-full transform transition-transform ${theme === 'dark' ? 'bg-primary translate-x-4' : 'bg-foreground'}`}></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}