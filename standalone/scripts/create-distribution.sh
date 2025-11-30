#!/bin/bash
# ============================================================================
# Shop Manager Distribution Package Creator
# ============================================================================
#
# This script creates distribution packages for Shop Manager standalone
#
# Usage:
#   ./create-distribution.sh --version 1.0.0
#   ./create-distribution.sh --version 1.0.0 --include-images
#   ./create-distribution.sh --version 1.0.0 --platform windows
#
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Default configuration
VERSION=""
INCLUDE_IMAGES=false
PLATFORM="all"  # all, windows, macos, linux
OUTPUT_DIR="dist"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --version)
            VERSION="$2"
            shift 2
            ;;
        --include-images)
            INCLUDE_IMAGES=true
            shift
            ;;
        --platform)
            PLATFORM="$2"
            shift 2
            ;;
        --output)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --version VERSION      Package version (required)"
            echo "  --include-images       Include Docker images for offline install"
            echo "  --platform PLATFORM    Target platform: all, windows, macos, linux"
            echo "  --output DIR           Output directory (default: dist)"
            echo "  --help                 Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate version
if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: Version is required${NC}"
    echo "Usage: $0 --version 1.0.0"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
STANDALONE_DIR="${PROJECT_ROOT}/standalone"

# Helper functions
print_header() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠  $1${NC}"
}

# ============================================================================
# Main Script
# ============================================================================

print_header "Shop Manager Distribution Package Creator v${VERSION}"

# Create output directory
mkdir -p "${OUTPUT_DIR}"

# ============================================================================
# 1. Create Docker Compose ZIP Package
# ============================================================================

print_header "Creating Docker Compose Package"

PACKAGE_NAME="shop-manager-standalone-v${VERSION}"
TEMP_DIR="${OUTPUT_DIR}/tmp/${PACKAGE_NAME}"

# Clean and create temp directory
rm -rf "${TEMP_DIR}"
mkdir -p "${TEMP_DIR}"

print_info "Copying files to temporary directory..."

# Copy main files
cp "${STANDALONE_DIR}/config.yaml" "${TEMP_DIR}/"
cp "${STANDALONE_DIR}/install.sh" "${TEMP_DIR}/"
cp "${STANDALONE_DIR}/install.bat" "${TEMP_DIR}/"
cp "${STANDALONE_DIR}/requirements.txt" "${TEMP_DIR}/"
cp "${STANDALONE_DIR}/README.md" "${TEMP_DIR}/"
cp "${STANDALONE_DIR}/DISTRIBUTION.md" "${TEMP_DIR}/"
cp "${PROJECT_ROOT}/LICENSE" "${TEMP_DIR}/" 2>/dev/null || echo "MIT License" > "${TEMP_DIR}/LICENSE"

# Copy directories (excluding dist to avoid recursive copy)
mkdir -p "${TEMP_DIR}/scripts"
rsync -a --exclude='dist' "${STANDALONE_DIR}/scripts/" "${TEMP_DIR}/scripts/"
cp -r "${STANDALONE_DIR}/templates" "${TEMP_DIR}/"
cp -r "${STANDALONE_DIR}/docs" "${TEMP_DIR}/"

# Copy Docker Compose files from project root
cp "${PROJECT_ROOT}/docker-compose.yml" "${TEMP_DIR}/"

# Create a version file
echo "${VERSION}" > "${TEMP_DIR}/VERSION"

print_success "Files copied"

# ============================================================================
# 2. Download Docker Images (if requested)
# ============================================================================

if [ "$INCLUDE_IMAGES" = true ]; then
    print_header "Downloading Docker Images for Offline Installation"

    mkdir -p "${TEMP_DIR}/docker-images"

    print_info "This may take 10-15 minutes depending on your internet speed..."

    # Pull images
    print_info "Pulling images..."
    cd "${PROJECT_ROOT}"
    docker compose pull

    # Save images
    print_info "Saving backend image..."
    docker save shop-manager-backend:latest | gzip > "${TEMP_DIR}/docker-images/backend.tar.gz"

    print_info "Saving frontend image..."
    docker save shop-manager-frontend:latest | gzip > "${TEMP_DIR}/docker-images/frontend.tar.gz"

    print_info "Saving PostgreSQL image..."
    docker save postgres:15-alpine | gzip > "${TEMP_DIR}/docker-images/postgres.tar.gz"

    print_info "Saving Keycloak image..."
    docker save quay.io/keycloak/keycloak:24.0.1 | gzip > "${TEMP_DIR}/docker-images/keycloak.tar.gz"

    print_info "Saving Kafka image..."
    docker save confluentinc/cp-kafka:7.5.0 | gzip > "${TEMP_DIR}/docker-images/kafka.tar.gz"

    print_info "Saving MinIO image..."
    docker save minio/minio:RELEASE.2024-03-07T00-43-48Z | gzip > "${TEMP_DIR}/docker-images/minio.tar.gz"

    # Create load script
    cat > "${TEMP_DIR}/docker-images/load-images.sh" <<'EOF'
#!/bin/bash
echo "Loading Docker images..."
for img in *.tar.gz; do
    echo "Loading $img..."
    docker load -i "$img"
done
echo "All images loaded successfully!"
EOF
    chmod +x "${TEMP_DIR}/docker-images/load-images.sh"

    print_success "Docker images saved"
fi

# ============================================================================
# 3. Create ZIP Archive
# ============================================================================

print_header "Creating ZIP Archive"

cd "${OUTPUT_DIR}/tmp"

if [ "$INCLUDE_IMAGES" = true ]; then
    ZIP_NAME="${PACKAGE_NAME}-full.zip"
else
    ZIP_NAME="${PACKAGE_NAME}.zip"
fi

print_info "Creating ${ZIP_NAME}..."
zip -r "${ZIP_NAME}" "${PACKAGE_NAME}" > /dev/null

mv "${ZIP_NAME}" "../"

print_success "ZIP package created: ${OUTPUT_DIR}/${ZIP_NAME}"

# Get file size
FILE_SIZE=$(du -h "${OUTPUT_DIR}/${ZIP_NAME}" | cut -f1)
print_info "Package size: ${FILE_SIZE}"

# ============================================================================
# 4. Create Checksums
# ============================================================================

print_header "Creating Checksums"

cd "${OUTPUT_DIR}"

# SHA256
sha256sum "${ZIP_NAME}" > "${ZIP_NAME}.sha256"
print_success "SHA256: $(cat ${ZIP_NAME}.sha256 | cut -d' ' -f1)"

# MD5 (for compatibility)
md5sum "${ZIP_NAME}" > "${ZIP_NAME}.md5" 2>/dev/null || md5 -r "${ZIP_NAME}" | awk '{print $1 "  " $2}' > "${ZIP_NAME}.md5"
print_success "MD5: $(cat ${ZIP_NAME}.md5 | cut -d' ' -f1)"

# ============================================================================
# 5. Build Electron Apps (if Node.js available)
# ============================================================================

if command -v node &> /dev/null && [ -f "${STANDALONE_DIR}/electron-app/package.json" ]; then
    print_header "Building Electron Desktop Apps"

    cd "${STANDALONE_DIR}/electron-app"

    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_info "Installing dependencies..."
        npm install
    fi

    # Update version in package.json
    print_info "Updating version to ${VERSION}..."
    node -e "const pkg=require('./package.json');pkg.version='${VERSION}';require('fs').writeFileSync('package.json',JSON.stringify(pkg,null,2))"

    # Build based on platform
    case "$PLATFORM" in
        windows)
            print_info "Building Windows installer..."
            npm run build:win
            ;;
        macos)
            print_info "Building macOS installer..."
            npm run build:mac
            ;;
        linux)
            print_info "Building Linux packages..."
            npm run build:linux
            ;;
        all)
            print_info "Building for all platforms (this may take several minutes)..."
            npm run build:all
            ;;
    esac

    # Copy built files to output directory
    if [ -d "dist" ]; then
        print_info "Copying Electron build artifacts..."
        cp -r dist/* "${OUTPUT_DIR}/"
        print_success "Electron apps built and copied to ${OUTPUT_DIR}"
    fi

else
    print_warning "Node.js not found or electron-app not configured, skipping Electron builds"
    print_info "To build Electron apps, install Node.js and run 'npm install' in standalone/electron-app"
fi

# ============================================================================
# 6. Create Release Notes
# ============================================================================

print_header "Creating Release Notes"

cat > "${OUTPUT_DIR}/RELEASE_NOTES_v${VERSION}.md" <<EOF
# Shop Manager v${VERSION} - Release Notes

## 📦 Distribution Packages

### Docker Compose (Multi-platform)

**Lightweight** (online installation):
- \`${PACKAGE_NAME}.zip\` ($(du -h "${OUTPUT_DIR}/${PACKAGE_NAME}.zip" 2>/dev/null | cut -f1 || echo "~50 MB"))
- Requires internet connection for Docker image download
- Installation time: 15-20 minutes

$(if [ "$INCLUDE_IMAGES" = true ]; then
echo "**Full Package** (offline installation):
- \`${PACKAGE_NAME}-full.zip\` ($(du -h "${OUTPUT_DIR}/${PACKAGE_NAME}-full.zip" 2>/dev/null | cut -f1 || echo "~2 GB"))
- Includes all Docker images
- No internet required after download
- Installation time: 10-15 minutes"
fi)

### Electron Desktop Apps

$(if [ "$PLATFORM" = "all" ] || [ "$PLATFORM" = "windows" ]; then
echo "**Windows:**
- \`Shop Manager-Setup-${VERSION}.exe\` (NSIS installer)
- Portable: \`Shop Manager-${VERSION}.exe\`"
fi)

$(if [ "$PLATFORM" = "all" ] || [ "$PLATFORM" = "macos" ]; then
echo "**macOS:**
- \`Shop Manager-${VERSION}.dmg\` (macOS 11+)
- Universal binary (Intel + Apple Silicon)"
fi)

$(if [ "$PLATFORM" = "all" ] || [ "$PLATFORM" = "linux" ]; then
echo "**Linux:**
- \`Shop Manager-${VERSION}.AppImage\` (Universal)
- \`shop-manager_${VERSION}_amd64.deb\` (Debian/Ubuntu)
- \`shop-manager-${VERSION}.x86_64.rpm\` (RHEL/Fedora)"
fi)

## 🚀 Quick Start

### Docker Compose Installation

\`\`\`bash
# Extract package
unzip ${PACKAGE_NAME}.zip
cd ${PACKAGE_NAME}

# Run installer
./install.sh  # Linux/macOS
# OR
install.bat   # Windows

# Access application
# Frontend: http://localhost:3001
# Login: admin@shopmanager.com / admin123
\`\`\`

### Desktop App Installation

1. Download installer for your platform
2. Double-click to run
3. Follow setup wizard (4 steps, ~5 minutes)
4. Launch from system tray

## ✨ What's New

- Complete standalone distribution system
- Kubernetes-level customization via config.yaml
- Visual setup wizard (Electron app)
- Cross-platform installers (Windows/macOS/Linux)
- Offline installation support
- SSL certificate auto-generation
- System tray integration

## 📋 System Requirements

**Minimum:**
- OS: Windows 10+, macOS 11+, Ubuntu 20.04+
- RAM: 8 GB (4 GB available)
- Disk: 20 GB free space
- CPU: 2 cores
- Docker Desktop 4.0+ or Docker Engine 20+

**Recommended:**
- RAM: 16 GB
- Disk: 50 GB SSD
- CPU: 4 cores
- Broadband internet (for initial setup)

## 🔐 Security

**Checksums:**
- SHA256: \`$(cat ${OUTPUT_DIR}/${ZIP_NAME}.sha256 | cut -d' ' -f1)\`
- MD5: \`$(cat ${OUTPUT_DIR}/${ZIP_NAME}.md5 | cut -d' ' -f1)\`

**Verification:**
\`\`\`bash
# Verify SHA256
sha256sum -c ${ZIP_NAME}.sha256

# Verify MD5
md5sum -c ${ZIP_NAME}.md5
\`\`\`

## 📞 Support

- Documentation: See README.md
- Issues: https://github.com/yourorg/shop-manager/issues
- Email: support@shopmanager.com
- Community: https://discord.gg/shopmanager

## 📄 License

See LICENSE file for details.

---

**Released:** $(date +"%Y-%m-%d")
**Build:** $(git rev-parse --short HEAD 2>/dev/null || echo "standalone")
EOF

print_success "Release notes created: ${OUTPUT_DIR}/RELEASE_NOTES_v${VERSION}.md"

# ============================================================================
# Cleanup
# ============================================================================

print_header "Cleaning Up"

rm -rf "${OUTPUT_DIR}/tmp"
print_success "Temporary files cleaned"

# ============================================================================
# Summary
# ============================================================================

print_header "Distribution Packages Created Successfully!"

echo ""
echo "📦 Packages:"
ls -lh "${OUTPUT_DIR}"/*.zip 2>/dev/null || true
ls -lh "${OUTPUT_DIR}"/*.exe 2>/dev/null || true
ls -lh "${OUTPUT_DIR}"/*.dmg 2>/dev/null || true
ls -lh "${OUTPUT_DIR}"/*.AppImage 2>/dev/null || true
ls -lh "${OUTPUT_DIR}"/*.deb 2>/dev/null || true
ls -lh "${OUTPUT_DIR}"/*.rpm 2>/dev/null || true

echo ""
echo "📄 Documentation:"
ls -1 "${OUTPUT_DIR}"/*.md 2>/dev/null || true

echo ""
echo "🔐 Checksums:"
ls -1 "${OUTPUT_DIR}"/*.sha256 2>/dev/null || true
ls -1 "${OUTPUT_DIR}"/*.md5 2>/dev/null || true

echo ""
echo -e "${GREEN}All files are in: ${OUTPUT_DIR}${NC}"
echo ""
echo "Next steps:"
echo "  1. Test installation on fresh machines"
echo "  2. Upload to GitHub Releases or your website"
echo "  3. Update download links"
echo "  4. Announce release!"
echo ""
