package com.princely.shopmanager.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tenant", "roles"})
@EqualsAndHashCode(callSuper = true, exclude = {"tenant", "roles"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    @JsonIgnoreProperties({"contactUser", "users", "shops"})
    private Tenant tenant;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;

    @NotEmpty(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @Email(message = "Email is not valid")
    @NotEmpty(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @NotEmpty(message = "First name is required")
    @Column(name = "first_name")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    @Column(name = "last_name")
    private String lastName;

    @NotEmpty(message = "Phone number is required")
    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull(message = "User status is required")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.INACTIVE;


    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @Column(name = "is_investor")
    private boolean isInvestor = false;

    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }
}