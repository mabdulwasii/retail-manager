#!/bin/bash
# ============================================================================
# Shop Manager - Placeholder Asset Generator
# ============================================================================
# Creates basic placeholder assets for installers
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Create directories
create_directories() {
    mkdir -p ../windows/assets
    mkdir -p ../macos/assets
    mkdir -p ../linux/assets
    print_success "Created asset directories"
}

# Generate Linux PNG (256x256)
generate_linux_png() {
    print_header "Generating Linux PNG Icon"

    if command -v convert &> /dev/null; then
        convert -size 256x256 -background "#4A90E2" -fill white \
            -gravity center -font Arial-Bold -pointsize 120 \
            label:"SM" \
            ../linux/assets/shop-manager.png
        print_success "Created shop-manager.png (256x256)"
    else
        print_warning "ImageMagick not found - skipping PNG generation"
        print_info "Install: brew install imagemagick (macOS) or sudo apt-get install imagemagick (Linux)"
    fi

    echo ""
}

# Generate macOS ICNS
generate_macos_icns() {
    print_header "Generating macOS ICNS Icon"

    if [[ "$OSTYPE" == "darwin"* ]]; then
        # Create temporary PNG if not exists
        TEMP_PNG="temp-icon.png"

        if command -v convert &> /dev/null; then
            convert -size 1024x1024 -background "#4A90E2" -fill white \
                -gravity center -font Arial-Bold -pointsize 480 \
                label:"SM" \
                "$TEMP_PNG"
            print_success "Created temporary PNG"
        else
            print_warning "ImageMagick not found - cannot create base icon"
            print_info "Install: brew install imagemagick"
            echo ""
            return
        fi

        # Create iconset
        mkdir -p shop-manager.iconset

        sips -z 16 16     "$TEMP_PNG" --out shop-manager.iconset/icon_16x16.png
        sips -z 32 32     "$TEMP_PNG" --out shop-manager.iconset/icon_16x16@2x.png
        sips -z 32 32     "$TEMP_PNG" --out shop-manager.iconset/icon_32x32.png
        sips -z 64 64     "$TEMP_PNG" --out shop-manager.iconset/icon_32x32@2x.png
        sips -z 128 128   "$TEMP_PNG" --out shop-manager.iconset/icon_128x128.png
        sips -z 256 256   "$TEMP_PNG" --out shop-manager.iconset/icon_128x128@2x.png
        sips -z 256 256   "$TEMP_PNG" --out shop-manager.iconset/icon_256x256.png
        sips -z 512 512   "$TEMP_PNG" --out shop-manager.iconset/icon_256x256@2x.png
        sips -z 512 512   "$TEMP_PNG" --out shop-manager.iconset/icon_512x512.png
        sips -z 1024 1024 "$TEMP_PNG" --out shop-manager.iconset/icon_512x512@2x.png

        print_success "Created iconset with all sizes"

        # Convert to ICNS
        iconutil -c icns shop-manager.iconset -o ../macos/assets/shop-manager.icns
        print_success "Created shop-manager.icns"

        # Cleanup
        rm -rf shop-manager.iconset "$TEMP_PNG"
        print_success "Cleaned up temporary files"
    else
        print_warning "Not running on macOS - skipping ICNS generation"
        print_info "ICNS files can only be created on macOS using iconutil"
        print_info "Alternative: Use online converter at https://cloudconvert.com/png-to-icns"
    fi

    echo ""
}

# Generate Windows ICO
generate_windows_ico() {
    print_header "Generating Windows ICO Icon"

    if command -v convert &> /dev/null; then
        # Create temporary PNG
        TEMP_PNG="temp-icon.png"
        convert -size 256x256 -background "#4A90E2" -fill white \
            -gravity center -font Arial-Bold -pointsize 120 \
            label:"SM" \
            "$TEMP_PNG"

        # Convert to ICO with multiple resolutions
        convert "$TEMP_PNG" \
            -define icon:auto-resize=256,128,96,64,48,32,16 \
            ../windows/assets/shop-manager.ico

        print_success "Created shop-manager.ico (multi-resolution)"

        # Cleanup
        rm "$TEMP_PNG"
    else
        print_warning "ImageMagick not found - skipping ICO generation"
        print_info "Install: brew install imagemagick (macOS) or choco install imagemagick (Windows)"
        print_info "Alternative: Use online converter at https://convertio.co/png-ico/"
    fi

    echo ""
}

# Generate Windows wizard images
generate_windows_wizard() {
    print_header "Generating Windows Wizard Images"

    if command -v convert &> /dev/null; then
        # Wizard large image (164x314)
        convert -size 164x314 -background "#4A90E2" \
            -fill white -gravity center \
            -font Arial-Bold -pointsize 24 \
            label:"Shop\nManager" \
            ../windows/assets/wizard-image.bmp
        print_success "Created wizard-image.bmp (164x314)"

        # Wizard small image (55x58)
        convert -size 55x58 -background "#4A90E2" \
            -fill white -gravity center \
            -font Arial-Bold -pointsize 18 \
            label:"SM" \
            ../windows/assets/wizard-small.bmp
        print_success "Created wizard-small.bmp (55x58)"
    else
        print_warning "ImageMagick not found - skipping wizard image generation"
    fi

    echo ""
}

# Generate macOS DMG background
generate_dmg_background() {
    print_header "Generating macOS DMG Background"

    if command -v convert &> /dev/null; then
        convert -size 800x400 -background "#f8f9fa" \
            -fill "#333333" -gravity center \
            -font Arial-Bold -pointsize 48 \
            label:"Shop Manager\n\nDrag to Applications" \
            ../macos/assets/dmg-background.png
        print_success "Created dmg-background.png (800x400)"
    else
        print_warning "ImageMagick not found - skipping DMG background generation"
    fi

    echo ""
}

# Display summary
display_summary() {
    print_header "Asset Generation Complete"

    echo ""
    echo -e "${GREEN}Placeholder assets generated successfully!${NC}"
    echo ""
    echo -e "${BLUE}Generated Assets:${NC}"
    echo ""

    if [ -f "../linux/assets/shop-manager.png" ]; then
        echo -e "${GREEN}✓${NC} Linux:   installers/linux/assets/shop-manager.png"
    else
        echo -e "${YELLOW}⚠${NC} Linux:   installers/linux/assets/shop-manager.png (not created)"
    fi

    if [ -f "../macos/assets/shop-manager.icns" ]; then
        echo -e "${GREEN}✓${NC} macOS:   installers/macos/assets/shop-manager.icns"
    else
        echo -e "${YELLOW}⚠${NC} macOS:   installers/macos/assets/shop-manager.icns (not created)"
    fi

    if [ -f "../macos/assets/dmg-background.png" ]; then
        echo -e "${GREEN}✓${NC} macOS:   installers/macos/assets/dmg-background.png"
    else
        echo -e "${YELLOW}⚠${NC} macOS:   installers/macos/assets/dmg-background.png (not created)"
    fi

    if [ -f "../windows/assets/shop-manager.ico" ]; then
        echo -e "${GREEN}✓${NC} Windows: installers/windows/assets/shop-manager.ico"
    else
        echo -e "${YELLOW}⚠${NC} Windows: installers/windows/assets/shop-manager.ico (not created)"
    fi

    if [ -f "../windows/assets/wizard-image.bmp" ]; then
        echo -e "${GREEN}✓${NC} Windows: installers/windows/assets/wizard-image.bmp"
    else
        echo -e "${YELLOW}⚠${NC} Windows: installers/windows/assets/wizard-image.bmp (not created)"
    fi

    if [ -f "../windows/assets/wizard-small.bmp" ]; then
        echo -e "${GREEN}✓${NC} Windows: installers/windows/assets/wizard-small.bmp"
    else
        echo -e "${YELLOW}⚠${NC} Windows: installers/windows/assets/wizard-small.bmp (not created)"
    fi

    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo ""
    echo "1. Review generated placeholders"
    echo "2. Replace with custom branded assets (optional)"
    echo "3. Build installers with: ../build-installers.sh"
    echo ""
    echo -e "${YELLOW}Note:${NC} These are basic placeholders. For production, replace with professionally designed assets."
    echo ""
}

# Main execution
main() {
    clear
    print_header "Shop Manager - Placeholder Asset Generator"
    echo ""

    create_directories
    generate_linux_png
    generate_macos_icns
    generate_windows_ico
    generate_windows_wizard
    generate_dmg_background
    display_summary
}

# Run main
main
