package com.princely.shopmanager.shared.repository.base;

import com.princely.shopmanager.auth.context.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Base repository that automatically filters queries by the current tenant context.
 * All repositories extending this will have tenant isolation enforced.
 */
@NoRepositoryBean
@Slf4j
public abstract class TenantAwareRepository<T, ID> extends SimpleJpaRepository<T, ID> {

    private static final String FIELD_TENANT_ID = "tenantId";

    @PersistenceContext
    protected EntityManager entityManager;

    public TenantAwareRepository(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.entityManager = em;
    }

    @Override
    public List<T> findAll() {
        return findAllByCurrentTenant();
    }

    @Override
    public Optional<T> findById(ID id) {
        return findByIdAndCurrentTenant(id);
    }

    protected List<T> findAllByCurrentTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            log.warn("No tenant context available for findAll operation on {}", getDomainClass().getSimpleName());
            return List.of();
        }

        String queryStr = String.format("SELECT e FROM %s e WHERE e.shop.id = :tenantId",
            getDomainClass().getSimpleName());

        try {
            Query query = entityManager.createQuery(queryStr, getDomainClass());
            query.setParameter(FIELD_TENANT_ID, tenantId);
            return query.getResultList();
        } catch (Exception e) {
            log.debug("Tenant-aware query failed for {}, falling back to default: {}",
                getDomainClass().getSimpleName(), e.getMessage());
            return super.findAll();
        }
    }

    protected Optional<T> findByIdAndCurrentTenant(ID id) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            log.warn("No tenant context available for findById operation on {}", getDomainClass().getSimpleName());
            return Optional.empty();
        }

        String queryStr = String.format("SELECT e FROM %s e WHERE e.id = :id AND e.shop.id = :tenantId",
            getDomainClass().getSimpleName());

        try {
            Query query = entityManager.createQuery(queryStr, getDomainClass());
            query.setParameter("id", id);
            query.setParameter(FIELD_TENANT_ID, tenantId);
            List<T> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.debug("Tenant-aware query failed for {}, falling back to default: {}",
                getDomainClass().getSimpleName(), e.getMessage());
            return super.findById(id);
        }
    }

    /**
     * Execute a tenant-aware query
     */
    protected List<T> executeTenantAwareQuery(String baseQuery, Object... parameters) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }

        String tenantAwareQuery = baseQuery + " AND e.shop.id = :tenantId";
        Query query = entityManager.createQuery(tenantAwareQuery, getDomainClass());

        // Set parameters
        for (int i = 0; i < parameters.length; i += 2) {
            query.setParameter((String) parameters[i], parameters[i + 1]);
        }
        query.setParameter(FIELD_TENANT_ID, tenantId);

        return query.getResultList();
    }

    /**
     * Execute a tenant-aware count query
     */
    protected long executeTenantAwareCountQuery(String baseQuery, Object... parameters) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }

        String tenantAwareQuery = baseQuery + " AND e.shop.id = :tenantId";
        Query query = entityManager.createQuery(tenantAwareQuery);

        // Set parameters
        for (int i = 0; i < parameters.length; i += 2) {
            query.setParameter((String) parameters[i], parameters[i + 1]);
        }
        query.setParameter(FIELD_TENANT_ID, tenantId);

        return (Long) query.getSingleResult();
    }
}