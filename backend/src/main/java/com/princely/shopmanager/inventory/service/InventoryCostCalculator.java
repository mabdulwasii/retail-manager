package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for calculating inventory costs across different unit types.
 *
 * Example calculation:
 * - Purchase: 20 packs @ ₦106,000 total
 * - Units: piece (1.0), pack (12.0), half_pack (6.0), quarter_pack (3.0)
 *
 * Results:
 * - Pack cost: ₦106,000 / 20 = ₦5,300.00
 * - Half pack: ₦5,300 × (6.0 / 12.0) = ₦2,650.00
 * - Quarter pack: ₦5,300 × (3.0 / 12.0) = ₦1,325.00
 * - Piece: ₦5,300 × (1.0 / 12.0) = ₦441.67 (rounded half-up)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCostCalculator {

    /**
     * Calculate cost for all unit types based on purchase information.
     *
     * @param totalPurchaseCost Total cost for all purchased quantity (e.g., ₦106,000)
     * @param purchaseQuantity Quantity purchased (e.g., 20)
     * @param purchaseUnit Unit type purchased (e.g., "pack")
     * @param unitDefinitions All available unit definitions for the product
     * @return Map of unit type to calculated cost (rounded to 2 decimals)
     */
    public Map<String, BigDecimal> calculateCostsForAllUnits(
            BigDecimal totalPurchaseCost,
            BigDecimal purchaseQuantity,
            String purchaseUnit,
            List<ProductUnitDefinition> unitDefinitions) {

        Map<String, BigDecimal> costs = new HashMap<>();

        // Validate inputs
        if (totalPurchaseCost == null || purchaseQuantity == null ||
            totalPurchaseCost.compareTo(BigDecimal.ZERO) <= 0 ||
            purchaseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid cost calculation inputs: totalCost={}, quantity={}",
                    totalPurchaseCost, purchaseQuantity);
            return costs;
        }

        // Step 1: Calculate cost per purchase unit
        BigDecimal purchaseUnitCost = totalPurchaseCost.divide(
                purchaseQuantity, 4, RoundingMode.HALF_UP);

        log.debug("Purchase unit cost: {} / {} = {}",
                totalPurchaseCost, purchaseQuantity, purchaseUnitCost);

        // Step 2: Find purchase unit's conversion factor
        ProductUnitDefinition purchaseUnitDef = unitDefinitions.stream()
                .filter(ud -> ud.getUnitType().equalsIgnoreCase(purchaseUnit))
                .findFirst()
                .orElse(null);

        if (purchaseUnitDef == null) {
            log.warn("Purchase unit '{}' not found in unit definitions. Cannot calculate costs.",
                    purchaseUnit);
            return costs;
        }

        BigDecimal purchaseConversionFactor = purchaseUnitDef.getConversionFactor();

        // Step 3: Calculate cost for each unit type
        for (ProductUnitDefinition unitDef : unitDefinitions) {
            BigDecimal unitConversionFactor = unitDef.getConversionFactor();

            // Formula: unitCost = purchaseUnitCost × (unitConversionFactor / purchaseConversionFactor)
            BigDecimal ratio = unitConversionFactor.divide(
                    purchaseConversionFactor, 10, RoundingMode.HALF_UP);

            BigDecimal unitCost = purchaseUnitCost.multiply(ratio)
                    .setScale(2, RoundingMode.HALF_UP);

            costs.put(unitDef.getUnitType(), unitCost);

            log.debug("Unit '{}': {} × ({} / {}) = {}",
                    unitDef.getUnitType(), purchaseUnitCost,
                    unitConversionFactor, purchaseConversionFactor, unitCost);
        }

        return costs;
    }

    /**
     * Calculate cost for a specific unit type.
     *
     * @param totalPurchaseCost Total cost for all purchased quantity
     * @param purchaseQuantity Quantity purchased
     * @param purchaseUnit Unit type purchased
     * @param targetUnit Unit type to calculate cost for
     * @param unitDefinitions All available unit definitions
     * @return Cost for the target unit, or null if calculation failed
     */
    public BigDecimal calculateCostForUnit(
            BigDecimal totalPurchaseCost,
            BigDecimal purchaseQuantity,
            String purchaseUnit,
            String targetUnit,
            List<ProductUnitDefinition> unitDefinitions) {

        Map<String, BigDecimal> allCosts = calculateCostsForAllUnits(
                totalPurchaseCost, purchaseQuantity, purchaseUnit, unitDefinitions);

        return allCosts.get(targetUnit);
    }

    /**
     * Convert quantity from one unit to another using base units.
     *
     * Example: Convert 2 packs to pieces
     * - Pack conversion factor: 12.0
     * - Result: 2 × 12 = 24 pieces
     *
     * @param quantity Quantity to convert
     * @param fromUnit Source unit type
     * @param toUnit Target unit type
     * @param unitDefinitions All available unit definitions
     * @return Converted quantity, or null if conversion failed
     */
    public BigDecimal convertQuantity(
            BigDecimal quantity,
            String fromUnit,
            String toUnit,
            List<ProductUnitDefinition> unitDefinitions) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Find conversion factors
        ProductUnitDefinition fromUnitDef = unitDefinitions.stream()
                .filter(ud -> ud.getUnitType().equalsIgnoreCase(fromUnit))
                .findFirst()
                .orElse(null);

        ProductUnitDefinition toUnitDef = unitDefinitions.stream()
                .filter(ud -> ud.getUnitType().equalsIgnoreCase(toUnit))
                .findFirst()
                .orElse(null);

        if (fromUnitDef == null || toUnitDef == null) {
            log.warn("Cannot convert from '{}' to '{}': unit definition not found",
                    fromUnit, toUnit);
            return null;
        }

        // Convert to base units first, then to target unit
        // quantityInBaseUnits = quantity × fromConversionFactor
        BigDecimal quantityInBaseUnits = quantity.multiply(fromUnitDef.getConversionFactor());

        // quantityInTargetUnit = quantityInBaseUnits / toConversionFactor
        BigDecimal result = quantityInBaseUnits.divide(
                toUnitDef.getConversionFactor(), 4, RoundingMode.HALF_UP);

        log.debug("Converted {} {} to {} {}: {} × {} / {} = {}",
                quantity, fromUnit, toUnit, result,
                quantity, fromUnitDef.getConversionFactor(),
                toUnitDef.getConversionFactor(), result);

        return result;
    }

    /**
     * Convert quantity to base units (e.g., pieces).
     *
     * @param quantity Quantity to convert
     * @param fromUnit Source unit type
     * @param unitDefinitions All available unit definitions
     * @return Quantity in base units
     */
    public Integer convertToBaseUnits(
            BigDecimal quantity,
            String fromUnit,
            List<ProductUnitDefinition> unitDefinitions) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        ProductUnitDefinition fromUnitDef = unitDefinitions.stream()
                .filter(ud -> ud.getUnitType().equalsIgnoreCase(fromUnit))
                .findFirst()
                .orElse(null);

        if (fromUnitDef == null) {
            log.warn("Cannot convert to base units: unit '{}' not found", fromUnit);
            return 0;
        }

        // quantityInBaseUnits = quantity × conversionFactor
        BigDecimal baseUnits = quantity.multiply(fromUnitDef.getConversionFactor());

        return baseUnits.intValue();
    }
}
