#!/bin/bash
set -e

echo "Configuring local DNS for Shop Manager on Docker Desktop..."

# Check if running on docker-desktop
CURRENT_CONTEXT=$(kubectl config current-context)
if [ "$CURRENT_CONTEXT" != "docker-desktop" ]; then
    echo "Warning: Current context is $CURRENT_CONTEXT, not docker-desktop"
    echo "Switch to docker-desktop with: kubectx docker-desktop"
fi

# Get ingress controller NodePort mappings
HTTP_PORT=$(kubectl get svc ingress-nginx-controller -n ingress-nginx -o jsonpath='{.spec.ports[?(@.name=="http")].nodePort}')
HTTPS_PORT=$(kubectl get svc ingress-nginx-controller -n ingress-nginx -o jsonpath='{.spec.ports[?(@.name=="https")].nodePort}')

echo "Ingress Controller Ports:"
echo "  HTTP:  $HTTP_PORT"
echo "  HTTPS: $HTTPS_PORT"

# Create /etc/hosts entries
echo ""
echo "Add these entries to your /etc/hosts file:"
echo "# Shop Manager Services - Docker Desktop Ingress"
echo "127.0.0.1 shop-manager.local"
echo "127.0.0.1 api.shop-manager.local"
echo "127.0.0.1 auth.shop-manager.local"

echo ""
echo "Or run this command to add them automatically:"
echo "sudo tee -a /etc/hosts << 'EOF'"
echo "# Shop Manager Services - Docker Desktop Ingress"
echo "127.0.0.1 shop-manager.local"
echo "127.0.0.1 api.shop-manager.local"
echo "127.0.0.1 auth.shop-manager.local"
echo "EOF"

echo ""
echo "✅ Ingress Configuration Complete!"
echo ""
echo "🚀 Access your services:"
echo "   Frontend:  https://shop-manager.local:$HTTPS_PORT"
echo "   Backend:   https://api.shop-manager.local:$HTTPS_PORT/actuator/health"
echo "   Keycloak:  https://auth.shop-manager.local:$HTTPS_PORT (when available)"
echo ""

# Test connectivity
echo "🧪 Testing connectivity..."
FRONTEND_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" https://shop-manager.local:$HTTPS_PORT/ 2>/dev/null || echo "000")
BACKEND_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" https://api.shop-manager.local:$HTTPS_PORT/actuator/health 2>/dev/null || echo "000")
KEYCLOAK_STATUS=$(curl -k -s -o /dev/null -w "%{http_code}" https://auth.shop-manager.local:$HTTPS_PORT/ 2>/dev/null || echo "000")

echo "   ✅ Frontend:  $FRONTEND_STATUS (https://shop-manager.local:$HTTPS_PORT)"
echo "   ✅ Backend:   $BACKEND_STATUS (https://api.shop-manager.local:$HTTPS_PORT/actuator/health)"
if [ "$KEYCLOAK_STATUS" = "200" ]; then
    echo "   ✅ Keycloak:  $KEYCLOAK_STATUS (https://auth.shop-manager.local:$HTTPS_PORT)"
else
    echo "   ❌ Keycloak:  $KEYCLOAK_STATUS (https://auth.shop-manager.local:$HTTPS_PORT) - Service may not be running"
fi

echo ""
echo "📋 Service Status:"
kubectl get pods -n shop-manager-fixed

echo ""
echo "🔧 Alternative access via NodePort (no DNS needed):"
echo "   Frontend:  http://localhost:30436"
echo "   Backend:   http://localhost:30081/actuator/health"
echo "   Ingress HTTP:  http://localhost:$HTTP_PORT"
echo "   Ingress HTTPS: https://localhost:$HTTPS_PORT"

echo ""
echo "🔍 Troubleshooting:"
echo "   • If services show 503 errors, check pod status: kubectl get pods -n shop-manager-fixed"
echo "   • If DNS doesn't work, verify /etc/hosts entries above"
echo "   • For Keycloak issues, check StatefulSet: kubectl get sts -n shop-manager-fixed"
echo "   • Use -k flag with curl for self-signed certificates: curl -k https://..."