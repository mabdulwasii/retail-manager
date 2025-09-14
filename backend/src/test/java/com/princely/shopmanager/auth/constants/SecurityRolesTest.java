package com.princely.shopmanager.auth.constants;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for SecurityRoles constants.
 */
class SecurityRolesTest {

    @Test
    void testAllRoleConstantsAreDefined() {
        // Test that all role constants are properly defined and non-null
        assertThat(SecurityRoles.ROLE_SYSTEM_ADMIN).isEqualTo("ROLE_SYSTEM_ADMIN");
        assertThat(SecurityRoles.ROLE_SUPER_ADMIN).isEqualTo("ROLE_SUPER_ADMIN");
        assertThat(SecurityRoles.ROLE_SHOP_OWNER).isEqualTo("ROLE_SHOP_OWNER");
        assertThat(SecurityRoles.ROLE_SHOP_MANAGER).isEqualTo("ROLE_SHOP_MANAGER");
        assertThat(SecurityRoles.ROLE_SHOP_EMPLOYEE).isEqualTo("ROLE_SHOP_EMPLOYEE");
        assertThat(SecurityRoles.ROLE_CASHIER).isEqualTo("ROLE_CASHIER");
        assertThat(SecurityRoles.ROLE_INVENTORY_MANAGER).isEqualTo("ROLE_INVENTORY_MANAGER");
        assertThat(SecurityRoles.ROLE_SALES_MANAGER).isEqualTo("ROLE_SALES_MANAGER");
        assertThat(SecurityRoles.ROLE_INVESTOR).isEqualTo("ROLE_INVESTOR");
        assertThat(SecurityRoles.ROLE_ACCOUNTANT).isEqualTo("ROLE_ACCOUNTANT");
        assertThat(SecurityRoles.ROLE_AUDITOR).isEqualTo("ROLE_AUDITOR");
        assertThat(SecurityRoles.ROLE_CUSTOMER).isEqualTo("ROLE_CUSTOMER");
        assertThat(SecurityRoles.ROLE_GUEST).isEqualTo("ROLE_GUEST");
    }

    @Test
    void testRoleNamesWithoutPrefix() {
        // Test role names without ROLE_ prefix
        assertThat(SecurityRoles.SYSTEM_ADMIN).isEqualTo("SYSTEM_ADMIN");
        assertThat(SecurityRoles.SUPER_ADMIN).isEqualTo("SUPER_ADMIN");
        assertThat(SecurityRoles.SHOP_OWNER).isEqualTo("SHOP_OWNER");
        assertThat(SecurityRoles.SHOP_MANAGER).isEqualTo("SHOP_MANAGER");
        assertThat(SecurityRoles.SHOP_EMPLOYEE).isEqualTo("SHOP_EMPLOYEE");
        assertThat(SecurityRoles.CASHIER).isEqualTo("CASHIER");
        assertThat(SecurityRoles.INVENTORY_MANAGER).isEqualTo("INVENTORY_MANAGER");
        assertThat(SecurityRoles.SALES_MANAGER).isEqualTo("SALES_MANAGER");
        assertThat(SecurityRoles.INVESTOR).isEqualTo("INVESTOR");
        assertThat(SecurityRoles.ACCOUNTANT).isEqualTo("ACCOUNTANT");
        assertThat(SecurityRoles.AUDITOR).isEqualTo("AUDITOR");
        assertThat(SecurityRoles.CUSTOMER).isEqualTo("CUSTOMER");
        assertThat(SecurityRoles.GUEST).isEqualTo("GUEST");
    }

    @Test
    void testPrivateConstructorPreventsInstantiation() throws Exception {
        // Test that the utility class has a private constructor
        Constructor<SecurityRoles> constructor = SecurityRoles.class.getDeclaredConstructor();
        assertThat(constructor.isAccessible()).isFalse();

        // Make constructor accessible and test instantiation
        constructor.setAccessible(true);
        assertThatThrownBy(constructor::newInstance)
            .hasCauseInstanceOf(Exception.class);
    }

    @Test
    void testRoleHierarchy() {
        // Test that system roles are correctly defined
        assertThat(SecurityRoles.ROLE_SYSTEM_ADMIN).contains("SYSTEM_ADMIN");
        assertThat(SecurityRoles.ROLE_SUPER_ADMIN).contains("SUPER_ADMIN");

        // Test that shop roles are correctly defined
        assertThat(SecurityRoles.ROLE_SHOP_OWNER).contains("SHOP_OWNER");
        assertThat(SecurityRoles.ROLE_SHOP_MANAGER).contains("SHOP_MANAGER");

        // Test that operational roles are correctly defined
        assertThat(SecurityRoles.ROLE_CASHIER).contains("CASHIER");
        assertThat(SecurityRoles.ROLE_INVENTORY_MANAGER).contains("INVENTORY_MANAGER");
    }
}