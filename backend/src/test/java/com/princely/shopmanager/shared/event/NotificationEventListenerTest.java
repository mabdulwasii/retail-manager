package com.princely.shopmanager.shared.event;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.event.TenantActivationNotificationEvent;
import com.princely.shopmanager.core.event.TenantRegistrationNotificationEvent;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Event Listener Tests")
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    private Role superAdminRole;
    private List<User> superAdmins;

    @BeforeEach
    void setUp() {
        superAdminRole = Role.builder()
            .name("SUPER_ADMIN")
            .description("Super Administrator")
            .build();

        User superAdmin1 = User.builder()
            .id("admin-1")
            .email("admin1@shopmanager.com")
            .firstName("Super")
            .lastName("Admin1")
            .roles(Set.of(superAdminRole))
            .build();

        User superAdmin2 = User.builder()
            .id("admin-2")
            .email("admin2@shopmanager.com")
            .firstName("Super")
            .lastName("Admin2")
            .roles(Set.of(superAdminRole))
            .build();

        superAdmins = List.of(superAdmin1, superAdmin2);
    }

    @Test
    @DisplayName("Should handle tenant registration notification event successfully")
    void shouldHandleTenantRegistrationNotificationEvent() {
        // Given
        TenantRegistrationNotificationEvent event = new TenantRegistrationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            "192.168.1.1",
            "TestUserAgent"
        );

        when(roleRepository.findByName("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.findByRolesContaining(superAdminRole)).thenReturn(superAdmins);

        // When
        notificationEventListener.handleTenantRegistrationNotification(event);

        // Then
        // Verify tenant confirmation was sent
        verify(notificationService).sendTenantRegistrationConfirmation(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator"
        );

        // Verify super admin alerts were sent
        verify(notificationService).sendNewTenantRegistrationAlert(
            "tenant-123",
            "Test Tenant Company",
            superAdmins
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Should handle tenant registration notification when no super admins found")
    void shouldHandleTenantRegistrationNotificationWhenNoSuperAdmins() {
        // Given
        TenantRegistrationNotificationEvent event = new TenantRegistrationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            "192.168.1.1",
            "TestUserAgent"
        );

        when(roleRepository.findByName("SUPER_ADMIN")).thenReturn(Optional.empty());

        // When
        notificationEventListener.handleTenantRegistrationNotification(event);

        // Then
        // Verify tenant confirmation was still sent
        verify(notificationService).sendTenantRegistrationConfirmation(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator"
        );

        // Verify no super admin alerts were sent
        verify(notificationService, never()).sendNewTenantRegistrationAlert(anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should handle tenant activation notification event for approval")
    void shouldHandleTenantActivationNotificationForApproval() {
        // Given
        TenantActivationNotificationEvent event = new TenantActivationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            true,
            null,
            "admin-user-123"
        );

        // When
        notificationEventListener.handleTenantActivationNotification(event);

        // Then
        verify(notificationService).sendTenantActivationNotification(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            true,
            null
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Should handle tenant activation notification event for rejection")
    void shouldHandleTenantActivationNotificationForRejection() {
        // Given
        TenantActivationNotificationEvent event = new TenantActivationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            false,
            "Does not meet business requirements",
            "admin-user-123"
        );

        // When
        notificationEventListener.handleTenantActivationNotification(event);

        // Then
        verify(notificationService).sendTenantActivationNotification(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            false,
            "Does not meet business requirements"
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    @DisplayName("Should handle notification service exceptions gracefully")
    void shouldHandleNotificationServiceExceptionsGracefully() {
        // Given
        TenantRegistrationNotificationEvent event = new TenantRegistrationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            "192.168.1.1",
            "TestUserAgent"
        );

        // Mock exception from notification service
        doThrow(new RuntimeException("Email service unavailable"))
            .when(notificationService)
            .sendTenantRegistrationConfirmation(anyString(), anyString(), anyString());

        // When & Then - Should not throw exception
        notificationEventListener.handleTenantRegistrationNotification(event);

        // Verify the method was called despite the exception
        verify(notificationService).sendTenantRegistrationConfirmation(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator"
        );
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully when retrieving super admins")
    void shouldHandleRepositoryExceptionsGracefully() {
        // Given
        TenantRegistrationNotificationEvent event = new TenantRegistrationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            "192.168.1.1",
            "TestUserAgent"
        );

        // Mock repository exception
        when(roleRepository.findByName("SUPER_ADMIN"))
            .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - Should not throw exception
        notificationEventListener.handleTenantRegistrationNotification(event);

        // Verify tenant confirmation was still sent
        verify(notificationService).sendTenantRegistrationConfirmation(
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator"
        );

        // Verify no super admin alerts were sent due to exception
        verify(notificationService, never()).sendNewTenantRegistrationAlert(anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should send notifications to multiple super admins")
    void shouldSendNotificationsToMultipleSuperAdmins() {
        // Given
        TenantRegistrationNotificationEvent event = new TenantRegistrationNotificationEvent(
            this,
            "tenant-123",
            "Test Tenant Company",
            "admin@testtenant.com",
            "Test Administrator",
            "192.168.1.1",
            "TestUserAgent"
        );

        when(roleRepository.findByName("SUPER_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.findByRolesContaining(superAdminRole)).thenReturn(superAdmins);

        // When
        notificationEventListener.handleTenantRegistrationNotification(event);

        // Then
        verify(notificationService).sendNewTenantRegistrationAlert(
            "tenant-123",
            "Test Tenant Company",
            superAdmins
        );

        // Verify the notification service received the correct list of super admins
        verify(userRepository).findByRolesContaining(superAdminRole);
    }
}