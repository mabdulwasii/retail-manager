import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/UnifiedAuthContext';
import configService from '@/config/runtime-config';
import { Permission } from '@/types/permissions';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  AlertCircle,
  AlertTriangle,
  CheckCircle,
  Cloud,
  Database,
  Globe,
  Loader2,
  Lock,
  RefreshCw,
  Save,
  Server,
  Settings as SettingsIcon,
} from 'lucide-react';
import api from '@/lib/axios';
import { toast } from 'sonner';

interface SystemSetting {
  id: string;
  key: string;
  value: string;
  category: 'SYSTEM' | 'DOMAIN' | 'SYNC' | 'STORAGE' | 'SECURITY' | 'DATABASE';
  dataType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON' | 'ENCRYPTED';
  description: string;
  requiresRestart: boolean;
  isSensitive: boolean;
  defaultValue: string;
  isModified: boolean;
  updatedBy?: string;
  updatedAt?: string;
  version: number;
}

interface GroupedSettings {
  SYSTEM?: SystemSetting[];
  DOMAIN?: SystemSetting[];
  SYNC?: SystemSetting[];
  STORAGE?: SystemSetting[];
  SECURITY?: SystemSetting[];
  DATABASE?: SystemSetting[];
}

export const SystemSettingsPage: React.FC = () => {
  const { hasAnyPermission } = useAuth();
  const [settings, setSettings] = useState<GroupedSettings>({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [modifiedSettings, setModifiedSettings] = useState<Map<string, string>>(new Map());
  const [requiresRestart, setRequiresRestart] = useState(false);

  // Check if user has permission to view/update settings
  const canView = hasAnyPermission([
    Permission.SYSTEM_SETTING_VIEW,
    Permission.SYSTEM_SETTING_UPDATE,
    Permission.SYSTEM_SETTING_MANAGE,
  ]);

  const canUpdate = hasAnyPermission([
    Permission.SYSTEM_SETTING_UPDATE,
    Permission.SYSTEM_SETTING_MANAGE,
  ]);

  // Hide in cloud mode
  if (configService.authMode === 'keycloak') {
    return (
      <div className="container mx-auto py-8 max-w-4xl">
        <Alert>
          <Cloud className="h-4 w-4" />
          <AlertDescription>
            System settings are managed via Kubernetes ConfigMaps and Secrets in cloud mode.
            Please contact your platform administrator to modify configuration.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  // Check permissions
  if (!canView) {
    return (
      <div className="container mx-auto py-8 max-w-4xl">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            You do not have permission to view system settings.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await api.get<GroupedSettings>('/settings/grouped');
      setSettings(response.data);
    } catch (err: any) {
      console.error('Failed to fetch settings:', err);
      setError(err.response?.data?.message || 'Failed to load system settings');
      toast.error('Failed to load settings');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSettingChange = (key: string, value: string) => {
    const newModified = new Map(modifiedSettings);
    newModified.set(key, value);
    setModifiedSettings(newModified);
  };

  const handleSaveCategory = async (category: keyof GroupedSettings) => {
    try {
      setSaving(true);
      const categorySettings = settings[category] || [];

      // Only update modified settings in this category
      const updates: Record<string, string> = {};
      categorySettings.forEach((setting) => {
        if (modifiedSettings.has(setting.key)) {
          updates[setting.key] = modifiedSettings.get(setting.key)!;
        }
      });

      if (Object.keys(updates).length === 0) {
        toast.info('No changes to save');
        return;
      }

      const response = await api.put<{
        settings: SystemSetting[];
        requiresRestart: boolean;
        message: string;
        restartCommand: string;
      }>('/settings/bulk', { updates });

      // Clear modified settings for this category
      const newModified = new Map(modifiedSettings);
      categorySettings.forEach((setting) => {
        newModified.delete(setting.key);
      });
      setModifiedSettings(newModified);

      // Refresh settings
      await fetchSettings();

      if (response.data.requiresRestart) {
        setRequiresRestart(true);
        toast.warning(response.data.message, {
          description: `Run: ${response.data.restartCommand}`,
          duration: 10000,
        });
      } else {
        toast.success(response.data.message);
      }
    } catch (err: any) {
      console.error('Failed to save settings:', err);
      toast.error(err.response?.data?.message || 'Failed to save settings');
    } finally {
      setSaving(false);
    }
  };

  const renderSettingInput = (setting: SystemSetting) => {
    const currentValue = modifiedSettings.get(setting.key) ?? setting.value;

    switch (setting.dataType) {
      case 'BOOLEAN':
        return (
          <div className="flex items-center space-x-2">
            <Switch
              checked={currentValue === 'true'}
              onCheckedChange={(checked) =>
                handleSettingChange(setting.key, checked.toString())
              }
              disabled={!canUpdate}
            />
            <Label>{currentValue === 'true' ? 'Enabled' : 'Disabled'}</Label>
          </div>
        );

      case 'NUMBER':
        return (
          <Input
            type="number"
            value={currentValue}
            onChange={(e) => handleSettingChange(setting.key, e.target.value)}
            disabled={!canUpdate}
          />
        );

      case 'ENCRYPTED':
        return (
          <div className="relative">
            <Input
              type="password"
              value={currentValue}
              onChange={(e) => handleSettingChange(setting.key, e.target.value)}
              disabled={!canUpdate}
              placeholder={setting.isSensitive ? '********' : ''}
            />
            <Lock className="absolute right-3 top-2.5 h-4 w-4 text-muted-foreground" />
          </div>
        );

      case 'STRING':
      default:
        return (
          <Input
            type="text"
            value={currentValue}
            onChange={(e) => handleSettingChange(setting.key, e.target.value)}
            disabled={!canUpdate}
          />
        );
    }
  };

  const renderCategoryContent = (
    category: keyof GroupedSettings,
    title: string,
    description: string,
    icon: React.ReactNode
  ) => {
    const categorySettings = settings[category] || [];
    const hasModifications = categorySettings.some((s) => modifiedSettings.has(s.key));

    return (
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              {icon}
              <div>
                <CardTitle>{title}</CardTitle>
                <CardDescription>{description}</CardDescription>
              </div>
            </div>
            {hasModifications && canUpdate && (
              <Button
                onClick={() => handleSaveCategory(category)}
                disabled={isSaving}
              >
                {isSaving ? (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                ) : (
                  <Save className="mr-2 h-4 w-4" />
                )}
                Save Changes
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {categorySettings.length === 0 ? (
            <p className="text-sm text-muted-foreground">No settings available in this category.</p>
          ) : (
            categorySettings.map((setting) => (
              <div key={setting.id} className="space-y-2">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <Label htmlFor={setting.key} className="font-medium">
                        {setting.key.split('.').pop()?.replace(/_/g, ' ').toUpperCase()}
                      </Label>
                      {setting.requiresRestart && (
                        <Badge variant="outline" className="text-xs">
                          <AlertTriangle className="mr-1 h-3 w-3" />
                          Restart Required
                        </Badge>
                      )}
                      {setting.isModified && (
                        <Badge variant="secondary" className="text-xs">
                          Modified
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground mt-1">
                      {setting.description}
                    </p>
                  </div>
                </div>
                <div className="mt-2">{renderSettingInput(setting)}</div>
                {setting.defaultValue && (
                  <p className="text-xs text-muted-foreground">
                    Default: {setting.defaultValue}
                  </p>
                )}
                {modifiedSettings.has(setting.key) && (
                  <Alert className="mt-2">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription className="text-xs">
                      Unsaved changes. Click "Save Changes" to apply.
                    </AlertDescription>
                  </Alert>
                )}
                <Separator className="mt-4" />
              </div>
            ))
          )}
        </CardContent>
      </Card>
    );
  };

  if (isLoading) {
    return (
      <div className="container mx-auto py-8 max-w-6xl">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin" />
          <span className="ml-2">Loading system settings...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mx-auto py-8 max-w-4xl">
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-8 max-w-6xl space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">System Settings</h1>
          <p className="text-muted-foreground mt-1">
            Manage Docker Lite configuration and preferences
          </p>
        </div>
        <Button variant="outline" onClick={fetchSettings}>
          <RefreshCw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Restart Warning */}
      {requiresRestart && (
        <Alert variant="destructive">
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>
            <strong>Container restart required!</strong> Some settings have been updated that require a restart to take effect.
            <br />
            Run: <code className="font-mono">docker-compose -f docker-compose-lite.yml restart</code>
          </AlertDescription>
        </Alert>
      )}

      {/* Settings Tabs */}
      <Tabs defaultValue="domain" className="w-full">
        <TabsList className="grid w-full grid-cols-6">
          <TabsTrigger value="domain">
            <Globe className="mr-2 h-4 w-4" />
            Domain
          </TabsTrigger>
          <TabsTrigger value="sync">
            <Cloud className="mr-2 h-4 w-4" />
            Sync
          </TabsTrigger>
          <TabsTrigger value="storage">
            <Server className="mr-2 h-4 w-4" />
            Storage
          </TabsTrigger>
          <TabsTrigger value="security">
            <Lock className="mr-2 h-4 w-4" />
            Security
          </TabsTrigger>
          <TabsTrigger value="database">
            <Database className="mr-2 h-4 w-4" />
            Database
          </TabsTrigger>
          <TabsTrigger value="system">
            <SettingsIcon className="mr-2 h-4 w-4" />
            System
          </TabsTrigger>
        </TabsList>

        <TabsContent value="domain" className="mt-6">
          {renderCategoryContent(
            'DOMAIN',
            'Domain Configuration',
            'Configure custom domain and network settings',
            <Globe className="h-5 w-5" />
          )}
        </TabsContent>

        <TabsContent value="sync" className="mt-6">
          {renderCategoryContent(
            'SYNC',
            'Cloud Synchronization',
            'Manage cloud sync settings for RetailHQ Cloud',
            <Cloud className="h-5 w-5" />
          )}
        </TabsContent>

        <TabsContent value="storage" className="mt-6">
          {renderCategoryContent(
            'STORAGE',
            'Storage Configuration',
            'File storage and upload settings',
            <Server className="h-5 w-5" />
          )}
        </TabsContent>

        <TabsContent value="security" className="mt-6">
          {renderCategoryContent(
            'SECURITY',
            'Security Settings',
            'Authentication and token configuration',
            <Lock className="h-5 w-5" />
          )}
        </TabsContent>

        <TabsContent value="database" className="mt-6">
          {renderCategoryContent(
            'DATABASE',
            'Database Settings',
            'Backup and maintenance configuration',
            <Database className="h-5 w-5" />
          )}
        </TabsContent>

        <TabsContent value="system" className="mt-6">
          {renderCategoryContent(
            'SYSTEM',
            'System Configuration',
            'General application settings',
            <SettingsIcon className="h-5 w-5" />
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
};
