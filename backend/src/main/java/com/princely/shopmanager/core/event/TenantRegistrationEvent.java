package com.princely.shopmanager.core.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Event published when a new tenant registration is submitted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRegistrationEvent extends ApplicationEvent {

    private String tenantId;
    private String tenantName;
    private String contactUserId;
    private String contactUserEmail;
    private List<String> shopIds;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    public TenantRegistrationEvent(Object source, String tenantId, String tenantName,
                                   String contactUserId, String contactUserEmail,
                                   List<String> shopIds, String ipAddress,
                                   String userAgent, Map<String, Object> metadata) {
        super(source);
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.contactUserId = contactUserId;
        this.contactUserEmail = contactUserEmail;
        this.shopIds = shopIds;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.metadata = metadata;
        this.timestamp = LocalDateTime.now();
    }
}