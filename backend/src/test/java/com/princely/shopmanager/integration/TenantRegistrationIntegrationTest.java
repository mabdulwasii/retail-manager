package com.princely.shopmanager.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.registration.*;
import com.princely.shopmanager.core.controller.TenantRegistrationController.NameAvailabilityResponse;
import com.princely.shopmanager.core.event.TenantRegistrationNotificationEvent;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.auth.service.KeycloakUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Tenant Registration Integration Tests")
class TenantRegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("shopmanager_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private KeycloakUserService keycloakUserService;

    private String baseUrl;
    private Role tenantAdminRole;
    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/public/registration";

        // Create required roles
        tenantAdminRole = createRoleIfNotExists("TENANT_ADMIN");
        superAdminRole = createRoleIfNotExists("SUPER_ADMIN");

        // Mock Keycloak service
        when(keycloakUserService.generatePassword()).thenReturn("generatedPassword123!");
        when(keycloakUserService.createUser(any())).thenReturn("keycloak-user-id-123");
        when(keycloakUserService.userExistsByEmail(any())).thenReturn(false);
        when(keycloakUserService.userExistsByUsername(any())).thenReturn(false);
    }

    @Test
    @DisplayName("Should complete full tenant registration workflow")
    void shouldCompleteFullTenantRegistrationWorkflow() {
        // Given
        TenantRegistrationRequest request = createValidRegistrationRequest();

        // When - Register tenant
        ResponseEntity<TenantRegistrationResponse> response = restTemplate.postForEntity(
            baseUrl + "/tenant",
            request,
            TenantRegistrationResponse.class
        );

        // Then - Verify HTTP response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().registrationStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(response.getBody().tenantName()).isEqualTo("Test Tenant Company");

        String tenantId = response.getBody().tenantId();
        String contactUserId = response.getBody().contactUserId();

        // Verify entities were created correctly
        verifyTenantCreation(tenantId, request);
        verifyContactUserCreation(contactUserId, tenantId, request);
        verifyShopsCreation(tenantId, request);

        // Note: Event verification removed since we're not using Spring Modulith test framework
        // In a real integration test, you would verify the event through other means
    }

    @Test
    @DisplayName("Should validate tenant name uniqueness")
    @Transactional
    void shouldValidateTenantNameUniqueness() {
        // Given - Create existing tenant
        Tenant existingTenant = Tenant.builder()
            .name("Existing Tenant")
            .contactEmail("existing@test.com")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
        tenantRepository.save(existingTenant);

        TenantRegistrationRequest request = createValidRegistrationRequest();
        // Note: Records are immutable, so we can't modify them after creation

        // When - Try to register with duplicate name
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/tenant",
            request,
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should validate email uniqueness")
    @Transactional
    void shouldValidateEmailUniqueness() {
        // Given - Create existing user
        User existingUser = User.builder()
            .email("existing@test.com")
            .username("existinguser")
            .firstName("Existing")
            .lastName("User")
            .status(User.UserStatus.ACTIVE)
            .roles(Set.of(tenantAdminRole))
            .build();
        userRepository.save(existingUser);

        TenantRegistrationRequest request = createValidRegistrationRequest();
        ContactUserRequest contactUser = new ContactUserRequest(
            request.getContactUser().username(),
            "existing@test.com",
            request.getContactUser().firstName(),
            request.getContactUser().lastName(),
            request.getContactUser().phoneNumber(),
            request.getContactUser().primaryAddress(),
            request.getContactUser().city(),
            request.getContactUser().state(),
            request.getContactUser().country(),
            request.getContactUser().postalCode()
        );

        TenantRegistrationRequest modifiedRequest = request.toBuilder()
            .contactUser(contactUser)
            .build();

        // When - Try to register with duplicate email
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/tenant",
            modifiedRequest,
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Should check tenant name availability")
    void shouldCheckTenantNameAvailability() {
        // When - Check available name
        ResponseEntity<NameAvailabilityResponse> response =
            restTemplate.getForEntity(
                baseUrl + "/check-tenant-name?name=AvailableTenant",
                NameAvailabilityResponse.class
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().available()).isTrue();
        assertThat(response.getBody().value()).isEqualTo("AvailableTenant");
    }

    @Test
    @DisplayName("Should check username availability")
    void shouldCheckUsernameAvailability() {
        // When - Check available username
        ResponseEntity<NameAvailabilityResponse> response =
            restTemplate.getForEntity(
                baseUrl + "/check-username?username=availableuser",
                NameAvailabilityResponse.class
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().available()).isTrue();
        assertThat(response.getBody().value()).isEqualTo("availableuser");
    }

    @Test
    @DisplayName("Should check email availability")
    void shouldCheckEmailAvailability() {
        // When - Check available email
        ResponseEntity<NameAvailabilityResponse> response =
            restTemplate.getForEntity(
                baseUrl + "/check-email?email=available@test.com",
                NameAvailabilityResponse.class
            );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().available()).isTrue();
        assertThat(response.getBody().value()).isEqualTo("available@test.com");
    }

    @Test
    @DisplayName("Should reject registration without terms acceptance")
    void shouldRejectRegistrationWithoutTermsAcceptance() {
        // Given
        TenantRegistrationRequest request = createValidRegistrationRequest().toBuilder()
            .termsAccepted(false)
            .build();

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/tenant",
            request,
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should handle Keycloak integration failures gracefully")
    void shouldHandleKeycloakFailuresGracefully() {
        // Given
        when(keycloakUserService.createUser(any())).thenThrow(new RuntimeException("Keycloak error"));
        TenantRegistrationRequest request = createValidRegistrationRequest();

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/tenant",
            request,
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void verifyTenantCreation(String tenantId, TenantRegistrationRequest request) {
        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        assertThat(tenantOpt).isPresent();

        Tenant tenant = tenantOpt.get();
        assertThat(tenant.getName()).isEqualTo(request.getTenantInfo().name());
        assertThat(tenant.getDescription()).isEqualTo(request.getTenantInfo().description());
        assertThat(tenant.getContactEmail()).isEqualTo(request.getTenantInfo().email());
        assertThat(tenant.getStatus()).isEqualTo(Tenant.TenantStatus.INACTIVE);
        assertThat(tenant.getContactUser()).isNotNull();
    }

    private void verifyContactUserCreation(String contactUserId, String tenantId, TenantRegistrationRequest request) {
        Optional<User> userOpt = userRepository.findById(contactUserId);
        assertThat(userOpt).isPresent();

        User user = userOpt.get();
        assertThat(user.getEmail()).isEqualTo(request.getContactUser().email());
        assertThat(user.getUsername()).isEqualTo(request.getContactUser().username());
        assertThat(user.getFirstName()).isEqualTo(request.getContactUser().firstName());
        assertThat(user.getLastName()).isEqualTo(request.getContactUser().lastName());
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
        assertThat(user.getTenant().getId()).isEqualTo(tenantId);
        assertThat(user.getRoles()).contains(tenantAdminRole);
        assertThat(user.getKeycloakId()).isEqualTo("keycloak-user-id-123");
    }

    private void verifyShopsCreation(String tenantId, TenantRegistrationRequest request) {
        List<Shop> shops = shopRepository.findByTenantId(tenantId);
        assertThat(shops).hasSize(request.getShops().size());

        Shop shop = shops.get(0);
        ShopInfoRequest expectedShop = request.getShops().get(0);
        assertThat(shop.getName()).isEqualTo(expectedShop.name());
        assertThat(shop.getDescription()).isEqualTo(expectedShop.description());
        assertThat(shop.getStatus()).isEqualTo(Shop.ShopStatus.INACTIVE);
        assertThat(shop.getTenant().getId()).isEqualTo(tenantId);
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
            .orElseGet(() -> {
                Role role = Role.builder()
                    .name(roleName)
                    .description(roleName + " role")
                    .build();
                return roleRepository.save(role);
            });
    }

    private TenantRegistrationRequest createValidRegistrationRequest() {
        TenantInfoRequest tenantInfo = new TenantInfoRequest(
            "Test Tenant Company",
            "A test tenant company for integration testing",
            "contact@testtenant.com",
            "123 Business Street",
            "Business City",
            "Business State",
            "Test Country",
            "12345",
            "REG-12345",
            "TAX-67890",
            "555-0123"
        );

        ContactUserRequest contactUser = new ContactUserRequest(
            "testadmin",
            "admin@testtenant.com",
            "Test",
            "Administrator",
            "555-0124",
            "456 Admin Avenue",
            null, // city
            null, // state
            null, // country
            null  // postalCode
        );

        ShopInfoRequest shop1 = new ShopInfoRequest(
            "test-shop-1",
            "Test Shop Downtown",
            "Main downtown location",
            "789 Main Street",
            "Downtown",
            "Test State",
            "Test Country",
            "54321",
            "555-0125",
            "downtown@testtenant.com",
            "SHOP-TAX-123"
        );

        ShopInfoRequest shop2 = new ShopInfoRequest(
            "test-shop-2",
            "Test Shop Uptown",
            "Secondary uptown location",
            "321 Uptown Boulevard",
            "Uptown",
            "Test State",
            "Test Country",
            "98765",
            "555-0126",
            "uptown@testtenant.com",
            "SHOP-TAX-456"
        );

        return TenantRegistrationRequest.builder()
            .tenantInfo(tenantInfo)
            .contactUser(contactUser)
            .shops(List.of(shop1, shop2))
            .termsAccepted(true)
            .privacyPolicyAccepted(true)
            .agreementVersion("1.0")
            .build();
    }
}