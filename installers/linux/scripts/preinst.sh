#!/bin/bash
# ============================================================================
# Shop Manager - Debian Package Pre-Installation Script
# ============================================================================
# This script runs BEFORE the package is installed/upgraded
# It stops the running service to allow clean file replacement
# ============================================================================

set -e

# Only run on upgrade (not fresh install)
if [ "$1" = "upgrade" ]; then
    echo "Shop Manager: Preparing for upgrade..."

    # Stop systemd service if it exists and is running
    if systemctl is-active --quiet shop-manager 2>/dev/null; then
        echo "Shop Manager: Stopping service..."
        systemctl stop shop-manager || true
        sleep 2
    fi

    # Stop any running Shop Manager processes
    if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
        echo "Shop Manager: Stopping running processes..."
        pkill -TERM -f "shop-manager.*embedded.jar" || true
        sleep 3

        # Force kill if still running
        if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
            echo "Shop Manager: Force stopping stubborn processes..."
            pkill -KILL -f "shop-manager.*embedded.jar" || true
            sleep 1
        fi
    fi

    echo "Shop Manager: Ready for upgrade"
fi

exit 0
