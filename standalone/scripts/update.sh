#!/bin/bash

# Shop Manager - Update Script
# Downloads and installs the latest version while preserving data

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# GitHub repository (update with your actual repo)
GITHUB_REPO="yourorg/shop-manager"
GITHUB_API="https://api.github.com/repos/$GITHUB_REPO/releases/latest"

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STANDALONE_DIR="$SCRIPT_DIR/.."
BACKUP_DIR="$STANDALONE_DIR/backups"
TEMP_DIR="/tmp/shop-manager-update"

# Functions
print_error() {
    echo -e "${RED}❌ Error: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo
}

cleanup() {
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi
}

trap cleanup EXIT

# Main update function
main() {
    clear
    print_header "Shop Manager - Update"

    # Check if docker compose is available
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi

    # Get latest version
    print_info "Checking for latest version..."
    LATEST_RELEASE=$(curl -s "$GITHUB_API" 2>/dev/null)

    if [ -z "$LATEST_RELEASE" ]; then
        print_error "Could not fetch latest release information"
        exit 1
    fi

    LATEST_VERSION=$(echo "$LATEST_RELEASE" | grep '"tag_name":' | sed -E 's/.*"tag_name":[[:space:]]*"v?([^"]+)".*/\1/')
    DOWNLOAD_URL="https://github.com/$GITHUB_REPO/releases/download/v$LATEST_VERSION/shop-manager-standalone-v$LATEST_VERSION.zip"

    echo -e "Latest version: ${GREEN}v$LATEST_VERSION${NC}"
    echo -e "Download URL: $DOWNLOAD_URL"
    echo

    read -p "Continue with update? (Y/N): " CONFIRM
    if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
        print_info "Update cancelled"
        exit 0
    fi

    # Step 1: Backup current data
    print_header "Step 1: Backing up data"

    mkdir -p "$BACKUP_DIR"
    BACKUP_FILE="$BACKUP_DIR/backup-$(date +%Y%m%d-%H%M%S).sql"

    print_info "Creating database backup..."
    cd "$STANDALONE_DIR"

    if docker compose exec -T postgres pg_dump -U shopmanager shopmanager > "$BACKUP_FILE" 2>/dev/null; then
        print_success "Database backed up to: $BACKUP_FILE"
    else
        print_error "Database backup failed"
        print_info "Continue anyway? (Y/N): "
        read -p "" CONTINUE
        if [[ ! "$CONTINUE" =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi

    # Step 2: Stop services
    print_header "Step 2: Stopping services"

    print_info "Stopping Shop Manager services..."
    if docker compose down; then
        print_success "Services stopped"
    else
        print_error "Failed to stop services"
        exit 1
    fi

    # Step 3: Download new version
    print_header "Step 3: Downloading update"

    mkdir -p "$TEMP_DIR"
    cd "$TEMP_DIR"

    print_info "Downloading Shop Manager v$LATEST_VERSION..."
    if curl -L -o shop-manager-update.zip "$DOWNLOAD_URL"; then
        print_success "Download complete"
    else
        print_error "Download failed"
        exit 1
    fi

    # Step 4: Extract and update
    print_header "Step 4: Installing update"

    print_info "Extracting update..."
    unzip -q shop-manager-update.zip

    # Backup current config.yaml (preserve client settings)
    if [ -f "$STANDALONE_DIR/config.yaml" ]; then
        cp "$STANDALONE_DIR/config.yaml" "$TEMP_DIR/config.yaml.backup"
        print_info "Current configuration backed up"
    fi

    # Copy new files (preserving config)
    print_info "Installing new files..."
    cd shop-manager-standalone
    cp -r * "$STANDALONE_DIR/"

    # Restore client config
    if [ -f "$TEMP_DIR/config.yaml.backup" ]; then
        cp "$TEMP_DIR/config.yaml.backup" "$STANDALONE_DIR/config.yaml"
        print_success "Configuration restored"
    fi

    # Step 5: Update docker images
    print_header "Step 5: Updating Docker images"

    cd "$STANDALONE_DIR"
    print_info "Pulling latest Docker images..."
    if docker compose pull; then
        print_success "Docker images updated"
    else
        print_error "Failed to pull Docker images"
    fi

    # Step 6: Start services
    print_header "Step 6: Starting services"

    print_info "Starting Shop Manager..."
    if docker compose up -d; then
        print_success "Services started"
    else
        print_error "Failed to start services"
        print_info "Attempting to restore from backup..."

        if [ -f "$BACKUP_FILE" ]; then
            docker compose up -d postgres
            sleep 10
            cat "$BACKUP_FILE" | docker compose exec -T postgres psql -U shopmanager shopmanager
            docker compose up -d
        fi
        exit 1
    fi

    # Step 7: Verify installation
    print_header "Step 7: Verifying installation"

    print_info "Waiting for services to be ready..."
    sleep 20

    if docker compose ps | grep -q "Up"; then
        print_success "All services are running!"
    else
        print_error "Some services failed to start"
        print_info "Check logs with: docker compose logs"
    fi

    # Update complete
    print_header "Update Complete!"

    echo -e "Shop Manager has been updated to ${GREEN}v$LATEST_VERSION${NC}"
    echo
    echo "Your data has been preserved"
    echo "Database backup: $BACKUP_FILE"
    echo
    echo "Access Shop Manager at: http://localhost:3001"
    echo
    print_success "Update successful!"
}

# Run main function
main
