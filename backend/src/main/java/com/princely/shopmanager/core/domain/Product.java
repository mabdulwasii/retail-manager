package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_shop", columnList = "shop_id"),
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "category"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "category"})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Builder.Default
    @Column(name = "quantity_in_stock", nullable = false)
    private Integer quantityInStock = 0;

    @Builder.Default
    @Column(name = "minimum_stock_level")
    private Integer minimumStockLevel = 0;

    @Builder.Default
    @Column(name = "maximum_stock_level")
    private Integer maximumStockLevel = 1000;

    @Builder.Default
    @Column(name = "reorder_point")
    private Integer reorderPoint = 10;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_taxable")
    private boolean isTaxable = true;

    @Builder.Default
    @Column(name = "is_discountable")
    private boolean isDiscountable = true;

    @Column(name = "image_url")
    private String imageUrl;

    private String unit;

    @Column(name = "weight_in_grams")
    private Double weightInGrams;

    public enum ProductStatus {
        ACTIVE,
        INACTIVE,
        OUT_OF_STOCK,
        DISCONTINUED
    }

    public void decreaseStock(int quantity) {
        if (this.quantityInStock < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        this.quantityInStock -= quantity;
        if (this.quantityInStock == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void increaseStock(int quantity) {
        this.quantityInStock += quantity;
        if (this.status == ProductStatus.OUT_OF_STOCK && this.quantityInStock > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public boolean needsReorder() {
        return this.quantityInStock <= this.reorderPoint;
    }
}