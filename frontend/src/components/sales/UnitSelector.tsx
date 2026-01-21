import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCurrency } from "@/hooks/useCurrency";
import {
  InventoryUnitPrice,
  ProductUnitDefinition,
} from "@/types/api";
import { Info, Package } from "lucide-react";
import React from "react";

interface UnitSelectorProps {
  productName: string;
  unitDefinitions: ProductUnitDefinition[];
  unitPrices: InventoryUnitPrice[];
  selectedUnitType?: string;
  onUnitChange: (unitType: string, unitPrice: number) => void;
  disabled?: boolean;
  showPriceBreakdown?: boolean;
  className?: string;
}

export const UnitSelector: React.FC<UnitSelectorProps> = ({
  productName,
  unitDefinitions,
  unitPrices,
  selectedUnitType,
  onUnitChange,
  disabled = false,
  showPriceBreakdown = true,
  className = "",
}) => {
  const { formatCurrency } = useCurrency();

  // Sort unit definitions by sort order
  const sortedUnits = [...unitDefinitions].sort(
    (a, b) => a.sortOrder - b.sortOrder
  );

  // Get price for a unit type
  const getUnitPrice = (unitType: string): number => {
    const priceEntry = unitPrices.find((p) => p.unitType === unitType);
    return priceEntry?.sellingPrice || 0;
  };

  // Get selected unit definition
  const selectedUnit = selectedUnitType
    ? sortedUnits.find((u) => u.unitType === selectedUnitType)
    : null;

  const selectedPrice = selectedUnitType
    ? getUnitPrice(selectedUnitType)
    : 0;

  // Calculate price per base unit
  const calculatePricePerBaseUnit = (
    unitType: string,
    price: number
  ): number => {
    const unitDef = unitDefinitions.find((u) => u.unitType === unitType);
    if (!unitDef || unitDef.conversionFactor === 0) return 0;
    return price / unitDef.conversionFactor;
  };

  // Handle unit selection
  const handleUnitSelect = (unitType: string) => {
    const price = getUnitPrice(unitType);
    onUnitChange(unitType, price);
  };

  // No unit definitions available
  if (sortedUnits.length === 0) {
    return (
      <div className={className}>
        <Label className="text-sm font-medium">Unit</Label>
        <div className="mt-1 bg-gray-50 border border-gray-300 rounded px-3 py-2 text-sm text-gray-500">
          Single unit (no multi-unit pricing)
        </div>
      </div>
    );
  }

  // Get base unit for comparison
  const baseUnit = sortedUnits.find((u) => u.isBaseUnit);
  const baseUnitPrice = baseUnit ? getUnitPrice(baseUnit.unitType) : 0;

  return (
    <div className={`space-y-3 ${className}`}>
      <div>
        <Label className="text-sm font-medium flex items-center">
          <Package className="h-4 w-4 mr-1.5" />
          Select Unit
        </Label>
        <Select
          value={selectedUnitType}
          onValueChange={handleUnitSelect}
          disabled={disabled}
        >
          <SelectTrigger className="mt-1">
            <SelectValue placeholder="Choose unit type..." />
          </SelectTrigger>
          <SelectContent>
            {sortedUnits.map((unit) => {
              const price = getUnitPrice(unit.unitType);
              return (
                <SelectItem
                  key={unit.unitType}
                  value={unit.unitType}
                  disabled={price === 0}
                >
                  <div className="flex items-center justify-between w-full">
                    <span className="font-medium">
                      {unit.unitLabel}
                      {unit.isBaseUnit && (
                        <span className="ml-2 text-xs text-blue-600">
                          (Base)
                        </span>
                      )}
                    </span>
                    <span className="ml-4 text-gray-600">
                      {price > 0 ? formatCurrency(price) : "No price set"}
                    </span>
                  </div>
                </SelectItem>
              );
            })}
          </SelectContent>
        </Select>
      </div>

      {/* Selected Unit Details */}
      {selectedUnit && selectedPrice > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-sm font-semibold text-blue-900">
              {productName}
            </span>
            <span className="text-sm text-blue-600">
              {selectedUnit.unitLabel}
            </span>
          </div>

          <div className="flex items-center justify-between">
            <span className="text-xs text-blue-700">Unit Price:</span>
            <span className="text-lg font-bold text-blue-900">
              {formatCurrency(selectedPrice)}
            </span>
          </div>

          {showPriceBreakdown && !selectedUnit.isBaseUnit && (
            <div className="pt-2 border-t border-blue-300 space-y-1">
              <div className="flex items-center justify-between text-xs">
                <span className="text-blue-700">Conversion Factor:</span>
                <span className="font-mono text-blue-900">
                  {selectedUnit.conversionFactor} base units
                </span>
              </div>
              <div className="flex items-center justify-between text-xs">
                <span className="text-blue-700">Price per Base Unit:</span>
                <span className="font-mono text-blue-900">
                  {formatCurrency(
                    calculatePricePerBaseUnit(selectedUnit.unitType, selectedPrice)
                  )}
                </span>
              </div>
              {baseUnit && baseUnitPrice > 0 && (
                <div className="flex items-center justify-between text-xs">
                  <span className="text-blue-700">
                    vs. {baseUnit.unitLabel} Price:
                  </span>
                  <span className="font-mono text-blue-900">
                    {formatCurrency(baseUnitPrice)} each
                  </span>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* No Price Warning */}
      {selectedUnit && selectedPrice === 0 && (
        <div className="bg-yellow-50 border border-yellow-200 px-3 py-2 rounded flex items-start">
          <Info className="h-4 w-4 mr-2 mt-0.5 flex-shrink-0 text-yellow-600" />
          <p className="text-xs text-yellow-800">
            No selling price set for this unit. Please update the inventory to
            add a price.
          </p>
        </div>
      )}

      {/* All Units Price Table (Optional) */}
      {showPriceBreakdown && sortedUnits.length > 1 && (
        <div className="bg-gray-50 border border-gray-200 rounded-lg p-3">
          <p className="text-xs font-semibold text-gray-700 mb-2">
            Available Units:
          </p>
          <div className="space-y-1.5">
            {sortedUnits.map((unit) => {
              const price = getUnitPrice(unit.unitType);
              const pricePerBase = calculatePricePerBaseUnit(
                unit.unitType,
                price
              );
              const isSelected = unit.unitType === selectedUnitType;

              return (
                <div
                  key={unit.unitType}
                  className={`flex items-center justify-between text-xs px-2 py-1.5 rounded ${
                    isSelected
                      ? "bg-blue-100 text-blue-900 font-semibold"
                      : "text-gray-600"
                  }`}
                >
                  <span className="flex items-center">
                    {unit.unitLabel}
                    {unit.isBaseUnit && (
                      <span className="ml-1 text-[10px] text-blue-600">
                        (Base)
                      </span>
                    )}
                  </span>
                  <span className="flex items-center gap-2">
                    <span className="font-mono">
                      {price > 0 ? formatCurrency(price) : "—"}
                    </span>
                    {!unit.isBaseUnit && price > 0 && (
                      <span className="text-[10px] text-gray-500">
                        ({formatCurrency(pricePerBase)}/base)
                      </span>
                    )}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
