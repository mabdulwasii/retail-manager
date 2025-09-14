package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for creating a new shop.
 *
 * This DTO contains all the required and optional information needed
 * to create a new shop entity in the system. It includes validation
 * constraints to ensure data integrity and proper business rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new shop")
public class ShopCreateRequest {

    @Schema(description = "Shop name", example = "Downtown Electronics Store", required = true)
    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Shop description", example = "Electronics and gadgets store in downtown area")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Shop physical address", example = "123 Main Street", required = true)
    @NotBlank(message = "Address is required")
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

    @Schema(description = "Contact email address", example = "contact@downtownelectronics.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Schema(description = "Tax identification number", example = "TAX123456789")
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    @Schema(description = "Shop opening date", example = "2024-01-15T09:00:00")
    private LocalDateTime openingDate;
}