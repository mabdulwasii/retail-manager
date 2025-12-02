package com.princely.shopmanager.shared.domain;

/**
 * Marker interface for entities that are scoped to a specific shop.
 *
 * Entities implementing this interface can be validated for shop-level access control.
 * This enables consistent access validation patterns across all shop-scoped resources.
 *
 * <p>Examples of shop-aware entities:
 * <ul>
 *   <li>Product - belongs to a shop</li>
 *   <li>Inventory - belongs to a shop</li>
 *   <li>Expense - belongs to a shop</li>
 *   <li>SalesTransaction - belongs to a shop</li>
 *   <li>Receipt - belongs to a shop</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * @Entity
 * public class Product extends BaseEntity implements ShopAware {
 *     @ManyToOne
 *     private Shop shop;
 *
 *     @Override
 *     public String getShopId() {
 *         return shop != null ? shop.getId() : null;
 *     }
 * }
 * }</pre>
 *
 * @see com.princely.shopmanager.auth.security.ShopAccessValidator
 */
public interface ShopAware {

    /**
     * Returns the ID of the shop this entity belongs to.
     *
     * @return shop ID, or null if not associated with a shop
     */
    String getShopId();
}
