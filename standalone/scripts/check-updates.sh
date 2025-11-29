#!/bin/bash

# Shop Manager - Check for Updates
# Queries GitHub API for latest release and compares with current version

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# GitHub repository (update with your actual repo)
GITHUB_REPO="yourorg/shop-manager"
GITHUB_API="https://api.github.com/repos/$GITHUB_REPO/releases/latest"

# Get current version from config.yaml
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="$SCRIPT_DIR/../config.yaml"

if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: config.yaml not found at $CONFIG_FILE"
    exit 1
fi

# Extract version from config.yaml (assumes version: "1.0.0" format)
CURRENT_VERSION=$(grep -E "^version:" "$CONFIG_FILE" | sed -E 's/version:[[:space:]]*"?([^"]+)"?/\1/')

if [ -z "$CURRENT_VERSION" ]; then
    echo "Error: Could not determine current version"
    exit 1
fi

echo -e "${BLUE}Shop Manager Update Checker${NC}"
echo "=========================================="
echo -e "Current version: ${GREEN}v$CURRENT_VERSION${NC}"
echo

# Query GitHub API for latest release
echo "Checking for updates..."

LATEST_RELEASE=$(curl -s "$GITHUB_API" 2>/dev/null)

if [ -z "$LATEST_RELEASE" ]; then
    echo "Error: Could not fetch latest release information"
    echo "Please check your internet connection or GitHub API status"
    exit 1
fi

# Extract latest version
LATEST_VERSION=$(echo "$LATEST_RELEASE" | grep '"tag_name":' | sed -E 's/.*"tag_name":[[:space:]]*"v?([^"]+)".*/\1/')

if [ -z "$LATEST_VERSION" ]; then
    echo "Error: Could not parse latest version"
    exit 1
fi

echo -e "Latest version:  ${GREEN}v$LATEST_VERSION${NC}"
echo

# Compare versions
if [ "$CURRENT_VERSION" == "$LATEST_VERSION" ]; then
    echo -e "${GREEN}✅ You are running the latest version!${NC}"
    exit 0
fi

# Version comparison function
version_gt() {
    test "$(printf '%s\n' "$@" | sort -V | head -n 1)" != "$1"
}

if version_gt "$LATEST_VERSION" "$CURRENT_VERSION"; then
    echo -e "${YELLOW}🎉 A new version is available!${NC}"
    echo
    echo "What's new:"
    echo "$LATEST_RELEASE" | grep '"body":' | sed -E 's/.*"body":[[:space:]]*"(.*)".*/\1/' | head -c 200
    echo "..."
    echo
    echo "To update, run:"
    echo -e "  ${BLUE}./scripts/update.sh${NC}"
    echo
    echo "Or download manually from:"
    echo "  https://github.com/$GITHUB_REPO/releases/tag/v$LATEST_VERSION"
else
    echo -e "${GREEN}✅ You are running the latest version!${NC}"
    echo "(Your version is newer than the latest release)"
fi
