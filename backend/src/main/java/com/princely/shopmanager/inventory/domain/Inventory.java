package com.princely.shopmanager.inventory.domain;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.BaseEntity;
import com.princely.shopmanager.shared.domain.ShopAware;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"shop_id", "product_id", "batch_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "product", "unitPrices"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "product", "unitPrices"})
public class Inventory extends BaseEntity implements ShopAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder.Default
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @Builder.Default
    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 0;

    @Builder.Default
    @Column(name = "maximum_stock")
    private Integer maximumStock = 0;

    @Builder.Default
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 0;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    /**
     * Smallest sellable unit for stock tracking (piece, kg, liter, etc.)
     */
    @Builder.Default
    @Column(name = "base_unit", length = 50)
    private String baseUnit = "piece";

    /**
     * Unit in which this batch was purchased (e.g., pack, carton)
     */
    @Column(name = "purchase_unit", length = 50)
    private String purchaseUnit;

    /**
     * Quantity purchased in purchase_unit (e.g., 10 packs)
     */
    @Column(name = "purchase_quantity", precision = 10, scale = 2)
    private BigDecimal purchaseQuantity;

    /**
     * Total cost for all purchased quantity (e.g., ₦106,000 for 20 packs)
     * System will calculate cost per unit from this: totalPurchaseCost / purchaseQuantity
     */
    @Column(name = "total_purchase_cost", precision = 12, scale = 2)
    private BigDecimal totalPurchaseCost;

    private String location;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status = InventoryStatus.ACTIVE;

    @Column(name = "last_stock_update")
    private LocalDateTime lastStockUpdate;

    /**
     * Batch-specific selling prices for each unit type.
     * Different units can have different prices for this inventory batch.
     */
    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventoryUnitPrice> unitPrices = new ArrayList<>();

    public enum InventoryStatus {
        ACTIVE,
        INACTIVE,
        DISCONTINUED,
        QUARANTINED,
        EXPIRED
    }

    public Integer getAvailableStock() {
        // Calculate current stock from purchase quantity converted to base units
        Integer currentStockInBaseUnits = getCurrentStock();
        return Math.max(0, currentStockInBaseUnits - reservedStock);
    }

    /**
     * Calculate current stock in base units from purchase quantity
     * This is computed dynamically, not stored
     */
    public Integer getCurrentStock() {
        if (purchaseQuantity == null || purchaseQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        // Stock is tracked via purchaseQuantity - need to convert to base units
        // This will be properly calculated with conversion factors
        return purchaseQuantity.intValue(); // Temporary - will be enhanced with proper conversion
    }

    public boolean isLowStock() {
        return getAvailableStock() <= minimumStock;
    }

    public boolean isOutOfStock() {
        return getAvailableStock() <= 0;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        return expiryDate != null &&
               expiryDate.isAfter(LocalDate.now()) &&
               expiryDate.isBefore(LocalDate.now().plusDays(daysThreshold));
    }

    public boolean canSell(int quantity) {
        return status == InventoryStatus.ACTIVE &&
               !isExpired() &&
               getAvailableStock() >= quantity;
    }

    public void reserveStock(int quantity) {
        if (canSell(quantity)) {
            this.reservedStock += quantity;
            this.lastStockUpdate = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot reserve " + quantity + " units. Available: " + getAvailableStock());
        }
    }

    public void releaseReservedStock(int quantity) {
        this.reservedStock = Math.max(0, this.reservedStock - quantity);
        this.lastStockUpdate = LocalDateTime.now();
    }

    public void adjustStock(int newStock, String reason) {
        // Stock is now tracked via purchaseQuantity
        // This method updates the purchase quantity
        this.purchaseQuantity = BigDecimal.valueOf(newStock);
        this.lastStockUpdate = LocalDateTime.now();
    }

    public void addStock(int quantity) {
        if (this.purchaseQuantity == null) {
            this.purchaseQuantity = BigDecimal.ZERO;
        }
        this.purchaseQuantity = this.purchaseQuantity.add(BigDecimal.valueOf(quantity));
        this.lastStockUpdate = LocalDateTime.now();
    }

    public void removeStock(int quantity) {
        if (this.purchaseQuantity == null) {
            this.purchaseQuantity = BigDecimal.ZERO;
        }
        this.purchaseQuantity = this.purchaseQuantity.subtract(BigDecimal.valueOf(quantity)).max(BigDecimal.ZERO);
        this.lastStockUpdate = LocalDateTime.now();
    }

    /**
     * Returns the shop ID this inventory belongs to.
     * Required by {@link ShopAware} interface for shop-level access control.
     *
     * @return shop ID, or null if shop is not loaded
     */
    @Override
    public String getShopId() {
        return shop != null ? shop.getId() : null;
    }
}