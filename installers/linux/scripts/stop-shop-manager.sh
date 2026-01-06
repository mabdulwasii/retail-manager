#!/bin/bash
# ============================================================================
# Shop Manager - Linux Stop Script
# ============================================================================
# This script stops all running Shop Manager instances
# ============================================================================

set -e

echo "========================================="
echo "Shop Manager Stop Script"
echo "========================================="
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo "Running as root"
    IS_ROOT=true
else
    echo "Running as regular user"
    IS_ROOT=false
fi

echo ""

# Stop systemd service
echo "Checking for Shop Manager systemd service..."
if systemctl list-units --full --all | grep -q "shop-manager.service"; then
    if systemctl is-active --quiet shop-manager 2>/dev/null; then
        echo "[INFO] Shop Manager service is running"
        if [ "$IS_ROOT" = true ]; then
            echo "[ACTION] Stopping service..."
            systemctl stop shop-manager || true
            sleep 2
            echo "[OK] Service stopped"
        else
            echo "[WARN] Need root privileges to stop service. Run with sudo."
        fi
    else
        echo "[INFO] Shop Manager service is not running"
    fi
else
    echo "[INFO] No Shop Manager service found"
fi

echo ""

# Stop standalone JAR processes
echo "Checking for standalone Shop Manager processes..."
if pgrep -f "shop-manager.*\.jar" >/dev/null 2>&1; then
    echo "[INFO] Found running Shop Manager processes"
    echo "[ACTION] Stopping processes gracefully..."
    pkill -TERM -f "shop-manager.*\.jar" 2>/dev/null || true
    sleep 3

    # Check if still running
    if pgrep -f "shop-manager.*\.jar" >/dev/null 2>&1; then
        echo "[WARN] Processes still running, force stopping..."
        pkill -KILL -f "shop-manager.*\.jar" 2>/dev/null || true
        sleep 1
    fi
    echo "[OK] Shop Manager processes stopped"
else
    echo "[INFO] No standalone Shop Manager processes found"
fi

echo ""

# Stop embedded PostgreSQL
echo "Checking for embedded PostgreSQL..."
POSTGRES_STOPPED=false

# Check in current user's home
POSTGRES_PID_FILE="$HOME/.shopmanager/data/postgres/postmaster.pid"
if [ -f "$POSTGRES_PID_FILE" ]; then
    POSTGRES_PID=$(head -n 1 "$POSTGRES_PID_FILE" 2>/dev/null || echo "")
    if [ -n "$POSTGRES_PID" ] && ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
        echo "[INFO] Found PostgreSQL process (PID: $POSTGRES_PID)"
        echo "[ACTION] Stopping PostgreSQL..."
        kill "$POSTGRES_PID" 2>/dev/null || true
        sleep 2

        # Force kill if still running
        if ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
            echo "[WARN] Force stopping PostgreSQL..."
            kill -9 "$POSTGRES_PID" 2>/dev/null || true
            sleep 1
        fi
        rm -f "$POSTGRES_PID_FILE"
        echo "[OK] PostgreSQL stopped"
        POSTGRES_STOPPED=true
    fi
fi

# If root, check other users' home directories
if [ "$IS_ROOT" = true ] && [ "$POSTGRES_STOPPED" = false ]; then
    for user_home in /home/*; do
        POSTGRES_PID_FILE="$user_home/.shopmanager/data/postgres/postmaster.pid"
        if [ -f "$POSTGRES_PID_FILE" ]; then
            POSTGRES_PID=$(head -n 1 "$POSTGRES_PID_FILE" 2>/dev/null || echo "")
            if [ -n "$POSTGRES_PID" ] && ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
                echo "[INFO] Found PostgreSQL in $user_home (PID: $POSTGRES_PID)"
                echo "[ACTION] Stopping PostgreSQL..."
                kill "$POSTGRES_PID" 2>/dev/null || true
                sleep 2

                if ps -p "$POSTGRES_PID" >/dev/null 2>&1; then
                    kill -9 "$POSTGRES_PID" 2>/dev/null || true
                    sleep 1
                fi
                rm -f "$POSTGRES_PID_FILE"
                echo "[OK] PostgreSQL stopped"
                POSTGRES_STOPPED=true
            fi
        fi
    done
fi

if [ "$POSTGRES_STOPPED" = false ]; then
    echo "[INFO] No embedded PostgreSQL found"
fi

echo ""

# Clean up PID files
echo "Cleaning up PID files..."
CLEANED=false

if [ -f "$HOME/.shopmanager/shop-manager.pid" ]; then
    rm -f "$HOME/.shopmanager/shop-manager.pid"
    echo "[OK] Removed $HOME/.shopmanager/shop-manager.pid"
    CLEANED=true
fi

if [ "$IS_ROOT" = true ]; then
    if [ -f /var/run/shop-manager.pid ]; then
        rm -f /var/run/shop-manager.pid
        echo "[OK] Removed /var/run/shop-manager.pid"
        CLEANED=true
    fi

    for user_home in /home/*; do
        if [ -f "$user_home/.shopmanager/shop-manager.pid" ]; then
            rm -f "$user_home/.shopmanager/shop-manager.pid"
            echo "[OK] Removed $user_home/.shopmanager/shop-manager.pid"
            CLEANED=true
        fi
    done
fi

if [ "$CLEANED" = false ]; then
    echo "[INFO] No PID files to clean"
fi

echo ""

# Verify ports are free
echo "Verifying ports..."

if lsof -i :8081 -sTCP:LISTEN >/dev/null 2>&1; then
    echo "[WARN] Port 8081 is still in use:"
    lsof -i :8081 -sTCP:LISTEN
else
    echo "[OK] Port 8081 is available"
fi

if lsof -i :5433 -sTCP:LISTEN >/dev/null 2>&1; then
    echo "[WARN] Port 5433 is still in use:"
    lsof -i :5433 -sTCP:LISTEN
else
    echo "[OK] Port 5433 is available"
fi

echo ""
echo "========================================="
echo "Shop Manager stop completed"
echo "========================================="
