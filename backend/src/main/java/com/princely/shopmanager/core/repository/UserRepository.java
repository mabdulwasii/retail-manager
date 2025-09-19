package com.princely.shopmanager.core.repository;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByKeycloakId(String keycloakId);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId")
    List<User> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.status = :status")
    List<User> findByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenant.id = :tenantId")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByKeycloakId(String keycloakId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRolesContaining(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRolesContaining(@Param("role") Role role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}