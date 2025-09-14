package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.exception.BusinessRuleViolationException;
import com.princely.shopmanager.shared.exception.InvalidStatusTransitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "features.business.state-machine.enabled", havingValue = "true", matchIfMissing = true)
public class ShopStatusStateMachine {

    private final Map<Shop.ShopStatus, Set<Shop.ShopStatus>> allowedTransitions = Map.of(
        Shop.ShopStatus.ACTIVE, Set.of(Shop.ShopStatus.INACTIVE, Shop.ShopStatus.SUSPENDED),
        Shop.ShopStatus.INACTIVE, Set.of(Shop.ShopStatus.ACTIVE, Shop.ShopStatus.SUSPENDED, Shop.ShopStatus.CLOSED),
        Shop.ShopStatus.SUSPENDED, Set.of(Shop.ShopStatus.ACTIVE, Shop.ShopStatus.INACTIVE),
        Shop.ShopStatus.CLOSED, Set.of() // Terminal state
    );

    public boolean canTransition(Shop.ShopStatus from, Shop.ShopStatus to) {
        return allowedTransitions.getOrDefault(from, Set.of()).contains(to);
    }

    public void validateTransition(Shop.ShopStatus from, Shop.ShopStatus to, String shopId) {
        if (!canTransition(from, to)) {
            throw new InvalidStatusTransitionException(
                String.format("Cannot transition from %s to %s", from, to)
            );
        }

        // Additional business rules
        if (to == Shop.ShopStatus.CLOSED && hasActiveInvestments(shopId)) {
            throw new BusinessRuleViolationException("Cannot close shop with active investments");
        }

        if (to == Shop.ShopStatus.SUSPENDED && hasPendingOrders(shopId)) {
            throw new BusinessRuleViolationException("Cannot suspend shop with pending orders");
        }
    }

    public void validateTransition(Shop.ShopStatus from, Shop.ShopStatus to) {
        validateTransition(from, to, null);
    }

    private boolean hasActiveInvestments(String shopId) {
        // TODO: Implement check for active investments
        // This would query the investment repository to check for active investments
        return false;
    }

    private boolean hasPendingOrders(String shopId) {
        // TODO: Implement check for pending orders
        // This would query the orders repository to check for pending orders
        return false;
    }

    public Set<Shop.ShopStatus> getAllowedTransitions(Shop.ShopStatus currentStatus) {
        return allowedTransitions.getOrDefault(currentStatus, Set.of());
    }
}