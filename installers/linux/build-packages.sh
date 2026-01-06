#!/bin/bash
# ============================================================================
# Shop Manager - Linux Package Builder Script
# ============================================================================
# This script creates Linux packages (.deb, .rpm, AppImage)
# ============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
APP_NAME="shop-manager"
APP_VERSION="${APP_VERSION:-1.0.0}"
JAR_VERSION="${JAR_VERSION:-1.0.0-SNAPSHOT}"
APP_DESCRIPTION="Retail Management Platform"
MAINTAINER="Princely Software <support@shopmanager.com>"
JAR_FILE="../../backend/target/shop-manager-${JAR_VERSION}-embedded.jar"
OUTPUT_DIR="../../build/installers/linux"

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

    # Check Linux
    if [[ "$OSTYPE" != "linux"* ]]; then
        print_warning "Not running on Linux. Some packages may not build correctly."
    fi

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

    # Check package tools
    if command -v dpkg-deb &> /dev/null; then
        print_success "dpkg-deb found (.deb support)"
        DEB_SUPPORT=true
    else
        print_warning "dpkg-deb not found (.deb packages will be skipped)"
        print_info "Install with: sudo apt-get install dpkg"
        DEB_SUPPORT=false
    fi

    if command -v rpmbuild &> /dev/null; then
        print_success "rpmbuild found (.rpm support)"
        RPM_SUPPORT=true
    else
        print_warning "rpmbuild not found (.rpm packages will be skipped)"
        print_info "Install with: sudo yum install rpm-build (RHEL) or sudo apt-get install rpm (Debian)"
        RPM_SUPPORT=false
    fi

    echo ""
}

# Clean previous build
clean_build() {
    print_header "Cleaning Previous Build"

    rm -rf build
    mkdir -p build
    mkdir -p "$OUTPUT_DIR"

    print_success "Build directory cleaned"
    echo ""
}

# Build .deb package
build_deb() {
    if [ "$DEB_SUPPORT" = false ]; then
        print_warning "Skipping .deb package (dpkg-deb not available)"
        return
    fi

    print_header "Building .deb Package"

    DEB_DIR="build/deb/${APP_NAME}_${APP_VERSION}"

    # Create directory structure
    mkdir -p "${DEB_DIR}/DEBIAN"
    mkdir -p "${DEB_DIR}/opt/${APP_NAME}/lib"
    mkdir -p "${DEB_DIR}/opt/${APP_NAME}/bin"
    mkdir -p "${DEB_DIR}/opt/${APP_NAME}/config"
    mkdir -p "${DEB_DIR}/etc/${APP_NAME}"
    mkdir -p "${DEB_DIR}/usr/share/applications"
    mkdir -p "${DEB_DIR}/usr/share/pixmaps"
    mkdir -p "${DEB_DIR}/usr/share/doc/${APP_NAME}"
    mkdir -p "${DEB_DIR}/lib/systemd/system"

    # Copy JAR
    cp "$JAR_FILE" "${DEB_DIR}/opt/${APP_NAME}/lib/${APP_NAME}.jar"
    print_success "Copied JAR"

    # Copy launcher script
    cp scripts/shop-manager "${DEB_DIR}/opt/${APP_NAME}/bin/"
    chmod +x "${DEB_DIR}/opt/${APP_NAME}/bin/shop-manager"
    print_success "Copied launcher script"

    # Copy configuration
    cp config/.env.template "${DEB_DIR}/etc/${APP_NAME}/shop-manager.env"
    cp config/application.yml "${DEB_DIR}/etc/${APP_NAME}/"
    print_success "Copied configuration"

    # Copy documentation
    cp ../../docs/EMBEDDED_DEPLOYMENT.md "${DEB_DIR}/usr/share/doc/${APP_NAME}/"
    cp ../../docs/CLOUD_SYNC_SETUP.md "${DEB_DIR}/usr/share/doc/${APP_NAME}/"
    cp ../../README.md "${DEB_DIR}/usr/share/doc/${APP_NAME}/"
    print_success "Copied documentation"

    # Create desktop entry
    cat > "${DEB_DIR}/usr/share/applications/${APP_NAME}.desktop" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=Shop Manager
Comment=${APP_DESCRIPTION}
Exec=/opt/${APP_NAME}/bin/shop-manager
Icon=${APP_NAME}
Terminal=false
Categories=Office;Finance;
Keywords=retail;pos;sales;inventory;
StartupNotify=true
EOF
    print_success "Created desktop entry"

    # Copy icon (if exists)
    if [ -f "assets/shop-manager.png" ]; then
        cp assets/shop-manager.png "${DEB_DIR}/usr/share/pixmaps/${APP_NAME}.png"
        print_success "Copied icon"
    fi

    # Create systemd service
    cat > "${DEB_DIR}/lib/systemd/system/${APP_NAME}.service" <<EOF
[Unit]
Description=Shop Manager
After=network.target

[Service]
Type=simple
User=shopmanager
Group=shopmanager
WorkingDirectory=/opt/${APP_NAME}
EnvironmentFile=/etc/${APP_NAME}/shop-manager.env
ExecStart=/usr/bin/java \${JAVA_OPTS} -Dspring.profiles.active=embedded -jar /opt/${APP_NAME}/lib/${APP_NAME}.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

[Install]
WantedBy=multi-user.target
EOF
    print_success "Created systemd service"

    # Create control file
    INSTALLED_SIZE=$(du -sk "${DEB_DIR}/opt/${APP_NAME}" | cut -f1)
    cat > "${DEB_DIR}/DEBIAN/control" <<EOF
Package: ${APP_NAME}
Version: ${APP_VERSION}
Section: misc
Priority: optional
Architecture: all
Depends: openjdk-21-jre-headless | openjdk-21-jre | default-jre (>= 2:1.21)
Installed-Size: ${INSTALLED_SIZE}
Maintainer: ${MAINTAINER}
Description: ${APP_DESCRIPTION}
 Shop Manager is a comprehensive retail management platform with support
 for sales, inventory, investments, and analytics. Features embedded H2
 database and optional cloud sync for multi-location businesses.
Homepage: https://github.com/yourorg/shop-manager
EOF
    print_success "Created control file"

    # Create postinst script
    cat > "${DEB_DIR}/DEBIAN/postinst" <<EOF
#!/bin/bash
set -e

# Create shopmanager user if not exists
if ! id shopmanager &>/dev/null; then
    useradd --system --home-dir /opt/${APP_NAME} --shell /bin/false shopmanager
fi

# Create data directory
mkdir -p /var/lib/${APP_NAME}/data/h2
mkdir -p /var/lib/${APP_NAME}/data/uploads
mkdir -p /var/lib/${APP_NAME}/data/logs
mkdir -p /var/lib/${APP_NAME}/data/backups

# Set permissions
chown -R shopmanager:shopmanager /opt/${APP_NAME}
chown -R shopmanager:shopmanager /var/lib/${APP_NAME}
chmod 755 /opt/${APP_NAME}/bin/shop-manager
chmod 600 /etc/${APP_NAME}/shop-manager.env

# Generate JWT secret if not exists
if grep -q "REPLACE_WITH_SECURE_RANDOM_SECRET" /etc/${APP_NAME}/shop-manager.env; then
    JWT_SECRET=\$(openssl rand -base64 64 | tr -d '\\n')
    sed -i "s|JWT_SECRET=REPLACE_WITH_SECURE_RANDOM_SECRET|JWT_SECRET=\$JWT_SECRET|" /etc/${APP_NAME}/shop-manager.env
fi

# Reload systemd
systemctl daemon-reload

echo ""
echo "Shop Manager installed successfully!"
echo ""
echo "To start Shop Manager:"
echo "  sudo systemctl start ${APP_NAME}"
echo ""
echo "To enable auto-start on boot:"
echo "  sudo systemctl enable ${APP_NAME}"
echo ""
echo "To configure:"
echo "  sudo nano /etc/${APP_NAME}/shop-manager.env"
echo ""

exit 0
EOF
    chmod 755 "${DEB_DIR}/DEBIAN/postinst"
    print_success "Created postinst script"

    # Create prerm script
    cat > "${DEB_DIR}/DEBIAN/prerm" <<EOF
#!/bin/bash
set -e

# Stop service if running
systemctl stop ${APP_NAME} || true
systemctl disable ${APP_NAME} || true

exit 0
EOF
    chmod 755 "${DEB_DIR}/DEBIAN/prerm"
    print_success "Created prerm script"

    # Build .deb package
    dpkg-deb --build --root-owner-group "${DEB_DIR}" "${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb"
    print_success "Built .deb package: ${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb"

    echo ""
}

# Build .rpm package
build_rpm() {
    if [ "$RPM_SUPPORT" = false ]; then
        print_warning "Skipping .rpm package (rpmbuild not available)"
        return
    fi

    print_header "Building .rpm Package"

    RPM_BUILD_DIR="build/rpm"
    mkdir -p "${RPM_BUILD_DIR}"/{BUILD,RPMS,SOURCES,SPECS,SRPMS}

    # Create spec file
    cat > "${RPM_BUILD_DIR}/SPECS/${APP_NAME}.spec" <<EOF
Name:           ${APP_NAME}
Version:        ${APP_VERSION}
Release:        1%{?dist}
Summary:        ${APP_DESCRIPTION}
License:        MIT
URL:            https://github.com/yourorg/shop-manager
Requires:       java-21-openjdk-headless
BuildArch:      noarch

%description
Shop Manager is a comprehensive retail management platform with support
for sales, inventory, investments, and analytics. Features embedded H2
database and optional cloud sync for multi-location businesses.

%prep

%build

%install
mkdir -p %{buildroot}/opt/${APP_NAME}/lib
mkdir -p %{buildroot}/opt/${APP_NAME}/bin
mkdir -p %{buildroot}/etc/${APP_NAME}
mkdir -p %{buildroot}/usr/share/applications
mkdir -p %{buildroot}/usr/share/pixmaps
mkdir -p %{buildroot}/lib/systemd/system
mkdir -p %{buildroot}/var/lib/${APP_NAME}/data/{h2,uploads,logs,backups}

# Copy files
cp ${PWD}/$JAR_FILE %{buildroot}/opt/${APP_NAME}/lib/${APP_NAME}.jar
cp ${PWD}/scripts/shop-manager %{buildroot}/opt/${APP_NAME}/bin/
chmod +x %{buildroot}/opt/${APP_NAME}/bin/shop-manager

cp ${PWD}/config/.env.template %{buildroot}/etc/${APP_NAME}/shop-manager.env
cp ${PWD}/config/application.yml %{buildroot}/etc/${APP_NAME}/

# Desktop entry
cat > %{buildroot}/usr/share/applications/${APP_NAME}.desktop <<DESKTOP
[Desktop Entry]
Version=1.0
Type=Application
Name=Shop Manager
Comment=${APP_DESCRIPTION}
Exec=/opt/${APP_NAME}/bin/shop-manager
Icon=${APP_NAME}
Terminal=false
Categories=Office;Finance;
Keywords=retail;pos;sales;inventory;
StartupNotify=true
DESKTOP

# Systemd service
cat > %{buildroot}/lib/systemd/system/${APP_NAME}.service <<SERVICE
[Unit]
Description=Shop Manager
After=network.target

[Service]
Type=simple
User=shopmanager
Group=shopmanager
WorkingDirectory=/opt/${APP_NAME}
EnvironmentFile=/etc/${APP_NAME}/shop-manager.env
ExecStart=/usr/bin/java \\\${JAVA_OPTS} -Dspring.profiles.active=embedded -jar /opt/${APP_NAME}/lib/${APP_NAME}.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${APP_NAME}

[Install]
WantedBy=multi-user.target
SERVICE

%files
/opt/${APP_NAME}/lib/${APP_NAME}.jar
/opt/${APP_NAME}/bin/shop-manager
/etc/${APP_NAME}/shop-manager.env
/etc/${APP_NAME}/application.yml
/usr/share/applications/${APP_NAME}.desktop
/lib/systemd/system/${APP_NAME}.service
%dir /var/lib/${APP_NAME}
%dir /var/lib/${APP_NAME}/data

%pre
getent group shopmanager >/dev/null || groupadd -r shopmanager
getent passwd shopmanager >/dev/null || useradd -r -g shopmanager -d /opt/${APP_NAME} -s /sbin/nologin -c "Shop Manager" shopmanager

%post
# Generate JWT secret if not exists
if grep -q "REPLACE_WITH_SECURE_RANDOM_SECRET" /etc/${APP_NAME}/shop-manager.env; then
    JWT_SECRET=\$(openssl rand -base64 64 | tr -d '\\n')
    sed -i "s|JWT_SECRET=REPLACE_WITH_SECURE_RANDOM_SECRET|JWT_SECRET=\$JWT_SECRET|" /etc/${APP_NAME}/shop-manager.env
fi

# Set permissions
chown -R shopmanager:shopmanager /opt/${APP_NAME}
chown -R shopmanager:shopmanager /var/lib/${APP_NAME}
chmod 600 /etc/${APP_NAME}/shop-manager.env

# Reload systemd
systemctl daemon-reload

echo ""
echo "Shop Manager installed successfully!"
echo "To start: sudo systemctl start ${APP_NAME}"
echo "To enable: sudo systemctl enable ${APP_NAME}"
echo "To configure: sudo nano /etc/${APP_NAME}/shop-manager.env"
echo ""

%preun
systemctl stop ${APP_NAME} || true
systemctl disable ${APP_NAME} || true

%changelog
* $(date "+%a %b %d %Y") Princely Software <support@shopmanager.com> - ${APP_VERSION}-1
- Initial release
EOF
    print_success "Created RPM spec file"

    # Build RPM
    rpmbuild --define "_topdir ${PWD}/${RPM_BUILD_DIR}" \
             -bb "${RPM_BUILD_DIR}/SPECS/${APP_NAME}.spec"

    # Copy to output directory
    cp "${RPM_BUILD_DIR}/RPMS/noarch/${APP_NAME}-${APP_VERSION}-1."*.rpm "${OUTPUT_DIR}/"
    print_success "Built .rpm package: ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-1.*.rpm"

    echo ""
}

# Build AppImage
build_appimage() {
    print_header "Building AppImage"

    print_warning "AppImage support is experimental and requires appimagetool"
    print_info "For now, creating a tarball instead"

    APPDIR="build/appimage/${APP_NAME}.AppDir"

    # Create AppDir structure
    mkdir -p "${APPDIR}/usr/bin"
    mkdir -p "${APPDIR}/usr/share/applications"
    mkdir -p "${APPDIR}/usr/share/icons/hicolor/256x256/apps"
    mkdir -p "${APPDIR}/usr/lib/${APP_NAME}"
    mkdir -p "${APPDIR}/usr/share/${APP_NAME}/config"

    # Copy JAR
    cp "$JAR_FILE" "${APPDIR}/usr/lib/${APP_NAME}/${APP_NAME}.jar"
    print_success "Copied JAR"

    # Copy launcher
    cp scripts/shop-manager "${APPDIR}/usr/bin/"
    chmod +x "${APPDIR}/usr/bin/shop-manager"
    print_success "Copied launcher"

    # Copy config
    cp config/.env.template "${APPDIR}/usr/share/${APP_NAME}/config/"
    cp config/application.yml "${APPDIR}/usr/share/${APP_NAME}/config/"
    print_success "Copied configuration"

    # Create desktop entry
    cat > "${APPDIR}/usr/share/applications/${APP_NAME}.desktop" <<EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=Shop Manager
Comment=${APP_DESCRIPTION}
Exec=shop-manager
Icon=${APP_NAME}
Terminal=false
Categories=Office;Finance;
EOF

    # Create AppRun
    cat > "${APPDIR}/AppRun" <<'EOF'
#!/bin/bash
HERE="$(dirname "$(readlink -f "${0}")")"
export PATH="${HERE}/usr/bin:${PATH}"
exec "${HERE}/usr/bin/shop-manager" "$@"
EOF
    chmod +x "${APPDIR}/AppRun"
    print_success "Created AppRun"

    # Create tarball (instead of AppImage for now)
    tar -czf "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz" \
        -C "build/appimage" "${APP_NAME}.AppDir"
    print_success "Created AppImage tarball: ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz"

    echo ""
}

# Verify packages
verify_packages() {
    print_header "Verifying Packages"

    if [ -f "${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb" ]; then
        DEB_SIZE=$(du -h "${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb" | awk '{print $1}')
        print_success ".deb package: $DEB_SIZE"
    fi

    if ls "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-1."*.rpm 1> /dev/null 2>&1; then
        RPM_SIZE=$(du -h "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-1."*.rpm | awk '{print $1}')
        print_success ".rpm package: $RPM_SIZE"
    fi

    if [ -f "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz" ]; then
        APP_SIZE=$(du -h "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz" | awk '{print $1}')
        print_success "AppImage tarball: $APP_SIZE"
    fi

    echo ""
}

# Display next steps
display_next_steps() {
    print_header "Build Complete!"

    echo ""
    echo -e "${GREEN}Linux packages created successfully!${NC}"
    echo ""
    echo -e "${BLUE}Output Directory:${NC} ${OUTPUT_DIR}"
    echo ""
    echo -e "${BLUE}Created Packages:${NC}"
    ls -lh "$OUTPUT_DIR" | tail -n +2 | awk '{print "  " $9 " (" $5 ")"}'
    echo ""
    echo -e "${BLUE}Installation Commands:${NC}"
    echo ""
    if [ -f "${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb" ]; then
        echo -e "${YELLOW}Debian/Ubuntu (.deb):${NC}"
        echo -e "  ${BLUE}sudo dpkg -i ${OUTPUT_DIR}/${APP_NAME}_${APP_VERSION}_all.deb${NC}"
        echo ""
    fi
    if ls "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-1."*.rpm 1> /dev/null 2>&1; then
        echo -e "${YELLOW}RHEL/Fedora (.rpm):${NC}"
        echo -e "  ${BLUE}sudo rpm -i ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-1.*.rpm${NC}"
        echo ""
    fi
    if [ -f "${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz" ]; then
        echo -e "${YELLOW}AppImage:${NC}"
        echo -e "  ${BLUE}tar -xzf ${OUTPUT_DIR}/${APP_NAME}-${APP_VERSION}-x86_64.AppImage.tar.gz${NC}"
        echo ""
    fi
}

# Main execution
main() {
    clear
    print_header "Shop Manager - Linux Package Builder"
    echo ""

    check_prerequisites
    clean_build
    build_deb
    build_rpm
    build_appimage
    verify_packages
    display_next_steps
}

# Run main
main
