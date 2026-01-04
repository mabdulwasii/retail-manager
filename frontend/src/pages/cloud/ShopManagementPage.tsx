import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Store,
  Plus,
  Edit,
  Power,
  PowerOff,
  Search,
  Mail,
  Phone,
  MapPin,
  CheckCircle,
  AlertCircle,
  Loader,
} from 'lucide-react';
import { CloudShop, CloudShopStatus, ShopLinkRequest } from '@/services/cloudAggregatorService';
import cloudAggregatorService from '@/services/cloudAggregatorService';
import { format } from 'date-fns';

/**
 * Shop Management Page
 * CRUD operations for managing shops within a tenant
 *
 * Features:
 * - List all shops with filtering/search
 * - Create new shop
 * - Edit shop details
 * - Activate/deactivate shops
 * - Real-time search and filtering
 * - Status badges
 */

interface ShopFormData {
  shopName: string;
  shopEmail: string;
  address?: string;
  city?: string;
  country?: string;
  phoneNumber?: string;
}

export const ShopManagementPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [shops, setShops] = useState<CloudShop[]>([]);
  const [filteredShops, setFilteredShops] = useState<CloudShop[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedShop, setSelectedShop] = useState<CloudShop | null>(null);

  // Form state
  const [formData, setFormData] = useState<ShopFormData>({
    shopName: '',
    shopEmail: '',
    address: '',
    city: '',
    country: '',
    phoneNumber: '',
  });

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  useEffect(() => {
    loadShops();
  }, [tenantId]);

  useEffect(() => {
    filterShops();
  }, [shops, searchQuery, statusFilter]);

  const loadShops = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // TODO: Replace with actual API call
      // const shopsData = await cloudAggregatorService.getShopsByTenant(tenantId);

      // Mock data for now
      const mockShops: CloudShop[] = [
        {
          id: '1',
          cloudTenantId: tenantId,
          shopName: 'Downtown Store',
          shopEmail: 'downtown@demoretail.com',
          status: CloudShopStatus.ACTIVE,
          address: '100 Main St',
          city: 'New York',
          country: 'USA',
          phoneNumber: '+1-555-0101',
          createdAt: '2024-01-15T10:00:00Z',
          updatedAt: '2024-12-20T15:30:00Z',
        },
        {
          id: '2',
          cloudTenantId: tenantId,
          shopName: 'Uptown Branch',
          shopEmail: 'uptown@demoretail.com',
          status: CloudShopStatus.ACTIVE,
          address: '200 Park Ave',
          city: 'New York',
          country: 'USA',
          phoneNumber: '+1-555-0102',
          createdAt: '2024-02-10T09:00:00Z',
          updatedAt: '2024-12-22T11:00:00Z',
        },
        {
          id: '3',
          cloudTenantId: tenantId,
          shopName: 'Westside Mall',
          shopEmail: 'westside@demoretail.com',
          status: CloudShopStatus.INACTIVE,
          address: '300 Broadway',
          city: 'New York',
          country: 'USA',
          phoneNumber: '+1-555-0103',
          createdAt: '2024-03-05T14:00:00Z',
          updatedAt: '2024-11-30T16:45:00Z',
        },
      ];

      setShops(mockShops);
    } catch (err) {
      console.error('Failed to load shops:', err);
      setError('Failed to load shops. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const filterShops = () => {
    let filtered = [...shops];

    // Search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (shop) =>
          shop.shopName.toLowerCase().includes(query) ||
          shop.shopEmail.toLowerCase().includes(query) ||
          shop.city?.toLowerCase().includes(query) ||
          shop.country?.toLowerCase().includes(query)
      );
    }

    // Status filter
    if (statusFilter !== 'ALL') {
      filtered = filtered.filter((shop) => shop.status === statusFilter);
    }

    setFilteredShops(filtered);
  };

  const resetForm = () => {
    setFormData({
      shopName: '',
      shopEmail: '',
      address: '',
      city: '',
      country: '',
      phoneNumber: '',
    });
  };

  const handleCreateShop = () => {
    resetForm();
    setIsCreateDialogOpen(true);
    setError(null);
    setSuccess(null);
  };

  const handleEditShop = (shop: CloudShop) => {
    setSelectedShop(shop);
    setFormData({
      shopName: shop.shopName,
      shopEmail: shop.shopEmail,
      address: shop.address || '',
      city: shop.city || '',
      country: shop.country || '',
      phoneNumber: shop.phoneNumber || '',
    });
    setIsEditDialogOpen(true);
    setError(null);
    setSuccess(null);
  };

  const handleSaveNewShop = async () => {
    try {
      setIsSaving(true);
      setError(null);

      // Validate required fields
      if (!formData.shopName || !formData.shopEmail) {
        setError('Shop name and email are required');
        return;
      }

      // Email validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.shopEmail)) {
        setError('Please enter a valid email address');
        return;
      }

      const shopRequest: ShopLinkRequest = {
        shopName: formData.shopName,
        shopEmail: formData.shopEmail,
        address: formData.address,
        city: formData.city,
        country: formData.country,
        phoneNumber: formData.phoneNumber,
      };

      // TODO: Replace with actual API call
      // await cloudAggregatorService.linkShop(tenantId, shopRequest);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));

      setSuccess('Shop created successfully');
      setIsCreateDialogOpen(false);
      resetForm();
      await loadShops();
    } catch (err) {
      console.error('Failed to create shop:', err);
      setError('Failed to create shop. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveEditShop = async () => {
    try {
      setIsSaving(true);
      setError(null);

      // Validate required fields
      if (!formData.shopName || !formData.shopEmail) {
        setError('Shop name and email are required');
        return;
      }

      // Email validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.shopEmail)) {
        setError('Please enter a valid email address');
        return;
      }

      // TODO: Replace with actual API call
      // await cloudAggregatorService.updateShop(selectedShop!.id, formData);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));

      setSuccess('Shop updated successfully');
      setIsEditDialogOpen(false);
      setSelectedShop(null);
      resetForm();
      await loadShops();
    } catch (err) {
      console.error('Failed to update shop:', err);
      setError('Failed to update shop. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleToggleShopStatus = async (shop: CloudShop) => {
    try {
      const newStatus =
        shop.status === CloudShopStatus.ACTIVE ? CloudShopStatus.INACTIVE : CloudShopStatus.ACTIVE;

      // TODO: Replace with actual API call
      // await cloudAggregatorService.updateShopStatus(shop.id, newStatus);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 500));

      setSuccess(`Shop ${newStatus === CloudShopStatus.ACTIVE ? 'activated' : 'deactivated'} successfully`);
      await loadShops();
    } catch (err) {
      console.error('Failed to toggle shop status:', err);
      setError('Failed to change shop status. Please try again.');
    }
  };

  const getStatusBadge = (status: CloudShopStatus) => {
    if (status === CloudShopStatus.ACTIVE) {
      return <Badge className="bg-green-500">Active</Badge>;
    } else if (status === CloudShopStatus.INACTIVE) {
      return <Badge variant="secondary">Inactive</Badge>;
    } else {
      return <Badge variant="destructive">Suspended</Badge>;
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Store className="h-8 w-8" />
            Shop Management
          </h1>
          <p className="text-muted-foreground mt-2">Manage all shop locations within your tenant</p>
        </div>
        <Card>
          <CardContent className="py-12">
            <div className="flex items-center justify-center">
              <div className="text-center">
                <Loader className="inline-block animate-spin h-12 w-12 text-primary" />
                <p className="mt-4 text-muted-foreground">Loading shops...</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Store className="h-8 w-8" />
            Shop Management
          </h1>
          <p className="text-muted-foreground mt-2">Manage all shop locations within your tenant</p>
        </div>
        <Button onClick={handleCreateShop}>
          <Plus className="h-4 w-4 mr-2" />
          Add Shop
        </Button>
      </div>

      {/* Success/Error Messages */}
      {success && (
        <Alert className="border-green-500 bg-green-50">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800">{success}</AlertDescription>
        </Alert>
      )}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filter Shops</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="search">Search</Label>
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="search"
                  className="pl-9"
                  placeholder="Search by name, email, city, or country..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger id="status">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Statuses</SelectItem>
                  <SelectItem value={CloudShopStatus.ACTIVE}>Active</SelectItem>
                  <SelectItem value={CloudShopStatus.INACTIVE}>Inactive</SelectItem>
                  <SelectItem value={CloudShopStatus.SUSPENDED}>Suspended</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Shops Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Shops ({filteredShops.length} of {shops.length})
          </CardTitle>
          <CardDescription>All retail locations linked to this tenant</CardDescription>
        </CardHeader>
        <CardContent>
          {filteredShops.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Store className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No shops found</h3>
              <p className="text-muted-foreground max-w-md mb-4">
                {searchQuery || statusFilter !== 'ALL'
                  ? 'No shops match your search criteria. Try adjusting your filters.'
                  : 'Get started by creating your first shop location.'}
              </p>
              {!searchQuery && statusFilter === 'ALL' && (
                <Button onClick={handleCreateShop}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Your First Shop
                </Button>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Shop Name</TableHead>
                    <TableHead>Contact</TableHead>
                    <TableHead>Location</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Last Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredShops.map((shop) => (
                    <TableRow key={shop.id}>
                      <TableCell className="font-medium">{shop.shopName}</TableCell>
                      <TableCell>
                        <div className="space-y-1">
                          <div className="flex items-center gap-2 text-sm">
                            <Mail className="h-3 w-3 text-muted-foreground" />
                            {shop.shopEmail}
                          </div>
                          {shop.phoneNumber && (
                            <div className="flex items-center gap-2 text-sm text-muted-foreground">
                              <Phone className="h-3 w-3" />
                              {shop.phoneNumber}
                            </div>
                          )}
                        </div>
                      </TableCell>
                      <TableCell>
                        {shop.city || shop.country ? (
                          <div className="flex items-center gap-2 text-sm">
                            <MapPin className="h-3 w-3 text-muted-foreground" />
                            {[shop.city, shop.country].filter(Boolean).join(', ')}
                          </div>
                        ) : (
                          <span className="text-muted-foreground text-sm">-</span>
                        )}
                      </TableCell>
                      <TableCell>{getStatusBadge(shop.status)}</TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {format(new Date(shop.updatedAt), 'MMM d, yyyy')}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleEditShop(shop)}
                          >
                            <Edit className="h-3 w-3 mr-1" />
                            Edit
                          </Button>
                          <Button
                            variant={shop.status === CloudShopStatus.ACTIVE ? 'outline' : 'default'}
                            size="sm"
                            onClick={() => handleToggleShopStatus(shop)}
                          >
                            {shop.status === CloudShopStatus.ACTIVE ? (
                              <>
                                <PowerOff className="h-3 w-3 mr-1" />
                                Deactivate
                              </>
                            ) : (
                              <>
                                <Power className="h-3 w-3 mr-1" />
                                Activate
                              </>
                            )}
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Create Shop Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Add New Shop</DialogTitle>
            <DialogDescription>Create a new shop location for your tenant</DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="create-shopName">
                  Shop Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="create-shopName"
                  value={formData.shopName}
                  onChange={(e) => setFormData({ ...formData, shopName: e.target.value })}
                  placeholder="Downtown Store"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="create-shopEmail">
                  Shop Email <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="create-shopEmail"
                  type="email"
                  value={formData.shopEmail}
                  onChange={(e) => setFormData({ ...formData, shopEmail: e.target.value })}
                  placeholder="shop@company.com"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="create-address">Street Address</Label>
              <Input
                id="create-address"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                placeholder="123 Main Street"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="create-city">City</Label>
                <Input
                  id="create-city"
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  placeholder="New York"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="create-country">Country</Label>
                <Input
                  id="create-country"
                  value={formData.country}
                  onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                  placeholder="USA"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="create-phoneNumber">Phone Number</Label>
              <Input
                id="create-phoneNumber"
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                placeholder="+1-555-0100"
              />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)} disabled={isSaving}>
              Cancel
            </Button>
            <Button onClick={handleSaveNewShop} disabled={isSaving}>
              {isSaving ? (
                <>
                  <Loader className="inline-block animate-spin h-4 w-4 mr-2" />
                  Creating...
                </>
              ) : (
                'Create Shop'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Edit Shop Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit Shop</DialogTitle>
            <DialogDescription>Update shop information</DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-shopName">
                  Shop Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="edit-shopName"
                  value={formData.shopName}
                  onChange={(e) => setFormData({ ...formData, shopName: e.target.value })}
                  placeholder="Downtown Store"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-shopEmail">
                  Shop Email <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="edit-shopEmail"
                  type="email"
                  value={formData.shopEmail}
                  onChange={(e) => setFormData({ ...formData, shopEmail: e.target.value })}
                  placeholder="shop@company.com"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-address">Street Address</Label>
              <Input
                id="edit-address"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                placeholder="123 Main Street"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-city">City</Label>
                <Input
                  id="edit-city"
                  value={formData.city}
                  onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                  placeholder="New York"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-country">Country</Label>
                <Input
                  id="edit-country"
                  value={formData.country}
                  onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                  placeholder="USA"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-phoneNumber">Phone Number</Label>
              <Input
                id="edit-phoneNumber"
                type="tel"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                placeholder="+1-555-0100"
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setIsEditDialogOpen(false);
                setSelectedShop(null);
              }}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button onClick={handleSaveEditShop} disabled={isSaving}>
              {isSaving ? (
                <>
                  <Loader className="inline-block animate-spin h-4 w-4 mr-2" />
                  Saving...
                </>
              ) : (
                'Save Changes'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default ShopManagementPage;
