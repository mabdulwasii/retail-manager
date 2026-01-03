import React from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ShopLinkRequest } from '@/services/cloudAggregatorService';
import { Store, Plus, Trash2, Mail, MapPin, Globe, Phone } from 'lucide-react';

interface ShopFormFieldsProps {
  shops: ShopLinkRequest[];
  onChange: (shops: ShopLinkRequest[]) => void;
  errors?: Record<number, Record<string, string>>;
}

export const ShopFormFields: React.FC<ShopFormFieldsProps> = ({
  shops,
  onChange,
  errors = {},
}) => {
  const handleShopChange = (index: number, field: keyof ShopLinkRequest, value: string) => {
    const updatedShops = [...shops];
    updatedShops[index] = {
      ...updatedShops[index],
      [field]: value,
    };
    onChange(updatedShops);
  };

  const handleAddShop = () => {
    onChange([
      ...shops,
      {
        shopName: '',
        shopEmail: '',
        address: '',
        city: '',
        country: '',
        phoneNumber: '',
      },
    ]);
  };

  const handleRemoveShop = (index: number) => {
    if (shops.length === 1) {
      // Don't allow removing the last shop
      return;
    }
    const updatedShops = shops.filter((_, i) => i !== index);
    onChange(updatedShops);
  };

  return (
    <div className="space-y-4">
      {shops.map((shop, index) => (
        <Card key={index} className="relative">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2 text-lg">
                <Store className="h-5 w-5" />
                Shop {index + 1}
                {shops.length > 1 && index === 0 && (
                  <span className="text-xs font-normal text-muted-foreground">(Primary)</span>
                )}
              </CardTitle>
              {shops.length > 1 && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleRemoveShop(index)}
                  className="text-destructive hover:text-destructive"
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              )}
            </div>
          </CardHeader>

          <CardContent className="space-y-4">
            {/* Shop Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Shop Name <span className="text-destructive">*</span>
              </label>
              <div className="relative">
                <Store className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  value={shop.shopName}
                  onChange={(e) => handleShopChange(index, 'shopName', e.target.value)}
                  placeholder="Downtown Store"
                  className="pl-10"
                  required
                />
              </div>
              {errors[index]?.shopName && (
                <p className="text-xs text-destructive mt-1">{errors[index].shopName}</p>
              )}
            </div>

            {/* Shop Email */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Shop Email</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="email"
                  value={shop.shopEmail || ''}
                  onChange={(e) => handleShopChange(index, 'shopEmail', e.target.value)}
                  placeholder="shop@example.com"
                  className="pl-10"
                />
              </div>
              {errors[index]?.shopEmail && (
                <p className="text-xs text-destructive mt-1">{errors[index].shopEmail}</p>
              )}
            </div>

            {/* Address */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Address</label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  value={shop.address || ''}
                  onChange={(e) => handleShopChange(index, 'address', e.target.value)}
                  placeholder="123 Main Street"
                  className="pl-10"
                />
              </div>
            </div>

            {/* City and Country */}
            <div className="grid md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">City</label>
                <Input
                  value={shop.city || ''}
                  onChange={(e) => handleShopChange(index, 'city', e.target.value)}
                  placeholder="New York"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Country</label>
                <div className="relative">
                  <Globe className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                  <Input
                    value={shop.country || ''}
                    onChange={(e) => handleShopChange(index, 'country', e.target.value)}
                    placeholder="USA"
                    className="pl-10"
                  />
                </div>
              </div>
            </div>

            {/* Phone Number */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Phone Number</label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  type="tel"
                  value={shop.phoneNumber || ''}
                  onChange={(e) => handleShopChange(index, 'phoneNumber', e.target.value)}
                  placeholder="+1 (555) 123-4567"
                  className="pl-10"
                />
              </div>
            </div>
          </CardContent>
        </Card>
      ))}

      {/* Add Shop Button */}
      <Button
        type="button"
        variant="outline"
        onClick={handleAddShop}
        className="w-full"
      >
        <Plus className="h-4 w-4 mr-2" />
        Add Another Shop
      </Button>

      <p className="text-xs text-muted-foreground text-center">
        You can add more shops later from your dashboard
      </p>
    </div>
  );
};
