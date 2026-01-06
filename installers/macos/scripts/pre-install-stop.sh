#!/bin/bash
# ============================================================================
# Shop Manager - macOS Pre-Install Stop Script
# ============================================================================
# This script stops any running Shop Manager instances before installation
# to prevent port conflicts and file locks during updates.
# ============================================================================

set -e

# Configuration
PID_FILE="$HOME/.shopmanager/shop-manager.pid"
POSTGRES_PID_FILE="$HOME/.shopmanager/data/postgres/postmaster.pid"
BACKEND_PORT=8081
POSTGRES_PORT=5433
GRACEFUL_TIMEOUT=5

echo "========================================="
echo "Shop Manager Pre-Install Cleanup"
echo "========================================="

# Function to check if a port is in use
is_port_in_use() {
    local port=$1
    lsof -i ":$port" -sTCP:LISTEN -t >/dev/null 2>&1
}

# Function to stop Shop Manager Java process
stop_shop_manager() {
    echo "🔍 Checking for running Shop Manager instances..."

    # Check PID file first
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE" 2>/dev/null || echo "")
        if [ -n "$PID" ] && ps -p "$PID" > /dev/null 2>&1; then
            echo "📌 Found Shop Manager process (PID: $PID)"
            echo "⏳ Attempting graceful shutdown..."

            kill "$PID" 2>/dev/null || true

            # Wait for graceful shutdown
            for i in $(seq 1 $GRACEFUL_TIMEOUT); do
                if ! ps -p "$PID" > /dev/null 2>&1; then
                    echo "✅ Shop Manager stopped gracefully"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done

            # Force kill if still running
            echo "⚠️  Graceful shutdown timeout, forcing termination..."
            kill -9 "$PID" 2>/dev/null || true
            sleep 1
            rm -f "$PID_FILE"
            echo "✅ Shop Manager force stopped"
        else
            # Stale PID file
            echo "🗑️  Removing stale PID file"
            rm -f "$PID_FILE"
        fi
    fi

    # Also check for any shop-manager JAR processes without PID file
    SHOP_PIDS=$(pgrep -f "shop-manager.*\.jar" 2>/dev/null || echo "")
    if [ -n "$SHOP_PIDS" ]; then
        echo "📌 Found additional Shop Manager processes: $SHOP_PIDS"
        echo "⏳ Stopping additional processes..."
        pkill -TERM -f "shop-manager.*\.jar" 2>/dev/null || true
        sleep 2

        # Force kill if still running
        if pgrep -f "shop-manager.*\.jar" >/dev/null 2>&1; then
            echo "⚠️  Force stopping remaining processes..."
            pkill -9 -f "shop-manager.*\.jar" 2>/dev/null || true
            sleep 1
        fi
        echo "✅ Additional processes stopped"
    fi
}

# Function to stop embedded PostgreSQL
stop_postgres() {
    echo "🔍 Checking for embedded PostgreSQL..."

    # Check PostgreSQL PID file
    if [ -f "$POSTGRES_PID_FILE" ]; then
        POSTGRES_PID=$(head -n 1 "$POSTGRES_PID_FILE" 2>/dev/null || echo "")
        if [ -n "$POSTGRES_PID" ] && ps -p "$POSTGRES_PID" > /dev/null 2>&1; then
            echo "📌 Found PostgreSQL process (PID: $POSTGRES_PID)"
            echo "⏳ Stopping PostgreSQL..."

            kill "$POSTGRES_PID" 2>/dev/null || true
            sleep 2

            # Force kill if still running
            if ps -p "$POSTGRES_PID" > /dev/null 2>&1; then
                echo "⚠️  Force stopping PostgreSQL..."
                kill -9 "$POSTGRES_PID" 2>/dev/null || true
                sleep 1
            fi
            echo "✅ PostgreSQL stopped"
        fi
        rm -f "$POSTGRES_PID_FILE"
    fi

    # Check for any postgres processes on our port
    POSTGRES_PIDS=$(lsof -ti ":$POSTGRES_PORT" 2>/dev/null || echo "")
    if [ -n "$POSTGRES_PIDS" ]; then
        echo "📌 Found PostgreSQL processes on port $POSTGRES_PORT: $POSTGRES_PIDS"
        echo "⏳ Stopping PostgreSQL processes..."
        for pid in $POSTGRES_PIDS; do
            kill "$pid" 2>/dev/null || true
        done
        sleep 2

        # Force kill if still running
        POSTGRES_PIDS=$(lsof -ti ":$POSTGRES_PORT" 2>/dev/null || echo "")
        if [ -n "$POSTGRES_PIDS" ]; then
            echo "⚠️  Force stopping PostgreSQL..."
            for pid in $POSTGRES_PIDS; do
                kill -9 "$pid" 2>/dev/null || true
            done
            sleep 1
        fi
        echo "✅ PostgreSQL processes stopped"
    fi
}

# Function to verify ports are free
verify_ports_free() {
    echo "🔍 Verifying ports are available..."

    local ports_ok=true

    if is_port_in_use $BACKEND_PORT; then
        echo "❌ Port $BACKEND_PORT is still in use"
        lsof -i ":$BACKEND_PORT"
        ports_ok=false
    else
        echo "✅ Port $BACKEND_PORT is available"
    fi

    if is_port_in_use $POSTGRES_PORT; then
        echo "❌ Port $POSTGRES_PORT is still in use"
        lsof -i ":$POSTGRES_PORT"
        ports_ok=false
    else
        echo "✅ Port $POSTGRES_PORT is available"
    fi

    if [ "$ports_ok" = false ]; then
        echo ""
        echo "⚠️  WARNING: Some ports are still in use."
        echo "    Installation may fail due to port conflicts."
        echo "    Please close any applications using ports $BACKEND_PORT or $POSTGRES_PORT"
        echo ""
        return 1
    fi

    return 0
}

# Main execution
main() {
    stop_shop_manager
    stop_postgres
    verify_ports_free

    echo ""
    echo "========================================="
    echo "✅ Pre-install cleanup completed"
    echo "Ready for Shop Manager installation"
    echo "========================================="
    echo ""
}

main "$@"
