#!/bin/bash
# ============================================================================
# Shop Manager Standalone Installer
# ============================================================================
#
# This script automates the installation of Shop Manager using Docker Compose
#
# Prerequisites:
#   - Docker Desktop or Docker Engine
#   - Python 3.7+ (for configuration generation)
#
# Usage:
#   ./install.sh
#   ./install.sh --config custom-config.yaml
#   ./install.sh --skip-config  # Use existing generated files
#
# ============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/config.yaml"
SKIP_CONFIG_GENERATION=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --config)
            CONFIG_FILE="$2"
            shift 2
            ;;
        --skip-config)
            SKIP_CONFIG_GENERATION=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --config FILE      Use custom config file (default: config.yaml)"
            echo "  --skip-config      Skip configuration generation"
            echo "  --help             Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# ============================================================================
# Helper Functions
# ============================================================================

print_header() {
    echo ""
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠  $1${NC}"
}

print_info() {
    echo -e "${CYAN}ℹ  $1${NC}"
}

check_command() {
    if ! command -v $1 &> /dev/null; then
        return 1
    fi
    return 0
}

# ============================================================================
# Step 1: Welcome and Prerequisites Check
# ============================================================================

clear
print_header "Shop Manager Standalone Installer"

echo -e "Welcome to the ${CYAN}Shop Manager${NC} installation wizard!"
echo "This installer will set up Shop Manager on your local machine using Docker."
echo ""
echo "Before we begin, you can configure Shop Manager with your business"
echo "details using the Configuration Wizard."
echo ""

read -p "Would you like to configure now? (Y/N) [Y]: " RUN_CONFIG
RUN_CONFIG=${RUN_CONFIG:-Y}

if [[ "$RUN_CONFIG" =~ ^[Yy]$ ]]; then
    echo ""
    print_info "Starting Configuration Wizard..."
    echo ""
    cd "$SCRIPT_DIR/scripts"
    if ./configure.sh; then
        print_success "Configuration complete"
    else
        echo ""
        print_warning "Configuration was cancelled or failed."
        read -p "Continue with installation using default config? (Y/N): " CONTINUE
        if [[ ! "$CONTINUE" =~ ^[Yy]$ ]]; then
            print_error "Installation cancelled"
            exit 1
        fi
    fi
    cd "$SCRIPT_DIR"
fi

echo ""

print_header "Step 1/7: Checking Prerequisites"

# Check Docker
print_info "Checking for Docker..."
if ! check_command docker; then
    print_error "Docker is not installed"
    echo ""
    echo "Please install Docker Desktop first:"
    echo "  macOS:   https://docs.docker.com/desktop/mac/install/"
    echo "  Linux:   https://docs.docker.com/engine/install/"
    echo "  Windows: https://docs.docker.com/desktop/windows/install/"
    echo ""
    exit 1
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    echo ""
    echo "Please start Docker Desktop and try again."
    exit 1
fi

print_success "Docker is installed and running ($(docker --version))"

# Check Docker Compose
print_info "Checking for Docker Compose..."
if ! docker compose version &> /dev/null; then
    print_error "Docker Compose is not available"
    echo ""
    echo "Docker Compose v2 is required. Please update Docker Desktop."
    exit 1
fi

print_success "Docker Compose is available ($(docker compose version --short))"

# Check Python
print_info "Checking for Python..."
if ! check_command python3; then
    print_warning "Python 3 is not installed"
    print_warning "Configuration generation will be skipped"
    SKIP_CONFIG_GENERATION=true
else
    PYTHON_VERSION=$(python3 --version | awk '{print $2}')
    print_success "Python is installed (${PYTHON_VERSION})"

    # Check for required Python packages
    print_info "Checking Python dependencies..."
    if ! python3 -c "import yaml" 2>/dev/null; then
        print_info "Installing PyYAML..."
        python3 -m pip install --quiet PyYAML || {
            print_warning "Could not install PyYAML, configuration generation may fail"
        }
    fi

    if ! python3 -c "import jinja2" 2>/dev/null; then
        print_info "Installing Jinja2..."
        python3 -m pip install --quiet Jinja2 || {
            print_warning "Could not install Jinja2, configuration generation may fail"
        }
    fi

    print_success "Python dependencies are installed"
fi

# ============================================================================
# Step 2: Configuration Generation
# ============================================================================

if [ "$SKIP_CONFIG_GENERATION" = false ]; then
    print_header "Step 2/7: Generating Configuration"

    if [ ! -f "$CONFIG_FILE" ]; then
        print_error "Configuration file not found: $CONFIG_FILE"
        echo ""
        echo "Please create a config.yaml file or use --config to specify a custom file."
        echo "You can use config.yaml as a template."
        exit 1
    fi

    print_info "Using configuration file: $CONFIG_FILE"

    # Run configuration generator
    print_info "Generating configuration files..."

    cd "$SCRIPT_DIR"
    if python3 scripts/generate-config.py --config "$CONFIG_FILE"; then
        print_success "Configuration files generated successfully"
    else
        print_error "Configuration generation failed"
        exit 1
    fi
else
    print_header "Step 2/7: Configuration (Skipped)"
    print_info "Using existing configuration files"
fi

# ============================================================================
# Step 3: Copy Generated Files
# ============================================================================

print_header "Step 3/7: Copying Configuration Files"

GENERATED_DIR="${SCRIPT_DIR}/generated"

if [ ! -d "$GENERATED_DIR" ]; then
    print_error "Generated files directory not found: $GENERATED_DIR"
    echo "Please run configuration generation first (without --skip-config)"
    exit 1
fi

# Copy .env file
if [ -f "${GENERATED_DIR}/.env" ]; then
    print_info "Copying .env file..."
    cp "${GENERATED_DIR}/.env" "${SCRIPT_DIR}/../.env"
    print_success ".env file copied"
else
    print_warning ".env file not found in generated directory"
fi

# Copy Keycloak realm
if [ -f "${GENERATED_DIR}/keycloak-realm.json" ]; then
    print_info "Copying Keycloak realm configuration..."
    mkdir -p "${SCRIPT_DIR}/../docker"
    cp "${GENERATED_DIR}/keycloak-realm.json" "${SCRIPT_DIR}/../docker/keycloak-realm.json"
    print_success "Keycloak realm configuration copied"
else
    print_warning "Keycloak realm file not found in generated directory"
fi

# Copy Docker Compose override if it exists
if [ -f "${GENERATED_DIR}/docker-compose.override.yml" ]; then
    print_info "Copying Docker Compose override..."
    cp "${GENERATED_DIR}/docker-compose.override.yml" "${SCRIPT_DIR}/../docker-compose.override.yml"
    print_success "Docker Compose override copied"
fi

# ============================================================================
# Step 4: Pull Docker Images
# ============================================================================

print_header "Step 4/7: Pulling Docker Images"

print_info "This may take several minutes depending on your internet connection..."

cd "${SCRIPT_DIR}/.."

# Pull images
if docker compose pull; then
    print_success "Docker images pulled successfully"
else
    print_warning "Some images could not be pulled, will try to build locally"
fi

# ============================================================================
# Step 5: Build Custom Images
# ============================================================================

print_header "Step 5/7: Building Application Images"

print_info "Building backend and frontend images..."

if docker compose build; then
    print_success "Application images built successfully"
else
    print_error "Failed to build application images"
    exit 1
fi

# ============================================================================
# Step 6: Start Services
# ============================================================================

print_header "Step 6/7: Starting Services"

print_info "Starting all services..."
print_info "This may take 2-3 minutes for all services to initialize..."

if docker compose up -d; then
    print_success "Services started successfully"
else
    print_error "Failed to start services"
    exit 1
fi

# Wait for services to initialize
print_info "Waiting for services to become healthy..."
sleep 10

# Check service status
echo ""
print_info "Service Status:"
docker compose ps

# ============================================================================
# Step 7: Verification
# ============================================================================

print_header "Step 7/7: Verification"

# Check if key services are running
services_ok=true

if docker compose ps | grep -q "shop-manager-postgres.*Up"; then
    print_success "Database is running"
else
    print_warning "Database may not be ready yet"
    services_ok=false
fi

if docker compose ps | grep -q "shop-manager-keycloak.*Up"; then
    print_success "Keycloak is running"
else
    print_warning "Keycloak may not be ready yet"
    services_ok=false
fi

if docker compose ps | grep -q "shop-manager-backend.*Up"; then
    print_success "Backend is running"
else
    print_warning "Backend may not be ready yet"
    services_ok=false
fi

if docker compose ps | grep -q "shop-manager-frontend.*Up"; then
    print_success "Frontend is running"
else
    print_warning "Frontend may not be ready yet"
    services_ok=false
fi

# ============================================================================
# Installation Complete
# ============================================================================

print_header "🎉 Installation Complete!"

echo ""
echo -e "${GREEN}Shop Manager has been successfully installed!${NC}"
echo ""

# Read domain from .env if it exists
if [ -f "${SCRIPT_DIR}/../.env" ]; then
    DOMAIN=$(grep '^DOMAIN=' "${SCRIPT_DIR}/../.env" | cut -d'=' -f2)
else
    DOMAIN="localhost"
fi

echo "🌐 Access URLs:"
echo "   Frontend:  http://${DOMAIN}:3001"
echo "   Backend:   http://${DOMAIN}:8081"
echo "   API Docs:  http://${DOMAIN}:8081/swagger-ui/index.html"
echo "   Keycloak:  http://${DOMAIN}:8080"
echo ""

# Show test user credentials if enabled
if grep -q "enabled: true" "$CONFIG_FILE" 2>/dev/null; then
    echo "🔑 Test User Credentials:"
    echo "   Email:     admin@shopmanager.com"
    echo "   Password:  admin123"
    echo ""
    print_warning "Test users are enabled! Disable them in production."
    echo ""
fi

if [ "$services_ok" = false ]; then
    print_warning "Some services may still be initializing. Please wait 1-2 minutes."
    echo ""
    echo "To check service status:"
    echo "   docker compose ps"
    echo ""
    echo "To view logs:"
    echo "   docker compose logs -f"
    echo ""
fi

echo "📝 Useful Commands:"
echo "   View logs:       docker compose logs -f"
echo "   Stop services:   docker compose down"
echo "   Restart:         docker compose restart"
echo "   Update:          docker compose pull && docker compose up -d"
echo ""

echo "📚 Documentation:"
echo "   Installation guide:    docs/INSTALL.md"
echo "   Configuration guide:   docs/CUSTOMIZE.md"
echo "   Troubleshooting:       docs/TROUBLESHOOTING.md"
echo ""

# Offer to open browser
echo ""
read -p "Would you like to open the application in your browser now? (y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open "http://${DOMAIN}:3001"
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        xdg-open "http://${DOMAIN}:3001" 2>/dev/null || {
            echo "Please open http://${DOMAIN}:3001 in your browser"
        }
    fi
fi

echo ""
print_success "Installation completed successfully!"
echo ""
