import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NumericInput } from "@/components/ui/numeric-input";
import { ProductUnitDefinitionRequest } from "@/types/api";
import { ArrowDown, ArrowUp, Plus, Trash2 } from "lucide-react";
import React, { useEffect, useState } from "react";

// Common unit type suggestions
const UNIT_TYPE_SUGGESTIONS = [
  "piece",
  "pack",
  "half_pack",
  "quarter_pack",
  "carton",
  "bottle",
  "can",
  "kg",
  "g",
  "liter",
  "ml",
  "dozen",
  "roll",
  "bag",
  "box",
  "crate"
];

// Unit label suggestions (examples)
const UNIT_LABEL_SUGGESTIONS: Record<string, string> = {
  "piece": "Piece",
  "pack": "Pack (12pcs)",
  "half_pack": "Half Pack (6pcs)",
  "quarter_pack": "Quarter Pack (3pcs)",
  "carton": "Carton (24pcs)",
  "bottle": "Bottle",
  "can": "Can",
  "kg": "Kilogram (kg)",
  "g": "Gram (g)",
  "liter": "Liter (L)",
  "ml": "Milliliter (ml)",
  "dozen": "Dozen (12pcs)",
  "roll": "Roll",
  "bag": "Bag",
  "box": "Box",
  "crate": "Crate"
};

// Predefined unit definition templates
export interface UnitTemplate {
  name: string;
  description: string;
  units: ProductUnitDefinitionRequest[];
}

export const UNIT_TEMPLATES: UnitTemplate[] = [
  {
    name: "Beverages (Bottles/Cans)",
    description: "For bottled or canned drinks (soft drinks, water, etc.)",
    units: [
      { unitType: "piece", unitLabel: "Piece", conversionFactor: 1.0, isBaseUnit: true, sortOrder: 0 },
      { unitType: "pack", unitLabel: "Pack (6pcs)", conversionFactor: 6.0, isBaseUnit: false, sortOrder: 1 },
      { unitType: "pack_12", unitLabel: "Pack (12pcs)", conversionFactor: 12.0, isBaseUnit: false, sortOrder: 2 },
      { unitType: "carton", unitLabel: "Carton (24pcs)", conversionFactor: 24.0, isBaseUnit: false, sortOrder: 3 },
    ],
  },
  {
    name: "Dry Goods (Weight-based)",
    description: "For items sold by weight (rice, flour, sugar, etc.)",
    units: [
      { unitType: "kg", unitLabel: "Kilogram (kg)", conversionFactor: 1.0, isBaseUnit: true, sortOrder: 0 },
      { unitType: "g", unitLabel: "Gram (g)", conversionFactor: 0.001, isBaseUnit: false, sortOrder: 1 },
      { unitType: "bag_5kg", unitLabel: "Bag (5kg)", conversionFactor: 5.0, isBaseUnit: false, sortOrder: 2 },
      { unitType: "bag_10kg", unitLabel: "Bag (10kg)", conversionFactor: 10.0, isBaseUnit: false, sortOrder: 3 },
      { unitType: "bag_25kg", unitLabel: "Bag (25kg)", conversionFactor: 25.0, isBaseUnit: false, sortOrder: 4 },
    ],
  },
  {
    name: "Sachets/Small Packs",
    description: "For sachet products (detergent, seasoning, etc.)",
    units: [
      { unitType: "sachet", unitLabel: "Sachet", conversionFactor: 1.0, isBaseUnit: true, sortOrder: 0 },
      { unitType: "bundle_10", unitLabel: "Bundle (10 sachets)", conversionFactor: 10.0, isBaseUnit: false, sortOrder: 1 },
      { unitType: "bundle_25", unitLabel: "Bundle (25 sachets)", conversionFactor: 25.0, isBaseUnit: false, sortOrder: 2 },
      { unitType: "carton", unitLabel: "Carton (100 sachets)", conversionFactor: 100.0, isBaseUnit: false, sortOrder: 3 },
    ],
  },
  {
    name: "Tissue/Paper Rolls",
    description: "For toilet paper, tissue, paper towels, etc.",
    units: [
      { unitType: "roll", unitLabel: "Roll", conversionFactor: 1.0, isBaseUnit: true, sortOrder: 0 },
      { unitType: "pack_4", unitLabel: "Pack (4 rolls)", conversionFactor: 4.0, isBaseUnit: false, sortOrder: 1 },
      { unitType: "pack_10", unitLabel: "Pack (10 rolls)", conversionFactor: 10.0, isBaseUnit: false, sortOrder: 2 },
      { unitType: "carton", unitLabel: "Carton (48 rolls)", conversionFactor: 48.0, isBaseUnit: false, sortOrder: 3 },
    ],
  },
  {
    name: "Electronics/General Merchandise",
    description: "For individual items sold as units",
    units: [
      { unitType: "piece", unitLabel: "Piece", conversionFactor: 1.0, isBaseUnit: true, sortOrder: 0 },
      { unitType: "pair", unitLabel: "Pair", conversionFactor: 2.0, isBaseUnit: false, sortOrder: 1 },
      { unitType: "set", unitLabel: "Set (6pcs)", conversionFactor: 6.0, isBaseUnit: false, sortOrder: 2 },
      { unitType: "dozen", unitLabel: "Dozen (12pcs)", conversionFactor: 12.0, isBaseUnit: false, sortOrder: 3 },
    ],
  },
];

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
  const [showTemplateDialog, setShowTemplateDialog] = useState(false);

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

  const handleApplyTemplate = (template: UnitTemplate) => {
    const updatedUnits = template.units.map((unit) => ({ ...unit }));
    setLocalUnits(updatedUnits);
    validateUnits(updatedUnits);
    onChange(updatedUnits);
    setShowTemplateDialog(false);
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
        <div className="flex gap-2">
          <Button
            type="button"
            onClick={() => setShowTemplateDialog(!showTemplateDialog)}
            disabled={disabled}
            variant="secondary"
            size="sm"
          >
            Use Template
          </Button>
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
      </div>

      {/* Template Selection Dialog */}
      {showTemplateDialog && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 space-y-3">
          <div className="flex justify-between items-start">
            <div>
              <h4 className="font-semibold text-blue-900">Choose a Template</h4>
              <p className="text-sm text-blue-700">Select a predefined unit structure for your product type</p>
            </div>
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={() => setShowTemplateDialog(false)}
              className="h-6 w-6 p-0"
            >
              ×
            </Button>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
            {UNIT_TEMPLATES.map((template) => (
              <button
                key={template.name}
                type="button"
                onClick={() => handleApplyTemplate(template)}
                disabled={disabled}
                className="text-left p-3 bg-white hover:bg-blue-100 border border-blue-200 rounded-md transition-colors disabled:opacity-50"
              >
                <div className="font-medium text-sm text-blue-900">{template.name}</div>
                <div className="text-xs text-blue-600 mt-1">{template.description}</div>
                <div className="text-xs text-gray-500 mt-2">
                  {template.units.length} units: {template.units.map(u => u.unitLabel).join(", ")}
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

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
                    list={`unit-type-suggestions-${index}`}
                    placeholder="e.g., piece, pack"
                    value={unit.unitType}
                    onChange={(e) => {
                      const value = e.target.value;
                      handleUpdateUnit(index, "unitType", value);
                      // Auto-suggest label if available
                      if (UNIT_LABEL_SUGGESTIONS[value.toLowerCase()] && !unit.unitLabel) {
                        handleUpdateUnit(index, "unitLabel", UNIT_LABEL_SUGGESTIONS[value.toLowerCase()]);
                      }
                    }}
                    disabled={disabled}
                    className="mt-1"
                  />
                  <datalist id={`unit-type-suggestions-${index}`}>
                    {UNIT_TYPE_SUGGESTIONS.map((suggestion) => (
                      <option key={suggestion} value={suggestion} />
                    ))}
                  </datalist>
                </div>

                {/* Unit Label */}
                <div className="col-span-3">
                  <Label className="text-xs">Display Label*</Label>
                  <Input
                    type="text"
                    list={`unit-label-suggestions-${index}`}
                    placeholder="e.g., Pack (12pcs)"
                    value={unit.unitLabel}
                    onChange={(e) =>
                      handleUpdateUnit(index, "unitLabel", e.target.value)
                    }
                    disabled={disabled}
                    className="mt-1"
                  />
                  <datalist id={`unit-label-suggestions-${index}`}>
                    {Object.values(UNIT_LABEL_SUGGESTIONS).map((suggestion) => (
                      <option key={suggestion} value={suggestion} />
                    ))}
                  </datalist>
                </div>

                {/* Conversion Factor */}
                <div className="col-span-2">
                  <Label className="text-xs">
                    Factor* {unit.isBaseUnit && "(1.0)"}
                  </Label>
                  <NumericInput
                    value={unit.conversionFactor}
                    onValueChange={(values) =>
                      handleUpdateUnit(index, "conversionFactor", values.floatValue || 0)
                    }
                    disabled={disabled || unit.isBaseUnit}
                    decimalScale={4}
                    allowNegative={false}
                    className="mt-1"
                    isNumberInput={true}
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
