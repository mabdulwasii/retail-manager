#!/bin/bash

# ============================================================================
# lite-init.sh - Initialization Script for Docker Compose Lite
# ============================================================================
# This script sets up the environment for running Shop Manager in lite mode
# ============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Generate a secure random secret
generate_secret() {
    openssl rand -base64 64 | tr -d '\n'
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker Desktop or Docker Engine."
        exit 1
    fi
    print_success "Docker is installed"

    # Check Docker Compose
    if ! docker compose version &> /dev/null; then
        print_error "Docker Compose is not installed or not available."
        exit 1
    fi
    print_success "Docker Compose is available"

    # Check if Docker is running
    if ! docker info &> /dev/null; then
        print_error "Docker is not running. Please start Docker."
        exit 1
    fi
    print_success "Docker is running"

    echo ""
}

# Create directory structure
create_directories() {
    print_header "Creating Directory Structure"

    directories=(
        "./data"
        "./data/h2"
        "./data/uploads"
        "./data/logs"
        "./data/backups"
    )

    for dir in "${directories[@]}"; do
        if [ ! -d "$dir" ]; then
            mkdir -p "$dir"
            print_success "Created directory: $dir"
        else
            print_info "Directory already exists: $dir"
        fi
    done

    # Set permissions
    chmod 755 ./data
    chmod 755 ./data/h2
    chmod 755 ./data/uploads
    chmod 755 ./data/logs
    chmod 755 ./data/backups

    print_success "Directory permissions set"
    echo ""
}

# Create or update .env.lite file
create_env_file() {
    print_header "Creating Environment Configuration"

    if [ -f ".env.lite" ]; then
        print_warning ".env.lite already exists. Creating backup..."
        cp .env.lite .env.lite.backup.$(date +%Y%m%d_%H%M%S)
        print_success "Backup created"
    fi

    # Generate JWT secret
    JWT_SECRET=$(generate_secret)

    cat > .env.lite <<EOF
# ============================================================================
# Shop Manager - Docker Compose Lite Configuration
# ============================================================================
# Generated on: $(date)
# ============================================================================

# Application Version
APP_VERSION=1.0.0

# Port Configuration
BACKEND_PORT=8081
FRONTEND_PORT=3001

# Data Directory
DATA_DIR=./data

# H2 Database Configuration
H2_PASSWORD=
H2_CONSOLE_ENABLED=false

# JWT Authentication (IMPORTANT: Keep this secret secure!)
JWT_SECRET=${JWT_SECRET}

# Cloud Sync Configuration (Optional)
CLOUD_SYNC_ENABLED=false
CLOUD_API_URL=
CLOUD_API_KEY=
STORE_ID=
SYNC_CRON=0 0 * * * ?
ANONYMIZE_PII=false

# ============================================================================
# Advanced Configuration (Usually no need to change)
# ============================================================================

# JVM Memory Settings
JAVA_OPTS=-Xms256m -Xmx512m

EOF

    print_success ".env.lite file created"
    print_warning "IMPORTANT: Keep your .env.lite file secure (contains JWT_SECRET)"
    echo ""
}

# Build embedded JAR
build_embedded_jar() {
    print_header "Building Embedded JAR"

    if [ ! -f "backend/target/shop-manager-1.0.0-SNAPSHOT-embedded.jar" ]; then
        print_info "Embedded JAR not found. Building..."
        cd backend
        ./mvnw clean package -Pembedded -DskipTests
        cd ..
        print_success "Embedded JAR built successfully"
    else
        print_info "Embedded JAR already exists. Skipping build."
        read -p "Do you want to rebuild? (y/N): " rebuild
        if [[ $rebuild =~ ^[Yy]$ ]]; then
            cd backend
            ./mvnw clean package -Pembedded -DskipTests
            cd ..
            print_success "Embedded JAR rebuilt successfully"
        fi
    fi
    echo ""
}

# Verify configuration
verify_configuration() {
    print_header "Verifying Configuration"

    if [ ! -f ".env.lite" ]; then
        print_error ".env.lite file not found"
        exit 1
    fi
    print_success ".env.lite file found"

    if [ ! -f "docker-compose-lite.yml" ]; then
        print_error "docker-compose-lite.yml file not found"
        exit 1
    fi
    print_success "docker-compose-lite.yml file found"

    if [ ! -f "backend/Dockerfile.lite" ]; then
        print_error "backend/Dockerfile.lite not found"
        exit 1
    fi
    print_success "backend/Dockerfile.lite found"

    if [ ! -f "frontend/Dockerfile.lite" ]; then
        print_error "frontend/Dockerfile.lite not found"
        exit 1
    fi
    print_success "frontend/Dockerfile.lite found"

    echo ""
}

# Display next steps
display_next_steps() {
    print_header "Setup Complete!"

    echo ""
    echo -e "${GREEN}Your Shop Manager Lite environment is ready!${NC}"
    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo ""
    echo -e "1. ${YELLOW}Review configuration:${NC}"
    echo -e "   ${BLUE}nano .env.lite${NC}"
    echo ""
    echo -e "2. ${YELLOW}Start the application:${NC}"
    echo -e "   ${BLUE}docker compose -f docker-compose-lite.yml --env-file .env.lite up -d${NC}"
    echo ""
    echo -e "3. ${YELLOW}Access the application:${NC}"
    echo -e "   Frontend: ${GREEN}http://localhost:3001${NC}"
    echo -e "   Backend:  ${GREEN}http://localhost:8081${NC}"
    echo ""
    echo -e "4. ${YELLOW}Check logs:${NC}"
    echo -e "   ${BLUE}docker compose -f docker-compose-lite.yml logs -f${NC}"
    echo ""
    echo -e "5. ${YELLOW}Stop the application:${NC}"
    echo -e "   ${BLUE}docker compose -f docker-compose-lite.yml down${NC}"
    echo ""
    echo -e "${YELLOW}📖 For more information, see:${NC} docs/DOCKER_LITE_DEPLOYMENT.md"
    echo ""
}

# Main execution
main() {
    clear
    print_header "Shop Manager - Docker Compose Lite Setup"
    echo ""

    check_prerequisites
    create_directories
    create_env_file
    build_embedded_jar
    verify_configuration
    display_next_steps
}

# Run main function
main
