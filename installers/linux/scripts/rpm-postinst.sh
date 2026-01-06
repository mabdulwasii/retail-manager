#!/bin/bash
# ============================================================================
# Shop Manager - RPM Package Post-Installation Script
# ============================================================================
# This script runs AFTER the RPM package is installed/upgraded
# ============================================================================

set -e

# $1 = 1: Initial installation
# $1 = 2: Upgrade

# Create system user if needed
if ! getent passwd shop-manager >/dev/null; then
    echo "Shop Manager: Creating system user..."
    useradd --system --no-create-home --shell /bin/false shop-manager || true
fi

# Set ownership and permissions
if [ -d /opt/shop-manager ]; then
    chown -R shop-manager:shop-manager /opt/shop-manager || true
    chmod 755 /opt/shop-manager || true
fi

if [ -d /var/lib/shop-manager ]; then
    chown -R shop-manager:shop-manager /var/lib/shop-manager || true
    chmod 755 /var/lib/shop-manager || true
fi

# Reload systemd and start service
if [ -f /etc/systemd/system/shop-manager.service ]; then
    echo "Shop Manager: Reloading systemd daemon..."
    systemctl daemon-reload || true

    # Enable on boot
    systemctl enable shop-manager || true

    if [ "$1" = "1" ]; then
        # Fresh install - start service
        echo "Shop Manager: Starting service..."
        systemctl start shop-manager || true
    elif [ "$1" = "2" ]; then
        # Upgrade - restart service
        echo "Shop Manager: Restarting service after upgrade..."
        systemctl restart shop-manager || true
    fi
fi

echo "Shop Manager: Installation/upgrade complete"

exit 0
