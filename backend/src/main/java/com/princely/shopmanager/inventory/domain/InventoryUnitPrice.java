package com.princely.shopmanager.inventory.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Batch-specific selling prices for each unit type.
 * While ProductUnitDefinition defines the structure (what units exist),
 * InventoryUnitPrice defines the prices (what each unit costs for this specific batch).
 *
 * Example:
 * Inventory Batch #123 (Coca-Cola, expires 2026-12-31):
 * - piece: ₦1,050
 * - pack: ₦12,000
 * - half_pack: ₦6,050
 * - carton: ₦24,500
 *
 * Different batches of the same product can have different prices.
 */
@Entity
@Table(name = "inventory_unit_prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"inventory_id", "unit_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "inventory")
@EqualsAndHashCode(callSuper = true, exclude = "inventory")
public class InventoryUnitPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    /**
     * Unit type (must match a unit_type in product_unit_definitions)
     * Examples: piece, pack, half_pack, carton, roll, custom
     */
    @Column(name = "unit_type", nullable = false, length = 50)
    private String unitType;

    /**
     * Selling price for this unit in this specific batch
     */
    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    /**
     * Validates that selling price is non-negative
     */
    @PrePersist
    @PreUpdate
    private void validatePrice() {
        if (sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Selling price cannot be negative");
        }
    }
}
