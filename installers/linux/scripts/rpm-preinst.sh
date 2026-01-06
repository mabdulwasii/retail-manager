#!/bin/bash
# ============================================================================
# Shop Manager - RPM Package Pre-Installation Script
# ============================================================================
# This script runs BEFORE the RPM package is installed/upgraded
# ============================================================================

set -e

# $1 = 1: Initial installation
# $1 = 2: Upgrade

stop_shop_manager() {
    echo "Shop Manager: Stopping running instances..."

    # Stop systemd service if running
    if systemctl is-active --quiet shop-manager 2>/dev/null; then
        echo "Shop Manager: Stopping systemd service..."
        systemctl stop shop-manager || true
        sleep 2
    fi

    # Stop any running Shop Manager processes
    if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
        echo "Shop Manager: Stopping standalone JAR processes..."
        pkill -TERM -f "shop-manager.*embedded.jar" || true
        sleep 3

        # Force kill if still running
        if pgrep -f "shop-manager.*embedded.jar" >/dev/null 2>&1; then
            echo "Shop Manager: Force stopping stubborn processes..."
            pkill -KILL -f "shop-manager.*embedded.jar" || true
            sleep 1
        fi
    fi

    # Stop embedded PostgreSQL processes
    for user_home in /home/*; do
        POSTGRES_PID_FILE="$user_home/.shopmanager/data/postgres/postmaster.pid"
        if [ -f "$POSTGRES_PID_FILE" ]; then
            POSTGRES_PID=$(head -n 1 "$POSTGRES_PID_FILE" 2>/dev/null || echo "")
            if [ -n "$POSTGRES_PID" ] && ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
                echo "Shop Manager: Stopping embedded PostgreSQL (PID: $POSTGRES_PID)..."
                kill "$POSTGRES_PID" 2>/dev/null || true
                sleep 2

                # Force kill if still running
                if ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
                    kill -9 "$POSTGRES_PID" 2>/dev/null || true
                    sleep 1
                fi
            fi
            rm -f "$POSTGRES_PID_FILE"
        fi
    done

    # Clean up PID files
    for user_home in /home/*; do
        [ -f "$user_home/.shopmanager/shop-manager.pid" ] && rm -f "$user_home/.shopmanager/shop-manager.pid"
    done
    [ -f /var/run/shop-manager.pid ] && rm -f /var/run/shop-manager.pid

    echo "Shop Manager: Stop completed"
}

if [ "$1" = "2" ]; then
    # Upgrade - stop the service
    echo "Shop Manager: Preparing for upgrade..."
    stop_shop_manager
    echo "Shop Manager: Ready for upgrade"
elif [ "$1" = "1" ]; then
    # Fresh install - also stop in case of manual installation
    echo "Shop Manager: Preparing for fresh installation..."
    stop_shop_manager
fi

exit 0
