package com.princely.shopmanager.fraud.event;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new fraud alert is created.
 * Triggers notification workflows to alert relevant stakeholders.
 */
@Getter
public class FraudAlertCreatedEvent extends ApplicationEvent {

    private final transient FraudAlert alert;
    private final String tenantId;
    private final String shopId;

    public FraudAlertCreatedEvent(Object source, FraudAlert alert, String tenantId, String shopId) {
        super(source);
        this.alert = alert;
        this.tenantId = tenantId;
        this.shopId = shopId;
    }

    @Override
    public String toString() {
        return String.format("FraudAlertCreatedEvent{alertNumber='%s', severity='%s', tenantId='%s', shopId='%s'}",
            alert.getAlertNumber(), alert.getSeverity(), tenantId, shopId);
    }
}