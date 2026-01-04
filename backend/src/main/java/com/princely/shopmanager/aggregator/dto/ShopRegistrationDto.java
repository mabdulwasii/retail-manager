package com.princely.shopmanager.aggregator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for shop information during registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopRegistrationDto {

    @NotEmpty(message = "Shop name is required")
    private String shopName;

    @Email(message = "Valid email is required")
    private String shopEmail;

    private String address;

    private String city;

    private String state;

    private String country;

    private String phoneNumber;
}