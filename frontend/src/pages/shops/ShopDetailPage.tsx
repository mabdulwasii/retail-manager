import React from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  ArrowLeft,
  Store,
  MapPin,
  Mail,
  Phone,
  Calendar,
  Edit,
  Settings as SettingsIcon,
  Loader2,
  AlertCircle,
  Building2,
  Hash,
  Globe,
  FileText,
  TrendingUp,
  Package,
  DollarSign,
  Users,
  Activity
} from 'lucide-react'
import { useShopById } from '@/hooks/useShops'
import { useSalesSummary, useInventorySummary, useExpenseSummary } from '@/hooks/useDashboard'
import { ShopStatusBadge, ShopMetricsCard } from '@/components/shops'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useUpdateShopStatus } from '@/hooks/useShops'

export const ShopDetailPage: React.FC = () => {
  const { shopId } = useParams<{ shopId: string }>()
  const navigate = useNavigate()
  
  const { data: shop, isLoading, isError, error } = useShopById(shopId)
  const { data: salesSummary, isLoading: loadingSales } = useSalesSummary(shopId, 'month')
  const { data: inventorySummary, isLoading: loadingInventory } = useInventorySummary(shopId)
  const { data: expenseSummary, isLoading: loadingExpenses } = useExpenseSummary(shopId, 'month')
  const updateStatusMutation = useUpdateShopStatus()

  const handleStatusChange = async (newStatus: string) => {
    if (!shopId) return
    await updateStatusMutation.mutateAsync({ shopId, status: newStatus })
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (isError || !shop) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" onClick={() => navigate('/shops')}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shops
        </Button>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error?.message || 'Shop not found'}
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={() => navigate('/shops')}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shops
        </Button>
        
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div className="flex items-start gap-4">
            <div className="p-3 bg-primary/10 rounded-lg">
              <Store className="h-8 w-8 text-primary" />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-3xl font-bold tracking-tight">{shop.name}</h1>
                <ShopStatusBadge status={shop.status} showIcon />
              </div>
              <p className="text-muted-foreground mt-1">
                {shop.description || 'No description available'}
              </p>
            </div>
          </div>

          <div className="flex gap-2">
            <Link to={`/shops/${shopId}/edit`}>
              <Button>
                <Edit className="mr-2 h-4 w-4" />
                Edit Shop
              </Button>
            </Link>
            
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="outline">
                  <SettingsIcon className="mr-2 h-4 w-4" />
                  Actions
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel>Shop Actions</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild>
                  <Link to={`/shops/${shopId}/settings`}>Shop Settings</Link>
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuLabel>Change Status</DropdownMenuLabel>
                {shop.status !== 'ACTIVE' && (
                  <DropdownMenuItem onClick={() => handleStatusChange('ACTIVE')}>
                    Set as Active
                  </DropdownMenuItem>
                )}
                {shop.status !== 'INACTIVE' && (
                  <DropdownMenuItem onClick={() => handleStatusChange('INACTIVE')}>
                    Set as Inactive
                  </DropdownMenuItem>
                )}
                {shop.status !== 'SUSPENDED' && (
                  <DropdownMenuItem onClick={() => handleStatusChange('SUSPENDED')}>
                    Suspend Shop
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <ShopMetricsCard
          title="Total Revenue"
          value={`$${salesSummary?.totalRevenue?.toLocaleString() || '0'}`}
          icon={<DollarSign className="h-4 w-4" />}
          subtitle="This month"
          loading={loadingSales}
        />

        <ShopMetricsCard
          title="Transactions"
          value={salesSummary?.totalTransactions || 0}
          icon={<Activity className="h-4 w-4" />}
          subtitle="This month"
          loading={loadingSales}
        />

        <ShopMetricsCard
          title="Inventory Items"
          value={inventorySummary?.totalItems || 0}
          icon={<Package className="h-4 w-4" />}
          subtitle={`${inventorySummary?.lowStockItems || 0} low stock`}
          loading={loadingInventory}
        />

        <ShopMetricsCard
          title="Monthly Expenses"
          value={`$${expenseSummary?.monthlyTotal?.toLocaleString() || '0'}`}
          icon={<TrendingUp className="h-4 w-4" />}
          subtitle={`${expenseSummary?.pendingApproval || 0} pending`}
          loading={loadingExpenses}
        />
      </div>

      {/* Detailed Information Tabs */}
      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="contact">Contact & Address</TabsTrigger>
          <TabsTrigger value="tax">Tax Information</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Shop Information</CardTitle>
              <CardDescription>Basic details about the shop</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Shop Name</p>
                  <div className="flex items-center gap-2">
                    <Store className="h-4 w-4 text-muted-foreground" />
                    <p className="text-base">{shop.name}</p>
                  </div>
                </div>

                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Status</p>
                  <ShopStatusBadge status={shop.status} showIcon />
                </div>

                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Shop ID</p>
                  <div className="flex items-center gap-2">
                    <Hash className="h-4 w-4 text-muted-foreground" />
                    <p className="text-base font-mono">{shop.id}</p>
                  </div>
                </div>

                {shop.openingDate && (
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">Opening Date</p>
                    <div className="flex items-center gap-2">
                      <Calendar className="h-4 w-4 text-muted-foreground" />
                      <p className="text-base">
                        {new Date(shop.openingDate).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </p>
                    </div>
                  </div>
                )}
              </div>

              {shop.description && (
                <>
                  <Separator />
                  <div className="space-y-1">
                    <p className="text-sm font-medium text-muted-foreground">Description</p>
                    <div className="flex items-start gap-2">
                      <FileText className="h-4 w-4 text-muted-foreground mt-0.5" />
                      <p className="text-base">{shop.description}</p>
                    </div>
                  </div>
                </>
              )}

              <Separator />
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Created At</p>
                  <p className="text-base">
                    {new Date(shop.createdAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </p>
                </div>

                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Last Updated</p>
                  <p className="text-base">
                    {new Date(shop.updatedAt).toLocaleDateString('en-US', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="contact" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Contact Information</CardTitle>
              <CardDescription>How to reach this shop</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-1">
                <p className="text-sm font-medium text-muted-foreground">Email Address</p>
                <div className="flex items-center gap-2">
                  <Mail className="h-4 w-4 text-muted-foreground" />
                  <a href={`mailto:${shop.email}`} className="text-base text-primary hover:underline">
                    {shop.email}
                  </a>
                </div>
              </div>

              {shop.phoneNumber && (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Phone Number</p>
                  <div className="flex items-center gap-2">
                    <Phone className="h-4 w-4 text-muted-foreground" />
                    <a href={`tel:${shop.phoneNumber}`} className="text-base text-primary hover:underline">
                      {shop.phoneNumber}
                    </a>
                  </div>
                </div>
              )}

              <Separator />

              <div className="space-y-1">
                <p className="text-sm font-medium text-muted-foreground">Full Address</p>
                <div className="flex items-start gap-2">
                  <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
                  <div className="space-y-1">
                    {shop.address && <p className="text-base">{shop.address}</p>}
                    <p className="text-base">
                      {[shop.city, shop.state, shop.postalCode].filter(Boolean).join(', ')}
                    </p>
                    {shop.country && (
                      <div className="flex items-center gap-2">
                        <Globe className="h-4 w-4 text-muted-foreground" />
                        <p className="text-base">{shop.country}</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="tax" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Tax Information</CardTitle>
              <CardDescription>Tax identification and compliance details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {shop.taxId ? (
                <div className="space-y-1">
                  <p className="text-sm font-medium text-muted-foreground">Tax ID / VAT Number</p>
                  <div className="flex items-center gap-2">
                    <Building2 className="h-4 w-4 text-muted-foreground" />
                    <p className="text-base font-mono">{shop.taxId}</p>
                  </div>
                </div>
              ) : (
                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    No tax ID has been set for this shop. Consider adding one in the shop settings.
                  </AlertDescription>
                </Alert>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common tasks for this shop</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <Button variant="outline" className="w-full justify-start" asChild>
              <Link to={`/shops/${shopId}/edit`}>
                <Edit className="mr-2 h-4 w-4" />
                Edit Shop Details
              </Link>
            </Button>
            <Button variant="outline" className="w-full justify-start" asChild>
              <Link to={`/shops/${shopId}/settings`}>
                <SettingsIcon className="mr-2 h-4 w-4" />
                Shop Settings
              </Link>
            </Button>
            <Button variant="outline" className="w-full justify-start" asChild>
              <Link to={`/inventory?shopId=${shopId}`}>
                <Package className="mr-2 h-4 w-4" />
                View Inventory
              </Link>
            </Button>
            <Button variant="outline" className="w-full justify-start" asChild>
              <Link to={`/sales?shopId=${shopId}`}>
                <Activity className="mr-2 h-4 w-4" />
                View Sales
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}