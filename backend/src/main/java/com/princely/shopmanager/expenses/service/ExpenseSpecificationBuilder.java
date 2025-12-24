package com.princely.shopmanager.expenses.service;

import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.dto.ExpenseFilterCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for building JPA Specifications for expense filtering.
 * Constructs dynamic queries based on filter criteria.
 */
@Service
@Slf4j
public class ExpenseSpecificationBuilder {

    /**
     * Creates a JPA Specification for filtering expenses based on criteria.
     *
     * @param shopId Shop ID (required filter)
     * @param criteria Additional filter criteria
     * @return JPA Specification for querying expenses
     */
    public Specification<Expense> buildSpecification(String shopId, ExpenseFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Always filter by shop
            predicates.add(cb.equal(root.get("shopId"), shopId));

            // Add date range filters
            addDateRangePredicates(criteria, root, cb, predicates);

            // Add amount range filters
            addAmountRangePredicates(criteria, root, cb, predicates);

            // Add categorical filters
            addCategoricalPredicates(criteria, root, cb, predicates);

            // Add search query filter
            addSearchPredicates(criteria, root, cb, predicates);

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Adds date range predicates (start date, end date).
     */
    private void addDateRangePredicates(ExpenseFilterCriteria criteria,
                                       jakarta.persistence.criteria.Root<Expense> root,
                                       jakarta.persistence.criteria.CriteriaBuilder cb,
                                       List<jakarta.persistence.criteria.Predicate> predicates) {
        if (criteria.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("expenseDate"), criteria.getStartDate()));
        }

        if (criteria.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("expenseDate"), criteria.getEndDate()));
        }
    }

    /**
     * Adds amount range predicates (min amount, max amount).
     */
    private void addAmountRangePredicates(ExpenseFilterCriteria criteria,
                                         jakarta.persistence.criteria.Root<Expense> root,
                                         jakarta.persistence.criteria.CriteriaBuilder cb,
                                         List<jakarta.persistence.criteria.Predicate> predicates) {
        if (criteria.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), criteria.getMinAmount()));
        }

        if (criteria.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("amount"), criteria.getMaxAmount()));
        }
    }

    /**
     * Adds categorical predicates (status, category, creator).
     */
    private void addCategoricalPredicates(ExpenseFilterCriteria criteria,
                                         jakarta.persistence.criteria.Root<Expense> root,
                                         jakarta.persistence.criteria.CriteriaBuilder cb,
                                         List<jakarta.persistence.criteria.Predicate> predicates) {
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }

        if (criteria.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("categoryId"), criteria.getCategoryId()));
        }

        if (criteria.getCreatedBy() != null) {
            predicates.add(cb.equal(root.get("expenseCreatedBy"), criteria.getCreatedBy()));
        }
    }

    /**
     * Adds search query predicates (title, description, vendor name).
     */
    private void addSearchPredicates(ExpenseFilterCriteria criteria,
                                     jakarta.persistence.criteria.Root<Expense> root,
                                     jakarta.persistence.criteria.CriteriaBuilder cb,
                                     List<jakarta.persistence.criteria.Predicate> predicates) {
        if (criteria.getSearchQuery() != null && !criteria.getSearchQuery().trim().isEmpty()) {
            String searchPattern = "%" + criteria.getSearchQuery().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("title")), searchPattern),
                cb.like(cb.lower(root.get("description")), searchPattern),
                cb.like(cb.lower(root.get("vendorName")), searchPattern)
            ));
        }
    }
}
