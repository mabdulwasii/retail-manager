package com.princely.shopmanager.aggregator.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/**
 * Cloud Tenant entity for the aggregator service.
 * Stores information about tenants registered from local embedded installations.
 * This is separate from the core Tenant entity to maintain independence
 * between cloud aggregator and main multi-tenant application.
 */
@Entity
@Table(name = "cloud_tenants", indexes = {
        @Index(name = "idx_cloud_tenant_api_key", columnList = "api_key"),
        @Index(name = "idx_cloud_tenant_email", columnList = "tenant_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"apiKeyHash"})
@EqualsAndHashCode(callSuper = true)
public class CloudTenant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Tenant name is required")
    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @NotEmpty(message = "Tenant email is required")
    @Email(message = "Valid email is required")
    @Column(name = "tenant_email", nullable = false)
    private String tenantEmail;

    @Column(name = "company_registration")
    private String companyRegistration;

    @Column(name = "tax_id")
    private String taxId;

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
     * API key for authenticating sync requests from local installation.
     * This is hashed and stored securely.
     */
    @NotEmpty(message = "API key hash is required")
    @Column(name = "api_key_hash", nullable = false, length = 500)
    private String apiKeyHash;

    /**
     * Status of the cloud tenant registration.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.ACTIVE;

    /**
     * Number of shops registered under this tenant.
     */
    @Builder.Default
    @Column(name = "shop_count", nullable = false)
    private Integer shopCount = 0;

    /**
     * Subscription tier for the cloud tenant.
     * Could be used for future billing/feature access control.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", length = 50)
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    public enum Status {
        ACTIVE,
        SUSPENDED,
        INACTIVE
    }

    public enum SubscriptionTier {
        FREE,
        BASIC,
        PREMIUM,
        ENTERPRISE
    }

    /**
     * Increment shop count when a new shop is registered.
     */
    public void incrementShopCount() {
        this.shopCount = (this.shopCount == null ? 0 : this.shopCount) + 1;
    }

    /**
     * Decrement shop count when a shop is removed.
     */
    public void decrementShopCount() {
        this.shopCount = Math.max(0, (this.shopCount == null ? 0 : this.shopCount) - 1);
    }

    /**
     * Check if tenant is active.
     */
    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }
}