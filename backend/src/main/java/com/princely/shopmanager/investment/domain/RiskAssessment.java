package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_assessments", indexes = {
    @Index(name = "idx_risk_shop", columnList = "shop_id"),
    @Index(name = "idx_risk_transaction", columnList = "transaction_id"),
    @Index(name = "idx_risk_date", columnList = "assessment_date"),
    @Index(name = "idx_risk_level", columnList = "risk_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "transaction"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "transaction"})
public class RiskAssessment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private SalesTransaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false)
    private AssessmentType assessmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    @ElementCollection
    @CollectionTable(
        name = "risk_assessment_flags",
        joinColumns = @JoinColumn(name = "risk_assessment_id")
    )
    @Column(name = "flag")
    @Builder.Default
    private List<String> flags = new ArrayList<>();

    @Column(name = "details", length = 2000)
    private String details;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.PENDING;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_action")
    private ResolutionAction resolutionAction;

    public enum AssessmentType {
        TRANSACTION_FRAUD,
        INVESTMENT_RISK,
        OPERATIONAL_RISK,
        COMPLIANCE_CHECK
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AssessmentStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        ESCALATED
    }

    public enum ResolutionAction {
        NO_ACTION,
        MONITOR,
        INVESTIGATE,
        BLOCK_TRANSACTION,
        SUSPEND_ACCOUNT,
        REPORT_AUTHORITIES
    }

    public void addFlag(String flag) {
        if (!flags.contains(flag)) {
            flags.add(flag);
        }
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void approve(String reviewedBy, String reviewNotes) {
        this.status = AssessmentStatus.APPROVED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
        this.resolutionAction = ResolutionAction.NO_ACTION;
    }

    public void reject(String reviewedBy, String reviewNotes, ResolutionAction action) {
        this.status = AssessmentStatus.REJECTED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
        this.resolutionAction = action;
    }

    public void escalate(String reviewedBy, String reviewNotes) {
        this.status = AssessmentStatus.ESCALATED;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reviewNotes;
    }

    public boolean requiresReview() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }

    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }
}