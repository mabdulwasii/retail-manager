package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.service.UserManagementService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
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
    private TenantRepository tenantRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private SuperAdminBootstrapService bootstrapService;

    private Role superAdminRole;
    private Tenant defaultTenant;
    private Shop defaultShop;

    @BeforeEach
    void setUp() {
        // Create bootstrap service manually
        bootstrapService = new SuperAdminBootstrapService(
                userRepository,
                roleRepository,
                auditService,
                tenantRepository,
                shopRepository,
                environment
        );

        // Inject optional dependencies using reflection
        ReflectionTestUtils.setField(bootstrapService, "userManagementService", userManagementService);
        ReflectionTestUtils.setField(bootstrapService, "passwordEncoder", passwordEncoder);

        // Set up test data
        superAdminRole = new Role();
        superAdminRole.setName("ROLE_SYSTEM_ADMIN");

        defaultTenant = new Tenant();
        defaultTenant.setId("tenant-1");
        defaultTenant.setName("Default Tenant");

        defaultShop = new Shop();
        defaultShop.setId("shop-1");
        defaultShop.setName("Default Shop");
        defaultShop.setTenant(defaultTenant);

        // Set default configuration values
        ReflectionTestUtils.setField(bootstrapService, "bootstrapEnabled", true);
        ReflectionTestUtils.setField(bootstrapService, "superAdminUsername", "superadmin");
        ReflectionTestUtils.setField(bootstrapService, "superAdminEmail", "superAdmin@shopmanager.local");
        ReflectionTestUtils.setField(bootstrapService, "superAdminFirstName", "Super");
        ReflectionTestUtils.setField(bootstrapService, "superAdminLastName", "Admin");
        ReflectionTestUtils.setField(bootstrapService, "superAdminPhoneNumber", "1-000-000-0000");
        ReflectionTestUtils.setField(bootstrapService, "superAdminPassword", "");
    }

    @Test
    @DisplayName("Should create super admin when none exists (embedded mode)")
    void shouldCreateSuperAdminWhenNoneExists() {
        // Given - Embedded mode
        when(environment.getActiveProfiles()).thenReturn(new String[]{"embedded"});
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(tenantRepository.findAll()).thenReturn(List.of(defaultTenant));
        when(shopRepository.findByTenantId("tenant-1")).thenReturn(List.of(defaultShop));
        when(passwordEncoder.encode("changeme")).thenReturn("hashed-password");

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("changeme");
        verify(auditService).logEvent(eq("SYSTEM_ADMIN_BOOTSTRAP"), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should skip bootstrap when super admin already exists")
    void shouldSkipBootstrapWhenSuperAdminExists() {
        // Given
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(1L);

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository, never()).save(any(User.class));
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
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.empty());

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should use provided password when configured (embedded mode)")
    void shouldUseProvidedPasswordWhenConfigured() {
        // Given - Embedded mode with custom password
        ReflectionTestUtils.setField(bootstrapService, "superAdminPassword", "custom-password");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"embedded"});
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(tenantRepository.findAll()).thenReturn(List.of(defaultTenant));
        when(shopRepository.findByTenantId("tenant-1")).thenReturn(List.of(defaultShop));
        when(passwordEncoder.encode("custom-password")).thenReturn("hashed-custom-password");

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then
        verify(passwordEncoder).encode("custom-password");
        verify(userRepository).save(any(User.class));
        verify(auditService).logEvent(eq("SYSTEM_ADMIN_BOOTSTRAP"), anyString(), anyMap());
    }

    @Test
    @DisplayName("Should return correct bootstrap requirement status")
    void shouldReturnCorrectBootstrapRequirementStatus() {
        // Given - bootstrap enabled, no existing super admin
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
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
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
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
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.empty());

        // When
        long count = bootstrapService.getSuperAdminCount();

        // Then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle user management service creation failure gracefully (cloud mode)")
    void shouldHandleKeycloakCreationFailureGracefully() {
        // Given - Cloud mode
        when(environment.getActiveProfiles()).thenReturn(new String[]{"cloud"});
        when(roleRepository.findByName("SYSTEM_ADMIN")).thenReturn(Optional.of(superAdminRole));
        when(userRepository.countByRolesContaining(superAdminRole)).thenReturn(0L);
        when(userManagementService.generatePassword()).thenReturn("generated-password");
        when(userManagementService.createUser(any())).thenThrow(new RuntimeException("User management error"));

        // When
        bootstrapService.bootstrapSuperAdmin();

        // Then - Should not save user if user management service fails
        verify(userRepository, never()).save(any(User.class));
        verify(auditService, never()).logEvent(anyString(), anyString(), anyMap());
    }
}