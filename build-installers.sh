#!/bin/bash
# ============================================================================
# Shop Manager - Master Installer Build Script
# ============================================================================
# This script orchestrates building all platform-specific installers
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
VERSION="1.0.0"
BUILD_DIR="build/installers"

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

# Detect platform
detect_platform() {
    case "$OSTYPE" in
        linux*)   PLATFORM="linux" ;;
        darwin*)  PLATFORM="macos" ;;
        msys*|cygwin*|win32) PLATFORM="windows" ;;
        *)        PLATFORM="unknown" ;;
    esac
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    print_success "Java is installed: $(java -version 2>&1 | head -1)"

    # Check Maven
    if [ ! -f "backend/mvnw" ]; then
        print_error "Maven wrapper not found in backend directory"
        exit 1
    fi
    print_success "Maven wrapper found"

    echo ""
}

# Build embedded JAR
build_embedded_jar() {
    print_header "Building Embedded JAR"

    if [ -f "backend/target/shop-manager-*-embedded.jar" ]; then
        print_info "Embedded JAR already exists"
        read -p "Rebuild? (y/N): " rebuild
        if [[ ! $rebuild =~ ^[Yy]$ ]]; then
            print_info "Skipping JAR build"
            echo ""
            return
        fi
    fi

    print_info "Building embedded JAR with Maven..."
    cd backend
    ./mvnw clean package -Pembedded -DskipTests
    cd ..

    if [ -f "backend/target/shop-manager-"*"-embedded.jar" ]; then
        JAR_SIZE=$(du -h backend/target/shop-manager-*-embedded.jar | awk '{print $1}')
        print_success "Embedded JAR built successfully ($JAR_SIZE)"
    else
        print_error "Embedded JAR build failed"
        exit 1
    fi

    echo ""
}

# Build Windows installer
build_windows() {
    print_header "Building Windows Installer"

    if [ "$PLATFORM" != "windows" ]; then
        print_warning "Not running on Windows, skipping Windows installer"
        print_info "To build Windows installer:"
        print_info "  1. Transfer installers/windows/ to Windows machine"
        print_info "  2. Install Inno Setup: https://jrsoftware.org/isinfo.php"
        print_info "  3. Open shop-manager.iss in Inno Setup Compiler"
        print_info "  4. Click Build → Compile"
        echo ""
        return
    fi

    cd installers/windows

    if command -v iscc &> /dev/null; then
        print_info "Building with Inno Setup..."
        iscc shop-manager.iss
        print_success "Windows installer built"
    else
        print_error "Inno Setup not found"
        print_info "Install from: https://jrsoftware.org/isinfo.php"
    fi

    cd ../..
    echo ""
}

# Build macOS installer
build_macos() {
    print_header "Building macOS Installer"

    if [ "$PLATFORM" != "macos" ]; then
        print_warning "Not running on macOS, skipping macOS installer"
        print_info "To build macOS installer:"
        print_info "  1. Transfer project to macOS machine"
        print_info "  2. Run: cd installers/macos && ./build-dmg.sh"
        echo ""
        return
    fi

    cd installers/macos
    chmod +x build-dmg.sh
    ./build-dmg.sh
    cd ../..

    echo ""
}

# Build Linux packages
build_linux() {
    print_header "Building Linux Packages"

    cd installers/linux
    chmod +x build-packages.sh
    ./build-packages.sh
    cd ../..

    echo ""
}

# Display summary
display_summary() {
    print_header "Build Summary"

    echo ""
    echo -e "${GREEN}Installer build process complete!${NC}"
    echo ""
    echo -e "${BLUE}Platform: ${PLATFORM}${NC}"
    echo ""

    if [ -d "$BUILD_DIR" ]; then
        echo -e "${BLUE}Generated Installers:${NC}"
        echo ""

        # Windows
        if [ -d "$BUILD_DIR/windows" ] && [ "$(ls -A $BUILD_DIR/windows 2>/dev/null)" ]; then
            echo -e "${YELLOW}Windows:${NC}"
            ls -lh "$BUILD_DIR/windows" | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'
            echo ""
        fi

        # macOS
        if [ -d "$BUILD_DIR/macos" ] && [ "$(ls -A $BUILD_DIR/macos 2>/dev/null)" ]; then
            echo -e "${YELLOW}macOS:${NC}"
            ls -lh "$BUILD_DIR/macos" | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'
            echo ""
        fi

        # Linux
        if [ -d "$BUILD_DIR/linux" ] && [ "$(ls -A $BUILD_DIR/linux 2>/dev/null)" ]; then
            echo -e "${YELLOW}Linux:${NC}"
            ls -lh "$BUILD_DIR/linux" | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'
            echo ""
        fi
    else
        print_warning "No installers built"
    fi

    echo -e "${BLUE}Next Steps:${NC}"
    echo ""
    echo -e "1. ${YELLOW}Test installers on target platforms${NC}"
    echo -e "2. ${YELLOW}Upload to GitHub releases:${NC}"
    echo -e "   ${BLUE}gh release create v${VERSION} $BUILD_DIR/**/*${NC}"
    echo ""
    echo -e "3. ${YELLOW}Update documentation with download links${NC}"
    echo ""
}

# Main execution
main() {
    clear
    print_header "Shop Manager - Installer Build System"
    echo -e "${BLUE}Version: ${VERSION}${NC}"
    echo ""

    detect_platform
    print_info "Detected platform: $PLATFORM"
    echo ""

    check_prerequisites
    build_embedded_jar

    # Provide build options
    echo -e "${BLUE}Build Options:${NC}"
    echo -e "  ${YELLOW}1${NC} - Build installers for current platform only"
    echo -e "  ${YELLOW}2${NC} - Build all available installers"
    echo -e "  ${YELLOW}3${NC} - Build embedded JAR only (already done)"
    echo ""
    read -p "Select option (1-3) [1]: " option
    option=${option:-1}

    case $option in
        1)
            case $PLATFORM in
                windows)
                    build_windows
                    ;;
                macos)
                    build_macos
                    ;;
                linux)
                    build_linux
                    ;;
                *)
                    print_error "Unknown platform: $PLATFORM"
                    exit 1
                    ;;
            esac
            ;;
        2)
            build_windows
            build_macos
            build_linux
            ;;
        3)
            print_info "Embedded JAR build complete"
            ;;
        *)
            print_error "Invalid option: $option"
            exit 1
            ;;
    esac

    display_summary
}

# Run main
main
