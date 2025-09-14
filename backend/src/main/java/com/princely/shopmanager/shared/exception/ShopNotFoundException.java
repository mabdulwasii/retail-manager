package com.princely.shopmanager.shared.exception;

public class ShopNotFoundException extends BusinessException {
    public ShopNotFoundException(String shopId) {
        super("SHOP_NOT_FOUND", "Shop not found: " + shopId);
    }
}