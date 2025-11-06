import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Input } from '@/components/ui/input'
import { 
  Plus, 
  Store, 
  MapPin, 
  Calendar, 
  Mail, 
  Phone, 
  Settings, 
  AlertCircle,
  Loader2,
  Search,
  Eye
} from 'lucide-react'
import { useShops, useUpdateShopStatus } from '@/hooks/useShops'
import { ShopResponse } from '@/services/shopService'
import { ShopStatusBadge } from '@/components/shops'
import { useAuth } from '@/context/ManualAuthContext'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

export const ShopsPage: React.FC = () => {
  const { hasPermission } = useAuth()
  const [page] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')
  
  const { data: shopsData, isLoading, isError, error } = useShops(page, 20)
  const updateStatusMutation = useUpdateShopStatus()
  
  // Check permissions based on backend permission matrix
  const canCreateShop = hasPermission('SHOP_CREATE')       // TENANT_ADMIN and above
  const canUpdateShop = hasPermission('SHOP_UPDATE')       // OWNER and above
  const canDeleteShop = hasPermission('SHOP_DELETE')       // TENANT_ADMIN and above

  const shops = shopsData?.content || []
  const totalElements = shopsData?.totalElements || 0

  // Filter shops based on search query
  const filteredShops = shops.filter((shop) => {
    if (!searchQuery) return true
    const query = searchQuery.toLowerCase()
    return (
      shop.name.toLowerCase().includes(query) ||
      shop.email.toLowerCase().includes(query) ||
      shop.address?.toLowerCase().includes(query) ||
      shop.city?.toLowerCase().includes(query)
    )
  })


  const handleStatusChange = async (shopId: string, newStatus: string) => {
    await updateStatusMutation.mutateAsync({ shopId, status: newStatus })
  }

  // Calculate stats
  const activeCount = shops.filter((s) => s.status === 'ACTIVE').length
  const inactiveCount = shops.filter((s) => s.status === 'INACTIVE').length
  const suspendedCount = shops.filter((s) => s.status === 'SUSPENDED').length

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Shops</h1>
          <p className="text-muted-foreground mt-1">
            Manage your retail locations and shop settings
          </p>
        </div>
        {canCreateShop && (
          <Link to="/shops/create">
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create Shop
            </Button>
          </Link>
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Shops</CardTitle>
            <Store className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalElements}</div>
            <p className="text-xs text-muted-foreground mt-1">
              All registered shops
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active</CardTitle>
            <Store className="h-4 w-4 text-green-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">{activeCount}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Currently operating
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Inactive</CardTitle>
            <Store className="h-4 w-4 text-yellow-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-yellow-600">{inactiveCount}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Temporarily closed
            </p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Suspended</CardTitle>
            <Store className="h-4 w-4 text-red-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-red-600">{suspendedCount}</div>
            <p className="text-xs text-muted-foreground mt-1">
              Under review
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Search Bar */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search shops..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-9"
        />
      </div>

      {/* Error State */}
      {isError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Failed to load shops: {error?.message || 'An error occurred'}
          </AlertDescription>
        </Alert>
      )}

      {/* Loading State */}
      {isLoading && (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredShops.length === 0 && (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Store className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">
              {searchQuery ? 'No shops found' : 'No shops yet'}
            </h3>
            <p className="text-muted-foreground text-center mb-4">
              {searchQuery 
                ? 'Try adjusting your search criteria' 
                : 'Get started by creating your first shop'}
            </p>
            {!searchQuery && canCreateShop && (
              <Link to="/shops/create">
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Create Shop
                </Button>
              </Link>
            )}
          </CardContent>
        </Card>
      )}

      {/* Shops Grid */}
      {!isLoading && filteredShops.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredShops.map((shop) => (
            <ShopCard 
              key={shop.id} 
              shop={shop} 
              onStatusChange={handleStatusChange}
              isUpdatingStatus={updateStatusMutation.isPending}
              canUpdateShop={canUpdateShop}
              canDeleteShop={canDeleteShop}
            />
          ))}
        </div>
      )}
    </div>
  )
}

interface ShopCardProps {
  shop: ShopResponse
  onStatusChange: (shopId: string, status: string) => void
  isUpdatingStatus: boolean
  canUpdateShop: boolean
  canDeleteShop: boolean
}

const ShopCard: React.FC<ShopCardProps> = ({ shop, onStatusChange, isUpdatingStatus, canUpdateShop, canDeleteShop }) => {
  const formatAddress = () => {
    const parts = [shop.address, shop.city, shop.state, shop.country].filter(Boolean)
    return parts.join(', ') || 'No address'
  }

  return (
    <Card className="hover:shadow-lg transition-shadow duration-200">
      <CardHeader>
        <div className="flex justify-between items-start gap-2">
          <div className="flex-1 min-w-0">
            <CardTitle className="text-lg truncate">{shop.name}</CardTitle>
            <CardDescription className="line-clamp-2">
              {shop.description || 'No description'}
            </CardDescription>
          </div>
          <ShopStatusBadge status={shop.status} showIcon />
        </div>
      </CardHeader>
      
      <CardContent className="space-y-3">
        <div className="space-y-2 text-sm">
          <div className="flex items-center text-muted-foreground">
            <MapPin className="mr-2 h-4 w-4 flex-shrink-0" />
            <span className="truncate">{formatAddress()}</span>
          </div>
          
          {shop.email && (
            <div className="flex items-center text-muted-foreground">
              <Mail className="mr-2 h-4 w-4 flex-shrink-0" />
              <span className="truncate">{shop.email}</span>
            </div>
          )}
          
          {shop.phoneNumber && (
            <div className="flex items-center text-muted-foreground">
              <Phone className="mr-2 h-4 w-4 flex-shrink-0" />
              <span className="truncate">{shop.phoneNumber}</span>
            </div>
          )}
          
          {shop.openingDate && (
            <div className="flex items-center text-muted-foreground">
              <Calendar className="mr-2 h-4 w-4 flex-shrink-0" />
              <span>Opened: {new Date(shop.openingDate).toLocaleDateString()}</span>
            </div>
          )}
        </div>

        <div className="flex gap-2 pt-2">
          <Link to={`/shops/${shop.id}`} className="flex-1">
            <Button variant="outline" className="w-full">
              <Eye className="mr-2 h-4 w-4" />
              View Details
            </Button>
          </Link>
          
          {(canUpdateShop || canDeleteShop) && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline" size="icon" disabled={isUpdatingStatus}>
                  {isUpdatingStatus ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Settings className="h-4 w-4" />
                  )}
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel>Actions</DropdownMenuLabel>
                <DropdownMenuSeparator />
                
                {canUpdateShop && (
                  <>
                    <DropdownMenuItem asChild>
                      <Link to={`/shops/${shop.id}/edit`}>Edit Shop</Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuLabel>Change Status</DropdownMenuLabel>
                    {shop.status !== 'ACTIVE' && (
                      <DropdownMenuItem onClick={() => onStatusChange(shop.id, 'ACTIVE')}>
                        Set as Active
                      </DropdownMenuItem>
                    )}
                    {shop.status !== 'INACTIVE' && (
                      <DropdownMenuItem onClick={() => onStatusChange(shop.id, 'INACTIVE')}>
                        Set as Inactive
                      </DropdownMenuItem>
                    )}
                    {shop.status !== 'SUSPENDED' && (
                      <DropdownMenuItem onClick={() => onStatusChange(shop.id, 'SUSPENDED')}>
                        Suspend Shop
                      </DropdownMenuItem>
                    )}
                  </>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </CardContent>
    </Card>
  )
}