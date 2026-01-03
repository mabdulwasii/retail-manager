import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Settings } from 'lucide-react';

/**
 * Tenant Settings Page
 * Manage tenant configuration and preferences
 *
 * TODO Phase 6.1: Implement full settings management
 * - Company information
 * - Timezone and locale settings
 * - Notification preferences
 * - Feature flags
 * - Security settings
 */

export const TenantSettingsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <Settings className="h-8 w-8" />
          Tenant Settings
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage your tenant configuration and preferences
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Settings Management</CardTitle>
          <CardDescription>Configure tenant-wide settings</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Settings className="h-16 w-16 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">Settings Coming in Phase 6.1</h3>
            <p className="text-muted-foreground max-w-md">
              Tenant settings will include company information, timezone configuration,
              notification preferences, and security settings.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TenantSettingsPage;
