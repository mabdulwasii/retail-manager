import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { UnitSelector } from "@/components/sales/UnitSelector";
import {
  Product,
  ProductUnitDefinition,
  InventoryUnitPrice,
} from "@/types/api";
import React, { useState, useEffect } from "react";
import { Package } from "lucide-react";

interface UnitSelectDialogProps {
  isOpen: boolean;
  onClose: () => void;
  product: Product;
  inventoryId: string;
  unitDefinitions: ProductUnitDefinition[];
  unitPrices: InventoryUnitPrice[];
  onConfirm: (unitType: string, unitPrice: number) => void;
}

export const UnitSelectDialog: React.FC<UnitSelectDialogProps> = ({
  isOpen,
  onClose,
  product,
  inventoryId,
  unitDefinitions,
  unitPrices,
  onConfirm,
}) => {
  const [selectedUnitType, setSelectedUnitType] = useState<string | undefined>();
  const [selectedUnitPrice, setSelectedUnitPrice] = useState<number>(0);

  // Auto-select base unit when dialog opens
  useEffect(() => {
    if (isOpen && unitDefinitions.length > 0 && !selectedUnitType) {
      const baseUnit = unitDefinitions.find(u => u.isBaseUnit);
      if (baseUnit) {
        const basePrice = unitPrices.find(p => p.unitType === baseUnit.unitType);
        if (basePrice && basePrice.sellingPrice > 0) {
          setSelectedUnitType(baseUnit.unitType);
          setSelectedUnitPrice(basePrice.sellingPrice);
        }
      }
    }
  }, [isOpen, unitDefinitions, unitPrices, selectedUnitType]);

  const handleUnitChange = (unitType: string, unitPrice: number) => {
    setSelectedUnitType(unitType);
    setSelectedUnitPrice(unitPrice);
  };

  const handleConfirm = () => {
    if (selectedUnitType && selectedUnitPrice > 0) {
      onConfirm(selectedUnitType, selectedUnitPrice);
      // Reset state
      setSelectedUnitType(undefined);
      setSelectedUnitPrice(0);
      onClose();
    }
  };

  const handleClose = () => {
    setSelectedUnitType(undefined);
    setSelectedUnitPrice(0);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Package className="h-5 w-5" />
            Select Unit for {product.name}
          </DialogTitle>
        </DialogHeader>

        <div className="py-4">
          <UnitSelector
            productName={product.name}
            unitDefinitions={unitDefinitions}
            unitPrices={unitPrices}
            selectedUnitType={selectedUnitType}
            onUnitChange={handleUnitChange}
            showPriceBreakdown={true}
          />
        </div>

        <DialogFooter className="gap-2">
          <Button type="button" variant="outline" onClick={handleClose}>
            Cancel
          </Button>
          <Button
            type="button"
            onClick={handleConfirm}
            disabled={!selectedUnitType || selectedUnitPrice === 0}
          >
            Add to Cart
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
