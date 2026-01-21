package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Defines available units for a product (catalog level - no prices).
 * Each product can have multiple unit definitions (e.g., piece, pack, half_pack).
 * The actual selling prices are set at the inventory level (batch-specific).
 *
 * Example:
 * Product: Coca-Cola 500ml
 * Units:
 * - piece (base unit, conversion=1.0)
 * - pack (conversion=12.0) - 1 pack = 12 pieces
 * - half_pack (conversion=6.0) - 1 half pack = 6 pieces
 * - carton (conversion=24.0) - 1 carton = 24 pieces
 */
@Entity
@Table(name = "product_unit_definitions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product_id", "unit_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "product")
@EqualsAndHashCode(callSuper = true, exclude = "product")
public class ProductUnitDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Unit identifier: piece, pack, half_pack, quarter_pack, carton, roll, custom, etc.
     */
    @Column(name = "unit_type", nullable = false, length = 50)
    private String unitType;

    /**
     * Display name: "Piece", "Pack (12pcs)", "Half Pack (6pcs)", "Carton (2 packs)"
     */
    @Column(name = "unit_label", nullable = false, length = 100)
    private String unitLabel;

    /**
     * How many base units in this unit.
     * Example: If base unit is "piece" and this is "pack" of 12, conversion_factor = 12.0
     */
    @Column(name = "conversion_factor", nullable = false, precision = 10, scale = 4)
    private BigDecimal conversionFactor;

    /**
     * True for the smallest unit (usually piece, kg, liter, etc.)
     * Base unit always has conversion_factor = 1.0
     */
    @Column(name = "is_base_unit", nullable = false)
    @Builder.Default
    private Boolean isBaseUnit = false;

    /**
     * Sort order for display (0 = first, higher numbers displayed later)
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Validates that base unit has conversion factor of 1.0
     */
    @PrePersist
    @PreUpdate
    private void validateBaseUnit() {
        if (Boolean.TRUE.equals(isBaseUnit)) {
            if (conversionFactor.compareTo(BigDecimal.ONE) != 0) {
                throw new IllegalStateException("Base unit must have conversion factor of 1.0");
            }
        }
        if (conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Conversion factor must be positive");
        }
    }
}
