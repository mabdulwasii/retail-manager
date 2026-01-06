package com.princely.shopmanager.embedded.controller;

import com.princely.shopmanager.embedded.dto.UpdateCheckResponse;
import com.princely.shopmanager.embedded.service.UpdateCheckService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Update Controller Tests")
class UpdateControllerTest {

    @Mock
    private UpdateCheckService updateCheckService;

    @InjectMocks
    private UpdateController updateController;

    private JwtPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        testPrincipal = JwtPrincipal.builder()
                .userId("user-123")
                .preferredUsername("admin")
                .tenantId("tenant-456")
                .shopId("shop-789")
                .roles(List.of("SYSTEM_ADMIN"))
                .build();
    }

    @Test
    @DisplayName("Should trigger manual update check successfully")
    void shouldTriggerManualUpdateCheck() {
        // Arrange
        UpdateCheckResponse expectedResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .latestVersion("0.1.29")
                .updateAvailable(true)
                .checkedAt(LocalDateTime.now())
                .releaseDate("2026-01-06")
                .downloadUrls(Map.of("windows", "https://test.com/installer.exe"))
                .releaseNotesUrl("https://github.com/test/releases/tag/v0.1.29")
                .status("SUCCESS")
                .build();

        when(updateCheckService.checkForUpdates()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.checkForUpdates(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        assertThat(response.getBody().getUpdateAvailable()).isTrue();

        verify(updateCheckService, times(1)).checkForUpdates();
    }

    @Test
    @DisplayName("Should handle manual check when no update available")
    void shouldHandleNoUpdateAvailable() {
        // Arrange
        UpdateCheckResponse expectedResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.29")
                .latestVersion("0.1.29")
                .updateAvailable(false)
                .checkedAt(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        when(updateCheckService.checkForUpdates()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.checkForUpdates(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUpdateAvailable()).isFalse();
        assertThat(response.getBody().getCurrentVersion()).isEqualTo("0.1.29");
        assertThat(response.getBody().getLatestVersion()).isEqualTo("0.1.29");
    }

    @Test
    @DisplayName("Should handle API errors during manual check")
    void shouldHandleApiErrorsDuringManualCheck() {
        // Arrange
        UpdateCheckResponse errorResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .updateAvailable(false)
                .checkedAt(LocalDateTime.now())
                .status("ERROR")
                .errorMessage("Failed to connect to cloud API")
                .build();

        when(updateCheckService.checkForUpdates()).thenReturn(errorResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.checkForUpdates(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("ERROR");
        assertThat(response.getBody().getErrorMessage()).contains("Failed to connect");
    }

    @Test
    @DisplayName("Should get cached update status successfully")
    void shouldGetCachedStatusSuccessfully() {
        // Arrange
        UpdateCheckResponse cachedResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .latestVersion("0.1.29")
                .updateAvailable(true)
                .checkedAt(LocalDateTime.now().minusHours(2))
                .releaseDate("2026-01-06")
                .status("SUCCESS")
                .build();

        when(updateCheckService.getCachedStatus()).thenReturn(cachedResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.getUpdateStatus(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(cachedResponse);
        assertThat(response.getBody().getUpdateAvailable()).isTrue();

        verify(updateCheckService, times(1)).getCachedStatus();
        verify(updateCheckService, never()).checkForUpdates();
    }

    @Test
    @DisplayName("Should return no content when no cached status exists")
    void shouldReturnNoContentWhenNoCacheExists() {
        // Arrange
        when(updateCheckService.getCachedStatus()).thenReturn(null);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.getUpdateStatus(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(updateCheckService, times(1)).getCachedStatus();
    }

    @Test
    @DisplayName("Should get cached error status")
    void shouldGetCachedErrorStatus() {
        // Arrange
        UpdateCheckResponse cachedErrorResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .updateAvailable(false)
                .checkedAt(LocalDateTime.now().minusMinutes(30))
                .status("ERROR")
                .errorMessage("Previous check failed")
                .build();

        when(updateCheckService.getCachedStatus()).thenReturn(cachedErrorResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.getUpdateStatus(testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo("ERROR");
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Previous check failed");
    }

    @Test
    @DisplayName("Should work with different user roles")
    void shouldWorkWithDifferentUserRoles() {
        // Arrange - TENANT_ADMIN role
        JwtPrincipal tenantAdmin = JwtPrincipal.builder()
                .userId("user-456")
                .preferredUsername("tenant-admin")
                .tenantId("tenant-456")
                .shopId("shop-789")
                .roles(List.of("TENANT_ADMIN"))
                .build();

        UpdateCheckResponse response = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .latestVersion("0.1.28")
                .updateAvailable(false)
                .status("SUCCESS")
                .build();

        when(updateCheckService.checkForUpdates()).thenReturn(response);

        // Act
        ResponseEntity<UpdateCheckResponse> result = updateController.checkForUpdates(tenantAdmin);

        // Assert
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(updateCheckService).checkForUpdates();
    }

    @Test
    @DisplayName("Should include all version info in response")
    void shouldIncludeAllVersionInfo() {
        // Arrange
        Map<String, String> downloadUrls = Map.of(
                "windows", "https://github.com/test/releases/shop-manager-0.1.29-windows.exe",
                "macos", "https://github.com/test/releases/shop-manager-0.1.29-macos.dmg",
                "linux_deb", "https://github.com/test/releases/shop-manager_0.1.29_all.deb",
                "linux_rpm", "https://github.com/test/releases/shop-manager-0.1.29-1.x86_64.rpm"
        );

        UpdateCheckResponse expectedResponse = UpdateCheckResponse.builder()
                .currentVersion("0.1.28")
                .latestVersion("0.1.29")
                .updateAvailable(true)
                .checkedAt(LocalDateTime.now())
                .releaseDate("2026-01-06")
                .downloadUrls(downloadUrls)
                .releaseNotesUrl("https://github.com/test/releases/tag/v0.1.29")
                .status("SUCCESS")
                .build();

        when(updateCheckService.checkForUpdates()).thenReturn(expectedResponse);

        // Act
        ResponseEntity<UpdateCheckResponse> response = updateController.checkForUpdates(testPrincipal);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDownloadUrls()).hasSize(4);
        assertThat(response.getBody().getDownloadUrls()).containsKeys("windows", "macos", "linux_deb", "linux_rpm");
        assertThat(response.getBody().getReleaseNotesUrl()).contains("v0.1.29");
        assertThat(response.getBody().getReleaseDate()).isEqualTo("2026-01-06");
    }
}
