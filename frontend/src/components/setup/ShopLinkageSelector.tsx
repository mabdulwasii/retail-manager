import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Store, MapPin, Mail, Phone, AlertCircle, CheckCircle } from 'lucide-react';
import { Shop } from '@/services/shopService';

interface ShopLinkageSelectorProps {
  shops: Shop[];
  selectedShopId: string | null;
  onSelect: (shopId: string) => void;
  isLoading?: boolean;
}

/**
 * Shop Linkage Selector
 * Allows user to select which local shop to link to cloud tenant
 */
export const ShopLinkageSelector: React.FC<ShopLinkageSelectorProps> = ({
  shops,
  selectedShopId,
  onSelect,
  isLoading = false,
}) => {
  if (isLoading) {
    return (
      <Card className="w-full max-w-2xl mx-auto">
        <CardContent className="pt-6">
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-muted-foreground">Loading shops...</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (shops.length === 0) {
    return (
      <Card className="w-full max-w-2xl mx-auto">
        <CardHeader>
          <CardTitle>No Shops Available</CardTitle>
          <CardDescription>
            You need to have at least one shop configured before linking to cloud
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              Please create a shop first, then return to the setup wizard to enable cloud sync.
            </AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    );
  }

  const selectedShop = shops.find(shop => shop.id === selectedShopId);

  return (
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader>
        <div className="flex items-center gap-2 mb-2">
          <Store className="h-6 w-6 text-blue-600" />
          <CardTitle>Select Shop to Link</CardTitle>
        </div>
        <CardDescription>
          Choose which shop location to sync with RetailHQ Cloud
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-6">
        {/* Shop Selection */}
        <RadioGroup value={selectedShopId || ''} onValueChange={onSelect}>
          <div className="space-y-3">
            {shops.map((shop) => (
              <div
                key={shop.id}
                className={`relative flex items-start space-x-3 rounded-lg border p-4 cursor-pointer transition-all hover:shadow-md ${
                  selectedShopId === shop.id
                    ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-500'
                    : 'border-gray-200 hover:border-blue-300'
                }`}
                onClick={() => onSelect(shop.id)}
              >
                <RadioGroupItem value={shop.id} id={shop.id} className="mt-1" />
                <Label htmlFor={shop.id} className="flex-1 cursor-pointer">
                  <div className="space-y-2">
                    {/* Shop Name */}
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <Store className="h-4 w-4 text-gray-500" />
                        <span className="font-semibold text-gray-900">{shop.name}</span>
                      </div>
                      {selectedShopId === shop.id && (
                        <CheckCircle className="h-5 w-5 text-blue-600" />
                      )}
                    </div>

                    {/* Shop Details */}
                    <div className="text-sm text-gray-600 space-y-1">
                      {shop.description && <p>{shop.description}</p>}

                      <div className="flex flex-wrap gap-x-4 gap-y-1 mt-2">
                        {shop.address && (
                          <div className="flex items-center gap-1">
                            <MapPin className="h-3 w-3" />
                            <span>
                              {shop.address}
                              {shop.city && `, ${shop.city}`}
                              {shop.country && `, ${shop.country}`}
                            </span>
                          </div>
                        )}

                        {shop.email && (
                          <div className="flex items-center gap-1">
                            <Mail className="h-3 w-3" />
                            <span>{shop.email}</span>
                          </div>
                        )}

                        {shop.phoneNumber && (
                          <div className="flex items-center gap-1">
                            <Phone className="h-3 w-3" />
                            <span>{shop.phoneNumber}</span>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Status Badge */}
                    <div>
                      <span
                        className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                          shop.status === 'ACTIVE'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {shop.status}
                      </span>
                    </div>
                  </div>
                </Label>
              </div>
            ))}
          </div>
        </RadioGroup>

        {/* Selection Summary */}
        {selectedShop && (
          <Alert className="bg-blue-50 border-blue-200">
            <CheckCircle className="h-4 w-4 text-blue-600" />
            <AlertDescription>
              <strong>{selectedShop.name}</strong> will be linked to your cloud tenant.
              All data from this shop will be synced to RetailHQ Cloud for centralized reporting and analytics.
            </AlertDescription>
          </Alert>
        )}

        {/* Info Alert */}
        {!selectedShopId && (
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <strong>Note:</strong> You can link additional shops later from your cloud dashboard.
              For now, select the primary shop you want to sync.
            </AlertDescription>
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};

export default ShopLinkageSelector;
