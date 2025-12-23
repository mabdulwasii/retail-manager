package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.domain.TenantConfiguration;
import com.princely.shopmanager.core.dto.TenantConfigurationRequest;
import com.princely.shopmanager.core.repository.TenantConfigurationRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TenantConfigurationService {

    private static final String ERROR_CONFIG_PREFIX = "Configuration '";

    private final TenantConfigurationRepository configurationRepository;
    private final TenantRepository tenantRepository;

    /**
     * Get all configurations for a tenant.
     */
    @Transactional(readOnly = true)
    public List<TenantConfiguration> getAllConfigurations(String tenantId) {
        validateTenantExists(tenantId);
        log.debug("Retrieving all configurations for tenant: {}", tenantId);
        return configurationRepository.findByTenantId(tenantId);
    }

    /**
     * Get all active configurations for a tenant.
     */
    @Transactional(readOnly = true)
    public List<TenantConfiguration> getActiveConfigurations(String tenantId) {
        validateTenantExists(tenantId);
        log.debug("Retrieving active configurations for tenant: {}", tenantId);
        return configurationRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    /**
     * Get configurations by category for a tenant.
     */
    @Transactional(readOnly = true)
    public List<TenantConfiguration> getConfigurationsByCategory(
        String tenantId,
        TenantConfiguration.ConfigCategory category
    ) {
        validateTenantExists(tenantId);
        log.debug("Retrieving configurations for tenant: {} and category: {}", tenantId, category);
        return configurationRepository.findByTenantIdAndCategory(tenantId, category);
    }

    /**
     * Get a specific configuration by key.
     */
    @Transactional(readOnly = true)
    public TenantConfiguration getConfiguration(String tenantId, String key) {
        validateTenantExists(tenantId);
        log.debug("Retrieving configuration for tenant: {} and key: {}", tenantId, key);
        return configurationRepository.findByTenantIdAndKey(tenantId, key)
            .orElseThrow(() -> new BusinessException(
                "CONFIG_NOT_FOUND",
                "Configuration not found for tenant: " + tenantId + " and key: " + key
            ));
    }

    /**
     * Get configuration value by key (returns effective value).
     */
    @Transactional(readOnly = true)
    public String getConfigurationValue(String tenantId, String key) {
        TenantConfiguration config = getConfiguration(tenantId, key);
        return config.getEffectiveValue();
    }

    /**
     * Get all configurations as a key-value map (effective values).
     */
    @Transactional(readOnly = true)
    public Map<String, String> getConfigurationsAsMap(String tenantId) {
        List<TenantConfiguration> configs = getActiveConfigurations(tenantId);
        return configs.stream()
            .collect(Collectors.toMap(
                TenantConfiguration::getKey,
                TenantConfiguration::getEffectiveValue
            ));
    }

    /**
     * Create a new configuration.
     */
    public TenantConfiguration createConfiguration(String tenantId, TenantConfigurationRequest request) {
        validateTenantExists(tenantId);

        // Check if configuration already exists
        if (configurationRepository.existsByTenantIdAndKey(tenantId, request.getKey())) {
            throw new IllegalArgumentException(
                "Configuration with key '" + request.getKey() + "' already exists for tenant: " + tenantId
            );
        }

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new BusinessException("TENANT_NOT_FOUND", "Tenant not found: " + tenantId));

        TenantConfiguration configuration = TenantConfiguration.builder()
            .tenant(tenant)
            .key(request.getKey())
            .value(request.getValue())
            .defaultValue(request.getDefaultValue())
            .valueType(request.getValueType())
            .category(request.getCategory())
            .description(request.getDescription())
            .editable(request.getEditable() != null ? request.getEditable() : true)
            .active(request.getActive() != null ? request.getActive() : true)
            .build();

        TenantConfiguration saved = configurationRepository.save(configuration);
        log.info("Created configuration {} for tenant: {}", request.getKey(), tenantId);
        return saved;
    }

    /**
     * Update an existing configuration.
     */
    public TenantConfiguration updateConfiguration(
        String tenantId,
        String key,
        TenantConfigurationRequest request
    ) {
        TenantConfiguration configuration = getConfiguration(tenantId, key);

        // Check if configuration is editable
        if (!configuration.isEditable()) {
            throw new IllegalStateException(
                ERROR_CONFIG_PREFIX + key + "' is not editable"
            );
        }

        // Update fields
        if (request.getValue() != null) {
            configuration.setValue(request.getValue());
        }
        if (request.getDefaultValue() != null) {
            configuration.setDefaultValue(request.getDefaultValue());
        }
        if (request.getValueType() != null) {
            configuration.setValueType(request.getValueType());
        }
        if (request.getCategory() != null) {
            configuration.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            configuration.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            configuration.setActive(request.getActive());
        }

        TenantConfiguration updated = configurationRepository.save(configuration);
        log.info("Updated configuration {} for tenant: {}", key, tenantId);
        return updated;
    }

    /**
     * Update configuration value only.
     */
    public TenantConfiguration updateConfigurationValue(String tenantId, String key, String value) {
        TenantConfiguration configuration = getConfiguration(tenantId, key);

        if (!configuration.isEditable()) {
            throw new IllegalStateException(
                ERROR_CONFIG_PREFIX + key + "' is not editable"
            );
        }

        configuration.setValue(value);
        TenantConfiguration updated = configurationRepository.save(configuration);
        log.info("Updated value for configuration {} in tenant: {}", key, tenantId);
        return updated;
    }

    /**
     * Delete a configuration.
     */
    public void deleteConfiguration(String tenantId, String key) {
        TenantConfiguration configuration = getConfiguration(tenantId, key);

        if (!configuration.isEditable()) {
            throw new IllegalStateException(
                ERROR_CONFIG_PREFIX + key + "' is not editable and cannot be deleted"
            );
        }

        configurationRepository.delete(configuration);
        log.info("Deleted configuration {} for tenant: {}", key, tenantId);
    }

    /**
     * Bulk create or update configurations.
     */
    public List<TenantConfiguration> bulkUpsertConfigurations(
        String tenantId,
        List<TenantConfigurationRequest> requests
    ) {
        validateTenantExists(tenantId);

        return requests.stream()
            .map(request -> {
                if (configurationRepository.existsByTenantIdAndKey(tenantId, request.getKey())) {
                    return updateConfiguration(tenantId, request.getKey(), request);
                } else {
                    return createConfiguration(tenantId, request);
                }
            })
            .collect(Collectors.toList());
    }

    private void validateTenantExists(String tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new BusinessException("TENANT_NOT_FOUND", "Tenant not found: " + tenantId);
        }
    }
}
