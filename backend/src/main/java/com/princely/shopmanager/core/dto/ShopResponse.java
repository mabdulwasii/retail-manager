package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Shop;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for shop response data.
 *
 * This DTO represents the shop information returned in API responses.
 * It excludes sensitive internal details while providing all necessary
 * information for client applications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shop information response")
public class ShopResponse {

    @Schema(description = "Unique shop identifier", example = "shop-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Shop name", example = "Downtown Electronics Store")
    private String name;

    @Schema(description = "Tenant identifier", example = "tenant-downtown-electronics")
    private String tenantId;

    @Schema(description = "Shop description", example = "Electronics and gadgets store in downtown area")
    private String description;

    @Schema(description = "Shop physical address", example = "123 Main Street")
    private String address;

    @Schema(description = "City", example = "New York")
    private String city;

    @Schema(description = "State or province", example = "NY")
    private String state;

    @Schema(description = "Country", example = "United States")
    private String country;

    @Schema(description = "Postal code", example = "10001")
    private String postalCode;

    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    private String phoneNumber;

    @Schema(description = "Contact email address", example = "contact@downtownelectronics.com")
    private String email;

    @Schema(description = "Tax identification number", example = "TAX123456789")
    private String taxId;

    @Schema(description = "Shop status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "CLOSED"})
    private String status;

    @Schema(description = "Shop opening date", example = "2024-01-15T09:00:00")
    private LocalDateTime openingDate;

    @Schema(description = "When the shop was created in the system", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "When the shop was last updated", example = "2024-01-15T14:30:00")
    private LocalDateTime updatedAt;

    /**
     * Factory method to create ShopResponse from Shop entity.
     *
     * @param shop The shop entity to convert
     * @return ShopResponse DTO with mapped data
     */
    public static ShopResponse fromEntity(Shop shop) {
        return ShopResponse.builder()
            .id(shop.getId())
            .name(shop.getName())
            .tenantId(shop.getTenantId())
            .description(shop.getDescription())
            .address(shop.getAddress())
            .city(shop.getCity())
            .state(shop.getState())
            .country(shop.getCountry())
            .postalCode(shop.getPostalCode())
            .phoneNumber(shop.getPhoneNumber())
            .email(shop.getEmail())
            .taxId(shop.getTaxId())
            .status(shop.getStatus() != null ? shop.getStatus().name() : null)
            .openingDate(shop.getOpeningDate())
            .createdAt(shop.getCreatedAt())
            .updatedAt(shop.getUpdatedAt())
            .build();
    }
}