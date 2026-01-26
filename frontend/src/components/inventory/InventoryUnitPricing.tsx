import React, { useState, useEffect } from 'react';
import { Label } from '@/components/ui/label';
import { NumericInput } from '@/components/ui/numeric-input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { AlertCircle, Package } from 'lucide-react';
import { Product, ProductUnitDefinition } from '@/types/api';

export interface UnitPrice {
  unitType: string;
  sellingPrice: number;
}

interface InventoryUnitPricingProps {
  product: Product | undefined;
  costPrice: string;
  onCostPriceChange: (value: string) => void;
  unitPrices: UnitPrice[];
  onUnitPricesChange: (unitPrices: UnitPrice[]) => void;
  errors?: {
    costPrice?: string;
    unitPrices?: string;
  };
}

export const InventoryUnitPricing: React.FC<InventoryUnitPricingProps> = ({
  product,
  costPrice,
  onCostPriceChange,
  unitPrices,
  onUnitPricesChange,
  errors,
}) => {
  const [localPrices, setLocalPrices] = useState<Record<string, string>>({});

  // Initialize local prices when product or unitPrices change
  useEffect(() => {
    if (product?.unitDefinitions && unitPrices.length > 0) {
      const priceMap: Record<string, string> = {};
      unitPrices.forEach((up) => {
        priceMap[up.unitType] = up.sellingPrice.toString();
      });
      setLocalPrices(priceMap);
    }
  }, [product, unitPrices]);

  const handlePriceChange = (unitType: string, value: string) => {
    // Update local state
    const newPrices = { ...localPrices, [unitType]: value };
    setLocalPrices(newPrices);

    // Convert to UnitPrice array and notify parent
    const updatedUnitPrices: UnitPrice[] = [];

    product?.unitDefinitions?.forEach((unit) => {
      const priceValue = newPrices[unit.unitType];
      if (priceValue && priceValue.trim() !== '' && parseFloat(priceValue) > 0) {
        updatedUnitPrices.push({
          unitType: unit.unitType,
          sellingPrice: parseFloat(priceValue),
        });
      }
    });

    onUnitPricesChange(updatedUnitPrices);
  };

  // If product has no unit definitions, show traditional pricing
  if (!product?.unitDefinitions || product.unitDefinitions.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Pricing Information</CardTitle>
          <CardDescription>
            This product has no unit definitions. Using traditional pricing.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="costPrice">
              Cost Price <span className="text-red-500">*</span>
            </Label>
            <NumericInput
              id="costPrice"
              value={costPrice}
              onValueChange={(values) => onCostPriceChange(values.value || '')}
              placeholder="0.00"
              prefix="₦ "
              decimalScale={2}
              fixedDecimalScale={false}
              allowNegative={false}
            />
            <p className="text-xs text-muted-foreground">
              Purchase price per unit
            </p>
            {errors?.costPrice && (
              <p className="text-sm text-red-500 flex items-center gap-1">
                <AlertCircle className="h-3 w-3" />
                {errors.costPrice}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="baseUnitPrice">
              Selling Price <span className="text-red-500">*</span>
            </Label>
            <NumericInput
              id="baseUnitPrice"
              value={localPrices['piece'] || ''}
              onValueChange={(values) => handlePriceChange('piece', values.value || '')}
              placeholder="0.00"
              prefix="₦ "
              decimalScale={2}
              fixedDecimalScale={false}
              allowNegative={false}
            />
            <p className="text-xs text-muted-foreground">
              Retail price per piece
            </p>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Sort unit definitions by sortOrder
  const sortedUnits = [...product.unitDefinitions].sort(
    (a, b) => a.sortOrder - b.sortOrder
  );

  return (
    <Card>
      <CardHeader>
        <CardTitle>Pricing Information</CardTitle>
        <CardDescription>
          Set prices for each unit type defined for this product
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Cost Price - batch level */}
        <div className="space-y-2">
          <Label htmlFor="costPrice">
            Cost Price (Base Unit) <span className="text-red-500">*</span>
          </Label>
          <NumericInput
            id="costPrice"
            value={costPrice}
            onValueChange={(values) => onCostPriceChange(values.value || '')}
            placeholder="0.00"
            prefix="₦ "
            decimalScale={2}
            fixedDecimalScale={false}
            allowNegative={false}
          />
          <p className="text-xs text-muted-foreground">
            Purchase price per base unit ({product.unitDefinitions.find(u => u.isBaseUnit)?.unitLabel || 'piece'})
          </p>
          {errors?.costPrice && (
            <p className="text-sm text-red-500 flex items-center gap-1">
              <AlertCircle className="h-3 w-3" />
              {errors.costPrice}
            </p>
          )}
        </div>

        {/* Unit-specific selling prices */}
        <div className="space-y-4">
          <div className="flex items-center gap-2 text-sm font-medium">
            <Package className="h-4 w-4" />
            <span>Selling Prices by Unit Type</span>
          </div>

          <div className="space-y-3 pl-6 border-l-2 border-muted">
            {sortedUnits.map((unit) => (
              <div key={unit.id} className="space-y-2">
                <Label htmlFor={`price-${unit.unitType}`}>
                  {unit.unitLabel}
                  {unit.isBaseUnit && <span className="text-red-500"> *</span>}
                </Label>
                <div className="flex items-start gap-2">
                  <NumericInput
                    id={`price-${unit.unitType}`}
                    value={localPrices[unit.unitType] || ''}
                    onValueChange={(values) =>
                      handlePriceChange(unit.unitType, values.value || '')
                    }
                    placeholder="0.00"
                    prefix="₦ "
                    decimalScale={2}
                    fixedDecimalScale={false}
                    allowNegative={false}
                    className="flex-1"
                  />
                  <div className="text-xs text-muted-foreground pt-2 min-w-[80px]">
                    ×{unit.conversionFactor}
                  </div>
                </div>
                {unit.isBaseUnit && (
                  <p className="text-xs text-muted-foreground">
                    Base unit - required
                  </p>
                )}
              </div>
            ))}
          </div>

          {errors?.unitPrices && (
            <p className="text-sm text-red-500 flex items-center gap-1">
              <AlertCircle className="h-3 w-3" />
              {errors.unitPrices}
            </p>
          )}

          <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-md p-3 mt-4">
            <p className="text-xs text-blue-700 dark:text-blue-400">
              <strong>Note:</strong> You can set different selling prices for each unit type.
              At minimum, set the price for the base unit. Optional unit types can be left blank.
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
