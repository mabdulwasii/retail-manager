package com.princely.shopmanager.auth.principal;

import com.princely.shopmanager.auth.domain.JwtPrincipal;

import java.util.List;

/**
 * Principal interface representing an authenticated user
 */
public interface UserPrincipal {
    String getUserId();
    String getUsername();
    String getEmail();
    String getFullName();
    List<String> getRoles();
    boolean hasRole(String role);

    /**
     * Create UserPrincipal from JwtPrincipal
     */
    static UserPrincipal from(JwtPrincipal jwtPrincipal) {
        return new JwtUserPrincipal(jwtPrincipal);
    }

    /**
     * Implementation backed by JwtPrincipal
     */
    class JwtUserPrincipal implements UserPrincipal {
        private final JwtPrincipal jwtPrincipal;

        public JwtUserPrincipal(JwtPrincipal jwtPrincipal) {
            this.jwtPrincipal = jwtPrincipal;
        }

        @Override
        public String getUserId() {
            return jwtPrincipal.getUserId();
        }

        @Override
        public String getUsername() {
            return jwtPrincipal.getUsername();
        }

        @Override
        public String getEmail() {
            return jwtPrincipal.getEmail();
        }

        @Override
        public String getFullName() {
            return jwtPrincipal.getFullName();
        }

        @Override
        public List<String> getRoles() {
            return jwtPrincipal.getRoles();
        }

        @Override
        public boolean hasRole(String role) {
            return jwtPrincipal.hasRole(role);
        }
    }

    /**
     * Simple implementation for testing
     */
    static UserPrincipal of(String userId, String email, String username, List<String> roles) {
        return new UserPrincipal() {
            @Override
            public String getUserId() {
                return userId;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getEmail() {
                return email;
            }

            @Override
            public String getFullName() {
                return username;
            }

            @Override
            public List<String> getRoles() {
                return roles;
            }

            @Override
            public boolean hasRole(String role) {
                return roles != null && roles.contains(role);
            }
        };
    }
}