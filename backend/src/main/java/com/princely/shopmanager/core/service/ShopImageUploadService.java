package com.princely.shopmanager.core.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for handling image uploads and file management operations.
 *
 * This service provides:
 * - Image file validation
 * - File upload handling
 * - File system operations
 * - Image type validation
 */
@Service
@Slf4j
public class ShopImageUploadService {

    // Configuration for file uploads
    private static final String UPLOAD_DIR = "uploads/shop-assets/";
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/svg+xml"};

    /**
     * Uploads an image file and returns the URL path.
     *
     * @param shopId The shop ID for naming the file
     * @param imageFile The image file to upload
     * @param imageType The type of image (e.g., "logo", "banner", "favicon")
     * @return The URL path to the uploaded image
     * @throws IOException if file upload fails
     * @throws IllegalArgumentException if file is invalid
     */
    public String uploadImage(String shopId, MultipartFile imageFile, String imageType) throws IOException {
        log.info("Uploading {} image for shop: {}", imageType, shopId);

        // Validate file
        validateImageFile(imageFile, imageType);

        // Generate unique filename
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = shopId + "_" + imageType + "_" + UUID.randomUUID() + "." + fileExtension;

        // Save file to filesystem
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String imageUrl = "/api/assets/" + uniqueFilename;
        log.info("{} image uploaded successfully for shop: {} at path: {}", imageType, shopId, imageUrl);

        return imageUrl;
    }

    /**
     * Validates an uploaded image file.
     *
     * @param file The file to validate
     * @param fileType The type of file being uploaded
     * @throws IllegalArgumentException if file is invalid
     */
    public void validateImageFile(MultipartFile file, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(fileType + " file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(fileType + " file size cannot exceed " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException(fileType + " file must be a valid image (JPEG, PNG, GIF, or SVG)");
        }
    }

    /**
     * Checks if the content type is allowed for image uploads.
     *
     * @param contentType The content type to check
     * @return true if the content type is allowed, false otherwise
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts file extension from filename.
     *
     * @param filename The filename to extract extension from
     * @return The file extension in lowercase
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}