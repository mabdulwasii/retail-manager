package com.princely.shopmanager.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a tenant registration is completed and notifications need to be sent
 */
@Getter
public class TenantRegistrationNotificationEvent extends ApplicationEvent {

    private final String tenantId;
    private final String tenantName;
    private final String contactUserEmail;
    private final String contactUserName;
    private final String clientIp;
    private final String userAgent;

    public TenantRegistrationNotificationEvent(Object source, String tenantId, String tenantName,
                                             String contactUserEmail, String contactUserName,
                                             String clientIp, String userAgent) {
        super(source);
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.contactUserEmail = contactUserEmail;
        this.contactUserName = contactUserName;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
    }
}