#!/bin/bash
#
# Script to update existing Keycloak users from camelCase to snake_case attributes
# This fixes the issue where JWT tokens don't contain tenant_id and shop_id claims
#
# Usage: ./fix-keycloak-user-attributes.sh
#

set -e

REALM="${KEYCLOAK_REALM:-retail}"
KEYCLOAK_URL="${KEYCLOAK_URL:-https://auth.retail.gomco.com}"
ADMIN_USER="${KEYCLOAK_ADMIN_USER:-admin}"

echo "============================================"
echo "Keycloak User Attribute Migration Script"
echo "============================================"
echo ""
echo "This script will update user attributes from:"
echo "  tenantId -> tenant_id"
echo "  shopId   -> shop_id"
echo ""
echo "Realm: $REALM"
echo "Keycloak URL: $KEYCLOAK_URL"
echo ""

# Check if running in Kubernetes
if kubectl get pods -n gomco | grep -q keycloak; then
    echo "Detected Kubernetes environment"
    echo ""
    echo "Please run this command manually in the Keycloak pod:"
    echo ""
    echo "kubectl exec -it \$(kubectl get pods -n gomco -l app.kubernetes.io/name=keycloak -o jsonpath='{.items[0].metadata.name}') -n gomco -- bash -c '"
    echo ""
    echo "# Login to Keycloak admin CLI"
    echo "/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user \$KEYCLOAK_ADMIN --password \$KEYCLOAK_ADMIN_PASSWORD"
    echo ""
    echo "# Get all users in the realm"
    echo "/opt/keycloak/bin/kcadm.sh get users -r $REALM --fields id,username,attributes > /tmp/users.json"
    echo ""
    echo "# For each user, update attributes (requires jq)"
    echo "/opt/keycloak/bin/kcadm.sh get users -r $REALM | jq -r '.[] | .id' | while read USER_ID; do"
    echo "  echo \"Processing user: \$USER_ID\""
    echo "  "
    echo "  # Get user details"
    echo "  USER_JSON=\$(/opt/keycloak/bin/kcadm.sh get users/\$USER_ID -r $REALM)"
    echo "  "
    echo "  # Extract tenantId and shopId (camelCase)"
    echo "  TENANT_ID=\$(echo \$USER_JSON | jq -r '.attributes.tenantId[0] // empty')"
    echo "  SHOP_ID=\$(echo \$USER_JSON | jq -r '.attributes.shopId[0] // empty')"
    echo "  "
    echo "  # Update to snake_case if camelCase exists"
    echo "  if [ -n \"\$TENANT_ID\" ]; then"
    echo "    /opt/keycloak/bin/kcadm.sh update users/\$USER_ID -r $REALM -s 'attributes.tenant_id=[\"\$TENANT_ID\"]'"
    echo "    /opt/keycloak/bin/kcadm.sh update users/\$USER_ID -r $REALM -s 'attributes.tenantId=[]'"
    echo "    echo \"  Updated tenant_id for user \$USER_ID\""
    echo "  fi"
    echo "  "
    echo "  if [ -n \"\$SHOP_ID\" ]; then"
    echo "    /opt/keycloak/bin/kcadm.sh update users/\$USER_ID -r $REALM -s 'attributes.shop_id=[\"\$SHOP_ID\"]'"
    echo "    /opt/keycloak/bin/kcadm.sh update users/\$USER_ID -r $REALM -s 'attributes.shopId=[]'"
    echo "    echo \"  Updated shop_id for user \$USER_ID\""
    echo "  fi"
    echo "done"
    echo ""
    echo "echo \"Migration complete!\""
    echo "'"
    echo ""
else
    echo "ERROR: Not running in Kubernetes environment"
    echo "Please run this script in the Kubernetes cluster where Keycloak is deployed"
    exit 1
fi
