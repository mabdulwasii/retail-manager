import { Permission } from "@/types/permissions";
import React, { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './UnifiedAuthContext';
import { shopService, ShopResponse } from '@/services/shopService';

interface ShopContextType {
  selectedShopId: string | null
  selectedShop: ShopResponse | null
  setSelectedShopId: (shopId: string) => void
  canManageMultipleShops: boolean
  isLoadingShop: boolean
}

const ShopContext = createContext<ShopContextType | undefined>(undefined)

interface ShopProviderProps {
  children: ReactNode
}

export const ShopProvider: React.FC<ShopProviderProps> = ({ children }) => {
  const { user, hasAnyPermission } = useAuth()
  
  const canManageMultipleShops = hasAnyPermission([Permission.SHOP_LIST, Permission.SHOP_LIST_ALL])

  const [selectedShopId, setSelectedShopIdState] = useState<string | null>(() => {
    const stored = localStorage.getItem('selectedShopId')
    if (stored) return stored
    
    return user?.shopId || null
  })
  
  const [selectedShop, setSelectedShop] = useState<ShopResponse | null>(null)
  const [isLoadingShop, setIsLoadingShop] = useState(false)
  
  useEffect(() => {
    if (user?.shopId) {
      setSelectedShopIdState(user.shopId)
      localStorage.setItem('selectedShopId', user.shopId)
    }
  }, [user?.shopId])
  
  // Fetch shop details when selectedShopId changes
  useEffect(() => {
    const fetchShopDetails = async () => {
      if (!selectedShopId) {
        setSelectedShop(null)
        return
      }
      
      setIsLoadingShop(true)
      try {
        const shopData = await shopService.getShopById(selectedShopId)
        setSelectedShop(shopData)
        // Store in localStorage for quick access
        localStorage.setItem('selectedShop', JSON.stringify(shopData))
      } catch (error) {
        console.error('Failed to fetch shop details:', error)
        setSelectedShop(null)
      } finally {
        setIsLoadingShop(false)
      }
    }
    
    fetchShopDetails()
  }, [selectedShopId])
  
  const setSelectedShopId = (shopId: string) => {
    setSelectedShopIdState(shopId)
    localStorage.setItem('selectedShopId', shopId)
  }
  
  const value = useMemo(
    () => ({ 
      selectedShopId, 
      selectedShop, 
      setSelectedShopId, 
      canManageMultipleShops,
      isLoadingShop 
    }),
    [selectedShopId, selectedShop, canManageMultipleShops, isLoadingShop]
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
