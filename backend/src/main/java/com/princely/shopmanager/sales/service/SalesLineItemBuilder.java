package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.sales.domain.LineItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for building sales line items from inventory allocations.
 * Handles pricing logic and product denormalization.
 */
@Service
@Slf4j
public class SalesLineItemBuilder {

    /**
     * Builds line items from inventory allocations.
     *
     * @param allocations List of inventory allocations
     * @return List of line items ready to be added to transaction
     */
    public List<LineItem> buildFromAllocations(List<InventoryAllocationService.InventoryAllocation> allocations) {
        log.debug("Building {} line items from allocations", allocations.size());

        List<LineItem> lineItems = new ArrayList<>();

        for (InventoryAllocationService.InventoryAllocation allocation : allocations) {
            LineItem lineItem = buildLineItem(allocation);
            lineItems.add(lineItem);
        }

        log.debug("Successfully built {} line items", lineItems.size());
        return lineItems;
    }

    /**
     * Builds a single line item from an inventory allocation.
     * Gets selling price from first allocated inventory batch or uses request price.
     */
    private LineItem buildLineItem(InventoryAllocationService.InventoryAllocation allocation) {
        // Get selling price from first allocated inventory batch
        BigDecimal sellingPrice = allocation.inventories.isEmpty() ?
            allocation.request.getUnitPrice() :
            allocation.inventories.get(0).getSellingPrice();

        LineItem lineItem = LineItem.builder()
            .product(allocation.product)
            .productName(allocation.product.getName())
            .productSku(allocation.product.getSku())
            .productCategory(allocation.product.getCategory() != null ?
                allocation.product.getCategory().getName() : null)
            .quantity(allocation.request.getQuantity())
            .unitPrice(allocation.request.getUnitPrice() != null ?
                allocation.request.getUnitPrice() : sellingPrice)
            .discountAmount(allocation.request.getDiscount() != null ?
                allocation.request.getDiscount() : BigDecimal.ZERO)
            .build();

        lineItem.calculateLineTotal();

        return lineItem;
    }
}
