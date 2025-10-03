#!/bin/bash

# Fully automated access script - no /etc/hosts modification needed
# This script sets up port-forwarding to access services without DNS changes

echo "🚀 Starting fully automated service access (no /etc/hosts changes needed)..."

# Kill any existing port-forwards
echo "🧹 Cleaning up existing port-forwards..."
pkill -f "kubectl port-forward.*shop-manager" 2>/dev/null || true
pkill -f "kubectl port-forward.*gomco" 2>/dev/null || true

# Wait a moment for cleanup
sleep 2

echo "🔗 Setting up port-forwards..."

# Start port-forwards in background
kubectl port-forward -n shop-manager service/gomco-services-backend 8081:80 &
BACKEND_PID=$!

kubectl port-forward -n shop-manager service/gomco-services-keycloak 8080:80 &
KEYCLOAK_PID=$!

kubectl port-forward -n shop-manager service/gomco-services-frontend 3001:3000 &
FRONTEND_PID=$!

# Wait a moment for port-forwards to establish
sleep 3

echo ""
echo "✅ Services are now accessible at:"
echo "   🖥️  Frontend:    http://localhost:3001"
echo "   📊 Backend API: http://localhost:8081"
echo "   🔐 Keycloak:    http://localhost:8080"
echo ""
echo "🎯 Quick access URLs:"
echo "   • Frontend App:     http://localhost:3001"
echo "   • API Health:       http://localhost:8081/api/actuator/health"
echo "   • Keycloak Admin:   http://localhost:8080"
echo "   • Keycloak Realm:   http://localhost:8080/realms/gomco-services"
echo ""
echo "📝 Credentials (Development Only):"
echo "   • Admin: admin@shopmanager.com / admin123"
echo "   • Manager: manager@shopmanager.com / manager123"
echo "   • Keycloak Admin: admin / GomcoAdmin2024!"
echo ""
echo "⏹️  To stop all port-forwards, run: pkill -f 'kubectl port-forward'"
echo "📋 Process IDs: Backend=$BACKEND_PID, Keycloak=$KEYCLOAK_PID, Frontend=$FRONTEND_PID"