package com.princely.shopmanager.shared.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.domain.BaseEntity;
import com.princely.shopmanager.shared.service.AuditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EntityAuditListener {

    private static AuditService auditService;
    private static ShopRepository shopRepository;
    private static ObjectMapper objectMapper;

    @Autowired
    public void setAuditService(@Lazy AuditService auditService) {
        EntityAuditListener.auditService = auditService;
    }

    @Autowired
    public void setShopRepository(@Lazy ShopRepository shopRepository) {
        EntityAuditListener.shopRepository = shopRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        EntityAuditListener.objectMapper = objectMapper;
    }

    @PrePersist
    public void prePersist(Object entity) {
        if (shouldAudit(entity)) {
            try {
                String entityData = serializeEntity(entity);
                logEntityEvent(entity, AuditLog.ActionType.CREATE, null, entityData);
            } catch (Exception e) {
                log.warn("Failed to audit entity creation: {}", e.getMessage());
            }
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (shouldAudit(entity)) {
            try {
                String oldData = getCurrentEntityState(entity);
                String newData = serializeEntity(entity);
                logEntityEvent(entity, AuditLog.ActionType.UPDATE, oldData, newData);
            } catch (Exception e) {
                log.warn("Failed to audit entity update: {}", e.getMessage());
            }
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (shouldAudit(entity)) {
            try {
                String entityData = serializeEntity(entity);
                logEntityEvent(entity, AuditLog.ActionType.DELETE, entityData, null);
            } catch (Exception e) {
                log.warn("Failed to audit entity deletion: {}", e.getMessage());
            }
        }
    }

    private boolean shouldAudit(Object entity) {
        // Audit all BaseEntity instances except:
        // - AuditLog itself (to avoid infinite loops)
        // - AnalyticsCache (to avoid lazy-loading issues with detached entities)
        // - Tenant, User, Shop (to avoid circular reference serialization issues)
        //   These entities have explicit service-level auditing in their respective services
        return entity instanceof BaseEntity
            && !(entity instanceof AuditLog)
            && !(entity instanceof Tenant)
            && !(entity instanceof User)
            && !(entity instanceof Shop)
            && !entity.getClass().getSimpleName().equals("AnalyticsCache");
    }

    private void logEntityEvent(Object entity, AuditLog.ActionType actionType,
                               String oldValues, String newValues) {
        if (auditService == null) {
            log.debug("AuditService not available, skipping entity audit");
            return;
        }

        try {
            String userId = TenantContext.getCurrentUserId();
            String userName = TenantContext.getCurrentUserName();
            String tenantId = TenantContext.getCurrentTenant();

            if (userId == null) {
                userId = "SYSTEM";
                userName = "system";
            }

            Shop shop = null;
            if (tenantId != null && shopRepository != null) {
                shop = shopRepository.findById(tenantId).orElse(null);
            }

            String entityType = entity.getClass().getSimpleName();
            String entityId = getEntityId(entity);

            String description = String.format("Entity %s %s: %s",
                entityType, actionType.name().toLowerCase(), entityId);

            auditService.logDataModification(
                shop, userId, userName, actionType, entityType, entityId,
                description, oldValues, newValues
            );

        } catch (Exception e) {
            log.error("Failed to log entity audit event", e);
        }
    }

    private String serializeEntity(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            log.debug("Failed to serialize entity for audit: {}", e.getMessage());
            return entity.toString();
        }
    }

    private String getCurrentEntityState(Object entity) {
        // This is a simplified approach - in a real system you might want to
        // fetch the current state from the database
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            log.debug("Failed to get current entity state: {}", e.getMessage());
            return null;
        }
    }

    private String getEntityId(Object entity) {
        if (entity instanceof BaseEntity baseEntity) {
            try {
                // Use reflection to get the ID field
                var idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object id = idField.get(entity);
                return id != null ? id.toString() : "unknown";
            } catch (Exception e) {
                log.debug("Failed to extract entity ID: {}", e.getMessage());
                return "unknown";
            }
        }
        return "unknown";
    }
}