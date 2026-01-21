import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NumericInput } from "@/components/ui/numeric-input";
import { ProductUnitDefinitionRequest } from "@/types/api";
import { ArrowDown, ArrowUp, Plus, Trash2 } from "lucide-react";
import React, { useEffect, useState } from "react";

interface UnitDefinitionManagerProps {
  unitDefinitions: ProductUnitDefinitionRequest[];
  onChange: (unitDefinitions: ProductUnitDefinitionRequest[]) => void;
  disabled?: boolean;
}

export const UnitDefinitionManager: React.FC<UnitDefinitionManagerProps> = ({
  unitDefinitions,
  onChange,
  disabled = false,
}) => {
  const [localUnits, setLocalUnits] = useState<ProductUnitDefinitionRequest[]>(
    unitDefinitions
  );
  const [errors, setErrors] = useState<Record<number, string>>({});

  useEffect(() => {
    setLocalUnits(unitDefinitions);
  }, [unitDefinitions]);

  const validateUnits = (units: ProductUnitDefinitionRequest[]): boolean => {
    const newErrors: Record<number, string> = {};

    // Check for base unit
    const baseUnits = units.filter((u) => u.isBaseUnit);
    if (baseUnits.length === 0) {
      newErrors[0] = "At least one base unit is required";
    } else if (baseUnits.length > 1) {
      units.forEach((u, i) => {
        if (u.isBaseUnit) {
          newErrors[i] = "Only one base unit is allowed";
        }
      });
    }

    // Validate base unit conversion factor
    units.forEach((u, i) => {
      if (u.isBaseUnit && u.conversionFactor !== 1.0) {
        newErrors[i] = "Base unit must have conversion factor of 1.0";
      }
      if (!u.unitType || u.unitType.trim() === "") {
        newErrors[i] = "Unit type is required";
      }
      if (!u.unitLabel || u.unitLabel.trim() === "") {
        newErrors[i] = "Unit label is required";
      }
      if (u.conversionFactor <= 0) {
        newErrors[i] = "Conversion factor must be positive";
      }
    });

    // Check for duplicate unit types
    const unitTypes = units.map((u) => u.unitType.toLowerCase());
    const duplicates = unitTypes.filter(
      (type, index) => unitTypes.indexOf(type) !== index
    );
    if (duplicates.length > 0) {
      units.forEach((u, i) => {
        if (duplicates.includes(u.unitType.toLowerCase())) {
          newErrors[i] = `Duplicate unit type: ${u.unitType}`;
        }
      });
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAddUnit = () => {
    const newUnit: ProductUnitDefinitionRequest = {
      unitType: "",
      unitLabel: "",
      conversionFactor: 1.0,
      isBaseUnit: localUnits.length === 0, // First unit is base unit by default
      sortOrder: localUnits.length,
    };
    const updatedUnits = [...localUnits, newUnit];
    setLocalUnits(updatedUnits);
    onChange(updatedUnits);
  };

  const handleUpdateUnit = (
    index: number,
    field: keyof ProductUnitDefinitionRequest,
    value: string | number | boolean
  ) => {
    const updatedUnits = [...localUnits];
    updatedUnits[index] = { ...updatedUnits[index], [field]: value };

    // If setting as base unit, ensure conversion factor is 1.0
    if (field === "isBaseUnit" && value === true) {
      updatedUnits[index].conversionFactor = 1.0;
      // Unset other base units
      updatedUnits.forEach((u, i) => {
        if (i !== index) {
          u.isBaseUnit = false;
        }
      });
    }

    setLocalUnits(updatedUnits);
    validateUnits(updatedUnits);
    onChange(updatedUnits);
  };

  const handleRemoveUnit = (index: number) => {
    const updatedUnits = localUnits.filter((_, i) => i !== index);
    // Re-assign sort orders
    updatedUnits.forEach((u, i) => {
      u.sortOrder = i;
    });
    setLocalUnits(updatedUnits);
    validateUnits(updatedUnits);
    onChange(updatedUnits);
  };

  const handleMoveUnit = (index: number, direction: "up" | "down") => {
    if (
      (direction === "up" && index === 0) ||
      (direction === "down" && index === localUnits.length - 1)
    ) {
      return;
    }

    const updatedUnits = [...localUnits];
    const targetIndex = direction === "up" ? index - 1 : index + 1;
    [updatedUnits[index], updatedUnits[targetIndex]] = [
      updatedUnits[targetIndex],
      updatedUnits[index],
    ];

    // Update sort orders
    updatedUnits.forEach((u, i) => {
      u.sortOrder = i;
    });

    setLocalUnits(updatedUnits);
    onChange(updatedUnits);
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <Label className="text-base font-semibold">Unit Definitions</Label>
          <p className="text-sm text-gray-500 mt-1">
            Define multiple units for this product (e.g., piece, pack, carton).
            The base unit is used for inventory tracking.
          </p>
        </div>
        <Button
          type="button"
          onClick={handleAddUnit}
          disabled={disabled}
          variant="outline"
          size="sm"
        >
          <Plus className="h-4 w-4 mr-2" />
          Add Unit
        </Button>
      </div>

      {Object.keys(errors).length > 0 && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          <p className="font-semibold">Validation Errors:</p>
          <ul className="list-disc list-inside text-sm mt-1">
            {Object.entries(errors).map(([key, error]) => (
              <li key={key}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      {localUnits.length === 0 ? (
        <div className="text-center py-8 bg-gray-50 rounded-lg border-2 border-dashed border-gray-300">
          <p className="text-gray-500">
            No unit definitions yet. Click "Add Unit" to get started.
          </p>
          <p className="text-sm text-gray-400 mt-1">
            The first unit will be set as the base unit automatically.
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {localUnits.map((unit, index) => (
            <div
              key={index}
              className={`border rounded-lg p-4 ${
                unit.isBaseUnit ? "bg-blue-50 border-blue-200" : "bg-white"
              } ${errors[index] ? "border-red-300" : ""}`}
            >
              <div className="grid grid-cols-12 gap-3 items-start">
                {/* Unit Type */}
                <div className="col-span-3">
                  <Label className="text-xs">Unit Type*</Label>
                  <Input
                    type="text"
                    placeholder="e.g., piece, pack"
                    value={unit.unitType}
                    onChange={(e) =>
                      handleUpdateUnit(index, "unitType", e.target.value)
                    }
                    disabled={disabled}
                    className="mt-1"
                  />
                </div>

                {/* Unit Label */}
                <div className="col-span-3">
                  <Label className="text-xs">Display Label*</Label>
                  <Input
                    type="text"
                    placeholder="e.g., Pack (12pcs)"
                    value={unit.unitLabel}
                    onChange={(e) =>
                      handleUpdateUnit(index, "unitLabel", e.target.value)
                    }
                    disabled={disabled}
                    className="mt-1"
                  />
                </div>

                {/* Conversion Factor */}
                <div className="col-span-2">
                  <Label className="text-xs">
                    Factor* {unit.isBaseUnit && "(1.0)"}
                  </Label>
                  <NumericInput
                    value={unit.conversionFactor}
                    onChange={(value) =>
                      handleUpdateUnit(index, "conversionFactor", value)
                    }
                    disabled={disabled || unit.isBaseUnit}
                    min={0.0001}
                    step={0.01}
                    decimals={4}
                    className="mt-1"
                  />
                </div>

                {/* Base Unit Checkbox */}
                <div className="col-span-2 flex items-center pt-6">
                  <input
                    type="checkbox"
                    id={`base-${index}`}
                    checked={unit.isBaseUnit || false}
                    onChange={(e) =>
                      handleUpdateUnit(index, "isBaseUnit", e.target.checked)
                    }
                    disabled={disabled}
                    className="h-4 w-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
                  />
                  <label
                    htmlFor={`base-${index}`}
                    className="ml-2 text-xs text-gray-700 cursor-pointer"
                  >
                    Base Unit
                  </label>
                </div>

                {/* Actions */}
                <div className="col-span-2 flex gap-1 pt-6">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleMoveUnit(index, "up")}
                    disabled={disabled || index === 0}
                    className="p-1 h-8 w-8"
                  >
                    <ArrowUp className="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleMoveUnit(index, "down")}
                    disabled={disabled || index === localUnits.length - 1}
                    className="p-1 h-8 w-8"
                  >
                    <ArrowDown className="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleRemoveUnit(index)}
                    disabled={disabled || localUnits.length === 1}
                    className="p-1 h-8 w-8 text-red-600 hover:text-red-700 hover:bg-red-50"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              {errors[index] && (
                <p className="text-xs text-red-600 mt-2">{errors[index]}</p>
              )}
            </div>
          ))}
        </div>
      )}

      {localUnits.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 px-4 py-3 rounded">
          <p className="text-sm text-blue-800">
            <strong>Tip:</strong> The conversion factor represents how many base
            units are in one of this unit. For example, if "piece" is the base
            unit and a "pack" contains 12 pieces, the pack's conversion factor
            should be 12.0.
          </p>
        </div>
      )}
    </div>
  );
};
