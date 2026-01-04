package com.princely.shopmanager.shared.specification;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.dto.AuditLogFilterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AuditLogSpecification.
 * Tests the specification creation logic without database interaction.
 */
@DisplayName("Audit Log Specification - Unit Tests")
class AuditLogSpecificationTest {

    private Shop testShop;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("test-shop");
        testShop.setName("Test Shop");
    }

    @Test
    @DisplayName("Should create specification with shop filter only")
    void shouldCreateSpecificationWithShopFilterOnly() {
        // Given
        AuditLogFilterRequest filters = new AuditLogFilterRequest();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with action type filter")
    void shouldCreateSpecificationWithActionTypeFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .actionType("CREATE")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with entity type filter")
    void shouldCreateSpecificationWithEntityTypeFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .entityType("PRODUCT")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with search filter")
    void shouldCreateSpecificationWithSearchFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .search("test search")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with date range filter")
    void shouldCreateSpecificationWithDateRangeFilter() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .dateFrom(now.minusDays(7))
            .dateTo(now)
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with category filter")
    void shouldCreateSpecificationWithCategoryFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .category("DATA_MODIFICATION")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with severity filter")
    void shouldCreateSpecificationWithSeverityFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .severity("WARNING")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with success filter")
    void shouldCreateSpecificationWithSuccessFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .success(false)
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with user ID filter")
    void shouldCreateSpecificationWithUserIdFilter() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .userId("user-123")
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with all filters combined")
    void shouldCreateSpecificationWithAllFilters() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .actionType("CREATE")
            .entityType("PRODUCT")
            .search("widget")
            .dateFrom(now.minusDays(7))
            .dateTo(now)
            .category("DATA_MODIFICATION")
            .userId("user-123")
            .severity("INFO")
            .success(true)
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should not throw exception with invalid action type")
    void shouldNotThrowExceptionWithInvalidActionType() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .actionType("INVALID_ACTION")
            .build();

        // When/Then - Should not throw exception
        assertThatCode(() -> AuditLogSpecification.fromFilters(testShop, filters))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not throw exception with invalid category")
    void shouldNotThrowExceptionWithInvalidCategory() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .category("INVALID_CATEGORY")
            .build();

        // When/Then - Should not throw exception
        assertThatCode(() -> AuditLogSpecification.fromFilters(testShop, filters))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not throw exception with invalid severity")
    void shouldNotThrowExceptionWithInvalidSeverity() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .severity("INVALID_SEVERITY")
            .build();

        // When/Then - Should not throw exception
        assertThatCode(() -> AuditLogSpecification.fromFilters(testShop, filters))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create specification with empty filters")
    void shouldCreateSpecificationWithEmptyFilters() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder().build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("Should create specification with null string filters")
    void shouldCreateSpecificationWithNullStringFilters() {
        // Given
        AuditLogFilterRequest filters = AuditLogFilterRequest.builder()
            .actionType(null)
            .entityType(null)
            .search(null)
            .category(null)
            .severity(null)
            .build();

        // When
        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(testShop, filters);

        // Then
        assertThat(spec).isNotNull();
    }
}
