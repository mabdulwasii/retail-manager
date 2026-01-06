package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.dto.UpdateCheckResponse;
import com.princely.shopmanager.embedded.dto.VersionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Update Check Service Tests")
class UpdateCheckServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private UpdateCheckService updateCheckService;

    private static final String CURRENT_VERSION = "0.1.28";
    private static final String LATEST_VERSION = "0.1.29";
    private static final String CLOUD_API_URL = "https://api.retailhq.app";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(updateCheckService, "currentVersion", CURRENT_VERSION);
        ReflectionTestUtils.setField(updateCheckService, "cloudApiUrl", CLOUD_API_URL);

        when(restClientBuilder.build()).thenReturn(restClient);
    }

    @Test
    @DisplayName("Should detect update available when latest version is higher")
    void shouldDetectUpdateAvailable() {
        // Arrange
        VersionInfo versionInfo = VersionInfo.builder()
                .version(LATEST_VERSION)
                .releaseDate("2026-01-06")
                .downloadUrls(Map.of(
                        "windows", "https://github.com/test/releases/shop-manager-0.1.29-windows.exe",
                        "macos", "https://github.com/test/releases/shop-manager-0.1.29-macos.dmg"
                ))
                .releaseNotes("https://github.com/test/releases/tag/v0.1.29")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCurrentVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(response.getLatestVersion()).isEqualTo(LATEST_VERSION);
        assertThat(response.getUpdateAvailable()).isTrue();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getReleaseDate()).isEqualTo("2026-01-06");
        assertThat(response.getDownloadUrls()).isNotEmpty();
        assertThat(response.getReleaseNotesUrl()).contains("v0.1.29");

        verify(restClient).get();
        verify(requestHeadersUriSpec).uri(CLOUD_API_URL + "/api/registration/latest-version");
    }

    @Test
    @DisplayName("Should detect no update when current version equals latest")
    void shouldDetectNoUpdateWhenVersionsEqual() {
        // Arrange
        VersionInfo versionInfo = VersionInfo.builder()
                .version(CURRENT_VERSION)
                .releaseDate("2026-01-05")
                .downloadUrls(Map.of())
                .releaseNotes("https://github.com/test/releases/tag/v0.1.28")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCurrentVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(response.getLatestVersion()).isEqualTo(CURRENT_VERSION);
        assertThat(response.getUpdateAvailable()).isFalse();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    @DisplayName("Should detect no update when current version is higher")
    void shouldDetectNoUpdateWhenCurrentVersionHigher() {
        // Arrange
        ReflectionTestUtils.setField(updateCheckService, "currentVersion", "0.2.0");

        VersionInfo versionInfo = VersionInfo.builder()
                .version("0.1.29")
                .releaseDate("2026-01-06")
                .downloadUrls(Map.of())
                .releaseNotes("https://github.com/test/releases/tag/v0.1.29")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUpdateAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should handle major version updates")
    void shouldHandleMajorVersionUpdate() {
        // Arrange
        VersionInfo versionInfo = VersionInfo.builder()
                .version("1.0.0")
                .releaseDate("2026-01-10")
                .downloadUrls(Map.of())
                .releaseNotes("https://github.com/test/releases/tag/v1.0.0")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUpdateAvailable()).isTrue();
        assertThat(response.getLatestVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should handle minor version updates")
    void shouldHandleMinorVersionUpdate() {
        // Arrange
        VersionInfo versionInfo = VersionInfo.builder()
                .version("0.2.0")
                .releaseDate("2026-01-08")
                .downloadUrls(Map.of())
                .releaseNotes("https://github.com/test/releases/tag/v0.2.0")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUpdateAvailable()).isTrue();
        assertThat(response.getLatestVersion()).isEqualTo("0.2.0");
    }

    @Test
    @DisplayName("Should handle API errors gracefully")
    void shouldHandleApiErrors() {
        // Arrange
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenThrow(new RuntimeException("API connection failed"));

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getErrorMessage()).contains("Error checking for updates");
        assertThat(response.getUpdateAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should handle null response from API")
    void shouldHandleNullResponse() {
        // Arrange
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(null);

        // Act
        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getErrorMessage()).contains("Failed to retrieve version information");
        assertThat(response.getUpdateAvailable()).isFalse();
    }

    @Test
    @DisplayName("Should cache successful update check results")
    void shouldCacheSuccessfulResults() {
        // Arrange
        VersionInfo versionInfo = VersionInfo.builder()
                .version(LATEST_VERSION)
                .releaseDate("2026-01-06")
                .downloadUrls(Map.of())
                .releaseNotes("https://github.com/test/releases/tag/v0.1.29")
                .build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenReturn(versionInfo);

        // Act
        UpdateCheckResponse firstResponse = updateCheckService.checkForUpdates();
        UpdateCheckResponse cachedResponse = updateCheckService.getCachedStatus();

        // Assert
        assertThat(cachedResponse).isNotNull();
        assertThat(cachedResponse.getUpdateAvailable()).isEqualTo(firstResponse.getUpdateAvailable());
        assertThat(cachedResponse.getLatestVersion()).isEqualTo(firstResponse.getLatestVersion());
    }

    @Test
    @DisplayName("Should return null cached status when no check performed")
    void shouldReturnNullWhenNoCacheExists() {
        // Act
        UpdateCheckResponse cachedResponse = updateCheckService.getCachedStatus();

        // Assert
        assertThat(cachedResponse).isNull();
    }

    @Test
    @DisplayName("Should cache error responses")
    void shouldCacheErrorResponses() {
        // Arrange
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(VersionInfo.class)).thenThrow(new RuntimeException("Network error"));

        // Act
        UpdateCheckResponse errorResponse = updateCheckService.checkForUpdates();
        UpdateCheckResponse cachedResponse = updateCheckService.getCachedStatus();

        // Assert
        assertThat(cachedResponse).isNotNull();
        assertThat(cachedResponse.getStatus()).isEqualTo("ERROR");
        assertThat(cachedResponse).isEqualTo(errorResponse);
    }
}
