package com.princely.shopmanager.inventory.specification;

import com.princely.shopmanager.inventory.domain.Inventory;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class InventorySpecifications {

    // Field name constants
    private static final String FIELD_EXPIRY_DATE = "expiryDate";
    private static final String FIELD_PRODUCT = "product";

    public static Specification<Inventory> hasLowStock() {
        return (root, query, cb) ->
            cb.lessThanOrEqualTo(
                cb.diff(root.<Integer>get("currentStock"), root.<Integer>get("reservedStock")),
                root.<Integer>get("minimumStock")
            );
    }

    public static Specification<Inventory> expiresWithinDays(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return (root, query, cb) ->
            cb.and(
                cb.isNotNull(root.get(FIELD_EXPIRY_DATE)),
                cb.lessThanOrEqualTo(root.get(FIELD_EXPIRY_DATE), threshold)
            );
    }

    public static Specification<Inventory> forShop(String shopId) {
        return (root, query, cb) ->
            cb.equal(root.get("shop").get("id"), shopId);
    }

    public static Specification<Inventory> hasStatus(Inventory.InventoryStatus status) {
        return (root, query, cb) ->
            cb.equal(root.get("status"), status);
    }

    public static Specification<Inventory> forProduct(String productId) {
        return (root, query, cb) ->
            cb.equal(root.get(FIELD_PRODUCT).get("id"), productId);
    }

    public static Specification<Inventory> atLocation(String location) {
        return (root, query, cb) ->
            cb.equal(root.get("location"), location);
    }

    public static Specification<Inventory> hasStockAvailable(int minimumQuantity) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(
                cb.diff(root.<Integer>get("currentStock"), root.<Integer>get("reservedStock")),
                minimumQuantity
            );
    }

    public static Specification<Inventory> isExpired() {
        return (root, query, cb) ->
            cb.and(
                cb.isNotNull(root.get(FIELD_EXPIRY_DATE)),
                cb.lessThan(root.get(FIELD_EXPIRY_DATE), LocalDate.now())
            );
    }

    public static Specification<Inventory> forCategory(String categoryId) {
        return (root, query, cb) ->
            cb.equal(root.get(FIELD_PRODUCT).get("category").get("id"), categoryId);
    }

    public static Specification<Inventory> productNameContains(String productName) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get(FIELD_PRODUCT).get("name")),
                   "%" + productName.toLowerCase() + "%");
    }
}