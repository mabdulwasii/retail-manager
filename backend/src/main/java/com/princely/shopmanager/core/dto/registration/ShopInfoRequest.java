package com.princely.shopmanager.core.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Shop information for registration (Page 3)
 * A tenant can register multiple shops at once
 */
public record ShopInfoRequest(
    // Optional: for updating existing shops during registration
    String id,

    @NotEmpty(message = "Shop name is required")
    @Size(min = 2, max = 100, message = "Shop name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Shop name can only contain letters, numbers, and spaces")
    String name,

    @NotEmpty(message = "Shop description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    String description,

    @NotEmpty(message = "Shop address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    String address,

    @Size(max = 100, message = "City cannot exceed 100 characters")
    String city,

    @Size(max = 100, message = "State cannot exceed 100 characters")
    String state,

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    String country,

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    String postalCode,

    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phoneNumber,

    @Email(message = "Valid email is required")
    @NotEmpty(message = "Email is required")
    String email,

    // Optional business details
    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    String taxId
) {}