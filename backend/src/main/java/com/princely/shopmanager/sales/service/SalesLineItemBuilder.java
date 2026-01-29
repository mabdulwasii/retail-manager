package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.sales.domain.LineItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for building sales line items from inventory allocations.
 * Handles pricing logic, product denormalization, and multi-batch FEFO allocation.
 * When a sale spans multiple inventory batches, creates separate line items for
 * accurate batch-specific cost tracking.
 */
@Service
@Slf4j
public class SalesLineItemBuilder {

    /**
     * Builds line items from inventory allocations.
     * For each allocation, if it spans multiple batches, creates separate line items
     * for accurate cost tracking per batch (FEFO with chaining).
     *
     * @param allocations List of inventory allocations
     * @return List of line items ready to be added to transaction
     */
    public List<LineItem> buildFromAllocations(List<InventoryAllocationService.InventoryAllocation> allocations) {
        log.debug("Building line items from {} allocations", allocations.size());

        List<LineItem> lineItems = new ArrayList<>();

        for (InventoryAllocationService.InventoryAllocation allocation : allocations) {
            // Check if allocation spans multiple batches
            if (allocation.inventories.size() > 1) {
                // Create separate line items for each batch (for accurate cost tracking)
                lineItems.addAll(buildMultiBatchLineItems(allocation));
            } else {
                // Single batch allocation - create one line item
                lineItems.add(buildSingleBatchLineItem(allocation));
            }
        }

        log.debug("Successfully built {} line items", lineItems.size());
        return lineItems;
    }

    /**
     * Builds a single line item from a single-batch allocation.
     * Gets selling price and cost price from the allocated inventory batch.
     * Uses InventoryUnitPrice for unit-specific cost if available.
     */
    private LineItem buildSingleBatchLineItem(InventoryAllocationService.InventoryAllocation allocation) {
        var inventory = allocation.inventories.isEmpty() ? null : allocation.inventories.get(0);

        // Get unit-specific selling price from InventoryUnitPrice or fallback to inventory selling price
        BigDecimal sellingPrice = allocation.request.getUnitPrice();
        if (sellingPrice == null && inventory != null) {
            sellingPrice = getUnitSellingPrice(inventory, allocation.unitType);
        }

        // Get unit-specific cost price from InventoryUnitPrice for accurate profit calculation
        BigDecimal costPrice = null;
        if (inventory != null) {
            costPrice = getUnitCostPrice(inventory, allocation.unitType);
        }

        LineItem lineItem = LineItem.builder()
            .product(allocation.product)
            .productName(allocation.product.getName())
            .productSku(allocation.product.getSku())
            .productCategory(allocation.product.getCategory() != null ?
                allocation.product.getCategory().getName() : null)
            .quantity(allocation.request.getQuantity())
            .unitType(allocation.unitType)
            .unitLabel(allocation.unitLabel)
            .unitConversionFactor(allocation.conversionFactor)
            .baseUnitQuantity(allocation.baseUnitQuantity)
            .unitPrice(sellingPrice)
            .costPrice(costPrice)
            .discountAmount(allocation.request.getDiscount() != null ?
                allocation.request.getDiscount() : BigDecimal.ZERO)
            .build();

        lineItem.calculateLineTotal();

        log.debug("Built single-batch line item: {} {} of {} @ {} per unit (cost: {}, base units: {})",
            lineItem.getQuantity(),
            lineItem.getUnitType() != null ? lineItem.getUnitType() : "units",
            lineItem.getProductName(),
            sellingPrice,
            costPrice,
            lineItem.getBaseUnitQuantity());

        return lineItem;
    }

    /**
     * Builds multiple line items when allocation spans multiple inventory batches.
     * Each line item represents a portion from a specific batch with that batch's cost price.
     *
     * Example: Need 25 packs, Batch A has 20, Batch B has 10
     * - Line Item 1: 20 packs from Batch A @ Batch A cost
     * - Line Item 2: 5 packs from Batch B @ Batch B cost
     */
    private List<LineItem> buildMultiBatchLineItems(InventoryAllocationService.InventoryAllocation allocation) {
        List<LineItem> lineItems = new ArrayList<>();
        int remainingBaseUnits = allocation.baseUnitQuantity;

        log.debug("Creating multi-batch line items for {} base units across {} batches",
            remainingBaseUnits, allocation.inventories.size());

        for (int i = 0; i < allocation.inventories.size() && remainingBaseUnits > 0; i++) {
            var inventory = allocation.inventories.get(i);
            int availableInBatch = inventory.getAvailableStock();
            int quantityFromBatch = Math.min(remainingBaseUnits, availableInBatch);

            // Calculate quantity in the original unit type (e.g., packs)
            // quantityInOriginalUnit = quantityFromBatch / conversionFactor
            int quantityInOriginalUnit = quantityFromBatch;
            if (allocation.conversionFactor != null && allocation.conversionFactor.compareTo(BigDecimal.ONE) > 0) {
                quantityInOriginalUnit = BigDecimal.valueOf(quantityFromBatch)
                    .divide(allocation.conversionFactor, 0, java.math.RoundingMode.DOWN)
                    .intValue();
            }

            // Get batch-specific and unit-specific cost price
            BigDecimal batchCostPrice = getUnitCostPrice(inventory, allocation.unitType);

            // Use request unit price or unit-specific selling price
            BigDecimal unitPrice = allocation.request.getUnitPrice();
            if (unitPrice == null) {
                unitPrice = getUnitSellingPrice(inventory, allocation.unitType);
            }

            // Calculate proportional discount
            BigDecimal discount = BigDecimal.ZERO;
            if (allocation.request.getDiscount() != null && allocation.request.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                // Distribute discount proportionally
                BigDecimal ratio = BigDecimal.valueOf(quantityFromBatch)
                    .divide(BigDecimal.valueOf(allocation.baseUnitQuantity), 4, java.math.RoundingMode.HALF_UP);
                discount = allocation.request.getDiscount().multiply(ratio);
            }

            LineItem batchLineItem = LineItem.builder()
                .product(allocation.product)
                .productName(allocation.product.getName())
                .productSku(allocation.product.getSku())
                .productCategory(allocation.product.getCategory() != null ?
                    allocation.product.getCategory().getName() : null)
                .quantity(quantityInOriginalUnit > 0 ? quantityInOriginalUnit : 1) // At least 1
                .unitType(allocation.unitType)
                .unitLabel(allocation.unitLabel)
                .unitConversionFactor(allocation.conversionFactor)
                .baseUnitQuantity(quantityFromBatch)
                .unitPrice(unitPrice)
                .costPrice(batchCostPrice)  // Batch-specific cost for accurate profit
                .discountAmount(discount)
                .build();

            batchLineItem.calculateLineTotal();
            lineItems.add(batchLineItem);

            log.debug("Created line item from batch {}/{}: {} {} of {} @ {} per unit (batch cost: {})",
                i + 1, allocation.inventories.size(),
                batchLineItem.getQuantity(),
                batchLineItem.getUnitType() != null ? batchLineItem.getUnitType() : "units",
                batchLineItem.getProductName(),
                unitPrice,
                batchCostPrice);

            remainingBaseUnits -= quantityFromBatch;
        }

        return lineItems;
    }

    /**
     * Gets the cost price for a specific unit type from InventoryUnitPrice.
     * Falls back to inventory base cost price if unit price not found.
     *
     * @param inventory Inventory batch
     * @param unitType Unit type (e.g., "pack", "piece")
     * @return Cost price for the unit type
     */
    private BigDecimal getUnitCostPrice(com.princely.shopmanager.inventory.domain.Inventory inventory, String unitType) {
        if (unitType == null || inventory.getUnitPrices() == null) {
            return inventory.getCostPrice();
        }

        // Find unit-specific cost price
        return inventory.getUnitPrices().stream()
            .filter(up -> up.getUnitType().equalsIgnoreCase(unitType))
            .findFirst()
            .map(com.princely.shopmanager.inventory.domain.InventoryUnitPrice::getCostPrice)
            .orElse(inventory.getCostPrice()); // Fallback to base cost price
    }

    /**
     * Gets the selling price for a specific unit type from InventoryUnitPrice.
     * Falls back to inventory base selling price if unit price not found.
     *
     * @param inventory Inventory batch
     * @param unitType Unit type (e.g., "pack", "piece")
     * @return Selling price for the unit type
     */
    private BigDecimal getUnitSellingPrice(com.princely.shopmanager.inventory.domain.Inventory inventory, String unitType) {
        if (unitType == null || inventory.getUnitPrices() == null) {
            return inventory.getSellingPrice();
        }

        // Find unit-specific selling price
        return inventory.getUnitPrices().stream()
            .filter(up -> up.getUnitType().equalsIgnoreCase(unitType))
            .findFirst()
            .map(com.princely.shopmanager.inventory.domain.InventoryUnitPrice::getSellingPrice)
            .orElse(inventory.getSellingPrice()); // Fallback to base selling price
    }
}
