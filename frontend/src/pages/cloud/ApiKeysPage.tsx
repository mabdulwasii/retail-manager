import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Key, Plus, AlertCircle } from 'lucide-react';
import { useApiKeys, useCreateApiKey, useRevokeApiKey, useRegenerateApiKey } from '@/hooks/useCloudApiKeys';
import { ApiKeyCard } from '@/components/cloud/ApiKeyCard';
import { CreateApiKeyDialog } from '@/components/cloud/CreateApiKeyDialog';
import { CreateApiKeyRequest } from '@/services/cloudApiKeysService';

/**
 * API Keys Management Page
 * Manage API keys for local shop sync with full CRUD operations
 */

export const ApiKeysPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [selectedKeyIdForUsage, setSelectedKeyIdForUsage] = useState<string | null>(null);

  // Fetch API keys
  const { data: apiKeys, isLoading, error } = useApiKeys(tenantId);

  // Mutations
  const createApiKey = useCreateApiKey();
  const revokeApiKey = useRevokeApiKey();
  const regenerateApiKey = useRegenerateApiKey();

  const handleCreateApiKey = async (request: CreateApiKeyRequest) => {
    return createApiKey.mutateAsync(request);
  };

  const handleRevokeApiKey = async (keyId: string) => {
    await revokeApiKey.mutateAsync({ tenantId, keyId });
  };

  const handleRegenerateApiKey = async (keyId: string) => {
    await regenerateApiKey.mutateAsync({ tenantId, keyId });
  };

  const handleViewUsage = (keyId: string) => {
    setSelectedKeyIdForUsage(keyId);
    // TODO: Implement usage stats modal
    alert(`View usage stats for key: ${keyId}`);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Key className="h-8 w-8" />
            API Keys
          </h1>
          <p className="text-muted-foreground mt-2">
            Manage API keys for cloud synchronization
          </p>
        </div>
        <Button onClick={() => setIsCreateDialogOpen(true)}>
          <Plus className="h-4 w-4 mr-2" />
          Create API Key
        </Button>
      </div>

      {/* Error State */}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            Failed to load API keys. Please try again later.
          </AlertDescription>
        </Alert>
      )}

      {/* Loading State */}
      {isLoading && (
        <Card>
          <CardContent className="py-12">
            <div className="flex flex-col items-center justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              <p className="text-muted-foreground mt-4">Loading API keys...</p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Empty State */}
      {!isLoading && !error && (!apiKeys || apiKeys.length === 0) && (
        <Card>
          <CardHeader>
            <CardTitle>No API Keys</CardTitle>
            <CardDescription>Get started by creating your first API key</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col items-center justify-center py-8 text-center">
              <Key className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">Create Your First API Key</h3>
              <p className="text-muted-foreground max-w-md mb-4">
                API keys allow your local shop instances to securely sync data with the cloud.
                Create a key to get started.
              </p>
              <Button onClick={() => setIsCreateDialogOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                Create API Key
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* API Keys List */}
      {!isLoading && !error && apiKeys && apiKeys.length > 0 && (
        <div className="space-y-4">
          <div className="text-sm text-muted-foreground">
            {apiKeys.filter((k) => k.isActive).length} active key(s) •{' '}
            {apiKeys.filter((k) => !k.isActive).length} revoked
          </div>
          {apiKeys.map((apiKey) => (
            <ApiKeyCard
              key={apiKey.id}
              apiKey={apiKey}
              onRevoke={handleRevokeApiKey}
              onRegenerate={handleRegenerateApiKey}
              onViewUsage={handleViewUsage}
            />
          ))}
        </div>
      )}

      {/* Security Notice */}
      <Alert>
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>
          <strong>Security Best Practices:</strong> Never share API keys publicly or commit them
          to version control. Rotate keys regularly and revoke any keys that may have been
          compromised.
        </AlertDescription>
      </Alert>

      {/* Create API Key Dialog */}
      <CreateApiKeyDialog
        isOpen={isCreateDialogOpen}
        onClose={() => setIsCreateDialogOpen(false)}
        tenantId={tenantId}
        onSubmit={handleCreateApiKey}
      />
    </div>
  );
};

export default ApiKeysPage;
