import { UserRole } from "@/types/roles";
import React, { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './ManualAuthContext';

interface ShopContextType {
  selectedShopId: string | null
  setSelectedShopId: (shopId: string) => void
  canManageMultipleShops: boolean
}

const ShopContext = createContext<ShopContextType | undefined>(undefined)

interface ShopProviderProps {
  children: ReactNode
}

export const ShopProvider: React.FC<ShopProviderProps> = ({ children }) => {
  const { user, hasAnyRole, hasPermission } = useAuth()
  
  const canManageMultipleShops = hasAnyRole([ UserRole.TENANT_ADMIN, UserRole.SYSTEM_ADMIN])
  //const canManageMultipleShops =  hasPermission(Permission.SHOP_MANAGE)

  const [selectedShopId, setSelectedShopIdState] = useState<string | null>(() => {
    const stored = localStorage.getItem('selectedShopId')
    if (stored) return stored
    
    return user?.shopId || null
  })
  
  useEffect(() => {
    if (user?.shopId) {
      setSelectedShopIdState(user.shopId)
      localStorage.setItem('selectedShopId', user.shopId)
    }
  }, [user?.shopId])
  
  const setSelectedShopId = (shopId: string) => {
    setSelectedShopIdState(shopId)
    localStorage.setItem('selectedShopId', shopId)
  }
  
  const value = useMemo(
    () => ({ selectedShopId, setSelectedShopId, canManageMultipleShops }),
    [selectedShopId, canManageMultipleShops]
  )
  
  return (
    <ShopContext.Provider value={value}>
      {children}
    </ShopContext.Provider>
  )
}

export const useShopContext = (): ShopContextType => {
  const context = useContext(ShopContext)
  if (context === undefined) {
    throw new Error('useShopContext must be used within a ShopProvider')
  }
  return context
}
