package com.princely.shopmanager.shared.event;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.event.TenantActivationNotificationEvent;
import com.princely.shopmanager.core.event.TenantRegistrationNotificationEvent;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event listener for handling notification events asynchronously
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Handle tenant registration notification events
     */
    @ApplicationModuleListener
    @Async("notificationTaskExecutor")
    public void handleTenantRegistrationNotification(TenantRegistrationNotificationEvent event) {
        log.info("Processing tenant registration notification event for tenant: {}", event.getTenantName());

        try {
            // Send confirmation to tenant contact user
            notificationService.sendTenantRegistrationConfirmation(
                event.getTenantName(),
                event.getContactUserEmail(),
                event.getContactUserName()
            );

            // Get all super admins and send alerts
            List<User> superAdmins = getSuperAdmins();
            if (!superAdmins.isEmpty()) {
                notificationService.sendNewTenantRegistrationAlert(
                    event.getTenantId(),
                    event.getTenantName(),
                    superAdmins
                );
                log.info("Sent registration alerts to {} super admins for tenant: {}",
                        superAdmins.size(), event.getTenantName());
            } else {
                log.warn("No super admins found - registration alerts not sent for tenant: {}", event.getTenantName());
            }

        } catch (Exception e) {
            log.error("Failed to process tenant registration notification for tenant: {}", event.getTenantName(), e);
        }
    }

    /**
     * Handle tenant activation notification events
     */
    @ApplicationModuleListener
    @Async("notificationTaskExecutor")
    public void handleTenantActivationNotification(TenantActivationNotificationEvent event) {
        log.info("Processing tenant activation notification event for tenant: {} (approved: {})",
                event.getTenantName(), event.isApproved());

        try {
            notificationService.sendTenantActivationNotification(
                event.getTenantName(),
                event.getContactUserEmail(),
                event.getContactUserName(),
                event.isApproved(),
                event.getRejectionReason()
            );

            log.info("Sent activation notification for tenant: {} (approved: {})",
                    event.getTenantName(), event.isApproved());

        } catch (Exception e) {
            log.error("Failed to process tenant activation notification for tenant: {}", event.getTenantName(), e);
        }
    }

    /**
     * Get all super admin users
     */
    private List<User> getSuperAdmins() {
        try {
            Role superAdminRole = roleRepository.findByName("SYSTEM_ADMIN").orElse(null);
            if (superAdminRole == null) {
                log.warn("SYSTEM_ADMIN role not found");
                return List.of();
            }

            return userRepository.findByRolesContaining(superAdminRole);

        } catch (Exception e) {
            log.error("Failed to retrieve super admin users", e);
            return List.of();
        }
    }
}