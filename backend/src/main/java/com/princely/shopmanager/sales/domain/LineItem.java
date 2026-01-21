package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transaction", "product"})
@EqualsAndHashCode(callSuper = true, exclude = {"transaction", "product"})
public class LineItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private SalesTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "product_category")
    private String productCategory;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Unit type sold (piece, pack, half_pack, etc.) - matches product_unit_definitions
     */
    @Column(name = "unit_type", length = 50)
    private String unitType;

    /**
     * Display label for the unit sold (e.g., "Pack (12pcs)")
     */
    @Column(name = "unit_label", length = 100)
    private String unitLabel;

    /**
     * Conversion factor at time of sale (e.g., 12 for pack of 12)
     */
    @Builder.Default
    @Column(name = "unit_conversion_factor", precision = 10, scale = 4)
    private BigDecimal unitConversionFactor = BigDecimal.ONE;

    /**
     * Quantity in base units (quantity × conversion_factor) used for FEFO
     * Example: Sold 5 packs → quantity=5, unitConversionFactor=12, baseUnitQuantity=60
     */
    @Column(name = "base_unit_quantity")
    private Integer baseUnitQuantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Builder.Default
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        }

        lineTotal = subtotal.subtract(discountAmount).add(taxAmount);
    }
}