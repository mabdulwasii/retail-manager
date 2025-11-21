package com.princely.shopmanager.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.princely.shopmanager.shared.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"products", "tenant"})
@EqualsAndHashCode(callSuper = true, exclude = {"products", "tenant"})
public class Shop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull(message = "Tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnoreProperties({"contactUser", "users", "shops"})
    private Tenant tenant;

    @NotEmpty(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @NotEmpty(message = "Address is required")
    @Column(nullable = false)
    private String address;

    private String city;

    private String state;

    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @NotEmpty(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email is required")
    @Column(nullable = false)
    private String email;

    @Column(name = "tax_id")
    private String taxId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShopStatus status = ShopStatus.INACTIVE;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Builder.Default
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();


    @Embedded
    private ShopConfiguration configuration;

    public enum ShopStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        CLOSED
    }

    // Convenience method for receipt service
    public String getPhone() {
        return phoneNumber;
    }
}