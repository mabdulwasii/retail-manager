package com.princely.shopmanager.aggregator.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Cloud API Key entity.
 * Allows tenants to manage multiple API keys with different permissions.
 * Each key can be independently revoked, regenerated, and tracked for usage.
 */
@Entity
@Table(name = "cloud_api_keys", indexes = {
        @Index(name = "idx_cloud_api_key_tenant", columnList = "tenant_id"),
        @Index(name = "idx_cloud_api_key_prefix", columnList = "key_prefix"),
        @Index(name = "idx_cloud_api_key_status", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"keyHash"})
@EqualsAndHashCode(callSuper = true)
public class CloudApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    /**
     * First 8 characters of the API key for display purposes.
     * Format: "a1b2c3d4..."
     */
    @NotEmpty(message = "Key prefix is required")
    @Column(name = "key_prefix", nullable = false, length = 16)
    private String keyPrefix;

    /**
     * Masked version of the full key for display.
     * Format: "a1b2c3d4...xyz9"
     */
    @Column(name = "masked_key", length = 100)
    private String maskedKey;

    /**
     * Hashed version of the full API key (bcrypt).
     * The actual key is never stored in plaintext.
     */
    @NotEmpty(message = "Key hash is required")
    @Column(name = "key_hash", nullable = false, length = 500)
    private String keyHash;

    /**
     * Human-readable description of the API key purpose.
     */
    @NotEmpty(message = "Description is required")
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Timestamp when the key was last used.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Timestamp when the key expires (null = never expires).
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Whether the key is currently active.
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Total number of API requests made with this key.
     */
    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    /**
     * Permissions granted to this API key.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cloud_api_key_permissions",
            joinColumns = @JoinColumn(name = "api_key_id")
    )
    @Column(name = "permission")
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    /**
     * Check if the key is currently valid (active and not expired).
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    /**
     * Mark the key as used and increment usage count.
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount = (this.usageCount == null ? 0L : this.usageCount) + 1;
    }

    /**
     * Revoke the API key.
     */
    public void revoke() {
        this.isActive = false;
    }

    /**
     * Check if key has a specific permission.
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * Available API key permissions.
     */
    public static class Permissions {
        public static final String READ = "READ";
        public static final String WRITE = "WRITE";
        public static final String DELETE = "DELETE";
        public static final String SYNC = "SYNC";
        public static final String ADMIN = "ADMIN";

        private Permissions() {
            // Utility class
        }
    }
}
