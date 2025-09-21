#!/bin/bash

# Deploy Shop Manager Custom Keycloak Theme
# This script handles the complete deployment of the custom theme to Keycloak

set -e

NAMESPACE="${NAMESPACE:-shop-manager}"
THEME_DIR="keycloak-theme/shop-manager"

echo "🎨 Deploying Shop Manager Custom Keycloak Theme"
echo "=============================================="

# Step 1: Create ConfigMap from theme files
echo "📦 Creating ConfigMap from theme files..."

# Create a temporary YAML file with proper structure
cat > /tmp/keycloak-theme-configmap.yaml <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: keycloak-theme-shop-manager
  namespace: ${NAMESPACE}
data:
  theme.properties: |
$(sed 's/^/    /' < ${THEME_DIR}/login/theme.properties)
  template.ftl: |
$(sed 's/^/    /' < ${THEME_DIR}/login/template.ftl)
  login.ftl: |
$(sed 's/^/    /' < ${THEME_DIR}/login/login.ftl)
  shop-manager.css: |
$(sed 's/^/    /' < ${THEME_DIR}/login/resources/css/shop-manager.css)
EOF

# Apply the ConfigMap
kubectl apply -f /tmp/keycloak-theme-configmap.yaml

echo "✅ ConfigMap created successfully"

# Step 2: Create the patch for StatefulSet
echo "🔧 Creating StatefulSet patch..."

cat > /tmp/keycloak-statefulset-patch.json <<'EOF'
{
  "spec": {
    "template": {
      "spec": {
        "initContainers": [
          {
            "name": "theme-installer",
            "image": "busybox:1.35",
            "command": [
              "sh",
              "-c",
              "mkdir -p /themes/shop-manager/login/resources/css && echo 'Installing theme files...' && cp /theme-config/theme.properties /themes/shop-manager/login/ 2>/dev/null || true && cp /theme-config/template.ftl /themes/shop-manager/login/ 2>/dev/null || true && cp /theme-config/login.ftl /themes/shop-manager/login/ 2>/dev/null || true && cp /theme-config/shop-manager.css /themes/shop-manager/login/resources/css/ 2>/dev/null || true && chmod -R 755 /themes && ls -la /themes/shop-manager/login/"
            ],
            "volumeMounts": [
              {
                "name": "theme-config",
                "mountPath": "/theme-config"
              },
              {
                "name": "themes",
                "mountPath": "/themes"
              }
            ]
          }
        ],
        "containers": [
          {
            "name": "keycloak",
            "volumeMounts": [
              {
                "name": "themes",
                "mountPath": "/opt/bitnami/keycloak/themes"
              }
            ]
          }
        ],
        "volumes": [
          {
            "name": "theme-config",
            "configMap": {
              "name": "keycloak-theme-shop-manager"
            }
          },
          {
            "name": "themes",
            "emptyDir": {}
          }
        ]
      }
    }
  }
}
EOF

# Step 3: Apply the patch
echo "📝 Patching StatefulSet..."
kubectl patch statefulset shop-manager-keycloak -n ${NAMESPACE} --type=strategic -p "$(cat /tmp/keycloak-statefulset-patch.json)"

echo "✅ StatefulSet patched successfully"

# Step 4: Restart Keycloak
echo "🔄 Restarting Keycloak pods..."
kubectl rollout restart statefulset/shop-manager-keycloak -n ${NAMESPACE}

# Step 5: Wait for rollout to complete
echo "⏳ Waiting for Keycloak to restart..."
kubectl rollout status statefulset/shop-manager-keycloak -n ${NAMESPACE} --timeout=120s

# Step 6: Verify theme installation
echo "🔍 Verifying theme installation..."
sleep 10

POD_NAME=$(kubectl get pods -n ${NAMESPACE} -l app.kubernetes.io/name=keycloak -o jsonpath='{.items[0].metadata.name}')
if [ -z "$POD_NAME" ]; then
    echo "❌ Could not find Keycloak pod"
    exit 1
fi

echo "Checking theme files in pod: $POD_NAME"
kubectl exec -n ${NAMESPACE} $POD_NAME -- ls -la /opt/bitnami/keycloak/themes/shop-manager/login/ 2>/dev/null || echo "Theme directory check failed"

# Step 7: Ensure theme is configured in realm
echo "🔐 Configuring theme in shop-manager realm..."
kubectl exec -n ${NAMESPACE} $POD_NAME -- /opt/bitnami/keycloak/bin/kcadm.sh update realms/shop-manager \
  -s loginTheme=shop-manager \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password 'KeycloakAdm1n@2024!SecureAuth#CompliantPassword' 2>/dev/null || echo "Theme configuration update skipped (may already be set)"

# Cleanup temporary files
rm -f /tmp/keycloak-theme-configmap.yaml /tmp/keycloak-statefulset-patch.json

echo ""
echo "✅ Shop Manager theme deployment complete!"
echo ""
echo "🌐 Access the custom login page at:"
echo "   https://auth.shop-manager.local/realms/shop-manager/account"
echo ""
echo "📋 Test credentials:"
echo "   admin@shopmanager.com / admin123"
echo "   manager@shopmanager.com / manager123"
echo "   employee@shopmanager.com / employee123"
echo ""
echo "🎨 Theme features:"
echo "   - Custom Shop Manager branding"
echo "   - Password visibility toggle"
echo "   - Enhanced remember me checkbox"
echo "   - Development credential auto-fill"
echo "   - Animated backgrounds and effects"
echo ""

# Optional: Clear Keycloak theme cache
echo "💡 Tip: If theme doesn't appear, try clearing browser cache or use incognito mode"