package com.princely.shopmanager.auth.service;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSyncService Tests")
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserSyncService userSyncService;

    private JwtPrincipal testPrincipal;
    private Tenant testTenant;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Setup test tenant
        testTenant = new Tenant();
        testTenant.setId("tenant-123");
        testTenant.setName("Test Tenant");
        testTenant.setContactEmail("test@tenant.com");
        testTenant.setPrimaryAddress("Test Address");
        testTenant.setCity("Test City");
        testTenant.setState("Test State");
        testTenant.setCountry("Test Country");
        testTenant.setPostalCode("12345");
        testTenant.setStatus(Tenant.TenantStatus.ACTIVE);

        // Setup test role
        testRole = Role.builder()
            .id("role-123")
            .name("SHOP_MANAGER")
            .description("Shop Manager Role")
            .build();

        // Setup test JWT principal
        testPrincipal = JwtPrincipal.builder()
            .subject("keycloak-user-123")
            .preferredUsername("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .tenantId("tenant-123")
            .shopId("shop-456")
            .roles(List.of("SHOP_MANAGER", "CASHIER"))
            .build();
    }

    @Test
    @DisplayName("Should create new user when user doesn't exist")
    void shouldCreateNewUserWhenUserDoesntExist() {
        // Given
        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(testTenant));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-123");
            return user;
        });

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("keycloak-user-123");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getTenant()).isEqualTo(testTenant);
        assertThat(result.getStatus()).isEqualTo(User.UserStatus.ACTIVE);

        // Verify user was saved
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when tenant doesn't exist")
    void shouldThrowExceptionWhenTenantDoesntExist() {
        // Given
        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSyncService.syncUserFromKeycloak(testPrincipal))
            .isInstanceOf(UserSyncService.UserSyncException.class)
            .hasMessageContaining("Tenant tenant-123 does not exist in database");

        // Verify user was NOT saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when tenant ID is null")
    void shouldThrowExceptionWhenTenantIdIsNull() {
        // Given
        JwtPrincipal principalWithoutTenant = JwtPrincipal.builder()
            .subject("keycloak-user-123")
            .preferredUsername("testuser")
            .email("test@example.com")
            .tenantId(null)
            .build();

        when(userRepository.findByKeycloakId(principalWithoutTenant.getSubject())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSyncService.syncUserFromKeycloak(principalWithoutTenant))
            .isInstanceOf(UserSyncService.UserSyncException.class)
            .hasMessageContaining("does not exist in database");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update existing user when user exists")
    void shouldUpdateExistingUserWhenUserExists() {
        // Given
        User existingUser = User.builder()
            .id("user-123")
            .keycloakId("keycloak-user-123")
            .username("oldusername")
            .email("old@example.com")
            .firstName("Old")
            .lastName("Name")
            .phoneNumber("123456789")
            .tenant(testTenant)
            .status(User.UserStatus.ACTIVE)
            .roles(new HashSet<>())
            .build();

        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKeycloakId()).isEqualTo("keycloak-user-123");
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");

        // Verify user was saved (updated)
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should not update user when data is already up-to-date")
    void shouldNotUpdateUserWhenDataIsUpToDate() {
        // Given
        User existingUser = User.builder()
            .id("user-123")
            .keycloakId("keycloak-user-123")
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("N/A") // Use N/A instead of empty to match the sync behavior
            .tenant(testTenant)
            .status(User.UserStatus.ACTIVE)
            .roles(new HashSet<>(Set.of(testRole)))
            .build();

        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(testRole));

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("user-123");

        // Verify user was NOT saved (no changes)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should sync roles from JWT to user")
    void shouldSyncRolesFromJwtToUser() {
        // Given
        Role role1 = Role.builder().id("role-1").name("SHOP_MANAGER").build();
        Role role2 = Role.builder().id("role-2").name("CASHIER").build();

        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(testTenant));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(role1, role2));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result.getRoles()).hasSize(2);
        assertThat(result.getRoles()).extracting(Role::getName)
            .containsExactlyInAnyOrder("SHOP_MANAGER", "CASHIER");

        // Verify roles were queried
        ArgumentCaptor<List<String>> rolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(roleRepository).findByNameIn(rolesCaptor.capture());
        assertThat(rolesCaptor.getValue()).containsExactlyInAnyOrder("SHOP_MANAGER", "CASHIER");
    }

    @Test
    @DisplayName("Should handle empty roles in JWT")
    void shouldHandleEmptyRolesInJwt() {
        // Given
        JwtPrincipal principalWithoutRoles = JwtPrincipal.builder()
            .subject("keycloak-user-123")
            .preferredUsername("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .tenantId("tenant-123")
            .roles(List.of())
            .build();

        when(userRepository.findByKeycloakId(principalWithoutRoles.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(testTenant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(principalWithoutRoles);

        // Then
        assertThat(result.getRoles()).isEmpty();
        verify(roleRepository, never()).findByNameIn(anyList());
    }

    @Test
    @DisplayName("Should set status to ACTIVE for new users")
    void shouldSetStatusToActiveForNewUsers() {
        // Given
        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(testTenant));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should activate suspended user on sync")
    void shouldActivateSuspendedUserOnSync() {
        // Given
        User suspendedUser = User.builder()
            .id("user-123")
            .keycloakId("keycloak-user-123")
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("")
            .tenant(testTenant)
            .status(User.UserStatus.SUSPENDED)
            .roles(new HashSet<>())
            .build();

        when(userRepository.findByKeycloakId(testPrincipal.getSubject())).thenReturn(Optional.of(suspendedUser));
        when(roleRepository.findByNameIn(anyList())).thenReturn(List.of(testRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(testPrincipal);

        // Then
        assertThat(result.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle missing firstName and lastName gracefully")
    void shouldHandleMissingFirstNameAndLastNameGracefully() {
        // Given
        JwtPrincipal principalWithoutNames = JwtPrincipal.builder()
            .subject("keycloak-user-123")
            .preferredUsername("testuser")
            .email("test@example.com")
            .firstName(null)
            .lastName(null)
            .tenantId("tenant-123")
            .roles(List.of())
            .build();

        when(userRepository.findByKeycloakId(principalWithoutNames.getSubject())).thenReturn(Optional.empty());
        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(testTenant));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userSyncService.syncUserFromKeycloak(principalWithoutNames);

        // Then
        assertThat(result.getFirstName()).isEqualTo("");
        assertThat(result.getLastName()).isEqualTo("");
    }
}
