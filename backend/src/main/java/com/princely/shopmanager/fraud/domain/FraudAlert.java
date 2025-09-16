package com.princely.shopmanager.fraud.domain;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.domain.Shop;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String alertNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "investment_id")
    private String investmentId;

    @Column(name = "risk_score", precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel;

    @ElementCollection
    @CollectionTable(name = "fraud_alert_evidence", joinColumns = @JoinColumn(name = "alert_id"))
    @MapKeyColumn(name = "evidence_key")
    @Column(name = "evidence_value", columnDefinition = "TEXT")
    private Map<String, String> evidence;

    @Column(name = "detection_rule")
    private String detectionRule;

    @Column(name = "detection_timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime detectionTimestamp = LocalDateTime.now();

    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_by")
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "false_positive")
    @Builder.Default
    private Boolean falsePositive = false;

    @Column(name = "tenant_id", nullable = false)
    @Builder.Default
    private String tenantId = TenantContext.getCurrentTenant();

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AlertType {
        SUSPICIOUS_TRANSACTION,
        UNUSUAL_INVESTMENT_PATTERN,
        EXCESSIVE_WITHDRAWALS,
        DUPLICATE_TRANSACTIONS,
        VELOCITY_FRAUD,
        ACCOUNT_TAKEOVER,
        PRICE_MANIPULATION,
        RETURN_FRAUD,
        COLLUSION_DETECTION,
        ANOMALOUS_BEHAVIOR
    }

    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        ACTIVE,
        ACKNOWLEDGED,
        INVESTIGATING,
        RESOLVED,
        FALSE_POSITIVE,
        DISMISSED
    }

    public boolean canBeAcknowledged() {
        return status == AlertStatus.ACTIVE;
    }

    public boolean canBeResolved() {
        return status == AlertStatus.ACKNOWLEDGED || status == AlertStatus.INVESTIGATING;
    }

    public void acknowledge(String acknowledgedBy) {
        if (!canBeAcknowledged()) {
            throw new IllegalStateException("Alert cannot be acknowledged in current status: " + status);
        }
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = acknowledgedBy;
        this.acknowledgedAt = LocalDateTime.now();
    }

    public void resolve(String resolvedBy, String resolutionNotes) {
        if (!canBeResolved()) {
            throw new IllegalStateException("Alert cannot be resolved in current status: " + status);
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
    }

    public void markAsFalsePositive(String userId, String notes) {
        this.status = AlertStatus.FALSE_POSITIVE;
        this.falsePositive = true;
        this.resolvedBy = userId;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = notes;
    }

    public void dismiss(String userId, String reason) {
        this.status = AlertStatus.DISMISSED;
        this.resolvedBy = userId;
        this.resolvedAt = LocalDateTime.now();
        this.resolutionNotes = reason;
    }

    // Convenience methods for shop data
    public String getShopId() {
        return shop != null ? shop.getId() : null;
    }

    public String getShopName() {
        return shop != null ? shop.getName() : null;
    }
}