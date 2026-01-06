#!/bin/bash
# ============================================================================
# Shop Manager - RPM Package Pre-Installation Script
# ============================================================================
# This script runs BEFORE the RPM package is installed/upgraded
# ============================================================================

set -e

# $1 = 1: Initial installation
# $1 = 2: Upgrade

if [ "$1" = "2" ]; then
    # Upgrade - stop the service
    echo "Shop Manager: Preparing for upgrade..."

    # Stop systemd service if running
    if systemctl is-active --quiet shop-manager 2>/dev/null; then
        echo "Shop Manager: Stopping service..."
        systemctl stop shop-manager || true
        sleep 2
    fi

    # Stop any running processes
    if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
        echo "Shop Manager: Stopping running processes..."
        pkill -TERM -f "shop-manager.*embedded.jar" || true
        sleep 3

        # Force kill if still running
        if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
            pkill -KILL -f "shop-manager.*embedded.jar" || true
            sleep 1
        fi
    fi

    echo "Shop Manager: Ready for upgrade"
fi

exit 0
