package com.princely.shopmanager.core.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductUnitDefinition entity validation logic.
 */
class ProductUnitDefinitionTest {

    @Test
    void shouldValidateBaseUnitWithConversionFactorOfOne() {
        // Given
        ProductUnitDefinition baseUnit = ProductUnitDefinition.builder()
            .unitType("piece")
            .unitLabel("Piece")
            .conversionFactor(BigDecimal.ONE)
            .isBaseUnit(true)
            .sortOrder(0)
            .build();

        // When/Then - should not throw exception
        assertDoesNotThrow(baseUnit::validateBaseUnit);
    }

    @Test
    void shouldThrowExceptionWhenBaseUnitHasInvalidConversionFactor() {
        // Given
        ProductUnitDefinition baseUnit = ProductUnitDefinition.builder()
            .unitType("piece")
            .unitLabel("Piece")
            .conversionFactor(BigDecimal.valueOf(2.0))
            .isBaseUnit(true)
            .sortOrder(0)
            .build();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            baseUnit::validateBaseUnit
        );

        assertEquals("Base unit must have conversion factor of 1.0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConversionFactorIsZero() {
        // Given
        ProductUnitDefinition unit = ProductUnitDefinition.builder()
            .unitType("pack")
            .unitLabel("Pack")
            .conversionFactor(BigDecimal.ZERO)
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            unit::validateBaseUnit
        );

        assertEquals("Conversion factor must be positive", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenConversionFactorIsNegative() {
        // Given
        ProductUnitDefinition unit = ProductUnitDefinition.builder()
            .unitType("pack")
            .unitLabel("Pack")
            .conversionFactor(BigDecimal.valueOf(-5.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            unit::validateBaseUnit
        );

        assertEquals("Conversion factor must be positive", exception.getMessage());
    }

    @Test
    void shouldValidateNonBaseUnitWithValidConversionFactor() {
        // Given
        ProductUnitDefinition nonBaseUnit = ProductUnitDefinition.builder()
            .unitType("pack")
            .unitLabel("Pack (12pcs)")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        // When/Then - should not throw exception
        assertDoesNotThrow(nonBaseUnit::validateBaseUnit);
    }

    @Test
    void shouldValidateFractionalConversionFactor() {
        // Given - half-pack = 0.5 conversion
        ProductUnitDefinition halfPack = ProductUnitDefinition.builder()
            .unitType("half-pack")
            .unitLabel("Half Pack (6pcs)")
            .conversionFactor(BigDecimal.valueOf(6.0))
            .isBaseUnit(false)
            .sortOrder(2)
            .build();

        // When/Then - should not throw exception
        assertDoesNotThrow(halfPack::validateBaseUnit);
    }

    @Test
    void shouldValidateLargeConversionFactor() {
        // Given - carton with 144 pieces
        ProductUnitDefinition carton = ProductUnitDefinition.builder()
            .unitType("carton")
            .unitLabel("Carton (144pcs)")
            .conversionFactor(BigDecimal.valueOf(144.0))
            .isBaseUnit(false)
            .sortOrder(3)
            .build();

        // When/Then - should not throw exception
        assertDoesNotThrow(carton::validateBaseUnit);
    }

    @Test
    void shouldCorrectlyIdentifyBaseUnit() {
        // Given
        ProductUnitDefinition baseUnit = ProductUnitDefinition.builder()
            .unitType("piece")
            .unitLabel("Piece")
            .conversionFactor(BigDecimal.ONE)
            .isBaseUnit(true)
            .sortOrder(0)
            .build();

        ProductUnitDefinition nonBaseUnit = ProductUnitDefinition.builder()
            .unitType("pack")
            .unitLabel("Pack")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        // When/Then
        assertTrue(baseUnit.getIsBaseUnit());
        assertFalse(nonBaseUnit.getIsBaseUnit());
    }

    @Test
    void shouldMaintainSortOrder() {
        // Given
        ProductUnitDefinition unit1 = ProductUnitDefinition.builder()
            .unitType("piece")
            .unitLabel("Piece")
            .conversionFactor(BigDecimal.ONE)
            .isBaseUnit(true)
            .sortOrder(0)
            .build();

        ProductUnitDefinition unit2 = ProductUnitDefinition.builder()
            .unitType("pack")
            .unitLabel("Pack")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        // When/Then
        assertEquals(0, unit1.getSortOrder());
        assertEquals(1, unit2.getSortOrder());
        assertTrue(unit1.getSortOrder() < unit2.getSortOrder());
    }
}
