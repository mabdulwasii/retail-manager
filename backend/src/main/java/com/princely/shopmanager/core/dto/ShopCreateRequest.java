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

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_ADDRESS_LENGTH = 255;
    public static final int MAX_CITY_LENGTH = 100;
    public static final int MAX_STATE_LENGTH = 100;
    public static final int MAX_COUNTRY_LENGTH = 100;
    public static final int MAX_POSTAL_CODE_LENGTH = 20;
    public static final int MAX_PHONE_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_TAX_ID_LENGTH = 50;

    @Schema(description = "Shop name", example = "Downtown Electronics Store", required = true)
    @NotBlank(message = "Shop name is required")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "Shop name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters")
    private String name;

    @Schema(description = "Shop description", example = "Electronics and gadgets store in downtown area")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters")
    private String description;

    @Schema(description = "Shop physical address", example = "123 Main Street", required = true)
    @NotBlank(message = "Address is required")
    @Size(max = MAX_ADDRESS_LENGTH, message = "Address must not exceed " + MAX_ADDRESS_LENGTH + " characters")
    private String address;

    @Schema(description = "City", example = "New York")
    @Size(max = MAX_CITY_LENGTH, message = "City must not exceed " + MAX_CITY_LENGTH + " characters")
    private String city;

    @Schema(description = "State or province", example = "NY")
    @Size(max = MAX_STATE_LENGTH, message = "State must not exceed " + MAX_STATE_LENGTH + " characters")
    private String state;

    @Schema(description = "Country", example = "United States")
    @Size(max = MAX_COUNTRY_LENGTH, message = "Country must not exceed " + MAX_COUNTRY_LENGTH + " characters")
    private String country;

    @Schema(description = "Postal code", example = "10001")
    @Size(max = MAX_POSTAL_CODE_LENGTH, message = "Postal code must not exceed " + MAX_POSTAL_CODE_LENGTH + " characters")
    private String postalCode;

    @Schema(description = "Contact phone number", example = "+1-555-123-4567")
    @Size(max = MAX_PHONE_LENGTH, message = "Phone number must not exceed " + MAX_PHONE_LENGTH + " characters")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Contact email address", example = "contact@downtownelectronics.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = MAX_EMAIL_LENGTH, message = "Email must not exceed " + MAX_EMAIL_LENGTH + " characters")
    private String email;

    @Schema(description = "Tax identification number", example = "TAX123456789")
    @Size(max = MAX_TAX_ID_LENGTH, message = "Tax ID must not exceed " + MAX_TAX_ID_LENGTH + " characters")
    private String taxId;

    @Schema(description = "Shop opening date", example = "2024-01-15T09:00:00")
    private LocalDateTime openingDate;
}