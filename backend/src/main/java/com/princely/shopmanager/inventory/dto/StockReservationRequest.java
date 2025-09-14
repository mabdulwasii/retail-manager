package com.princely.shopmanager.inventory.dto;

import com.princely.shopmanager.inventory.domain.InventoryHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for stock reservation operations.
 *
 * This parameter object encapsulates all the necessary information
 * for reserving stock, reducing method parameter count and improving
 * code maintainability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationRequest {

    /**
     * The inventory ID to reserve stock from
     */
    private String inventoryId;

    /**
     * Quantity to reserve
     */
    private int quantity;

    /**
     * Reference ID for tracking the reservation (e.g., order ID)
     */
    private String referenceId;

    /**
     * Type of reference for the reservation
     */
    private InventoryHistory.ReferenceType referenceType;

    /**
     * Optional reason for the reservation
     */
    private String reason;
}