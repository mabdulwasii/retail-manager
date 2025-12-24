package com.princely.shopmanager.fraud.event;

import com.princely.shopmanager.fraud.domain.RiskAssessment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new risk assessment is created.
 * Triggers notification workflows for high-risk assessments requiring review.
 */
@Getter
public class RiskAssessmentCreatedEvent extends ApplicationEvent {

    private final transient RiskAssessment assessment;
    private final String tenantId;
    private final String shopId;

    public RiskAssessmentCreatedEvent(Object source, RiskAssessment assessment, String tenantId, String shopId) {
        super(source);
        this.assessment = assessment;
        this.tenantId = tenantId;
        this.shopId = shopId;
    }

    @Override
    public String toString() {
        return String.format("RiskAssessmentCreatedEvent{assessmentId='%s', riskLevel='%s', tenantId='%s', shopId='%s'}",
            assessment.getId(), assessment.getRiskLevel(), tenantId, shopId);
    }
}