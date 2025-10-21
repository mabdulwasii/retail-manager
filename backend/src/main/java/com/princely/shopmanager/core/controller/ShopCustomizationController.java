package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.ShopCustomization;
import com.princely.shopmanager.core.dto.ShopCustomizationRequest;
import com.princely.shopmanager.core.dto.ShopCustomizationResponse;
import com.princely.shopmanager.core.service.ShopCustomizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * REST Controller for shop customization and branding operations.
 *
 * This controller provides comprehensive customization management endpoints including:
 * - CRUD operations for shop customization settings
 * - Color scheme and theme management
 * - Logo and image uploads
 * - Receipt customization
 * - UI preferences and layout settings
 *
 * All endpoints are secured and require appropriate authentication and authorization.
 *
 * @author Shop Manager Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/shops/{shopId}/customization")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shop Customization", description = "Operations for managing shop branding and visual customization")
@SecurityRequirement(name = "bearerAuth")
public class ShopCustomizationController {

    private final ShopCustomizationService customizationService;

    /**
     * Retrieves the customization settings for a specific shop.
     *
     * @param shopId Shop ID
     * @return Shop customization settings or 404 if not configured
     */
    @Operation(
        summary = "Get shop customization",
        description = "Retrieves the customization settings for a shop including colors, logos, themes, and layout preferences."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customization settings retrieved successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found or customization not configured",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> getCustomization(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId
    ) {
        log.debug("Retrieving customization for shop: {}", shopId);
        Optional<ShopCustomization> customization = customizationService.getShopCustomization(shopId);

        return customization
            .map(c -> ResponseEntity.ok(ShopCustomizationResponse.fromEntity(c)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates or updates customization settings for a shop.
     *
     * @param shopId Shop ID
     * @param request Customization settings to apply
     * @return Updated customization settings
     */
    @Operation(
        summary = "Update shop customization",
        description = "Creates or updates customization settings for a shop. Supports partial updates - only provided fields are updated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customization updated successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid customization data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires OWNER or MANAGER role"
        )
    })
    @PutMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> updateCustomization(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Valid @RequestBody ShopCustomizationRequest request
    ) {
        log.info("Updating customization for shop: {}", shopId);

        // Get or create customization
        Optional<ShopCustomization> existingCustomization = customizationService.getShopCustomization(shopId);
        ShopCustomization customization;

        if (existingCustomization.isPresent()) {
            customization = existingCustomization.get();
            request.applyTo(customization);
        } else {
            customization = ShopCustomization.builder().build();
            request.applyTo(customization);
        }

        ShopCustomization saved = customizationService.saveShopCustomization(shopId, customization);
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(saved));
    }

    /**
     * Updates the color scheme for a shop.
     *
     * @param shopId Shop ID
     * @param primaryColor Primary brand color
     * @param secondaryColor Secondary brand color
     * @param accentColor Accent color
     * @return Updated customization
     */
    @Operation(
        summary = "Update color scheme",
        description = "Updates the color scheme (primary, secondary, and accent colors) for a shop."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Color scheme updated successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid color format",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @PatchMapping("/colors")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> updateColorScheme(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "Primary color", example = "#007bff")
        @RequestParam(required = false) String primaryColor,
        @Parameter(description = "Secondary color", example = "#6c757d")
        @RequestParam(required = false) String secondaryColor,
        @Parameter(description = "Accent color", example = "#28a745")
        @RequestParam(required = false) String accentColor
    ) {
        log.info("Updating color scheme for shop: {}", shopId);
        ShopCustomization updated = customizationService.updateColorScheme(
            shopId, primaryColor, secondaryColor, accentColor
        );
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(updated));
    }

    /**
     * Updates theme settings for a shop.
     *
     * @param shopId Shop ID
     * @param themeVariant Theme variant (LIGHT, DARK, AUTO)
     * @param fontSize Font size (SMALL, MEDIUM, LARGE)
     * @return Updated customization
     */
    @Operation(
        summary = "Update theme settings",
        description = "Updates theme variant and font size preferences for a shop."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Theme settings updated successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid theme settings",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @PatchMapping("/theme")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> updateThemeSettings(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "Theme variant", example = "LIGHT")
        @RequestParam(required = false) ShopCustomization.ThemeVariant themeVariant,
        @Parameter(description = "Font size", example = "MEDIUM")
        @RequestParam(required = false) ShopCustomization.FontSize fontSize
    ) {
        log.info("Updating theme settings for shop: {}", shopId);
        ShopCustomization updated = customizationService.updateThemeSettings(shopId, themeVariant, fontSize);
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(updated));
    }

    /**
     * Uploads a logo for a shop.
     *
     * @param shopId Shop ID
     * @param file Logo file
     * @return Updated customization with new logo URL
     */
    @Operation(
        summary = "Upload shop logo",
        description = "Uploads a logo image for a shop. Accepts common image formats (PNG, JPG, SVG)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logo uploaded successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid file format or size",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> uploadLogo(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "Logo file")
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        log.info("Uploading logo for shop: {}", shopId);
        ShopCustomization updated = customizationService.uploadLogo(shopId, file);
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(updated));
    }

    /**
     * Updates contact information for a shop.
     *
     * @param shopId Shop ID
     * @param websiteUrl Website URL
     * @param socialMediaLinks Social media links (JSON)
     * @return Updated customization
     */
    @Operation(
        summary = "Update contact information",
        description = "Updates website URL and social media links for a shop."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contact information updated successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @PatchMapping("/contact")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<ShopCustomizationResponse> updateContactInfo(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId,
        @Parameter(description = "Website URL", example = "https://www.myshop.com")
        @RequestParam(required = false) String websiteUrl,
        @Parameter(description = "Social media links (JSON)")
        @RequestParam(required = false) String socialMediaLinks
    ) {
        log.info("Updating contact info for shop: {}", shopId);
        ShopCustomization updated = customizationService.updateContactInfo(shopId, websiteUrl, socialMediaLinks);
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(updated));
    }

    /**
     * Resets customization to default settings.
     *
     * @param shopId Shop ID
     * @return Default customization settings
     */
    @Operation(
        summary = "Reset to default customization",
        description = "Resets all customization settings to default values."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customization reset successfully",
            content = @Content(schema = @Schema(implementation = ShopCustomizationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Shop not found",
            content = @Content(schema = @Schema(implementation = String.class))
        )
    })
    @DeleteMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('OWNER')")
    public ResponseEntity<ShopCustomizationResponse> resetToDefaults(
        @Parameter(description = "Shop ID", example = "shop-123e4567-e89b-12d3-a456-426614174000")
        @PathVariable String shopId
    ) {
        log.info("Resetting customization to defaults for shop: {}", shopId);
        ShopCustomization reset = customizationService.resetToDefaults(shopId);
        return ResponseEntity.ok(ShopCustomizationResponse.fromEntity(reset));
    }
}
