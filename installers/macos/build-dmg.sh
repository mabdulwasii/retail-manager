#!/bin/bash
# ============================================================================
# Shop Manager - macOS DMG Builder Script
# ============================================================================
# This script creates a macOS disk image (.dmg) installer
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
APP_NAME="Shop Manager"
APP_VERSION="${APP_VERSION:-1.0.0}"
JAR_VERSION="${JAR_VERSION:-1.0.0-SNAPSHOT}"
JAR_FILE="../../backend/target/shop-manager-${JAR_VERSION}-embedded.jar"
DMG_OUTPUT_DIR="../../build/installers/macos"
DMG_FILENAME="shop-manager-${APP_VERSION}-macos-x64.dmg"
APP_BUNDLE_DIR="build/${APP_NAME}.app"
VOLUME_NAME="${APP_NAME} ${APP_VERSION}"

# Functions
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check macOS
    if [[ "$OSTYPE" != "darwin"* ]]; then
        print_error "This script must run on macOS"
        exit 1
    fi
    print_success "Running on macOS"

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    print_success "Java is installed"

    # Check embedded JAR
    if [ ! -f "$JAR_FILE" ]; then
        print_error "Embedded JAR not found: $JAR_FILE"
        print_info "Build it with: cd backend && ./mvnw clean package -Pembedded -DskipTests"
        exit 1
    fi
    print_success "Embedded JAR found"

    # Check create-dmg (optional but recommended)
    if ! command -v create-dmg &> /dev/null; then
        print_warning "create-dmg not found. Install with: brew install create-dmg"
        print_info "Will use hdiutil instead (basic DMG without custom background)"
    else
        print_success "create-dmg is installed"
    fi

    echo ""
}

# Clean previous build
clean_build() {
    print_header "Cleaning Previous Build"

    if [ -d "build" ]; then
        rm -rf build
        print_success "Removed build directory"
    fi

    mkdir -p build
    print_success "Created build directory"

    echo ""
}

# Create macOS app bundle
create_app_bundle() {
    print_header "Creating macOS App Bundle"

    # Create app bundle structure
    mkdir -p "${APP_BUNDLE_DIR}/Contents/MacOS"
    mkdir -p "${APP_BUNDLE_DIR}/Contents/Resources"
    mkdir -p "${APP_BUNDLE_DIR}/Contents/Resources/config"
    mkdir -p "${APP_BUNDLE_DIR}/Contents/Resources/docs"
    print_success "Created app bundle structure"

    # Copy JAR
    cp "$JAR_FILE" "${APP_BUNDLE_DIR}/Contents/Resources/shop-manager.jar"
    print_success "Copied embedded JAR"

    # Copy launcher script
    cp scripts/shop-manager "${APP_BUNDLE_DIR}/Contents/MacOS/shop-manager"
    chmod +x "${APP_BUNDLE_DIR}/Contents/MacOS/shop-manager"
    print_success "Copied launcher script"

    # Copy configuration templates
    cp config/.env.template "${APP_BUNDLE_DIR}/Contents/Resources/config/"
    cp config/application.yml "${APP_BUNDLE_DIR}/Contents/Resources/config/"
    print_success "Copied configuration templates"

    # Copy documentation
    cp ../../docs/EMBEDDED_DEPLOYMENT.md "${APP_BUNDLE_DIR}/Contents/Resources/docs/"
    cp ../../docs/CLOUD_SYNC_SETUP.md "${APP_BUNDLE_DIR}/Contents/Resources/docs/"
    cp ../../README.md "${APP_BUNDLE_DIR}/Contents/Resources/docs/"
    print_success "Copied documentation"

    # Copy icon (if exists)
    if [ -f "assets/shop-manager.icns" ]; then
        cp assets/shop-manager.icns "${APP_BUNDLE_DIR}/Contents/Resources/"
        print_success "Copied application icon"
    else
        print_warning "Icon not found: assets/shop-manager.icns"
    fi

    # Create Info.plist
    cat > "${APP_BUNDLE_DIR}/Contents/Info.plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>${APP_NAME}</string>
    <key>CFBundleDisplayName</key>
    <string>${APP_NAME}</string>
    <key>CFBundleIdentifier</key>
    <string>com.princely.shopmanager</string>
    <key>CFBundleVersion</key>
    <string>${APP_VERSION}</string>
    <key>CFBundleShortVersionString</key>
    <string>${APP_VERSION}</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleSignature</key>
    <string>SMGR</string>
    <key>CFBundleExecutable</key>
    <string>shop-manager</string>
    <key>CFBundleIconFile</key>
    <string>shop-manager.icns</string>
    <key>LSMinimumSystemVersion</key>
    <string>11.0</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>LSApplicationCategoryType</key>
    <string>public.app-category.business</string>
    <key>NSHumanReadableCopyright</key>
    <string>Copyright © 2025 Princely Software. All rights reserved.</string>
</dict>
</plist>
EOF
    print_success "Created Info.plist"

    # Create PkgInfo
    echo -n "APPLSMGR" > "${APP_BUNDLE_DIR}/Contents/PkgInfo"
    print_success "Created PkgInfo"

    echo ""
}

# Create DMG using create-dmg (recommended)
create_dmg_with_tool() {
    print_header "Creating DMG with create-dmg"

    mkdir -p "$DMG_OUTPUT_DIR"

    create-dmg \
        --volname "$VOLUME_NAME" \
        --volicon "assets/shop-manager.icns" \
        --window-pos 200 120 \
        --window-size 800 400 \
        --icon-size 100 \
        --icon "${APP_NAME}.app" 200 190 \
        --hide-extension "${APP_NAME}.app" \
        --app-drop-link 600 185 \
        --background "assets/dmg-background.png" \
        "${DMG_OUTPUT_DIR}/${DMG_FILENAME}" \
        "build/"

    print_success "DMG created: ${DMG_OUTPUT_DIR}/${DMG_FILENAME}"
    echo ""
}

# Create basic DMG using hdiutil
create_dmg_with_hdiutil() {
    print_header "Creating DMG with hdiutil"

    mkdir -p "$DMG_OUTPUT_DIR"

    # Create temporary DMG
    TEMP_DMG="${DMG_OUTPUT_DIR}/temp.dmg"

    hdiutil create -srcfolder "build/" -volname "$VOLUME_NAME" -fs HFS+ \
        -fsargs "-c c=64,a=16,e=16" -format UDRW -size 500m "$TEMP_DMG"

    print_success "Created temporary DMG"

    # Mount temporary DMG
    # Parse output to get mount point - look for line with /Volumes/ path
    MOUNT_OUTPUT=$(hdiutil attach -readwrite -noverify -noautoopen "$TEMP_DMG")
    MOUNT_DIR=$(echo "$MOUNT_OUTPUT" | grep "/Volumes/" | awk '{for(i=3;i<=NF;i++) printf "%s%s", $i, (i<NF ? " " : "\n")}')

    if [ -z "$MOUNT_DIR" ]; then
        print_error "Failed to mount DMG or parse mount directory"
        print_error "hdiutil output: $MOUNT_OUTPUT"
        exit 1
    fi

    print_success "Mounted DMG: $MOUNT_DIR"

    # Create symbolic link to Applications
    ln -s /Applications "$MOUNT_DIR/Applications"
    print_success "Created Applications symlink"

    # Unmount
    hdiutil detach "$MOUNT_DIR"
    if [ $? -ne 0 ]; then
        print_error "Failed to unmount DMG"
        exit 1
    fi
    print_success "Unmounted DMG"

    # Convert to compressed read-only
    hdiutil convert "$TEMP_DMG" -format UDZO -imagekey zlib-level=9 \
        -o "${DMG_OUTPUT_DIR}/${DMG_FILENAME}"

    print_success "Converted to compressed DMG"

    # Cleanup
    rm "$TEMP_DMG"
    print_success "Cleaned up temporary files"

    print_success "DMG created: ${DMG_OUTPUT_DIR}/${DMG_FILENAME}"
    echo ""
}

# Sign the app bundle (optional, requires Apple Developer account)
sign_app_bundle() {
    if [ -n "$SIGNING_IDENTITY" ]; then
        print_header "Signing App Bundle"

        codesign --deep --force --verify --verbose \
            --sign "$SIGNING_IDENTITY" \
            "${APP_BUNDLE_DIR}"

        print_success "App bundle signed"
        echo ""
    fi
}

# Verify DMG
verify_dmg() {
    print_header "Verifying DMG"

    if [ -f "${DMG_OUTPUT_DIR}/${DMG_FILENAME}" ]; then
        DMG_SIZE=$(du -h "${DMG_OUTPUT_DIR}/${DMG_FILENAME}" | awk '{print $1}')
        print_success "DMG size: $DMG_SIZE"

        # Verify DMG integrity
        hdiutil verify "${DMG_OUTPUT_DIR}/${DMG_FILENAME}" > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            print_success "DMG verification passed"
        else
            print_error "DMG verification failed"
        fi
    else
        print_error "DMG not found"
    fi

    echo ""
}

# Display next steps
display_next_steps() {
    print_header "Build Complete!"

    echo ""
    echo -e "${GREEN}macOS installer created successfully!${NC}"
    echo ""
    echo -e "${BLUE}Output:${NC}"
    echo -e "  ${DMG_OUTPUT_DIR}/${DMG_FILENAME}"
    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo ""
    echo -e "1. ${YELLOW}Test the DMG:${NC}"
    echo -e "   ${BLUE}open ${DMG_OUTPUT_DIR}/${DMG_FILENAME}${NC}"
    echo ""
    echo -e "2. ${YELLOW}Distribute the DMG:${NC}"
    echo -e "   - Upload to GitHub releases"
    echo -e "   - Share download link with users"
    echo ""
    echo -e "3. ${YELLOW}For production (optional):${NC}"
    echo -e "   - Sign with Apple Developer ID"
    echo -e "   - Notarize with Apple"
    echo -e "   - See: https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution"
    echo ""
}

# Main execution
main() {
    clear
    print_header "Shop Manager - macOS DMG Builder"
    echo ""

    check_prerequisites
    clean_build
    create_app_bundle

    # Try to use create-dmg first, fall back to hdiutil
    if command -v create-dmg &> /dev/null; then
        if [ -f "assets/dmg-background.png" ]; then
            create_dmg_with_tool
        else
            print_warning "DMG background not found, using hdiutil"
            create_dmg_with_hdiutil
        fi
    else
        create_dmg_with_hdiutil
    fi

    verify_dmg
    display_next_steps
}

# Run main
main
