import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Key } from 'lucide-react';

/**
 * API Keys Management Page
 * Manage API keys for local shop sync
 *
 * Phase 1: Empty state
 * Phase 2: API key display (masked), regenerate, copy, usage stats
 */

export const ApiKeysPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <Key className="h-8 w-8" />
          API Keys
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage API keys for cloud synchronization
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>API Key Management</CardTitle>
          <CardDescription>Configure keys for local shop cloud sync</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Key className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">API Keys Coming Soon</h3>
            <p className="text-muted-foreground max-w-md">
              API key display, regeneration, and usage statistics
              will be available in Phase 2.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ApiKeysPage;
