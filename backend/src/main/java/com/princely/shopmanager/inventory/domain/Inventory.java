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
     * Current stock in base units (e.g., pieces).
     * This is the authoritative stock counter decremented on each sale.
     * Initialised to purchaseQuantity × conversionFactor when stock is created.
     */
    @Builder.Default
    @Column(name = "current_stock", nullable = false)
    private Long currentStock = 0L;

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
     * Returns current stock in base units (e.g., pieces).
     * This is the authoritative remaining stock counter.
     */
    public Integer getCurrentStock() {
        return currentStock != null ? currentStock.intValue() : 0;
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

    /**
     * Sets the current stock to an absolute value (base units).
     * Used for manual stock adjustments.
     */
    public void adjustStock(int newStock, String reason) {
        this.currentStock = (long) Math.max(0, newStock);
        this.lastStockUpdate = LocalDateTime.now();
    }

    /**
     * Adds stock in base units (e.g., after a stock return or new purchase).
     */
    public void addStock(int quantity) {
        if (this.currentStock == null) {
            this.currentStock = 0L;
        }
        this.currentStock = this.currentStock + quantity;
        this.lastStockUpdate = LocalDateTime.now();
    }

    /**
     * Removes stock in base units (e.g., after a sale).
     * The sales pipeline always converts to base units before calling this.
     */
    public void removeStock(int quantity) {
        if (this.currentStock == null) {
            this.currentStock = 0L;
        }
        this.currentStock = Math.max(0L, this.currentStock - quantity);
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