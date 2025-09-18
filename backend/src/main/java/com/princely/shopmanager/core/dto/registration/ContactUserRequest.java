package com.princely.shopmanager.core.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Contact user information for registration (Page 2)
 * This will become the tenant admin user
 * Password will be generated and sent via email
 * User will be required to change on first login
 */
public record ContactUserRequest(
    @NotEmpty(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    String username,

    @Email(message = "Valid email is required")
    @NotEmpty(message = "Email is required")
    String email,

    @NotEmpty(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    String firstName,

    @NotEmpty(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    String lastName,

    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    String phoneNumber,

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
    String postalCode
) {}