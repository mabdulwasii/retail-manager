package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Super Admin Bootstrap Service Tests")
class SuperAdminBootstrapServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks
    private SuperAdminBootstrapService bootstrapService;

    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        superAdminRole = new Role();
        superAdminRole.setName("ROLE_SYSTEM_ADMIN");

        // Set default configuration values
        ReflectionTestUtils.setField(bootstrapService, "bootstrapEnabled", true);
        ReflectionTestUtils.setField(bootstrapService, "superAdminUsername", "superadmin");
        ReflectionTestUtils.setField(bootstrapService, "superAdminEmail", "superAdmin@shopmanager.local");
        ReflectionTestUtils.setField(bootstrapService, "superAdminFirstName", "Super");
        ReflectionTestUtils.setField(bootstrapService, "superAdminLastName", "Admin");
        ReflectionTestUtils.setField(bootstrapService, "superAdminPassword", "");
    }

    @Test
    @DisplayName("Should create super admin when none exists")
    void shouldCreateSuperAdminWhenNoneExists() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(keycloakUserService.generatePassword()).thenReturn("generated-password");
        when(keycloakUserService.createUser(any())).thenReturn("keycloak-id-123");

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository).save(any(User.class));
        verify(keycloakUserService).createUser(any());
        verify(auditService).logEvent(eq("SYSTEM_ADMIN_BOOTSTRAP"), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should skip bootstrap when super admin already exists")
    void shouldSkipBootstrapWhenSuperAdminExists() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(1L);

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(keycloakUserService, never()).createUser(any());
        verify(auditService, never()).logEvent(anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should skip bootstrap when disabled")
    void shouldSkipBootstrapWhenDisabled() {
        // Given
        ReflectionTestUtils.setField(bootstrapService, "bootstrapEnabled", false);

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).countByRolesContaining(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle missing SYSTEM_ADMIN role gracefully")
    void shouldHandleMissingSuperAdminRoleGracefully() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.empty());

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(keycloakUserService, never()).createUser(any());
    }

    @Test
    @DisplayName("Should use provided password when configured")
    void shouldUseProvidedPasswordWhenConfigured() {
        // Given
        ReflectionTestUtils.setField(bootstrapService, "superAdminPassword", "custom-password");
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(keycloakUserService.createUser(any())).thenReturn("keycloak-id-123");

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(keycloakUserService, never()).generatePassword();
        verify(keycloakUserService).createUser(argThat(request ->
            request.password().equals("custom-password")));
    }

    @Test
    @DisplayName("Should return correct bootstrap requirement status")
    void shouldReturnCorrectBootstrapRequirementStatus() {
        // Given - bootstrap enabled, no existing super admin
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);

        // When
        boolean isRequired = bootstrapService.isBootstrapRequired();

        // Then
        assertThat(isRequired).isTrue();

        // Given - existing super admin
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(1L);

        // When
        boolean isNotRequired = bootstrapService.isBootstrapRequired();

        // Then
        assertThat(isNotRequired).isFalse();
    }

    @Test
    @DisplayName("Should return correct super admin count")
    void shouldReturnCorrectSuperAdminCount() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(2L);

        // When
        long count = bootstrapService.getSuperAdminCount();

        // Then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should return zero count when SYSTEM_ADMIN role not found")
    void shouldReturnZeroCountWhenRoleNotFound() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.empty());

        // When
        long count = bootstrapService.getSuperAdminCount();

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle Keycloak creation failure gracefully")
    void shouldHandleKeycloakCreationFailureGracefully() {
        // Given
        when(roleRepository.findByName("ROLE_SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(keycloakUserService.generatePassword()).thenReturn("generated-password");
        when(keycloakUserService.createUser(any())).thenThrow(new RuntimeException("Keycloak error"));

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logEvent(anyString(), anyString(), anyMap());
    }
}