package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Shop;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating shop information.
 *
 * This DTO contains fields that can be updated for an existing shop.
 * All fields are optional to allow partial updates. The shop ID and
 * tenant ID cannot be changed after creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating shop information")
public class ShopUpdateRequest {

    @Schema(description = "Shop name", example = "Downtown Electronics Store")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Shop description", example = "Electronics and gadgets store in downtown area")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Shop physical address", example = "123 Main Street")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Schema(description = "City", example = "New York")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Schema(description = "State or province", example = "NY")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Schema(description = "Country", example = "United States")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Schema(description = "Postal code", example = "10001")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phoneNumber;

    @Schema(description = "Contact email address", example = "contact@downtownelectronics.com")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Schema(description = "Tax identification number", example = "TAX123456789")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Schema(description = "Shop status", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "CLOSED"})
    private String status;

    @Schema(description = "Shop opening date", example = "2024-01-15T09:00:00")
    private LocalDateTime openingDate;

    @Schema(description = "Shop configuration settings")
    private ShopConfigurationRequest configuration;

    /**
     * Applies the update request to an existing shop entity.
     * Only updates fields that are not null in the request.
     *
     * @param shop The existing shop entity to update
     */
    public void applyTo(Shop shop) {
        if (name != null) shop.setName(name);
        if (description != null) shop.setDescription(description);
        if (address != null) shop.setAddress(address);
        if (city != null) shop.setCity(city);
        if (state != null) shop.setState(state);
        if (country != null) shop.setCountry(country);
        if (postalCode != null) shop.setPostalCode(postalCode);
        if (phoneNumber != null) shop.setPhoneNumber(phoneNumber);
        if (email != null) shop.setEmail(email);
        if (taxId != null) shop.setTaxId(taxId);
        if (status != null) shop.setStatus(Shop.ShopStatus.valueOf(status.toUpperCase()));
        if (openingDate != null) shop.setOpeningDate(openingDate);
        if (configuration != null) {
            if (shop.getConfiguration() == null) {
                shop.setConfiguration(configuration.toEntity());
            } else {
                configuration.applyTo(shop.getConfiguration());
            }
        }
    }
}