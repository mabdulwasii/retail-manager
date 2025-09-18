package com.princely.shopmanager.core.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Tenant information for registration (Page 1)
 */
public record TenantInfoRequest(
    @NotEmpty(message = "Tenant name is required")
    @Size(min = 2, max = 100, message = "Tenant name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$", message = "Tenant name can only contain letters, numbers, and spaces")
    String name,

    @NotEmpty(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    String description,

    @Email(message = "Valid email is required")
    @NotEmpty(message = "Contact email is required")
    String email,

    @NotEmpty(message = "Primary address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    String primaryAddress,

    @Size(max = 100, message = "City cannot exceed 100 characters")
    String city,

    @Size(max = 100, message = "State cannot exceed 100 characters")
    String state,

    @Size(max = 100, message = "Country cannot exceed 100 characters")
    String country,

    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    String postalCode,

    // Optional business registration details
    @Size(max = 50, message = "Company registration cannot exceed 50 characters")
    String companyRegistration,

    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    String taxId,

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    String contactPhone
) {}