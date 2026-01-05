package com.princely.shopmanager.aggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.aggregator.domain.CloudShop;
import com.princely.shopmanager.aggregator.domain.CloudTenant;
import com.princely.shopmanager.aggregator.dto.ShopLinkRequest;
import com.princely.shopmanager.aggregator.dto.ShopRegistrationDto;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationRequest;
import com.princely.shopmanager.aggregator.dto.TenantRegistrationResponse;
import com.princely.shopmanager.aggregator.repository.CloudShopRepository;
import com.princely.shopmanager.aggregator.repository.CloudTenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AggregatorController.
 * Tests cloud aggregator API endpoints for tenant registration and management.
 * These endpoints are PUBLIC (no JWT required) and use API key authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Cloud Aggregator API - Integration Tests")
class AggregatorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CloudTenantRepository cloudTenantRepository;

    @Autowired
    private CloudShopRepository cloudShopRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AGGREGATOR_BASE_URL = "/api/registration";
    private static final String TEST_API_KEY = "rhq_test_api_key_123456789";

    private CloudTenant existingCloudTenant;
    private String apiKeyHash;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        cloudShopRepository.deleteAll();
        cloudTenantRepository.deleteAll();

        // Create existing cloud tenant for testing shop link/unregister
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        apiKeyHash = passwordEncoder.encode(TEST_API_KEY);

        existingCloudTenant = CloudTenant.builder()
                .tenantName("Existing Tenant " + uniqueSuffix)
                .tenantEmail("existing" + uniqueSuffix + "@test.com")
                .companyRegistration("REG" + uniqueSuffix)
                .taxId("TAX" + uniqueSuffix)
                .address("123 Existing St")
                .city("Test City")
                .country("Test Country")
                .phoneNumber("123-456-7890")
                .apiKeyHash(apiKeyHash)
                .status(CloudTenant.Status.ACTIVE)
                .shopCount(1)
                .subscriptionTier(CloudTenant.SubscriptionTier.FREE)
                .build();
        existingCloudTenant = cloudTenantRepository.save(existingCloudTenant);

        // Create a shop for the existing tenant
        CloudShop existingShop = CloudShop.builder()
                .cloudTenant(existingCloudTenant)
                .shopName("Existing Shop")
                .shopEmail("shop@test.com")
                .address("456 Shop St")
                .city("Test City")
                .status(CloudShop.Status.ACTIVE)
                .build();
        cloudShopRepository.save(existingShop);
    }

    @Test
    @DisplayName("Health check should return OK")
    void healthCheck_Success() throws Exception {
        mockMvc.perform(get(AGGREGATOR_BASE_URL + "/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("operational")));
    }

    @Test
    @DisplayName("Register tenant with valid data should succeed")
    void registerTenant_ValidRequest_Success() throws Exception {
        // Arrange
        String uniqueEmail = "newretail" + System.currentTimeMillis() + "@test.com";

        ShopRegistrationDto shop1 = ShopRegistrationDto.builder()
                .shopName("Main Shop")
                .shopEmail("main@test.com")
                .address("123 Main St")
                .city("City A")
                .country("Country A")
                .phoneNumber("111-222-3333")
                .build();

        ShopRegistrationDto shop2 = ShopRegistrationDto.builder()
                .shopName("Branch Shop")
                .shopEmail("branch@test.com")
                .build();

        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .tenantName("New Retail Business")
                .tenantEmail(uniqueEmail)
                .companyRegistration("REG12345")
                .taxId("TAX67890")
                .address("789 Business Ave")
                .city("Business City")
                .country("Business Country")
                .phoneNumber("555-123-4567")
                .shops(Arrays.asList(shop1, shop2))
                .build();

        // Act & Assert
        MvcResult result = mockMvc.perform(post(AGGREGATOR_BASE_URL + "/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cloudTenantId").exists())
                .andExpect(jsonPath("$.cloudTenantId").isNotEmpty())
                .andExpect(jsonPath("$.apiKey").exists())
                .andExpect(jsonPath("$.apiKey").isNotEmpty())
                .andExpect(jsonPath("$.apiKey").value(startsWith("rhq_")))
                .andExpect(jsonPath("$.registeredShopsCount").value(2))
                .andExpect(jsonPath("$.message").value(containsString("successfully registered")))
                .andReturn();

        // Verify database state
        String responseBody = result.getResponse().getContentAsString();
        TenantRegistrationResponse response = objectMapper.readValue(responseBody, TenantRegistrationResponse.class);

        CloudTenant savedTenant = cloudTenantRepository.findById(response.getCloudTenantId()).orElse(null);
        assertThat(savedTenant).isNotNull();
        assertThat(savedTenant.getTenantName()).isEqualTo("New Retail Business");
        assertThat(savedTenant.getTenantEmail()).isEqualTo(uniqueEmail);
        assertThat(savedTenant.getShopCount()).isEqualTo(2);
        assertThat(savedTenant.getStatus()).isEqualTo(CloudTenant.Status.ACTIVE);
        assertThat(savedTenant.getSubscriptionTier()).isEqualTo(CloudTenant.SubscriptionTier.FREE);

        List<CloudShop> shops = cloudShopRepository.findByCloudTenantId(response.getCloudTenantId());
        assertThat(shops).hasSize(2);
        assertThat(shops).extracting(CloudShop::getShopName)
                .containsExactlyInAnyOrder("Main Shop", "Branch Shop");
    }

    @Test
    @DisplayName("Register tenant with duplicate email should fail")
    void registerTenant_DuplicateEmail_Fail() throws Exception {
        // Arrange
        ShopRegistrationDto shop = ShopRegistrationDto.builder()
                .shopName("Test Shop")
                .build();

        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .tenantName("Duplicate Tenant")
                .tenantEmail(existingCloudTenant.getTenantEmail()) // Duplicate
                .shops(Collections.singletonList(shop))
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already registered")));
    }

    @Test
    @DisplayName("Register tenant with no shops should fail validation")
    void registerTenant_NoShops_Fail() throws Exception {
        // Arrange
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .tenantName("No Shops Tenant")
                .tenantEmail("noshops@test.com")
                .shops(Collections.emptyList()) // Empty shops list
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("At least one shop")));
    }

    @Test
    @DisplayName("Register tenant with missing required fields should fail validation")
    void registerTenant_MissingRequiredFields_Fail() throws Exception {
        // Arrange
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .tenantEmail("incomplete@test.com") // Missing tenantName and shops
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/tenants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Link shop with valid API key should succeed")
    void linkShop_ValidApiKey_Success() throws Exception {
        // Arrange
        ShopRegistrationDto newShop = ShopRegistrationDto.builder()
                .shopName("New Linked Shop")
                .shopEmail("linked@test.com")
                .address("789 New St")
                .city("Linked City")
                .country("Linked Country")
                .phoneNumber("999-888-7777")
                .build();

        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(existingCloudTenant.getId())
                .shop(newShop)
                .build();

        int initialShopCount = existingCloudTenant.getShopCount();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/shops")
                        .header("X-API-Key", TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Verify shop was created
        List<CloudShop> shops = cloudShopRepository.findByCloudTenantId(existingCloudTenant.getId());
        assertThat(shops).hasSize(initialShopCount + 1);
        assertThat(shops).extracting(CloudShop::getShopName)
                .contains("New Linked Shop");

        // Verify shop count was incremented
        CloudTenant updatedTenant = cloudTenantRepository.findById(existingCloudTenant.getId()).orElseThrow();
        assertThat(updatedTenant.getShopCount()).isEqualTo(initialShopCount + 1);
    }

    @Test
    @DisplayName("Link shop with invalid API key should fail")
    void linkShop_InvalidApiKey_Fail() throws Exception {
        // Arrange
        ShopRegistrationDto newShop = ShopRegistrationDto.builder()
                .shopName("Unauthorized Shop")
                .build();

        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(existingCloudTenant.getId())
                .shop(newShop)
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/shops")
                        .header("X-API-Key", "wrong_api_key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid API key")));
    }

    @Test
    @DisplayName("Link shop without API key should fail")
    void linkShop_MissingApiKey_Fail() throws Exception {
        // Arrange
        ShopRegistrationDto newShop = ShopRegistrationDto.builder()
                .shopName("No Auth Shop")
                .build();

        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId(existingCloudTenant.getId())
                .shop(newShop)
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/shops")
                        // No X-API-Key header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("API key is required")));
    }

    @Test
    @DisplayName("Link shop to nonexistent tenant should fail")
    void linkShop_NonexistentTenant_Fail() throws Exception {
        // Arrange
        ShopRegistrationDto newShop = ShopRegistrationDto.builder()
                .shopName("Orphan Shop")
                .build();

        ShopLinkRequest request = ShopLinkRequest.builder()
                .cloudTenantId("nonexistent-tenant-id")
                .shop(newShop)
                .build();

        // Act & Assert
        mockMvc.perform(post(AGGREGATOR_BASE_URL + "/shops")
                        .header("X-API-Key", TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Cloud tenant not found")));
    }

    @Test
    @DisplayName("Unregister tenant with valid API key should succeed")
    void unregisterTenant_ValidApiKey_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(AGGREGATOR_BASE_URL + "/tenants/" + existingCloudTenant.getId())
                        .header("X-API-Key", TEST_API_KEY))
                .andExpect(status().isOk());

        // Verify tenant and shops were deleted
        assertThat(cloudTenantRepository.findById(existingCloudTenant.getId())).isEmpty();
        assertThat(cloudShopRepository.findByCloudTenantId(existingCloudTenant.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unregister tenant with invalid API key should fail")
    void unregisterTenant_InvalidApiKey_Fail() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(AGGREGATOR_BASE_URL + "/tenants/" + existingCloudTenant.getId())
                        .header("X-API-Key", "wrong_key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid API key")));

        // Verify tenant still exists
        assertThat(cloudTenantRepository.findById(existingCloudTenant.getId())).isPresent();
    }

    @Test
    @DisplayName("Unregister nonexistent tenant should fail")
    void unregisterTenant_NonexistentTenant_Fail() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(AGGREGATOR_BASE_URL + "/tenants/nonexistent-id")
                        .header("X-API-Key", TEST_API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("Cloud tenant not found")));
    }
}
