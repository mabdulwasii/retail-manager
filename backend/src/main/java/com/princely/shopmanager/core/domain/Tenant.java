package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "company_registration")
    private String companyRegistration;

    @Column(name = "tax_id")
    private String taxId;

    @Column(nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

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
    private TenantStatus status = TenantStatus.ACTIVE;

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
}