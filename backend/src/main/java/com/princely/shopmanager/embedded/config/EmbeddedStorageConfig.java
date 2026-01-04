package com.princely.shopmanager.embedded.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Storage configuration for embedded mode.
 * Replaces MinIO with local filesystem storage.
 */
@Slf4j
@Configuration
@Profile("embedded")
@ConditionalOnProperty(name = "application.storage.type", havingValue = "filesystem")
public class EmbeddedStorageConfig {

    @Value("${application.storage.location}")
    private String storageLocation;

    @Value("${application.storage.max-file-size}")
    private long maxFileSize;

    @Value("${application.storage.allowed-extensions}")
    private String allowedExtensions;

    @Bean
    public FileSystemStorageService fileSystemStorageService() throws IOException {
        Path storagePath = Paths.get(storageLocation).toAbsolutePath().normalize();

        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.info("Created storage directory: {}", storagePath);
        }

        List<String> extensions = Arrays.asList(allowedExtensions.split(","));

        return new FileSystemStorageService(storagePath, maxFileSize, extensions);
    }

    /**
     * File system storage service
     */
    public static class FileSystemStorageService {
        private final Path rootLocation;
        private final long maxFileSize;
        private final List<String> allowedExtensions;

        public FileSystemStorageService(Path rootLocation, long maxFileSize, List<String> allowedExtensions) {
            this.rootLocation = rootLocation;
            this.maxFileSize = maxFileSize;
            this.allowedExtensions = allowedExtensions;
        }

        public Path getRootLocation() {
            return rootLocation;
        }

        public long getMaxFileSize() {
            return maxFileSize;
        }

        public List<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        /**
         * Check if file extension is allowed
         */
        public boolean isExtensionAllowed(String filename) {
            if (filename == null || !filename.contains(".")) {
                return false;
            }
            String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            return allowedExtensions.contains(extension);
        }

        /**
         * Validate file size
         */
        public boolean isFileSizeValid(long size) {
            return size > 0 && size <= maxFileSize;
        }

        /**
         * Get storage path for a specific category
         */
        public Path getCategoryPath(String category) throws IOException {
            Path categoryPath = rootLocation.resolve(category).normalize();
            if (!Files.exists(categoryPath)) {
                Files.createDirectories(categoryPath);
            }
            return categoryPath;
        }

        /**
         * Get full path for a file
         */
        public Path getFilePath(String category, String filename) {
            return rootLocation.resolve(category).resolve(filename).normalize();
        }
    }
}
