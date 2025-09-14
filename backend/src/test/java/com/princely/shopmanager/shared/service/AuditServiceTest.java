package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private Shop testShop;
    private AuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        testAuditLog = new AuditLog();
        testAuditLog.setId("audit-1");
        testAuditLog.setShop(testShop);
        testAuditLog.setUserId("user-1");
        testAuditLog.setUsername("testuser");
        testAuditLog.setCategory(AuditLog.AuditCategory.SECURITY_EVENT);
        testAuditLog.setActionType(AuditLog.ActionType.LOGIN);
        testAuditLog.setActionDescription("User login successful");
        testAuditLog.setSeverity(AuditLog.Severity.INFO);
        testAuditLog.setSuccess(true);
    }

    @Test
    void logSecurityEvent_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logSecurityEvent(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.LOGIN, "User login successful",
            "192.168.1.1", true
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getShop()).isEqualTo(testShop);
        assertThat(savedAudit.getUserId()).isEqualTo("user-1");
        assertThat(savedAudit.getUsername()).isEqualTo("testuser");
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.LOGIN);
        assertThat(savedAudit.getActionDescription()).isEqualTo("User login successful");
        assertThat(savedAudit.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(savedAudit.isSuccess()).isTrue();
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.SECURITY_EVENT);
    }

    @Test
    void logSecurityEvent_WithRepositoryException_ShouldHandleGracefully() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - should not throw exception
        assertThatCode(() -> auditService.logSecurityEvent(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.LOGIN, "User login successful",
            "192.168.1.1", true
        )).doesNotThrowAnyException();

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void logDataModification_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logDataModification(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.UPDATE, "Product", "product-1",
            "Updated product price", "price: 10.00", "price: 12.00"
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getShop()).isEqualTo(testShop);
        assertThat(savedAudit.getUserId()).isEqualTo("user-1");
        assertThat(savedAudit.getUsername()).isEqualTo("testuser");
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.UPDATE);
        assertThat(savedAudit.getEntityType()).isEqualTo("Product");
        assertThat(savedAudit.getEntityId()).isEqualTo("product-1");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Updated product price");
        assertThat(savedAudit.getOldValues()).isEqualTo("price: 10.00");
        assertThat(savedAudit.getNewValues()).isEqualTo("price: 12.00");
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.DATA_MODIFICATION);
    }

    @Test
    void logDataModification_WithRepositoryException_ShouldHandleGracefully() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatCode(() -> auditService.logDataModification(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.UPDATE, "Product", "product-1",
            "Updated product price", "price: 10.00", "price: 12.00"
        )).doesNotThrowAnyException();
    }

    @Test
    void logFinancialTransaction_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logFinancialTransaction(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.CREATE, "transaction-1",
            "Sale completed for $100.00", true
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getShop()).isEqualTo(testShop);
        assertThat(savedAudit.getUserId()).isEqualTo("user-1");
        assertThat(savedAudit.getUsername()).isEqualTo("testuser");
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.CREATE);
        assertThat(savedAudit.getEntityId()).isEqualTo("transaction-1");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Sale completed for $100.00");
        assertThat(savedAudit.isSuccess()).isTrue();
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.FINANCIAL_TRANSACTION);
    }

    @Test
    void logFinancialTransaction_WithFailure_ShouldLogFailure() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logFinancialTransaction(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.CREATE, "transaction-1",
            "Sale failed due to insufficient funds", false
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.isSuccess()).isFalse();
    }

    @Test
    void logCustomEvent_WithAllParameters_ShouldSaveAuditLog() {
        // Arrange
        Map<String, String> details = Map.of("key1", "value1", "key2", "value2");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCustomEvent(
            testShop, "user-1", "testuser",
            AuditLog.AuditCategory.SYSTEM_EVENT, AuditLog.ActionType.EXPORT,
            "CustomEntity", "entity-1", "Custom event occurred",
            details, AuditLog.Severity.WARNING
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getShop()).isEqualTo(testShop);
        assertThat(savedAudit.getUserId()).isEqualTo("user-1");
        assertThat(savedAudit.getUsername()).isEqualTo("testuser");
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.SYSTEM_EVENT);
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.EXPORT);
        assertThat(savedAudit.getEntityType()).isEqualTo("CustomEntity");
        assertThat(savedAudit.getEntityId()).isEqualTo("entity-1");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Custom event occurred");
        assertThat(savedAudit.getDetails()).isEqualTo(details);
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.WARNING);
        assertThat(savedAudit.isSuccess()).isTrue();
    }

    @Test
    void logCustomEvent_WithNullDetails_ShouldUseEmptyMap() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logCustomEvent(
            testShop, "user-1", "testuser",
            AuditLog.AuditCategory.SYSTEM_EVENT, AuditLog.ActionType.EXPORT,
            "CustomEntity", "entity-1", "Custom event occurred",
            null, null
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getDetails()).isEqualTo(Map.of());
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.INFO);
    }

    @Test
    void logError_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logError(
            testShop, "user-1", "testuser",
            AuditLog.ActionType.BACKUP, "System error occurred",
            "NullPointerException: Object reference not set"
        );

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getShop()).isEqualTo(testShop);
        assertThat(savedAudit.getUserId()).isEqualTo("user-1");
        assertThat(savedAudit.getUsername()).isEqualTo("testuser");
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.SYSTEM_EVENT);
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.BACKUP);
        assertThat(savedAudit.getActionDescription()).isEqualTo("System error occurred");
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.ERROR);
        assertThat(savedAudit.isSuccess()).isFalse();
        assertThat(savedAudit.getErrorMessage()).isEqualTo("NullPointerException: Object reference not set");
    }

    @Test
    void logEntityCreation_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logEntityCreation("Product", "product-123", "Created new product: Laptop");

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.DATA_MODIFICATION);
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.CREATE);
        assertThat(savedAudit.getEntityType()).isEqualTo("Product");
        assertThat(savedAudit.getEntityId()).isEqualTo("product-123");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Created new product: Laptop");
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.INFO);
        assertThat(savedAudit.isSuccess()).isTrue();
        assertThat(savedAudit.getActionDate()).isNotNull();
    }

    @Test
    void logEntityCreation_WithRepositoryException_ShouldHandleGracefully() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatCode(() -> auditService.logEntityCreation("Product", "product-123", "Created new product: Laptop"))
            .doesNotThrowAnyException();
    }

    @Test
    void logEntityModification_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logEntityModification("Product", "product-123", "Updated product price from $10 to $12");

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.DATA_MODIFICATION);
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.UPDATE);
        assertThat(savedAudit.getEntityType()).isEqualTo("Product");
        assertThat(savedAudit.getEntityId()).isEqualTo("product-123");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Updated product price from $10 to $12");
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.INFO);
        assertThat(savedAudit.isSuccess()).isTrue();
    }

    @Test
    void logEntityModification_WithRepositoryException_ShouldHandleGracefully() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatCode(() -> auditService.logEntityModification("Product", "product-123", "Updated product"))
            .doesNotThrowAnyException();
    }

    @Test
    void logEntityDeletion_WithValidParameters_ShouldSaveAuditLog() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        // Act
        auditService.logEntityDeletion("Product", "product-123", "Deleted product: Laptop");

        // Assert
        ArgumentCaptor<AuditLog> auditCaptor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(auditCaptor.capture());

        AuditLog savedAudit = auditCaptor.getValue();
        assertThat(savedAudit.getCategory()).isEqualTo(AuditLog.AuditCategory.DATA_MODIFICATION);
        assertThat(savedAudit.getActionType()).isEqualTo(AuditLog.ActionType.DELETE);
        assertThat(savedAudit.getEntityType()).isEqualTo("Product");
        assertThat(savedAudit.getEntityId()).isEqualTo("product-123");
        assertThat(savedAudit.getActionDescription()).isEqualTo("Deleted product: Laptop");
        assertThat(savedAudit.getSeverity()).isEqualTo(AuditLog.Severity.INFO);
        assertThat(savedAudit.isSuccess()).isTrue();
    }

    @Test
    void logEntityDeletion_WithRepositoryException_ShouldHandleGracefully() {
        // Arrange
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatCode(() -> auditService.logEntityDeletion("Product", "product-123", "Deleted product"))
            .doesNotThrowAnyException();
    }
}