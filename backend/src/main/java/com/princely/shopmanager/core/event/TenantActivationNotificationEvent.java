package com.princely.shopmanager.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a tenant is activated/rejected and notification needs to be sent
 */
@Getter
public class TenantActivationNotificationEvent extends ApplicationEvent {

    private final String tenantId;
    private final String tenantName;
    private final String contactUserEmail;
    private final String contactUserName;
    private final boolean approved;
    private final String rejectionReason;
    private final String adminUserId;

    public TenantActivationNotificationEvent(Object source, String tenantId, String tenantName,
                                           String contactUserEmail, String contactUserName,
                                           boolean approved, String rejectionReason, String adminUserId) {
        super(source);
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.contactUserEmail = contactUserEmail;
        this.contactUserName = contactUserName;
        this.approved = approved;
        this.rejectionReason = rejectionReason;
        this.adminUserId = adminUserId;
    }
}