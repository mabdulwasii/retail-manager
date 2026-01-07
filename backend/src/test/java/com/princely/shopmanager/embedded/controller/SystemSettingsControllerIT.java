package com.princely.shopmanager.embedded.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.repository.PermissionRepository;
import com.princely.shopmanager.core.repository.RoleRepository;
import com.princely.shopmanager.core.repository.UserRepository;
import com.princely.shopmanager.embedded.domain.SystemSettings;
import com.princely.shopmanager.embedded.dto.BulkUpdateSettingsRequest;
import com.princely.shopmanager.embedded.dto.UpdateSettingRequest;
import com.princely.shopmanager.embedded.repository.SystemSettingsRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SystemSettingsController.
 * Tests system settings endpoints with real Spring Boot context in embedded mode.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("embedded")
@Transactional
@DisplayName("System Settings Controller - Integration Tests")
class SystemSettingsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemSettingsRepository settingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String SETTINGS_BASE_URL = "/api/settings";
    private String adminToken;
    private String userToken;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        // Clean up
        settingsRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create permissions
        Permission viewPermission = createPermission("SYSTEM_SETTING_VIEW", "View system settings");
        Permission updatePermission = createPermission("SYSTEM_SETTING_UPDATE", "Update system settings");
        Permission managePermission = createPermission("SYSTEM_SETTING_MANAGE", "Manage all system settings");

        // Create admin role with all permissions
        Role adminRole = createRole("SYSTEM_ADMIN", Set.of(viewPermission, updatePermission, managePermission));

        // Create normal role with no permissions
        Role userRole = createRole("EMPLOYEE", Set.of());

        // Create admin user
        adminUser = createUser("admin", "admin@test.com", "password123", Set.of(adminRole));
        adminToken = jwtTokenProvider.generateAccessToken(adminUser);

        // Create normal user
        normalUser = createUser("user", "user@test.com", "password123", Set.of(userRole));
        userToken = jwtTokenProvider.generateAccessToken(normalUser);

        // Create test settings
        createTestSettings();
    }

    // ============================================================================
    // GET /api/settings/grouped Tests
    // ============================================================================

    @Test
    @DisplayName("GET /api/settings/grouped - Should return 200 with grouped settings for admin")
    void shouldGetGroupedSettingsForAdmin() throws Exception {
        mockMvc.perform(get(SETTINGS_BASE_URL + "/grouped")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DOMAIN").isArray())
                .andExpect(jsonPath("$.SYNC").isArray())
                .andExpect(jsonPath("$.DOMAIN[0].key").exists())
                .andExpect(jsonPath("$.DOMAIN[0].value").exists())
                .andExpect(jsonPath("$.DOMAIN[0].category").value("DOMAIN"));
    }

    @Test
    @DisplayName("GET /api/settings/grouped - Should return 403 for user without permissions")
    void shouldReturn403ForUnauthorizedUser() throws Exception {
        mockMvc.perform(get(SETTINGS_BASE_URL + "/grouped")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/settings/grouped - Should return 401 without token")
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get(SETTINGS_BASE_URL + "/grouped"))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================================
    // PUT /api/settings/{key} Tests
    // ============================================================================

    @Test
    @DisplayName("PUT /api/settings/{key} - Should update setting successfully")
    void shouldUpdateSettingSuccessfully() throws Exception {
        // Given
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setValue("new-domain.local");

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/custom.domain")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setting.key").value("custom.domain"))
                .andExpect(jsonPath("$.setting.value").value("new-domain.local"))
                .andExpect(jsonPath("$.requiresRestart").isBoolean())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/settings/{key} - Should return 400 for invalid number value")
    void shouldReturn400ForInvalidNumberValue() throws Exception {
        // Given
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setValue("invalid-number");

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/max.upload.size")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/settings/{key} - Should return 400 for invalid boolean value")
    void shouldReturn400ForInvalidBooleanValue() throws Exception {
        // Given
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setValue("yes");  // Invalid boolean

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/cloud.sync.enabled")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/settings/{key} - Should return 404 for non-existent key")
    void shouldReturn404ForNonExistentKey() throws Exception {
        // Given
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setValue("value");

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/nonexistent.key")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/settings/{key} - Should return 403 without UPDATE permission")
    void shouldReturn403WithoutUpdatePermission() throws Exception {
        // Given
        UpdateSettingRequest request = new UpdateSettingRequest();
        request.setValue("new-value");

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/custom.domain")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ============================================================================
    // PUT /api/settings/bulk Tests
    // ============================================================================

    @Test
    @DisplayName("PUT /api/settings/bulk - Should update multiple settings")
    void shouldUpdateMultipleSettings() throws Exception {
        // Given
        Map<String, String> updates = new HashMap<>();
        updates.put("custom.domain", "bulk-update.local");
        updates.put("cloud.sync.enabled", "true");

        BulkUpdateSettingsRequest request = new BulkUpdateSettingsRequest();
        request.setUpdates(updates);

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/bulk")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settings").isArray())
                .andExpect(jsonPath("$.settings.length()").value(2))
                .andExpect(jsonPath("$.requiresRestart").isBoolean())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PUT /api/settings/bulk - Should return restart warning when needed")
    void shouldReturnRestartWarningWhenNeeded() throws Exception {
        // Given - custom.domain requires restart
        Map<String, String> updates = new HashMap<>();
        updates.put("custom.domain", "restart-required.local");

        BulkUpdateSettingsRequest request = new BulkUpdateSettingsRequest();
        request.setUpdates(updates);

        // When / Then
        mockMvc.perform(put(SETTINGS_BASE_URL + "/bulk")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresRestart").value(true))
                .andExpect(jsonPath("$.restartCommand").value("docker-compose -f docker-compose-lite.yml restart"));
    }

    // ============================================================================
    // POST /api/settings/{key}/reset Tests
    // ============================================================================

    @Test
    @DisplayName("POST /api/settings/{key}/reset - Should reset setting to default")
    void shouldResetSettingToDefault() throws Exception {
        // Given - Update setting first
        SystemSettings setting = settingsRepository.findByKey("custom.domain").orElseThrow();
        setting.setValue("modified-value");
        settingsRepository.save(setting);

        // When / Then
        mockMvc.perform(post(SETTINGS_BASE_URL + "/custom.domain/reset")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("custom.domain"))
                .andExpect(jsonPath("$.value").value("shopmanager.local")); // Default value
    }

    // ============================================================================
    // Sensitive Value Masking Tests
    // ============================================================================

    @Test
    @DisplayName("Should mask sensitive values in response")
    void shouldMaskSensitiveValuesInResponse() throws Exception {
        mockMvc.perform(get(SETTINGS_BASE_URL + "/grouped")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SECURITY[?(@.key=='jwt.secret')].value").value("********"));
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private void createTestSettings() {
        // Domain settings
        SystemSettings domainSetting = createSetting(
            "custom.domain",
            "shopmanager.local",
            SettingCategory.DOMAIN,
            SettingDataType.STRING,
            "Custom domain for the application",
            true,  // requires restart
            false  // not sensitive
        );

        // Sync settings
        SystemSettings syncSetting = createSetting(
            "cloud.sync.enabled",
            "false",
            SettingCategory.SYNC,
            SettingDataType.BOOLEAN,
            "Enable cloud synchronization",
            false,
            false
        );

        // Storage settings
        SystemSettings storageSetting = createSetting(
            "max.upload.size",
            "10",
            SettingCategory.STORAGE,
            SettingDataType.NUMBER,
            "Maximum upload size in MB",
            false,
            false
        );

        // Security settings - sensitive
        SystemSettings securitySetting = createSetting(
            "jwt.secret",
            "secret-key-12345",
            SettingCategory.SECURITY,
            SettingDataType.ENCRYPTED,
            "JWT secret key",
            true,
            true  // sensitive
        );

        settingsRepository.save(domainSetting);
        settingsRepository.save(syncSetting);
        settingsRepository.save(storageSetting);
        settingsRepository.save(securitySetting);
    }

    private SystemSettings createSetting(String key, String value, SettingCategory category,
                                         SettingDataType dataType, String description,
                                         boolean requiresRestart, boolean isSensitive) {
        SystemSettings setting = new SystemSettings();
        setting.setKey(key);
        setting.setValue(value);
        setting.setCategory(category);
        setting.setDataType(dataType);
        setting.setDescription(description);
        setting.setRequiresRestart(requiresRestart);
        setting.setIsSensitive(isSensitive);
        setting.setDefaultValue(value);
        setting.setVersion(0);
        return setting;
    }

    private Permission createPermission(String name, String description) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }

    private Role createRole(String name, Set<Permission> permissions) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("Test role: " + name);
        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    private User createUser(String username, String email, String password, Set<Role> roles) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(roles);
        user.setActive(true);
        return userRepository.save(user);
    }
}
