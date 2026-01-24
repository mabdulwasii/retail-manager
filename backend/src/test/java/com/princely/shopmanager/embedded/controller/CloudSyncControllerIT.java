package com.princely.shopmanager.embedded.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.repository.CloudSyncConfigRepository;
import com.princely.shopmanager.embedded.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CloudSyncController.
 * Tests cloud sync management endpoints with real Spring Boot context in embedded mode.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("embedded")
@TestPropertySource(properties = {
    "app.keycloak.enabled=false",
    "app.update-check.enabled=true",
    "application.encryption.secret=test-encryption-secret-key-32chars",
    "application.encryption.salt=0123456789abcdef",
    "embedded.postgres.data-dir=./target/test-postgres-cloud-sync",
    "embedded.postgres.port=5434"
})
@DisplayName("Cloud Sync Controller - Integration Tests")
class CloudSyncControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CloudSyncConfigRepository cloudSyncConfigRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.security.crypto.encrypt.TextEncryptor textEncryptor;

    private static final String CLOUD_SYNC_BASE_URL = "/api/cloud-sync";

    private Tenant testTenant;
    private Shop testShop;
    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUp() {
        // Use UUID to ensure unique identifiers across test runs
        String uniqueSuffix = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // Create test tenant
        testTenant = new Tenant();
        testTenant.setName("Test Tenant " + uniqueSuffix);
        testTenant.setContactEmail("tenant" + uniqueSuffix + "@test.com");
        testTenant.setPrimaryAddress("123 Test Street");
        testTenant.setCity("Test City");
        testTenant.setState("TS");
        testTenant.setCountry("Test Country");
        testTenant = tenantRepository.save(testTenant);

        // Create test shop
        testShop = new Shop();
        testShop.setName("Test Shop " + uniqueSuffix);
        testShop.setTenant(testTenant);
        testShop.setEmail("shop" + uniqueSuffix + "@test.com");
        testShop.setAddress("456 Shop Street");
        testShop.setPhoneNumber("+1234567890");
        testShop = shopRepository.save(testShop);

        // Get TENANT_ADMIN role
        Role tenantAdminRole = roleRepository.findByName("TENANT_ADMIN")
                .orElseThrow(() -> new IllegalStateException("TENANT_ADMIN role not found"));

        // Create admin user
        String adminEmail = "admin" + uniqueSuffix + "@test.com";
        adminUser = User.builder()
                .username(adminEmail)
                .email(adminEmail)
                .firstName("Admin")
                .lastName("User")
                .phoneNumber("+1234567890")
                .passwordHash(passwordEncoder.encode("password"))
                .tenant(testTenant)
                .shop(testShop)
                .roles(Set.of(tenantAdminRole))
                .status(User.UserStatus.ACTIVE)
                .build();
        adminUser = userRepository.save(adminUser);

        // Generate JWT token
        adminToken = jwtTokenProvider.generateAccessToken(adminUser);
    }

    // ============================================================================
    // GET /api/cloud-sync/config Tests
    // ============================================================================

    @Test
    @DisplayName("GET /config - Should return not configured when no config exists")
    void shouldReturnNotConfiguredWhenNoConfig() throws Exception {
        mockMvc.perform(get(CLOUD_SYNC_BASE_URL + "/config")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false));
    }

    @Test
    @DisplayName("GET /config - Should return config when exists")
    void shouldReturnConfigWhenExists() throws Exception {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(testTenant.getId())
                .cloudTenantId("cloud-tenant-123")
                .cloudApiKey(textEncryptor.encrypt("test-api-key"))
                .cloudApiUrl("https://cloud.test.com")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
        cloudSyncConfigRepository.saveAndFlush(config);

        // When/Then
        mockMvc.perform(get(CLOUD_SYNC_BASE_URL + "/config")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.cloudApiUrl").value("https://cloud.test.com"))
                .andExpect(jsonPath("$.syncEnabled").value(true));
    }

    @Test
    @DisplayName("GET /config - Should return 403 when no auth token")
    void shouldReturn403WhenNoAuthToken() throws Exception {
        mockMvc.perform(get(CLOUD_SYNC_BASE_URL + "/config"))
                .andExpect(status().isForbidden());
    }

    // ============================================================================
    // GET /api/cloud-sync/status Tests
    // ============================================================================

    @Test
    @DisplayName("GET /status - Should return status when not configured")
    void shouldReturnStatusWhenNotConfigured() throws Exception {
        mockMvc.perform(get(CLOUD_SYNC_BASE_URL + "/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.status").value("NOT_CONFIGURED"));
    }

    @Test
    @DisplayName("GET /status - Should return status when configured but disabled")
    void shouldReturnStatusWhenConfiguredButDisabled() throws Exception {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(testTenant.getId())
                .cloudTenantId("cloud-tenant-123")
                .cloudApiKey(textEncryptor.encrypt("test-api-key"))
                .cloudApiUrl("https://cloud.test.com")
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
        cloudSyncConfigRepository.saveAndFlush(config);

        // When/Then
        mockMvc.perform(get(CLOUD_SYNC_BASE_URL + "/status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.cloudApiUrl").value("https://cloud.test.com"));
    }

    // ============================================================================
    // PUT /api/cloud-sync/config Tests
    // ============================================================================

    @Test
    @DisplayName("PUT /config - Should update cloud API URL")
    void shouldUpdateCloudApiUrl() throws Exception {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(testTenant.getId())
                .cloudTenantId("cloud-tenant-123")
                .cloudApiKey(textEncryptor.encrypt("test-api-key"))
                .cloudApiUrl("https://cloud-old.test.com")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
        cloudSyncConfigRepository.saveAndFlush(config);

        CloudSyncController.CloudSyncUpdateRequest updateRequest =
                new CloudSyncController.CloudSyncUpdateRequest("https://cloud-new.test.com");

        // When/Then
        mockMvc.perform(put(CLOUD_SYNC_BASE_URL + "/config")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cloudApiUrl").value("https://cloud-new.test.com"));
    }

    @Test
    @DisplayName("PUT /config - Should return 428 when cloud sync not configured")
    void shouldReturn428WhenUpdatingNonExistentConfig() throws Exception {
        CloudSyncController.CloudSyncUpdateRequest updateRequest =
                new CloudSyncController.CloudSyncUpdateRequest("https://cloud.test.com");

        mockMvc.perform(put(CLOUD_SYNC_BASE_URL + "/config")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isPreconditionRequired());
    }

    // ============================================================================
    // POST /api/cloud-sync/enable Tests
    // ============================================================================

    @Test
    @DisplayName("POST /enable - Should enable cloud sync")
    void shouldEnableCloudSync() throws Exception {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(testTenant.getId())
                .cloudTenantId("cloud-tenant-123")
                .cloudApiKey(textEncryptor.encrypt("test-api-key"))
                .cloudApiUrl("https://cloud.test.com")
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
        cloudSyncConfigRepository.saveAndFlush(config);

        // When/Then
        mockMvc.perform(post(CLOUD_SYNC_BASE_URL + "/enable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncEnabled").value(true));
    }

    @Test
    @DisplayName("POST /enable - Should return 428 when cloud sync not configured")
    void shouldReturn428WhenEnablingNonConfigured() throws Exception {
        mockMvc.perform(post(CLOUD_SYNC_BASE_URL + "/enable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isPreconditionRequired());
    }

    // ============================================================================
    // POST /api/cloud-sync/disable Tests
    // ============================================================================

    @Test
    @DisplayName("POST /disable - Should disable cloud sync")
    void shouldDisableCloudSync() throws Exception {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(testTenant.getId())
                .cloudTenantId("cloud-tenant-123")
                .cloudApiKey(textEncryptor.encrypt("test-api-key"))
                .cloudApiUrl("https://cloud.test.com")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
        cloudSyncConfigRepository.saveAndFlush(config);

        // When/Then
        mockMvc.perform(post(CLOUD_SYNC_BASE_URL + "/disable")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncEnabled").value(false));
    }

    // ============================================================================
    // POST /api/cloud-sync/sync Tests
    // ============================================================================

    @Test
    @DisplayName("POST /sync - Should trigger manual sync")
    void shouldTriggerManualSync() throws Exception {
        mockMvc.perform(post(CLOUD_SYNC_BASE_URL + "/sync")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true))
                .andExpect(jsonPath("$.message", containsString("Manual sync started")));
    }
}
