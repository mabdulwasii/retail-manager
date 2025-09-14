package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Shop;
import org.springframework.data.jpa.domain.Specification;

public class ShopSpecifications {

    public static Specification<Shop> hasStatus(Shop.ShopStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Shop> belongsToTenant(String tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    public static Specification<Shop> nameContains(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Shop> inCity(String city) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<Shop> inState(String state) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("state")), state.toLowerCase());
    }

    public static Specification<Shop> inCountry(String country) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("country")), country.toLowerCase());
    }

    public static Specification<Shop> emailContains(String email) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }
}