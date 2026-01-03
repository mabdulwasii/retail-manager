import React, { useState } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Key, Copy, Trash2, RefreshCw, Calendar, Activity } from 'lucide-react';
import { ApiKey } from '@/services/cloudApiKeysService';
import { format } from 'date-fns';

interface ApiKeyCardProps {
  apiKey: ApiKey;
  onRevoke: (keyId: string) => void;
  onRegenerate: (keyId: string) => void;
  onViewUsage: (keyId: string) => void;
}

/**
 * API Key Card Component
 * Displays a single API key with actions
 */
export const ApiKeyCard: React.FC<ApiKeyCardProps> = ({
  apiKey,
  onRevoke,
  onRegenerate,
  onViewUsage,
}) => {
  const [showRevokeDialog, setShowRevokeDialog] = useState(false);
  const [showRegenerateDialog, setShowRegenerateDialog] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleCopyKey = () => {
    navigator.clipboard.writeText(apiKey.maskedKey);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleRevoke = () => {
    onRevoke(apiKey.id);
    setShowRevokeDialog(false);
  };

  const handleRegenerate = () => {
    onRegenerate(apiKey.id);
    setShowRegenerateDialog(false);
  };

  return (
    <>
      <Card className={!apiKey.isActive ? 'opacity-60' : ''}>
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            {/* Key Info */}
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-3">
                <div className="p-2 bg-blue-100 rounded-lg">
                  <Key className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-semibold text-lg">{apiKey.description}</h3>
                    <Badge variant={apiKey.isActive ? 'default' : 'secondary'}>
                      {apiKey.isActive ? 'Active' : 'Revoked'}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground font-mono">
                    {apiKey.maskedKey}
                  </p>
                </div>
              </div>

              {/* Metadata */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4 text-sm">
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Calendar className="h-4 w-4" />
                  <span>
                    Created: {format(new Date(apiKey.createdAt), 'MMM d, yyyy')}
                  </span>
                </div>
                {apiKey.lastUsedAt && (
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Activity className="h-4 w-4" />
                    <span>
                      Last used: {format(new Date(apiKey.lastUsedAt), 'MMM d, yyyy')}
                    </span>
                  </div>
                )}
                {apiKey.expiresAt && (
                  <div className="flex items-center gap-2 text-orange-600">
                    <Calendar className="h-4 w-4" />
                    <span>
                      Expires: {format(new Date(apiKey.expiresAt), 'MMM d, yyyy')}
                    </span>
                  </div>
                )}
                <div className="flex items-center gap-2 text-muted-foreground">
                  <Activity className="h-4 w-4" />
                  <span>{apiKey.usageCount.toLocaleString()} requests</span>
                </div>
              </div>

              {/* Permissions */}
              <div className="mt-3">
                <p className="text-xs text-muted-foreground mb-1">Permissions:</p>
                <div className="flex flex-wrap gap-1">
                  {apiKey.permissions.map((permission) => (
                    <Badge key={permission} variant="outline" className="text-xs">
                      {permission}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>

            {/* Actions */}
            <div className="flex flex-col gap-2 ml-4">
              <Button
                variant="outline"
                size="sm"
                onClick={handleCopyKey}
                disabled={!apiKey.isActive}
              >
                <Copy className="h-4 w-4 mr-1" />
                {copied ? 'Copied!' : 'Copy'}
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => onViewUsage(apiKey.id)}
                disabled={!apiKey.isActive}
              >
                <Activity className="h-4 w-4 mr-1" />
                Usage
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowRegenerateDialog(true)}
                disabled={!apiKey.isActive}
              >
                <RefreshCw className="h-4 w-4 mr-1" />
                Regenerate
              </Button>
              <Button
                variant="destructive"
                size="sm"
                onClick={() => setShowRevokeDialog(true)}
                disabled={!apiKey.isActive}
              >
                <Trash2 className="h-4 w-4 mr-1" />
                Revoke
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Revoke Confirmation Dialog */}
      <AlertDialog open={showRevokeDialog} onOpenChange={setShowRevokeDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Revoke API Key?</AlertDialogTitle>
            <AlertDialogDescription>
              This action cannot be undone. The API key "{apiKey.description}" will be
              immediately revoked and all requests using this key will fail.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleRevoke} className="bg-red-600 hover:bg-red-700">
              Revoke Key
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Regenerate Confirmation Dialog */}
      <AlertDialog open={showRegenerateDialog} onOpenChange={setShowRegenerateDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Regenerate API Key?</AlertDialogTitle>
            <AlertDialogDescription>
              This will create a new API key with the same permissions and revoke the current
              one. You'll need to update all systems using the old key.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleRegenerate}>
              Regenerate Key
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
};

export default ApiKeyCard;
