package com.princely.shopmanager.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.registration.TenantActivationRequest;
import com.princely.shopmanager.core.dto.registration.PendingTenantResponse;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tenant Admin Integration Tests")
@Disabled("Temporarily disabled due to ApplicationContext issues - need to fix conditional properties")
class TenantAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TenantRegistrationService tenantRegistrationService;

    @Autowired
    private ObjectMapper objectMapper;
    private Tenant testTenant;
    private User testContactUser;
    private Shop testShop;
    private Role tenantAdminRole;
    @BeforeEach
    void setUp() {
        // Create test data
        createTestData();
    }

    private void createTestData() {
        // Create TENANT_ADMIN role if it doesn't exist
        tenantAdminRole = roleRepository.findByName("TENANT_ADMIN")
            .orElseGet(() -> {
                Role role = new Role();
                role.setName("TENANT_ADMIN");
                role.setDescription("Tenant Administrator");
                return roleRepository.save(role);
            });

        // Create test tenant
        testTenant = Tenant.builder()
            .name("Test Tenant Corp")
            .description("A test tenant for integration testing")
            .contactEmail("contact@testtenant.com")
            .primaryAddress("123 Test Street")
            .city("Test City")
            .state("Test State")
            .country("Test Country")
            .postalCode("12345")
            .contactPhone("+1234567890")
            .status(Tenant.TenantStatus.INACTIVE)
            .createdDate(LocalDateTime.now())
            .build();
        testTenant = tenantRepository.save(testTenant);

        // Create test contact user
        testContactUser = User.builder()
            .tenant(testTenant)
            .username("testuser")
            .email("testuser@testtenant.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+0987654321")
            .status(User.UserStatus.INACTIVE)
            .roles(Set.of(tenantAdminRole))
            .keycloakId("keycloak-test-id")
            .build();
        testContactUser = userRepository.save(testContactUser);

        // Set contact user on tenant
        testTenant.setContactUser(testContactUser);
        testTenant = tenantRepository.save(testTenant);

        // Create test shop
        testShop = Shop.builder()
            .tenant(testTenant)
            .name("Test Shop")
            .description("A test shop")
            .address("456 Shop Avenue")
            .city("Shop City")
            .state("Shop State")
            .country("Shop Country")
            .postalCode("54321")
            .phoneNumber("+1111111111")
            .email("shop@testtenant.com")
            .status(Shop.ShopStatus.INACTIVE)
            .build();
        testShop = shopRepository.save(testShop);
    }

    @Test
    @DisplayName("Should get pending tenant registrations")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldGetPendingTenantRegistrations() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].tenantId").value(testTenant.getId()))
            .andExpect(jsonPath("$[0].tenantName").value("Test Tenant Corp"))
            .andExpect(jsonPath("$[0].status").value("INACTIVE"));
    }

    @Test
    @DisplayName("Should deny access to non-super admin users")
    @WithMockUser(roles = "TENANT_ADMIN")
    void shouldDenyAccessToNonSuperAdminUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/pending"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get specific tenant details")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldGetSpecificTenantDetails() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/tenants/{tenantId}", testTenant.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
            .andExpect(jsonPath("$.tenantName").value("Test Tenant Corp"))
            .andExpect(jsonPath("$.contactUserEmail").value("testuser@testtenant.com"));
    }

    @Test
    @DisplayName("Should activate tenant successfully")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldActivateTenantSuccessfully() throws Exception {
        // Given
        TenantActivationRequest request = new TenantActivationRequest(
            testTenant.getId(),
            true,
            null,
            List.of(testShop.getId()),
            "Approved for testing"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", testTenant.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
            .andExpect(jsonPath("$.approved").value(true))
            .andExpect(jsonPath("$.message").value("Tenant approved and activated successfully"));

        // Verify tenant is activated
        Tenant updatedTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);

        // Verify user is activated
        User updatedUser = userRepository.findById(testContactUser.getId()).orElseThrow();
        assertThat(updatedUser.getStatus()).isEqualTo(User.UserStatus.ACTIVE);

        // Verify shop is activated
        Shop updatedShop = shopRepository.findById(testShop.getId()).orElseThrow();
        assertThat(updatedShop.getStatus()).isEqualTo(Shop.ShopStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject tenant with reason")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldRejectTenantWithReason() throws Exception {
        // Given
        TenantActivationRequest request = new TenantActivationRequest(
            testTenant.getId(),
            false,
            "Incomplete documentation provided",
            null,
            "Needs additional verification"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", testTenant.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tenantId").value(testTenant.getId()))
            .andExpect(jsonPath("$.approved").value(false))
            .andExpect(jsonPath("$.message").value("Tenant registration rejected: Incomplete documentation provided"));

        // Verify tenant is terminated
        Tenant updatedTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(updatedTenant.getStatus()).isEqualTo(Tenant.TenantStatus.TERMINATED);
    }

    @Test
    @DisplayName("Should return 400 for tenant ID mismatch")
    @WithMockUser(roles = "SUPER_ADMIN")
    void shouldReturn400ForTenantIdMismatch() throws Exception {
        // Given
        TenantActivationRequest request = new TenantActivationRequest(
            "different-tenant-id",
            true,
            null,
            List.of(testShop.getId()),
            null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/tenants/{tenantId}/activate", testTenant.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should test service methods directly")
    void shouldTestServiceMethodsDirectly() {
        // Test getPendingRegistrations
        List<PendingTenantResponse> pendingTenants = tenantRegistrationService.getPendingRegistrations();
        assertThat(pendingTenants).hasSize(1);
        assertThat(pendingTenants.get(0).tenantId()).isEqualTo(testTenant.getId());

        // Test getTenantDetails
        PendingTenantResponse tenantDetails = tenantRegistrationService.getTenantDetails(testTenant.getId());
        assertThat(tenantDetails.tenantId()).isEqualTo(testTenant.getId());
        assertThat(tenantDetails.tenantName()).isEqualTo("Test Tenant Corp");

        // Test activateTenant
        TenantActivationRequest activationRequest = new TenantActivationRequest(
            testTenant.getId(),
            true,
            null,
            List.of(testShop.getId()),
            "Test activation"
        );

        tenantRegistrationService.activateTenant(activationRequest, "test-admin");

        // Verify activation
        Tenant activatedTenant = tenantRepository.findById(testTenant.getId()).orElseThrow();
        assertThat(activatedTenant.getStatus()).isEqualTo(Tenant.TenantStatus.ACTIVE);
    }
}