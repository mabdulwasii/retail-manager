package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.service.KeycloakUserService;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.registration.*;
import com.princely.shopmanager.core.event.TenantActivationNotificationEvent;
import com.princely.shopmanager.core.event.TenantRegistrationNotificationEvent;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.shared.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tenant Registration Service Tests")
class TenantRegistrationServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private KeycloakUserService keycloakUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TenantRegistrationService tenantRegistrationService;

    private Role tenantAdminRole;
    private TenantRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        tenantAdminRole = Role.builder()
            .name("TENANT_ADMIN")
            .description("Tenant Administrator")
            .build();

        validRequest = createValidRegistrationRequest();
    }

    @Test
    @DisplayName("Should successfully register tenant with all components")
    void shouldSuccessfullyRegisterTenant() {
        // Given
        setupSuccessfulRegistrationMocks();

        // When
        TenantRegistrationResponse response = tenantRegistrationService.registerTenant(
            validRequest, "192.168.1.1", "TestUserAgent"
        );

        // Then
        assertThat(response.registrationStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(response.tenantName()).isEqualTo("Test Tenant");

        // Verify tenant creation
        verify(tenantRepository, times(2)).save(any(Tenant.class));

        // Verify user creation
        verify(userRepository).save(any(User.class));

        // Verify Keycloak integration
        verify(keycloakUserService).generatePassword();
        verify(keycloakUserService).createUser(any());

        // Verify shops creation
        verify(shopRepository).save(any(Shop.class));

        // Verify audit logging
        verify(auditService).logEvent(eq("TENANT_REGISTRATION"), anyString(), any());

        // Verify events published
        verify(eventPublisher, times(2)).publishEvent(any());
    }

    @Test
    @DisplayName("Should publish notification event after successful registration")
    void shouldPublishNotificationEventAfterSuccessfulRegistration() {
        // Given
        setupSuccessfulRegistrationMocks();
        ArgumentCaptor<TenantRegistrationNotificationEvent> eventCaptor =
            ArgumentCaptor.forClass(TenantRegistrationNotificationEvent.class);

        // When
        tenantRegistrationService.registerTenant(validRequest, "192.168.1.1", "TestUserAgent");

        // Then
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<Object> publishedEvents = eventCaptor.getAllValues().stream()
            .map(Object.class::cast)
            .toList();

        assertThat(publishedEvents).anyMatch(event ->
            event instanceof TenantRegistrationNotificationEvent notificationEvent &&
            notificationEvent.getTenantName().equals("Test Tenant") &&
            notificationEvent.getContactUserEmail().equals("admin@test.com")
        );
    }

    @Test
    @DisplayName("Should throw exception when tenant name already exists")
    void shouldThrowExceptionWhenTenantNameExists() {
        // Given
        when(tenantRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tenantRegistrationService.registerTenant(
            validRequest, "192.168.1.1", "TestUserAgent"
        ))
        .isInstanceOf(TenantRegistrationException.class)
        .hasMessageContaining("Tenant name already exists");
    }

    @Test
    @DisplayName("Should throw exception when contact user email already exists")
    void shouldThrowExceptionWhenContactUserEmailExists() {
        // Given
        when(tenantRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tenantRegistrationService.registerTenant(
            validRequest, "192.168.1.1", "TestUserAgent"
        ))
        .isInstanceOf(TenantRegistrationException.class)
        .hasMessageContaining("User with email already exists");
    }

    @Test
    @DisplayName("Should throw exception when contact user username already exists")
    void shouldThrowExceptionWhenContactUserUsernameExists() {
        // Given
        when(tenantRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tenantRegistrationService.registerTenant(
            validRequest, "192.168.1.1", "TestUserAgent"
        ))
        .isInstanceOf(TenantRegistrationException.class)
        .hasMessageContaining("Username already exists");
    }

    @Test
    @DisplayName("Should throw exception when terms not accepted")
    void shouldThrowExceptionWhenTermsNotAccepted() {
        // Given
        TenantRegistrationRequest requestWithoutTerms = validRequest.toBuilder()
            .termsAccepted(false)
            .build();

        when(tenantRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(keycloakUserService.userExistsByEmail(anyString())).thenReturn(false);
        when(keycloakUserService.userExistsByUsername(anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> tenantRegistrationService.registerTenant(
            requestWithoutTerms, "192.168.1.1", "TestUserAgent"
        ))
        .isInstanceOf(TenantRegistrationException.class)
        .hasMessageContaining("Terms and conditions must be accepted");
    }

    @Test
    @DisplayName("Should activate tenant successfully")
    void shouldActivateTenantSuccessfully() {
        // Given
        Tenant inactiveTenant = createInactiveTenant();
        User contactUser = createContactUser(inactiveTenant);
        inactiveTenant.setContactUser(contactUser);

        TenantActivationRequest activationRequest = TenantActivationRequest.approve(
            "tenant-123", List.of("shop-123")
        );

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(inactiveTenant));
        when(shopRepository.findAllById(anyList())).thenReturn(List.of(createInactiveShop(inactiveTenant)));

        ArgumentCaptor<TenantActivationNotificationEvent> eventCaptor =
            ArgumentCaptor.forClass(TenantActivationNotificationEvent.class);

        // When
        tenantRegistrationService.activateTenant(activationRequest, "admin-123");

        // Then
        assertThat(inactiveTenant.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
        assertThat(contactUser.getStatus()).isEqualTo(User.UserStatus.ACTIVE);

        verify(tenantRepository).save(inactiveTenant);
        verify(userRepository).save(contactUser);
        verify(keycloakUserService).updateUserStatus(contactUser.getKeycloakId(), true);
        verify(shopRepository).saveAll(anyList());
        verify(auditService).logEvent(eq("TENANT_ACTIVATION"), anyString(), any());

        // Verify activation notification event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TenantActivationNotificationEvent event = eventCaptor.getValue();
        assertThat(event.getTenantId()).isEqualTo("tenant-123");
        assertThat(event.isApproved()).isTrue();
    }

    @Test
    @DisplayName("Should reject tenant successfully")
    void shouldRejectTenantSuccessfully() {
        // Given
        Tenant inactiveTenant = createInactiveTenant();
        User contactUser = createContactUser(inactiveTenant);
        inactiveTenant.setContactUser(contactUser);

        TenantActivationRequest rejectionRequest = TenantActivationRequest.reject(
            "tenant-123", "Does not meet requirements"
        );

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(inactiveTenant));

        ArgumentCaptor<TenantActivationNotificationEvent> eventCaptor =
            ArgumentCaptor.forClass(TenantActivationNotificationEvent.class);

        // When
        tenantRegistrationService.activateTenant(rejectionRequest, "admin-123");

        // Then
        assertThat(inactiveTenant.getStatus()).isEqualTo(Tenant.TenantStatus.TERMINATED);

        verify(tenantRepository).save(inactiveTenant);
        verify(keycloakUserService).updateUserStatus(contactUser.getKeycloakId(), false);
        verify(auditService).logEvent(eq("TENANT_REJECTION"), anyString(), any());

        // Verify rejection notification event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TenantActivationNotificationEvent event = eventCaptor.getValue();
        assertThat(event.getTenantId()).isEqualTo("tenant-123");
        assertThat(event.isApproved()).isFalse();
        assertThat(event.getRejectionReason()).isEqualTo("Does not meet requirements");
    }

    @Test
    @DisplayName("Should check tenant name availability correctly")
    void shouldCheckTenantNameAvailability() {
        // Given
        when(tenantRepository.existsByNameIgnoreCase("available-name")).thenReturn(false);
        when(tenantRepository.existsByNameIgnoreCase("taken-name")).thenReturn(true);

        // When & Then
        assertThat(tenantRegistrationService.isTenantNameAvailable("available-name")).isTrue();
        assertThat(tenantRegistrationService.isTenantNameAvailable("taken-name")).isFalse();
    }

    @Test
    @DisplayName("Should get pending registrations correctly")
    void shouldGetPendingRegistrationsCorrectly() {
        // Given
        Tenant pendingTenant = createInactiveTenant();
        when(tenantRepository.findByStatus(Tenant.TenantStatus.INACTIVE))
            .thenReturn(List.of(pendingTenant));

        // When
        List<PendingTenantResponse> pendingRegistrations = tenantRegistrationService.getPendingRegistrations();

        // Then
        assertThat(pendingRegistrations).hasSize(1);
        assertThat(pendingRegistrations.get(0).tenantId()).isEqualTo("tenant-123");
        assertThat(pendingRegistrations.get(0).tenantName()).isEqualTo("Test Tenant");
    }

    private void setupSuccessfulRegistrationMocks() {
        // Mock validation checks
        when(tenantRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(keycloakUserService.userExistsByEmail(anyString())).thenReturn(false);
        when(keycloakUserService.userExistsByUsername(anyString())).thenReturn(false);

        // Mock role
        when(roleRepository.findByName("TENANT_ADMIN")).thenReturn(Optional.of(tenantAdminRole));

        // Mock Keycloak
        when(keycloakUserService.generatePassword()).thenReturn("generatedPassword123!");
        when(keycloakUserService.createUser(any())).thenReturn("keycloak-user-id-123");

        // Mock entity saves
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setId("tenant-123");
            return tenant;
        });

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-123");
            return user;
        });

        when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> {
            Shop shop = invocation.getArgument(0);
            shop.setId("shop-123");
            return shop;
        });
    }

    private Tenant createInactiveTenant() {
        return Tenant.builder()
            .id("tenant-123")
            .name("Test Tenant")
            .contactEmail("contact@test.com")
            .status(Tenant.TenantStatus.INACTIVE)
            .build();
    }

    private User createContactUser(Tenant tenant) {
        return User.builder()
            .id("user-123")
            .tenant(tenant)
            .email("admin@test.com")
            .username("testadmin")
            .firstName("Test")
            .lastName("Admin")
            .status(User.UserStatus.INACTIVE)
            .roles(Set.of(tenantAdminRole))
            .keycloakId("keycloak-123")
            .build();
    }

    private Shop createInactiveShop(Tenant tenant) {
        return Shop.builder()
            .id("shop-123")
            .tenant(tenant)
            .name("Test Shop")
            .status(Shop.ShopStatus.INACTIVE)
            .build();
    }

    private TenantRegistrationRequest createValidRegistrationRequest() {
        TenantInfoRequest tenantInfo = new TenantInfoRequest(
            "Test Tenant",
            "Test Description for tenant registration",
            "tenant@test.com",
            "123 Main St",
            null, // city
            null, // state
            null, // country
            null, // postalCode
            null, // companyRegistration
            null, // taxId
            "555-0123"
        );

        ContactUserRequest contactUser = new ContactUserRequest(
            "testadmin",
            "admin@test.com",
            "Test",
            "Admin",
            "555-0124",
            "123 Admin St",
            null, // city
            null, // state
            null, // country
            null  // postalCode
        );

        ShopInfoRequest shop = new ShopInfoRequest(
            "shop-1",
            "Test Shop",
            "Test Shop Description",
            "123 Shop St",
            null, // city
            null, // state
            null, // country
            null, // postalCode
            "555-0125",
            "shop@test.com",
            null  // taxId
        );

        return TenantRegistrationRequest.builder()
            .tenantInfo(tenantInfo)
            .contactUser(contactUser)
            .shops(List.of(shop))
            .termsAccepted(true)
            .privacyPolicyAccepted(true)
            .agreementVersion("1.0")
            .build();
    }
}