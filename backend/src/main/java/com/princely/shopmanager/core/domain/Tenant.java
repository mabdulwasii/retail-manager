package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shops", "users"})
@EqualsAndHashCode(callSuper = true, exclude = {"shops", "users"})
public class Tenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotEmpty(message = "Description is required")
    @Column(length = 1000)
    private String description;

    @Column(name = "company_registration")
    private String companyRegistration;

    @Column(name = "tax_id")
    private String taxId;

    @Column(nullable = false)
    private String contactEmail;

    @NotNull(message = "Contact user is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private User contactUser;

    @Column(name = "contact_phone")
    private String contactPhone;

    @NotEmpty(message = "Primary address is required")
    @Column(name = "primary_address")
    private String primaryAddress;

    private String city;

    private String state;

    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.INACTIVE;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Builder.Default
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Shop> shops = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    public enum TenantStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        TERMINATED
    }

    /**
     * Get the contact email - either from the User entity or the legacy field
     */
    public String getEffectiveContactEmail() {
        if (contactUser != null && contactUser.getEmail() != null) {
            return contactUser.getEmail();
        }
        return contactEmail;
    }

    /**
     * Get the contact user's full name if available
     */
    public String getContactUserFullName() {
        if (contactUser != null) {
            return contactUser.getFullName();
        }
        return null;
    }

    /**
     * Check if this tenant has a proper User entity as contact
     */
    public boolean hasContactUser() {
        return contactUser != null;
    }
}