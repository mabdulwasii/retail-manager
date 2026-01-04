package com.princely.shopmanager.shared.specification;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.dto.AuditLogFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification for filtering audit logs.
 * Provides type-safe and composable query building.
 */
public class AuditLogSpecification {

    /**
     * Create specification from filter request and shop
     */
    public static Specification<AuditLog> fromFilters(Shop shop, AuditLogFilterRequest filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by shop
            predicates.add(criteriaBuilder.equal(root.get("shop"), shop));

            // Action type filter
            if (filters.getActionType() != null && !filters.getActionType().isEmpty()) {
                try {
                    AuditLog.ActionType actionType = AuditLog.ActionType.valueOf(filters.getActionType());
                    predicates.add(criteriaBuilder.equal(root.get("actionType"), actionType));
                } catch (IllegalArgumentException e) {
                    // Invalid action type, ignore filter
                }
            }

            // Entity type filter
            if (filters.getEntityType() != null && !filters.getEntityType().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), filters.getEntityType()));
            }

            // Category filter
            if (filters.getCategory() != null && !filters.getCategory().isEmpty()) {
                try {
                    AuditLog.AuditCategory category = AuditLog.AuditCategory.valueOf(filters.getCategory());
                    predicates.add(criteriaBuilder.equal(root.get("category"), category));
                } catch (IllegalArgumentException e) {
                    // Invalid category, ignore filter
                }
            }

            // User ID filter
            if (filters.getUserId() != null && !filters.getUserId().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), filters.getUserId()));
            }

            // Date range filters
            if (filters.getDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("actionDate"), filters.getDateFrom()));
            }

            if (filters.getDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("actionDate"), filters.getDateTo()));
            }

            // Severity filter
            if (filters.getSeverity() != null && !filters.getSeverity().isEmpty()) {
                try {
                    AuditLog.Severity severity = AuditLog.Severity.valueOf(filters.getSeverity());
                    predicates.add(criteriaBuilder.equal(root.get("severity"), severity));
                } catch (IllegalArgumentException e) {
                    // Invalid severity, ignore filter
                }
            }

            // Success filter
            if (filters.getSuccess() != null) {
                predicates.add(criteriaBuilder.equal(root.get("success"), filters.getSuccess()));
            }

            // Search filter (searches across description, username, and entity type)
            if (filters.getSearch() != null && !filters.getSearch().isEmpty()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";
                Predicate descriptionMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("actionDescription")),
                    searchPattern
                );
                Predicate usernameMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")),
                    searchPattern
                );
                Predicate entityTypeMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("entityType")),
                    searchPattern
                );

                predicates.add(criteriaBuilder.or(descriptionMatch, usernameMatch, entityTypeMatch));
            }

            // Order by action date descending
            query.orderBy(criteriaBuilder.desc(root.get("actionDate")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
