package com.princely.shopmanager.shared.security;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.TenantAware;
import com.princely.shopmanager.shared.exception.TenantAccessDeniedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "features.security.tenant-validation.enabled", havingValue = "true", matchIfMissing = true)
public class TenantSecurityValidator {

    public void validateTenantAccess(String resourceTenantId) {
        String currentTenantId = TenantContext.getCurrentTenantId();
        if (currentTenantId != null && !currentTenantId.equals(resourceTenantId)) {
            throw new TenantAccessDeniedException("Resource belongs to different tenant");
        }
    }

    public <T extends TenantAware> T validateAndReturn(T entity) {
        validateTenantAccess(entity.getTenantId());
        return entity;
    }

    public void validateShopAccess(Shop shop) {
        if (shop != null && shop.getTenant() != null) {
            validateTenantAccess(shop.getTenant().getId());
        }
    }

    public Shop validateAndReturnShop(Shop shop) {
        validateShopAccess(shop);
        return shop;
    }
}