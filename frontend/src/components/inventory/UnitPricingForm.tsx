import { Label } from "@/components/ui/label";
import { NumericInput } from "@/components/ui/numeric-input";
import { useCurrency } from "@/hooks/useCurrency";
import {
  InventoryUnitPriceRequest,
  ProductUnitDefinition,
} from "@/types/api";
import { Info } from "lucide-react";
import React, { useEffect, useState } from "react";

interface UnitPricingFormProps {
  productName?: string;
  unitDefinitions: ProductUnitDefinition[];
  unitPrices: InventoryUnitPriceRequest[];
  onChange: (unitPrices: InventoryUnitPriceRequest[]) => void;
  disabled?: boolean;
}

export const UnitPricingForm: React.FC<UnitPricingFormProps> = ({
  productName,
  unitDefinitions,
  unitPrices,
  onChange,
  disabled = false,
}) => {
  const { formatCurrency } = useCurrency();
  const [localPrices, setLocalPrices] = useState<
    InventoryUnitPriceRequest[]
  >(unitPrices);

  useEffect(() => {
    setLocalPrices(unitPrices);
  }, [unitPrices]);

  const handlePriceChange = (unitType: string, sellingPrice: number) => {
    const existingIndex = localPrices.findIndex(
      (p) => p.unitType === unitType
    );

    let updatedPrices: InventoryUnitPriceRequest[];

    if (existingIndex >= 0) {
      // Update existing price
      updatedPrices = [...localPrices];
      updatedPrices[existingIndex] = { unitType, sellingPrice };
    } else {
      // Add new price
      updatedPrices = [...localPrices, { unitType, sellingPrice }];
    }

    setLocalPrices(updatedPrices);
    onChange(updatedPrices);
  };

  const getPrice = (unitType: string): number => {
    const priceEntry = localPrices.find((p) => p.unitType === unitType);
    return priceEntry?.sellingPrice || 0;
  };

  const calculatePricePerBaseUnit = (
    unitType: string,
    sellingPrice: number
  ): number => {
    const unitDef = unitDefinitions.find((u) => u.unitType === unitType);
    if (!unitDef || unitDef.conversionFactor === 0) return 0;
    return sellingPrice / unitDef.conversionFactor;
  };

  // Sort unit definitions by sort order
  const sortedUnits = [...unitDefinitions].sort(
    (a, b) => a.sortOrder - b.sortOrder
  );

  if (unitDefinitions.length === 0) {
    return (
      <div className="space-y-4">
        <Label className="text-base font-semibold">Unit Pricing</Label>
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-800 px-4 py-3 rounded flex items-start">
          <Info className="h-5 w-5 mr-2 mt-0.5 flex-shrink-0" />
          <p className="text-sm">
            This product has no unit definitions. Please add unit definitions to
            the product first to enable multi-unit pricing.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div>
        <Label className="text-base font-semibold">Unit Pricing</Label>
        <p className="text-sm text-gray-500 mt-1">
          {productName
            ? `Set selling prices for each unit of ${productName}`
            : "Set selling prices for each unit type"}
        </p>
      </div>

      <div className="bg-blue-50 border border-blue-200 px-4 py-3 rounded flex items-start">
        <Info className="h-5 w-5 mr-2 mt-0.5 flex-shrink-0 text-blue-600" />
        <div className="text-sm text-blue-800">
          <p className="font-semibold mb-1">Pricing Guidelines:</p>
          <ul className="list-disc list-inside space-y-1 text-xs">
            <li>
              Enter the selling price for each unit type (NOT automatically
              calculated)
            </li>
            <li>
              Piece prices are typically HIGHER than pack price ÷ pack size
            </li>
            <li>
              Example: Pack of 12 @ ₦12,000 → Piece @ ₦1,050 (not ₦1,000)
            </li>
            <li>Price per base unit shown for comparison purposes only</li>
          </ul>
        </div>
      </div>

      <div className="space-y-3">
        {sortedUnits.map((unitDef) => {
          const price = getPrice(unitDef.unitType);
          const pricePerBase = calculatePricePerBaseUnit(
            unitDef.unitType,
            price
          );

          return (
            <div
              key={unitDef.unitType}
              className={`border rounded-lg p-4 ${
                unitDef.isBaseUnit ? "bg-blue-50 border-blue-200" : "bg-white"
              }`}
            >
              <div className="grid grid-cols-12 gap-4 items-start">
                {/* Unit Info */}
                <div className="col-span-5">
                  <Label className="text-sm font-semibold">
                    {unitDef.unitLabel}
                    {unitDef.isBaseUnit && (
                      <span className="ml-2 text-xs font-normal text-blue-600">
                        (Base Unit)
                      </span>
                    )}
                  </Label>
                  <p className="text-xs text-gray-500 mt-1">
                    Conversion: {unitDef.conversionFactor} base units
                  </p>
                </div>

                {/* Selling Price Input */}
                <div className="col-span-4">
                  <Label className="text-xs">Selling Price*</Label>
                  <NumericInput
                    value={price}
                    onChange={(value) =>
                      handlePriceChange(unitDef.unitType, value)
                    }
                    disabled={disabled}
                    min={0}
                    step={0.01}
                    decimals={2}
                    className="mt-1"
                    placeholder="0.00"
                  />
                </div>

                {/* Price Per Base Unit (Calculated) */}
                <div className="col-span-3">
                  <Label className="text-xs">Price/Base Unit</Label>
                  <div className="mt-1 px-3 py-2 bg-gray-100 rounded text-sm font-mono text-right">
                    {price > 0 ? formatCurrency(pricePerBase) : "—"}
                  </div>
                </div>
              </div>

              {/* Warning for unusual pricing */}
              {!unitDef.isBaseUnit && price > 0 && pricePerBase > 0 && (
                <div className="mt-3">
                  {(() => {
                    // Get base unit price
                    const baseUnit = unitDefinitions.find((u) => u.isBaseUnit);
                    if (!baseUnit) return null;

                    const baseUnitPrice = getPrice(baseUnit.unitType);
                    if (baseUnitPrice === 0) return null;

                    const baseUnitPricePerBase = calculatePricePerBaseUnit(
                      baseUnit.unitType,
                      baseUnitPrice
                    );

                    // Check if this unit's price per base is significantly lower
                    // (which would be unusual - bulk pricing shouldn't be higher per unit)
                    const ratio = pricePerBase / baseUnitPricePerBase;

                    if (ratio > 1.1) {
                      return (
                        <div className="bg-yellow-50 border border-yellow-200 px-3 py-2 rounded flex items-start">
                          <Info className="h-4 w-4 mr-2 mt-0.5 flex-shrink-0 text-yellow-600" />
                          <p className="text-xs text-yellow-800">
                            <strong>Note:</strong> This unit's price per base
                            unit ({formatCurrency(pricePerBase)}) is higher than
                            the base unit price ({formatCurrency(baseUnitPricePerBase)}).
                            This is typical for retail—customers pay more per piece when
                            buying in smaller quantities.
                          </p>
                        </div>
                      );
                    }

                    return null;
                  })()}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {localPrices.length === 0 && (
        <div className="bg-yellow-50 border border-yellow-200 px-4 py-3 rounded">
          <p className="text-sm text-yellow-800">
            No prices set yet. Please set at least one selling price.
          </p>
        </div>
      )}
    </div>
  );
};
