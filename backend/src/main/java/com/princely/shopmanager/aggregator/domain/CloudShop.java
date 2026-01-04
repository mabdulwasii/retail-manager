package com.princely.shopmanager.aggregator.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Cloud Shop entity for the aggregator service.
 * Represents individual shops registered from local embedded installations.
 */
@Entity
@Table(name = "cloud_shops", indexes = {
        @Index(name = "idx_cloud_shop_tenant", columnList = "cloud_tenant_id"),
        @Index(name = "idx_cloud_shop_email", columnList = "shop_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
public class CloudShop extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull(message = "Cloud tenant is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cloud_tenant_id", nullable = false)
    private CloudTenant cloudTenant;

    @NotEmpty(message = "Shop name is required")
    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Email(message = "Valid email is required")
    @Column(name = "shop_email")
    private String shopEmail;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Status of the shop registration.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    /**
     * Check if shop is active.
     */
    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }
}