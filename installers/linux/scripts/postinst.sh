#!/bin/bash
# ============================================================================
# Shop Manager - Debian Package Post-Installation Script
# ============================================================================
# This script runs AFTER the package is installed/upgraded
# It restarts the service if it was running before
# ============================================================================

set -e

case "$1" in
    configure)
        # Create shop-manager system user if it doesn't exist
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

        # Reload systemd daemon if service file exists
        if [ -f /etc/systemd/system/shop-manager.service ]; then
            echo "Shop Manager: Reloading systemd daemon..."
            systemctl daemon-reload || true

            # Enable service on boot
            echo "Shop Manager: Enabling service..."
            systemctl enable shop-manager || true

            # Start or restart service
            if systemctl is-enabled --quiet shop-manager 2>/dev/null; then
                echo "Shop Manager: Starting service..."
                systemctl start shop-manager || true
            fi
        fi

        echo "Shop Manager: Installation/upgrade complete"
        ;;

    abort-upgrade|abort-remove|abort-deconfigure)
        # Restart service if upgrade was aborted
        if systemctl is-enabled --quiet shop-manager 2>/dev/null; then
            echo "Shop Manager: Restarting service after aborted upgrade..."
            systemctl start shop-manager || true
        fi
        ;;
esac

exit 0
