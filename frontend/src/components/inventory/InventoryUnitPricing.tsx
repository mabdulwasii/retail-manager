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
  // Purchase unit fields
  purchaseUnit?: string;
  onPurchaseUnitChange?: (unit: string) => void;
  purchaseQuantity?: string;
  onPurchaseQuantityChange?: (quantity: string) => void;
  purchaseUnitCost?: string;
  onPurchaseUnitCostChange?: (cost: string) => void;
  errors?: {
    costPrice?: string;
    unitPrices?: string;
    purchaseUnit?: string;
    purchaseQuantity?: string;
    purchaseUnitCost?: string;
  };
}

export const InventoryUnitPricing: React.FC<InventoryUnitPricingProps> = ({
  product,
  costPrice,
  onCostPriceChange,
  unitPrices,
  onUnitPricesChange,
  purchaseUnit,
  onPurchaseUnitChange,
  purchaseQuantity,
  onPurchaseQuantityChange,
  purchaseUnitCost,
  onPurchaseUnitCostChange,
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
        {/* Purchase Unit Selection */}
        <div className="space-y-4 pb-4 border-b">
          <div className="flex items-center gap-2 text-sm font-medium text-blue-700 dark:text-blue-400">
            <Package className="h-4 w-4" />
            <span>Purchase Information</span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pl-6">
            {/* Purchase Unit Selector */}
            <div className="space-y-2">
              <Label htmlFor="purchaseUnit">
                Purchase Unit <span className="text-red-500">*</span>
              </Label>
              <select
                id="purchaseUnit"
                value={purchaseUnit || ''}
                onChange={(e) => onPurchaseUnitChange?.(e.target.value)}
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <option value="">Select unit...</option>
                {sortedUnits.map((unit) => (
                  <option key={unit.id} value={unit.unitType}>
                    {unit.unitLabel} (×{unit.conversionFactor})
                  </option>
                ))}
              </select>
              <p className="text-xs text-muted-foreground">
                Unit you buy in (pack, carton, etc.)
              </p>
            </div>

            {/* Purchase Quantity */}
            <div className="space-y-2">
              <Label htmlFor="purchaseQuantity">
                Quantity Purchased
              </Label>
              <NumericInput
                id="purchaseQuantity"
                value={purchaseQuantity || ''}
                onValueChange={(values) => onPurchaseQuantityChange?.(values.value || '')}
                placeholder="0"
                decimalScale={0}
                allowNegative={false}
              />
              <p className="text-xs text-muted-foreground">
                How many units purchased
              </p>
            </div>

            {/* Purchase Unit Cost */}
            <div className="space-y-2">
              <Label htmlFor="purchaseUnitCost">
                Cost per Unit <span className="text-red-500">*</span>
              </Label>
              <NumericInput
                id="purchaseUnitCost"
                value={purchaseUnitCost || ''}
                onValueChange={(values) => onPurchaseUnitCostChange?.(values.value || '')}
                placeholder="0.00"
                prefix="₦ "
                decimalScale={2}
                fixedDecimalScale={false}
                allowNegative={false}
              />
              <p className="text-xs text-muted-foreground">
                Price per {purchaseUnit ? sortedUnits.find(u => u.unitType === purchaseUnit)?.unitLabel : 'unit'}
              </p>
              {errors?.purchaseUnitCost && (
                <p className="text-sm text-red-500 flex items-center gap-1">
                  <AlertCircle className="h-3 w-3" />
                  {errors.purchaseUnitCost}
                </p>
              )}
            </div>
          </div>

          {/* Auto-calculated base cost */}
          {purchaseUnit && purchaseUnitCost && (
            <div className="bg-blue-50 dark:bg-blue-900/10 border border-blue-200 dark:border-blue-800 rounded-md p-3 ml-6">
              <p className="text-sm text-blue-700 dark:text-blue-400">
                <strong>Cost per base unit:</strong> {(() => {
                  const selectedUnit = sortedUnits.find(u => u.unitType === purchaseUnit);
                  if (selectedUnit && parseFloat(purchaseUnitCost) > 0) {
                    const baseUnitCost = parseFloat(purchaseUnitCost) / selectedUnit.conversionFactor;
                    return `₦${baseUnitCost.toFixed(2)} per ${product.unitDefinitions.find(u => u.isBaseUnit)?.unitLabel || 'piece'}`;
                  }
                  return 'N/A';
                })()}
              </p>
            </div>
          )}
        </div>

        {/* Hidden cost price field for backward compatibility */}
        <input type="hidden" value={costPrice} />

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
